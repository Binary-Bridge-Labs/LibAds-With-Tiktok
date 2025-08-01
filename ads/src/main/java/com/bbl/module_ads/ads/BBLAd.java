package com.bbl.module_ads.ads;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEventFailure;
import com.adjust.sdk.AdjustEventSuccess;
import com.adjust.sdk.AdjustSessionFailure;
import com.adjust.sdk.AdjustSessionSuccess;
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnAttributionChangedListener;
import com.adjust.sdk.OnEventTrackingFailedListener;
import com.adjust.sdk.OnEventTrackingSucceededListener;
import com.adjust.sdk.OnSessionTrackingFailedListener;
import com.adjust.sdk.OnSessionTrackingSucceededListener;
import com.bbl.module_ads.admob.Admob;
import com.bbl.module_ads.admob.AppOpenManager;
import com.bbl.module_ads.ads.wrapper.ApInterstitialAd;
import com.bbl.module_ads.ads.wrapper.ApInterstitialPriorityAd;
import com.bbl.module_ads.ads.wrapper.ApNativeAd;
import com.bbl.module_ads.ads.wrapper.ApRewardAd;
import com.bbl.module_ads.ads.wrapper.ApRewardItem;
import com.bbl.module_ads.config.BBLAdConfig;
import com.bbl.module_ads.event.BBLAdjust;
import com.bbl.module_ads.funtion.AdCallback;
import com.bbl.module_ads.funtion.AdType;
import com.bbl.module_ads.funtion.RewardCallback;
import com.bbl.module_ads.util.AppUtil;
import com.bbl.module_ads.util.SharePreferenceUtils;
import com.facebook.FacebookSdk;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;

public class BBLAd {
    public static final String TAG_ADJUST = "BBLAdjust";
    public static final String TAG = "BBLAd";
    private static volatile BBLAd INSTANCE;
    private BBLAdConfig adConfig;
    private BBLInitCallback initCallback;
    private Boolean initAdSuccess = false;

