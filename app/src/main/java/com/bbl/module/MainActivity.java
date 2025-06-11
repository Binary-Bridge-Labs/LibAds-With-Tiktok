package com.bbl.module;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bbl.module_ads.ads.BBLAd;
import com.bbl.module_ads.ads.wrapper.ApInterstitialAd;
import com.bbl.module_ads.ads.wrapper.ApNativeAd;
import com.bbl.module_ads.ads.wrapper.ApRewardAd;
import com.bbl.module_ads.ads.wrapper.ApRewardItem;
import com.bbl.module_ads.billing.AppPurchase;
import com.bbl.module_ads.funtion.AdCallback;
import com.bbl.module_ads.funtion.AdType;
import com.bbl.module_ads.funtion.PurchaseListener;
import com.bbl.module_ads.funtion.RewardCallback;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.bbl.module.BuildConfig;
import com.bbl.module.R;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;

public class MainActivity extends AppCompatActivity {
    private ApInterstitialAd mInterstitialAd;
    private Button btnLoad, btnShow, btnIap, btnLoadReward, btnShowReward;
    private FrameLayout frAds;
    private ShimmerFrameLayout shimmerAds;
    private ApNativeAd mApNativeAd;
    private ApRewardAd rewardedAds;
    private boolean isEarn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnLoad = findViewById(R.id.btnLoad);
        btnShow = findViewById(R.id.btnShow);
        btnIap = findViewById(R.id.btnIap);
        btnLoadReward = findViewById(R.id.btnLoadReward);
        btnShowReward = findViewById(R.id.btnShowReward);
        frAds = findViewById(R.id.fr_ads);
        shimmerAds = findViewById(R.id.shimmer_native);


        // Interstitial Ads
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BBLAd.getInstance().getInterstitialAds(MainActivity.this, BuildConfig.ad_interstitial_splash, new AdCallback() {
                    @Override
                    public void onApInterstitialLoad(@Nullable ApInterstitialAd apInterstitialAd) {
                        super.onApInterstitialLoad(apInterstitialAd);
                        mInterstitialAd = apInterstitialAd;
                        Toast.makeText(MainActivity.this, "Ads Ready", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdLogRev(AdValue adValue, String adUnitId, String mediationAdapterClassName, AdType adType) {
                        super.onAdLogRev(adValue, adUnitId, mediationAdapterClassName, adType);
                        Log.d("LuanDev", "onAdLogRev: 1M: " + adValue + " " + adUnitId + " " + mediationAdapterClassName + " " + adType);
                    }
                });
            }
        });
        btnShow.setOnClickListener(v -> BBLAd.getInstance().forceShowInterstitial(MainActivity.this, mInterstitialAd, new AdCallback() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.d("LuanDev", "onAdClicked: 1M");
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                Log.d("LuanDev", "onAdImpression: 1M");
            }
        }, true));

        // Banner Ads
        BBLAd.getInstance().loadBanner(this, BuildConfig.ad_banner);
        /*BBLAd.getInstance().loadCollapsibleBanner(this, BuildConfig.ad_banner, AppConstant.CollapsibleGravity.BOTTOM, new AdCallback());*/

        // Native Ads: Load And Show
        BBLAd.getInstance().loadNativeAd(this, BuildConfig.ad_native, R.layout.native_large, frAds, shimmerAds, new AdCallback() {
            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                frAds.removeAllViews();
            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);
                frAds.removeAllViews();
            }
        });

        // Native Ads: Load
        BBLAd.getInstance().loadNativeAdResultCallback(this, BuildConfig.ad_native, R.layout.native_large, new AdCallback() {
            @Override
            public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);

                mApNativeAd = nativeAd;
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);

                mApNativeAd = null;
            }

            @Override
            public void onAdFailedToShow(@Nullable AdError adError) {
                super.onAdFailedToShow(adError);

                mApNativeAd = null;
            }
        });

        // Native Ads: Show
        if (mApNativeAd != null) {
            BBLAd.getInstance().populateNativeAdView(this, mApNativeAd, frAds, shimmerAds);
        }

        // In-App Purchase
        AppPurchase.getInstance().setPurchaseListener(new PurchaseListener() {
            @Override
            public void onProductPurchased(String productId, String transactionDetails) {
                Toast.makeText(MainActivity.this, "onProductPurchased", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void displayErrorMessage(String errorMsg) {
                Toast.makeText(MainActivity.this, "displayErrorMessage", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUserCancelBilling() {
                Toast.makeText(MainActivity.this, "onUserCancelBilling", Toast.LENGTH_SHORT).show();
            }
        });

        btnIap.setOnClickListener(v -> AppPurchase.getInstance().purchase(MainActivity.this, "android.test.purchased"));

        // Reward Ads
        btnLoadReward.setOnClickListener(v -> {
            rewardedAds =         BBLAd.getInstance().getRewardAd(this, BuildConfig.ad_reward);});

        btnShowReward.setOnClickListener(v -> {
            isEarn = false;
            BBLAd.getInstance().forceShowRewardAd(MainActivity.this, rewardedAds, new AdCallback() {
                @Override
                public void onUserEarnedReward(@NonNull ApRewardItem rewardItem) {
                    super.onUserEarnedReward(rewardItem);
                }

                @Override
                public void onAdClicked() {

                }

                @Override
                public void onAdClicked(String adUnitId, String mediationAdapterClassName, AdType adType) {

                }

                @Override
                public void onAdImpression() {

                }

                @Override
                public void onAdLogRev(AdValue adValue, String adUnitId, String mediationAdapterClassName, AdType adType) {

                }
            });
        });
    }
}
