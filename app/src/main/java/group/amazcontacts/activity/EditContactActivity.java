package group.amazcontacts.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import group.amazcontacts.R;
import group.amazcontacts.adapter.PhoneInputAdapter;
import group.amazcontacts.model.AmazTheme;
import group.amazcontacts.model.Contact;
import group.amazcontacts.model.PhoneNumber;

public class EditContactActivity extends AppCompatActivity {
    private ActionBar mActionBar;
    private EditText editTextNameEditContact;
    private Button buttonAddPhoneNumberEditContact;
    private ListView listViewPhoneEdit;
    private ArrayList<PhoneNumber> listPhoneNumber;
    private Contact contact;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);
        mapping();
        setUpView();
        setUpEvents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_new_contact_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                save();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpView() {
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);
        initializeTheme();
        setUpValues();
    }

    private void setUpValues() {
        contact = new Contact();
        Intent i = getIntent();
        if ( i == null ) return;
        contact = (Contact) i.getSerializableExtra("contactToEdit");
        editTextNameEditContact.setText(contact.getName());
        listPhoneNumber = (ArrayList<PhoneNumber>) contact.getPhoneNumbers();
        refreshListView();
    }

    private void initializeTheme() {
        SharedPreferences pref = getSharedPreferences("theme", Context.MODE_PRIVATE);
        int colorFromPref = pref.getInt("themeColor", AmazTheme.BLUE_ACCENT);
        int colorDrawable = ContextCompat.getColor(getApplicationContext(), colorFromPref);
        mActionBar.setBackgroundDrawable(new ColorDrawable(colorDrawable));
        mActionBar.setTitle(Html.fromHtml("<font color=\"black\">" + getString(R.string.app_name) + "</font>"));
    }

    private void setUpEvents() {
        buttonAddPhoneNumberEditContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listPhoneNumber.size() < 5) {
                    listPhoneNumber.add(new PhoneNumber("2", ""));
                    refreshListView();
                } else {
                    Toast.makeText(EditContactActivity.this, "Each contact can only have 5 numbers!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void mapping() {
        editTextNameEditContact = findViewById(R.id.editTextNameEditContact);
        buttonAddPhoneNumberEditContact = findViewById(R.id.buttonAddPhoneNumberEditContact);
        listViewPhoneEdit = findViewById(R.id.listViewPhoneEdit);
        mActionBar = getSupportActionBar();

        listPhoneNumber = new ArrayList<>();
    }

    public ArrayList<String> generateTypes() {
        return PhoneNumber.autoGenerateTypeStringList();
    }

    private void refreshListView() {
        PhoneInputAdapter phoneInputAdapter = new PhoneInputAdapter(listPhoneNumber, generateTypes(), this);
        listViewPhoneEdit.setAdapter(phoneInputAdapter);
    }

    private void save() {
        boolean allValueFilled = true;
        String contactId = contact.getId();
        ArrayList<String> phoneList = new ArrayList<>(), typeList = new ArrayList<>();
        ArrayList<PhoneNumber> newPhoneNumberList = new ArrayList<>();
        Intent intent = new Intent();
        String name = editTextNameEditContact.getText().toString();
        if (listViewPhoneEdit.getChildCount() == 0) {
            Toast.makeText(this, "Please add at least one phone number", Toast.LENGTH_LONG).show();
            return;
        } else if (name.isEmpty()) {
            allValueFilled = false;
        }
        for (int i = 0; i < listViewPhoneEdit.getChildCount(); i++) {
            View v = listViewPhoneEdit.getChildAt(i);
            Spinner sp = v.findViewById(R.id.spinnerPhoneType);
            EditText et = v.findViewById(R.id.editTextPhoneNumber);
            String phone = et.getText().toString();
            String type = sp.getSelectedItem().toString();
            if (phone.isEmpty()) {
                allValueFilled = false;
                break;
            }
            phoneList.add(phone);
            typeList.add(type);
            newPhoneNumberList.add(new PhoneNumber(String.valueOf(PhoneNumber.getPhoneTypeInt(type)), phone));
        }

        if (!allValueFilled) {
            Toast.makeText(this, "You have to fill in all the fields!", Toast.LENGTH_LONG).show();
            return;
        }

        else {
            try {
                Contact oldContact = new Contact();
                oldContact.setAvatar_url(contact.getAvatar_url());
                oldContact.setEmail(contact.getEmail());
                oldContact.setFavored(contact.isFavored());
                deleteContactById(Long.parseLong(contactId));
                int newContactId = AddNewContactActivity.addToContactList(this, name, phoneList, typeList);
                contact.setName(name);
                contact.setPhoneNumbers(newPhoneNumberList);
                contact.setId(String.valueOf(newContactId));
                contact.setEmail(oldContact.getEmail());
                contact.setFavored(oldContact.isFavored());
                contact.setAvatar_url(oldContact.getAvatar_url());
                intent.putExtra("newContact", contact);
            } catch (Exception e) {
                Toast.makeText(this, "Some error occurred!", Toast.LENGTH_SHORT).show();
            }
        }
        setResult(ContactDetailActivity.RESULT_CODE_FROM_EDIT_CONTACT, intent);
        finish();
    }

    private void deleteContactById(final long id) {
        ContentResolver contentResolver = getContentResolver();
        final Cursor cur = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, ContactsContract.Contacts._ID + "="
                + id, null, null);

        if (cur != null) {
            while (cur.moveToNext()) {
                try {
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    cur.moveToFirst(); // Because cursor reach end of the database, which meanes NULL (NO data to get)
                    String lookupKey = cur.getString(cur
                            .getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                            lookupKey);
                    int delete = contentResolver.delete(uri, ContactsContract.Contacts._ID + "=" + id, null);

                } catch (Exception e) {
                    Log.e("TAG", "deleteContactById: ", e);
                }
            }
            cur.close();
        }

    }


}
