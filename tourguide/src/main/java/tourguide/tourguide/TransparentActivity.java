package tourguide.tourguide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;

/**f
* Created by aaronliew on 9/2/15.
*/
public class TransparentActivity extends Activity {
    RelativeLayout relativeLayout;
    ToolTip toolTip;
    Pointer pointer;
    public static String TAG= TransparentActivity.class.getSimpleName();

    public HashMap<Integer, TourGuide> integerTourGuideHashMap;

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

        Intent intent=this.getIntent();
        Bundle bundle=intent.getExtras();


        //pass technique
        final HighlightViewParams highlightViewParams= (HighlightViewParams) bundle.getSerializable("HighlightView");

        toolTip = new ToolTip().
                setTitle("Welcome!").
                setDescription("Click on Get Started to begin...");

        // Setup pointer for demo
        pointer = new Pointer();

        Log.d("Height XY",String.valueOf(highlightViewParams.getHeight()));

        final ViewTreeObserver viewTreeObserver = relativeLayout.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // make sure this only run once

                relativeLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                setupView(highlightViewParams);

                if (GetTheEvent()!=null) {
                    for (HighlightViewParams highlightViewParams : GetTheEvent().getHighlightViewParamsList()) {
                        setupView(highlightViewParams);
                    }
                }

                TransparentActivity.this.getSharedPreferences("TourGuide", 0).edit().remove("Sticky Event").commit();
            }
        });
    }

    public void onEvent(ActivityEvent event){
//        finish();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Pause");
//        setTransparentActivity(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        setTransparentActivity(false);
        Log.d(TAG, "Stop");
    }

    public void setupView(final HighlightViewParams highlightViewParams){

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
                .setPointer(pointer)
                .setToolTip(toolTip)
                .setOverlay(null)
                .playOn(mHighlightedView));



    }

    public void onEvent(CleanUpEvent event) {
        Log.d("Transparent", "Received View id: "+event.getViewId());
        integerTourGuideHashMap.get(event.getViewId()).cleanUp();
    }

    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }
    private void setTransparentActivity(boolean isActive){
        SharedPreferences sharedPref = this.getSharedPreferences("TourGuide", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("IsActivityActive", isActive);
        editor.commit();
    }

    private SampleJSON GetTheEvent(){
        SharedPreferences sharedPref = this.getSharedPreferences("TourGuide", Context.MODE_PRIVATE);
        String Json = sharedPref.getString("Sticky Event", "");
        Log.d("Transparent", "Json String: "+Json);
        return new Gson().fromJson(Json, SampleJSON.class);
    }

    private class SampleJSON {

        public List<HighlightViewParams> highlightViewParamsList;

        public void setHighlightViewParamsList(List<HighlightViewParams> highlightViewParamsList) {
            this.highlightViewParamsList = highlightViewParamsList;
        }

        public List<HighlightViewParams> getHighlightViewParamsList() {
            return highlightViewParamsList;
        }
    }
}
/**
* Problem : If there is two tourguides or more, need to launch two activities
* Solution : Gather every single info of the tourGuides, then launch it at once
*      Problems arise: Need to use different method to do it. Need to communication with the transparent activity. Therefore, need to get the current context. Need application to do it
*
**/
