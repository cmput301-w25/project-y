package com.example.y.controllers;

import android.app.Activity;
import android.content.Context;

import com.example.y.models.FollowRequest;
import com.example.y.repositories.FollowRequestRepository;
import com.example.y.services.SessionManager;
import com.example.y.utils.FollowRequestArrayAdapter;
import com.example.y.views.FollowRequestsActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;

import java.util.ArrayList;

/**
 * Controller to manage follow requests
 * Listens for follow requests and updates UI
 */
public class FollowRequestController implements FollowRequestRepository.FollowRequestListener {

    private String user;
    private Context context;
    private FollowRequestArrayAdapter adapter;
    private ArrayList<FollowRequest> reqs;

    public FollowRequestController() {}

    /**
     * Starts controller and gets follow requests sent to current user
     *
     * @param context   the context
     * @param onSuccess Callback for successful initialization
     * @param onFailure Callback for initialization failure
     */
    public FollowRequestController(Context context, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.context = context;

        SessionManager sessionManager = new SessionManager(context);
        user = sessionManager.getUsername();

        // Get all requests to logged in user, then set adapter, then add this to follow req listener
        FollowRequestRepository.getInstance().getAllRequestsTo(user, reqs -> {
            // Populate data
            this.reqs = new ArrayList<FollowRequest>(reqs);

            // Listen for request updates
            FollowRequestRepository.getInstance().addListener(this);

            // Create adapter
            adapter = new FollowRequestArrayAdapter(context, this.reqs);

            // Check if empty
            updateEmptyState();

            onSuccess.onSuccess(null);
        }, onFailure);
    }

    /**
     * Call when a new follow request is sent to user
     *
     * @param followRequest Follow request record to be added.
     */
    @Override
    public void onFollowRequestAdded(FollowRequest followRequest) {
        if (followRequest.getRequestee().equals(user)) {
            insertReq(followRequest);
            updateEmptyState();
            notifyAdapter();
        }
    }

    /**
     * call when user declines a follow request
     *
     * @param requester Username of the requester of the follow request that was deleted.
     * @param requestee Username of the requestee of the follow record that was deleted.
     */
    @Override
    public void onFollowRequestDeleted(String requester, String requestee) {
        if (requestee.equals(user)) {
            reqs.removeIf(req -> req.getRequester().equals(requester));
            updateEmptyState();
            notifyAdapter();
        }
    }

    /**
     * Inserts a follow request into the adapter list by date time descending.
     * Uses binary search on date time in order to keep the array sorted.
     *
     * @param req Follow request to insert.
     */
    protected void insertReq(FollowRequest req) {
        Timestamp key = req.getTimestamp();

        // Binary search for insertion spot
        int low = 0;
        int high = reqs.size() - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int cmp = reqs.get(mid).getTimestamp().compareTo(key);

            if (cmp == 0) {
                if (reqs.get(mid).getRequester().equals(req.getRequester())) {
                    return; // Request already exists
                } else {
                    high = mid - 1; // Continue searching
                }
            } else if (cmp < 0) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        // Insert follow request:wq
        reqs.add(low, req);
    }

    /**
     * Notifies the follow request adapter that there was a change.
     * This update happens in the main thread.
     */
    private void notifyAdapter() {
        if (adapter != null && context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> adapter.notifyDataSetChanged());
        }
    }

    /**
     * Returns the adapter for the list of follow requests
     *
     * @return FollowRequestArrayAdapter
     */
    public FollowRequestArrayAdapter getAdapter() {
        return adapter;
    }

    public ArrayList<FollowRequest> getReqs() {
        return reqs;
    }

    public void setReqs(ArrayList<FollowRequest> reqs) {
        this.reqs = reqs;
    }

    private void updateEmptyState() {
        if (context instanceof FollowRequestsActivity) {
            FollowRequestsActivity activity = (FollowRequestsActivity) context;
            ((Activity) context).runOnUiThread(() -> {
                if (reqs == null || reqs.isEmpty()) {
                    activity.setvisible();
                } else {
                    activity.setnotvisible();
                }
            });
        }
    }


}
