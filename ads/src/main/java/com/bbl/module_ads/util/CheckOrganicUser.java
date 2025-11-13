package com.bbl.module_ads.util;

import android.app.Activity;
import android.app.Application;
import android.net.Uri;
import android.os.RemoteException;
import android.text.TextUtils;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class CheckOrganicUser {

    private CheckOrganicUser() {
        // Utility class
    }





    public static void checkOrganicUser(Application activity, Callback callback) {
        Objects.requireNonNull(activity, "activity == null");

        final Callback safeCallback = callback != null ? callback : Callback.EMPTY;
        final InstallReferrerClient client = InstallReferrerClient.newBuilder(activity).build();

        client.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                try {
                    if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                        handleSuccess(client, safeCallback);
                    } else if (responseCode == InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED) {
                        safeCallback.onError("InstallReferrer feature not supported");
                    } else if (responseCode == InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE) {
                        safeCallback.onError("InstallReferrer service unavailable");
                    } else {
                        safeCallback.onError("InstallReferrer unknown response: " + responseCode);
                    }
                } finally {
                    client.endConnection();
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                safeCallback.onServiceDisconnected();
            }
        });
    }

    private static void handleSuccess(InstallReferrerClient client, Callback callback) {
        try {
            ReferrerDetails details = client.getInstallReferrer();
            String referrerUrl = details != null ? details.getInstallReferrer() : null;
            Map<String, String> parameters = parseReferrer(referrerUrl);
            boolean isOrganic = determineOrganic(parameters, referrerUrl);
            callback.onResult(isOrganic, referrerUrl, parameters);
        } catch (RemoteException exception) {
            callback.onError("Failed to obtain install referrer: " + exception.getMessage());
        }
    }

    private static Map<String, String> parseReferrer(String referrerUrl) {
        if (TextUtils.isEmpty(referrerUrl)) {
            return Collections.emptyMap();
        }

        String[] pairs = referrerUrl.split("&");
        Map<String, String> result = new LinkedHashMap<>(pairs.length);

        for (String pair : pairs) {
            if (TextUtils.isEmpty(pair)) {
                continue;
            }

            int separatorIndex = pair.indexOf('=');
            if (separatorIndex <= 0 || separatorIndex == pair.length() - 1) {
                continue;
            }

            String key = Uri.decode(pair.substring(0, separatorIndex));
            String value = Uri.decode(pair.substring(separatorIndex + 1));

            if (!TextUtils.isEmpty(key)) {
                result.put(key, value);
            }
        }

        return Collections.unmodifiableMap(result);
    }

    private static boolean determineOrganic(Map<String, String> parameters, String referrerUrl) {
        if (parameters.isEmpty()) {
            return true;
        }

        if (parameters.containsKey("gclid") || parameters.containsKey("utm_id")) {
            return false;
        }

        String medium = normalize(parameters.get("utm_medium"));
        if (medium != null && !isOrganicValue(medium)) {
            return false;
        }

        String source = normalize(parameters.get("utm_source"));
        if (source != null && !isOrganicSource(source)) {
            return false;
        }

        if (!TextUtils.isEmpty(referrerUrl) && referrerUrl.toLowerCase(Locale.US).contains("click_id")) {
            return false;
        }

        return true;
    }

    private static String normalize(String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.US);
    }

    private static boolean isOrganicValue(String medium) {
        return "organic".equals(medium) || "(not set)".equals(medium) || "".equals(medium);
    }

    private static boolean isOrganicSource(String source) {
        return "google-play".equals(source) || "google play".equals(source) || isOrganicValue(source);
    }
    public static  void checkIsOrganicUserSimple(
            Application activity,
            OrganicUserCallback callback
    ) {

        final OrganicUserCallback safeCallback = callback != null ? callback : OrganicUserCallback.EMPTY;

        checkOrganicUser(activity, new Callback() {
            @Override
            public void onResult(boolean isOrganic, String referrerUrl, Map<String, String> parameters) {
                if (isOrganic) {
                    safeCallback.onOrganicUserResult(true, "Install referrer indicates organic: " + referrerUrl);
                } else {
                    safeCallback.onOrganicUserResult(false, "Install referrer indicates non-organic: " + referrerUrl);
                }
            }

            @Override
            public void onError(String message) {
                // On error, assume not organic for safety
                safeCallback.onOrganicUserResult(false, "Install referrer check error: " + message);
            }

            @Override
            public void onServiceDisconnected() {
                // On disconnect, assume not organic for safety
                safeCallback.onOrganicUserResult(false, "Install referrer service disconnected");
            }
        });
    }

    public static void checkIsOrganicUser(
            Activity activity,
            String expectedKeystoreSha256,
            OrganicUserCallback callback
    ) {
        Objects.requireNonNull(activity, "activity == null");

        final OrganicUserCallback safeCallback = callback != null ? callback : OrganicUserCallback.EMPTY;

        // If VARIANT_DEV is false, always return false (not organic)
        if (!AppUtil.VARIANT_DEV) {
            safeCallback.onOrganicUserResult(false, "VARIANT_DEV is false");
            return;
        }

        // Check keystore first
        boolean keystoreValid = CheckValidateKeystore.checkValidateKeystore(
                activity,
                expectedKeystoreSha256,
                true
        );

        // If keystore is invalid (different from expected), user is organic
        if (!keystoreValid) {
            safeCallback.onOrganicUserResult(true, "Keystore is different from expected");
            return;
        }

        // If keystore is valid, check install referrer

    }

    public interface OrganicUserCallback {
        OrganicUserCallback EMPTY = new OrganicUserCallback() {
            @Override
            public void onOrganicUserResult(boolean isOrganicUser, String reason) {

            }
        };

        void onOrganicUserResult(boolean isOrganicUser, String reason);
    }

    public interface Callback {
        Callback EMPTY = new Callback() {
            @Override
            public void onResult(boolean isOrganic, String referrerUrl, Map<String, String> parameters) {
                // no-op
            }
        };

        void onResult(boolean isOrganic, String referrerUrl, Map<String, String> parameters);

        default void onError(String message) {
            // no-op
        }

        default void onServiceDisconnected() {
            // no-op
        }
    }
}
