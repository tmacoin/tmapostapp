package org.tmacoin.post.android.tmitter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.tma.peer.thin.TwitterAccount;
import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.persistance.SubscriptionStore;

import java.util.List;

public class MySubscriptionsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_subscriptions);

        List<TwitterAccount> subscribedAccounts = SubscriptionStore.getInstance().getSubscriptions();

        if(subscribedAccounts.isEmpty()) {
            return;
        }

        TextView resultTextView = findViewById(R.id.textViewMessage);
        resultTextView.setText("Number of subscription(s) found: " + subscribedAccounts.size());

        ListView listView = findViewById(R.id.simpleListView);
        TmitterAdapter arrayAdapter = new TmitterAdapter(this, subscribedAccounts);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final TwitterAccount twitterAccount = (TwitterAccount) parent.getItemAtPosition(position);
                showMessage(twitterAccount);
            }
        });
    }

    private void showMessage(TwitterAccount twitterAccount) {
        Intent intent = new Intent(this, ShowMyTweetsActivity.class);
        intent.putExtra("twitterAccount", twitterAccount);
        startActivity(intent);
    }
}
