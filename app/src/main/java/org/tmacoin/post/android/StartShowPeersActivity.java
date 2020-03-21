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

public class StartShowPeersActivity extends StartActivity {

    private static final TmaLogger logger = TmaLogger.getLogger();
    private boolean active = true;
    private String text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_peers);
        active = true;
        ThreadExecutor.getInstance().execute(new TmaRunnable("NewMessageNotifier") {
            public void doRun() {
                process();
            }
        });
    }

    private void process() {
        final TextView peersTextView = findViewById(R.id.peers_text_view);
        while(active) {
            Network network = Network.getInstance();
            if(network == null) {
                ThreadExecutor.sleep(Constants.ONE_SECOND);
                continue;
            }
            final StringBuilder stringBuilder = new StringBuilder();
            List<Peer> list = network.getConnectedPeers();
            for(Peer peer: list) {
                stringBuilder.append(peer.getRawSocket().toString()).append(System.lineSeparator());
            }
            String localText = stringBuilder.toString();
            if(text.equals(localText)) {
                return;
            }
            text = localText;
            new Handler(Looper.getMainLooper()).post(new Runnable(){
                @Override
                public void run() {
                    try {
                        peersTextView.setText(text);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            });

            ThreadExecutor.sleep(Constants.ONE_SECOND);
        }
    }

    @Override
    protected void onDestroy() {
        active = false;
        super.onDestroy();
    }

}
