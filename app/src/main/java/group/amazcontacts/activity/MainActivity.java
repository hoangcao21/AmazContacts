package group.amazcontacts.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import group.amazcontacts.R;
import group.amazcontacts.adapter.ViewPagerAdapter;
import group.amazcontacts.fragment.ContactsFragment;
import group.amazcontacts.fragment.DialFragment;
import group.amazcontacts.fragment.FavoritesFragment;
import group.amazcontacts.model.AmazTheme;

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
    private SearchView searchView;
    private ContactsFragment contactsFragment;
    private SharedPreferences pref;

    private String searchQuery = "";
    private int searchingTab;

    private final static int REQUEST_PERMISSION_CODE = 100;
    private static boolean isReadContactsPermissionGranted;
    private static boolean isCallPhonePermissionGranted;
    private static boolean isWriteContactsPermissionGranted;
    private static int tabPosition; // Used when permission granted (cấp quyền thành công) at ContactsFragment

    public static int REQUEST_CODE_TO_SETTINGS = 200;
    public static int RESULT_CODE_FROM_SETTINGS = 201;

    public static int REQUEST_CODE_TO_ADD_NEW = 300;
    public static int RESULT_CODE_FROM_ADD_NEW = 301;

    public static Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapping();
        requestPermissions(0, getApplicationContext(), MainActivity.this); // Request all permission when creating GUI
        setupViews();
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (searchingTab == tabPosition) {
//            if (tabPosition == 1) {
//                new ContactsUpdateUI(searchQuery).execute("");
//            } else if (tabPosition == 2) {
//                new FavoritesUpdateUI(searchQuery).execute("");
//            }
//        } else {
//            if (tabPosition == 1) {
//                new ContactsUpdateUI().execute("");
//            } else if (tabPosition == 2) {
//                new FavoritesUpdateUI().execute("");
//            }
//        }
//
//    }

    private boolean firstInitial = false;
    private boolean firstLoadOfContacts = false;
    private boolean firstLoadOfFavorites = false;

    private void setupViews() {

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        ContactsFragment contactsFragment = new ContactsFragment();
        contactsFragment.setParentActivty(MainActivity.this);
        FavoritesFragment favoritesFragment = new FavoritesFragment();
        favoritesFragment.setParentActivity(MainActivity.this);
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
                try {
                    // Nếu là ở tab khác tab Dial, thì hiển thị search view
                    MenuItem searchMenuItem = mainMenu.findItem(R.id.action_search);
                    searchMenuItem.setVisible(true);
                    searchView.setVisibility(View.VISIBLE);

                    if (!isReadContactsPermissionGranted || !isCallPhonePermissionGranted || !isWriteContactsPermissionGranted) {
                        requestPermissions(tab.getPosition(), getApplicationContext(), MainActivity.this);
                    }

                    tabPosition = tab.getPosition();

                    // Đây là trong trường hợp chuyển từ tab khác sang tab Contacts nhưng đã có đủ quyền READ or WRITE contacts
                    if (!firstInitial && tabPosition == 1
                            && isReadContactsPermissionGranted
                            && isWriteContactsPermissionGranted) {
//                    ContactsFragment.setContacts(getApplicationContext(), MainActivity.this);
                        ContactsFragment.getListView().setAdapter(null);
//                        new ContactsUpdateUI().execute("");
                        firstInitial = true;
                    }

                    if (tabPosition == 0
                            && isCallPhonePermissionGranted && isReadContactsPermissionGranted && isWriteContactsPermissionGranted) {
                        DialFragment.setPhoneNumber("");
                        DialFragment.isPermissionsGranted();
                        // Giấu search view khi vào tab Dial Fragment
                        searchMenuItem.setVisible(false);
                        searchView.setVisibility(View.GONE);
                    }

                    if (tabPosition == 0 &&
                            (!isCallPhonePermissionGranted || !isReadContactsPermissionGranted || !isWriteContactsPermissionGranted)) {
                        DialFragment.setPhoneNumber("");
                        requestPermissions(tab.getPosition(), getApplicationContext(), MainActivity.this);
                        // Giấu search view khi vào tab Dial Fragment
                        searchMenuItem.setVisible(false);
                        searchView.setVisibility(View.GONE);
                    }

                    if (tabPosition == 1 &&
                            isReadContactsPermissionGranted && isWriteContactsPermissionGranted) {
//                        toolbar.collapseActionView();
                        if (!firstLoadOfContacts) {
                            new ContactsUpdateUI(searchQuery).execute("");
                            firstLoadOfContacts = true;
                        }
                    }

                    if (tabPosition == 2 &&
                            isReadContactsPermissionGranted && isWriteContactsPermissionGranted) {
                        searchMenuItem.setVisible(false);
                        searchView.setVisibility(View.GONE);
                        if (!firstLoadOfContacts) {
                            new FavoritesUpdateUI(searchQuery).execute("");
                            firstLoadOfFavorites = true;
                        }
                    }
                } catch (java.lang.NullPointerException ex) {
                    finish();
                    startActivity(new Intent(MainActivity.this, MainActivity.class));
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
        initializeTheme();
    }

    private void initializeTheme() {
        pref = getSharedPreferences("theme", Context.MODE_PRIVATE);
        int colorFromPref = pref.getInt("themeColor", AmazTheme.BLUE_ACCENT);
        changeToolbarColor(colorFromPref);
    }

    public void changeToolbarColor(int color) {
        toolbar.setBackgroundColor(getResources().getColor(color));
    }

    private Menu mainMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        // Cần thiết cho việc giấu search view theo ý Hiếu
        mainMenu = menu;


        MenuItem menuItem = menu.findItem(R.id.action_search);
        menuItem.setVisible(false);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (tabPosition == 1) {
                    searchQuery = query;
                    searchingTab = tabPosition;
                    new ContactsUpdateUI(query).execute("");
                    return true;
                }
//                else if (tabPosition == 2) {
//                    searchQuery = query;
//                    searchingTab = tabPosition;
//                    new FavoritesUpdateUI(query).execute("");
//                    return true;
//                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
//                new ContactsUpdateUI(newText).execute("");
//                return true;
            }
        });
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //Collapse the action item.
                    toolbar.collapseActionView();
                    //Clear the filter/search query.
