package org.tmacoin.post.android;

import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class TmaAndroidUtil {

    public static void enableScroll(EditText editText) {

        editText.setVerticalScrollBarEnabled(true);
        editText.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        editText.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        editText.setMovementMethod(ScrollingMovementMethod.getInstance());

        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                view.getParent().requestDisallowInterceptTouchEvent(true);
                if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0 && (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0)
                {
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            }
        });

    }
}
