package com.backagen.peid;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.backagen.peid.customView.EditTextWithCompletionMonitoring;
import com.backagen.peid.testFragment.BlankActivity;
import com.google.android.apps.common.testing.ui.espresso.action.EspressoKey;
import com.google.common.collect.Lists;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.*;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.*;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by samwong on 14/07/2014.
 */
public class EditTextWithCompletionMonitoringTests extends ActivityInstrumentationTestCase2<BlankActivity> {

    private int finishedEditingCallCount;
    private static final int TEST_SUBJECT_ID = 123456;

    public EditTextWithCompletionMonitoringTests() {
        super(BlankActivity.class);
    }

    @Override

    protected void setUp() throws Exception {
        super.setUp();
        getActivity();
        finishedEditingCallCount  = 0;


    }

    public void testPrecondition(){
        setupViews();
        assertEquals("call count should be zero at the beginning", 0, finishedEditingCallCount);
    }

    public void testWhenSwitchFocusToAnotherTextField_ListenerIsCalled(){
        setupViews();
        assertTrue("call count should be zero at the beginning", 0 == finishedEditingCallCount);

        int previousCount = 0;
        onView(withId(TEST_SUBJECT_ID)).perform(typeText("Apple"));
        onView(allOf(supportsInputMethods(), not(withId(TEST_SUBJECT_ID)))).perform(typeText("Blah"));
        assertTrue("should call callback after completing subject field", previousCount < finishedEditingCallCount);
        previousCount = finishedEditingCallCount;

        onView(withId(TEST_SUBJECT_ID)).perform(typeText("Mango"));
        onView(allOf(supportsInputMethods(), not(withId(TEST_SUBJECT_ID)))).perform(typeText("Blah"));
        assertTrue("should call callback after completing subject field", previousCount < finishedEditingCallCount);
        previousCount = finishedEditingCallCount;

        onView(isAssignableFrom(Button.class)).perform(click());
        assertTrue("Should not trigger callback again", previousCount == finishedEditingCallCount);
        previousCount = finishedEditingCallCount;

        onView(withId(TEST_SUBJECT_ID)).perform(typeText("Orange")).perform(pressImeActionButton());
        assertTrue("should call callback again after clicking other things", previousCount < finishedEditingCallCount);
        previousCount = finishedEditingCallCount;

        onView(withId(TEST_SUBJECT_ID)).perform(typeText("Coconut")).perform(pressKey(EditorInfo.IME_ACTION_NEXT));
        assertTrue("should call callback again after clicking other things", previousCount < finishedEditingCallCount);
        previousCount = finishedEditingCallCount;

    }

    private void setupViews() {
        final EditTextWithCompletionMonitoring testSubject = new EditTextWithCompletionMonitoring(getActivity())
                .addFinishedEditingListener(new EditTextWithCompletionMonitoring.FinishedEditingListener() {
                    @Override
                    public void idempotentFinishedEditingHandler(EditText view) {
                        finishedEditingCallCount++;
                    }
                });
        //noinspection ResourceType
        testSubject.setId(TEST_SUBJECT_ID);

        final Button distractionButton = new Button(getActivity());
        final EditText distractionEditText = new EditText(getActivity());

        final LinearLayout rootView = (LinearLayout) getActivity().findViewById(R.id.blank_activity_root);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(View view : Lists.newArrayList(testSubject, distractionButton, distractionEditText)){
                    rootView.addView(view);
                }
            }
        });

    }

}
