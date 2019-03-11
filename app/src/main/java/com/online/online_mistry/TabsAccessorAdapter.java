package com.online.online_mistry;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter {
    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {

            case 0:
                HomeFragment homeFragment = new HomeFragment();
                return homeFragment;

            case 1:
                HistoryFragment historyFragment = new HistoryFragment();
                return historyFragment;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 2;
    }


    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Dashboard";
            case 1:
                return "History";
            default:
                return null;
        }

    }
}
