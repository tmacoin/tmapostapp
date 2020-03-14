package org.tmacoin.post.android.posting;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.tma.peer.Network;
import org.tma.peer.thin.Ratee;
import org.tma.peer.thin.Rating;
import org.tma.peer.thin.ResponseHolder;
import org.tma.peer.thin.SearchRatingForRaterRequest;
import org.tma.peer.thin.SearchRatingRequest;
import org.tma.util.StringUtil;
import org.tmacoin.post.android.AndroidExecutor;
import org.tmacoin.post.android.R;

import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.TmaAndroidUtil;

import java.util.Date;
import java.util.List;

public class ShowPostActivity extends BaseActivity {

    private String result = "";
    private List<Rating> list;
    private Ratee ratee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_post_wait);
        ratee = (Ratee) getIntent().getSerializableExtra("ratee");


        process();
    }

    private void process() {
        new AndroidExecutor() {

            @Override
            public void start() throws Exception {
                processAsync();
            }

            @Override
            public void finish() {
                processSync();
            }
        }.run();
    }

    private void processAsync() throws Exception {
        findRatings();
    }

    private void findRatings() {
        Network network = Network.getInstance();
        TmaAndroidUtil.checkNetwork();

        SearchRatingRequest request = new SearchRatingRequest(network, ratee.getName(), ratee.getTransactionId());
        request.start();

        list = (List<Rating>) ResponseHolder.getInstance().getObject(request.getCorrelationId());

        if(list == null) {
            result  = ("Failed to retrieve ratings. Please try again");
            return;
        }

        result = ("Total number of comments found for " + ratee.getName() + ": " + list.size());

    }

    private void processSync() {
        setContentView(R.layout.activity_show_post);

        if(!StringUtil.isEmpty(result)) {
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        }
        if(list == null || list.isEmpty()) {
            return;
        }
        ListView listView = findViewById(R.id.simpleListView);
        RatingAdapter arrayAdapter = new RatingAdapter(this, list);
        listView.setAdapter(arrayAdapter);

        View header =  getLayoutInflater().inflate(R.layout.activity_show_post_header, null);
        listView.addHeaderView(header, null, false);

        TextView textViewPost = header.findViewById(R.id.textViewPost);
        textViewPost.setText(ratee.getName());
        TextView textViewDescription = header.findViewById(R.id.textViewDescription);
        textViewDescription.setText(ratee.getDescription());
        TextView textViewDate = header.findViewById(R.id.textViewDate);
        textViewDate.setText(new Date(ratee.getTimeStamp()).toString());
        TextView textViewIdentifier = header.findViewById(R.id.textViewIdentifier);
        textViewIdentifier.setText(ratee.getTransactionId());

        TextView totalRating = header.findViewById(R.id.totalRating);
        totalRating.setText("Total rating is " + ratee.getTotalRating());

    }
}
