package com.fffz.amessenger.sample;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;

import java.io.FileInputStream;
import java.util.List;

public class Util {

    private static String processName;

    public static String getProcessName(Context context) {
        if (processName != null) {
            return processName;
        }
        //will not null
        processName = getProcessNameInternal(context);
        return processName;
    }

    private static String getProcessNameInternal(final Context context) {
        int myPid = android.os.Process.myPid();

        if (context == null || myPid <= 0) {
            return "";
        }

        ActivityManager.RunningAppProcessInfo myProcess = null;
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager != null) {
            List<ActivityManager.RunningAppProcessInfo> appProcessList = activityManager
                    .getRunningAppProcesses();

            if (appProcessList != null) {
                try {
                    for (ActivityManager.RunningAppProcessInfo process : appProcessList) {
                        if (process.pid == myPid) {
                            myProcess = process;
                            break;
                        }
                    }
                } catch (Exception e) {
                }

                if (myProcess != null) {
                    return myProcess.processName;
                }
            }
        }

        byte[] b = new byte[128];
        FileInputStream in = null;
        try {
            in = new FileInputStream("/proc/" + myPid + "/cmdline");
            int len = in.read(b);
            if (len > 0) {
                for (int i = 0; i < len; i++) { // lots of '0' in tail , remove them
                    if (b[i] > 128 || b[i] <= 0) {
                        len = i;
                        break;
                    }
                }
                return new String(b, 0, len);
            }

        } catch (Exception e) {
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
            }
        }
        return "";
    }

    public static boolean isMainProcess(Context context) {
        String processName = getProcessName(context);
        if (!TextUtils.isEmpty(processName) && processName.equals(context.getPackageName())) {
            return true;
        }
        return false;
    }

    public static boolean isPlayerProcess(Context context) {
        String processName = getProcessName(context);
        if (!TextUtils.isEmpty(processName) && processName.equals(context.getPackageName() + ":player")) {
            return true;
        }
        return false;
    }

    public static boolean isDownloadProcess(Context context) {
        String processName = getProcessName(context);
        if (!TextUtils.isEmpty(processName) && processName.equals(context.getPackageName() + ":download")) {
            return true;
        }
        return false;
    }

}
