package com.mkapp.solardroid;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.android.apksig.ApkSigner;

public class ApkBuilder {

    public interface ProgressListener {
        void onProgress(String message);
    }

    private Context context;
    private File toolsDir;
    private File workDir;
    private ProgressListener listener;

    public ApkBuilder(Context context) {
        this.context = context;
        this.toolsDir = new File(context.getFilesDir(), "buildtools");
        this.workDir = new File(context.getCacheDir(), "apkbuild");
    }

    public void setProgressListener(ProgressListener l) {
        this.listener = l;
    }

    private void log(String msg) {
        if (listener != null) {
            listener.onProgress(msg);
        }
    }

    public File getAapt2() {
        return new File(context.getApplicationInfo().nativeLibraryDir, "libaapt2tool.so");
    }

    public File getZipalign() {
        return new File(context.getApplicationInfo().nativeLibraryDir, "libzipaligntool.so");
    }

    public File getAndroidJar() {
        return new File(toolsDir, "android.jar");
    }

    public File getKeystore() {
        return new File(toolsDir, "solardroid.keystore");
    }

    public void ensureToolsExtracted() throws Exception {
        if (!toolsDir.exists()) {
            toolsDir.mkdirs();
        }
        extractAssetIfMissing("tools/aapt2", getAapt2());
        extractAssetIfMissing("tools/solardroid.keystore", getKeystore());
        extractAssetIfMissing("tools/android.jar", getAndroidJar());

        getAapt2().setExecutable(true, false);
    }

    private void extractAssetIfMissing(String assetPath, File destFile) throws Exception {
        if (destFile.exists() && destFile.length() > 0) {
            return;
        }
        InputStream in = context.getAssets().open(assetPath);
        OutputStream out = new FileOutputStream(destFile);
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        out.close();
    }

    public File getTemplateApk() {
        ApplicationInfo appInfo = context.getApplicationInfo();
        return new File(appInfo.sourceDir);
    }

    private File prepareWorkDir() throws Exception {
        if (workDir.exists()) {
            deleteRecursive(workDir);
        }
        workDir.mkdirs();
        return workDir;
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

    private String readOrientationFromConfig(File projectDir) {
        File configFile = new File(projectDir, "config.lua");
        if (!configFile.exists()) {
            return "portrait";
        }
        try {
            StringBuilder sb = new StringBuilder();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(new FileInputStream(configFile), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            String content = sb.toString();
            int idx = content.indexOf("solarDroid");
            if (idx < 0) return "portrait";
            String after = content.substring(idx);
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("orientation\\s*=\\s*[\"']([a-zA-Z]+)[\"']");
            java.util.regex.Matcher m = p.matcher(after);
            if (m.find()) {
                return m.group(1).toLowerCase();
            }
        } catch (Exception e) {
            // ignora, usa padrao
        }
        return "portrait";
    }

    private String generateManifest(String packageName, String appName, String versionName, int versionCode, String orientation) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        sb.append("<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n");
        sb.append("    package=\"").append(packageName).append("\"\n");
        sb.append("    android:versionCode=\"").append(versionCode).append("\"\n");
        sb.append("    android:versionName=\"").append(versionName).append("\">\n");
        sb.append("    <uses-sdk android:minSdkVersion=\"21\" android:targetSdkVersion=\"34\" />\n");
        sb.append("    <uses-feature android:glEsVersion=\"0x00020000\" android:required=\"true\" />\n");
        sb.append("    <uses-permission android:name=\"android.permission.INTERNET\" />\n");
        sb.append("    <application\n");
        sb.append("        android:label=\"").append(appName).append("\"\n");
        sb.append("        android:allowBackup=\"true\"\n");
        sb.append("        android:hardwareAccelerated=\"true\"\n");
        sb.append("        android:theme=\"@android:style/Theme.Material.NoActionBar\"\n");
        sb.append(">\n");
        String androidOrientation = orientation.equals("landscape") ? "landscape" : "portrait";
        sb.append("        <activity\n");
        sb.append("            android:name=\"com.mkapp.solardroid.StandaloneRunActivity\"\n");
        sb.append("            android:screenOrientation=\"").append(androidOrientation).append("\"\n");
        sb.append("            android:theme=\"@android:style/Theme.Material.NoActionBar\"\n");
        sb.append("            android:exported=\"true\">\n");
        sb.append("            <intent-filter>\n");
        sb.append("                <action android:name=\"android.intent.action.MAIN\" />\n");
        sb.append("                <category android:name=\"android.intent.category.LAUNCHER\" />\n");
        sb.append("            </intent-filter>\n");
        sb.append("        </activity>\n");
        sb.append("        <activity\n");
        sb.append("            android:name=\"com.mkapp.solardroid.CrashActivity\"\n");
        sb.append("            android:exported=\"false\" />\n");
        sb.append("    </application>\n");
        sb.append("</manifest>\n");
        return sb.toString();
    }

    private void runCommand(List<String> command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Comando falhou (" + command.get(0) + "): " + output.toString());
        }
    }

