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

import java.security.MessageDigest;


public class VerifyAppGuard {
    private final int APP_PACKAGE_DOT_COUNT = 3; // number of dots present in package name

    Activity currentAct;
    StringBuilder logBuilder;

    public int guardAppWith(Activity act, String verifyAppSignature, String myPackageName, boolean noDualApp, boolean noEmulator, boolean noPackageInstaller, boolean noDebugger, boolean noDevOptionMode, boolean enableLogs, boolean getAppSignature)
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
                return 9;
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
                return 10;
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
        String manufacturer = "", brand = "", fingerprint = "", product = "", device = "", model = "", hardware = "";

        try {
            manufacturer = Build.MANUFACTURER;
        } catch (Exception e) {}

        try {
            brand = Build.BRAND;
        } catch (Exception e) {}

        try {
            fingerprint = Build.FINGERPRINT;
        } catch (Exception e) {}

        try {
            product = Build.PRODUCT;
        } catch (Exception e) {}

        try {
            device = Build.DEVICE;
        } catch (Exception e) {}

        try {
            model = Build.MODEL;
        } catch (Exception e) {}

        try {
            hardware = Build.HARDWARE;
        } catch (Exception e) {}

        logBuilder.append("Build.MANUFACTURER:::").append(manufacturer).append("\n");
        logBuilder.append("Build.BRAND:::").append(brand).append("\n");
        logBuilder.append("Build.FINGERPRINT:::").append(fingerprint).append("\n");
        logBuilder.append("Build.PRODUCT:::").append(product).append("\n");
        logBuilder.append("Build.MODEL:::").append(model).append("\n");
        logBuilder.append("Build.DEVICE:::").append(device).append("\n");
        logBuilder.append("Build.HARDWARE:::").append(hardware).append("\n");
        logBuilder.append("--------------------------------").append("\n");

        int ratingCheckEmulator = 0;

        if (product.contains("sdk") || product.contains("Andy") ||
                product.contains("ttVM_Hdragon") || product.contains("google_sdk") ||
                product.contains("Droid4X") || product.contains("nox") ||
                product.contains("sdk_x86") || product.contains("sdk_google") ||
                product.contains("vbox86p")) {
            ratingCheckEmulator++;
        }


        if (manufacturer.equalsIgnoreCase("unknown") || manufacturer.equalsIgnoreCase("Genymotion") ||
                manufacturer.contains("Andy") || manufacturer.contains("MIT") ||
                manufacturer.contains("nox") || manufacturer.contains("TiantianVM")) {
            ratingCheckEmulator++;
        }

        if (brand.equalsIgnoreCase("generic") || brand.equalsIgnoreCase("generic_x86") ||
                brand.equalsIgnoreCase("TTVM") || brand.contains("Andy")) {
            ratingCheckEmulator++;
        }

        if (device.contains("generic") || device.contains("generic_x86") ||
                device.contains("Andy") || device.contains("ttVM_Hdragon") ||
                device.contains("Droid4X") || device.contains("nox") ||
                device.contains("generic_x86_64") || device.contains("vbox86p")) {
            ratingCheckEmulator++;
        }

        if (model.equalsIgnoreCase("sdk") || model.equalsIgnoreCase("google_sdk") ||
                model.contains("Droid4X") || model.contains("TiantianVM") ||
                model.contains("Andy") || model.equalsIgnoreCase(
                "Android SDK built for x86_64") ||
                model.equalsIgnoreCase("Android SDK built for x86")) {
            ratingCheckEmulator++;
        }

        if (hardware.equalsIgnoreCase("goldfish") || hardware.equalsIgnoreCase("vbox86") ||
                hardware.contains("nox") || hardware.contains("ttVM_x86")) {
            ratingCheckEmulator++;
        }

        if (fingerprint.contains("generic") ||
                fingerprint.contains("generic/sdk/generic") ||
                fingerprint.contains("generic_x86/sdk_x86/generic_x86") ||
                fingerprint.contains("Andy") || fingerprint.contains("ttVM_Hdragon") ||
                fingerprint.contains("generic_x86_64") ||
                fingerprint.contains("generic/google_sdk/generic") ||
                fingerprint.contains("vbox86p") ||
                fingerprint.contains("generic/vbox86p/vbox86p")) {
            ratingCheckEmulator++;
        }


        return ratingCheckEmulator > 3;

    }

    private boolean detectDebugger() {
        boolean result = false;

        try {
            String keys = Build.TAGS;
            result =  (keys != null && keys.contains("test-keys"));

            if(!result){
                result = Debug.isDebuggerConnected();
            }

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
}
