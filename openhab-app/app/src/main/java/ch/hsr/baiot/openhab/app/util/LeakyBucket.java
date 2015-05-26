package ch.hsr.baiot.openhab.app.util;

import java.util.concurrent.TimeUnit;

/**
 * Created by dominik on 22.05.15.
 */
public class LeakyBucket {

    private int mCounter = 0;
    private int mMax = 1;
    private TimeUnit mInTime;
    private OnBucketFullListener mListener;

    public LeakyBucket(OnBucketFullListener listener, int max, TimeUnit inTime) {
        mListener = listener;
        mMax = max;
        mInTime = inTime;
    }


    public void fill() {
        mCounter++;
        if(mCounter >= mMax) {
            mListener.onFull();
        }
    }

    public static interface OnBucketFullListener {
        public void onFull();
    }

}
