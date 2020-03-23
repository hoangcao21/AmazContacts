package group.amazcontacts.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import group.amazcontacts.R;
import group.amazcontacts.adapter.ContactAdapter;
import group.amazcontacts.fragment.ContactsFragment;
import group.amazcontacts.model.Contact;
import group.amazcontacts.model.PhoneNumber;

public class SettingActivity extends AppCompatActivity {
    private ImageView profileAvatar;
    private TextView textViewName;
    private TextView textViewEmail;
    private Button btnSignOut;
    private Spinner spinnerTheme;

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
        int color = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);

        bar.setBackgroundDrawable(new ColorDrawable(color));
        bar.setTitle(Html.fromHtml("<font color=\"black\">" + getString(R.string.app_name) + "</font>"));

        Window window = getWindow();

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.transparent));

        spinnerTheme = findViewById(R.id.spinnerTheme);
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
                new BackupContactsTask().execute();
            }
        });

        btnDownloadContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    ImageView imageView;


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

            InputStream uploadStream = null;

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
                        contactRef.child("avatar_url").setValue(downloadUri[0].toString());
                        contactRef.child("name").setValue(contact.getName());
                        contactRef.child("phoneNumbers").setValue(contact.getPhoneNumbers());
                        break;
                    }
                }
            }
            Log.i("BACK UP CONTACTS: ", "DONE");
            return null;
        }

        @Override // GUI task here
        protected void onPostExecute(String s) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    // TODO: Làm downloadcontact nốt
    class DownloadContactsTask extends AsyncTask<String, String, String> {
        private ContactAdapter contactAdapter;
        private String searchKey;

        public DownloadContactsTask() {
            this.searchKey = "";
        }

        public DownloadContactsTask(String searchKey) {
            this.searchKey = searchKey;
        }

        @Override
        protected String doInBackground(String... strings) { // Only non-GUI task
            DatabaseReference databaseReference = firebaseDatabase.getReference("contacts");
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference itemsRef = databaseReference.child(firebaseUser.getUid()).child("items");
            itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String id = snapshot.getKey();
                        String avatar_url;
                        String name;
                        List<PhoneNumber> phoneNumbers;
                        boolean isFavored;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            return null;
        }

        @Override // GUI task here
        protected void onPostExecute(String s) {

        }
    }


    public void addThemeListToSpinner(ArrayList<Integer> list) {
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, list);
        spinnerTheme.setAdapter(adapter);
    }

    public ArrayList<Integer> generateDummyThemes() {
        ArrayList<Integer> listThemes = new ArrayList<>();
        int s1 = R.color.colorAccent;
        int s2 = R.color.bananaYellow;
        int s3 = R.color.replyOrange;
        listThemes.add(s1);
        listThemes.add(s2);
        listThemes.add(s3);
//        listThemes.add(s4);
        return listThemes;
    }
}
