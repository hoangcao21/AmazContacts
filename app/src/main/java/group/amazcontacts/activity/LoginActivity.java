package group.amazcontacts.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

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
                firebaseAuth.signInWithEmailAndPassword(email , password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        Toast.makeText(getApplicationContext(), "Login successfully",Toast.LENGTH_LONG).show();
                        prepareUIforLoginResult();
                    }
                }).addOnFailureListener(LoginActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Login failed",Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                        prepareUIforLoginResult();
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
        if (requestCode == this.RC_SIGN_IN) {
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
    }
}
