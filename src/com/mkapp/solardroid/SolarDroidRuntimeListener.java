package com.mkapp.solardroid;

import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeListener;
import com.naef.jnlua.LuaState;

public class SolarDroidRuntimeListener implements CoronaRuntimeListener {

    @Override
    public void onLoaded(CoronaRuntime runtime) {
        try {
            LuaState L = runtime.getLuaState();
            L.pushJavaFunction(new SolarDroidPrintFunction());
            L.setField(LuaState.GLOBALSINDEX, "printf");
        } catch (Exception e) {
            ConsoleLog.add("[DEBUG] ERRO ao registrar printf: " + e.getMessage());
        }

        try {
            LuaState L = runtime.getLuaState();
            java.io.ByteArrayInputStream is = new java.io.ByteArrayInputStream(
                PreviewBootstrap.LUA_CODE.getBytes("UTF-8")
            );
            L.load(is, "previewBootstrap");
            L.call(0, 0);
        } catch (Exception e) {
            ConsoleLog.add("[DEBUG] ERRO ao injetar bootstrap: " + e.getMessage());
        }
    }

    @Override
    public void onStarted(CoronaRuntime runtime) {
    }

    @Override
    public void onSuspended(CoronaRuntime runtime) {
    }

    @Override
    public void onResumed(CoronaRuntime runtime) {
    }

    @Override
    public void onExiting(CoronaRuntime runtime) {
    }
}
