package com.example.hyferion.spotifyrec.Swipe;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class FragmentPagerAdapter extends FragmentStatePagerAdapter {

    public FragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {

        switch (i){
            case 0:
                return new SwipeFragment();

            case 1:
                return new OverviewFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
