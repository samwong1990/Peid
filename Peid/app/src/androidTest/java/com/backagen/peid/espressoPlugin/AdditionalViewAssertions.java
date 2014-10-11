package com.backagen.peid.espressoPlugin;

import android.view.View;

import com.google.android.apps.common.testing.ui.espresso.NoMatchingViewException;
import com.google.android.apps.common.testing.ui.espresso.ViewAssertion;
import com.google.android.apps.common.testing.ui.espresso.util.HumanReadables;
import com.google.common.base.Optional;

import junit.framework.AssertionFailedError;

import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.apps.common.testing.ui.espresso.util.TreeIterables.breadthFirstViewTraversal;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by samwong on 14/07/2014.
 */
public class AdditionalViewAssertions {
    /**
     * Returns a generic {@link com.google.android.apps.common.testing.ui.espresso.ViewAssertion} that asserts that there is a
     * given number of descendant views that match the specified matcher.
     *
     * Example: onView(rootView).check(has(3, isAssignableFrom(EditText.class));
     *
     * adapted from : https://gist.github.com/acontal/9038375
     * @param minimalCount the number of descendant views that should match the specified matcher
     * @param selector the matcher to select the descendant views
     * @throws AssertionError if the number of views that match the selector is different from expectedCount
     */
    public static ViewAssertion hasAtLeast(final int minimalCount, final Matcher<View> selector) {
        return new ViewAssertion() {
            @Override
            public void check(Optional<View> view, Optional<NoMatchingViewException> noViewFoundException) {
                checkArgument(view.isPresent());
                View rootView = view.get();

                Iterable<View> descendantViews = breadthFirstViewTraversal(rootView);
                List<View> selectedViews = new ArrayList<View>();
                for (View descendantView : descendantViews) {
                    if (selector.matches(descendantView)) {
                        selectedViews.add(descendantView);
                    }
                }

                if (selectedViews.size() < minimalCount) {
                    String errorMessage = HumanReadables.getViewHierarchyErrorMessage(rootView,
                            Optional.of(selectedViews),
                            String.format("Found %d views instead of %d matching: %s", selectedViews.size(), minimalCount, selector),
                            Optional.of("****MATCHES****"));
                    throw new AssertionFailedError(errorMessage);
                }
            }
        };
    }
}
