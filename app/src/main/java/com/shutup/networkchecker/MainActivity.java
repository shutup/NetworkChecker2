package com.shutup.networkchecker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.viewPager)
    ViewPager mViewPager;

    private List<String> mTabTitles;
    private List<Fragment> mFragmentList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mTabTitles = new ArrayList<>();
        mTabTitles.add(getString(R.string.status));
        mTabTitles.add(getString(R.string.details));

        mFragmentList = new ArrayList<>();
        mFragmentList.add(new FragmentStatus()) ;
        mFragmentList.add(new FragmentDetails()) ;

        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragmentList.get(position);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTabTitles.get(position);
            }

            @Override
            public int getCount() {
                return mFragmentList.size();
            }
        });
    }
}
