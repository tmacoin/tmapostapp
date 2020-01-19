package org.tmacoin.post.android.messaging;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import org.tma.peer.Network;
import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.R;

public class ShowMessagesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_messages);
        TextView myMessagesTextView = findViewById(R.id.myMessagesTextView);
        myMessagesTextView.setText("Messages for " + Network.getInstance().getTmaAddress());
    }
}
