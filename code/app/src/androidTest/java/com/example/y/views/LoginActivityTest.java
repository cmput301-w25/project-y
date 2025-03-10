package com.example.y.views;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.view.View;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.y.R;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;

public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule = new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void checkIfLoginButtonIsDisplayed() {
        onView(withId(R.id.login_button)).check(matches(isDisplayed()));
    }

    @Test
    public void checkIfUserNameError(){
        Matcher<View> username = withId(R.id.username);
        onView(username).check(matches(isDisplayed()));
        onView(username).perform(typeText("Tegen"));
        onView(username).perform(clearText());
        onView(username).check(matches(hasErrorText("Username cannot be empty!")));


    }


    @Test
    public void checkIfPasswordError(){
        Matcher<View> password = withId(R.id.password);
        onView(password).check(matches(isDisplayed()));
        onView(password).perform(typeText("Tegen"));
        onView(password).perform(clearText());
        onView(password).check(matches(hasErrorText("Password cannot be empty!")));
    }







}
