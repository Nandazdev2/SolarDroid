package com.mkapp.solardroid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TestMinimalActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("SolarDroid abriu com sucesso!");
        tv.setTextSize(20);
        setContentView(tv);
    }
}
