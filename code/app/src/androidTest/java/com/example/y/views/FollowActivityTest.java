package com.example.y.views;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.y.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class FollowActivityTest {

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

    @Test
    public void followActivityTest() throws InterruptedException {

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
                allOf(withId(R.id.btnSearch), withContentDescription("Search"),
                        childAtPosition(
                                allOf(withId(R.id.header),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                0)),
                                2),
                        isDisplayed()));
        appCompatImageButton.perform(click());
        Thread.sleep(2000);
        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.searchEditText),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText3.perform(replaceText("testUser234"), closeSoftKeyboard());
        Thread.sleep(2000);
        ViewInteraction followButton = onView(
                allOf(withId(R.id.searchFollowBtn), withText("Follow"),
                        childAtPosition(
                                allOf(withId(R.id.resultView),
                                        withParent(withId(R.id.searchResultList))),
                                2),
                        isDisplayed()));
        followButton.perform(click());

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
        Thread.sleep(2000);
        ViewInteraction materialButton2 = onView(
                allOf(withId(R.id.logOutBtn), withText("Log Out"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                4),
                        isDisplayed()));
        materialButton2.perform(click());
        Thread.sleep(2000);
        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.username),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText4.perform(replaceText("testUser234"), closeSoftKeyboard());

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.password),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatEditText5.perform(replaceText("testing"), closeSoftKeyboard());

        ViewInteraction materialButton3 = onView(
                allOf(withId(R.id.login_button), withText("Login"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        materialButton3.perform(click());
        Thread.sleep(2000);
        ViewInteraction appCompatImageButton3 = onView(
                allOf(withId(R.id.btnUserProfile), withContentDescription("User Profile"),
                        childAtPosition(
                                allOf(withId(R.id.header),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                0)),
                                4),
                        isDisplayed()));
        appCompatImageButton3.perform(click());
        Thread.sleep(2000);
        ViewInteraction materialButton4 = onView(
                allOf(withId(R.id.followReqBtn), withText("Requests"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                3),
                        isDisplayed()));
        materialButton4.perform(click());
        Thread.sleep(2000);
        ViewInteraction textView = onView(
                allOf(withId(R.id.username), withText("testUser123"),
                        withParent(allOf(withId(R.id.followReqView),
                                withParent(withId(R.id.listviewFollowRequests)))),
                        isDisplayed()));
        textView.check(matches(withText("testUser123")));
        Thread.sleep(2000);
        ViewInteraction materialButton5 = onView(
                allOf(withId(R.id.acceptBtn), withText("Accept"),
                        childAtPosition(
                                allOf(withId(R.id.followReqView),
                                        withParent(withId(R.id.listviewFollowRequests))),
                                1),
                        isDisplayed()));
        materialButton5.perform(click());
        Thread.sleep(2000);
        ViewInteraction textView2 = onView(
                allOf(withId(R.id.EmptyFollowRequests), withText("No Follow Requests"),
                        withParent(withParent(withId(R.id.content))),
                        isDisplayed()));
        textView2.check(matches(withText("No Follow Requests")));

        ViewInteraction appCompatImageButton4 = onView(
                allOf(withId(R.id.btnUserProfile), withContentDescription("User Profile"),
                        childAtPosition(
                                allOf(withId(R.id.header),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                0)),
                                4),
                        isDisplayed()));
        appCompatImageButton4.perform(click());

        ViewInteraction materialButton6 = onView(
                allOf(withId(R.id.logOutBtn), withText("Log Out"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                4),
                        isDisplayed()));
        materialButton6.perform(click());
        Thread.sleep(2000);
        ViewInteraction appCompatEditText6 = onView(
                allOf(withId(R.id.username),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText6.perform(replaceText("testUser123"), closeSoftKeyboard());

        ViewInteraction appCompatEditText7 = onView(
                allOf(withId(R.id.password),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatEditText7.perform(replaceText("testing"), closeSoftKeyboard());

        ViewInteraction materialButton7 = onView(
                allOf(withId(R.id.login_button), withText("Login"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        materialButton7.perform(click());
        Thread.sleep(2000);
        ViewInteraction appCompatImageButton5 = onView(
                allOf(withId(R.id.btnSearch), withContentDescription("Search"),
                        childAtPosition(
                                allOf(withId(R.id.header),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                0)),
                                2),
                        isDisplayed()));
        appCompatImageButton5.perform(click());
        Thread.sleep(2000);
        ViewInteraction appCompatEditText8 = onView(
                allOf(withId(R.id.searchEditText),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText8.perform(replaceText("testUser234"), closeSoftKeyboard());

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.username), withText("testUser234"),
                        withParent(allOf(withId(R.id.resultView),
                                withParent(withId(R.id.searchResultList)))),
                        isDisplayed()));
        textView3.check(matches(withText("testUser234")));

        ViewInteraction button = onView(
                allOf(withId(R.id.searchFollowBtn), withText("Following"),
                        withParent(allOf(withId(R.id.resultView),
                                withParent(withId(R.id.searchResultList)))),
                        isDisplayed()));
        button.check(matches(isDisplayed()));

        ViewInteraction followButton2 = onView(
                allOf(withId(R.id.searchFollowBtn), withText("Following"),
                        childAtPosition(
                                allOf(withId(R.id.resultView),
                                        withParent(withId(R.id.searchResultList))),
                                2),
                        isDisplayed()));
        followButton2.perform(click());
        Thread.sleep(2000);

    }
}
