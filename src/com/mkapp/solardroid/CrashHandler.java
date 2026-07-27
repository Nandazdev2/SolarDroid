package com.mkapp.solardroid;

import android.app.Activity;
import android.content.Intent;
import java.io.PrintWriter;
import java.io.StringWriter;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private final Activity myActivity;
    private final Thread.UncaughtExceptionHandler myDefaultHandler;

    public CrashHandler(Activity activity) {
        myActivity = activity;
        myDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        final String stackTrace = sw.toString();

        try {
            Intent intent = new Intent(myActivity, CrashActivity.class);
            intent.putExtra("error_text", stackTrace);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            myActivity.startActivity(intent);
        } catch (Exception e) {
            // Se nem isso funcionar, deixa o handler padrão agir
        }

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}
