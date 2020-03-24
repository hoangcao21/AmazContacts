package group.amazcontacts.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import group.amazcontacts.R;
import group.amazcontacts.adapter.ContactDetailAdapter;
import group.amazcontacts.model.AmazTheme;
import group.amazcontacts.model.Contact;

public class ContactDetailActivity extends AppCompatActivity {

    private ImageView avatar;
    private TextView name;
    private ListView phoneListView;
    private ActionBar mActionBar;
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
        mActionBar.setBackgroundDrawable(getResources().getDrawable(AmazTheme.BLUE_ACCENT));
        Intent i = getIntent();
        if ( i == null ){
            return;
        }
        // get contact
        Contact c = (Contact) i.getSerializableExtra("contact");
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
        }
        return true;
    }

    private void mapping(){
        avatar = findViewById(R.id.contact_avatar);
        name = findViewById(R.id.contact_name);
        phoneListView = findViewById(R.id.phone_number_listView);
        mActionBar = getSupportActionBar();
    }
}
