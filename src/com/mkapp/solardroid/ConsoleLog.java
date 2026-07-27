package com.mkapp.solardroid;

import java.util.ArrayList;
import java.util.List;

public class ConsoleLog {
    private static final List<String> logs = new ArrayList<String>();
    private static final int MAX_LINES = 500;

    public static synchronized void add(String line) {
        logs.add(line);
        if (logs.size() > MAX_LINES) {
            logs.remove(0);
        }
    }

    public static synchronized String getAll() {
        StringBuilder sb = new StringBuilder();
        for (String line : logs) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    public static synchronized void clear() {
        logs.clear();
    }
}
