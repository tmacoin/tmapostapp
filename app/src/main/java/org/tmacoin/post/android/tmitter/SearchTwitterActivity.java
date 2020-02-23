package org.tmacoin.post.android.tmitter;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.tma.blockchain.Wallet;
import org.tma.peer.BootstrapRequest;
import org.tma.peer.Network;
import org.tma.peer.thin.ResponseHolder;
import org.tma.peer.thin.SearchTwitterRequest;
import org.tma.peer.thin.SecureMessage;
import org.tma.peer.thin.TwitterAccount;
import org.tma.util.StringUtil;
import org.tma.util.TmaLogger;
import org.tmacoin.post.android.AndroidExecutor;
import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.R;
import org.tmacoin.post.android.Wallets;
import org.tmacoin.post.android.messaging.AddAddressActivity;
import org.tmacoin.post.android.messaging.MessageAdapter;

import java.util.Collection;
import java.util.List;

public class SearchTwitterActivity extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();

    private String tmitterAccount;
    private String result = "";
    List<TwitterAccount> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_twitter);
        Button buttonSearch = findViewById(R.id.buttonSearch);
        buttonSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
                buttonClicked();
            }
        });

        EditText editTextAccountName = findViewById(R.id.editTextAccountName);
        editTextAccountName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    hideKeyboard(v);
                    buttonClicked();
                }
                return false;
            }
        });

        updateStatus(getResources().getString(R.string.network_status) + ": " + Network.getInstance().getPeerCount().toString());
    }

    private void buttonClicked() {

        load();
        if(!validate()) {
            return;
        }
        Toast.makeText(SearchTwitterActivity.this, getResources().getString(R.string.wait), Toast.LENGTH_LONG).show();
        setContentView(R.layout.activity_search_tmitter_wait);
        updateStatus(getResources().getString(R.string.network_status) + ": " + Network.getInstance().getPeerCount().toString());
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
        if(searchTweet()) {
            result = "Number of accounts found: " + list.size();
        }
    }

    private boolean searchTweet() {
        Network network = Network.getInstance();
        if(!network.isPeerSetComplete()) {
            BootstrapRequest.getInstance().start();
        }

        SearchTwitterRequest request = new SearchTwitterRequest(network, tmitterAccount);
        request.start();

        list = (List<TwitterAccount>) ResponseHolder.getInstance().getObject(request.getCorrelationId());

        if(list == null) {
            result = ("Failed to retrieve tmitter accounts. Please try again");
            return false;
        }
        return true;
    }


    private void processSync() {
        setContentView(R.layout.activity_search_tmitter_complete);
        TextView resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setText(result);
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();

        ListView listView = findViewById(R.id.simpleListView);
        TmitterAdapter arrayAdapter = new TmitterAdapter(this, list);
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
        intent.putExtra("tmaAddress", twitterAccount.getTmaAddress());
        startActivity(intent);
    }


    private void load() {
        EditText editTextAccountName = findViewById(R.id.editTextAccountName);
        tmitterAccount = StringUtil.trim(editTextAccountName.getText().toString());
    }

    private boolean validate() {
        if (StringUtil.isEmpty(tmitterAccount)) {
            Toast.makeText(this, getResources().getString(R.string.enter_tmitter_account), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}

