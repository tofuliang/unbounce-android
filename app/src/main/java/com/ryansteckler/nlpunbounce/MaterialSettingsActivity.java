package com.ryansteckler.nlpunbounce;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ryansteckler.inappbilling.IabHelper;
import com.ryansteckler.inappbilling.IabResult;
import com.ryansteckler.inappbilling.Inventory;
import com.ryansteckler.inappbilling.Purchase;
import com.ryansteckler.nlpunbounce.helpers.LocaleHelper;
import com.ryansteckler.nlpunbounce.helpers.ThemeHelper;


public class MaterialSettingsActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        WakelocksFragment.OnFragmentInteractionListener,
        BaseDetailFragment.FragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener,
        AlarmsFragment.OnFragmentInteractionListener,
        RegexFragment.OnFragmentInteractionListener,
        ServicesFragment.OnFragmentInteractionListener {

    private static final String TAG = "Amplify: ";
    IabHelper mHelper;

    int mCurTheme = ThemeHelper.THEME_DEFAULT;
    int mCurForceEnglish = -1;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private boolean mIsPremium = true;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private int mLastActionbarColor = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "MaterialSettingsActivity onCreate called");

        mCurTheme = ThemeHelper.onActivityCreateSetTheme(this);
        mCurForceEnglish = LocaleHelper.onActivityCreateSetLocale(this);
        setContentView(R.layout.activity_material_settings);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                drawerLayout);

        mLastActionbarColor = getResources().getColor(R.color.background_primary);

        Log.i(TAG, "MaterialSettingsActivity Starting SELinux service");
        startService(new Intent(this, SELinuxService.class));

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Update theme
        mCurTheme = ThemeHelper.onActivityResumeVerifyTheme(this, mCurTheme);
        mCurForceEnglish = LocaleHelper.onActivityResumeVerifyLocale(this, mCurForceEnglish);

    }

    public boolean isPremium() {
        return mIsPremium;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        if (position == 0) {
            if (fragmentManager.getBackStackEntryCount() == 0) {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                        .replace(R.id.container, HomeFragment.newInstance(), "home")
                        .commit();
            } else {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                        .replace(R.id.container, HomeFragment.newInstance(), "home")
                        .addToBackStack("home")
                        .commit();
            }
        } else if (position == 1) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.container, WakelocksFragment.newInstance(), "wakelocks")
                    .addToBackStack("wakelocks")
                    .commit();
        } else if (position == 2) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.container, AlarmsFragment.newInstance(), "alarms")
                    .addToBackStack("alarms")
                    .commit();
        } else if (position == 3) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.container, ServicesFragment.newInstance(), "services")
                    .addToBackStack("services")
                    .commit();
        } else if (position == 4) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public void onWakelocksSetTitle(String title) {
        mTitle = title;
        restoreActionBar();
        animateActionbarBackground(getResources().getColor(R.color.background_secondary), 400);
    }

    @Override
    public void onWakelocksSetTaskerTitle(String id) {
        //Ignore because we're not in Tasker mode.
    }

    @Override
    public void onHomeSetTitle(String title) {
        mTitle = title;
        restoreActionBar();
        animateActionbarBackground(getResources().getColor(R.color.background_primary), 400);
    }

    @Override
    public void onDetailSetTitle(String title) {
        mTitle = title;
        restoreActionBar();
    }

    @Override
    public void onDetailSetTaskerTitle(String title) {
        //Do nothing because we're not in Tasker mode.
    }

    private void animateActionbarBackground(final int colorTo, final int durationInMs) {
        //Not great form, but the animation to show the details view takes 400ms.  We'll set our background
        //color back to normal once the animation finishes.  I wish there was a more elegant way to avoid
        //a timer.
        final ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), mLastActionbarColor, colorTo);
        final ColorDrawable colorDrawable = new ColorDrawable(mLastActionbarColor);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(final ValueAnimator animator) {
                colorDrawable.setColor((Integer) animator.getAnimatedValue());
                getActionBar().setBackgroundDrawable(colorDrawable);
            }
        });
        if (durationInMs >= 0)
            colorAnimation.setDuration(durationInMs);
        colorAnimation.start();
        mLastActionbarColor = colorTo;

    }

    @Override
    public void onAlarmsSetTitle(String title) {
        mTitle = title;
        restoreActionBar();
        animateActionbarBackground(getResources().getColor(R.color.background_four), 400);
    }

    @Override
    public void onAlarmsSetTaskerTitle(String title) {
        //Do nothing because we're not in Tasker mode.
    }


    @Override
    public void onServicesSetTitle(String title) {
        mTitle = title;
        restoreActionBar();
        animateActionbarBackground(getResources().getColor(R.color.background_three), 400);

    }

    @Override
    public void onSetTaskerTitle(String title) {
        //Do nothing because we're not in Tasker mode.

    }

    @Override
    public void onRegexSetTitle(String title) {
        mTitle = title;
        restoreActionBar();
    }

    @Override
    public void onRegexSetTaskerTitle(String title) {
        //Do nothing because we're not in Tasker mode.
    }
}
