package com.mkapp.solardroid;

import com.naef.jnlua.LuaState;
import com.naef.jnlua.JavaFunction;

public class SolarDroidPrintFunction implements JavaFunction {

    @Override
    public int invoke(LuaState luaState) {
        int nArgs = luaState.getTop();
        StringBuilder sb = new StringBuilder();

        for (int i = 1; i <= nArgs; i++) {
            if (i > 1) {
                sb.append("\t");
            }
            if (luaState.isString(i) || luaState.isNumber(i)) {
                sb.append(luaState.toString(i));
            } else if (luaState.isBoolean(i)) {
                sb.append(luaState.toBoolean(i));
            } else if (luaState.isNil(i)) {
                sb.append("nil");
            } else {
                sb.append(luaState.type(i).toString());
            }
        }

        String line = sb.toString();
        ConsoleLog.add(line);
        android.util.Log.v("Corona", line);

        return 0;
    }
}
