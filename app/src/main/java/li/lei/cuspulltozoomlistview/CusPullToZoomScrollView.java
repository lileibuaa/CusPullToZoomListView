package li.lei.cuspulltozoomlistview;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ScrollView;

/**
 * Created by lei on 1/19/16.
 * you can you up...
 */
public class CusPullToZoomScrollView extends ScrollView {
    private static final String LOG_TAG = "Cus_Scroll_view";
    private ImageView mHeaderView;
    private float mLastYPosition;
    private int mDefaultHeight;
    private long mAnimDuration;
    private long mStartTimeStamp;
    private int mStartOffset;
    private int mLastPointId;

    public CusPullToZoomScrollView(Context context) {
        super(context);
        init();
    }

    private void init() {
        mAnimDuration = DateUtils.SECOND_IN_MILLIS;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHeaderView = new ImageView(getContext());
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.splash, options);
        mDefaultHeight = (int) (options.outHeight * 1f / options.outWidth * metrics.widthPixels);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(metrics.widthPixels, mDefaultHeight);
        mHeaderView.setLayoutParams(params);
        mHeaderView.setImageResource(R.drawable.splash);
        mHeaderView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ((ViewGroup) getChildAt(0)).addView(mHeaderView, 0);
    }

    public CusPullToZoomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CusPullToZoomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        int eventIndex = ev.getActionIndex();
        String pointYPath = null;
        for (int i = 0; i < ev.getPointerCount(); i++)
            pointYPath += ev.getY(i) + "\t";
        Log.d(LOG_TAG, "on action pointer id\t" + eventIndex + "\t" + ev.getPointerId(eventIndex) + "\t" + ev.getPointerCount() + "\t" + pointYPath);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(LOG_TAG, "on action down");
                mLastPointId = ev.getPointerId(eventIndex);
                removeCallbacks(restoreRunnable);
                mLastYPosition = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(LOG_TAG, "on action move");
                int activeIndex = ev.findPointerIndex(mLastPointId);
                if (getScrollY() == 0 && (ev.getY(activeIndex) - mLastYPosition > 0 || mHeaderView.getLayoutParams().height != mDefaultHeight)) {
                    float offset = ev.getY(activeIndex) - mLastYPosition;
                    mLastYPosition = ev.getY(activeIndex);
                    scaleHeaderImageWithOffset(offset);
                    return true;
                } else mLastYPosition = ev.getY(activeIndex);
                break;
            case MotionEvent.ACTION_UP:
                Log.d(LOG_TAG, "on action up");
            case MotionEvent.ACTION_CANCEL:
                restoreHeaderImage();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mLastPointId = ev.getPointerId(eventIndex);
                mLastYPosition = ev.getY(eventIndex);
                Log.d(LOG_TAG, "on action pointer down");
                break;
            case MotionEvent.ACTION_POINTER_UP:
                handleSecondaryPointerUp(ev);
                Log.d(LOG_TAG, "on action pointer up");
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void handleSecondaryPointerUp(MotionEvent ev) {
        int pointId = ev.getPointerId(ev.getActionIndex());
        if (pointId == mLastPointId) {
            mLastPointId = ev.getPointerId(ev.getActionIndex() == 0 ? 1 : 0);
            mLastYPosition = ev.getY(ev.findPointerIndex(mLastPointId));
        }
    }

    private void restoreHeaderImage() {
        mStartTimeStamp = System.currentTimeMillis();
        mStartOffset = mHeaderView.getLayoutParams().height - mDefaultHeight;
        post(restoreRunnable);
    }

    private Runnable restoreRunnable = new Runnable() {
        @Override
        public void run() {
            long timeOffset = System.currentTimeMillis() - mStartTimeStamp;
            if (timeOffset > mAnimDuration) {
                scaleHeaderImage(0);
                return;
            }
            float interpolatorValue = interpolator.getInterpolation(timeOffset * 1.0f / mAnimDuration);
            Log.d(LOG_TAG, interpolatorValue + "\t" + timeOffset);
            scaleHeaderImage(mStartOffset * (1 - interpolatorValue));
            post(this);
        }
    };

    private void scaleHeaderImage(float range) {
        ViewGroup.LayoutParams params = mHeaderView.getLayoutParams();
        params.height = (int) (mDefaultHeight + range);
        mHeaderView.setLayoutParams(params);
    }

    private DecelerateInterpolator interpolator = new DecelerateInterpolator(3);

    private void scaleHeaderImageWithOffset(float offset) {
        ViewGroup.LayoutParams params = mHeaderView.getLayoutParams();
        Log.d(LOG_TAG, mDefaultHeight + "\t" + params.height + "\t" + offset);
        params.height = (int) Math.max(1, params.height + offset);
        mHeaderView.setLayoutParams(params);
    }
}
