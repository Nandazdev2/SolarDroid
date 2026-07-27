package com.mkapp.solardroid;

import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaError;
import com.naef.jnlua.JavaFunction;

public class SolarDroidErrorHandler implements JavaFunction {

    @Override
    public int invoke(LuaState luaState) {
        String errorMessage = null;
        String luaStackDump = null;

        if (luaState.isString(1)) {
            errorMessage = luaState.toString(1);
        } else if (luaState.isJavaObjectRaw(1)) {
            Object value = luaState.toJavaObjectRaw(1);
            if (value instanceof LuaError) {
                LuaError luaError = (LuaError) value;
                errorMessage = luaError.toString();
            }
        }

        if (errorMessage == null || errorMessage.length() <= 0) {
            errorMessage = "Lua runtime error occurred.";
        }

        try {
            int index = luaState.getTop();
            luaState.getField(LuaState.GLOBALSINDEX, "debug");
            if (luaState.isTable(-1)) {
                luaState.getField(-1, "traceback");
                if (luaState.isFunction(-1)) {
                    luaState.call(0, 1);
                    if (luaState.isString(-1)) {
                        luaStackDump = luaState.toString(-1);
                    }
                }
            }
            luaState.setTop(index);
        } catch (Exception e) {
            // ignore, sem stack trace se falhar
        }

        StringBuilder builder = new StringBuilder();
        builder.append("[ERRO] ").append(errorMessage);
        if (luaStackDump != null && luaStackDump.length() > 0 && !luaStackDump.equals("stack traceback:")) {
            builder.append("\n").append(luaStackDump);
        }

        ConsoleLog.add(builder.toString());
        android.util.Log.i("SolarDroid", builder.toString());

        luaState.pushString(errorMessage);
        return 1;
    }
}
