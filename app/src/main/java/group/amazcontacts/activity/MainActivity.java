package group.amazcontacts.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import group.amazcontacts.R;
import group.amazcontacts.adapter.ViewPagerAdapter;
import group.amazcontacts.fragment.ContactsFragment;
import group.amazcontacts.fragment.DialFragment;
import group.amazcontacts.fragment.FavoritesFragment;

/*
 * https://docs.google.com/document/d/1sP5KD_XXHOblEPt25EoaZcFrQHEWvK0knfX1phlpCJU/edit?fbclid=IwAR0b0Zt5IQBWt1iFb3zx7UbjQlB9rQEmjLle8U5bvhb5X_-1Fci6oKP-J38&pli=1
 *
 *  Variant: debugUnitTest
    Config: debug
    Store: C:\Users\LEGION\.android\debug.keystore
    Alias: AndroidDebugKey
    MD5: 1D:E8:28:33:8B:77:85:FE:2F:03:66:AC:16:80:AF:18
    SHA1: 6C:FF:EC:87:5A:03:3B:22:90:E3:AB:0D:F5:A9:50:29:27:E4:4D:B3
    SHA-256: 2F:03:64:FB:F4:01:32:16:61:D4:52:B2:38:A8:0B:90:C0:20:F5:9C:1B:12:B8:AB:24:6B:67:2B:F0:3C:22:FC
    Valid until: Sunday, December 5, 2049
 * */
public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private static Toolbar toolbar;
    private ContactsFragment contactsFragment;
    private final int REQUEST_PERMISSION_CODE = 100;
    private boolean isReadContactsPermissionGranted;
    private boolean isCallPhonePermissionGranted;
    private boolean isWriteContactsPermissionGranted;
    private int tabPosition; // Used when permission granted (cấp quyền thành công) at ContactsFragment

    public static Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapping();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("AmazContacts", MODE_PRIVATE); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        List<String> permissionNeeded = isPermissionsGranted(0); // Request all permission when creating GUI

        if (!permissionNeeded.isEmpty()) { // This is needed when user disable permissions manually
            for (String permission : permissionNeeded) {
                if (permission.equals(Manifest.permission.CALL_PHONE)) {
                    isCallPhonePermissionGranted = false;
                    editor.putBoolean("isCallPhonePermissionGranted", false);
                    editor.apply();
                } else {
                    isCallPhonePermissionGranted = true;
                    editor.putBoolean("isCallPhonePermissionGranted", true);
                    editor.apply();
                }

                if (permission.equals(Manifest.permission.READ_CONTACTS)) {
                    isReadContactsPermissionGranted = false;
                    editor.putBoolean("isReadContactsPermissionGranted", false);
                    editor.apply();
                } else {
                    isReadContactsPermissionGranted = true;
                    editor.putBoolean("isReadContactsPermissionGranted", true);
                    editor.apply();
                }

                if (permission.equals(Manifest.permission.WRITE_CONTACTS)) {
                    isWriteContactsPermissionGranted = false;
                    editor.putBoolean("isWriteContactsPermissionGranted", false);
                    editor.apply();
                } else {
                    isWriteContactsPermissionGranted = true;
                    editor.putBoolean("isWriteContactsPermissionGranted", true);
                    editor.apply();
                }
            }
            requestPermissions(0); // Request all permission when creating GUI
        }

