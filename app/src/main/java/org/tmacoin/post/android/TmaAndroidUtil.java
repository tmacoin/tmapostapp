package org.tmacoin.post.android;

import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import org.tma.peer.BootstrapRequest;
import org.tma.peer.Network;
import org.tma.util.ThreadExecutor;
import org.tma.util.TmaRunnable;

public class TmaAndroidUtil {

    public static final String STOP = "stop";
    public static final String START = "start";

    public static void enableScroll(final EditText editText) {

        editText.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (editText.hasFocus()) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK){
                        case MotionEvent.ACTION_SCROLL:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            return true;
                    }
                }
                return false;
            }
        });

    }

    public static void checkNetwork() {

        final Network network = Network.getInstance();
        if (!network.isPeerSetCompleteForMyShard()) {
            if (network.getMyPeers().isEmpty()) {
                new BootstrapRequest(network).start();
            } else {
                ThreadExecutor.getInstance().execute(new TmaRunnable("checkNetwork") {
                    public void doRun() {
                        new BootstrapRequest(network).start();
                    }
                });
            }
        }

    }
}
