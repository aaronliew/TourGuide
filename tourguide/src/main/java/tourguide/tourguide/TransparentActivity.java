package tourguide.tourguide;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.HashMap;
import java.util.Map;

/**f
* Created by aaronliew on 9/2/15.
*/
public class TransparentActivity extends Activity {
    RelativeLayout relativeLayout;

    TourGuideDataHolder tourGuideDataHolder = TourGuideDataHolder.getInstance();
    public static String TAG= TransparentActivity.class.getSimpleName();

    static String KEY_CLEAN_UP_EVENT = "CleanUpEvent";
    static String KEY_CLOSE_ACTIVITY_EVENT = "CloseActivityEvent";
    static String KEY_NEXT_TOURGUIDE_EVENT = "NextTourGuideEvent";


    public HashMap<Integer, TourGuide> integerTourGuideHashMap;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("CleanUpEvent")) {
                int HighlightViewID = intent.getIntExtra("HighlightViewID", 0);
                cleanUp(HighlightViewID);
            }

            if (action.equals("CloseActivityEvent")){
                Log.d("activity", "close");
                finish();
            }

            if (action.equals("NextTourGuideEvent")&& TourGuideDataHolder.getInstance().isLayoutDrawn()){
                Log.d("IsActive", "next tourguide");
                Integer HighlightViewID = intent.getIntExtra("HighlightViewID", 0);

                setupView(tourGuideDataHolder.getViewHighlightViewMap().get(HighlightViewID),
                        tourGuideDataHolder.getViewTourGuideMap().get(HighlightViewID));

                tourGuideDataHolder.clearViewTourGuideMap();
                tourGuideDataHolder.clearViewHighlightViewMap();
            }


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("Transparent","On Create");


        integerTourGuideHashMap= new HashMap<>();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        // ...but notify us that it happened.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        setContentView(R.layout.test);

        relativeLayout = (RelativeLayout)findViewById(R.id.test);

        final ViewTreeObserver viewTreeObserver = relativeLayout.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // make sure this only run once
                relativeLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                for (Map.Entry<Integer,HighlightViewParams> entry : tourGuideDataHolder.getViewHighlightViewMap().entrySet()){
                    HighlightViewParams highlightViewParams = entry.getValue();
                    setupView(highlightViewParams, tourGuideDataHolder.getViewTourGuideMap().get(entry.getKey()));
                }

                tourGuideDataHolder.setLayoutDrawn(true);
                tourGuideDataHolder.clearViewTourGuideMap();
                tourGuideDataHolder.clearViewHighlightViewMap();

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Pause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        tourGuideDataHolder.setTransparentScreenShown(false);
        tourGuideDataHolder.setLayoutDrawn(false);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        Log.d(TAG, "Stop");
    }

    public void setupView(final HighlightViewParams highlightViewParams, TourGuide tourGuide){
        Log.d("Tempo","Temporary setup");
        DisplayMetrics dm = new DisplayMetrics();
        TransparentActivity.this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int topOffset = dm.heightPixels - relativeLayout.getMeasuredHeight();
        ImageView mHighlightedView = new ImageView(TransparentActivity.this);
        mHighlightedView.setLayoutParams(new ViewGroup.LayoutParams(highlightViewParams.getWidth(), highlightViewParams.getHeight()));
        Log.d("Height XY","Y Transparent Locations on Screen: "+String.valueOf(highlightViewParams.getLocationOnScreen()[1]));
        Log.d("Height XY","Offset on Screen: "+String.valueOf(topOffset));
        mHighlightedView.setX(highlightViewParams.getLocationOnScreen()[0]);
        mHighlightedView.setY(highlightViewParams.getLocationOnScreen()[1]-topOffset);
        Log.d("Height XY","Offset on Screen In dp: "+String.valueOf(convertPixelsToDp(topOffset, TransparentActivity.this)));

        relativeLayout.addView(mHighlightedView);

        Log.d("Transparent", "View id:  " + highlightViewParams.getViewId());
        integerTourGuideHashMap.put(highlightViewParams.getViewId(), TourGuide.init(TransparentActivity.this).with(TourGuide.Technique.Click)
                .setPointer(tourGuide.mPointer)
                .setToolTip(tourGuide.mToolTip)
                .setOverlay(tourGuide.mOverlay)
                .playOn(mHighlightedView));
    }

    public void cleanUp(Integer viewID) {
        Log.d("Transparent", "Received View id: "+viewID);
        integerTourGuideHashMap.get(viewID).cleanUp();
    }

    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("my-event");
        //list of events
        filter.addAction(KEY_CLEAN_UP_EVENT);
        filter.addAction(KEY_CLOSE_ACTIVITY_EVENT);
        filter.addAction(KEY_NEXT_TOURGUIDE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

}
/**
* Problem : If there is two tourguides or more, need to launch two activities
* Solution : Gather every single info of the tourGuides, then launch it at once
*      Problems arise: Need to use different method to do it. Need to communication with the transparent activity. Therefore, need to get the current context. Need application to do it
*
**/
