package tourguide.tourguide;

/**
 * Created by aaronliew on 9/8/15.
 */
public class CleanUpEvent {
    private int ViewId;

    public CleanUpEvent(Integer ViewId){
        this.ViewId=ViewId;
    }

    public int getViewId() {
        return ViewId;
    }
}
