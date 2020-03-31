package group.amazcontacts.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import group.amazcontacts.R;
import group.amazcontacts.model.AmazTheme;
import group.amazcontacts.service.GoogleSignInHandlerService;

public class LoginActivity extends AppCompatActivity {

    private Button signinButton , createAccountButton ;
    private EditText emailEdittext , passwordEdittext;
    private SignInButton signInWithGoogleButton;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    private static final int  RC_SIGN_IN = 2;
    private GoogleSignInOptions gso;
    private GoogleSignInHandlerService googleSignInHandlerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mapping();
        setup();
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareUIForlogin();
                String email = emailEdittext.getText().toString();
                String password = passwordEdittext.getText().toString();
                if(email.isEmpty() || password.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Email and password cannot be empty", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }
                firebaseAuth.signInWithEmailAndPassword(email , password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            Toast.makeText(getApplicationContext(), "Sign in with "+user.getEmail(), Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(getApplicationContext(), "Sign in failed: "+task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

            }
        });
        signInWithGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareUIForlogin();

                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext() , gso);
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this , SignUpActivity.class);
                startActivity(i);
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void prepareUIForlogin(){
        progressBar.setVisibility(View.VISIBLE);
    }
    private void prepareUIforLoginResult(){
        progressBar.setVisibility(View.INVISIBLE);
    }
    private void setup(){
        initializeTheme();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInHandlerService = new GoogleSignInHandlerService(firebaseAuth, progressBar);
        prepareUIforLoginResult();
    }
    private void handleSignInResult(Task<GoogleSignInAccount> task){
        googleSignInHandlerService.handleSignInResult(LoginActivity.this, task);
    }
    private void mapping(){
        signinButton = findViewById(R.id.buttonLogin);
        signInWithGoogleButton = findViewById(R.id.signInWithGoogleButton);
        createAccountButton = findViewById(R.id.buttonCreateNewAccount);

        emailEdittext = findViewById(R.id.emailEditText);
        passwordEdittext = findViewById(R.id.passwordEditText);

        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.loadingBar);

        // HoangCH thêm vào để đồng nhất màu sắc giao diện
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ActionBar bar = getSupportActionBar();
        int color = ContextCompat.getColor(getApplicationContext(), R.color.blueAccent);

        bar.setBackgroundDrawable(new ColorDrawable(color));
        bar.setTitle(Html.fromHtml("<font color=\"black\">" + getString(R.string.app_name) + "</font>"));

        Window window = getWindow();

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.transparent));
    }
    private void initializeTheme() {
        SharedPreferences pref = getSharedPreferences("theme", Context.MODE_PRIVATE);
        int colorFromPref = pref.getInt("themeColor", AmazTheme.BLUE_ACCENT);
        int colorDrawable = ContextCompat.getColor(getApplicationContext(), colorFromPref);
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setBackgroundDrawable(new ColorDrawable(colorDrawable));
        mActionBar.setTitle(Html.fromHtml("<font color=\"black\">" + getString(R.string.app_name) + "</font>"));
    }
}