//                    searchQuery = "";
                    if (tabPosition == 1) {
                        new ContactsUpdateUI().execute("");
                    } else if (tabPosition == 2) {
                        new FavoritesUpdateUI().execute("");
                    }
                }
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intentToAddNew = new Intent(getApplicationContext(), AddNewContactActivity.class);
                startActivityForResult(intentToAddNew, REQUEST_CODE_TO_ADD_NEW);
                break;
            case R.id.action_setting:
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivityForResult(intent, REQUEST_CODE_TO_SETTINGS);
                break;
            case R.id.action_log_out:
                // firebase signout
                FirebaseAuth.getInstance().signOut();
                //gmail signout

                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
                googleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Sign out from gmail", Toast.LENGTH_LONG).show();
                    }
                });
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                i.putExtra(SignUpActivity.NO_LOGIN_SILENT, false);

                SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("Automatic Login", false);
                editor.commit();

                startActivity(i);
//                finish();
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public static List<String> isPermissionsGranted(int position, Context context) {
        SharedPreferences pref = context.getSharedPreferences("AmazContacts", MODE_PRIVATE); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        int READ_CONTACTS_PERMISSION = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS);
        int WRITE_CONTACTS_PERMISSION = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_CONTACTS);
        int CALL_PHONE_PERMISSION = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE);


        List<String> listPermissionsNeeded = new ArrayList<>();

        if (CALL_PHONE_PERMISSION != PackageManager.PERMISSION_GRANTED && position == 0) {
            isCallPhonePermissionGranted = false;
            editor.putBoolean("isCallPhonePermissionGranted", false);
            editor.apply();
            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
        } else if (CALL_PHONE_PERMISSION == PackageManager.PERMISSION_GRANTED && position == 0) {
            isCallPhonePermissionGranted = true;
            editor.putBoolean("isCallPhonePermissionGranted", true);
            editor.apply();
        }

        if (READ_CONTACTS_PERMISSION != PackageManager.PERMISSION_GRANTED) {
            isReadContactsPermissionGranted = false;
            editor.putBoolean("isReadContactsPermissionGranted", false);
            editor.apply();
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        } else {
            isReadContactsPermissionGranted = true;
            editor.putBoolean("isReadContactsPermissionGranted", true);
            editor.apply();
        }

        if (WRITE_CONTACTS_PERMISSION != PackageManager.PERMISSION_GRANTED) {
            isWriteContactsPermissionGranted = false;
            editor.putBoolean("isWriteContactsPermissionGranted", false);
            editor.apply();
            listPermissionsNeeded.add(Manifest.permission.WRITE_CONTACTS);
        } else {
            isWriteContactsPermissionGranted = true;
            editor.putBoolean("isWriteContactsPermissionGranted", true);
            editor.apply();
        }

        return listPermissionsNeeded;
    }

    public static void requestPermissions(int position, Context context, Activity activity) {
        if (!isCallPhonePermissionGranted || !isReadContactsPermissionGranted || !isWriteContactsPermissionGranted) {

            List<String> permissionNeeded = isPermissionsGranted(position, context);

            if (!permissionNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(activity, permissionNeeded.toArray(new String[permissionNeeded.size()]), REQUEST_PERMISSION_CODE);
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
                switch (permissions[i]) {
                    case Manifest.permission.CALL_PHONE:
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            isCallPhonePermissionGranted = true;
                            editor.putBoolean("isCallPhonePermissionGranted", true);
                            editor.apply();
                        }
                        break;
                    case Manifest.permission.READ_CONTACTS:
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            isReadContactsPermissionGranted = true;
                            editor.putBoolean("isReadContactsPermissionGranted", true);
                            editor.apply();
                        }
                        break;
                    case Manifest.permission.WRITE_CONTACTS:
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            isWriteContactsPermissionGranted = true;
                            editor.putBoolean("isWriteContactsPermissionGranted", true);
                            editor.apply();
                        }
                        break;
                }
            }
        }

        // Used when permission granted at ContactsFragment
        if (tabPosition == 1 && isReadContactsPermissionGranted && isWriteContactsPermissionGranted) {
            new ContactsUpdateUI().execute("");
            firstInitial = true;
            firstLoadOfContacts = true;
        } else if (tabPosition == 0 && isCallPhonePermissionGranted && isReadContactsPermissionGranted && isWriteContactsPermissionGranted) {
            DialFragment.isPermissionsGranted();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_TO_SETTINGS && resultCode == RESULT_CODE_FROM_SETTINGS) {
            if (data != null) {
                int color = data.getIntExtra("color", AmazTheme.BLUE_ACCENT);
                changeToolbarColor(color);
                Log.i("test", "get result successfully");

                boolean isDownloadDone = data.getBooleanExtra("isDownloadDone", false);

                if (isDownloadDone) {
                    new ContactsUpdateUI().execute("");
                }
            }
        }

        if (requestCode == REQUEST_CODE_TO_ADD_NEW && resultCode == RESULT_CODE_FROM_ADD_NEW) {
            if (data != null) {
                boolean isNewContactAdded = data.getBooleanExtra("isNewContactAdded", false);

                if (isNewContactAdded) {
                    new ContactsUpdateUI(searchQuery).execute("");
                }
            }

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

    class FavoritesUpdateUI extends AsyncTask<String, String, String> {

        private String searchKey;

        public FavoritesUpdateUI(String searchKey) {
            this.searchKey = searchKey;
        }

        public FavoritesUpdateUI() {
            this.searchKey = "";
        }

        @Override
        protected String doInBackground(String... strings) {
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            (new FavoritesFragment()).loadListFavoriteToScreen(searchKey);
        }
    }
}
