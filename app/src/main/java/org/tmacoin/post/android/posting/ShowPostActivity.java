package org.tmacoin.post.android.posting;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import org.tma.peer.thin.Ratee;
import org.tmacoin.post.android.R;

import org.tmacoin.post.android.BaseActivity;

public class ShowPostActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_post);
        Ratee ratee = (Ratee) getIntent().getSerializableExtra("ratee");
        TextView textViewPost = findViewById(R.id.textViewPost);
        textViewPost.setText(ratee.getName());
        TextView textViewDescription = findViewById(R.id.textViewDescription);
        textViewDescription.setText(ratee.getDescription());
    }
}
