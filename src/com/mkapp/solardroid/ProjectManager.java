package com.mkapp.solardroid;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.ansca.corona.CoronaEnvironment;

public class ProjectManager {

    private static final String DEFAULT_MAIN_LUA =
        "display.setDefault(\"background\", 0.05, 0.05, 0.1)\n\n" +
        "local title = display.newText(\"Ola SolarDroid!\", display.contentCenterX, display.contentCenterY, native.systemFont, 20)\n" +
        "title:setFillColor(1, 1, 1)\n\n" +
        "local circle = display.newCircle(display.contentCenterX, display.contentCenterY + 80, 30)\n" +
        "circle:setFillColor(0.3, 0.7, 1)\n" +
        "transition.to(circle, {rotation=360, time=2000, iterations=-1})\n";

    private static final String DEFAULT_CONFIG_LUA =
        "application =\n" +
        "{\n" +
        "\tcontent =\n" +
        "\t{\n" +
        "\t\twidth = 320,\n" +
        "\t\theight = 480,\n" +
        "\t\tscale = \"letterbox\",\n" +
        "\t\tfps = 60,\n" +
        "\n" +
        "\t\timageSuffix =\n" +
        "\t\t{\n" +
        "\t\t\t[\"@2x\"] = 2,\n" +
        "\t\t\t[\"@4x\"] = 4,\n" +
        "\t\t},\n" +
        "\t},\n" +
        "}\n" +
        "\n" +
        "solarDroid =\n" +
        "{\n" +
        "\torientation = \"portrait\", -- portrait ou landscape\n" +
        "\timmersive = false, -- true esconde status bar e navigation bar\n" +
        "\tpermissions = {}, -- ex: {\"android.permission.CAMERA\", \"android.permission.RECORD_AUDIO\"}\n" +
        "}\n";

    public static File getProjectsRootDir(Context context) {
        File documentsDir = CoronaEnvironment.getDocumentsDirectory(context);
        File projectsDir = new File(documentsDir, "Projects");
        if (!projectsDir.exists()) {
            projectsDir.mkdirs();
        }
        return projectsDir;
    }

    public static File getProjectDir(Context context, String projectName) {
        return new File(getProjectsRootDir(context), projectName);
    }

    public static boolean projectExists(Context context, String projectName) {
        return getProjectDir(context, projectName).exists();
    }

    public static File createProject(Context context, String projectName) throws Exception {
        File projectDir = getProjectDir(context, projectName);
        if (projectDir.exists()) {
            throw new Exception("Projeto ja existe");
        }
        if (!projectDir.mkdirs()) {
            throw new Exception("Nao foi possivel criar a pasta do projeto");
        }

        writeFile(new File(projectDir, "main.lua"), DEFAULT_MAIN_LUA);
        writeFile(new File(projectDir, "config.lua"), DEFAULT_CONFIG_LUA);

        return projectDir;
    }

    private static void writeFile(File file, String content) throws Exception {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content.getBytes("UTF-8"));
        fos.close();
    }

    public static List<File> listProjects(Context context) {
        File root = getProjectsRootDir(context);
        File[] dirs = root.listFiles();
        List<File> result = new ArrayList<File>();
        if (dirs != null) {
            for (File d : dirs) {
                if (d.isDirectory()) {
                    result.add(d);
                }
            }
        }
        Arrays.sort(result.toArray(new File[0]));
        java.util.Collections.sort(result, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                return Long.compare(b.lastModified(), a.lastModified());
            }
        });
        return result;
    }
}