    public static synchronized BBLAd getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BBLAd();
        }
        return INSTANCE;
    }

    public BBLAdConfig getAdConfig() {
        return adConfig;
    }

    public void setCountClickToShowAds(int countClickToShowAds) {
        Admob.getInstance().setNumToShowAds(countClickToShowAds);
    }

    public void setCountClickToShowAds(int countClickToShowAds, int currentClicked) {
        Admob.getInstance().setNumToShowAds(countClickToShowAds, currentClicked);
    }

    public void init(Application context, BBLAdConfig adConfig) {
        if (adConfig == null) {
            throw new RuntimeException("Cant not set BBLAdConfig null");
        }
        this.adConfig = adConfig;
        AppUtil.VARIANT_DEV = adConfig.isVariantDev();
        if (adConfig.isEnableAdjust()) {
            BBLAdjust.enableAdjust = true;
            setupAdjust(adConfig.isVariantDev(), adConfig.getAdjustConfig().getAdjustToken());
        }

        Admob.getInstance().init(context, adConfig.getListDeviceTest(), adConfig.getAdjustTokenTiktok());
        if (adConfig.isEnableAdResume()) {
            AppOpenManager.getInstance().init(adConfig.getApplication(), adConfig.getIdAdResume());
        }
        FacebookSdk.setClientToken(adConfig.getFacebookClientToken());
        FacebookSdk.sdkInitialize(context);
    }

    public void setInitCallback(BBLInitCallback initCallback) {
        this.initCallback = initCallback;
        if (initAdSuccess)
            initCallback.initAdSuccess();
    }

    private void setupAdjust(Boolean buildDebug, String adjustToken) {
        String environment = buildDebug ? AdjustConfig.ENVIRONMENT_SANDBOX : AdjustConfig.ENVIRONMENT_PRODUCTION;
        AdjustConfig config = new AdjustConfig(adConfig.getApplication(), adjustToken, environment);

        // Change the log level.
        config.setLogLevel(LogLevel.VERBOSE);
        config.setPreinstallTrackingEnabled(true);
        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                Log.d(TAG_ADJUST, "Attribution callback called!");
                Log.d(TAG_ADJUST, "Attribution: " + attribution.toString());
            }
        });

        // Set event success tracking delegate.
        config.setOnEventTrackingSucceededListener(new OnEventTrackingSucceededListener() {
            @Override
            public void onFinishedEventTrackingSucceeded(AdjustEventSuccess eventSuccessResponseData) {
                Log.d(TAG_ADJUST, "Event success callback called!");
                Log.d(TAG_ADJUST, "Event success data: " + eventSuccessResponseData.toString());
            }
        });
        // Set event failure tracking delegate.
        config.setOnEventTrackingFailedListener(new OnEventTrackingFailedListener() {
            @Override
            public void onFinishedEventTrackingFailed(AdjustEventFailure eventFailureResponseData) {
                Log.d(TAG_ADJUST, "Event failure callback called!");
                Log.d(TAG_ADJUST, "Event failure data: " + eventFailureResponseData.toString());
            }
        });

        // Set session success tracking delegate.
        config.setOnSessionTrackingSucceededListener(new OnSessionTrackingSucceededListener() {
            @Override
            public void onFinishedSessionTrackingSucceeded(AdjustSessionSuccess sessionSuccessResponseData) {
                Log.d(TAG_ADJUST, "Session success callback called!");
                Log.d(TAG_ADJUST, "Session success data: " + sessionSuccessResponseData.toString());
            }
        });

        // Set session failure tracking delegate.
        config.setOnSessionTrackingFailedListener(new OnSessionTrackingFailedListener() {
            @Override
            public void onFinishedSessionTrackingFailed(AdjustSessionFailure sessionFailureResponseData) {
                Log.d(TAG_ADJUST, "Session failure callback called!");
                Log.d(TAG_ADJUST, "Session failure data: " + sessionFailureResponseData.toString());
            }
        });


        config.setSendInBackground(true);
        Adjust.onCreate(config);
        adConfig.getApplication().registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
    }

    private static final class AdjustLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityResumed(Activity activity) {
            Adjust.onResume();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Adjust.onPause();
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }
    }

    public void loadBanner(Activity mActivity, String id) {
        Admob.getInstance().loadBanner(mActivity, id);
    }

    public void loadBanner(Activity mActivity, String id, AdCallback adCallback) {
        Admob.getInstance().loadBanner(mActivity, id, adCallback);
    }

    public void loadCollapsibleBanner(Activity activity, String id, String gravity, AdCallback adCallback) {
        Admob.getInstance().loadCollapsibleBanner(activity, id, gravity, adCallback);
    }

    public void loadCollapsibleBannerFragment(Activity activity, String id, View rootView, String gravity, AdCallback adCallback) {
        Admob.getInstance().loadCollapsibleBannerFragment(activity, id, rootView, gravity, adCallback);
    }

    public void loadCollapsibleBannerSizeMedium(Activity activity, String id, String gravity, AdSize sizeBanner, AdCallback adCallback) {
        Admob.getInstance().loadCollapsibleBannerSizeMedium(activity, id, gravity, sizeBanner, adCallback);
    }

    public void loadBannerFragment(Activity mActivity, String id, View rootView) {
        Admob.getInstance().loadBannerFragment(mActivity, id, rootView);
    }

    public void loadBannerFragment(Activity mActivity, String id, View rootView, AdCallback adCallback) {
        Admob.getInstance().loadBannerFragment(mActivity, id, rootView, adCallback);
    }

    public void loadInlineBanner(Activity mActivity, String idBanner, String inlineStyle) {
        Admob.getInstance().loadInlineBanner(mActivity, idBanner, inlineStyle);
    }

    public void loadInlineBanner(Activity mActivity, String idBanner, String inlineStyle, AdCallback adCallback) {
        Admob.getInstance().loadInlineBanner(mActivity, idBanner, inlineStyle, adCallback);
    }

    public void loadBannerInlineFragment(Activity mActivity, String idBanner, View rootView, String inlineStyle) {
        Admob.getInstance().loadInlineBannerFragment(mActivity, idBanner, rootView, inlineStyle);
    }

    public void loadBannerInlineFragment(Activity mActivity, String idBanner, View rootView, String inlineStyle, AdCallback adCallback) {
        Admob.getInstance().loadInlineBannerFragment(mActivity, idBanner, rootView, inlineStyle, adCallback);
    }

    public void loadSplashInterstitialAds(Context context, String id, long timeOut, long timeDelay, AdCallback adListener) {
        Admob.getInstance().loadSplashInterstitialAds(context, id, timeOut, timeDelay, true, adListener);
    }

    public void onCheckShowSplashWhenFail(AppCompatActivity activity, AdCallback callback, int timeDelay) {
        Admob.getInstance().onCheckShowSplashWhenFail(activity, callback, timeDelay);
    }

    public ApInterstitialAd getInterstitialAds(Context context, String id, AdCallback adListener) {
        ApInterstitialAd apInterstitialAd = new ApInterstitialAd();
        Admob.getInstance().getInterstitialAds(context, id, new AdCallback() {
            @Override
            public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                apInterstitialAd.setInterstitialAd(interstitialAd);
                adListener.onApInterstitialLoad(apInterstitialAd);
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                Log.d(TAG, "Admob onAdFailedToLoad");
                adListener.onAdFailedToLoad(i);
            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);
                Log.d(TAG, "Admob onAdFailedToShow");
                adListener.onAdFailedToShow(adError);
            }

            @Override
            public void onAdLogRev(AdValue adValue, String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                adListener.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
            }
        });
        return apInterstitialAd;
    }

    public void  forceShowInterstitial(@NonNull Context context, ApInterstitialAd mInterstitialAd,
                                       @NonNull final AdCallback callback, boolean shouldReloadAds) {
        if (System.currentTimeMillis() - SharePreferenceUtils.getLastImpressionInterstitialTime(context)
                < BBLAd.getInstance().adConfig.getIntervalInterstitialAd() * 1000L
        ) {
            callback.onNextAction();
            return;
        }
        if (mInterstitialAd == null || mInterstitialAd.isNotReady()) {
            callback.onNextAction();
            return;
        }
        AdCallback adCallback = new AdCallback() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                callback.onAdClosed();
                if (shouldReloadAds) {
                    Admob.getInstance().getInterstitialAds(context, mInterstitialAd.getInterstitialAd().getAdUnitId(), new AdCallback() {
                        @Override
                        public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                            super.onInterstitialLoad(interstitialAd);
                            mInterstitialAd.setInterstitialAd(interstitialAd);
                            callback.onInterstitialLoad(mInterstitialAd.getInterstitialAd());
                        }

                        @Override
                        public void onAdFailedToLoad(@Nullable LoadAdError i) {
                            super.onAdFailedToLoad(i);
                            mInterstitialAd.setInterstitialAd(null);
                            callback.onAdFailedToLoad(i);
                        }

                        @Override
                        public void onAdFailedToShow(@Nullable AdError adError) {
                            super.onAdFailedToShow(adError);
                            callback.onAdFailedToShow(adError);
                        }

                    });
                } else {
                    mInterstitialAd.setInterstitialAd(null);
                }
            }

            @Override
            public void onNextAction() {
                super.onNextAction();
                callback.onNextAction();
            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);
                callback.onAdFailedToShow(adError);
                if (shouldReloadAds) {
                    Admob.getInstance().getInterstitialAds(context, mInterstitialAd.getInterstitialAd().getAdUnitId(), new AdCallback() {
                        @Override
                        public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                            super.onInterstitialLoad(interstitialAd);
                            mInterstitialAd.setInterstitialAd(interstitialAd);
                            callback.onInterstitialLoad(mInterstitialAd.getInterstitialAd());
                        }

                        @Override
                        public void onAdFailedToLoad(@Nullable LoadAdError i) {
                            super.onAdFailedToLoad(i);
                            callback.onAdFailedToLoad(i);
                        }

                        @Override
                        public void onAdFailedToShow(@Nullable AdError adError) {
                            super.onAdFailedToShow(adError);
                            callback.onAdFailedToShow(adError);
                        }

                        @Override
                        public void onAdLogRev(AdValue adValue, String adUnitId, String mediationAdapterClassName, AdType adType) {
                            super.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                            callback.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                        }
                    });
                } else {
                    mInterstitialAd.setInterstitialAd(null);
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                callback.onAdClicked();
                BBLAdjust.pushTrackEventCLick(adValueAds);
            }

            @Override
            public void onAdClicked(String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdClicked(adUnitId, mediationAdapterClassName, adType);
                callback.onAdClicked(adUnitId, mediationAdapterClassName, adType);
            }

            AdValue adValueAds = null;

            @Override
            public void onAdLogRev(AdValue adValue, String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                callback.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                adValueAds = adValue;
            }

            @Override
            public void onInterstitialShow() {
                super.onInterstitialShow();
                callback.onInterstitialShow();
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                callback.onAdImpression();
            }
        };
        Admob.getInstance().forceShowInterstitial(context, mInterstitialAd.getInterstitialAd(), adCallback);
    }

    public void loadNativeAdResultCallback(final Activity activity, String id,
                                           int layoutCustomNative, AdCallback callback) {
        Admob.getInstance().loadNativeAd(((Context) activity), id, new AdCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                callback.onNativeAdLoaded(new ApNativeAd(layoutCustomNative, unifiedNativeAd));
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                callback.onAdFailedToLoad(i);
            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);
                callback.onAdFailedToShow(adError);
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                callback.onAdClicked();
                BBLAdjust.pushTrackEventCLick(adValueAds);
            }

            AdValue adValueAds = null;
            @Override
            public void onAdLogRev(AdValue adValue, String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                callback.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                adValueAds = adValue;
            }

            @Override
            public void onAdClicked(String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdClicked(adUnitId, mediationAdapterClassName, adType);
                callback.onAdClicked(adUnitId, mediationAdapterClassName, adType);

            }
        });
    }

    public void loadNativeAd(final Activity activity, String id,
                             int layoutCustomNative, FrameLayout adPlaceHolder, ShimmerFrameLayout
                                     containerShimmerLoading, AdCallback callback) {
        Admob.getInstance().loadNativeAd(((Context) activity), id, new AdCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                callback.onNativeAdLoaded(new ApNativeAd(layoutCustomNative, unifiedNativeAd));
                populateNativeAdView(activity, new ApNativeAd(layoutCustomNative, unifiedNativeAd), adPlaceHolder, containerShimmerLoading);
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                callback.onAdImpression();
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                callback.onAdFailedToLoad(i);
            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);
                callback.onAdFailedToShow(adError);
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                callback.onAdClicked();
                BBLAdjust.pushTrackEventCLick(adValueAds);
            }

            AdValue adValueAds = null;

            @Override
            public void onAdLogRev(AdValue adValue, String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                callback.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                adValueAds = adValue;
            }

            @Override
            public void onAdClicked(String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdClicked(adUnitId, mediationAdapterClassName, adType);
                callback.onAdClicked(adUnitId, mediationAdapterClassName, adType);
            }
        });
    }


    public void loadNativeAdResultCallback(final Activity activity, String idNormal, String idHighPriority1, String idHighPriority2, int layoutCustomNative, FrameLayout adPlaceHolder, ShimmerFrameLayout containerShimmerLoading, AdCallback callback) {
        loadNativeAdsWithHighTwoIdToShow(activity, idNormal, idHighPriority1, idHighPriority2, layoutCustomNative, adPlaceHolder, containerShimmerLoading, callback);
    }


    public void loadNativeAdsWithHighTwoIdToShow(final Activity activity, String idNormal, String idHighPriority1, String idHighPriority2, int layoutCustomNative, FrameLayout adPlaceHolder, ShimmerFrameLayout containerShimmerLoading, AdCallback callback) {

        final ApNativeAd[] nativeAdNormal = new ApNativeAd[1];
        final ApNativeAd[] nativeAdHigh = new ApNativeAd[1];

        Runnable checkAndCallback = () -> {
            ApNativeAd adToShow = nativeAdHigh[0] != null ? nativeAdHigh[0] : nativeAdNormal[0];
            if (adToShow != null) {
                callback.onNativeAdLoaded(adToShow);
                populateNativeAdView(activity, adToShow, adPlaceHolder, containerShimmerLoading);
            }
        };

        // Load Normal Ad
        Admob.getInstance().loadNativeAd(activity, idNormal, new AdCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                Log.d("Zimmy9x", "onUnifiedNativeAdLoaded: Normal");
                nativeAdNormal[0] = new ApNativeAd(layoutCustomNative, unifiedNativeAd);

            }


            @Override
            public void onAdFailedToShowAll(@Nullable AdError adError) {
                super.onAdFailedToShowAll(adError);
                callback.onAdFailedToShowAll(adError);
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError error) {
                callback.onAdFailedToLoad(error);
            }
        });

        // Load High Priority Ad
        Admob.getInstance().loadNativeAd(activity, idHighPriority1, new AdCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                Log.d("Zimmy9x", "onUnifiedNativeAdLoaded: High Priority Ad 1");
                nativeAdHigh[0] = new ApNativeAd(layoutCustomNative, unifiedNativeAd);
                checkAndCallback.run();
            }

            @Override
            public void onAdClickedHigh() {
                super.onAdClickedHigh();
                callback.onAdClickedAll();
            }

            @Override
            public void onAdFailedToShowHigh(@Nullable AdError adError) {
                super.onAdFailedToShowHigh(adError);
                callback.onAdFailedToShowHigh(adError);
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError error) {
                callback.onAdFailedToLoad(error);
                if (idHighPriority2 != null) {
                    Admob.getInstance().loadNativeAd(activity, idHighPriority2, new AdCallback() {
                        @Override
                        public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                            Log.d("Zimmy9x", "onUnifiedNativeAdLoaded: High Priority Ad 2");
                            nativeAdHigh[0] = new ApNativeAd(layoutCustomNative, unifiedNativeAd);
                            checkAndCallback.run();
                        }

                        @Override
                        public void onAdFailedToShowHigh(@Nullable AdError adError) {
                            super.onAdFailedToShowHigh(adError);
                            callback.onAdFailedToShowHigh(adError);
                        }

                        @Override
                        public void onAdClickedAll() {
                            super.onAdClickedAll();
                            callback.onAdClickedAll();
                        }

                        @Override
                        public void onAdFailedToLoad(@Nullable LoadAdError error) {
                            callback.onAdFailedToLoad(error);
                            checkAndCallback.run();
                        }
                    });
                } else {
                    checkAndCallback.run();
                }
            }
        });
    }


    public void loadNativeAdsWithHighTwoId(final Activity activity, String idNormal, String idHighPriority1, String idHighPriority2, int layoutCustomNative, FrameLayout adPlaceHolder, ShimmerFrameLayout containerShimmerLoading, AdCallback callback) {

        final ApNativeAd[] nativeAdNormal = new ApNativeAd[1];
        final ApNativeAd[] nativeAdHigh = new ApNativeAd[1];

        Runnable checkAndCallback = () -> {
            ApNativeAd adToShow = nativeAdHigh[0] != null ? nativeAdHigh[0] : nativeAdNormal[0];
            if (adToShow != null) {
                callback.onNativeAdLoaded(adToShow);
                populateNativeAdView(activity, adToShow, adPlaceHolder, containerShimmerLoading);
            }
        };

        // Load Normal Ad
        Admob.getInstance().loadNativeAd(activity, idNormal, new AdCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                nativeAdNormal[0] = new ApNativeAd(layoutCustomNative, unifiedNativeAd);
                checkAndCallback.run();
            }


            @Override
            public void onAdFailedToShowAll(@Nullable AdError adError) {
                super.onAdFailedToShowAll(adError);
                callback.onAdFailedToShowAll(adError);
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError error) {
                callback.onAdFailedToLoad(error);
            }
        });

        // Load High Priority Ad
        Admob.getInstance().loadNativeAd(activity, idHighPriority1, new AdCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                nativeAdHigh[0] = new ApNativeAd(layoutCustomNative, unifiedNativeAd);
                checkAndCallback.run();
            }

            @Override
            public void onAdClickedHigh() {
                super.onAdClickedHigh();
                callback.onAdClickedAll();
            }

            @Override
            public void onAdFailedToShowHigh(@Nullable AdError adError) {
                super.onAdFailedToShowHigh(adError);
                callback.onAdFailedToShowHigh(adError);
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError error) {
                callback.onAdFailedToLoad(error);
                if (idHighPriority2 != null) {
                    Admob.getInstance().loadNativeAd(activity, idHighPriority2, new AdCallback() {
                        @Override
                        public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                            nativeAdHigh[0] = new ApNativeAd(layoutCustomNative, unifiedNativeAd);
                            checkAndCallback.run();
                        }

                        @Override
                        public void onAdFailedToShowHigh(@Nullable AdError adError) {
                            super.onAdFailedToShowHigh(adError);
                            callback.onAdFailedToShowHigh(adError);
                        }

                        @Override
                        public void onAdClickedAll() {
                            super.onAdClickedAll();
                            callback.onAdClickedAll();
                        }

                        @Override
                        public void onAdFailedToLoad(@Nullable LoadAdError error) {
                            callback.onAdFailedToLoad(error);
                            checkAndCallback.run();
                        }
                    });
                } else {
                    checkAndCallback.run();
                }
            }
        });
    }


    public void populateNativeAdView(Activity activity, ApNativeAd apNativeAd, FrameLayout adPlaceHolder, ShimmerFrameLayout containerShimmerLoading) {
        if (apNativeAd.getAdmobNativeAd() == null && apNativeAd.getNativeView() == null) {
            containerShimmerLoading.setVisibility(View.GONE);
            return;
        }
        @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) LayoutInflater.from(activity).inflate(apNativeAd.getLayoutCustomNative(), null);
        containerShimmerLoading.stopShimmer();
        containerShimmerLoading.setVisibility(View.GONE);
        adPlaceHolder.setVisibility(View.VISIBLE);
        Admob.getInstance().populateUnifiedNativeAdView(apNativeAd.getAdmobNativeAd(), adView);
        adPlaceHolder.removeAllViews();
        adPlaceHolder.addView(adView);
    }



    public ApRewardAd getRewardAd(Activity activity, String id) {
        ApRewardAd apRewardAd = new ApRewardAd();
        Admob.getInstance().initRewardAds(activity, id, new AdCallback() {
            @Override
            public void onRewardAdLoaded(RewardedAd rewardedAd) {
                super.onRewardAdLoaded(rewardedAd);
                Log.i(TAG, "getRewardAd AdLoaded: ");
                apRewardAd.setAdmobReward(rewardedAd);
            }
        });

        return apRewardAd;
    }


    public void forceShowRewardAd(Activity activity, ApRewardAd apRewardAd, AdCallback
            callback) {
        if (!apRewardAd.isReady()) {
            Log.e(TAG, "forceShowRewardAd fail: reward ad not ready");
            callback.onNextAction();
            return;
        }


        Admob.getInstance().showRewardAds(activity, apRewardAd.getAdmobReward(), new RewardCallback() {

            @Override
            public void onUserEarnedReward(RewardItem var1) {
                callback.onUserEarnedReward(new ApRewardItem(var1));
            }

            @Override
            public void onRewardedAdClosed() {
                apRewardAd.clean();
                callback.onNextAction();
            }

            @Override
            public void onRewardedAdFailedToShow(int codeError) {
                apRewardAd.clean();
                callback.onAdFailedToShow(new AdError(codeError, "note msg", "Reward"));
            }

            @Override
            public void onAdClicked() {
                if (callback != null) {
                    callback.onAdClicked();
                }
                BBLAdjust.pushTrackEventCLick(adValueAds);
            }

            AdValue adValueAds = null;


            @Override
            public void onAdClicked(String adUnitId, String mediationAdapterClassName, AdType adType) {

            }

            @Override
            public void onAdImpression() {

            }

            @Override
            public void onAdLogRev(AdValue adValue, String adUnitId, String mediationAdapterClassName, AdType adType) {
               if (callback != null){
                   callback.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                   adValueAds = adValue;
               }
            }
        });



}

    public void initRewardAds(Context context, String id, RewardCallback callback) {
        Admob.getInstance().initRewardAds(context, id, callback);
    }

    public void initRewardAds(Context context, String id, AdCallback callback) {
        Admob.getInstance().initRewardAds(context, id, callback);
    }

    public void getRewardInterstitial(Context context, String id, AdCallback callback) {
        Admob.getInstance().getRewardInterstitial(context, id, callback);
    }

    public void showRewardInterstitial(Activity activity, RewardedInterstitialAd rewardedInterstitialAd, RewardCallback adCallback) {
        Admob.getInstance().showRewardInterstitial(activity, rewardedInterstitialAd, adCallback);
    }

    public void showRewardAds(Activity context, RewardCallback adCallback) {
        Admob.getInstance().showRewardAds(context, adCallback);
    }

    public void showRewardAds(Activity context, RewardedAd rewardedAd, RewardCallback adCallback) {
        Admob.getInstance().showRewardAds(context, rewardedAd, adCallback);
    }

    public void loadInterSplashPriority4SameTime(final Context context,
                                                 String idAdsHigh1,
                                                 String idAdsHigh2,
                                                 String idAdsHigh3,
                                                 String idAdsNormal,
                                                 long timeOut,
                                                 long timeDelay,
                                                 AdCallback adListener) {
        Admob.getInstance().loadInterSplashPriority4SameTime(context, idAdsHigh1, idAdsHigh2, idAdsHigh3, idAdsNormal, timeOut, timeDelay, adListener);
    }

    public void onShowSplashPriority4(AppCompatActivity activity, AdCallback adListener) {
        Admob.getInstance().onShowSplashPriority4(activity, adListener);
    }

    public void onCheckShowSplashPriority4WhenFail(AppCompatActivity activity, AdCallback callback, int timeDelay) {
        Admob.getInstance().onCheckShowSplashPriority4WhenFail(activity, callback, timeDelay);
    }

    private boolean isFinishLoadNativeAdHigh1 = false;
    private boolean isFinishLoadNativeAdHigh2 = false;
    private boolean isFinishLoadNativeAdHigh3 = false;
    private boolean isFinishLoadNativeAdNormal = false;

    private ApNativeAd apNativeAdHigh2;
    private ApNativeAd apNativeAdHigh3;
    private ApNativeAd apNativeAdNormal;

    public void loadNative4SameTime(final Activity activity, String idAdHigh1, String idAdHigh2, String idAdHigh3, String idAdNormal, int layoutCustomNative, AdCallback adCallback) {
        isFinishLoadNativeAdHigh1 = false;
        isFinishLoadNativeAdHigh2 = false;
        isFinishLoadNativeAdHigh3 = false;

        apNativeAdHigh2 = null;
        apNativeAdHigh3 = null;
        apNativeAdNormal = null;

        loadNativeAdResultCallback(activity, idAdHigh1, layoutCustomNative, new AdCallback() {
            @Override
            public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                adCallback.onNativeAdLoaded(nativeAd);
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                adCallback.onAdClicked();
            }

            @Override
            public void onAdLogRev(AdValue adValue, String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                adCallback.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
            }

            @Override
            public void onAdClicked(String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdClicked(adUnitId, mediationAdapterClassName, adType);
                adCallback.onAdClicked(adUnitId, mediationAdapterClassName, adType);
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                if (isFinishLoadNativeAdHigh2 && apNativeAdHigh2 != null) {
                    adCallback.onNativeAdLoaded(apNativeAdHigh2);
                } else if (isFinishLoadNativeAdHigh3 && apNativeAdHigh3 != null) {
                    adCallback.onNativeAdLoaded(apNativeAdHigh3);
                } else if (isFinishLoadNativeAdNormal && apNativeAdNormal != null) {
                    adCallback.onNativeAdLoaded(apNativeAdNormal);
                } else {
                    // waiting for ads loaded
                    isFinishLoadNativeAdHigh1 = true;
                }
            }
        });

        loadNativeAdResultCallback(activity, idAdHigh2, layoutCustomNative, new AdCallback() {
            @Override
            public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                if (isFinishLoadNativeAdHigh1) {
                    adCallback.onNativeAdLoaded(nativeAd);
                } else {
                    isFinishLoadNativeAdHigh2 = true;
                    apNativeAdHigh2 = nativeAd;
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                adCallback.onAdClicked();
            }

            @Override
            public void onAdLogRev(AdValue adValue, String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                adCallback.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
            }

            @Override
            public void onAdClicked(String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdClicked(adUnitId, mediationAdapterClassName, adType);
                adCallback.onAdClicked(adUnitId, mediationAdapterClassName, adType);
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                if (isFinishLoadNativeAdHigh1) {
                    if (isFinishLoadNativeAdHigh3 && apNativeAdHigh3 != null) {
                        adCallback.onNativeAdLoaded(apNativeAdHigh3);
                    } else if (isFinishLoadNativeAdNormal && apNativeAdNormal != null) {
                        adCallback.onNativeAdLoaded(apNativeAdNormal);
                    } else {
                        isFinishLoadNativeAdHigh2 = true;
                    }
                } else {
                    isFinishLoadNativeAdHigh2 = true;
                    apNativeAdHigh2 = null;
                }
            }
        });

        loadNativeAdResultCallback(activity, idAdHigh3, layoutCustomNative, new AdCallback() {
            @Override
            public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                if (isFinishLoadNativeAdHigh1 && isFinishLoadNativeAdHigh2) {
                    adCallback.onNativeAdLoaded(nativeAd);
                } else {
                    isFinishLoadNativeAdHigh3 = true;
                    apNativeAdHigh3 = nativeAd;
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                adCallback.onAdClicked();
            }


            @Override
            public void onAdLogRev(AdValue adValue, String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                adCallback.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
            }

            @Override
            public void onAdClicked(String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdClicked(adUnitId, mediationAdapterClassName, adType);
                adCallback.onAdClicked(adUnitId, mediationAdapterClassName, adType);
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                if (isFinishLoadNativeAdHigh1 && isFinishLoadNativeAdHigh2) {
                    if (isFinishLoadNativeAdNormal && apNativeAdNormal != null) {
                        adCallback.onNativeAdLoaded(apNativeAdNormal);
                    } else {
                        isFinishLoadNativeAdHigh3 = true;
                    }
                } else {
                    isFinishLoadNativeAdHigh3 = true;
                    apNativeAdHigh3 = null;
                }
            }
        });

        loadNativeAdResultCallback(activity, idAdNormal, layoutCustomNative, new AdCallback() {
            @Override
            public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                if (isFinishLoadNativeAdHigh1 && isFinishLoadNativeAdHigh2 && isFinishLoadNativeAdHigh3) {
                    adCallback.onNativeAdLoaded(nativeAd);
                } else {
                    isFinishLoadNativeAdNormal = true;
                    apNativeAdNormal = nativeAd;
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                adCallback.onAdClicked();
            }

            @Override
            public void onAdLogRev(AdValue adValue, String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                adCallback.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
            }

            @Override
            public void onAdClicked(String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdClicked(adUnitId, mediationAdapterClassName, adType);
                adCallback.onAdClicked(adUnitId, mediationAdapterClassName, adType);
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                if (isFinishLoadNativeAdHigh1 && isFinishLoadNativeAdHigh2 && isFinishLoadNativeAdHigh3) {
                    adCallback.onNativeAdLoaded(apNativeAdNormal);
                } else {
                    isFinishLoadNativeAdNormal = true;
                    apNativeAdNormal = null;
                }
            }
        });
    }

    public void loadPriorityInterstitialAds(Context context, ApInterstitialPriorityAd apInterstitialPriorityAd, AdCallback adCallback) {
        loadPriorityInterstitialAdsFromAdmob(context, apInterstitialPriorityAd, adCallback);
    }

    public void loadPriorityInterstitialAdsFromAdmob(Context context,
                                                     ApInterstitialPriorityAd apInterstitialPriorityAd,
                                                     AdCallback adCallback) {
        if (!apInterstitialPriorityAd.getHigh1PriorityId().isEmpty()
                && !apInterstitialPriorityAd.getHigh1PriorityInterstitialAd().isReady()
        ) {
            loadAdsInterHigh1Priority(context, apInterstitialPriorityAd, adCallback);
        }

        if (!apInterstitialPriorityAd.getHigh2PriorityId().isEmpty()
                && !apInterstitialPriorityAd.getHigh2PriorityInterstitialAd().isReady()
        ) {
            loadAdsInterHigh2Priority(context, apInterstitialPriorityAd, adCallback);
        }

        if (!apInterstitialPriorityAd.getHigh3PriorityId().isEmpty()
                && !apInterstitialPriorityAd.getHigh3PriorityInterstitialAd().isReady()
        ) {
            loadAdsInterHigh3Priority(context, apInterstitialPriorityAd, adCallback);
        }

        if (!apInterstitialPriorityAd.getNormalPriorityId().isEmpty()
                && !apInterstitialPriorityAd.getNormalPriorityInterstitialAd().isReady()
        ) {
            loadInterNormalPriority(context, apInterstitialPriorityAd, adCallback);
        }
    }

    private void loadAdsInterHigh1Priority(Context context, ApInterstitialPriorityAd apInterstitialPriorityAd, AdCallback adCallback) {
        Admob.getInstance().getInterstitialAds(context, apInterstitialPriorityAd.getHigh1PriorityId(), new AdCallback() {
            @Override
            public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                Log.d(TAG, "onInterstitialLoad idAdsNormalPriority");
                apInterstitialPriorityAd.getHigh1PriorityInterstitialAd().setInterstitialAd(interstitialAd);
                adCallback.onApInterstitialLoad(apInterstitialPriorityAd.getHigh1PriorityInterstitialAd());
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                Log.e(TAG, "onAdFailedToLoad: idAdsNormalPriority: " + i);
                adCallback.onAdFailedToLoad(i);
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                adCallback.onAdClicked();
                BBLAdjust.pushTrackEventCLick(adValueAds);
            }

            @Override
            public void onAdClicked(String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdClicked(adUnitId, mediationAdapterClassName, adType);
                adCallback.onAdClicked(adUnitId, mediationAdapterClassName, adType);
            }

            AdValue adValueAds = null;
            @Override
            public void onAdLogRev(AdValue adValue, String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                adCallback.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                adValueAds = adValue;

            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                adCallback.onAdImpression();
            }
        });
    }

    private void loadAdsInterHigh2Priority(Context context, ApInterstitialPriorityAd apInterstitialPriorityAd, AdCallback adCallback) {
        Admob.getInstance().getInterstitialAds(context, apInterstitialPriorityAd.getHigh2PriorityId(), new AdCallback() {
            @Override
            public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                Log.d(TAG, "onInterstitialLoad idAdsNormalPriority");
                apInterstitialPriorityAd.getHigh2PriorityInterstitialAd().setInterstitialAd(interstitialAd);
                adCallback.onApInterstitialLoad(apInterstitialPriorityAd.getHigh2PriorityInterstitialAd());
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                Log.e(TAG, "onAdFailedToLoad: idAdsNormalPriority: " + i);
                adCallback.onAdFailedToLoad(i);
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                adCallback.onAdClicked();
            }

            @Override
            public void onAdClicked(String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdClicked(adUnitId, mediationAdapterClassName, adType);
                adCallback.onAdClicked(adUnitId, mediationAdapterClassName, adType);
            }

            @Override
            public void onAdLogRev(AdValue adValue, String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                adCallback.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                adCallback.onAdImpression();
            }
        });
    }

    private void loadAdsInterHigh3Priority(Context context, ApInterstitialPriorityAd apInterstitialPriorityAd, AdCallback adCallback) {
        Admob.getInstance().getInterstitialAds(context, apInterstitialPriorityAd.getHigh3PriorityId(), new AdCallback() {
            @Override
            public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                Log.d(TAG, "onInterstitialLoad idAdsNormalPriority");
                apInterstitialPriorityAd.getHigh3PriorityInterstitialAd().setInterstitialAd(interstitialAd);
                adCallback.onApInterstitialLoad(apInterstitialPriorityAd.getHigh3PriorityInterstitialAd());
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                Log.e(TAG, "onAdFailedToLoad: idAdsNormalPriority: " + i);
                adCallback.onAdFailedToLoad(i);
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                adCallback.onAdClicked();
            }

            @Override
            public void onAdClicked(String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdClicked(adUnitId, mediationAdapterClassName, adType);
                adCallback.onAdClicked(adUnitId, mediationAdapterClassName, adType);
            }

            @Override
            public void onAdLogRev(AdValue adValue, String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                adCallback.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                adCallback.onAdImpression();
            }
        });
    }

    private void loadInterNormalPriority(Context context, ApInterstitialPriorityAd apInterstitialPriorityAd, AdCallback adCallback) {
        Admob.getInstance().getInterstitialAds(context, apInterstitialPriorityAd.getNormalPriorityId(), new AdCallback() {
            @Override
            public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                Log.d(TAG, "onInterstitialLoad idAdsNormalPriority");
                apInterstitialPriorityAd.getNormalPriorityInterstitialAd().setInterstitialAd(interstitialAd);
                adCallback.onApInterstitialLoad(apInterstitialPriorityAd.getNormalPriorityInterstitialAd());
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                Log.e(TAG, "onAdFailedToLoad: idAdsNormalPriority: " + i);
                adCallback.onAdFailedToLoad(i);
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                adCallback.onAdClicked();
            }

            @Override
            public void onAdClicked(String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdClicked(adUnitId, mediationAdapterClassName, adType);
                adCallback.onAdClicked(adUnitId, mediationAdapterClassName, adType);
            }

            @Override
            public void onAdLogRev(AdValue adValue, String adUnitId, String mediationAdapterClassName, AdType adType) {
                super.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                adCallback.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                adCallback.onAdImpression();
            }
        });
    }

    public void forceShowInterstitialPriority(Context context, ApInterstitialPriorityAd apInterstitialPriorityAd, AdCallback adCallback, boolean isReloadAds) {
        ApInterstitialAd interstitialAd;
        if (apInterstitialPriorityAd.getHigh1PriorityInterstitialAd() != null
                && apInterstitialPriorityAd.getHigh1PriorityInterstitialAd().isReady()
        ) {
            interstitialAd = apInterstitialPriorityAd.getHigh1PriorityInterstitialAd();
        } else if (apInterstitialPriorityAd.getHigh2PriorityInterstitialAd() != null
                && apInterstitialPriorityAd.getHigh2PriorityInterstitialAd().isReady()
        ) {
            interstitialAd = apInterstitialPriorityAd.getHigh2PriorityInterstitialAd();
        } else if (apInterstitialPriorityAd.getHigh3PriorityInterstitialAd() != null
                && apInterstitialPriorityAd.getHigh3PriorityInterstitialAd().isReady()
        ) {
            interstitialAd = apInterstitialPriorityAd.getHigh3PriorityInterstitialAd();
        } else if (apInterstitialPriorityAd.getNormalPriorityInterstitialAd() != null
                && apInterstitialPriorityAd.getNormalPriorityInterstitialAd().isReady()
        ) {
            interstitialAd = apInterstitialPriorityAd.getNormalPriorityInterstitialAd();
        } else {
            adCallback.onNextAction();
            if (isReloadAds) {
                loadPriorityInterstitialAds(context, apInterstitialPriorityAd, new AdCallback());
            }
            return;
        }
        forceShowInterstitial(context,
                interstitialAd,
                new AdCallback() {
                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        adCallback.onNextAction();
                    }

                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        interstitialAd.setInterstitialAd(null);
                        adCallback.onAdClosed();
                        if (isReloadAds) {
                            loadPriorityInterstitialAds(context, apInterstitialPriorityAd, new AdCallback());
                        }
                    }

                    @Override
                    public void onInterstitialShow() {
                        super.onInterstitialShow();
                        adCallback.onInterstitialShow();
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        adCallback.onAdClicked();
                    }

                    @Override
                    public void onAdClicked(String adUnitId, String mediationAdapterClassName, AdType adType) {
                        super.onAdClicked(adUnitId, mediationAdapterClassName, adType);
                        adCallback.onAdClicked(adUnitId, mediationAdapterClassName, adType);
                    }

                    @Override
                    public void onAdLogRev(AdValue adValue, String adUnitId, String mediationAdapterClassName, AdType adType) {
                        super.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                        adCallback.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                    }

                    @Override
                    public void onAdFailedToShow(@Nullable AdError adError) {
                        super.onAdFailedToShow(adError);
                        adCallback.onAdFailedToShow(adError);
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                        adCallback.onAdImpression();
                    }
                },
                false
        );
    }
}

