package com.example.y.views;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.view.View;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.y.R;
import com.example.y.services.SessionManager;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SignUpActivityTest {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    @Rule
    public ActivityScenarioRule<SignUpActivity> activityRule = new ActivityScenarioRule<>(SignUpActivity.class);
    private static SessionManager session;

    @Before
    public void setUp() throws Exception {
    session = new SessionManager(context);
    session.saveSession("tegen");
    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void checkNameEmptyError() {
        // Check if the name field is empty
        Matcher<View> name = withId(R.id.name);
        onView(name).check(matches(isDisplayed()));
        onView(name).perform(typeText("Tegen"));
        onView(name).perform(clearText());
        onView(name).check(matches(hasErrorText("Name cannot be empty!")));
    }

    @Test
    public void checkUserNameEmptyError(){
        Matcher<View> username = withId(R.id.username);
        onView(username).check(matches(isDisplayed()));
        onView(username).perform(typeText("Tegen"));
        onView(username).perform(clearText());
        onView(username).check(matches(hasErrorText("Username cannot be empty!")));
    }
    @Test
    public void checkEmailEmptyError(){
        Matcher<View> email = withId(R.id.email);
        onView(email).check(matches(isDisplayed()));
        onView(email).perform(typeText("Tegen@gmail.com"));
        onView(email).perform(clearText());
        onView(email).check(matches(hasErrorText("Email cannot be empty!")));
    }
    @Test
    public void checkConfirmEmailEmptyError(){
        Matcher<View> email = withId(R.id.confirm_email);
        onView(withId(R.id.email)).perform(typeText("tegen@gmail.com"));
        onView(email).check(matches(isDisplayed()));
        onView(email).perform(typeText("Tegen@gmail.com"));
        onView(email).perform(clearText());
        onView(email).check(matches(hasErrorText("Emails do not match!")));
    }
    @Test
    public void checkEmptyPassword(){
        Matcher<View> password = withId(R.id.password);
        onView(password).check(matches(isDisplayed()));
        onView(password).perform(typeText("Tegen"));
        onView(password).perform(clearText());
        onView(password).check(matches(hasErrorText("Password cannot be empty!")));
    }
    @Test
    public void checkEmptyConfirmPassword(){
        Matcher<View> password = withId(R.id.confirmPassword);
        onView(password).check(matches(isDisplayed()));
        onView(password).perform(typeText("Tegen"));
        onView(withId(R.id.password)).perform(typeText("tegen@gmail.com"));
        onView(password).perform(clearText());
        onView(password).check(matches(hasErrorText("Passwords do not match!")));
    }



    @Test
    public void checkEmailValidation(){
        Matcher<View> ogEmail = withId(R.id.email);
        Matcher<View> cEmail = withId(R.id.confirm_email);
        onView(ogEmail).check(matches(isDisplayed()));
        onView(cEmail).check(matches(isDisplayed()));
        onView(ogEmail).perform(typeText("tegen@test.com"));

        onView(cEmail).perform(typeText("tege@test.com"));
        onView(cEmail).check(matches(hasErrorText("Emails do not match!")));

        onView(cEmail).perform(clearText());

        onView(cEmail).check(matches(hasErrorText("Emails do not match!")));
        onView(cEmail).perform(typeText("tegen@test.com"));
        onView(ogEmail).perform(clearText());
        onView(ogEmail).perform(typeText("tegen@test.com"));



    }

    @Test
    public void checkPasswordValidation() throws InterruptedException {
        Matcher<View> password = withId(R.id.password);
        Matcher<View> cPassword = withId(R.id.confirmPassword);
        onView(password).check(matches(isDisplayed()));
        onView(cPassword).check(matches(isDisplayed()));
        onView(password).perform(typeText("tegen"));
        onView(cPassword).perform(typeText("tegn"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        onView(cPassword).check(matches(hasErrorText("Passwords do not match!")));

//        onView(password).perform(typeText("tegen@test.com"));
//        onView(cPassword).perform(typeText("tegen@test.com"));
//        onView(password).perform(clearText());
//        onView(cPassword).check(matches(hasErrorText("Passwords do not match!")));

    }





}