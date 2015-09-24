package tourguide.tourguide;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;


import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

import java.util.List;


/**
 * Created by tanjunrong on 2/10/15.
 */
public class TourGuide {

    /**
     * This describes the animation techniques
     * */
    public enum Technique {
        Click, HorizontalLeft, HorizontalRight, VerticalUpward, VerticalDownward
    }

    /**
     * This describes the allowable motion, for example if you want the users to learn about clicking, but want to stop them from swiping, then use ClickOnly
     */
    public enum MotionType {
        AllowAll, ClickOnly, SwipeOnly
    }
    private Technique mTechnique;
    private View mHighlightedView;
    private Activity mActivity;
    private MotionType mMotionType;
    private FrameLayoutWithHole mFrameLayout;
    private View mToolTipViewGroup;
    public ToolTip mToolTip;
    public Pointer mPointer;
    public Overlay mOverlay;

    private Sequence mSequence;

    private WindowManager.LayoutParams params;

    WindowManager wm;

    private static TourGuideDataHolder tourGuideDataHolder = TourGuideDataHolder.getInstance();

    /*************
     *
     * Public API
     *
     *************/

    public void initWindowManager(){
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                0,
//              WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                      | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.setTitle("Load Average");
        wm = (WindowManager) mActivity.getSystemService(Activity.WINDOW_SERVICE);
    }

    /* Static builder */
    public static TourGuide init(Activity activity){
        if (activity instanceof TransparentActivity){
            tourGuideDataHolder.setmActivity(activity);
        }

        return new TourGuide(activity);
    }

    /* Constructor */
    public TourGuide(Activity activity){
        mActivity = activity;

    }

    /**
     * Setter for the animation to be used
     * @param technique Animation to be used
     * @return return TourGuide instance for chaining purpose
     */
    public TourGuide with(Technique technique) {
        mTechnique = technique;
        return this;
    }

    /**
     * Sets which motion type is motionType
     * @param motionType
     * @return return TourGuide instance for chaining purpose
     */
    public TourGuide motionType(MotionType motionType){
        mMotionType = motionType;
        return this;
    }

    /**
     * Sets the duration
     * @param view the view in which the tutorial button will be placed on top of
     * @return return TourGuide instance for chaining purpose
     */
    public TourGuide playOn(View view){
        mHighlightedView = view;
        initWindowManager();
        setupView();
        return this;
    }

//    public TourGuide playOn(View view){
//
////        if (getVisibleFragment()!=null){
////            Log.d("Checking fragment", "fragment!");
////        }
//        mHighlightedView = view;
//        setupTransparentActivity();
//        return this;
//    }

