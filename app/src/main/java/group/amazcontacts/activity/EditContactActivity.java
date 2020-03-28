package group.amazcontacts.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import group.amazcontacts.R;
import group.amazcontacts.adapter.PhoneInputAdapter;
import group.amazcontacts.model.AmazTheme;
import group.amazcontacts.model.PhoneNumber;

public class EditContactActivity extends AppCompatActivity {
    private ActionBar mActionBar;
    private EditText editTextNameEditContact;
    private Button buttonAddPhoneNumberEditContact;
    private ListView listViewPhoneEdit;
    private ArrayList<PhoneNumber> listPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);
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
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpView() {
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);
        initializeTheme();
//        addTypeListToSpinner(generateTypes());
    }

    private void initializeTheme() {
        SharedPreferences pref = getSharedPreferences("theme", Context.MODE_PRIVATE);
        int colorFromPref = pref.getInt("themeColor", AmazTheme.BLUE_ACCENT);
        int colorDrawable = ContextCompat.getColor(getApplicationContext(), colorFromPref);
        mActionBar.setBackgroundDrawable(new ColorDrawable(colorDrawable));
        mActionBar.setTitle(Html.fromHtml("<font color=\"black\">" + getString(R.string.app_name) + "</font>"));
    }

//    private void setUpEvents() {
//        buttonAddPhoneNumber.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (listPhoneNumber.size() < 5) {
//                    listPhoneNumber.add(new PhoneNumber("2", ""));
//                    refreshListView();
//                } else {
//                    Toast.makeText(AddNewContactActivity.this, "Each contact can only have 5 numbers!", Toast.LENGTH_SHORT).show();
//                }
//
//
//            }
//        });
//    }

    private void mapping() {
        editTextNameEditContact = findViewById(R.id.editTextNameEditContact);
        buttonAddPhoneNumberEditContact = findViewById(R.id.buttonAddPhoneNumberEditContact);
        listViewPhoneEdit = findViewById(R.id.listViewPhoneEdit);
        mActionBar = getSupportActionBar();

        listPhoneNumber = new ArrayList<>();
//        refreshListView();
    }

    public ArrayList<String> generateTypes() {
        return PhoneNumber.autoGenerateTypeStringList();
    }

//    private void refreshListView() {
//        PhoneInputAdapter phoneInputAdapter = new PhoneInputAdapter(listPhoneNumber, generateTypes(), this);
//        listViewPhone.setAdapter(phoneInputAdapter);
//    }
}
