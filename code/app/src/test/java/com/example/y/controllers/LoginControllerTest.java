package com.example.y.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.example.y.services.AuthManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LoginControllerTest {

    @Mock
    AuthManager mockAuthManager;

    private LoginController loginController;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        loginController = new LoginController();
        loginController.setAuthManager(mockAuthManager);
    }

    @Test
    public void TestOnLogInUser() {
        // Test empty username error
        try {
            loginController.onLoginUser("", "password", user -> {}, e -> assertEquals(0, 0));
        } catch (Exception e) {
            fail("Attempted to log in with empty username");
        }

        // Test empty password error
        try {
            loginController.onLoginUser("username", "", user -> {}, e -> assertEquals(0, 0));
        } catch (Exception e) {
            fail("Attempted to log in with empty password");
        }
    }

}
