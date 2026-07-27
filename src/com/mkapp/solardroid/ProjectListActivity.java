package com.mkapp.solardroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.List;

public class ProjectListActivity extends Activity {

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2002 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                File pendingApk = BuildApkDialog.getPendingApk();
                java.io.OutputStream out = getContentResolver().openOutputStream(data.getData());
                java.io.InputStream in = new java.io.FileInputStream(pendingApk);
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) { out.write(buffer, 0, read); }
                in.close();
                out.close();
                Toast.makeText(this, "APK salvo com sucesso!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private LinearLayout projectListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(Color.parseColor("#1A1A1A"));

        // Topbar
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setPadding(24, 32, 16, 16);
        topBar.setBackgroundColor(Color.parseColor("#2A2A2A"));
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText("SolarDroid");
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        topBar.addView(title, titleParams);

        Button openButton = new Button(this);
        openButton.setText("Open");
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = ProjectManager.listProjects(ProjectListActivity.this).size();
                Toast.makeText(ProjectListActivity.this, count + " projeto(s). Toque em um para abrir.", Toast.LENGTH_SHORT).show();
            }
        });
        topBar.addView(openButton);

        Button newButton = new Button(this);
        newButton.setText("New");
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewProjectDialog();
            }
        });
        topBar.addView(newButton);

        rootLayout.addView(topBar);

        // Lista de projetos (scrollable)
        ScrollView scrollView = new ScrollView(this);
        projectListContainer = new LinearLayout(this);
        projectListContainer.setOrientation(LinearLayout.VERTICAL);
        projectListContainer.setPadding(16, 16, 16, 16);
        scrollView.addView(projectListContainer);

        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        );
        rootLayout.addView(scrollView, scrollParams);

        setContentView(rootLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProjectList();
    }

    private void refreshProjectList() {
        projectListContainer.removeAllViews();
        List<File> projects = ProjectManager.listProjects(this);

        if (projects.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Nenhum projeto ainda. Toque em \"New\" para criar.");
            emptyText.setTextColor(Color.parseColor("#888888"));
            emptyText.setPadding(8, 32, 8, 8);
            projectListContainer.addView(emptyText);
            return;
        }

        for (final File projectDir : projects) {
            projectListContainer.addView(createProjectCard(projectDir));
        }
    }

    private View createProjectCard(final File projectDir) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#2A2A2A"));
        card.setPadding(20, 16, 20, 16);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 12);
        card.setLayoutParams(cardParams);

        TextView nameText = new TextView(this);
        nameText.setText(projectDir.getName());
        nameText.setTextColor(Color.WHITE);
        nameText.setTextSize(16);
        card.addView(nameText);

        TextView pathText = new TextView(this);
        pathText.setText(projectDir.getAbsolutePath());
        pathText.setTextColor(Color.parseColor("#888888"));
        pathText.setTextSize(11);
        pathText.setPadding(0, 4, 0, 0);
        card.addView(pathText);

        card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showProjectOptions(projectDir);
                return true;
            }
        });

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectListActivity.this, EditorActivity.class);
                intent.putExtra("projectName", projectDir.getName());
                startActivity(intent);
            }
        });

        return card;
    }

    private void showNewProjectDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Nome do projeto");

        new AlertDialog.Builder(this)
            .setTitle("Novo Projeto")
            .setView(input)
            .setPositiveButton("Criar", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    String name = input.getText().toString().trim();
                    createNewProject(name);
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void createNewProject(String name) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Digite um nome valido", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ProjectManager.projectExists(this, name)) {
            Toast.makeText(this, "Ja existe um projeto com esse nome", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            ProjectManager.createProject(this, name);
            refreshProjectList();
            Toast.makeText(this, "Projeto criado!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showProjectOptions(final File projectDir) {
        new AlertDialog.Builder(this)
            .setTitle(projectDir.getName())
            .setItems(new String[]{"Build APK", "Excluir"}, new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    if (which == 0) {
                        BuildApkDialog.show(ProjectListActivity.this, projectDir);
                    } else if (which == 1) {
                        confirmDeleteProject(projectDir);
                    }
                }
            })
            .show();
    }

    private void confirmDeleteProject(final File projectDir) {
        new AlertDialog.Builder(this)
            .setTitle("Excluir projeto?")
            .setMessage("Isso vai apagar o projeto permanentemente.")
            .setPositiveButton("Excluir", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    deleteRecursiveFile(projectDir);
                    refreshProjectList();
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void deleteRecursiveFile(File f) {
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursiveFile(child);
                }
            }
        }
        f.delete();
    }
}
