package com.bbl.module_ads.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public final class CheckValidateKeystore {

    private CheckValidateKeystore() {
        // Utility class
    }

    public static boolean checkValidateKeystore(Context context, String expectedFingerprint) {
        return checkValidateKeystore(context, expectedFingerprint, true);
    }

    public static boolean checkValidateKeystore(
            Context context,
            String expectedFingerprint,
            boolean skipIfDebuggable
    ) {
        if (context == null) {
            return false;
        }

        if (skipIfDebuggable && isDebuggable(context)) {
            return true;
        }

        String normalizedExpected = normalizeFingerprint(expectedFingerprint);
        if (normalizedExpected == null || normalizedExpected.isEmpty()) {
            return false;
        }

        String actualFingerprint = getSha256Fingerprint(context);
        if (actualFingerprint == null) {
            return false;
        }

        actualFingerprint = normalizeFingerprint(actualFingerprint);
        return normalizedExpected.equals(actualFingerprint);
    }

    public static String getSha256Fingerprint(Context context) {
        if (context == null) {
            return null;
        }

        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();

        try {
            byte[] certificateBytes;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageInfo packageInfo = packageManager.getPackageInfo(
                        packageName,
                        PackageManager.GET_SIGNING_CERTIFICATES
                );

                SigningInfo signingInfo = packageInfo.signingInfo;
                if (signingInfo == null) {
                    return null;
                }

                Signature[] signatures = signingInfo.getApkContentsSigners();
                if (signatures == null || signatures.length == 0) {
                    return null;
                }

                certificateBytes = signatures[0].toByteArray();
            } else {
                @SuppressWarnings("deprecation")
                PackageInfo packageInfo = packageManager.getPackageInfo(
                        packageName,
                        PackageManager.GET_SIGNATURES
                );

                @SuppressWarnings("deprecation")
                Signature[] signatures = packageInfo.signatures;
                if (signatures == null || signatures.length == 0) {
                    return null;
                }

                certificateBytes = signatures[0].toByteArray();
            }

            if (certificateBytes == null) {
                return null;
            }

            return toSha256(certificateBytes);
        } catch (PackageManager.NameNotFoundException exception) {
            return null;
        }
    }

    private static String toSha256(byte[] source) {
        if (source == null) {
            return null;
        }

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(source);

            StringBuilder builder = new StringBuilder(digest.length * 3);
            for (int index = 0; index < digest.length; index++) {
                if (index > 0) {
                    builder.append(':');
                }
                builder.append(String.format(Locale.US, "%02X", digest[index]));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            return null;
        }
    }

    private static String normalizeFingerprint(String fingerprint) {
        if (fingerprint == null) {
            return null;
        }

        String normalized = fingerprint.replace(":", "").replace(" ", "");
        return normalized.toLowerCase(Locale.US);
    }

    private static boolean isDebuggable(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        return (applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }
}