    private void setupTransparentActivity(){

        mHighlightedView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {

            }

            @Override
            public void onViewDetachedFromWindow(View view) {
                closetheTourGuide();
            }
        });
        if (!tourGuideDataHolder.isTransparentScreenShown() && !(mActivity instanceof TransparentActivity)) {
            tourGuideDataHolder.setTransparentScreenShown(true);
            final ViewTreeObserver viewTreeObserver = mHighlightedView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // make sure this only run once
                    mHighlightedView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    TourGuideDataHolder.getInstance().addData(mHighlightedView.getId(), TourGuide.this);
                    TourGuideDataHolder.getInstance().addDataToViewHighlightViewMap(mHighlightedView.getId(), getHighlightViewInfo());

                    Intent i = new Intent(mActivity, TransparentActivity.class);
                    mActivity.startActivity(i);
                }
            });
        }
        else if (tourGuideDataHolder.isTransparentScreenShown() && !(mActivity instanceof TransparentActivity) ) {
            if (TourGuideDataHolder.getInstance().isLayoutDrawn()){
                nextTourGuideEvent(mHighlightedView.getId());
            }
            else {
                final ViewTreeObserver viewTreeObserver = mHighlightedView.getViewTreeObserver();
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        mHighlightedView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        TourGuideDataHolder.getInstance().addData(mHighlightedView.getId(), TourGuide.this);
                        TourGuideDataHolder.getInstance().addDataToViewHighlightViewMap(mHighlightedView.getId(), getHighlightViewInfo());
                    }
                });
            }
        }
        else {
            setupView();
        }
    }

    private HighlightViewParams getHighlightViewInfo(){
        int [] pos = new int[2];
        mHighlightedView.getLocationOnScreen(pos);
        Log.d("Height XY","X Locations on Screen: "+String.valueOf(pos[0]));
        HighlightViewParams highlightViewParams = new HighlightViewParams();
        highlightViewParams.setHeight(mHighlightedView.getHeight());
        highlightViewParams.setWidth(mHighlightedView.getWidth());
        highlightViewParams.setLocationOnScreen(pos);
        Log.d("Transparent", "Id: "+mHighlightedView.getId());
        highlightViewParams.setViewId(mHighlightedView.getId());

        return highlightViewParams;
    }

    /**
     * Sets the overlay
     * @param overlay this overlay object should contain the attributes of the overlay, such as background color, animation, Style, etc
     * @return return TourGuide instance for chaining purpose
     */
    public TourGuide setOverlay(Overlay overlay){
        mOverlay = overlay;
        return this;
    }
    /**
     * Set the toolTip
     * @param toolTip this toolTip object should contain the attributes of the ToolTip, such as, the title text, and the description text, background color, etc
     * @return return TourGuide instance for chaining purpose
     */
    public TourGuide setToolTip(ToolTip toolTip){
        mToolTip = toolTip;
        return this;
    }
    /**
     * Set the Pointer
     * @param pointer this pointer object should contain the attributes of the Pointer, such as the pointer color, pointer gravity, etc, refer to @Link{pointer}
     * @return return TourGuide instance for chaining purpose
     */
    public TourGuide setPointer(Pointer pointer){
        mPointer = pointer;
        return this;
    }
    /**
     * Clean up the tutorial that is added to the activity
     */
     public void cleanUp(){
         Log.d("Transparent", "Cleanup: "+ tourGuideDataHolder.isTransparentScreenShown());
         if (tourGuideDataHolder.isTransparentScreenShown() && !(mActivity instanceof TransparentActivity) && mHighlightedView!=null){
             Log.d("Transparent", "Sent View id: "+ mHighlightedView.getId());
             if (mHighlightedView.getId()!=-1) {
                 cleanUpEvent(mHighlightedView.getId());
             }
         }
         else {
             Log.d("Transparent", "Cleanup");
             if (mFrameLayout!=null) {
                 mFrameLayout.cleanUp();
             }
             if (mToolTipViewGroup != null) {
                 ((ViewGroup) mActivity.getWindow().getDecorView()).removeView(mToolTipViewGroup);
             }
         }
    }


    public TourGuide playLater(View view){
        mHighlightedView = view;
        return this;
    }

    /**************************
     * Sequence related method
     **************************/

    public TourGuide playInSequence(Sequence sequence){
        setSequence(sequence);
        next();
        return this;
    }

    public TourGuide setSequence(Sequence sequence){
        mSequence = sequence;
        mSequence.setParentTourGuide(this);
        for (TourGuide tourGuide : sequence.mTourGuideArray){
            if (tourGuide.mHighlightedView == null) {
                throw new NullPointerException("Please specify the view using 'playLater' method");
            }
        }
        return this;
    }

    public TourGuide next(){
        if (mFrameLayout!=null) {
            cleanUp();
        }

        if (mSequence.mCurrentSequence < mSequence.mTourGuideArray.length) {
            setToolTip(mSequence.getToolTip());
            setPointer(mSequence.getPointer());
            setOverlay(mSequence.getOverlay());

            mHighlightedView = mSequence.getNextTourGuide().mHighlightedView;

            setupView();
            mSequence.mCurrentSequence++;
        }
        return this;
    }

    /**
     *
     * @return FrameLayoutWithHole that is used as overlay
     */
    public FrameLayoutWithHole getOverlay(){
        return mFrameLayout;
    }
    /**
     *
     * @return the ToolTip container View
     */
    public View getToolTip(){
        return mToolTipViewGroup;
    }

    /******
     *
     * Private methods
     *
     *******/
    //TODO: move into Pointer
    private int getXBasedOnGravity(int width){
        int [] pos = new int[2];
        mHighlightedView.getLocationOnScreen(pos);
        int x = pos[0];
        if((mPointer.mGravity & Gravity.RIGHT) == Gravity.RIGHT){
            return x+mHighlightedView.getWidth()-width;
        } else if ((mPointer.mGravity & Gravity.LEFT) == Gravity.LEFT) {
            return x;
        } else { // this is center
            return x+mHighlightedView.getWidth()/2-width/2;
        }
    }
    //TODO: move into Pointer
    private int getYBasedOnGravity(int height){
        int [] pos = new int[2];
        mHighlightedView.getLocationOnScreen(pos);
        int y = pos[1];
        if((mPointer.mGravity & Gravity.BOTTOM) == Gravity.BOTTOM){
            return y+mHighlightedView.getHeight()-height;
        } else if ((mPointer.mGravity & Gravity.TOP) == Gravity.TOP) {
            return y;
        }else { // this is center
            return y+mHighlightedView.getHeight()/2-height/2;
        }
    }

    private void setupView(){
//        TODO: throw exception if either mActivity, mDuration, mHighlightedView is null
        checking();
        final ViewTreeObserver viewTreeObserver = mHighlightedView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // make sure this only run once
                mHighlightedView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                /* Initialize a frame layout with a hole */
                mFrameLayout = new FrameLayoutWithHole(mActivity, mHighlightedView, mMotionType, mOverlay);
                /* handle click disable */
                handleDisableClicking(mFrameLayout);

                /* setup floating action button */
                if (mPointer != null) {
                    FloatingActionButton fab = setupAndAddFABToFrameLayout(mFrameLayout);
                    performAnimationOn(fab);
                }
                setupFrameLayout();
                /* setup tooltip view */
                setupToolTip();
            }
        });

    }
    private void checking(){
        // There is not check for tooltip because tooltip can be null, it means there no tooltip will be shown

    }
    private void handleDisableClicking(FrameLayoutWithHole frameLayoutWithHole){
        // 1. if user provides an overlay listener, use that as 1st priority
        if (mOverlay != null && mOverlay.mOnClickListener!=null) {
            frameLayoutWithHole.setClickable(true);
            frameLayoutWithHole.setOnClickListener(mOverlay.mOnClickListener);
        }
        // 2. if overlay listener is not provided, check if it's disabled
        else if (mOverlay != null && mOverlay.mDisableClick) {
            Log.w("tourguide", "Overlay's default OnClickListener is null, it will proceed to next tourguide when it is clicked");
            frameLayoutWithHole.setViewHole(mHighlightedView);
            frameLayoutWithHole.setSoundEffectsEnabled(false);
        }
    }

    private void setupToolTip(){
        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);

        if (mToolTip != null) {
            /* inflate and get views */
            ViewGroup parent = (ViewGroup) mActivity.getWindow().getDecorView();
            LayoutInflater layoutInflater = mActivity.getLayoutInflater();
            mToolTipViewGroup = layoutInflater.inflate(R.layout.tooltip, null);
            View toolTipContainer = mToolTipViewGroup.findViewById(R.id.toolTip_container);
            TextView toolTipTitleTV = (TextView) mToolTipViewGroup.findViewById(R.id.title);
            TextView toolTipDescriptionTV = (TextView) mToolTipViewGroup.findViewById(R.id.description);

            /* set tooltip attributes */
            toolTipContainer.setBackgroundColor(mToolTip.mBackgroundColor);
            toolTipTitleTV.setText(mToolTip.mTitle);
            toolTipDescriptionTV.setText(mToolTip.mDescription);

            mToolTipViewGroup.startAnimation(mToolTip.mEnterAnimation);

            /* add setShadow if it's turned on */
            if (mToolTip.mShadow) {
                mToolTipViewGroup.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.drop_shadow));
            }

            /* position and size calculation */
            int [] pos = new int[2];
            mHighlightedView.getLocationOnScreen(pos);
            int targetViewX = pos[0];
            final int targetViewY = pos[1];

            // get measured size of tooltip
            mToolTipViewGroup.measure(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            int toolTipMeasuredWidth = mToolTipViewGroup.getMeasuredWidth();
            int toolTipMeasuredHeight = mToolTipViewGroup.getMeasuredHeight();

            Point resultPoint = new Point(); // this holds the final position of tooltip
            float density = mActivity.getResources().getDisplayMetrics().density;
            final float adjustment = 10 * density; //adjustment is that little overlapping area of tooltip and targeted button

            // calculate x position, based on gravity, tooltipMeasuredWidth, parent max width, x position of target view, adjustment
            if (toolTipMeasuredWidth > parent.getWidth()){
                resultPoint.x = getXForTooTip(mToolTip.mGravity, parent.getWidth(), targetViewX, adjustment);
            } else {
                resultPoint.x = getXForTooTip(mToolTip.mGravity, toolTipMeasuredWidth, targetViewX, adjustment);
            }

            resultPoint.y = getYForTooTip(mToolTip.mGravity, toolTipMeasuredHeight, targetViewY, adjustment);

            // add view to parent
//            ((ViewGroup) mActivity.getWindow().getDecorView().findViewById(android.R.id.content)).addView(mToolTipViewGroup, layoutParams);
            parent.addView(mToolTipViewGroup, layoutParams);

            // 1. width < screen check
            if (toolTipMeasuredWidth > parent.getWidth()){
                mToolTipViewGroup.getLayoutParams().width = parent.getWidth();
                toolTipMeasuredWidth = parent.getWidth();
            }
            // 2. x left boundary check
            if (resultPoint.x < 0){
                mToolTipViewGroup.getLayoutParams().width = toolTipMeasuredWidth + resultPoint.x; //since point.x is negative, use plus
                resultPoint.x = 0;
            }
            // 3. x right boundary check
            int tempRightX = resultPoint.x + toolTipMeasuredWidth;
            if ( tempRightX > parent.getWidth()){
                mToolTipViewGroup.getLayoutParams().width = parent.getWidth() - resultPoint.x; //since point.x is negative, use plus
            }

            // pass toolTip onClickListener into toolTipViewGroup
            if (mToolTip.mOnClickListener!=null) {
                mToolTipViewGroup.setOnClickListener(mToolTip.mOnClickListener);
            }

            // TODO: no boundary check for height yet, this is a unlikely case though
            // height boundary can be fixed by user changing the gravity to the other size, since there are plenty of space vertically compared to horizontally

            // this needs an viewTreeObserver, that's because TextView measurement of it's vertical height is not accurate (didn't take into account of multiple lines yet) before it's rendered
            // re-calculate height again once it's rendered
            final ViewTreeObserver viewTreeObserver = mToolTipViewGroup.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mToolTipViewGroup.getViewTreeObserver().removeGlobalOnLayoutListener(this);// make sure this only run once

                    int fixedY;
                    int toolTipHeightAfterLayouted = mToolTipViewGroup.getHeight();
                    fixedY = getYForTooTip(mToolTip.mGravity, toolTipHeightAfterLayouted, targetViewY, adjustment);
                    layoutParams.setMargins((int)mToolTipViewGroup.getX(),fixedY,0,0);
                }
            });

            // set the position using setMargins on the left and top
            layoutParams.setMargins(resultPoint.x, resultPoint.y, 0, 0);
        }

    }

    private int getXForTooTip(int gravity, int toolTipMeasuredWidth, int targetViewX, float adjustment){
        int x;
        if ((gravity & Gravity.LEFT) == Gravity.LEFT){
            x = targetViewX - toolTipMeasuredWidth + (int)adjustment;
        } else if ((gravity & Gravity.RIGHT) == Gravity.RIGHT) {
            x = targetViewX + mHighlightedView.getWidth() - (int)adjustment;
        } else {
            x = targetViewX + mHighlightedView.getWidth() / 2 - toolTipMeasuredWidth / 2;
        }
        return x;
    }
    private int getYForTooTip(int gravity, int toolTipMeasuredHeight, int targetViewY, float adjustment){
        int y;
        if ((gravity & Gravity.TOP) == Gravity.TOP) {

            if (((gravity & Gravity.LEFT) == Gravity.LEFT) || ((gravity & Gravity.RIGHT) == Gravity.RIGHT)) {
                y =  targetViewY - toolTipMeasuredHeight + (int)adjustment;
            } else {
                y =  targetViewY - toolTipMeasuredHeight - (int)adjustment;
            }
        } else { // this is center
            if (((gravity & Gravity.LEFT) == Gravity.LEFT) || ((gravity & Gravity.RIGHT) == Gravity.RIGHT)) {
                y =  targetViewY + mHighlightedView.getHeight() - (int) adjustment;
            } else {
                y =  targetViewY + mHighlightedView.getHeight() + (int) adjustment;
            }
        }
        return y;
    }

    private FloatingActionButton setupAndAddFABToFrameLayout(final FrameLayoutWithHole frameLayoutWithHole){
        // invisFab is invisible, and it's only used for getting the width and height
        final FloatingActionButton invisFab = new FloatingActionButton(mActivity);
        invisFab.setSize(FloatingActionButton.SIZE_MINI);
        invisFab.setVisibility(View.INVISIBLE);
        ((ViewGroup)mActivity.getWindow().getDecorView()).addView(invisFab);

        // fab is the real fab that is going to be added
        final FloatingActionButton fab = new FloatingActionButton(mActivity);
        fab.setBackgroundColor(Color.BLUE);
        fab.setSize(FloatingActionButton.SIZE_MINI);
        fab.setColorNormal(mPointer.mColor);
        fab.setStrokeVisible(false);
        fab.setClickable(false);

        // When invisFab is layouted, it's width and height can be used to calculate the correct position of fab
        final ViewTreeObserver viewTreeObserver = invisFab.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // make sure this only run once
                invisFab.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                frameLayoutWithHole.addView(fab, params);

                // measure size of image to be placed
                params.setMargins(getXBasedOnGravity(invisFab.getWidth()), getYBasedOnGravity(invisFab.getHeight()), 0, 0);
            }
        });


        return fab;
    }

    private void setupFrameLayout(){
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        ViewGroup contentArea = (ViewGroup) mActivity.getWindow().getDecorView().findViewById(android.R.id.content);
        int [] pos = new int[2];
        contentArea.getLocationOnScreen(pos);
        // frameLayoutWithHole's coordinates are calculated taking full screen height into account
        // but we're adding it to the content area only, so we need to offset it to the same Y value of contentArea

        layoutParams.setMargins(0,-pos[1],0,0);

        wm.addView(mFrameLayout,params);
//        ((ViewGroup) mActivity.getWindow().getDecorView().findViewById(android.R.id.content)).addView(mFrameLayout, layoutParams);
    }

    private void performAnimationOn(final View view){

        if (mTechnique != null && mTechnique == Technique.HorizontalLeft){

            final AnimatorSet animatorSet = new AnimatorSet();
            final AnimatorSet animatorSet2 = new AnimatorSet();
            Animator.AnimatorListener lis1 = new Animator.AnimatorListener() {
                @Override public void onAnimationStart(Animator animator) {}
                @Override public void onAnimationCancel(Animator animator) {}
                @Override public void onAnimationRepeat(Animator animator) {}
                @Override
                public void onAnimationEnd(Animator animator) {
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                    view.setTranslationX(0);
                    animatorSet2.start();
                }
            };
            Animator.AnimatorListener lis2 = new Animator.AnimatorListener() {
                @Override public void onAnimationStart(Animator animator) {}
                @Override public void onAnimationCancel(Animator animator) {}
                @Override public void onAnimationRepeat(Animator animator) {}
                @Override
                public void onAnimationEnd(Animator animator) {
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                    view.setTranslationX(0);
                    animatorSet.start();
                }
            };

            long fadeInDuration = 800;
            long scaleDownDuration = 800;
            long goLeftXDuration = 2000;
            long fadeOutDuration = goLeftXDuration;
            float translationX = getScreenWidth()/2;

            final ValueAnimator fadeInAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            fadeInAnim.setDuration(fadeInDuration);
            final ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.85f);
            scaleDownX.setDuration(scaleDownDuration);
            final ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.85f);
            scaleDownY.setDuration(scaleDownDuration);
            final ObjectAnimator goLeftX = ObjectAnimator.ofFloat(view, "translationX", -translationX);
            goLeftX.setDuration(goLeftXDuration);
            final ValueAnimator fadeOutAnim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            fadeOutAnim.setDuration(fadeOutDuration);

            final ValueAnimator fadeInAnim2 = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            fadeInAnim2.setDuration(fadeInDuration);
            final ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.85f);
            scaleDownX2.setDuration(scaleDownDuration);
            final ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.85f);
            scaleDownY2.setDuration(scaleDownDuration);
            final ObjectAnimator goLeftX2 = ObjectAnimator.ofFloat(view, "translationX", -translationX);
            goLeftX2.setDuration(goLeftXDuration);
            final ValueAnimator fadeOutAnim2 = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            fadeOutAnim2.setDuration(fadeOutDuration);

            animatorSet.play(fadeInAnim);
            animatorSet.play(scaleDownX).with(scaleDownY).after(fadeInAnim);
            animatorSet.play(goLeftX).with(fadeOutAnim).after(scaleDownY);

            animatorSet2.play(fadeInAnim2);
            animatorSet2.play(scaleDownX2).with(scaleDownY2).after(fadeInAnim2);
            animatorSet2.play(goLeftX2).with(fadeOutAnim2).after(scaleDownY2);

            animatorSet.addListener(lis1);
            animatorSet2.addListener(lis2);
            animatorSet.start();

            /* these animatorSets are kept track in FrameLayout, so that they can be cleaned up when FrameLayout is detached from window */
            mFrameLayout.addAnimatorSet(animatorSet);
            mFrameLayout.addAnimatorSet(animatorSet2);
        } else if (mTechnique != null && mTechnique == Technique.HorizontalRight){

        } else if (mTechnique != null && mTechnique == Technique.VerticalUpward){

        } else if (mTechnique != null && mTechnique == Technique.VerticalDownward){

        } else { // do click for default case
            final AnimatorSet animatorSet = new AnimatorSet();
            final AnimatorSet animatorSet2 = new AnimatorSet();
            Animator.AnimatorListener lis1 = new Animator.AnimatorListener() {
                @Override public void onAnimationStart(Animator animator) {}
                @Override public void onAnimationCancel(Animator animator) {}
                @Override public void onAnimationRepeat(Animator animator) {}
                @Override
                public void onAnimationEnd(Animator animator) {
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                    view.setTranslationX(0);
                    animatorSet2.start();
                }
            };
            Animator.AnimatorListener lis2 = new Animator.AnimatorListener() {
                @Override public void onAnimationStart(Animator animator) {}
                @Override public void onAnimationCancel(Animator animator) {}
                @Override public void onAnimationRepeat(Animator animator) {}
                @Override
                public void onAnimationEnd(Animator animator) {
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                    view.setTranslationX(0);
                    animatorSet.start();
                }
            };

            long fadeInDuration = 800;
            long scaleDownDuration = 800;
            long fadeOutDuration = 800;
            long delay = 1000;

            final ValueAnimator delayAnim = ObjectAnimator.ofFloat(view, "translationX", 0);
            delayAnim.setDuration(delay);
            final ValueAnimator fadeInAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            fadeInAnim.setDuration(fadeInDuration);
            final ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.85f);
            scaleDownX.setDuration(scaleDownDuration);
            final ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.85f);
            scaleDownY.setDuration(scaleDownDuration);
            final ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.85f, 1f);
            scaleUpX.setDuration(scaleDownDuration);
            final ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.85f, 1f);
            scaleUpY.setDuration(scaleDownDuration);
            final ValueAnimator fadeOutAnim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            fadeOutAnim.setDuration(fadeOutDuration);

            final ValueAnimator delayAnim2 = ObjectAnimator.ofFloat(view, "translationX", 0);
            delayAnim2.setDuration(delay);
            final ValueAnimator fadeInAnim2 = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            fadeInAnim2.setDuration(fadeInDuration);
            final ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.85f);
            scaleDownX2.setDuration(scaleDownDuration);
            final ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.85f);
            scaleDownY2.setDuration(scaleDownDuration);
            final ObjectAnimator scaleUpX2 = ObjectAnimator.ofFloat(view, "scaleX", 0.85f, 1f);
            scaleUpX2.setDuration(scaleDownDuration);
            final ObjectAnimator scaleUpY2 = ObjectAnimator.ofFloat(view, "scaleY", 0.85f, 1f);
            scaleUpY2.setDuration(scaleDownDuration);
            final ValueAnimator fadeOutAnim2 = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            fadeOutAnim2.setDuration(fadeOutDuration);
            view.setAlpha(0);
            animatorSet.setStartDelay(mToolTip != null ? mToolTip.mEnterAnimation.getDuration() : 0);
            animatorSet.play(fadeInAnim);
            animatorSet.play(scaleDownX).with(scaleDownY).after(fadeInAnim);
            animatorSet.play(scaleUpX).with(scaleUpY).with(fadeOutAnim).after(scaleDownY);
            animatorSet.play(delayAnim).after(scaleUpY);

            animatorSet2.play(fadeInAnim2);
            animatorSet2.play(scaleDownX2).with(scaleDownY2).after(fadeInAnim2);
            animatorSet2.play(scaleUpX2).with(scaleUpY2).with(fadeOutAnim2).after(scaleDownY2);
            animatorSet2.play(delayAnim2).after(scaleUpY2);

            animatorSet.addListener(lis1);
            animatorSet2.addListener(lis2);
            animatorSet.start();

            /* these animatorSets are kept track in FrameLayout, so that they can be cleaned up when FrameLayout is detached from window */
            mFrameLayout.addAnimatorSet(animatorSet);
            mFrameLayout.addAnimatorSet(animatorSet2);
        }
    }
    private int getScreenWidth(){
        if (mActivity!=null) {
            Display display = mActivity.getWindowManager().getDefaultDisplay();
            /* getSize() is only available on API 13+ */
//            Point size = new Point();
//            display.getSize(size);
            return display.getWidth();
        } else {
            return 0;
        }
    }

    public void closetheTourGuide(){
        tourGuideDataHolder.getmActivity().finish();
//        closeActivityEvent();
    }

    // Send an Intent with an action named "my-event".
    private void cleanUpEvent(Integer ViewID) {
        Log.d("message", "message sent: CleanUpEvent");
        Intent intent = new Intent(TransparentActivity.KEY_CLEAN_UP_EVENT);
        // add data
        intent.putExtra("HighlightViewID", ViewID);
        LocalBroadcastManager.getInstance(mActivity).sendBroadcast(intent);
    }

    // Send an Intent with an action named "my-event".
    private void nextTourGuideEvent(Integer ViewID) {
        Log.d("message", "message sent: NextTourGuideEvent");
        TourGuideDataHolder.getInstance().addData(mHighlightedView.getId(), TourGuide.this);
        TourGuideDataHolder.getInstance().addDataToViewHighlightViewMap(mHighlightedView.getId(), getHighlightViewInfo());
        Intent intent = new Intent(TransparentActivity.KEY_NEXT_TOURGUIDE_EVENT);
        // add data
        intent.putExtra("HighlightViewID", ViewID);
        LocalBroadcastManager.getInstance(mActivity).sendBroadcast(intent);
    }

    private void closeActivityEvent() {
        Log.d("message", "message sent");
        Intent intent = new Intent(TransparentActivity.KEY_CLOSE_ACTIVITY_EVENT);
        // add data
        intent.putExtra("message", "data");
        LocalBroadcastManager.getInstance(mActivity).sendBroadcast(intent);
    }

    public Fragment getVisibleFragment(){
        if (!(mActivity instanceof TransparentActivity)) {
            FragmentManager fragmentManager = ((ActionBarActivity) mActivity).getSupportFragmentManager();
            List<Fragment> fragments = fragmentManager.getFragments();
            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isVisible())
                    return fragment;
            }

        }
        return null;
    }


}
