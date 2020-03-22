package group.amazcontacts.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import group.amazcontacts.activity.MainActivity;

public class GoogleSignInHandlerService {
    private final String TAG = this.getClass().getSimpleName();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private ProgressBar progressBar;
    public GoogleSignInHandlerService() {
    }

    public GoogleSignInHandlerService(FirebaseAuth firebaseAuth, ProgressBar progressBar) {
        this.firebaseAuth = firebaseAuth;
        this.progressBar = progressBar;
    }

    public void handleSignInResult(Context applicationContext, Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
            Toast.makeText(applicationContext, "Login success with "+account.getEmail(), Toast.LENGTH_SHORT)
                    .show();
            SharedPreferences sharedPreferences = applicationContext.getSharedPreferences("data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("Automatic Login",true);
            editor.commit();
            Intent i = new Intent(applicationContext , MainActivity.class);
            applicationContext.startActivity(i);
        } catch (ApiException e) {
            Toast.makeText(applicationContext, "Login Unsuccessful", Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
            Log.e(TAG, "handleSignInResult: "+e.getStatusCode());
        }
    }
    public void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        progressBar.setVisibility(View.VISIBLE);
        // [END_EXCLUDE]
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = firebaseAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }

                        // [START_EXCLUDE]
                        progressBar.setVisibility(View.INVISIBLE);
                        // [END_EXCLUDE]
                    }
                });
    }
}
