package tourguide.tourguidedemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.lang.ref.WeakReference;

import tourguide.tourguide.Pointer;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;


public class DialogFragmentActivity extends ActionBarActivity {
    public TourGuide mTutorialHandler;
    public Activity mActivity;
    public static final String COLOR_DEMO = "color_demo";
    public static final String GRAVITY_DEMO = "gravity_demo";

    private static WeakReference<DialogFragmentActivity> wrActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Get parameters from main activity */
        Intent intent = getIntent();
        boolean color_demo = intent.getBooleanExtra(COLOR_DEMO, false);
        boolean gravity_demo = intent.getBooleanExtra(GRAVITY_DEMO, false);

        super.onCreate(savedInstanceState);
        mActivity = this;

        wrActivity = new WeakReference<DialogFragmentActivity>(this);
        setContentView(R.layout.activity_basic);

        Button button = (Button)findViewById(R.id.button);


        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
//                mTutorialHandler.closetheTourGuide();
                showEditDialog();
//                showTransparentFragment();
            }
        });

        ToolTip toolTip = new ToolTip().
                setTitle("Welcome!").
                setDescription("Click on Get Started to begin...");

        // Setup pointer for demo
        Pointer pointer = new Pointer();


//        mTutorialHandler = TourGuide.init(this).with(TourGuide.Technique.Click)
//                .setPointer(pointer)
//                .setToolTip(toolTip)
//                .setOverlay(new Overlay().setBackgroundColor(Color.parseColor("#66FF0000")))
//                .playOn(button);
    }


    private void showEditDialog() {
        final ActionBarActivity activity = wrActivity.get();
        if (activity != null && !activity.isFinishing()) {
            FragmentManager fm = activity.getSupportFragmentManager();
            DialogFragmentSample editNameDialog = new DialogFragmentSample();
            editNameDialog.show(fm, "fragment_edit_name");
        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Dialog", "OnResume");
    }

    @Override
    protected void onPause() {
        Log.d("Dialog", "OnPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("Dialog", "OnStop");
        super.onStop();
    }
}