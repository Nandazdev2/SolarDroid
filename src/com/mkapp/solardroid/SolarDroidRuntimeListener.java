package com.mkapp.solardroid;

import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeListener;
import com.naef.jnlua.LuaState;

public class SolarDroidRuntimeListener implements CoronaRuntimeListener {

    @Override
    public void onLoaded(CoronaRuntime runtime) {
        ConsoleLog.add("[DEBUG] onLoaded foi chamado");
        try {
            LuaState L = runtime.getLuaState();
            L.pushJavaFunction(new SolarDroidPrintFunction());
            L.setField(LuaState.GLOBALSINDEX, "printf");
            ConsoleLog.add("[DEBUG] printf registrado");
        } catch (Exception e) {
            ConsoleLog.add("[DEBUG] ERRO: " + e.getMessage());
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
