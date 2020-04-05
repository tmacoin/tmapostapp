package org.tmacoin.post.android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import org.tma.peer.Network;
import org.tma.peer.Peer;
import org.tma.util.Constants;
import org.tma.util.ThreadExecutor;
import org.tma.util.TmaLogger;
import org.tma.util.TmaRunnable;

import java.util.List;

public class ShowTransactionsActivity extends BaseActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_transactions);
    }



}
