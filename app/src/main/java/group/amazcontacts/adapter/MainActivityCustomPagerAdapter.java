package group.amazcontacts.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import group.amazcontacts.R;
import group.amazcontacts.fragment.MainActivityFragment;

public class MainActivityCustomPagerAdapter extends FragmentStatePagerAdapter {
    List<MainActivityFragment> fragments;
    public MainActivityCustomPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
        fragments = new ArrayList<>();
        MainActivityFragment fragment1 = new MainActivityFragment(R.layout.activity_fragment);
        fragments.add(fragment1);

        MainActivityFragment fragment2 = new MainActivityFragment(R.layout.activity_fragment_2);
        fragments.add(fragment2);

        MainActivityFragment fragment3 = new MainActivityFragment(R.layout.activity_fragement3);
        fragments.add(fragment3);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return "Fragment "+position;
    }
}
