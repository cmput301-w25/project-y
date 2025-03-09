package com.example.y.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.y.models.FollowRequest;
import com.example.y.repositories.FollowRequestRepository;
import com.example.y.services.SessionManager;
import com.example.y.utils.FollowRequestArrayAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;

public class FollowRequestControllerTest {

    private FollowRequestController followRequestController;

    @Before
    public void setUp() {
        followRequestController = new FollowRequestController();
    }
    @Test
    public void testInsertReq() {
        // Make reqs empty at initially
        followRequestController.setReqs(new ArrayList<FollowRequest>());

        // In the order they should be:
        FollowRequest r0 = new FollowRequest("0", "user", new Timestamp(4, 0));
        FollowRequest r1 = new FollowRequest("1", "user", new Timestamp(3, 0));
        FollowRequest r2 = new FollowRequest("2", "user", new Timestamp(2, 0));
        FollowRequest r3 = new FollowRequest("3", "user", new Timestamp(1, 0));

        Runnable assertAll = () -> {
            assertEquals(followRequestController.getReqs().get(0).getRequester(), "0");
            assertEquals(followRequestController.getReqs().get(1).getRequester(), "1");
            assertEquals(followRequestController.getReqs().get(2).getRequester(), "2");
            assertEquals(followRequestController.getReqs().get(3).getRequester(), "3");
        };

        // Test inserting in order
        followRequestController.insertReq(r0);
        followRequestController.insertReq(r1);
        followRequestController.insertReq(r2);
        followRequestController.insertReq(r3);
        assertAll.run();

        // Test inserting backwards
        followRequestController.insertReq(r3);
        followRequestController.insertReq(r2);
        followRequestController.insertReq(r1);
        followRequestController.insertReq(r0);
        assertAll.run();

        // Test inserting in the middle
        followRequestController.insertReq(r0);
        followRequestController.insertReq(r3);
        followRequestController.insertReq(r2);
        followRequestController.insertReq(r1);
        assertAll.run();
        followRequestController.insertReq(r0);
        followRequestController.insertReq(r3);
        followRequestController.insertReq(r1);
        followRequestController.insertReq(r2);
        assertAll.run();


    }

}
