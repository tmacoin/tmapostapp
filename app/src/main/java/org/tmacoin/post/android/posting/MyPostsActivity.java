package org.tmacoin.post.android.posting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.tma.peer.Network;
import org.tma.peer.thin.Ratee;
import org.tma.peer.thin.ResponseHolder;
import org.tma.peer.thin.SearchPostsRequest;
import org.tma.util.StringUtil;
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

        list = (List<Ratee>) ResponseHolder.getInstance().getObject(request.getCorrelationId());

        if(list == null) {
            result  = ("Failed to retrieve posts. Please try again");
            return;
        }

        result = "";

    }

    private void processSync() {
        setContentView(R.layout.activity_my_posts);
        if(!StringUtil.isEmpty(result)) {
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        }
        if(list == null || list.isEmpty()) {
            setContentView(R.layout.activity_show_no_post);
            return;
        }
        ListView listView = findViewById(R.id.simpleListView);
        RateeAdapter arrayAdapter = new RateeAdapter(this, list);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Ratee ratee = (Ratee) parent.getItemAtPosition(position);
                showPost(ratee);
            }
        });
    }

    private void showPost(Ratee ratee) {
        Intent intent = new Intent(this, ShowPostActivity.class);
        intent.putExtra("ratee", ratee);
        startActivity(intent);
    }


}
