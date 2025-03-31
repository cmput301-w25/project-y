package com.example.y.views;


import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
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
import static java.lang.Thread.sleep;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

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
public class CreatePrivateMoodEventActivityTest {
    //TODO: make it so that incase it crashes, it has a unique mood event, as well add thread.sleep() when the activity changes so that it doesn't get cooked by a race condition
    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

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
    Random rand = new Random();
    int x = rand.nextInt(100);
    @Test
    public void privateMoodActivityTest() throws InterruptedException {
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.username),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("testUIser"), closeSoftKeyboard());
        sleep(2000);
        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.username), withText("testUIser"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText2.perform(click());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.username), withText("testUIser"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText3.perform(replaceText("testUser"));

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.username), withText("testUser"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText4.perform(closeSoftKeyboard());
        sleep(2000);
        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.username), withText("testUser"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText5.perform(click());

        ViewInteraction appCompatEditText6 = onView(
                allOf(withId(R.id.username), withText("testUser"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText6.perform(replaceText("testUser123"));

        ViewInteraction appCompatEditText7 = onView(
                allOf(withId(R.id.username), withText("testUser123"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText7.perform(closeSoftKeyboard());
        sleep(2000);
        ViewInteraction appCompatEditText8 = onView(
                allOf(withId(R.id.password),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatEditText8.perform(replaceText("testing"), closeSoftKeyboard());

        ViewInteraction appCompatEditText9 = onView(
                allOf(withId(R.id.password), withText("testing"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatEditText9.perform(pressImeActionButton());
        sleep(2000);
        ViewInteraction materialButton = onView(
                allOf(withId(R.id.login_button), withText("Login"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        materialButton.perform(click());
        sleep(2000);
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
        sleep(2000);
        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.addMoodBtn), withContentDescription("Mood add button"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        floatingActionButton.perform(click());
        sleep(2000);


        ViewInteraction appCompatEditText10 = onView(
                allOf(withId(R.id.etReasonWhyText),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                1)));
        appCompatEditText10.perform(scrollTo(), replaceText("personal"+x), closeSoftKeyboard());

        ViewInteraction materialCheckBox = onView(
                allOf(withId(R.id.privacyCheckBox), withText("Private"),
                        childAtPosition(
                                allOf(withId(R.id.checkboxes),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                5)),
                                0)));
        materialCheckBox.perform(scrollTo(), click());
        Thread.sleep(2000);

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
        sleep(2000);
        ViewInteraction appCompatImageButton2 = onView(
                allOf(withId(R.id.btnUserProfile), withContentDescription("User Profile"),
                        childAtPosition(
                                allOf(withId(R.id.header),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                0)),
                                4),
                        isDisplayed()));
        appCompatImageButton2.perform(click());
        sleep(2000);
        assert true;
    }
}
