package org.tmacoin.post.android.posting;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.tma.peer.Network;
import org.tma.peer.thin.Rating;
import org.tma.peer.thin.ResponseHolder;
import org.tma.peer.thin.SearchRatingForRaterRequest;
import org.tma.util.StringUtil;
import org.tmacoin.post.android.AndroidExecutor;
import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.TmaAndroidUtil;

import java.util.List;

public class MyRatingsActivity extends BaseActivity {

    private String result = "";
    private List<Rating> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ratings_wait);
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
        String rater  =  network.getTmaAddress();
        TmaAndroidUtil.checkNetwork();

        SearchRatingForRaterRequest
                request = new SearchRatingForRaterRequest(network, rater);
        request.start();

        list = (List<Rating>) ResponseHolder.getInstance().getObject(request.getCorrelationId());

        if(list == null) {
            result  = ("Failed to retrieve ratings. Please try again");
            return;
        }

        result = ("Total number of comments found for " + rater + ": " + list.size());

    }

    private void processSync() {
        setContentView(R.layout.activity_my_ratings);
        if(!StringUtil.isEmpty(result)) {
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        }
        if(list == null || list.isEmpty()) {
            return;
        }
        ListView listView = findViewById(R.id.simpleListView);
        RatingAdapter arrayAdapter = new RatingAdapter(this, list);
        listView.setAdapter(arrayAdapter);
    }


}
