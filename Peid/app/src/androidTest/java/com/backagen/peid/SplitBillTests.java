package com.backagen.peid;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.test.InstrumentationTestCase;
import android.view.View;
import android.widget.EditText;

import com.backagen.peid.customView.EditTextWithCompletionMonitoring;
import com.google.android.apps.common.testing.ui.espresso.NoMatchingViewException;
import com.google.android.apps.common.testing.ui.espresso.matcher.BoundedMatcher;
import com.google.common.collect.Lists;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.pressImeActionButton;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.swipeLeft;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.swipeRight;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.doesNotExist;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isRoot;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class SplitBillTests extends ActivityInstrumentationTestCase2<SplitBill> {
    public SplitBillTests() {
        super(SplitBill.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        // Espresso will not launch our activity for us, we must launch it via getActivity().
        getActivity();
        // Make sure we start from the logged out state
        logOutIfLoggedIn();
    }

    private void logOutIfLoggedIn() {
        // Log out if it is logged in
        try{
            onView(withText(R.string.profile_logout_button_label)).perform(click());
        }catch(NoMatchingViewException ignored){

        }
    }

    public void testIfLoggedOutThenCannotScroll(){
        fail();
    }

    public void testLoggingInTriggerScroll() throws InterruptedException {
        fail();
    }

    public void testSignUpFlow() throws InterruptedException {
        final String randomName = new SimpleDateFormat("yyyyMMddHHmmssSSSZ").format(new Date());
        final String randomEmail = randomName + "@PeidTestDomainBeforeWeSeparateTheApp.example.com";
        final String randomPassword = randomName;
        logIn(randomName, randomEmail, randomPassword);


        onView(withId(R.id.profile_title)).check(matches(withText(R.string.profile_title_logged_in)));
        onView(withId(R.id.profile_email)).check(matches(withText(randomEmail)));
        onView(withId(R.id.profile_name)).check(matches(withText(randomName)));
    }

    private void logIn(String randomName, String randomEmail, String randomPassword) throws InterruptedException {
        onView(withId(R.id.login_or_logout_button))
                .perform(click());
        onView(withId(R.id.parse_signup_button))
                .perform(click());

        final Map<Integer, String> input = new HashMap<Integer, String>();
        input.put(R.id.signup_username_input, randomEmail);
        input.put(R.id.signup_password_input, randomPassword);
        input.put(R.id.signup_confirm_password_input, randomPassword);
        input.put(R.id.signup_name_input, randomName);

        for(Map.Entry<Integer, String> entry : input.entrySet()){
            onView(withId(entry.getKey()))
                    .perform(typeText(entry.getValue()));
        }

        onView(withId(R.id.create_account)).perform(click());

        Date stopWaitingAt = new Date();
        stopWaitingAt.setTime(stopWaitingAt.getTime() + 30 * 1000);
        while(new Date().before(stopWaitingAt)){
            try{
                onView(withText("Log out"));
                break;
            }catch(NoMatchingViewException e){
                System.out.println("Still waiting for Login to finish");
            }
        }
        Thread.sleep(3*1000); // Need a better way to wait for the Parse "Loading..." dialog's dismissal.
    }

    public void testLoginPage(){
        onView(withId(R.id.login_or_logout_button))
                .perform(click());
        List<Integer> itemsExpectedToBeVisible = Lists.newArrayList(R.id.login_username_input,
                R.id.login_password_input,
                R.id.parse_login_button,
                R.id.parse_signup_button,
                R.id.parse_login_help,
                R.id.facebook_login,
                R.id.twitter_login);

        for(Integer itemId : itemsExpectedToBeVisible){
            onView(withId(itemId)).check(matches(isDisplayed()));
        }
    }

    public void testEditingBillChangesConfirmationPage() throws InterruptedException {
        final String randomName = new SimpleDateFormat("yyyyMMddHHmmssSSSZ").format(new Date());
        final String randomEmail = randomName + "@PeidTestDomainBeforeWeSeparateTheApp.example.com";
        final String randomPassword = randomName;
        logIn(randomName, randomEmail, randomPassword);
        onView(isRoot()).perform(swipeLeft());


        onView(withId(R.id.bill_amount_input)).perform(typeText("12.34"));
        // Confirm we are not showing step 3
        onView(withId(R.id.confirm_bill_amount_textview)).check(doesNotExist());
        onView(withId(R.id.ugly_hack_for_ui_test_arrgh)).perform(typeText("a1@bc.def"));
        onView(withId(R.id.bill_details_next_btn)).perform(click());
        onView(withId(R.id.confirm_bill_amount_textview)).check(matches(withText("12.34")));
        onView(withId(R.id.confirm_party_size_textview)).check(matches(withText("2")));
        // Now go back and modify the amount
        onView(isRoot()).perform(swipeRight());
        onView(withId(R.id.bill_amount_input)).perform(clearText(), typeText("56.78"));
        onView(withId(R.id.ugly_hack_for_ui_test_arrgh2)).perform(typeText("a2@bc.def"));
        onView(isRoot()).perform(swipeLeft());
        onView(withId(R.id.confirm_bill_amount_textview)).check(matches(withText("56.78")));
        onView(withId(R.id.confirm_party_size_textview)).check(matches(withText("3")));

        // Now go back and modify the party size
    }

    public void testLoggingOutWouldRemoveSubsequentViews() throws InterruptedException {
        final String randomName = new SimpleDateFormat("yyyyMMddHHmmssSSSZ").format(new Date());
        final String randomEmail = randomName + "@PeidTestDomainBeforeWeSeparateTheApp.example.com";
        final String randomPassword = randomName;
        logIn(randomName, randomEmail, randomPassword);
        onView(isRoot()).perform(swipeLeft());
        onView(withId(R.id.bill_amount_input)).check(matches(isDisplayed()));

        // Now swipe back to login page and logout, assert we can't see the next page anymore
        // There was a bug where you can swipe and peek at the removed fragment, and
        // then get bounced back to the login fragment.
        onView(isRoot()).perform(swipeRight());
        onView(withId(R.id.login_or_logout_button)).perform(click());
        onView(withId(R.id.bill_amount_input)).check(doesNotExist());
    }

    public void testLoggedInPersonCanAlwaysSeeTheNextFragment() throws InterruptedException {
        final String randomName = new SimpleDateFormat("yyyyMMddHHmmssSSSZ").format(new Date());
        final String randomEmail = randomName + "@PeidTestDomainBeforeWeSeparateTheApp.example.com";
        final String randomPassword = randomName;
        logIn(randomName, randomEmail, randomPassword);
        onView(withId(R.id.bill_amount_input)).check(matches(withId(R.id.bill_amount_input)));;
        getInstrumentation().callActivityOnStop(getActivity());
        getInstrumentation().callActivityOnPause(getActivity());
        getInstrumentation().callActivityOnRestart(getActivity());
        getInstrumentation().callActivityOnResume(getActivity());
        onView(withId(R.id.bill_amount_input)).check(matches(withId(R.id.bill_amount_input)));;
    }



    public static Matcher<View> withHint(final int resourceId) {
        return new BoundedMatcher<View, EditText>(EditText.class) {
            private String resourceName = null;
            private String expectedHint = null;

            @Override
            public boolean matchesSafely(EditText editText) {
                if (null == expectedHint) {
                    try {
                        expectedHint = editText.getResources().getString(resourceId);
                        resourceName = editText.getResources().getResourceEntryName(resourceId);
                    } catch (Resources.NotFoundException ignored) {
            /* view could be from a context unaware of the resource id. */
                    }
                }

                if (null != expectedHint) {
                    return expectedHint.equals(editText.getHint());
                } else {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with string from resource id: ");
                description.appendValue(resourceId);
                if (null != resourceName) {
                    description.appendText("[");
                    description.appendText(resourceName);
                    description.appendText("]");
                }
                if (null != expectedHint) {
                    description.appendText(" value: ");
                    description.appendText(expectedHint);
                }
            }
        };
    }

}



