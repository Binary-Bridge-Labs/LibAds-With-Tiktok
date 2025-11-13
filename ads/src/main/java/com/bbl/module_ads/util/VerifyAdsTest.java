package com.bbl.module_ads.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kotlin.text.StringsKt;

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
public class VerifyAdsTest {

    private VerifyAdsTest() {
        // Utility class
    }

    public static List<String> getTestAdKeywords() {
        return new ArrayList<>(Arrays.asList(
                "Anunciodeprueba",
                "Annoncetest",
                "테스트광고",
                "TestAd",
                "Annuncioditesto",
                "Testanzeige",
                "TesIklan",
                "Quảngcáothửnghiệm",
                "Anúnciodeteste",
                "পরীক্ষামূলকবিজ্ঞাপন",
                "जाँचविज्ञापन",
                "إعلانتجريبي",
                "Тестовоеобъявление"
        ));
    }

    public static boolean isTestAd(String adTitle) {
        if (adTitle == null) {
            return false;
        }
        String normalized = StringsKt.replace(adTitle, " ", "", false);
        String[] parts = normalized.split(":");
        String candidate = parts.length > 0 ? parts[0] : normalized;
        for (String keyword : getTestAdKeywords()) {
            if (keyword.equalsIgnoreCase(candidate)) {
                return true;
            }
        }
        return false;
    }
}
