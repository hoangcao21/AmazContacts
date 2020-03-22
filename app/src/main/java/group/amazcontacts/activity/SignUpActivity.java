package group.amazcontacts.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import group.amazcontacts.R;
import group.amazcontacts.service.GoogleSignInHandlerService;

public class SignUpActivity extends AppCompatActivity {
    private EditText emailEditText , passwordEditText;
    private Button signUpButton;
    private SignInButton signUpWithGoogleButton;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInOptions gso;
    private final String TAG = getClass().getSimpleName();
    private final int RC_SIGN_IN = 1;
    private GoogleSignInHandlerService googleSignInHandlerService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mapping();
        setUp();
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                final String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                firebaseAuth.createUserWithEmailAndPassword(email , password)
                        .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    Toast.makeText(getApplicationContext(), "Created user with email: "+email, Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }else{
                                    Toast.makeText(getApplicationContext(), "Created user failed ",Toast.LENGTH_LONG).show();
                                    Log.e(TAG, "on sign up fail: "+task.getException());
                                }
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });
            }
        });
        signUpWithGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext() , gso);
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
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

    private void setUp(){
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar.setVisibility(View.INVISIBLE);
        // set up signin service
        googleSignInHandlerService = new GoogleSignInHandlerService(firebaseAuth, progressBar);
        // set up for google login
         gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                 .requestIdToken(getString(R.string.default_web_client_id))
                 .requestEmail()
                 .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient (SignUpActivity.this, gso);
        googleSignInClient.silentSignIn()
                .addOnCompleteListener(
                        this,
                        new OnCompleteListener<GoogleSignInAccount>() {
                            @Override
                            public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                                handleSignInResult(task);
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                Log.e(TAG, "Current user: " + user.getEmail());
                            }
                        });
    }
    private void handleSignInResult(Task<GoogleSignInAccount> task){
        googleSignInHandlerService.handleSignInResult(SignUpActivity.this, task);
    }
    private void mapping(){
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signUpButton = findViewById(R.id.signupButton);
        signUpWithGoogleButton = findViewById(R.id.signinWithGoogle);
        progressBar = findViewById(R.id.progressBar);
    }
}
