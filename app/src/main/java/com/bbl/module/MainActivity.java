package com.bbl.module;

import android.os.Bundle;
import android.os.Handler;
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
import com.bbl.module_ads.remote.ConfigManager;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdInspectorError;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnAdInspectorClosedListener;

public class MainActivity extends AppCompatActivity {
    private ApInterstitialAd mInterstitialAd;
    private Button btnLoad, btnShow, btnIap, btnLoadReward, btnShowReward;
    private FrameLayout frAds;

    private ShimmerFrameLayout shimmerAds;
    private ApNativeAd mApNativeAd;
    private ApRewardAd rewardedAds;
    private boolean isEarn = false;
    private ConfigManager configManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo Firebase Remote Config ngay khi vào app
        initializeFirebaseRemoteConfig();
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
            rewardedAds = BBLAd.getInstance().getRewardAd(this, BuildConfig.ad_reward);
        });

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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BBLAd.getInstance().loadNativeAd(MainActivity.this, "native_ob_1", new AdCallback() {

                    @Override
                    public void onNativeAdLoaded(@NonNull ApNativeAd nativeAd) {
                        super.onNativeAdLoaded(nativeAd);
                        BBLAd.getInstance().populateNativeAdView(MainActivity.this, nativeAd, frAds, new AdCallback());
                    }
                });
            }
        }, 5000); // Delay 5 giây để đảm bảo Remote Config đã được tải


//        MobileAds.openAdInspector(this, new OnAdInspectorClosedListener() {
//            public void onAdInspectorClosed(@Nullable AdInspectorError error) {
//
//            }
//        });
    }

    /**
     * Khởi tạo Firebase Remote Config ngay khi vào app
     */
    private void initializeFirebaseRemoteConfig() {
        Log.d("MainActivity", "=== INITIALIZING FIREBASE REMOTE CONFIG ===");

        configManager = ConfigManager.getInstance(this);

        // Khởi tạo và load config từ Firebase Remote Config
        boolean initSuccess = configManager.initialize();
        if (initSuccess) {
            Log.d("MainActivity", "✅ ConfigManager initialized successfully");
        } else {
            Log.e("MainActivity", "❌ ConfigManager initialization failed");
        }

    }

    /**
     * Test lấy config để verify Firebase Remote Config hoạt động
     */
    private void getNativeConfigByName() {
        Log.d("MainActivity", "=== TESTING CONFIG RETRIEVAL ===");

        // Kiểm tra trạng thái initialization trước
        if (configManager == null) {
            Log.e("MainActivity", "❌ ConfigManager is null!");
            return;
        }

        if (!configManager.isInitialized()) {
            Log.e("MainActivity", "❌ ConfigManager not initialized yet!");
            return;
        }

        Log.d("MainActivity", "✅ ConfigManager is initialized and ready");

        // Test lấy config theo ID
        com.bbl.module_ads.remote.NativeConfig config = configManager.getNativeConfig("native_ob_1");
        if (config != null) {
            Log.d("MainActivity", "✅ Retrieved config by ID 'native_ob_1':");
            Log.d("MainActivity", "   - Ad Unit ID: " + config.getIdAds());
            Log.d("MainActivity", "   - Layout: " + config.getLayout());
            Log.d("MainActivity", "   - Background: " + config.getBackgroundColor());
            Log.d("MainActivity", "   - CTA Color: " + config.getCtaColor());
            Log.d("MainActivity", "   - Is Show: " + config.isShow());
            Log.d("MainActivity", "   - Stroke: " + config.getStroke());
        } else {
            Log.e("MainActivity", "❌ Failed to retrieve config by ID 'native_ob_1'");

            // Debug: Log tất cả configs có sẵn
            Log.d("MainActivity", "Available config IDs: " + configManager.getAllConfigIds());
            Log.d("MainActivity", "Available groups: " + configManager.getAllGroupNames());
        }

        // Test lấy config từ group
        com.bbl.module_ads.remote.NativeConfig onboardingConfig = configManager.getOnboardingConfig(null);
        if (onboardingConfig != null) {
            Log.d("MainActivity", "✅ Retrieved random onboarding config:");
            Log.d("MainActivity", "   - Ad Unit ID: " + onboardingConfig.getIdAds());
            Log.d("MainActivity", "   - Layout: " + onboardingConfig.getLayout());
        } else {
            Log.e("MainActivity", "❌ Failed to retrieve onboarding config");
        }
    }


}
