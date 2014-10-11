package com.backagen.peid;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.test.ActivityInstrumentationTestCase2;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import static com.backagen.peid.espressoPlugin.AdditionalViewAssertions.hasAtLeast;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.*;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.*;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.*;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;

import com.backagen.peid.customView.EditTextWithCompletionMonitoring;
import com.backagen.peid.testFragment.BlankActivity;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

/**
 * Created by samwong on 14/07/2014.
 */
public class BillDetailsFragmentTests
        extends ActivityInstrumentationTestCase2<BlankActivity> {
    private BlankActivity mActivity;
    private static final String TAG_FOR_FRAGMENT = "TAG_FOR_FRAGMENT";

    public BillDetailsFragmentTests() {
        super(BlankActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        Fragment frag = startFragment(BillDetailsFragment.newInstance());
    }

    private Fragment startFragment(Fragment fragment) {
        FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();

        transaction.add(R.id.blank_activity_root, fragment, TAG_FOR_FRAGMENT);
        transaction.commit();
        getInstrumentation().waitForIdleSync();
        Fragment frag = mActivity.getFragmentManager().findFragmentByTag(TAG_FOR_FRAGMENT);
        return frag;
    }

    public void testPrecondition(){
        assertNotNull("Activity didn't boot properly", mActivity);
        assertNotNull("Fragment cannot be found", mActivity.getFragmentManager().findFragmentByTag(TAG_FOR_FRAGMENT));
    }

    public void testClickingNextWithIncompleteInfoShowsErrors(){
        onView(withId(R.id.bill_details_next_btn)).perform(click());
        assertBillAmountErrorIsShowing(true);
        assertNoPartySuppliedErrorIsShowing(true);

        onView(withId(R.id.bill_amount_input)).perform(typeText("123.45"));
        assertBillAmountErrorIsShowing(false);
        assertNoPartySuppliedErrorIsShowing(true);

        onView(withId(R.id.bill_amount_input)).perform(clearText());
        onView(withId(R.id.ugly_hack_for_ui_test_arrgh)).perform(typeText("ab@cd.com"));
        assertBillAmountErrorIsShowing(true);
        assertNoPartySuppliedErrorIsShowing(false);

    }

    private void assertBillAmountErrorIsShowing(boolean showing) {
        assertEquals(showing,
                getActivity().getString(R.string.bill_amount_empty_error_message).equals(
                        ((EditText) getActivity().findViewById(R.id.bill_amount_input)).getError()
                )
        );
    }

    private void assertNoPartySuppliedErrorIsShowing(boolean showing) {
        assertEquals(showing,
                getActivity().getString(R.string.no_party_supplied_error_message).equals(
                        ((EditText) getActivity().findViewById(R.id.ugly_hack_for_ui_test_arrgh)).getError()
                )
        );
    }

    public void testBillAmountInputIsSetToDecimal(){
        EditText editText = (EditText) getActivity().findViewById(R.id.bill_amount_input);
        assertTrue((editText.getInputType() | InputType.TYPE_NUMBER_FLAG_DECIMAL) > 0);
    }

    public void testSoftKeyboardDoesNotSquashViews(){
        fail();
    }

    public void testBillAmountValidation(){
        onView(withId(R.id.bill_amount_input)).perform(typeText("sadfasdfad")).check(matches(withText(""))).perform(clearText());
        onView(withId(R.id.bill_amount_input)).perform(typeText("123..45..67")).check(matches(withText("123.45"))).perform(clearText());
        onView(withId(R.id.bill_amount_input)).perform(typeText("000123")).check(matches(withText("0"))).perform(clearText());
        onView(withId(R.id.bill_amount_input)).perform(typeText("0000")).check(matches(withText("0"))).perform(clearText());
        onView(withId(R.id.bill_amount_input)).perform(typeText("0000")).check(matches(withText("0"))).perform(clearText());
        onView(withId(R.id.bill_amount_input)).perform(typeText("0")).check(matches(withText("0"))).perform(clearText());
        onView(withId(R.id.bill_amount_input)).perform(typeText("0.0")).check(matches(withText("0.0"))).perform(clearText());
        onView(withId(R.id.bill_amount_input)).perform(typeText("0.00")).check(matches(withText("0.00"))).perform(clearText());
        for(int i=0; i<50; i++){
            final double money = new Random().nextInt(10000) + (new Random().nextInt(100)/100.0);
            String moneyString = Double.toString(money);
            onView(withId(R.id.bill_amount_input)).perform(typeText(moneyString)).check(matches(withText(moneyString))).perform(clearText());
        }
    }

    public void testContactDetailsValidation(){
        final List<String> inputs = Lists.newArrayList("12.34", "Sam Wong", "@samwong1990", "s@mwong.hk", "07717304189", "");
        ViewGroup contactDetailsContainerAfterInsertion = propertyChecks(
                5,
                Functions.<LinearLayout>identity(),
                Functions.<EditTextWithCompletionMonitoring>identity(),
                new Function<Integer, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable Integer i) {
                        return inputs.get(i);
                    }
                }
        );
        onView(withId(R.id.bill_details_next_btn)).perform(click());
        for(int i=0; i<contactDetailsContainerAfterInsertion.getChildCount(); i++){
            if( i <= 2){
                assertNotNull(
                        String.format("Item %d's input is %s, getError() should not return null", i, inputs.get(i)),
                        ((EditText) contactDetailsContainerAfterInsertion.getChildAt(i)).getError());
            }else{
                assertNull(
                        String.format("Item %d's input is %s, getError() should return null", i, i < inputs.size() ? inputs.get(i) : "[NOINPUT]"),
                        ((EditText) contactDetailsContainerAfterInsertion.getChildAt(i)).getError());
            }
        }
    }

    public void testContactDetailsValidationIgnoresEmptyOnes(){
        ViewGroup contactDetailsContainerAfterInsertion = propertyChecks(
                5,
                Functions.<LinearLayout>identity(),
                Functions.<EditTextWithCompletionMonitoring>identity(), new Function<Integer, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable Integer input) {
                        return "07717304189";
                    }
                }
        );
        onView(withId(R.id.bill_details_next_btn)).perform(click());

        for(int i=0; i<contactDetailsContainerAfterInsertion.getChildCount(); i++){
            assertNull(String.format("Item %d's getError() should return null", i), ((EditText) contactDetailsContainerAfterInsertion.getChildAt(i)).getError());
        }
    }

    public void test_EMPTY_FIELDS_COUNT_GUARANTEE_IsMaintained() {
        final int minimumEmptyFields = BillDetailsFragment.EMPTY_FIELDS_COUNT_GUARANTEE;
        propertyChecks(
                50, new Function<LinearLayout, ViewInteraction>() {
                    @Nullable
                    @Override
                    public ViewInteraction apply(@Nullable LinearLayout input) {
                        return onView(withId(R.id.contact_details_container))
                                .check(
                                        hasAtLeast(minimumEmptyFields,
                                                allOf(
                                                        isAssignableFrom(EditTextWithCompletionMonitoring.class),
                                                        withText("")
                                                )
                                        )
                                );
                    }
                }, Functions.<EditTextWithCompletionMonitoring>identity(), new Function<Integer, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable Integer input) {
                        return Integer.toString(input);
                    }
                }
        );
    }

    public void testInputTypeIsSetToText(){
        propertyChecks(
                50, Functions.<LinearLayout>identity(), new Function<EditTextWithCompletionMonitoring, Object>() {
                    @Nullable
                    @Override
                    public Object apply(@Nullable EditTextWithCompletionMonitoring editText) {
                        assertTrue((editText.getInputType() | InputType.TYPE_CLASS_TEXT) > 0);
                        return null;
                    }
                }, new Function<Integer, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable Integer input) {
                        return Integer.toString(input);
                    }
                }
        );
    }

    public void testHintTextIsSet(){
        propertyChecks(
                50, Functions.<LinearLayout>identity(), new Function<EditTextWithCompletionMonitoring, Object>() {
                    @Nullable
                    @Override
                    public Object apply(@Nullable EditTextWithCompletionMonitoring editText) {
                        assertEquals(
                                editText != null ? editText.getHint().toString() : null,
                                getActivity().getString(R.string.contact_details_hint)
                        );
                        return null;
                    }
                }, new Function<Integer, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable Integer input) {
                        return Integer.toString(input);
                    }
                }
        );
    }

    private ViewGroup propertyChecks(
            int rowsToAdd, Function<LinearLayout, ? extends Object> correctnessCheckOnContainer, Function<EditTextWithCompletionMonitoring, ? extends Object> correctnessCheckOnEditText, Function<Integer, String> inputProvider){
        final LinearLayout root = (LinearLayout) getActivity().findViewById(R.id.contact_details_container);
        for(int i=0; i< rowsToAdd; i++){
            EditTextWithCompletionMonitoring editText = (EditTextWithCompletionMonitoring) root.getChildAt(i);
            // Horrible hack to help espresso find a specific view
            final int id = editText.hashCode();
            editText.setId(id);

            // Type something to make the list grow
            final String input = inputProvider.apply(i);
            onView(withId(id)).perform(scrollTo()).perform(typeText(input)).check(matches(withText(input)));

            // Apply correctness checks
            correctnessCheckOnContainer.apply(root);
            correctnessCheckOnEditText.apply(editText);
        }
        return root;
    }

}
