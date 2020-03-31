package group.amazcontacts.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import group.amazcontacts.R;
import group.amazcontacts.adapter.ContactDetailAdapter;
import group.amazcontacts.fragment.ContactsFragment;
import group.amazcontacts.model.AmazTheme;
import group.amazcontacts.model.Contact;
import group.amazcontacts.service.ContactDatabaseHandler;

public class ContactDetailActivity extends AppCompatActivity {

    private ImageView avatar;
    private TextView name;
    private ImageButton buttonEditContact;
    private ListView phoneListView;
    private ActionBar mActionBar;
    private SharedPreferences pref;
    private Contact c;
    private MenuItem item;

    public static int REQUEST_CODE_TO_EDIT_CONTACT = 400;
    public static int RESULT_CODE_FROM_EDIT_CONTACT = 401;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
        mapping();
        setUpView();
        setUpEvents();
    }
    private void setUpView(){
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);
        initializeTheme();
        Intent i = getIntent();
        if ( i == null ){
            return;
        }
        // get contact
        c = (Contact) i.getSerializableExtra("contact");
        loadContact();
    }

    private void loadContact() {
        // get avatar
        Glide.with(getApplicationContext()).
                load(c.getAvatar_url()).
                into(avatar);

        name.setText(c.getName());
        ContactDetailAdapter contactDetailAdapter = new ContactDetailAdapter(c.getPhoneNumbers() , this);
        phoneListView.setAdapter(contactDetailAdapter);
    }

    private void setUpEvents(){
        buttonEditContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToEditContact = new Intent(getApplicationContext(), EditContactActivity.class);
                intentToEditContact.putExtra("contactToEdit", c);
                startActivityForResult(intentToEditContact, REQUEST_CODE_TO_EDIT_CONTACT);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.change_favorite:
                handleMarkFavorite(item);
        }
        return true;
    }
    private void handleMarkFavorite(MenuItem item){
        try {
            String contactID = c.getId();
            ContactDatabaseHandler contactDatabaseHandler = new ContactDatabaseHandler(ContactDetailActivity.this);
            int newStarred = c.isFavored() ? 0 : 1 ;
            c.setFavored(!c.isFavored());
            if(c.isFavored()){
                item.setIcon(R.drawable.baseline_favorite_24);
            }else{
                item.setIcon(R.drawable.baseline_favorite_border_black_24); // HoangCH đổi icon cho phù hợp bối cảnh thay vì sử dụng icon add khi bỏ check favorite
            }
            String result = contactDatabaseHandler.setContactStarById(contactID, newStarred);
            Toast.makeText(getApplicationContext(), "Result "+result+" ",Toast.LENGTH_LONG).show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.contact_detail_menu, menu);
        MenuItem item = menu.findItem(R.id.change_favorite);

        if(c.isFavored()){
            item.setIcon(R.drawable.baseline_favorite_24);
        }else{
            item.setIcon(R.drawable.baseline_favorite_border_black_24); // HoangCH đổi icon cho phù hợp bối cảnh thay vì sử dụng icon add khi bỏ check favorite
        }

        return true;
    }


    private void mapping(){
        avatar = findViewById(R.id.contact_avatar);
        name = findViewById(R.id.contact_name);
        buttonEditContact = findViewById(R.id.buttonEditContact);
        phoneListView = findViewById(R.id.phone_number_listView);
        mActionBar = getSupportActionBar();
        item = findViewById(R.id.change_favorite);
        c = new Contact();
    }

    private void initializeTheme() {
        pref = getSharedPreferences("theme", Context.MODE_PRIVATE);
        int colorFromPref = pref.getInt("themeColor", AmazTheme.BLUE_ACCENT);
        int colorDrawable = ContextCompat.getColor(getApplicationContext(), colorFromPref);
        mActionBar.setBackgroundDrawable(new ColorDrawable(colorDrawable));
        mActionBar.setTitle(Html.fromHtml("<font color=\"black\">" + getString(R.string.app_name) + "</font>"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_TO_EDIT_CONTACT && resultCode == RESULT_CODE_FROM_EDIT_CONTACT) {
            if (data != null && data.getSerializableExtra("newContact") != null) {
                //get contact
                c = (Contact) data.getSerializableExtra("newContact");
                loadContact();
            }
        }
    }
}
