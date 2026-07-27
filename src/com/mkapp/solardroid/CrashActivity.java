package com.mkapp.solardroid;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class CrashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String errorText = getIntent().getStringExtra("error_text");
        if (errorText == null) {
            errorText = "Erro desconhecido (nenhum stack trace capturado)";
        }
        final String finalErrorText = errorText;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 48, 24, 24);
        root.setBackgroundColor(0xFF1A1A1A);

        TextView title = new TextView(this);
        title.setText("SolarDroid crashou");
        title.setTextColor(0xFFFF5555);
        title.setTextSize(20);
        title.setPadding(0, 0, 0, 24);
        root.addView(title);

        Button copyButton = new Button(this);
        copyButton.setText("Copiar erro");
        copyButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("SolarDroid Error", finalErrorText);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(CrashActivity.this, "Erro copiado!", Toast.LENGTH_SHORT).show();
        });
        root.addView(copyButton);

        Button closeButton = new Button(this);
        closeButton.setText("Fechar");
        closeButton.setOnClickListener(v -> finish());
        root.addView(closeButton);

        ScrollView scrollView = new ScrollView(this);
        TextView errorView = new TextView(this);
        errorView.setText(finalErrorText);
        errorView.setTextColor(0xFFCCCCCC);
        errorView.setTextIsSelectable(true);
        errorView.setPadding(0, 24, 0, 0);
        scrollView.addView(errorView);
        root.addView(scrollView);

        setContentView(root);
    }
}
