package com.example.y.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class SignUpControllerTest {

    private SignUpController signUpController;
    private String validEmail;
    private String validName;
    private String validUsername;
    private String validPassword;

    @Before
    public void setUp() {
        signUpController = new SignUpController();
        validEmail = "email@email.com";
        validName = "Name";
        validUsername = "username";
        validPassword = "password";
    }

    @Test
    public void testOnSignUp_UsernameEmpty() {
        try {
            signUpController.onSignUpUser(validEmail, validEmail, validName, "", validPassword, validPassword, unused -> {}, e -> {
                assertEquals("Username is required", e.getMessage());
            });
        } catch (Exception e) {
            fail("Sign up allowed with empty username");
        }
    }

    @Test
    public void testOnSignUp_UsernameTooShort() {
        try {
            signUpController.onSignUpUser(validEmail, validEmail, validName, "user", validPassword, validPassword, unused -> {}, e -> {
                assertEquals("Username length must be at least 5 and at most 20", e.getMessage());
            });
        } catch (Exception e) {
            fail("Sign up allowed with username length less than 5");
        }
    }

    @Test
    public void testOnSignUp_UsernameInvalidCharacters() {
        try {
            signUpController.onSignUpUser(validEmail, validEmail, validName, "1username", validPassword, validPassword, unused -> {}, e -> {
                assertEquals("Username can only contain letters, numbers, and underscores, and must not start with a number", e.getMessage());
            });
        } catch (Exception e) {
            fail("Sign up allowed with invalid username characters");
        }
    }

    @Test
    public void testOnSignUp_NameEmpty() {
        try {
            signUpController.onSignUpUser(validEmail, validEmail, "", validUsername, validPassword, validPassword, unused -> {}, e -> {
                assertEquals("Name is required", e.getMessage());
            });
        } catch (Exception e) {
            fail("Sign up allowed with empty name");
        }
    }

    @Test
    public void testOnSignUp_EmailEmpty() {
        try {
            signUpController.onSignUpUser("", "", validName, validUsername, validPassword, validPassword, unused -> {}, e -> {
                assertEquals("Email is required", e.getMessage());
            });
        } catch (Exception e) {
            fail("Sign up allowed with empty email");
        }
    }

    @Test
    public void testOnSignUp_ConfirmEmailEmpty() {
        try {
            signUpController.onSignUpUser(validEmail, "", validName, validUsername, validPassword, validPassword, unused -> {}, e -> {
                assertEquals("Confirm email is required", e.getMessage());
            });
        } catch (Exception e) {
            fail("Sign up allowed with empty confirm email");
        }
    }

    @Test
    public void testOnSignUp_EmailsDoNotMatch() {
        String differentEmail = "different@email.com";
        try {
            signUpController.onSignUpUser(validEmail, differentEmail, validName, validUsername, validPassword, validPassword, unused -> {}, e -> {
                assertEquals("Email and confirm email don't match", e.getMessage());
            });
        } catch (Exception e) {
            fail("Sign up allowed with mismatched emails");
        }
    }

    @Test
    public void testOnSignUp_InvalidEmailFormat() {
        String invalidEmail = "invalid-email";
        try {
            signUpController.onSignUpUser(invalidEmail, invalidEmail, validName, validUsername, validPassword, validPassword, unused -> {}, e -> {
                assertEquals("Invalid email format", e.getMessage());
            });
        } catch (Exception e) {
            fail("Sign up allowed with invalid email format");
        }
    }

    @Test
    public void testOnSignUp_PasswordEmpty() {
        try {
            signUpController.onSignUpUser(validEmail, validEmail, validName, validUsername, "", "", unused -> {}, e -> {
                assertEquals("Password is required", e.getMessage());
            });
        } catch (Exception e) {
            fail("Sign up allowed with empty password");
        }
    }

    @Test
    public void testOnSignUp_ConfirmPasswordEmpty() {
        try {
            signUpController.onSignUpUser(validEmail, validEmail, validName, validUsername, validPassword, "", unused -> {}, e -> {
                assertEquals("Confirm password is required", e.getMessage());
            });
        } catch (Exception e) {
            fail("Sign up allowed with empty confirm password");
        }
    }

    @Test
    public void testOnSignUp_PasswordTooShort() {
        String shortPassword = "pass";
        try {
            signUpController.onSignUpUser(validEmail, validEmail, validName, validUsername, shortPassword, shortPassword, unused -> {}, e -> {
                assertEquals("Password length must be at least 5", e.getMessage());
            });
        } catch (Exception e) {
            fail("Sign up allowed with password length less than 5");
        }
    }

    @Test
    public void testOnSignUp_PasswordSameAsUsername() {
        try {
            signUpController.onSignUpUser(validEmail, validEmail, validName, validUsername, validUsername, validUsername, unused -> {}, e -> {
                assertEquals("Password cannot be the same as username", e.getMessage());
            });
        } catch (Exception e) {
            fail("Sign up allowed with password same as username");
        }
    }

    @Test
    public void testOnSignUp_PasswordsDoNotMatch() {
        String differentPassword = "differentPassword";
        try {
            signUpController.onSignUpUser(validEmail, validEmail, validName, validUsername, validPassword, differentPassword, unused -> {}, e -> {
                assertEquals("Password and confirm password don't match", e.getMessage());
            });
        } catch (Exception e) {
            fail("Sign up allowed with mismatched passwords");
        }
    }

}