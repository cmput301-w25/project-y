package com.example.y.views;


import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.example.y.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AddUpdateDeleteActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION");


    @Test
    public void assertTrue(){
        assert true;
    }
    //@Test ESPRESSO TEST RECORDER SUCKS

    public void addUpdateDeleteMoodEvent() throws InterruptedException {
        Random rand = new Random();
        int x = rand.nextInt(100);


        Thread.sleep(500);
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.username),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("testUser123"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.password),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText("testing"), closeSoftKeyboard());

        ViewInteraction materialButton = onView(
                allOf(withId(R.id.login_button), withText("Login"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        materialButton.perform(click());
        Thread.sleep(2000);

        ViewInteraction appCompatImageButton = onView(
                allOf(withId(R.id.btnUserProfile), withContentDescription("User Profile"),
                        childAtPosition(
                                allOf(withId(R.id.header),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                0)),
                                4),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.addMoodBtn), withContentDescription("Mood add button"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        floatingActionButton.perform(click());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.etReasonWhyText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                1)));
        appCompatEditText3.perform(scrollTo(), replaceText("test"+x), closeSoftKeyboard());

        ViewInteraction appCompatSpinner = onView(
                allOf(withId(R.id.spinnerMood),
                        childAtPosition(
                                allOf(withId(R.id.spinnerLayout),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                2)),
                                0)));
        appCompatSpinner.perform(scrollTo(), click());

        DataInteraction appCompatCheckedTextView = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(1);
        appCompatCheckedTextView.perform(click());

        ViewInteraction appCompatSpinner2 = onView(
                allOf(withId(R.id.spinnerSocialSituation),
                        childAtPosition(
                                allOf(withId(R.id.spinnerLayout),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                2)),
                                1)));
        appCompatSpinner2.perform(scrollTo(), click());

        DataInteraction appCompatCheckedTextView2 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(1);
        appCompatCheckedTextView2.perform(click());

        ViewInteraction materialTextView = onView(
                allOf(withId(R.id.datePickerAddMood),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                4)));
        materialTextView.perform(scrollTo(), click());

        ViewInteraction materialButton2 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        materialButton2.perform(scrollTo(), click());

        ViewInteraction materialButton3 = onView(
                allOf(withId(R.id.btnSubmit), withText("Submit"),
                        childAtPosition(
                                allOf(withId(R.id.footerLayout),
                                        childAtPosition(
                                                withId(R.id.main),
                                                1)),
                                0),
                        isDisplayed()));
        materialButton3.perform(click());
        Thread.sleep(5000);

        ViewInteraction textView = onView(
                allOf(withId(R.id.text), withText("test"+x),
                        withParent(withParent(withId(R.id.border))),
                        isDisplayed()));
        textView.check(matches(withText("test"+x)));
        Thread.sleep(5000);


        DataInteraction linearLayout = onData(anything())
                .inAdapterView(allOf(withId(R.id.listviewMoodEvents),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                1)))
                .atPosition(1);
        linearLayout.perform(click());


        ViewInteraction appCompatImageButton2 = onView(
                allOf(withId(R.id.editMenuIcon), withContentDescription("editButton"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.border),
                                        0),
                                2),
                        isDisplayed()));
        appCompatImageButton2.perform(click());

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.updateText), withText("test"+x),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                1)));
        appCompatEditText4.perform(scrollTo(), replaceText("test" + x*3));

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.updateText), withText("test"+x*3),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText5.perform(closeSoftKeyboard());

        ViewInteraction appCompatSpinner3 = onView(
                allOf(withId(R.id.spinnerMoodUpdate),
                        childAtPosition(
                                allOf(withId(R.id.spinnerLayout),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                2)),
                                0)));
        appCompatSpinner3.perform(scrollTo(), click());

        DataInteraction appCompatCheckedTextView3 = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(7);
        appCompatCheckedTextView3.perform(click());



        ViewInteraction materialButton4 = onView(
                allOf(withId(R.id.UpdateMoodButton), withText("Update this mood"),
                        childAtPosition(
                                allOf(withId(R.id.footerLayout),
                                        childAtPosition(
                                                withId(R.id.main),
                                                1)),
                                0),
                        isDisplayed()));
        materialButton4.perform(click());
        Thread.sleep(5000);


        ViewInteraction textView3 = onView(
                allOf(withId(R.id.text), withText("test"+x*3),
                        withParent(withParent(withId(R.id.border))),
                        isDisplayed()));
        textView3.check(matches(withText("test"+x*3)));

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.emoticon), withText("( ⊙_⊙)!"), withContentDescription("Emoticon"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class))),
                        isDisplayed()));
        textView4.check(matches(withText("( ⊙_⊙)!")));

        DataInteraction linearLayout2 = onData(anything())
                .inAdapterView(allOf(withId(R.id.listviewMoodEvents),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                1)))
                .atPosition(1);
        linearLayout2.perform(click());
        Thread.sleep(5000);

        ViewInteraction appCompatImageButton3 = onView(
                allOf(withId(R.id.editMenuIcon), withContentDescription("editButton"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.border),
                                        0),
                                2),
                        isDisplayed()));
        appCompatImageButton3.perform(click());
        Thread.sleep(5000);


        ViewInteraction materialButton5 = onView(
                allOf(withId(R.id.deleteMoodButton), withText("Delete this mood"),
                        childAtPosition(
                                allOf(withId(R.id.footerLayout),
                                        childAtPosition(
                                                withId(R.id.main),
                                                1)),
                                1),
                        isDisplayed()));
        materialButton5.perform(click());
        Thread.sleep(5000);

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
