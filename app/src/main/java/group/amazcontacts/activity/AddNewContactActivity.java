package group.amazcontacts.activity;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
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
import group.amazcontacts.model.PhoneNumber;

public class AddNewContactActivity extends AppCompatActivity {
    private ActionBar mActionBar;
    private EditText editTextName;
    private Button buttonAddPhoneNumber;
    private ListView listViewPhone;
    private ArrayList<PhoneNumber> listPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_contact);
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
                boolean allValueFilled = true, hasPhoneNumber = true;
                ArrayList<String> phoneList, typeList;
                phoneList = new ArrayList<>();
                typeList = new ArrayList<>();
                String name = editTextName.getText().toString();
                if (listViewPhone.getChildCount() == 0) {
                    hasPhoneNumber = false;
                    Toast.makeText(this, "Please add at least one phone number", Toast.LENGTH_LONG).show();
                } else if (name.isEmpty()) {
                    allValueFilled = false;
                }
                for (int i = 0; i < listViewPhone.getChildCount(); i++) {
                    View v = listViewPhone.getChildAt(i);
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
                }

                if (!allValueFilled)
                    Toast.makeText(this, "You have to fill in all the fields!", Toast.LENGTH_LONG).show();
                else if (hasPhoneNumber) {
                    try {
                        addToContactList(this, name, phoneList, typeList);
                    } catch (Exception e) {
                        Toast.makeText(this, "Some error occurred!", Toast.LENGTH_SHORT).show();
                    }
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


    public ArrayList<String> generateTypes() {
        return PhoneNumber.autoGenerateTypeStringList();
    }

    private void setUpView() {
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);
        initializeTheme();
//        addTypeListToSpinner(generateTypes());
    }

    private void setUpEvents() {
        buttonAddPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listPhoneNumber.size() < 5) {
                    listPhoneNumber.add(new PhoneNumber("2", ""));
                    refreshListView();
                } else {
                    Toast.makeText(AddNewContactActivity.this, "Each contact can only have 5 numbers!", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    private void mapping() {
        editTextName = findViewById(R.id.editTextName);
        buttonAddPhoneNumber = findViewById(R.id.buttonAddPhoneNumber);
        listViewPhone = findViewById(R.id.listViewPhone);
        mActionBar = getSupportActionBar();

        listPhoneNumber = new ArrayList<>();
        refreshListView();
    }

    private void refreshListView() {
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

    public static void addToContactList(Context context, String strDisplayName, ArrayList<String> phoneList, ArrayList<String> typeList) throws Exception {

        ArrayList<ContentProviderOperation> cntProOper = new ArrayList<>();
        int contactIndex = cntProOper.size();//ContactSize
        ContentResolver contactHelper = context.getContentResolver();

        cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)//Step1
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

        //Display name will be inserted in ContactsContract.Data table
        cntProOper.add(ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)//Step2
                .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                .withValue(android.provider.ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, strDisplayName) // Name of the contact
                .build());

        for (int i = 0; i < Math.min(phoneList.size(), typeList.size()); i++) {
            String phone = phoneList.get(i);
            String type = typeList.get(i);

            int rawContactPhoneType = PhoneNumber.getPhoneTypeInt(type);
            //Mobile number will be inserted in ContactsContract.Data table
            cntProOper.add(ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)//Step 3
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                    .withValue(android.provider.ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone) // Number to be added
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, rawContactPhoneType).build()); //Type like HOME, MOBILE etc
        }

        ContentProviderResult[] s = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cntProOper); //apply above data insertion into contacts list

    }

}