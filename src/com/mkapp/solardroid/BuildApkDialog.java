package com.mkapp.solardroid;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;

public class BuildApkDialog {
    private static File pendingApkToSave;
    public static File getPendingApk() { return pendingApkToSave; }

    public static void show(final Context context, final File projectDir) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 8);

        TextView nameLabel = new TextView(context);
        nameLabel.setText("Nome do App");
        nameLabel.setTextSize(12);
        layout.addView(nameLabel);

        final EditText nameInput = new EditText(context);
        nameInput.setText(projectDir.getName());
        layout.addView(nameInput);

        TextView packageLabel = new TextView(context);
        packageLabel.setText("Pacote (com.exemplo.app)");
        packageLabel.setTextSize(12);
        packageLabel.setPadding(0, 24, 0, 0);
        layout.addView(packageLabel);

        final EditText packageInput = new EditText(context);
        packageInput.setText("com.solardroid." + projectDir.getName().toLowerCase().replaceAll("[^a-z0-9]", ""));
        packageInput.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(packageInput);

        TextView versionLabel = new TextView(context);
        versionLabel.setText("Versao (ex: 1.0)");
        versionLabel.setTextSize(12);
        versionLabel.setPadding(0, 24, 0, 0);
        layout.addView(versionLabel);

        final EditText versionInput = new EditText(context);
        versionInput.setText("1.0");
        layout.addView(versionInput);

        new AlertDialog.Builder(context)
            .setTitle("Build APK")
            .setView(layout)
            .setPositiveButton("Compilar", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    String appName = nameInput.getText().toString().trim();
                    String packageName = packageInput.getText().toString().trim();
                    String versionName = versionInput.getText().toString().trim();

                    if (appName.isEmpty() || packageName.isEmpty() || versionName.isEmpty()) {
                        Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!packageName.matches("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)+$")) {
                        Toast.makeText(context, "Pacote invalido (ex: com.exemplo.app)", Toast.LENGTH_LONG).show();
                        return;
                    }

                    startBuild(context, projectDir, packageName, appName, versionName);
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private static void startBuild(final Context context, final File projectDir, final String packageName, final String appName, final String versionName) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Compilando APK...");
        progressDialog.setMessage("Iniciando...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ApkBuilder builder = new ApkBuilder(context);
                    builder.setProgressListener(new ApkBuilder.ProgressListener() {
                        @Override
                        public void onProgress(final String message) {
                            ((android.app.Activity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.setMessage(message);
                                }
                            });
                        }
                    });

                    final File outputApk = builder.buildApk(projectDir, packageName, appName, versionName, 1);

                    ((android.app.Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            offerToInstall(context, outputApk);
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    ((android.app.Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            new AlertDialog.Builder(context)
                                .setTitle("Erro no build")
                                .setMessage(e.getMessage())
                                .setPositiveButton("OK", null)
                                .show();
                        }
                    });
                }
            }
        }).start();
    }

    private static void offerToInstall(Context context, File apkFile) {
        new AlertDialog.Builder(context)
            .setTitle("APK gerado!")
            .setMessage("Local: " + apkFile.getAbsolutePath())
            .setPositiveButton("Instalar", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    installApk(context, apkFile);
                }
            })
            .setNeutralButton("Salvar como...", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    saveApkAs(context, apkFile);
                }
            })
            .setNegativeButton("Fechar", null)
            .show();
    }

    private static void saveApkAs(Context context, File apkFile) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.android.package-archive");
        intent.putExtra(Intent.EXTRA_TITLE, apkFile.getName());
        intent.putExtra("solardroid_apk_path", apkFile.getAbsolutePath());
        ((android.app.Activity) context).startActivityForResult(intent, 2002);
        pendingApkToSave = apkFile;
    }

    private static void installApk(Context context, File apkFile) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            android.net.Uri apkUri = android.net.Uri.fromFile(apkFile);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Nao foi possivel abrir instalador. APK salvo em: " + apkFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
    }
}
