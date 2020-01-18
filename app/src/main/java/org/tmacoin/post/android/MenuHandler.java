package org.tmacoin.post.android;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.Toast;

import org.tmacoin.post.android.messaging.SendMessageActivity;

public class MenuHandler {

    public void handleExit(Activity activity) {
        Intent homeScreen = new Intent(Intent.ACTION_MAIN);
        homeScreen.addCategory(Intent.CATEGORY_HOME);
        homeScreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(homeScreen);
    }

    public void handleChangePassword(Activity activity) {
        Intent intent = new Intent(activity, ChangePasswordActivity.class);
        activity.startActivity(intent);
    }

    public void handleGetBalance(Activity activity) {
        Intent intent = new Intent(activity, GetBalanceActivity.class);
        activity.startActivity(intent);
    }

    public void handleSendTransaction(Activity activity) {
        Intent intent = new Intent(activity, SendTransactionActivity.class);
        activity.startActivity(intent);
    }

    public void handleShowAddress(Activity activity) {
        Intent intent = new Intent(activity, ConnectedToNetworkActivity.class);
        activity.startActivity(intent);
    }

    public void handleSendSecureMessage(Activity activity) {
        Intent intent = new Intent(activity, SendMessageActivity.class);
        activity.startActivity(intent);
    }

    public void handleShowMessages(Activity activity) {
        Toast.makeText(activity, activity.getResources().getString(R.string.show_messages), Toast.LENGTH_SHORT).show();
    }

    public void handleCreateTwitter(Activity activity) {
        Toast.makeText(activity, activity.getResources().getString(R.string.create_twitter), Toast.LENGTH_SHORT).show();
    }

    public void handleShowMyTweets(Activity activity) {
        Toast.makeText(activity, activity.getResources().getString(R.string.show_messages), Toast.LENGTH_SHORT).show();
    }

    public void handleSendTweet(Activity activity) {
        Toast.makeText(activity, activity.getResources().getString(R.string.send_tweet), Toast.LENGTH_SHORT).show();
    }

    public void handleSearchTwitter(Activity activity) {
        Toast.makeText(activity, activity.getResources().getString(R.string.search_twitter), Toast.LENGTH_SHORT).show();
    }

    public void handleMySubscription(Activity activity) {
        Toast.makeText(activity, activity.getResources().getString(R.string.my_subscription), Toast.LENGTH_SHORT).show();
    }


    public void handleCreatePost(Activity activity) {
        Toast.makeText(activity, activity.getResources().getString(R.string.create_post), Toast.LENGTH_SHORT).show();
    }

    public void handleFindPost(Activity activity) {
        Toast.makeText(activity, activity.getResources().getString(R.string.find_post), Toast.LENGTH_SHORT).show();
    }

    public void handleMyRatings(Activity activity) {
        Toast.makeText(activity, activity.getResources().getString(R.string.my_ratings), Toast.LENGTH_SHORT).show();
    }

    public void handleMyPosts(Activity activity) {
        Toast.makeText(activity, activity.getResources().getString(R.string.my_posts), Toast.LENGTH_SHORT).show();
    }
}
