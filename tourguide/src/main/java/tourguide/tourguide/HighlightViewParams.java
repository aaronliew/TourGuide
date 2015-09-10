package tourguide.tourguide;

import java.io.Serializable;

/**
 * Created by aaronliew on 9/3/15.
 */
public class HighlightViewParams implements Serializable {

    private int Height;
    private int Width;
    private int[] LocationOnScreen;
    private int ViewId;

    public int getHeight() {
        return Height;
    }

    public int getWidth() {
        return Width;
    }

    public int[] getLocationOnScreen() {
        return LocationOnScreen;
    }

    public void setHeight(int height) {
        Height = height;
    }

    public void setWidth(int width) {
        Width = width;
    }

    public void setLocationOnScreen(int[] locationOnScreen) {
        LocationOnScreen = locationOnScreen;
    }

    public void setViewId(int viewId) {
        ViewId = viewId;
    }

    public int getViewId() {
        return ViewId;
    }
}