//        // First time request needed
//        if (!isContactPermissionGranted || !isCallPhonePermissionGranted) {
//
//        }

        setupViews();
    }

    private boolean firstInitial = false;

    private void setupViews() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        ContactsFragment contactsFragment = new ContactsFragment();
        FavoritesFragment favoritesFragment = new FavoritesFragment();
        DialFragment dialFragment = new DialFragment();
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);

        // Add fragment with title to adapter
        viewPagerAdapter.addFragment(dialFragment, "Dial");
        viewPagerAdapter.addFragment(contactsFragment, "Contacts");
        viewPagerAdapter.addFragment(favoritesFragment, "Favorites");

        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // This is needed if the user
                if (!isReadContactsPermissionGranted || !isCallPhonePermissionGranted || !isWriteContactsPermissionGranted) {
                    requestPermissions(tab.getPosition());
                }

                tabPosition = tab.getPosition();

                if (!firstInitial && tabPosition == 1
                        && isReadContactsPermissionGranted
                        && isWriteContactsPermissionGranted) {
//                    ContactsFragment.setContacts(getApplicationContext(), MainActivity.this);
                    ContactsFragment.getListView().setAdapter(null);
                    new ContactsUpdateUI().execute("");
                    firstInitial = true;
                }

                if (tabPosition == 0 && isCallPhonePermissionGranted && isReadContactsPermissionGranted && isWriteContactsPermissionGranted) {
                    DialFragment.setPhoneNumber("");
                    DialFragment.isPermissionsGranted();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // Set icon for fragment
        Objects.requireNonNull(tabLayout.getTabAt(0)).setIcon(R.drawable.baseline_dialpad_black_24);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setIcon(R.drawable.baseline_contacts_black_24);
        Objects.requireNonNull(tabLayout.getTabAt(2)).setIcon(R.drawable.baseline_favorite_black_24);

    }

    private void mapping() {
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(this.getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorAccent));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            ListView contactListView = ContactsFragment.getListView();

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                new ContactsUpdateUI(newText).execute("");
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_setting:

                break;
            case R.id.action_log_out:
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(getApplicationContext(), SignUpActivity.class);
                i.putExtra(SignUpActivity.NO_LOGIN_SILENT, false);

                SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("Automatic Login",false);
                editor.commit();

                startActivity(i);
//                finish();
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public List<String> isPermissionsGranted(int position) {
        int READ_CONTACTS_PERMISSION = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS);
        int WRITE_CONTACTS_PERMISSION = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CONTACTS);
        int CALL_PHONE_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);


        List<String> listPermissionsNeeded = new ArrayList<>();

        if (CALL_PHONE_PERMISSION != PackageManager.PERMISSION_GRANTED && position == 0) {
            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
        }

        if (READ_CONTACTS_PERMISSION != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        }

        if (WRITE_CONTACTS_PERMISSION != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_CONTACTS);
        }

        return listPermissionsNeeded;
    }

    public void requestPermissions(int position) {
        if (!isCallPhonePermissionGranted || !isReadContactsPermissionGranted || !isWriteContactsPermissionGranted) {

            List<String> permissionNeeded = isPermissionsGranted(position);

            if (!permissionNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionNeeded.toArray(new String[permissionNeeded.size()]), REQUEST_PERMISSION_CODE);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("AmazContacts", MODE_PRIVATE); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.CALL_PHONE)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        isCallPhonePermissionGranted = true;
                        editor.putBoolean("isCallPhonePermissionGranted", true);
                        editor.apply();
                    }
                } else if (permissions[i].equals(Manifest.permission.READ_CONTACTS)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        isReadContactsPermissionGranted = true;
                        editor.putBoolean("isReadContactsPermissionGranted", true);
                        editor.apply();
                    }
                } else if (permissions[i].equals(Manifest.permission.WRITE_CONTACTS)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        isWriteContactsPermissionGranted = true;
                        editor.putBoolean("isWriteContactsPermissionGranted", true);
                        editor.apply();
                    }
                }
            }
        }

        // Used when permission granted at ContactsFragment
        if (tabPosition == 1 && isReadContactsPermissionGranted && isWriteContactsPermissionGranted) {
            new ContactsUpdateUI().execute("");
            firstInitial = true;
        } else if (tabPosition == 0 && isCallPhonePermissionGranted && isReadContactsPermissionGranted && isWriteContactsPermissionGranted) {
            DialFragment.isPermissionsGranted();
        }

    }

    class ContactsUpdateUI extends AsyncTask<String, String, String> {

        private String searchKey;

        public ContactsUpdateUI(String searchKey) {
            this.searchKey = searchKey;
        }

        public ContactsUpdateUI() {
            this.searchKey = "";
        }

        @Override
        protected String doInBackground(String... strings) {
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            ContactsFragment.setContacts(getApplicationContext(), MainActivity.this, searchKey);
        }
    }
}
