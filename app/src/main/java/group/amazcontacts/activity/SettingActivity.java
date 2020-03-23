package group.amazcontacts.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import group.amazcontacts.R;

public class SettingActivity extends AppCompatActivity {
    private ImageView profileAvatar;
    private TextView textViewName;
    private TextView textViewEmail;
    private Button btnSignOut;

    // Download & Import section
    private Button btnBackupContact;
    private Button btnDownloadContact;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mappingViews();
        setEvents();
    }

    private void mappingViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar bar = getSupportActionBar();
        int color = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);

        bar.setBackgroundDrawable(new ColorDrawable(color));

        Window window = getWindow();

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.transparent));

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

            }
        });

        btnDownloadContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
