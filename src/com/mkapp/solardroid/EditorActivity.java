package com.mkapp.solardroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;

public class EditorActivity extends Activity {

    private static final int REQUEST_CODE_IMPORT_FILE = 1001;

    private EditText codeEditor;
    private TextView consoleView;
    private ScrollView consoleScroll;
    private File projectDir;
    private File currentFile;
    private String projectName;

    private FrameLayout rootStack;
    private LinearLayout sideMenu;
    private LinearLayout fileListContainer;
    private boolean isMenuOpen = false;
    private TextView fileLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));

        projectName = getIntent().getStringExtra("projectName");
        if (projectName == null) {
            projectName = "SolarProject";
        }

        projectDir = ProjectManager.getProjectDir(this, projectName);
        if (!projectDir.exists()) {
            try {
                ProjectManager.createProject(this, projectName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        currentFile = new File(projectDir, "main.lua");

        rootStack = new FrameLayout(this);

        LinearLayout mainContent = buildMainContent();
        rootStack.addView(mainContent, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        ));

        sideMenu = buildSideMenu();
        FrameLayout.LayoutParams menuParams = new FrameLayout.LayoutParams(
            (int) (280 * getResources().getDisplayMetrics().density),
            FrameLayout.LayoutParams.MATCH_PARENT
        );
        sideMenu.setVisibility(View.GONE);
        rootStack.addView(sideMenu, menuParams);

        setContentView(rootStack);

        loadFile(currentFile);
    }

    private LinearLayout buildMainContent() {
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(Color.parseColor("#1A1A1A"));

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setPadding(16, 24, 16, 16);
        topBar.setBackgroundColor(Color.parseColor("#2A2A2A"));
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        Button menuButton = new Button(this);
        menuButton.setText("☰");
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMenu();
            }
        });
        topBar.addView(menuButton);

        fileLabel = new TextView(this);
        fileLabel.setText(projectName + " / " + currentFile.getName());
        fileLabel.setTextColor(Color.WHITE);
        fileLabel.setTextSize(14);
        fileLabel.setPadding(16, 0, 0, 0);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        );
        topBar.addView(fileLabel, labelParams);

        Button playButton = new Button(this);
        playButton.setText("▶ Play");
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndRun();
            }
        });
        topBar.addView(playButton);

        rootLayout.addView(topBar);

        codeEditor = new EditText(this);
        codeEditor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        codeEditor.setTypeface(Typeface.MONOSPACE);
        codeEditor.setTextSize(13);
        codeEditor.setTextColor(Color.parseColor("#E0E0E0"));
        codeEditor.setBackgroundColor(Color.parseColor("#1A1A1A"));
        codeEditor.setPadding(16, 16, 16, 16);
        codeEditor.setGravity(Gravity.TOP | Gravity.START);
        codeEditor.setHorizontallyScrolling(true);

        LinearLayout.LayoutParams editorParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 2f
        );
        rootLayout.addView(codeEditor, editorParams);

        LinearLayout consoleBar = new LinearLayout(this);
        consoleBar.setOrientation(LinearLayout.HORIZONTAL);
        consoleBar.setPadding(16, 8, 16, 8);
        consoleBar.setBackgroundColor(Color.parseColor("#252525"));
        consoleBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView consoleLabel = new TextView(this);
        consoleLabel.setText("Console");
        consoleLabel.setTextColor(Color.parseColor("#AAAAAA"));
        consoleLabel.setTextSize(12);
        LinearLayout.LayoutParams consoleLabelParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        );
        consoleBar.addView(consoleLabel, consoleLabelParams);

        Button copyButton = new Button(this);
        copyButton.setText("Copiar");
        copyButton.setTextSize(11);
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyLogToClipboard();
            }
        });
        consoleBar.addView(copyButton);

        Button clearButton = new Button(this);
        clearButton.setText("Limpar");
        clearButton.setTextSize(11);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConsoleLog.clear();
                refreshConsole();
            }
        });
        consoleBar.addView(clearButton);

        rootLayout.addView(consoleBar);

        consoleScroll = new ScrollView(this);
        consoleScroll.setBackgroundColor(Color.parseColor("#0F0F0F"));
        LinearLayout.LayoutParams consoleScrollParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        );
        consoleView = new TextView(this);
        consoleView.setTypeface(Typeface.MONOSPACE);
        consoleView.setTextSize(10);
        consoleView.setTextColor(Color.parseColor("#88FF88"));
        consoleView.setPadding(12, 8, 12, 8);
        consoleView.setTextIsSelectable(true);
        consoleScroll.addView(consoleView);
        rootLayout.addView(consoleScroll, consoleScrollParams);

        return rootLayout;
    }

    private LinearLayout buildSideMenu() {
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setBackgroundColor(Color.parseColor("#222222"));

        LinearLayout menuTop = new LinearLayout(this);
        menuTop.setOrientation(LinearLayout.HORIZONTAL);
        menuTop.setPadding(16, 24, 16, 16);
        menuTop.setBackgroundColor(Color.parseColor("#2A2A2A"));
        menuTop.setGravity(Gravity.CENTER_VERTICAL);

        TextView menuTitle = new TextView(this);
        menuTitle.setText("Arquivos");
        menuTitle.setTextColor(Color.WHITE);
        menuTitle.setTextSize(14);
        LinearLayout.LayoutParams menuTitleParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        menuTop.addView(menuTitle, menuTitleParams);

        Button newFileBtn = new Button(this);
        newFileBtn.setText("+F");
        newFileBtn.setTextSize(10);
        newFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewFileDialog(false);
            }
        });
        menuTop.addView(newFileBtn);

        Button newFolderBtn = new Button(this);
        newFolderBtn.setText("+P");
        newFolderBtn.setTextSize(10);
        newFolderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewFileDialog(true);
            }
        });
        menuTop.addView(newFolderBtn);

        Button importBtn = new Button(this);
        importBtn.setText("Import");
        importBtn.setTextSize(10);
        importBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
            }
        });
        menuTop.addView(importBtn);

        menu.addView(menuTop);

        TextView projectNameLabel = new TextView(this);
        projectNameLabel.setText(projectName);
        projectNameLabel.setTextColor(Color.parseColor("#66AAFF"));
        projectNameLabel.setTextSize(13);
        projectNameLabel.setPadding(16, 12, 16, 8);
        menu.addView(projectNameLabel);

        ScrollView scroll = new ScrollView(this);
        fileListContainer = new LinearLayout(this);
        fileListContainer.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(fileListContainer);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        );
        menu.addView(scroll, scrollParams);

        refreshFileList();

        return menu;
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        try {
            startActivityForResult(intent, REQUEST_CODE_IMPORT_FILE);
        } catch (Exception e) {
            Toast.makeText(this, "Nao foi possivel abrir o seletor de arquivos", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMPORT_FILE && resultCode == RESULT_OK && data != null) {
            int importedCount = 0;

            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    if (importFileFromUri(uri)) {
                        importedCount++;
                    }
                }
            } else if (data.getData() != null) {
                if (importFileFromUri(data.getData())) {
                    importedCount++;
                }
            }

            Toast.makeText(this, importedCount + " arquivo(s) importado(s)", Toast.LENGTH_SHORT).show();
            refreshFileList();
        }
    }

    private boolean importFileFromUri(Uri uri) {
        try {
            String fileName = getFileNameFromUri(uri);
            if (fileName == null) {
                fileName = "arquivo_importado";
            }

            File destFile = new File(projectDir, fileName);
            InputStream in = getContentResolver().openInputStream(uri);
            OutputStream out = new FileOutputStream(destFile);

            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private void refreshFileList() {
        fileListContainer.removeAllViews();
        addFilesFromDir(projectDir, 0);
    }

    private void addFilesFromDir(File dir, int depth) {
        File[] files = dir.listFiles();
        if (files == null) return;

        java.util.Arrays.sort(files, new java.util.Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                if (a.isDirectory() != b.isDirectory()) {
                    return a.isDirectory() ? -1 : 1;
                }
                return a.getName().compareToIgnoreCase(b.getName());
            }
        });

        for (final File f : files) {
            TextView item = new TextView(this);
            String prefix = "";
            for (int i = 0; i < depth; i++) prefix += "    ";
            item.setText(prefix + (f.isDirectory() ? "📁 " : "📄 ") + f.getName());
            item.setTextColor(f.equals(currentFile) ? Color.parseColor("#66AAFF") : Color.parseColor("#DDDDDD"));
            item.setTextSize(13);
            item.setPadding(16, 10, 16, 10);

            if (f.isFile()) {
            item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    confirmDeleteFile(f);
                    return true;
                }
            });

                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isTextFile(f)) {
                            saveCurrentFile();
                            currentFile = f;
                            loadFile(f);
                        } else {
                            Toast.makeText(EditorActivity.this, f.getName() + " (arquivo binario, nao editavel)", Toast.LENGTH_SHORT).show();
                        }
                        toggleMenu();
                    }
                });
            }

            fileListContainer.addView(item);

            if (f.isDirectory()) {
                addFilesFromDir(f, depth + 1);
            }
        }
    }

    private boolean isTextFile(File f) {
        String name = f.getName().toLowerCase();
        return name.endsWith(".lua") || name.endsWith(".txt") || name.endsWith(".json")
            || name.endsWith(".xml") || name.endsWith(".md");
    }

    private void confirmDeleteFile(final File f) {
        new AlertDialog.Builder(this)
            .setTitle("Excluir " + f.getName() + "?")
            .setPositiveButton("Excluir", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    deleteRecursive(f);
                    refreshFileList();
                    Toast.makeText(EditorActivity.this, "Excluido", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void deleteRecursive(File f) {
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        f.delete();
    }

    private void showNewFileDialog(final boolean isFolder) {
        final EditText input = new EditText(this);
        input.setHint(isFolder ? "nome_da_pasta" : "arquivo.lua");

        new AlertDialog.Builder(this)
            .setTitle(isFolder ? "Nova Pasta" : "Novo Arquivo")
            .setView(input)
            .setPositiveButton("Criar", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) return;
                    File newFile = new File(projectDir, name);
                    try {
                        if (isFolder) {
                            newFile.mkdirs();
                        } else {
                            newFile.createNewFile();
                        }
                        refreshFileList();
                    } catch (Exception e) {
                        Toast.makeText(EditorActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void toggleMenu() {
        isMenuOpen = !isMenuOpen;
        sideMenu.setVisibility(isMenuOpen ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (isMenuOpen) {
            toggleMenu();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshConsole();
    }

    private void refreshConsole() {
        consoleView.setText(ConsoleLog.getAll());
        consoleScroll.post(new Runnable() {
            @Override
            public void run() {
                consoleScroll.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void copyLogToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("SolarDroid Console", ConsoleLog.getAll());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Log copiado!", Toast.LENGTH_SHORT).show();
    }

    private void loadFile(File file) {
        try {
            if (file.exists()) {
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                reader.close();
                codeEditor.setText(sb.toString());
            } else {
                codeEditor.setText("");
            }
            if (fileLabel != null) {
                fileLabel.setText(projectName + " / " + file.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveCurrentFile() {
        try {
            String code = codeEditor.getText().toString();
            FileOutputStream fos = new FileOutputStream(currentFile);
            fos.write(code.getBytes("UTF-8"));
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveAndRun() {
        try {
            saveCurrentFile();
            Intent intent = new Intent(EditorActivity.this, MainActivity.class);
            intent.putExtra("projectName", projectName);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCurrentFile();
    }
}
