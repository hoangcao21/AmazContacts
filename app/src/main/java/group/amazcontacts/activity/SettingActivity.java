package group.amazcontacts.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import group.amazcontacts.R;
import group.amazcontacts.adapter.ContactAdapter;
import group.amazcontacts.fragment.ContactsFragment;
import group.amazcontacts.fragment.DialFragment;
import group.amazcontacts.model.AmazTheme;
import group.amazcontacts.model.Contact;
import group.amazcontacts.model.PhoneNumber;

public class SettingActivity extends AppCompatActivity {
    private ImageView profileAvatar;
    private TextView textViewName;
    private TextView textViewEmail;
    private Button btnSignOut;
    private Spinner spinnerTheme;
    public static int THEME_COLOR = 0;


    // Download & Import section
    private Button btnBackupContact;
    private Button btnDownloadContact;

    private List<Contact> contactList;
    private FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;
//        DatabaseReference myRef = database.getReference();
//        myRef.child("contacts").child(currentUser.getUid()).setValue("OK");
//        myRef.child("contacts").child(currentUser.getUid() + "LOL").setValue("OK");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mappingViews();
        setEvents();

        DatabaseReference databaseReference = firebaseDatabase.getReference();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void mappingViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar bar = getSupportActionBar();
        int color = ContextCompat.getColor(getApplicationContext(), R.color.blueAccent);

        bar.setBackgroundDrawable(new ColorDrawable(color));
        bar.setTitle(Html.fromHtml("<font color=\"black\">" + getString(R.string.app_name) + "</font>"));

