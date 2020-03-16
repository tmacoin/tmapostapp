package org.tmacoin.post.android;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.tmacoin.post.android.messaging.NewMessageNotifier;
import org.tmacoin.post.android.messaging.SendMessageActivity;
import org.tmacoin.post.android.messaging.ShowMessagesActivity;
import org.tmacoin.post.android.posting.CreatePost;
import org.tmacoin.post.android.posting.FindPostActivity;
import org.tmacoin.post.android.posting.MyPostsActivity;
import org.tmacoin.post.android.posting.MyRatingsActivity;
import org.tmacoin.post.android.tmitter.CreateAccount;
import org.tmacoin.post.android.tmitter.MySubscriptionsActivity;
import org.tmacoin.post.android.tmitter.SearchTwitterActivity;
import org.tmacoin.post.android.tmitter.SendTmeetActivity;
import org.tmacoin.post.android.tmitter.ShowMyTweetsActivity;

public class MenuHandler {

    public void handleExit(Activity activity) {
        Intent service = new Intent(activity, NewMessageNotifier.class);
        service.setAction(TmaAndroidUtil.STOP);
        ContextCompat.startForegroundService(activity, service);
    }

    public void handleChangePassword(Activity activity) {
        Intent intent = new Intent(activity, ChangePasswordActivity.class);
        activity.startActivity(intent);
    }

    public void handleGetFilesConfig(Activity activity) {
        Intent intent = new Intent(activity, GetFilesConfig.class);
        activity.startActivity(intent);
    }

    public void handleExportFilesConfig(Activity activity) {
        Intent intent = new Intent(activity, ExportFilesConfig.class);
        activity.startActivity(intent);
    }

    public void handleGetMyBalance(Activity activity) {
        Intent intent = new Intent(activity, GetMyBalanceActivity.class);
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
        Intent intent = new Intent(activity, ShowMessagesActivity.class);
        activity.startActivity(intent);
    }

    public void handleCreateTwitter(Activity activity) {
        Intent intent = new Intent(activity, CreateAccount.class);
        activity.startActivity(intent);
    }

    public void handleShowMyTweets(Activity activity) {
        Intent intent = new Intent(activity, ShowMyTweetsActivity.class);
        activity.startActivity(intent);
    }

    public void handleSendTweet(Activity activity) {
        Intent intent = new Intent(activity, SendTmeetActivity.class);
        activity.startActivity(intent);
    }

    public void handleSearchTwitter(Activity activity) {
        Intent intent = new Intent(activity, SearchTwitterActivity.class);
        activity.startActivity(intent);
    }

    public void handleMySubscription(Activity activity) {
        Intent intent = new Intent(activity, MySubscriptionsActivity.class);
        activity.startActivity(intent);
    }


    public void handleCreatePost(Activity activity) {
        Intent intent = new Intent(activity, CreatePost.class);
        activity.startActivity(intent);
    }

    public void handleFindPost(Activity activity) {
        Intent intent = new Intent(activity, FindPostActivity.class);
        activity.startActivity(intent);
    }

    public void handleMyRatings(Activity activity) {
        Intent intent = new Intent(activity, MyRatingsActivity.class);
        activity.startActivity(intent);
    }

    public void handleMyPosts(Activity activity) {
        Intent intent = new Intent(activity, MyPostsActivity.class);
        activity.startActivity(intent);
    }

    public void handleViewLog(Activity activity) {
        Intent intent = new Intent(activity, LogViewerActivity.class);
        activity.startActivity(intent);
    }

    public void handleShowPeers(Activity activity) {
        Intent intent = new Intent(activity, ShowPeersActivity.class);
        activity.startActivity(intent);
    }

}
