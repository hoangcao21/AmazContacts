package group.amazcontacts;

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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import group.amazcontacts.model.AmazTheme;
import group.amazcontacts.model.PhoneNumber;

public class AddNewContactActivity extends AppCompatActivity {
    private ActionBar mActionBar;
    private EditText editTextName, editTextPhone;
    private Spinner spinnerType;

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
                else Toast.makeText(this, name + ": " + phone + " (" + type + ", " + typeInt + ")", Toast.LENGTH_LONG).show();
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
        mActionBar = getSupportActionBar();
    }

    private void initializeTheme() {
        SharedPreferences pref = getSharedPreferences("theme", Context.MODE_PRIVATE);
        int colorFromPref = pref.getInt("themeColor", AmazTheme.BLUE_ACCENT);
        int colorDrawable = ContextCompat.getColor(getApplicationContext(), colorFromPref);
        mActionBar.setBackgroundDrawable(new ColorDrawable(colorDrawable));
        mActionBar.setTitle(Html.fromHtml("<font color=\"black\">" + getString(R.string.app_name) + "</font>"));
    }
}
