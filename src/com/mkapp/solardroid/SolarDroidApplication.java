package com.mkapp.solardroid;

import android.app.Application;
import com.ansca.corona.CoronaEnvironment;

public class SolarDroidApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ConsoleLog.add("[DEBUG] Application.onCreate - registrando listener");
        CoronaEnvironment.addRuntimeListener(new SolarDroidRuntimeListener());
        CoronaEnvironment.setLuaErrorHandler(new SolarDroidErrorHandler());
    }
}
