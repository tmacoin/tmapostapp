package org.tmacoin.post.android.tmitter;

import android.os.Bundle;

import org.tma.util.TmaLogger;
import org.tmacoin.post.android.BaseActivity;
import org.tmacoin.post.android.R;

import java.io.File;

public class CreateAccount extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();
    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tmitter);
       //new CreateTwitterAction(frame, account, description, passwordField);

    }
}
