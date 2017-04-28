package de.till_s.spotifyshuffleradio.utils;

import android.content.pm.PackageManager;

/**
 * Created by Till on 23.04.2017.
 */

final public class Utils {

    public static boolean isPackageInstalled(String packagename, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packagename, 0);

            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static int getTimestamp() {
        return (int) (System.currentTimeMillis() / 1000L);
    }

}
