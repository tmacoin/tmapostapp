package org.tmacoin.post.android;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MenuHandler menuHandler = new MenuHandler();
        switch (item.getItemId()) {
            case R.id.exit:
                menuHandler.handleExit(this);
                return true;
            case R.id.change_password:
                menuHandler.handleChangePassword(this);
                return true;
            case R.id.get_balance:
                menuHandler.handleGetBalance(this);
                return true;
            case R.id.send_transaction:
                menuHandler.handleSendTransaction(this);
                return true;
            case R.id.show_address:
                menuHandler.handleShowAddress(this);
                return true;

            case R.id.send_message:
                menuHandler.handleSendSecureMessage(this);
                return true;
            case R.id.show_messages:
                menuHandler.handleShowMessages(this);
                return true;

            case R.id.create_twitter:
                menuHandler.handleCreateTwitter(this);
                return true;
            case R.id.show_my_tweets:
                menuHandler.handleShowMyTweets(this);
                return true;
            case R.id.send_tweet:
                menuHandler.handleSendTweet(this);
                return true;
            case R.id.search_twitter:
                menuHandler.handleSearchTwitter(this);
                return true;
            case R.id.my_subscription:
                menuHandler.handleMySubscription(this);
                return true;

            case R.id.create_post:
                menuHandler.handleCreatePost(this);
                return true;
            case R.id.find_post:
                menuHandler.handleFindPost(this);
                return true;
            case R.id.my_ratings:
                menuHandler.handleMyRatings(this);
                return true;
            case R.id.my_posts:
                menuHandler.handleMyPosts(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
