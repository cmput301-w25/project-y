package com.example.y.controllers;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.y.models.FollowRequest;
import com.example.y.repositories.FollowRequestRepository;
import com.example.y.services.SessionManager;
import com.example.y.utils.FollowRequestArrayAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class FollowRequestController implements FollowRequestRepository.FollowRequestListener {

    private final String user;
    private final Context context;
    private FollowRequestArrayAdapter adapter;
    private ArrayList<FollowRequest> reqs;
    private TextView emptyTextView;

    public FollowRequestController(Context context, TextView emptyTextView, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        this.context = context;
        this.emptyTextView = emptyTextView;

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
        }, e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onFollowRequestAdded(FollowRequest followRequest) {
        if (followRequest.getRequestee().equals(user)) {
            insertReq(followRequest);
            updateEmptyState();
            notifyAdapter();
        }
    }

    @Override
    public void onFollowRequestDeleted(String requester, String requestee) {
        if (requestee.equals(user)) {
            reqs.removeIf(req -> req.getRequester().equals(requester));
            updateEmptyState();
            notifyAdapter();
        }
    }

    /**
     * Removes controller from follow request repository's listener set
     */
    public void onActivityStop() {
        FollowRequestRepository.getInstance().removeListener(this);
    }

    /**
     * Inserts a follow request into the adapter list by date time descending.
     * Uses binary search on date time in order to keep the array sorted.
     * @param req
     *      Follow request to insert.
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

        // Insert follow request
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

    public FollowRequestArrayAdapter getAdapter() { return adapter; }


    private void updateEmptyState() {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> {
                if (reqs.isEmpty()) {
                    emptyTextView.setVisibility(View.VISIBLE);
                } else {
                    emptyTextView.setVisibility(View.GONE); //
                }
            });
        }
    }
}
