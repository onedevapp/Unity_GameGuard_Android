package com.onedevapp.appguard;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Debug;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import java.security.MessageDigest;


public class VerifyAppGuard {
    private final int APP_PACKAGE_DOT_COUNT = 3; // number of dots present in package name

    Activity currentAct;
    StringBuilder logBuilder;

    public int guardAppWith(Activity act, String verifyAppSignature, String myPackageName, boolean noDualApp, boolean noEmulator, boolean noPackageInstaller, boolean noRoot, boolean noProxy, boolean noDebugger, boolean noDevOptionMode, boolean enableLogs, boolean getAppSignature)
    {
        currentAct = act;
        logBuilder = new StringBuilder();

        if(getAppSignature)
        {
            logBuilder.append("Get Certificate Signature").append("\n");
            logBuilder.append("--------------------------------").append("\n");
            logBuilder.append(getCertificateSignature());
            logBuilder.append("--------------------------------").append("\n");
        }

        if(verifyAppSignature != null && !verifyAppSignature.trim().isEmpty()){

            logBuilder.append("Started App Signature validation").append("\n");
            logBuilder.append("--------------------------------").append("\n");

            if(isAValidSignature(verifyAppSignature))
            {
                if (enableLogs) {
                    logBuilder.append("Validation Stopped at App Signature validation").append("\n");
                    Log.d("VerifyAppGuard", logBuilder.toString());
                } else {
                    logBuilder.setLength(0);
                }
                return 1;
            }
            logBuilder.append("--------------------------------").append("\n");
        }

        //Renamed?
        if (myPackageName != null && !myPackageName.trim().isEmpty())
        {
            logBuilder.append("Started Package Name validation").append("\n");
            logBuilder.append("--------------------------------").append("\n");
            logBuilder.append("currentAct.getPackageName():::").append(currentAct.getPackageName()).append("\n");
            logBuilder.append("myPackageName:::").append(myPackageName).append("\n");
            if(currentAct.getPackageName().compareTo(myPackageName) != 0)
            {
                if(enableLogs)
                {
                    logBuilder.append("Validation Stopped at Package Name validation").append("\n");
                    Log.d("VerifyAppGuard", logBuilder.toString());
                }
                else
                {
                    logBuilder.setLength(0);
                }
                return 2;
            }
            logBuilder.append("--------------------------------").append("\n");
        }

        //Relocated?
        if(noPackageInstaller)
        {
            logBuilder.append("Started Package Installation validation").append("\n");
            logBuilder.append("--------------------------------").append("\n");
            try {
                String installer = currentAct.getPackageManager()
                        .getInstallerPackageName(currentAct.getPackageName());

                logBuilder.append("Package installed via ").append(installer).append("\n");
                if(installer == null)
                {
                    if(enableLogs)
                    {
                        logBuilder.append("Stopped at Package Installation - UNKNOWN installation source").append("\n");
                        Log.d("VerifyAppGuard", logBuilder.toString());
                    }
                    else
                    {
                        logBuilder.setLength(0);
                    }
                    return 3;
                }
                if(installer.equals("com.google.android.packageinstaller") || installer.equals("com.android.packageinstaller") || installer.equals("com.google.android.apps.nbu.files"))
                {
                    if(enableLogs)
                    {
                        logBuilder.append("Stopped at Package Installation - APK installed").append("\n");
                        Log.d("VerifyAppGuard", logBuilder.toString());
                    }
                    else
                    {
                        logBuilder.setLength(0);
                    }
                    return 4;
                }
            } catch (Throwable e) {
                logBuilder.append("unable to read from package manager, ").append(e.toString()).append("\n");
            }

            logBuilder.append("--------------------------------").append("\n");
        }

        if(noDualApp)
        {
            logBuilder.append("Started DUAL validation").append("\n");
            logBuilder.append("--------------------------------").append("\n");
            String path = currentAct.getFilesDir().getPath();
            String DUAL_APP_ID_999 = "999";

            if (path.contains(DUAL_APP_ID_999))
            {
                if(enableLogs)
                {
                    logBuilder.append("Stopped at DUAL APP validation").append("\n");
                    Log.d("VerifyAppGuard", logBuilder.toString());
                }
                else
                {
                    logBuilder.setLength(0);
                }
                return 5;
            }
            else
            {
                int count = getDotCount(path);
                if (count > APP_PACKAGE_DOT_COUNT)
                {
                    if(enableLogs)
                    {
                        logBuilder.append("Stopped at DUAL APP - APP Package Count").append("\n");
                        Log.d("VerifyAppGuard", logBuilder.toString());
                    }
                    else
                    {
                        logBuilder.setLength(0);
                    }
                    return 6;
                }
            }

            if(zygoteCallCount())
            {
                if(enableLogs)
                {
                    logBuilder.append("Stopped at DUAL APP - Zygote Call Count").append("\n");
                    Log.d("VerifyAppGuard", logBuilder.toString());
                }
                else
                {
                    logBuilder.setLength(0);
                }
                return 7;
            }
            logBuilder.append("--------------------------------").append("\n");
        }

        if(noEmulator)
        {
            logBuilder.append("Started Emulator validation").append("\n");
            logBuilder.append("--------------------------------").append("\n");

            if(isEmulator())
            {
                if(enableLogs)
                {
                    logBuilder.append("Stopped at Emulator validation").append("\n");
                    Log.d("VerifyAppGuard", logBuilder.toString());
                }
                else
                {
                    logBuilder.setLength(0);
                }
                return 8;
            }
            logBuilder.append("--------------------------------").append("\n");
        }

        if(noRoot)
        {
            logBuilder.append("Started RootCheck validation").append("\n");
            logBuilder.append("--------------------------------").append("\n");

            if(checkRootMethod1() || checkRootMethod2() || checkRootMethod3())
            {
                if(enableLogs)
                {
                    logBuilder.append("Stopped at RootCheck validation").append("\n");
                    Log.d("VerifyAppGuard", logBuilder.toString());
                }
                else
                {
                    logBuilder.setLength(0);
                }
                return 9;
            }

            logBuilder.append("--------------------------------").append("\n");
        }

        if(noProxy)
        {
            logBuilder.append("Started Proxy validation").append("\n");
            logBuilder.append("--------------------------------").append("\n");

            String proxyHost = System.getProperty("https.proxyHost");
            if (proxyHost == null || proxyHost.trim().isEmpty())
                proxyHost = System.getProperty("http.proxyHost");
            String proxyPort = System.getProperty("https.proxyPort");
            if (proxyPort == null || proxyPort.trim().isEmpty())
                proxyPort = System.getProperty("http.proxyPort");

            if(!(proxyHost == null || proxyHost.trim().isEmpty()) && !(proxyPort == null || proxyPort.trim().isEmpty()))
            {
                if(enableLogs)
                {
                    logBuilder.append("Stopped at Proxy validation").append("\n");
                    logBuilder.append("proxyHost::").append(proxyHost).append("\n");
                    logBuilder.append("proxyPort::").append(proxyPort).append("\n");
                    Log.d("VerifyAppGuard", logBuilder.toString());
                }
                else
                {
                    logBuilder.setLength(0);
                }
                return 10;
            }

            logBuilder.append("--------------------------------").append("\n");
        }

        if(noDebugger)
        {
            logBuilder.append("Started Debugger validation").append("\n");
            logBuilder.append("--------------------------------").append("\n");

            if(detectDebugger())
            {
                if(enableLogs)
                {
                    logBuilder.append("Stopped at Debugger validation").append("\n");
                    Log.d("VerifyAppGuard", logBuilder.toString());
                }
                else
                {
                    logBuilder.setLength(0);
                }
                return 11;
            }
            logBuilder.append("--------------------------------").append("\n");
        }

        if(noDevOptionMode)
        {
            logBuilder.append("Started DevOptions validation").append("\n");
            logBuilder.append("--------------------------------").append("\n");

            if(detectDevMode())
            {
                if(enableLogs)
                {
                    logBuilder.append("Stopped at DevOptions validation").append("\n");
                    Log.d("VerifyAppGuard", logBuilder.toString());
                }
                else
                {
                    logBuilder.setLength(0);
                }
                return 12;
            }
            logBuilder.append("--------------------------------").append("\n");
        }

        if(enableLogs)
        {
            Log.d("VerifyAppGuard", logBuilder.toString());
        }
        else
        {
            logBuilder.setLength(0);
        }
        return 0;
    }

