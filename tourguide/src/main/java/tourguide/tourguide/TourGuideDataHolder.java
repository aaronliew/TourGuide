package tourguide.tourguide;

import android.app.Activity;

import java.util.HashMap;

/**
 * Created by aaronliew on 9/18/15.
 */
public class TourGuideDataHolder {


    private HashMap<Integer,HighlightViewParams> ViewHighlightViewMap = new HashMap<>();

    private HashMap<Integer,TourGuide> ViewTourGuideMap = new HashMap<>();

    private boolean isLayoutDrawn;

    private boolean isTransparentScreenShown = false;

    private Activity mActivity;


    public HashMap<Integer,TourGuide> getViewTourGuideMap() {return ViewTourGuideMap;}
    public void addData(int ViewID, TourGuide tourGuide) {this.ViewTourGuideMap.put(ViewID, tourGuide);}


    public HashMap<Integer, HighlightViewParams> getViewHighlightViewMap() {
        return ViewHighlightViewMap;
    }

    public void clearViewHighlightViewMap() {
        ViewHighlightViewMap = new HashMap<>();
    }

    public void clearViewTourGuideMap(){
        ViewTourGuideMap = new HashMap<>();
    }


    public void addDataToViewHighlightViewMap(int ViewID, HighlightViewParams highlightViewParams) {
        ViewHighlightViewMap.put(ViewID, highlightViewParams);
    }

    private static final TourGuideDataHolder holder = new TourGuideDataHolder();
    public static TourGuideDataHolder getInstance() {return holder;}

    public boolean isLayoutDrawn() {
        return isLayoutDrawn;
    }

    public void setLayoutDrawn(boolean isLayoutDrawn) {
        this.isLayoutDrawn = isLayoutDrawn;
    }

    public void setTransparentScreenShown(boolean isTransparentScreenShown) {
        this.isTransparentScreenShown = isTransparentScreenShown;
    }

    public boolean isTransparentScreenShown() {
        return isTransparentScreenShown;
    }

    public void setmActivity(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public Activity getmActivity() {
        return mActivity;
    }
}
