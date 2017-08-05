package com.mihir.portfoliomonitoring.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.mihir.portfoliomonitoring.R;
import com.mihir.portfoliomonitoring.fragments.AboutUsFragment;
import com.mihir.portfoliomonitoring.fragments.EmployerSelectionFragment;

public class EmployerSelectionActivity extends AppCompatActivity {

    boolean mIsLargeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employer_selection);

        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);

        setInitialFragment();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_employer_selection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // THIS IS YOUR DRAWER/HAMBURGER BUTTON
            case R.id.actionAboutUs:
                showDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setInitialFragment() {
        EmployerSelectionFragment employerSelectionFragment = new EmployerSelectionFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_Content, employerSelectionFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        //setUpToolbar();
    }

    public void showDialog() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        AboutUsFragment aboutUsFragment = new AboutUsFragment();

        if (mIsLargeLayout) {
            // The device is using a large layout, so show the fragment as a dialog
            aboutUsFragment.show(fragmentManager, "dialog");
        } else {
            // The device is smaller, so show the fragment fullscreen
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            // For a little polish, specify a transition animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.replace(R.id.frame_Content, aboutUsFragment).addToBackStack(null).commit();
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
        }
    }

}
