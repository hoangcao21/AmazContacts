package group.amazcontacts.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

import group.amazcontacts.R;
import group.amazcontacts.adapter.ViewPagerAdapter;
import group.amazcontacts.fragment.ContactsFragment;
import group.amazcontacts.fragment.DialFragment;
import group.amazcontacts.fragment.FavoritesFragment;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private ContactsFragment contactsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapping();
        setupViews();

    }

    private void setupViews() {
        ContactsFragment contactsFragment = new ContactsFragment();
        FavoritesFragment favoritesFragment = new FavoritesFragment();
        DialFragment dialFragment = new DialFragment();
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        // Add fragment with title to adapter
        viewPagerAdapter.addFragment(contactsFragment, "Contacts");
        viewPagerAdapter.addFragment(favoritesFragment, "Favorites");
        viewPagerAdapter.addFragment(dialFragment, "Dial");

        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

        // Set icon for fragment
        Objects.requireNonNull(tabLayout.getTabAt(0)).setIcon(R.drawable.baseline_contacts_black_24);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setIcon(R.drawable.baseline_favorite_black_24);
        Objects.requireNonNull(tabLayout.getTabAt(2)).setIcon(R.drawable.baseline_dialpad_black_24);

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
}
