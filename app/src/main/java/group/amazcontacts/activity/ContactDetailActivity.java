package group.amazcontacts.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import group.amazcontacts.R;
import group.amazcontacts.adapter.ContactDetailAdapter;
import group.amazcontacts.model.AmazTheme;
import group.amazcontacts.model.Contact;

public class ContactDetailActivity extends AppCompatActivity {

    private ImageView avatar;
    private TextView name;
    private ListView phoneListView;
    private ActionBar mActionBar;
    private SharedPreferences pref;
    private Contact c = new Contact();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
        mapping();
        setUpView();
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
        // get avatar
        Bitmap bmp;
        byte[] byteArray = getIntent().getByteArrayExtra("avatar");
        bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        avatar.setImageBitmap(bmp);

        name.setText(c.getName());

        ContactDetailAdapter contactDetailAdapter = new ContactDetailAdapter(c.getPhoneNumbers() , this);
        phoneListView.setAdapter(contactDetailAdapter);

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
        item.setChecked(!item.isChecked());
        if(item.isChecked()){

        }else{

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.contact_detail_menu, menu);
        if (c.isFavored()){
            menu.getItem(0).setTitle("Remove from favorite");
        }else{
            menu.getItem(0).setTitle("Add to favorite");
        }
        return true;
    }


    private void mapping(){
        avatar = findViewById(R.id.contact_avatar);
        name = findViewById(R.id.contact_name);
        phoneListView = findViewById(R.id.phone_number_listView);
        mActionBar = getSupportActionBar();
    }

    private void initializeTheme() {
        pref = getSharedPreferences("theme", Context.MODE_PRIVATE);
        int colorFromPref = pref.getInt("themeColor", AmazTheme.BLUE_ACCENT);
        int colorDrawable = ContextCompat.getColor(getApplicationContext(), colorFromPref);
        mActionBar.setBackgroundDrawable(new ColorDrawable(colorDrawable));
        mActionBar.setTitle(Html.fromHtml("<font color=\"black\">" + getString(R.string.app_name) + "</font>"));
    }
}
