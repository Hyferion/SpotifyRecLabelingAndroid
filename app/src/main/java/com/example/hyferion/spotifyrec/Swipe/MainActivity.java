package com.example.hyferion.spotifyrec.Swipe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Window;


import com.example.hyferion.spotifyrec.R;
import com.example.hyferion.spotifyrec.Settings.SettingsActivity;


public class MainActivity extends FragmentActivity {

    FragmentPagerAdapter fragmentPagerAdapter;
    ViewPager mViewPager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);


        mViewPager = (ViewPager) findViewById(R.id.pager);
        fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(fragmentPagerAdapter);

/*
        SharedPreferences sharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        if (sharedPreferences.getString("playlist","").equals("")){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        */

    }


}
