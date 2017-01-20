package com.bio4554.fam;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

/**
 * Created by bio4554 on 6/19/2016.
 */

public class ToolbarActivity extends FragmentActivity {
    private static final int NUM_PAGES = 3;
    private BottomBar mBottomBar;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private int position;
    private HomeActivity homeNew = new HomeActivity();
    private FamActivity famNew = new FamActivity();
    private ProfileActivity profileNew = new ProfileActivity();
    private boolean swiping = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toolbar_bottom_layout_pager);
        if(savedInstanceState != null) {
            return;
        }
        position = 1;
        mPager = (ViewPager)findViewById(R.id.mainPager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                System.out.println("ATTEMPTING PAGE CHANGE TO POS " + position);
                swiping = true;
                mBottomBar.selectTabAtPosition(position, true);

                swiping = false;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mBottomBar = BottomBar.attach(this, savedInstanceState);
        mBottomBar.setItems(R.menu.bottombar_menu);
        mBottomBar.setOnMenuTabClickListener(new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                    if(!swiping) {
                        if (menuItemId == R.id.bottomBarItemGroup) {
                            mPager.setCurrentItem(1);

                            // The user selected item number one.
                        } else if (menuItemId == R.id.bottomBarItemHome) {
                            mPager.setCurrentItem(2);

                        } else if (menuItemId == R.id.bottomBarItemProfile) {
                            mPager.setCurrentItem(0);

                        }
                    }
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {
                if (menuItemId == R.id.bottomBarItemGroup) {
                    // The user reselected item number one, scroll your content to top.
                } else if(menuItemId == R.id.bottomBarItemHome) {

                } else if(menuItemId == R.id.bottomBarItemProfile) {

                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Necessary to restore the BottomBar's state, otherwise we would
        // lose the current tab on orientation change.
        mBottomBar.onSaveInstanceState(outState);
    }



    public void restartLeftFam() {
        System.out.println("restartLeftFam() CALLED");
        Intent launchNext = new Intent(getApplicationContext(), ToolbarActivity.class);
        launchNext.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(launchNext);
    }

    public void addNewMember() {
        System.out.println("addNewMember() CALLED");
        Intent launchNext = new Intent(getApplicationContext(), AddMemberFragment.class);
        launchNext.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(launchNext);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            System.out.println("PAGER " + position);
            switch (position) {
                case 0:
                    return new ProfileActivity();
                case 1:
                    return new FamActivity();
                case 2:
                    return new HomeActivity();
                default:
                    return new HomeActivity();
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}