        Window window = getWindow();

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.transparent));

        //add themes to spinner
        spinnerTheme = findViewById(R.id.spinnerTheme);
        addThemeListToSpinner(generateThemes());

        profileAvatar = findViewById(R.id.imgProfileAvatar);
        textViewName = findViewById(R.id.textViewName);
        textViewEmail = findViewById(R.id.textViewEmail);
        btnSignOut = findViewById(R.id.btnSignOut);

        btnBackupContact = findViewById(R.id.btnBackUp);
        btnDownloadContact = findViewById(R.id.btnDownload);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        textViewName.setText(currentUser.getDisplayName());
        textViewEmail.setText(currentUser.getEmail());
        Glide.with(getApplicationContext()).
                load(currentUser.getPhotoUrl()).
                into(profileAvatar);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        imageView = new ImageView(getApplicationContext());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(100);
            finish();
        }
        return true;
    }

    private void setEvents() {
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(getApplicationContext(), SignUpActivity.class);
                i.putExtra(SignUpActivity.NO_LOGIN_SILENT, false);

                SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("Automatic Login", false);
                editor.commit();

                startActivity(i);
            }
        });

        btnBackupContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isFullPermissions = isFullPermissions();
                if (isFullPermissions) {
                    new BackupContactsTask().execute();
                } else {
                    MainActivity.requestPermissions(1, getApplicationContext(), SettingActivity.this); // No need CALL_PHONE permission
                }
            }
        });

        btnDownloadContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isFullPermissions = isFullPermissions();
                if (isFullPermissions) {
                    new DownloadContactsTask().execute();
                } else {
                    MainActivity.requestPermissions(1, getApplicationContext(), SettingActivity.this); // No need CALL_PHONE permission
                }
            }
        });

        spinnerTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AmazTheme selected = (AmazTheme) spinnerTheme.getItemAtPosition(position);
                SettingActivity.changeTheme(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private boolean isFullPermissions() {
        int READ_CONTACTS_PERMISSION = ContextCompat.checkSelfPermission(SettingActivity.this,
                Manifest.permission.READ_CONTACTS);
        int WRITE_CONTACTS_PERMISSION = ContextCompat.checkSelfPermission(SettingActivity.this,
                Manifest.permission.WRITE_CONTACTS);
        return READ_CONTACTS_PERMISSION != -1 && WRITE_CONTACTS_PERMISSION != -1;
    }

    private ImageView imageView;


    class BackupContactsTask extends AsyncTask<String, String, String> {
        private ContactAdapter contactAdapter;
        private String searchKey;
        private ProgressDialog dialog;

        public BackupContactsTask() {
            this.searchKey = "";
            dialog = new ProgressDialog(SettingActivity.this);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Backing up your contacts on the could, please wait...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) { // Only non-GUI task
            contactList = ContactsFragment.getContacts(Objects.requireNonNull(getApplicationContext()), searchKey);
            final StorageReference storageRef = firebaseStorage.getReference();
            int count = 0;

            DatabaseReference databaseReference = firebaseDatabase.getReference("contacts");

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference itemsRef = databaseReference.child(firebaseUser.getUid()).child("items");
            itemsRef.removeValue();
            for (int i = 0; i < contactList.size(); i++) {
                final Uri[] downloadUri = {null};
                final StorageReference imagesRef = storageRef.child("images" + count);
                Contact contact = contactList.get(i);
                DatabaseReference contactRef = itemsRef.push();
                count++;

                Uri imgUri = Uri.parse(contact.getAvatar_url());
                imageView.setImageURI(null);
                imageView.setImageURI(imgUri);

                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = imagesRef.putBytes(data);

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return imagesRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            downloadUri[0] = task.getResult();
                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });

                while (true) {
                    if (downloadUri[0] != null) {
                        Log.i("BACK UP CONTACTS: Display Name:", contact.getName() + " - Phone Number: " +
                                contact.getPhoneNumbers().get(0).getPhoneNumber() + " - Uploaded Aavatar: " + downloadUri[0].toString());
                        contactRef.child("avatar_url").setValue(downloadUri[0].toString());
                        contactRef.child("name").setValue(contact.getName());
                        contactRef.child("phoneNumbers").setValue(contact.getPhoneNumbers());
                        break;
                    }
                }
            }

            return "OK";
        }

        @Override // GUI task here
        protected void onPostExecute(String s) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if (s.equals("OK"))
                Toast.makeText(getApplicationContext(), "Back up successfully!", Toast.LENGTH_LONG).show();
        }
    }


    class FetchImageFromServerTask extends AsyncTask<String, String, String> {
        private Contact contact;

        public FetchImageFromServerTask(Contact contact) {
            this.contact = contact;
        }

        @Override
        protected String doInBackground(String... urls) {
            URL url = null;
            HttpURLConnection connection = null;
            try {

                url = new URL(contact.getAvatar_url());
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
                fetchImageDone = true;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {

        }
    }

    class DownloadContactsTask extends AsyncTask<String, String, String> {
        private ContactAdapter contactAdapter;
        private ProgressDialog dialog;

        public DownloadContactsTask() {
            dialog = new ProgressDialog(SettingActivity.this);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Download your contacts on the could, please wait...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) { // Only non-GUI task
            contactList = new ArrayList<>();
            DatabaseReference databaseReference = firebaseDatabase.getReference("contacts");
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference itemsRef = databaseReference.child(firebaseUser.getUid()).child("items");
            itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String id = snapshot.getKey(); // ID lấy về hiện tại là làm màu ))
                        String name = snapshot.child("name").getValue(String.class);
                        String avatar_url = snapshot.child("avatar_url").getValue(String.class);
                        List<PhoneNumber> phoneNumbersList = new ArrayList<>();
                        Iterable<DataSnapshot> phoneNumbers = snapshot.child("phoneNumbers").getChildren();

                        for (DataSnapshot phoneNumber : phoneNumbers) {
                            String type = phoneNumber.child("phoneType").getValue(String.class);
                            String number = phoneNumber.child("phoneNumber").getValue(String.class);

                            phoneNumbersList.add(new PhoneNumber(type, number));
                        }

                        Contact contact = new Contact(id, null, avatar_url, name, phoneNumbersList, false);
                        contactList.add(contact);
                        Log.i("DOWNLOAD CONTACTS: ", "Display Name: " + contact.getName() + ", Primary Phone: " + contact.getPhoneNumbers().get(0).getPhoneNumber());
                    }

                    if (!contactList.isEmpty()) {
                        for (Contact contact : contactList) {
                            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

                            ops.add(ContentProviderOperation.newInsert(
                                    ContactsContract.RawContacts.CONTENT_URI)
                                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                                    .build());

                            //------------------------------------------------------ NAME
                            if (!contact.getName().isEmpty()) {
                                ops.add(ContentProviderOperation.newInsert(
                                        ContactsContract.Data.CONTENT_URI)
                                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                        .withValue(ContactsContract.Data.MIMETYPE,
                                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                                        .withValue(
                                                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                                contact.getName()).build());
                            }

                            //------------------------------------------------------ AVATAR
                            if (!contact.getAvatar_url().isEmpty()) {
                                // START: Get bit map from image URL
                                new FetchImageFromServerTask(contact).execute();

                                while (true) {
                                    if (fetchImageDone) {
                                        break;
                                    }
                                }

                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                                byte[] bitmapByteArray = stream.toByteArray();
                                // END

                                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, bitmapByteArray)
                                        .build());
                                fetchImageDone = false;
                            }

                            for (PhoneNumber phoneNumber : contact.getPhoneNumbers()) {

                                //------------------------------------------------------ MOBILE NUMBER
                                if (phoneNumber.getPhoneType().equals(ContactsContract.CommonDataKinds.
                                        Phone.TYPE_MOBILE + "")) {
                                    ops.add(ContentProviderOperation.
                                            newInsert(ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                            .withValue(ContactsContract.Data.MIMETYPE,
                                                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber.getPhoneNumber())
                                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                                            .build());
                                }

                                //------------------------------------------------------ HOME NUMBER
                                if (phoneNumber.getPhoneType().equals(ContactsContract.CommonDataKinds.
                                        Phone.TYPE_HOME + "")) {
                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                            .withValue(ContactsContract.Data.MIMETYPE,
                                                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber.getPhoneNumber())
                                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                                    ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
                                            .build());
                                }

                                //------------------------------------------------------ WORK NUMBER
                                if (phoneNumber.getPhoneType().equals(ContactsContract.CommonDataKinds.
                                        Phone.TYPE_WORK + "")) {
                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                            .withValue(ContactsContract.Data.MIMETYPE,
                                                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber.getPhoneNumber())
                                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
                                            .build());
                                }

                                //------------------------------------------------------ MAIN NUMBER
                                if (phoneNumber.getPhoneType().equals(ContactsContract.CommonDataKinds.
                                        Phone.TYPE_MAIN + "")) {
                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                            .withValue(ContactsContract.Data.MIMETYPE,
                                                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber.getPhoneNumber())
                                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                                    ContactsContract.CommonDataKinds.Phone.TYPE_MAIN)
                                            .build());
                                }

                                //------------------------------------------------------ WORK FAX
                                if (phoneNumber.getPhoneType().equals(ContactsContract.CommonDataKinds.
                                        Phone.TYPE_FAX_WORK + "")) {
                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                            .withValue(ContactsContract.Data.MIMETYPE,
                                                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber.getPhoneNumber())
                                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                                    ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK)
                                            .build());
                                }

                                //------------------------------------------------------ HOME FAX
                                if (phoneNumber.getPhoneType().equals(ContactsContract.CommonDataKinds.
                                        Phone.TYPE_FAX_HOME + "")) {
                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                            .withValue(ContactsContract.Data.MIMETYPE,
                                                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber.getPhoneNumber())
                                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                                    ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME)
                                            .build());
                                }

                                //------------------------------------------------------ PAGER
                                if (phoneNumber.getPhoneType().equals(ContactsContract.CommonDataKinds.
                                        Phone.TYPE_PAGER + "")) {
                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                            .withValue(ContactsContract.Data.MIMETYPE,
                                                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber.getPhoneNumber())
                                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                                    ContactsContract.CommonDataKinds.Phone.TYPE_PAGER)
                                            .build());
                                }

                                //------------------------------------------------------ OTHER
                                if (phoneNumber.getPhoneType().equals(ContactsContract.CommonDataKinds.
                                        Phone.TYPE_OTHER + "")) {
                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                            .withValue(ContactsContract.Data.MIMETYPE,
                                                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber.getPhoneNumber())
                                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                                    ContactsContract.CommonDataKinds.Phone.TYPE_OTHER)
                                            .build());
                                }
                            }

                            //------------------------------------------------------ IS STARRED (FAVORED)
                            // KHÔNG THÊM VÀO BỞI VÌ NẾU CÓ CÁC DANH BẠ TRÙNG TÊN HIỂN THỊ, TRÙNG SỐ ĐIỆN THOẠI THÌ ĐỀU BỊ SET LÀ STARRED
                            // ---> KHÔNG ĐÚNG

                            // Asking the Contact provider to create a new contact
                            try {
                                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                                Log.i("ADD TO SYSTEM CONTACT", "SUCCESS");
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            return "OK";
        }


        @Override // GUI task here
        protected void onPostExecute(String s) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if (s.equals("OK"))
                Toast.makeText(getApplicationContext(), "Download successfully!", Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap bitmap;
    private boolean fetchImageDone;

    public void addThemeListToSpinner(ArrayList<AmazTheme> list) {
        ArrayAdapter<AmazTheme> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, list);
        spinnerTheme.setAdapter(adapter);
    }

    public ArrayList<AmazTheme> generateThemes() {
        ArrayList<AmazTheme> listThemes = new ArrayList<>();
        AmazTheme s1 = new AmazTheme("Blue Accent", AmazTheme.BLUE_ACCENT);
        AmazTheme s2 = new AmazTheme("Banana Yellow", AmazTheme.BANANA_YELLOW);
        AmazTheme s3 = new AmazTheme("Reply Orange", AmazTheme.REPLY_ORANGE);
        listThemes.add(s1);
        listThemes.add(s2);
        listThemes.add(s3);
//        listThemes.add(s4);
        return listThemes;
    }

    public static void changeTheme(AmazTheme amazTheme) {
        changeTheme(amazTheme.getColor());
    }

    public static void changeTheme(int color) {
        DialFragment.changeDialColor(color);
        THEME_COLOR = color;
        Log.i("test", "THEME_COLOR = " + THEME_COLOR);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        if (THEME_COLOR != 0) intent.putExtra("color", THEME_COLOR);
        setResult(MainActivity.RESULT_CODE_FROM_SETTINGS, intent);
        finish();
        super.onBackPressed();
    }
}
