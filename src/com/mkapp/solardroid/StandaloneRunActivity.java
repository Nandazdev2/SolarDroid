package com.mkapp.solardroid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.ansca.corona.CoronaView;
import com.ansca.corona.CoronaEnvironment;

public class StandaloneRunActivity extends Activity {

    private CoronaView myCoronaView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
        CoronaEnvironment.addRuntimeListener(new SolarDroidRuntimeListener());
        CoronaEnvironment.setLuaErrorHandler(new SolarDroidErrorHandler());

        myCoronaView = new CoronaView(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        );

        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.addView(myCoronaView, layoutParams);
        setContentView(rootLayout);

        myCoronaView.init("SolarProject/");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myCoronaView != null) {
            myCoronaView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myCoronaView != null) {
            myCoronaView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myCoronaView != null) {
            myCoronaView.destroy();
        }
    }
}
