package com.example.y.utils;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static org.junit.Assert.*;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.y.R;
import com.example.y.models.Comment;
import com.example.y.views.UserProfileActivity;
import com.google.firebase.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

@RunWith(AndroidJUnit4.class)
public class CommentArrayAdapterTest {

    private Context context;
    private CommentArrayAdapter adapter;
    private ArrayList<Comment> comments;

    @Before
    public void setUp() {
        // Get an Activity context via ApplicationProvider (if possible, use an Activity context for startActivity())
        context = ApplicationProvider.getApplicationContext();
        comments = new ArrayList<>();

        // Create a sample Comment.
        Comment comment = new Comment();
        comment.setPosterUsername("testUser");
        comment.setText("This is a comment");
        // Use current timestamp for testing.
        comment.setTimestamp(new Timestamp(new Date()));
        comments.add(comment);

        // Create the adapter instance.
        adapter = new CommentArrayAdapter(context, comments);

        // Initialize Espresso Intents.
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testGetView_returnsProperView() {
        ViewGroup parent = new FrameLayout(context);
        View view = adapter.getView(0, null, parent);
        assertNotNull("View should not be null", view);

        TextView commentTextView = view.findViewById(R.id.commentText);
        TextView postingTimeView = view.findViewById(R.id.commentDateTime);
        assertNotNull("Comment text view should be present", commentTextView);
        assertNotNull("Posting time view should be present", postingTimeView);

        // Verify HTML-formatted comment text.
        Spanned expectedSpanned = Html.fromHtml("<b>testUser</b> This is a comment");
        String expectedHtml = Html.toHtml(expectedSpanned).trim();
        String actualHtml = Html.toHtml((Spanned) commentTextView.getText()).trim();
        assertEquals("Comment text should be properly formatted", expectedHtml, actualHtml);

        // Verify posting time.
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String expectedDate = sdf.format(comments.get(0).getTimestamp().toDate());
        assertEquals("Posting time should be formatted correctly", expectedDate, postingTimeView.getText().toString());
    }


    @Test
    public void testGetView_withNullTimestamp_showsJustNow() {
        // Create a comment with null timestamp.
        Comment commentNullTime = new Comment();
        commentNullTime.setPosterUsername("nullTimeUser");
        commentNullTime.setText("Comment without time");
        commentNullTime.setTimestamp(null);
        comments.add(commentNullTime);

        View view = adapter.getView(comments.size() - 1, null, new FrameLayout(context));
        TextView postingTimeView = view.findViewById(R.id.commentDateTime);
        assertEquals("If timestamp is null, posting time should be 'Just now'", "Just now", postingTimeView.getText().toString());
    }

    @Test
    public void testGetView_withNullText() {
        // Create a comment with null text.
        Comment commentNullText = new Comment();
        commentNullText.setPosterUsername("nullTextUser");
        commentNullText.setText(null);
        commentNullText.setTimestamp(new Timestamp(new Date()));
        comments.add(commentNullText);

        View view = adapter.getView(comments.size() - 1, null, new FrameLayout(context));
        TextView commentTextView = view.findViewById(R.id.commentText);
        // Expected formatting: "<b>nullTextUser</b> null"
        Spanned expectedSpanned = Html.fromHtml("<b>nullTextUser</b> null");
        String expectedHtml = Html.toHtml(expectedSpanned).trim();
        String actualHtml = Html.toHtml((Spanned) commentTextView.getText()).trim();
        assertEquals("If comment text is null, adapter should display 'null'", expectedHtml, actualHtml);
    }

    @Test
    public void testGetView_recyclesView() {
        ViewGroup parent = new FrameLayout(context);
        // Inflate initial view.
        View initialView = adapter.getView(0, null, parent);
        assertNotNull("Initial view should not be null", initialView);

        // Add a new comment.
        Comment comment2 = new Comment();
        comment2.setPosterUsername("otherUser");
        comment2.setText("Another comment");
        comment2.setTimestamp(new Timestamp(new Date()));
        comments.add(comment2);

        // Request view for second comment using the recycled view.
        View recycledView = adapter.getView(1, initialView, parent);
        assertSame("Recycled view should be reused", initialView, recycledView);

        TextView commentTextView = recycledView.findViewById(R.id.commentText);
        Spanned expectedSpanned = Html.fromHtml("<b>otherUser</b> Another comment");
        String expectedHtml = Html.toHtml(expectedSpanned).trim();
        String actualHtml = Html.toHtml((Spanned) commentTextView.getText()).trim();
        assertEquals("Recycled view should update its text for the new comment", expectedHtml, actualHtml);
    }
}