    public File buildApk(File projectDir, String packageName, String appName, String versionName, int versionCode) throws Exception {
        log("Preparando ferramentas...");
        ensureToolsExtracted();

        File work = prepareWorkDir();
        File manifestFile = new File(work, "AndroidManifest.xml");
        File linkedApk = new File(work, "linked.apk");
        File finalUnsigned = new File(work, "unsigned.apk");
        File finalAligned = new File(work, "aligned.apk");
        File outputApk = new File(work, appName.replaceAll("[^a-zA-Z0-9]", "_") + ".apk");

        log("Gerando AndroidManifest.xml...");
        String orientation = readOrientationFromConfig(projectDir);
        String manifestContent = generateManifest(packageName, appName, versionName, versionCode, orientation);
        FileOutputStream manifestOut = new FileOutputStream(manifestFile);
        manifestOut.write(manifestContent.getBytes("UTF-8"));
        manifestOut.close();

        log("Compilando recursos com aapt2...");
        List<String> aaptCommand = new ArrayList<String>();
        aaptCommand.add(getAapt2().getAbsolutePath());
        aaptCommand.add("link");
        aaptCommand.add("-o");
        aaptCommand.add(linkedApk.getAbsolutePath());
        aaptCommand.add("-I");
        aaptCommand.add(getAndroidJar().getAbsolutePath());
        aaptCommand.add("--manifest");
        aaptCommand.add(manifestFile.getAbsolutePath());
        runCommand(aaptCommand);

        log("Copiando estrutura da engine...");
        File templateApk = getTemplateApk();
        buildFinalApk(templateApk, linkedApk, projectDir, finalUnsigned);

        log("Assinando APK...");
        signApk(finalUnsigned, outputApk);
        signApk(finalUnsigned, outputApk);

        log("Concluido!");
        return outputApk;
    }

