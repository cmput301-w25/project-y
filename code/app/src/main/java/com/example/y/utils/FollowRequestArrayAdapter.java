package com.example.y.utils;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.y.R;
import com.example.y.models.FollowRequest;
import com.example.y.repositories.FollowRequestRepository;
import com.example.y.views.UserProfileActivity;

import java.util.ArrayList;

/**
 * Array adapter for showing follow requests in a list
 */
public class FollowRequestArrayAdapter extends ArrayAdapter<FollowRequest> {

    private final Context context;
    private final ArrayList<FollowRequest> requests;

    /**
     * Initializes the adapter
     * @param context  The application context
     * @param reqs The list of follow requests to display
     */
    public FollowRequestArrayAdapter(Context context, ArrayList<FollowRequest> reqs) {
        super(context, 0, reqs);
        requests = reqs;
        this.context = context;
    }

    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.follow_request_list_content, parent, false);
        }

        FollowRequest req = requests.get(position);
        TextView usernameTextView = view.findViewById(R.id.username);
        usernameTextView.setText(req.getRequester());


        // Accept and reject button logic
        view.findViewById(R.id.acceptBtn).setOnClickListener(v -> {
            FollowRequestRepository.getInstance().acceptRequest(req, unused -> {}, this::handleException);
        });
        view.findViewById(R.id.rejectBtn).setOnClickListener(v -> {
            FollowRequestRepository.getInstance().deleteFollowRequest(req.getRequester(), req.getRequestee(), unused -> {}, this::handleException);
        });
        view.findViewById(R.id.followReqView).setOnClickListener(v -> {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("user", req.getRequester());
            context.startActivity(intent);
        });
        return view;
    }

    /**
     * Handle exception by showing a message
     * @param e exception
     */
    private void handleException(Exception e) {
        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

}
