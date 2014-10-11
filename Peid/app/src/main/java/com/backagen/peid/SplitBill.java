package com.backagen.peid;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.parse.ParseUser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class SplitBill extends Activity implements
        BillDetailsFragment.OnBillDetailsFilledInListener,
        ConfirmationFragment.OnInstructionSentListener{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ConfirmationFragment mConfirmationFragment;
    private BillDetailsFragment mBillDetailsFragment;
    private FinishedFragment mFinishFragment;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split_bill);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Setup the first page to display
        final LoginFragment loginFragment = new LoginFragment();
        loginFragment.addOnLogonListener(new LoginFragment.OnLogonEventListener() {
            @Override
            public void onLoggedInWithCompletedProfile() {
                // If you modify the view here, you may run into recursive calling on this callback
                // because when you call notifydatachanged, it's gonna trigger the loginfragment,
                // which will call this again.
            }

            @Override
            public void onLoggedOut() {
                showOtherPages(false);
            }

            @Override
            public void onUserClickedBeginSplitBill() {
                showOtherPages(true);
                mViewPager.setCurrentItem(mSectionsPagerAdapter.getCount() - 1, true);
            }
        });
        mSectionsPagerAdapter.addFragment(loginFragment, 0);
    }

    private void showOtherPages(boolean show) {
        if(show){
            if(mBillDetailsFragment == null){
                mBillDetailsFragment = BillDetailsFragment.newInstance();
            }
            mSectionsPagerAdapter.addFragmentIfNew(mBillDetailsFragment);
        }else{
            // Only login page can show
            while(mSectionsPagerAdapter.getCount() > 1){
                mSectionsPagerAdapter.removeFragment(1); // Keep removing from the head
                // Clear all references
                mBillDetailsFragment = null;
                mConfirmationFragment = null;
                mFinishFragment = null;
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.split_bill, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBillDetailsUpdated(BigDecimal billAmount, List<ImmutableContactDetails> party) {
        // Only update if page have been rendered
        if(mConfirmationFragment != null){
            mConfirmationFragment.updateViews(ImmutableList.copyOf(party), billAmount);
        }
    }

    @Override
    public void onBillDetailsFilledIn(BigDecimal billAmount, List<ImmutableContactDetails> party) {
        if(mConfirmationFragment != null){
            mConfirmationFragment.updateViews(ImmutableList.copyOf(party), billAmount);
        }else{
            final ParseUser currentUser = ParseUser.getCurrentUser();
            String userEmail = currentUser.getEmail();
            String userName = currentUser.getString("name");
            mConfirmationFragment = ConfirmationFragment.newInstance(userName, userEmail, billAmount, Lists.newArrayList(party));
            mSectionsPagerAdapter.addFragment(mConfirmationFragment);
        }
        // User has confirmed, Send user to confirmation page
        mViewPager.setCurrentItem(mSectionsPagerAdapter.getCount() - 1, true);

    }

    @Override
    public void onInstructionSent(String nameOfOriginator, String userToGetMoney, List<ImmutableContactDetails> contacts, String amountToPayPerPerson) {
        if(mFinishFragment == null){
            mFinishFragment = FinishedFragment.newInstance();
            mSectionsPagerAdapter.addFragment(mFinishFragment);
        }
        mViewPager.setCurrentItem(mSectionsPagerAdapter.getCount() - 1, true);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        private List<Fragment> mFragments = new ArrayList<Fragment>();

        public SectionsPagerAdapter addFragmentIfNew(Fragment fragment){
            if(!mFragments.contains(fragment)){
                mFragments.add(fragment);
                notifyDataSetChanged();
            }
            return this;
        }

        public SectionsPagerAdapter addFragment(Fragment fragment){
            mFragments.add(fragment);
            notifyDataSetChanged();
            return this;
        }

        public SectionsPagerAdapter addFragment(Fragment fragment, int insertionPosition){
            mFragments.add(insertionPosition, fragment);
            notifyDataSetChanged();
            return this;
        }

        public SectionsPagerAdapter removeFragment(Fragment fragment){
            if(!mFragments.remove(fragment)){
                throw new IllegalStateException("Tried to remove non existent fragment");
            }
            notifyDataSetChanged();
            return this;
        }

        public SectionsPagerAdapter removeFragment(int position){
            if(mFragments.remove(position) == null){
                throw new IllegalStateException("Tried to remove non existent fragment");
            }
            notifyDataSetChanged();
            return this;
        }

        // This makes sure you can't peek at removed view.
        @Override
        public int getItemPosition(Object object) {
            return mFragments.contains(object) ? POSITION_UNCHANGED : POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return String.format("Step %d/3", position);
        }
    }
}