    private void buildFinalApk(File templateApk, File linkedApk, File projectDir, File outputFile) throws Exception {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile));
        java.util.Set<String> addedEntries = new java.util.HashSet<String>();

        // 1. Copiar do linkedApk (novo Manifest + resources.arsc + res/)
        ZipFile linkedZip = new ZipFile(linkedApk);
        Enumeration<? extends ZipEntry> linkedEntries = linkedZip.entries();
        while (linkedEntries.hasMoreElements()) {
            ZipEntry entry = linkedEntries.nextElement();
            if (addedEntries.contains(entry.getName())) continue;
            addZipEntry(zos, linkedZip.getInputStream(entry), entry.getName());
            addedEntries.add(entry.getName());
        }
        linkedZip.close();

        // 2. Copiar do template (classes.dex, lib/, META-INF removido depois na assinatura)
        ZipFile templateZip = new ZipFile(templateApk);
        Enumeration<? extends ZipEntry> templateEntries = templateZip.entries();
        while (templateEntries.hasMoreElements()) {
            ZipEntry entry = templateEntries.nextElement();
            String name = entry.getName();
            if (addedEntries.contains(name)) continue;
            if (name.startsWith("META-INF/")) continue;
            if (name.equals("AndroidManifest.xml")) continue;
            if (name.equals("resources.arsc")) continue;
            if (name.startsWith("res/")) continue;
            if (name.startsWith("assets/")) continue;
            addZipEntry(zos, templateZip.getInputStream(entry), name);
            addedEntries.add(name);
        }
        templateZip.close();

        // 3. Adicionar assets do projeto do usuario (main.lua, config.lua, etc.)
        log("Copiando arquivos de: " + projectDir.getAbsolutePath());
        File[] debugFiles = projectDir.listFiles();
        if (debugFiles != null) {
            for (File df : debugFiles) {
                log("  encontrado: " + df.getName() + (df.isDirectory() ? " [pasta]" : ""));
            }
        } else {
            log("  ERRO: projectDir.listFiles() retornou null!");
        }
        addProjectAssets(zos, projectDir, "assets/SolarProject", addedEntries);

        zos.close();
    }

    private void addProjectAssets(ZipOutputStream zos, File dir, String zipPrefix, java.util.Set<String> addedEntries) throws Exception {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            String entryName = zipPrefix + "/" + f.getName();
            if (f.isDirectory()) {
                addProjectAssets(zos, f, entryName, addedEntries);
            } else {
                if (addedEntries.contains(entryName)) continue;
                InputStream in = new FileInputStream(f);
                addZipEntry(zos, in, entryName);
                addedEntries.add(entryName);
            }
        }
    }

    private long currentZipOffset = 0;

    private void addZipEntry(ZipOutputStream zos, InputStream in, String name) throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            baos.write(buffer, 0, read);
        }
        in.close();
        byte[] data = baos.toByteArray();

        ZipEntry newEntry = new ZipEntry(name);
        int nameLen = name.getBytes("UTF-8").length;

        newEntry.setMethod(ZipEntry.STORED);
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(data);
        newEntry.setCrc(crc.getValue());
        newEntry.setSize(data.length);
        newEntry.setCompressedSize(data.length);

        int extraLen = 0;
        if (name.equals("resources.arsc")) {
            long dataStart = currentZipOffset + 30 + nameLen;
            int padding = (int) ((4 - (dataStart % 4)) % 4);
            if (padding > 0) {
                newEntry.setExtra(new byte[padding]);
                extraLen = padding;
            }
        }

        zos.putNextEntry(newEntry);
        zos.write(data);
        zos.closeEntry();

        currentZipOffset += 30 + nameLen + extraLen + data.length;
    }

    private void signApk(File inputApk, File outputApk) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        FileInputStream keystoreStream = new FileInputStream(getKeystore());
        char[] password = "solardroid123".toCharArray();
        keyStore.load(keystoreStream, password);
        keystoreStream.close();

        String alias = "solardroid";
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password);
        java.security.cert.Certificate[] certChain = keyStore.getCertificateChain(alias);

        List<X509Certificate> certs = new ArrayList<X509Certificate>();
        for (java.security.cert.Certificate c : certChain) {
            certs.add((X509Certificate) c);
        }

        ApkSigner.SignerConfig signerConfig = new ApkSigner.SignerConfig.Builder(
            "solardroid", privateKey, certs
        ).build();

        List<ApkSigner.SignerConfig> signerConfigs = new ArrayList<ApkSigner.SignerConfig>();
        signerConfigs.add(signerConfig);

        ApkSigner apkSigner = new ApkSigner.Builder(signerConfigs)
            .setInputApk(inputApk)
            .setOutputApk(outputApk)
            .setV1SigningEnabled(true)
            .setV2SigningEnabled(true)
            .setV3SigningEnabled(true)
            .setMinSdkVersion(21)
            .build();

        apkSigner.sign();
    }
}
