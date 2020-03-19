package group.amazcontacts.activity;

import android.Manifest;
import android.app.ActionBar;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

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
 *
 * */
public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private ContactsFragment contactsFragment;
    private final int REQUEST_PERMISSION_CODE = 100;
    private boolean isContactPermissionGranted;
    private boolean isCallPhonePermissionGranted;
    private int tabPosition; // Used when permission granted (cấp quyền thành công) at ContactsFragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapping();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("AmazContacts", MODE_PRIVATE);
        isContactPermissionGranted = pref.getBoolean("isContactPermissionGranted", false);
        isCallPhonePermissionGranted = pref.getBoolean("isCallPhonePermissionGranted", false);

        // First time request needed
        if (!isContactPermissionGranted || !isCallPhonePermissionGranted) {
            requestPermissions(0); // Request all permissions
        }

        setupViews();
    }

    private void setupViews() {
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
                if (!isContactPermissionGranted || !isCallPhonePermissionGranted) {
                    requestPermissions(tab.getPosition());
                }

                tabPosition = tab.getPosition();

//                if (tab.getPosition() == 1 && isContactPermissionGranted) {
//                    ContactsFragment.setContacts(getApplicationContext(), MainActivity.this);
//                }
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
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    public void requestPermissions(int position) {
        if (!isCallPhonePermissionGranted || !isContactPermissionGranted) {
            int READ_CONTACTS_PERMISSION = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_CONTACTS);
            int CALL_PHONE_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
            List<String> listPermissionsNeeded = new ArrayList<>();

            if (CALL_PHONE_PERMISSION != PackageManager.PERMISSION_GRANTED && position == 0) {
                listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
            }

            if (READ_CONTACTS_PERMISSION != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
            }

            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_PERMISSION_CODE);
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
                        isContactPermissionGranted = true;
                        editor.putBoolean("isContactPermissionGranted", true);
                        editor.apply();
                    }
                }
            }
        }

        // Used when permission granted at ContactsFragment
        if (tabPosition == 1 && isContactPermissionGranted) {
            ContactsFragment.setContacts(getApplicationContext(), MainActivity.this);
        }

    }
}
