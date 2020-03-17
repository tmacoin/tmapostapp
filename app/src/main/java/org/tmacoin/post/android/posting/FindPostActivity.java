package org.tmacoin.post.android.posting;

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

import org.tma.peer.Network;
import org.tma.peer.thin.Ratee;
import org.tma.peer.thin.ResponseHolder;
import org.tma.peer.thin.SearchRateesRequest;
import org.tma.util.StringUtil;
import org.tmacoin.post.android.AndroidContants;
import org.tmacoin.post.android.AndroidExecutor;
import org.tmacoin.post.android.R;

import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.TmaAndroidUtil;
import org.tmacoin.post.android.tmitter.TmeetAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FindPostActivity extends BaseActivity {

    private String post;
    private Set<String> words;
    private String result = "";
    private List<Ratee> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_post);

        Button buttonFindPost = findViewById(R.id.buttonFindPost);
        buttonFindPost.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        buttonFindPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
                buttonClicked();

            }
        });
        updateStatus(getResources().getString(R.string.network_status) + ": " + Network.getInstance().getPeerCount().toString());

        EditText editTextKeywords = findViewById(R.id.editTextKeywords);
        editTextKeywords.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    hideKeyboard(v);
                    buttonClicked();
                }
                return false;
            }
        });
    }

    private void buttonClicked() {
        load();
        if(!validate()) {
            return;
        }
        Toast.makeText(FindPostActivity.this, getResources().getString(R.string.wait), Toast.LENGTH_LONG).show();
        setContentView(R.layout.activity_find_post_wait);
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
        findRatees();
    }

    private void findRatees() {

        Network network = Network.getInstance();
        TmaAndroidUtil.checkNetwork();

        SearchRateesRequest request = new SearchRateesRequest(network, post, words);
        request.start();
        list = (List<Ratee>) ResponseHolder.getInstance().getObject(request.getCorrelationId());

        if(list == null) {
            result = ("Failed to retrieve posts. Please try again");
            return;
        }

        if(list.size() == 0) {
            result = ("No posts were found for provided keywords.");
            return;
        }

        result = "";

    }

    private void processSync() {
        setContentView(R.layout.activity_find_post_complete);
        if(!StringUtil.isEmpty(result)) {
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        }
        if(list == null || list.isEmpty()) {
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

    private boolean validate() {
        if (StringUtil.isEmpty(post) && words.isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.post_and_keywords_cannot_be_empty), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void load() {
        EditText editTextPost = findViewById(R.id.editTextPost);
        post = editTextPost.getText().toString().trim();
        EditText editTextKeywords = findViewById(R.id.editTextKeywords);
        String keywords = editTextKeywords.getText().toString().trim();
        words = TmaAndroidUtil.getKeywords(keywords);
    }

}
