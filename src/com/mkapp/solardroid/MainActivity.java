package com.mkapp.solardroid;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.widget.FrameLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ansca.corona.CoronaView;
import com.ansca.corona.CoronaEnvironment;

public class MainActivity extends Activity {

    private CoronaView myCoronaView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));

        ConsoleLog.add("--- Nova execucao iniciada ---");

        String projectName = getIntent().getStringExtra("projectName");
        if (projectName == null) {
            projectName = "SolarProject";
        }

        File projectDir = ProjectManager.getProjectDir(this, projectName);

        applySolarDroidConfig(projectDir);

        File documentsDir = CoronaEnvironment.getDocumentsDirectory(this);
        try {
            copyImageAssetsToRoot("SolarProject", documentsDir);
        } catch (Exception e) {
            ConsoleLog.add("[DEBUG] Erro copiando imagens pra raiz: " + e.getMessage());
        }

        String projectPath = projectDir.getAbsolutePath();
        if (!projectPath.endsWith("/")) {
            projectPath = projectPath + "/";
        }

        myCoronaView = new CoronaView(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        );

        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.addView(myCoronaView, layoutParams);
        setContentView(rootLayout);

        myCoronaView.init(projectPath);
    }

    private void applySolarDroidConfig(File projectDir) {
        File configFile = new File(projectDir, "config.lua");
        if (!configFile.exists()) {
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();

            String content = sb.toString();

            int solarDroidIndex = content.indexOf("solarDroid");
            if (solarDroidIndex < 0) {
                return;
            }
            String afterSolarDroid = content.substring(solarDroidIndex);

            Pattern orientationPattern = Pattern.compile("orientation\\s*=\\s*[\"']([a-zA-Z]+)[\"']");
            Matcher matcher = orientationPattern.matcher(afterSolarDroid);
            if (matcher.find()) {
                String orientation = matcher.group(1).toLowerCase();
                if (orientation.equals("landscape")) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    ConsoleLog.add("[DEBUG] Orientacao aplicada: landscape");
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    ConsoleLog.add("[DEBUG] Orientacao aplicada: portrait");
                }
            }
        } catch (Exception e) {
            ConsoleLog.add("[DEBUG] Erro lendo config.lua: " + e.getMessage());
        }
    }

    private void copyImageAssetsToRoot(String assetPath, File rootDestDir) throws Exception {
        AssetManager assetManager = getAssets();
        String[] files = assetManager.list(assetPath);

        if (files == null || files.length == 0) {
            return;
        }

        for (String fileName : files) {
            String fullAssetPath = assetPath + "/" + fileName;
            String[] subFiles = assetManager.list(fullAssetPath);

            if (subFiles != null && subFiles.length > 0) {
                copyImageAssetsToRoot(fullAssetPath, rootDestDir);
            } else {
                String lower = fileName.toLowerCase();
                if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                    File destFile = new File(rootDestDir, fileName);
                    copyAssetFile(fullAssetPath, destFile);
                }
            }
        }
    }

    private void copyAssetFile(String assetPath, File destFile) throws Exception {
        AssetManager assetManager = getAssets();
        InputStream in = assetManager.open(assetPath);
        OutputStream out = new FileOutputStream(destFile);

        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }

        in.close();
        out.close();
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
