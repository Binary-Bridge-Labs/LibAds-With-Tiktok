package com.bbl.module_ads.util;

import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

/**
 * \\ \\ \\ \\ \\ \\ \\ \\ || || || || || || // // // // // // // //
 * \\ \\ \\ \\ \\ \\ \\        _ooOoo_          // // // // // // //
 * \\ \\ \\ \\ \\ \\          o8888888o            // // // // // //
 * \\ \\ \\ \\ \\             88" . "88               // // // // //
 * \\ \\ \\ \\                (| -_- |)                  // // // //
 * \\ \\ \\                   O\  =  /O                     // // //
 * \\ \\                   ____/`---'\____                     // //
 * \\                    .'  \\|     |//  `.                      //
 * ==                   /  \\|||  :  |||//  \                     ==
 * ==                  /  _||||| -:- |||||-  \                    ==
 * ==                  |   | \\\  -  /// |   |                    ==
 * ==                  | \_|  ''\---/''  |   |                    ==
 * ==                  \  .-\__  `-`  ___/-. /                    ==
 * ==                ___`. .'  /--.--\  `. . ___                  ==
 * ==              ."" '<  `.___\_<|>_/___.'  >'"".               ==
 * ==            | | :  `- \`.;`\ _ /`;.`/ - ` : | |              \\
 * //            \  \ `-.   \_ __\ /__ _/   .-` /  /              \\
 * //      ========`-.____`-.___\_____/___.-`____.-'========      \\
 * //                           `=---='                           \\
 * // //   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  \\ \\
 * // // //    Buddha blessed    Never BUG    Never modify   \\ \\ \\
 **/
public class CheckLocalUser {

    private CheckLocalUser() {
        // Utility class
    }

    /**
     * Returns the current {@link Locale} in use.
     */
    public static Locale getCurrentLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList locales = LocaleList.getDefault();
            if (!locales.isEmpty()) {
                return locales.get(0);
            }
        }
        return Locale.getDefault();
    }

    /**
     * Returns the current ISO language code (e.g., {@code "en"}, {@code "vi"}).
     */
    public static String getCurrentLanguageCode() {
        return getCurrentLocale().getLanguage();
    }

    /**
     * Returns the current ISO country/region code (e.g., {@code "US"}, {@code "VN"}).
     */
    public static String getCurrentCountryCode() {
        return getCurrentLocale().getCountry();
    }

    /**
     * Checks whether the current locale corresponds to the United States.
     */
    public static boolean isCurrentUserFromUnitedStates() {
        return "US".equalsIgnoreCase(getCurrentCountryCode());
    }
}
