package group.amazcontacts.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import group.amazcontacts.R;

public class SplashActivity extends Activity {
    // TODO: Revert SPLASH_DISPLAY_LENGTH to 3000
    private final int SPLASH_DISPLAY_LENGTH = 10;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(SplashActivity.this,MainActivity.class);
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Intent signUpIntent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(signUpIntent);
                } else {
                    SplashActivity.this.startActivity(mainIntent);
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    SplashActivity.this.finish();
                }
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
