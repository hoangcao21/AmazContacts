package group.amazcontacts;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import group.amazcontacts.activity.MainActivity;
import group.amazcontacts.adapter.PhoneInputAdapter;
import group.amazcontacts.model.AmazTheme;
import group.amazcontacts.model.PhoneNumber;

public class AddNewContactActivity extends AppCompatActivity {
    private ActionBar mActionBar;
    private EditText editTextName, editTextPhone;
    private Spinner spinnerType;
    private ListView listViewPhone;
    private ArrayList<PhoneNumber> listPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_contact);
        mapping();
        setUpView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_new_contact_menu, menu);

//        MenuItem itemSave = menu.findItem(R.id.action_save);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                String name = editTextName.getText().toString();
                String phone = editTextPhone.getText().toString();
                String type = spinnerType.getSelectedItem().toString();
                int typeInt = PhoneNumber.getPhoneTypeInt(type);
                if (name.isEmpty() || phone.isEmpty()) Toast.makeText(this, "You have to fill in all the fields!", Toast.LENGTH_LONG).show();
                else {
                    addContact(name, phone, type);
                    Intent intent = new Intent();
                    setResult(MainActivity.RESULT_CODE_FROM_ADD_NEW, intent);
                    finish();
                }
                break;
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addTypeListToSpinner(ArrayList<String> list) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, list);
        spinnerType.setAdapter(adapter);
    }

    public ArrayList<String> generateTypes() {
        return PhoneNumber.autoGenerateTypeStringList();
    }

    private void setUpView() {
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);
        initializeTheme();
        addTypeListToSpinner(generateTypes());
    }

    private void mapping(){
        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        spinnerType = findViewById(R.id.spinnerType);
        listViewPhone = findViewById(R.id.listViewPhone);
        mActionBar = getSupportActionBar();

        listPhoneNumber = new ArrayList<>();
        listPhoneNumber.add(new PhoneNumber("2", "0343241728"));
        PhoneInputAdapter phoneInputAdapter = new PhoneInputAdapter(listPhoneNumber, generateTypes(), this);
        listViewPhone.setAdapter(phoneInputAdapter);
    }

    private void initializeTheme() {
        SharedPreferences pref = getSharedPreferences("theme", Context.MODE_PRIVATE);
        int colorFromPref = pref.getInt("themeColor", AmazTheme.BLUE_ACCENT);
        int colorDrawable = ContextCompat.getColor(getApplicationContext(), colorFromPref);
        mActionBar.setBackgroundDrawable(new ColorDrawable(colorDrawable));
        mActionBar.setTitle(Html.fromHtml("<font color=\"black\">" + getString(R.string.app_name) + "</font>"));
    }

    private void addContact(String name, String phone, String type) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        int rawContactInsertIndex = ops.size();
        int rawContactPhoneType = PhoneNumber.getPhoneTypeInt(type);

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name) // Name of the person
                .build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone) // Number of the person
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, rawContactPhoneType).build()); // Type of phone number
        try
        {
            ContentProviderResult[] res = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        }
        catch (Exception e) {
            Toast.makeText(this, "Some error occurred!", Toast.LENGTH_SHORT).show();
        }
    }
}
