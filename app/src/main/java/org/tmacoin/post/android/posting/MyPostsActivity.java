package org.tmacoin.post.android.posting;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import org.tma.peer.Network;
import org.tma.peer.thin.Ratee;
import org.tma.peer.thin.ResponseHolder;
import org.tma.peer.thin.SearchPostsRequest;
import org.tmacoin.post.android.AndroidExecutor;
import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.TmaAndroidUtil;

import java.util.List;

public class MyPostsActivity extends BaseActivity {

    private String result = "";
    private List<Ratee> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts_wait);
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
        findPosts();
    }

    private void findPosts() {

        Network network = Network.getInstance();
        String tmaAddress  =  network.getTmaAddress();
        TmaAndroidUtil.checkNetwork();

        SearchPostsRequest request = new SearchPostsRequest(network, tmaAddress);
        request.start();
        @SuppressWarnings("unchecked")
        List<Ratee> list = (List<Ratee>) ResponseHolder.getInstance().getObject(request.getCorrelationId());

        if(list == null) {
            result  = ("Failed to retrieve posts. Please try again");
            return;
        }

        result = ("Total number of posts found for " + tmaAddress + ": " + list.size());

    }

    private void processSync() {
        setContentView(R.layout.activity_my_posts);
        TextView resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setText(result);
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        updateStatus(getResources().getString(R.string.network_status) + ": " + Network.getInstance().getPeerCount().toString());
    }


}
