package org.tmacoin.post.android;

import android.text.method.PasswordTransformationMethod;
import android.view.View;

public class MyPasswordTransformationMethod extends PasswordTransformationMethod {

    private char DOT = '\u2022';

    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        return new PasswordCharSequence(source);
    }

    private class PasswordCharSequence implements CharSequence {

        private CharSequence mSource;

        public PasswordCharSequence(CharSequence source) {
            mSource = source;
        }

        public char charAt(int index) {
            return DOT;
        }

        public int length() {
            return mSource.length();
        }

        public CharSequence subSequence(int start, int end) {
            return new PasswordCharSequence(mSource.subSequence(start, end));
        }
    }
};