    /**
     * Checks if the apk signature is valid.
     *
     * @param certificateSignature The certificate signature.
     * @return a boolean indicating if the signature is valid.
     */

    private boolean isAValidSignature(String certificateSignature) {
        try {
            PackageInfo packageInfo = currentAct.getPackageManager().getPackageInfo(currentAct.getPackageName(), PackageManager.GET_SIGNATURES);

            // The APK is signed with multiple signatures, probably it was tampered.
            if (packageInfo.signatures.length > 1) {
                return false;
            }

            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");

                md.update(signature.toByteArray());

                if (certificateSignature.compareToIgnoreCase(Base64.encodeToString(md.digest(), Base64.DEFAULT)) == 0) {
                    return true;
                }
            }
        } catch (Exception exception) {
            Log.d("VerifyAppGuard", exception.getStackTrace().toString());
        }

        return false;
    }

    /**
     * If the count is more than 2 then the app is modified
     * @return Int
     */
    private boolean zygoteCallCount() {
        int zygoteInitCallCount = 0;
        try {
            throw new Exception("PiracyChecker");
        } catch (Exception e) {
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                if (stackTraceElement.getClassName().equals("com.android.internal.os.ZygoteInit")) {
                    zygoteInitCallCount++;
                }
                if (stackTraceElement.getClassName().equals("com.saurik.substrate.MS$2") && stackTraceElement.getMethodName().equals("invoked")) {
                    zygoteInitCallCount++;
                }
                if (stackTraceElement.getClassName().equals("de.robv.android.xposed.XposedBridge") && stackTraceElement.getMethodName().equals("main")) {
                    zygoteInitCallCount++;
                }
                if (stackTraceElement.getClassName().equals("de.robv.android.xposed.XposedBridge") && stackTraceElement.getMethodName().equals("handleHookedMethod")) {
                    zygoteInitCallCount++;
                }
            }
        }
        return zygoteInitCallCount > 1;
    }

    private int getDotCount(String path)
    {
        int count = 0;
        for (int i = 0; i < path.length(); i++)
        {
            if (count > APP_PACKAGE_DOT_COUNT)
            {
                break;
            }
            char DOT = '.';
            if (path.charAt(i) == DOT)
            {
                count++;
            }
        }
        return count;
    }

    private boolean isEmulator()
    {
        logBuilder.append("Build.MANUFACTURER:::").append(Build.MANUFACTURER).append("\n");
        logBuilder.append("Build.BRAND:::").append(Build.BRAND).append("\n");
        logBuilder.append("Build.FINGERPRINT:::").append(Build.FINGERPRINT).append("\n");
        logBuilder.append("Build.PRODUCT:::").append(Build.PRODUCT).append("\n");
        logBuilder.append("Build.MODEL:::").append(Build.MODEL).append("\n");
        logBuilder.append("Build.BOARD:::").append(Build.BOARD).append("\n");
        logBuilder.append("Build.HOST:::").append(Build.HOST).append("\n");
        logBuilder.append("Build.DEVICE:::").append(Build.DEVICE).append("\n");
        logBuilder.append("Build.HARDWARE:::").append(Build.HARDWARE).append("\n");
        logBuilder.append("--------------------------------").append("\n");

        return ((Build.MANUFACTURER == "Google" && Build.BRAND == "google" &&
                ((Build.FINGERPRINT.startsWith("google/sdk_gphone_")
                        && Build.FINGERPRINT.endsWith(":user/release-keys")
                        && Build.PRODUCT.startsWith("sdk_gphone_")
                        && Build.MODEL.startsWith("sdk_gphone_"))
                        //alternative
                        || (Build.FINGERPRINT.startsWith("google/sdk_gphone64_")
                        && (Build.FINGERPRINT.endsWith(":userdebug/dev-keys") || Build.FINGERPRINT.endsWith(":user/release-keys"))
                        && Build.PRODUCT.startsWith("sdk_gphone64_")
                        && Build.MODEL.startsWith("sdk_gphone64_"))))
                //
                || Build.PRODUCT.contains("simulator")
                || Build.PRODUCT.contains("sdk_gphone64_arm64")
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.FINGERPRINT.equals("robolectric")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                //bluestacks
                || Build.MANUFACTURER.contains("Geny")
                || Build.HOST.startsWith("Build")
                || Build.MANUFACTURER.equals("unknown")
                //MSI App Player
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || Build.PRODUCT == "google_sdk"
                //nox
                || Build.PRODUCT.toLowerCase().contains("nox")
                || Build.BOARD.toLowerCase().contains("nox")
                // another Android SDK emulator check
                || Build.DEVICE.equals("vbox86p")
                || Build.HARDWARE.equals("goldfish")
                || Build.HARDWARE.equals("vbox86"));
    }

    private boolean detectDebugger() {
        boolean result = false;

        try {
            
            result = Debug.isDebuggerConnected();

            if(!result){
                result = currentAct.getApplicationContext().getApplicationInfo().flags != 0 && ApplicationInfo.FLAG_DEBUGGABLE != 0;
            }

            if(!result){
                long start = Debug.threadCpuTimeNanos();
                for (int i = 0; i <= 999999; i++) continue;
                long stop = Debug.threadCpuTimeNanos();
                return stop - start >= 10000000;
            }

        } catch (Throwable e) {
        }

        return result;
    }


    private boolean detectDevMode() {

        boolean result = false;
        result = Settings.Secure.getInt(currentAct.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0
                || Settings.Secure.getInt(currentAct.getContentResolver(), Settings.Global.ADB_ENABLED, 0) == 1 ;

        return result;
    }

    public void killProcess()
    {
        if(currentAct == null){
            System.exit(0);
        }else{

            if(Build.VERSION.SDK_INT>=16 && Build.VERSION.SDK_INT<21){
                currentAct.finishAffinity();
            } else if(Build.VERSION.SDK_INT>=21){
                currentAct.finishAndRemoveTask();
            }
        }
    }

    /**
     * Get your current certificate signature.
     */

    private String getCertificateSignature() {
        try {
            PackageInfo packageInfo = currentAct.getPackageManager().getPackageInfo(currentAct.getPackageName(), PackageManager.GET_SIGNATURES);

            // The APK is signed with multiple signatures, probably it was tampered.
            if (packageInfo.signatures.length > 1) {
                return "";
            }

            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");

                md.update(signature.toByteArray());

                return Base64.encodeToString(md.digest(), Base64.DEFAULT);
            }
        } catch (Exception exception) {
            Log.d("VerifyAppGuard", exception.getStackTrace().toString());
        }
        return "";
    }

    /**
     * Checking the BUILD tag for test-keys. By default, stock Android ROMs from Google are built with release-keys tags.
     * If test-keys are present, this can mean that the Android build on the device is either a developer build or
     * an unofficial Google build.
     */
    private boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    /**
     * check if /system/app/Superuser.apk is present
     * This package is most often looked for on rooted devices. Superuser allows the user to authorize applications to run as root on the device.
     */
    private boolean checkRootMethod2() {
        String[] paths = {"/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                "/system/bin/failsafe/su", "/data/local/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    /**
     * Check if the SU command was successful
     * Execute su and then id to check if the current user has a uid of 0 or if it contains (root).
     */
    private boolean checkRootMethod3() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return in.readLine() != null;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }
}
