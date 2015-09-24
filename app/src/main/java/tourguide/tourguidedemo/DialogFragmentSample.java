package tourguide.tourguidedemo;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;

/**
* Created by aaronliew on 8/27/15.
*/
public class DialogFragmentSample extends DialogFragment {
    public TourGuide mTutorialHandler,mTutorialHandler1,mTutorialHandler2;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_basic, container);
        getDialog().setTitle("Bello!");

        final Button button = (Button) view.findViewById(R.id.button);
        final ImageButton closebutton = (ImageButton) view.findViewById(R.id.cross);
        final ImageButton gmailbutton = (ImageButton) view.findViewById(R.id.gmail);
        FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.tourguide);
//
        final ToolTip toolTip = new ToolTip().
                setTitle("Welcome!").
                setDescription("Click on Get Started to begin...");

        final ToolTip toolTip1 = new ToolTip().
                setTitle("Test!").
                setDescription("Click on Get Started to begin...");

        final ToolTip toolTip2 = new ToolTip().
                setTitle("Test Master!").
                setDescription("Click on Get Started to begin...");


        // Setup pointer for demo
        final Pointer pointer = new Pointer();


        // the return handler is used to manipulate the cleanup of all the tutorial elements
        mTutorialHandler = TourGuide.init(getActivity()).with(TourGuide.Technique.Click)
                .setPointer(pointer)
                .setToolTip(toolTip1)
                .setOverlay(new Overlay().setBackgroundColor(Color.parseColor("#66FF0000")))
                .playOn(closebutton);


//        mTutorialHandler1 = TourGuide.init(getActivity()).with(TourGuide.Technique.Click)
//                .setPointer(pointer)
//                .setToolTip(toolTip)
//                .setOverlay(null)
//                .playOn(closebutton);
//
//        mTutorialHandler2 = TourGuide.init(getActivity()).with(TourGuide.Technique.Click)
//                .setPointer(pointer)
//                .setToolTip(toolTip2)
//                .setOverlay(new Overlay().setBackgroundColor(Color.parseColor("#66FF0000")));
//                .playOnWindow(gmailbutton);


        closebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTutorialHandler.cleanUp();
                mTutorialHandler.setToolTip(toolTip).playOn(gmailbutton);
            }
        });

        gmailbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTutorialHandler.cleanUp();
                mTutorialHandler.setToolTip(toolTip2).playOn(button);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTutorialHandler.cleanUp();
                dismissAllowingStateLoss();
            }
        });
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        window.setLayout(1000, 1000);
    }


}
