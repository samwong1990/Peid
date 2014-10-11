package com.backagen.peid;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.backagen.peid.testFragment.BlankActivity;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import static android.test.MoreAsserts.assertMatchesRegex;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

/**
 * Created by samwong on 14/07/2014.
 */
public class ConfirmationFragmentTests
        extends ActivityInstrumentationTestCase2<BlankActivity> {
    private BlankActivity mActivity;
    private static final String TAG_FOR_FRAGMENT = "TAG_FOR_FRAGMENT";

    private TextView mBillAmountTextView;
    private TextView mCostOfTimePerPersonTextView;
    private TextView mFinalSharePerPersonTextView;
    private TextView mPartySizeTextView;
    private TextView mRawShareTextView;
    private LinearLayout mPeopleToBillListView;

    public ConfirmationFragmentTests() {
        super(BlankActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }

    private Fragment startFragment(Fragment fragment) {
        FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();

        transaction.add(R.id.blank_activity_root, fragment, TAG_FOR_FRAGMENT);
        transaction.commit();
        getInstrumentation().waitForIdleSync();
        return mActivity.getFragmentManager().findFragmentByTag(TAG_FOR_FRAGMENT);
    }

    private List<TextView> populateViewVariables(View root){
        mBillAmountTextView = (TextView) root.findViewById(R.id.confirm_bill_amount_textview);
        mCostOfTimePerPersonTextView = (TextView) root.findViewById(R.id.confirm_cost_of_time_per_person_textview);
        mFinalSharePerPersonTextView = (TextView) root.findViewById(R.id.confirm_share_per_person_textview);
        mPartySizeTextView = (TextView) root.findViewById(R.id.confirm_party_size_textview);
        mRawShareTextView = (TextView) root.findViewById(R.id.confirm_raw_share_textview);
        mPeopleToBillListView = (LinearLayout) root.findViewById(R.id.people_who_will_receive_bill_listview);
        return Lists.newArrayList(mBillAmountTextView, mPartySizeTextView, mRawShareTextView, mCostOfTimePerPersonTextView, mFinalSharePerPersonTextView);
    }
    public void testPrecondition() {
        assertNotNull("Activity didn't boot properly", mActivity);
    }

    public void testPartySizeTakeTheUserIntoAccount(){
        int expectedPartySizeExceptUserWithThePhone = new Random().nextInt(100);
        // random(1000) + random(100)/100
        BigDecimal billAmount = new BigDecimal(new Random().nextInt(1000))
                .add(new BigDecimal(new Random().nextInt(100)).divide(new BigDecimal(100)));

        ArrayList<ImmutableContactDetails> contacts = Lists.newArrayList();
        for(int i=0; i<expectedPartySizeExceptUserWithThePhone; i++){
            if(i % 2 == 0){
                contacts.add(new ImmutableContactDetails(null, String.format("%7d", i)));
            }else{
                contacts.add(new ImmutableContactDetails(String.format("%d@example.com", i), null));
            }
        }
        Fragment fragment = startFragment(ConfirmationFragment.newInstance("", "s@mwong.hk", billAmount, contacts));
        populateViewVariables(fragment.getView());
        assertEquals(expectedPartySizeExceptUserWithThePhone + 1, Integer.parseInt(mPartySizeTextView.getText().toString()));
    }

    public void testTheScreenShowsWhatIGave(){
        int expectedPartySizeExceptUserWithThePhone = new Random().nextInt(100);
        // random(1000) + random(100)/100
        BigDecimal billAmount = new BigDecimal(expectedPartySizeExceptUserWithThePhone* (new Random().nextInt(1000)+50))
                .add(new BigDecimal(new Random().nextInt(100)).divide(new BigDecimal(100)));

        ArrayList<ImmutableContactDetails> contacts = Lists.newArrayList();
        for(int i=0; i<expectedPartySizeExceptUserWithThePhone; i++){
            if(i % 2 == 0){
                contacts.add(new ImmutableContactDetails(null, String.format("%7d", i)));
            }else{
                contacts.add(new ImmutableContactDetails(String.format("%d@example.com", i), null));
            }
        }
        Fragment fragment = startFragment(ConfirmationFragment.newInstance("", "s@mwong.hk", billAmount, contacts));
        List<TextView> textViews = populateViewVariables(fragment.getView());

        StringBuilder viewStrings = new StringBuilder("\n");
        for(TextView view : textViews){
            viewStrings.append(view.getText().toString()).append("\n");
            System.out.println("view value:" + view.getText());
            Log.i("Test", "view value:" + view.getText());
            assertMatchesRegex(String.format("View %s is not in money format", view),
                    Constants.MONEY_REGEX,
                    view.getText().toString());
        }

        BigDecimal onScreenBillAmount = getBigDecimal(mBillAmountTextView);
        assertEquals(viewStrings.toString(), onScreenBillAmount.doubleValue(), billAmount.doubleValue());

        int onScreenPartySize = Integer.parseInt(mPartySizeTextView.getText().toString());
        assertEquals(viewStrings.toString(), onScreenPartySize, expectedPartySizeExceptUserWithThePhone + 1);

        BigDecimal rawShare = getBigDecimal(mRawShareTextView);
        BigDecimal reconstructOriginalBillAmount = rawShare.multiply(new BigDecimal(onScreenPartySize));
        assertTrue(viewStrings.toString(), getAbsError(reconstructOriginalBillAmount, billAmount) < 0.01);
        assertTrue(viewStrings + "We should always collect more. reconstructOriginalBillAmount=" + reconstructOriginalBillAmount + "billAmount=" + billAmount,
                reconstructOriginalBillAmount.compareTo(billAmount) >= 0);

        BigDecimal onScreenCostOfTime = getBigDecimal(mCostOfTimePerPersonTextView);
        BigDecimal reconstructRawShare = onScreenCostOfTime.divide(BigDecimal.valueOf(ConfirmationFragment.COST_OF_TIME_FACTOR), 5, RoundingMode.UP);
        assertTrue(viewStrings + String.format("reconstructRawShare=%f rawShare=%f", reconstructRawShare, rawShare),
                getAbsError(reconstructRawShare, rawShare) < 0.05);
        assertTrue(viewStrings + "We should always collect more. reconstructRawShare=" + reconstructRawShare + "onScreenCostOfTime=" + onScreenCostOfTime,
                reconstructRawShare.compareTo(onScreenCostOfTime) >= 0);

        BigDecimal finalSharePerPerson = getBigDecimal(mFinalSharePerPersonTextView);
        BigDecimal reconstructRawShareFromFinal = finalSharePerPerson.add(onScreenCostOfTime.negate());
        assertTrue(viewStrings.toString() + "reconstructRawShareFromFinal=" + reconstructRawShareFromFinal + " rawShare=" + rawShare,
                reconstructRawShareFromFinal.compareTo(rawShare) >= 0);
        assertTrue(viewStrings + String.format("reconstructRawShareFromFinal=%f rawShare=%f", reconstructRawShareFromFinal, rawShare),
                getAbsError(reconstructRawShareFromFinal, rawShare) < 0.05);

        Set<String> expectedContactsOnScreen = Sets.newHashSet(
                Lists.transform(
                        contacts,
                        new Function<ImmutableContactDetails, String>() {
                            @Nullable
                            @Override
                            public String apply(@Nullable ImmutableContactDetails input) {
                                if(input != null){
                                    if(input.getEmail() != null){
                                        return input.getEmail();
                                    }else if(input.getPhoneNumber() != null){
                                        return input.getPhoneNumber();
                                    }
                                }
                                throw new IllegalStateException("Congrats, you messed up. You need a test to test your test");
                            }
                        }));
        for(int i=0; i<mPeopleToBillListView.getChildCount(); i++){
            TextView view = (TextView) mPeopleToBillListView.getChildAt(i);
            assertTrue(expectedContactsOnScreen.contains(view.getText().toString()));
        }
    }

    public void testFinishTriggersEmail() throws InterruptedException {
        int expectedPartySizeExceptUserWithThePhone = new Random().nextInt(10)+5;
        // random(1000) + random(100)/100
        BigDecimal billAmount = new BigDecimal(expectedPartySizeExceptUserWithThePhone* (new Random().nextInt(1000)+50))
                .add(new BigDecimal(new Random().nextInt(100)).divide(new BigDecimal(100)));

        ArrayList<ImmutableContactDetails> contacts = Lists.newArrayList();
        for(int i=0; i<expectedPartySizeExceptUserWithThePhone; i++){
            if(i % 2 == 0){
                contacts.add(new ImmutableContactDetails(null, String.format("%7d", i)));
            }else{
                contacts.add(new ImmutableContactDetails(String.format("sam.wong.1990+%d@gmail.com", i), null));
            }
        }
        Fragment fragment = startFragment(ConfirmationFragment.newInstance("sam name", "sam.wong.1990+originator@gmail.com", billAmount, contacts));
        populateViewVariables(fragment.getView());
        onView(withId(R.id.finish_btn)).perform(click());
    }

    private BigDecimal getBigDecimal(TextView textView) {
        return new BigDecimal(textView.getText().toString());
    }

    private double getAbsError(BigDecimal newVal, BigDecimal oldVal){
        return newVal.add(oldVal.negate()).divide(oldVal, 5, RoundingMode.HALF_EVEN).doubleValue();
    }
}
