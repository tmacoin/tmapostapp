package org.tmacoin.post.android;

import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import org.tma.peer.BootstrapRequest;
import org.tma.peer.Network;
import org.tma.util.ThreadExecutor;
import org.tma.util.TmaRunnable;

import java.util.HashSet;
import java.util.Set;

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
        BootstrapRequest.getInstance().startAndWait();
    }

    public static Set<String> getKeywords(String keywords) {
        Set<String> set = new HashSet<>();
        String[] strings = keywords.split(" ");
        for(String str: strings) {
            if(set.size() > AndroidContants.MAX_NUMBER_OF_KEYWORDS) {
                break;
            }
            if(!"".equals(str)) {
                set.add(str.toLowerCase());
            }
        }
        return set;
    }
}
