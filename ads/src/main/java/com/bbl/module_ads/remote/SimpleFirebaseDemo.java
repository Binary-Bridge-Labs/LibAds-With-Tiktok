package com.bbl.module_ads.remote;

import android.content.Context;
import android.util.Log;

/**
 * Demo đơn giản để test Firebase Remote Config integration
 */
public class SimpleFirebaseDemo {
    
    private static final String TAG = "SimpleFirebaseDemo";
    
    /**
     * Test cơ bản Firebase Remote Config
     * @param context Application context
     */
    public static void testFirebaseRemoteConfig(Context context) {
        Log.d(TAG, "=== TEST FIREBASE REMOTE CONFIG ===");
        
        try {
            // Khởi tạo ConfigManager
            ConfigManager configManager = ConfigManager.getInstance(context);
            
            Log.d(TAG, "ConfigManager created, calling initialize()...");
            
            // Khởi tạo và load config
            boolean initialized = configManager.initialize();
            Log.d(TAG, "ConfigManager initialized: " + initialized);
            
            if (initialized) {
                // Test lấy config theo ID
                NativeConfig config = configManager.getNativeConfig("native_ob_1");
                if (config != null) {
                    Log.d(TAG, "✅ SUCCESS: Lấy được config từ Firebase:");
                    Log.d(TAG, "   - ID Ads: " + config.getIdAds());
                    Log.d(TAG, "   - Layout: " + config.getLayout());
                    Log.d(TAG, "   - Background Color: " + config.getBackgroundColor());
                    Log.d(TAG, "   - CTA Color: " + config.getCtaColor());
                    Log.d(TAG, "   - Is Show: " + config.isShow());
                    Log.d(TAG, "   - Stroke: " + config.getStroke());
                } else {
                    Log.e(TAG, "❌ FAILED: Không lấy được config");
                }
                
                // Test lấy config theo group
                java.util.List<NativeConfig> onboardingConfigs = configManager.getConfigsByGroup("onboarding_group");
                Log.d(TAG, "Onboarding group configs count: " + onboardingConfigs.size());
                
                // Test lấy config ngẫu nhiên
                NativeConfig randomConfig = configManager.getRandomConfigInGroup("onboarding_group");
                if (randomConfig != null) {
                    Log.d(TAG, "Random config: " + randomConfig.getIdAds());
                }
                
            } else {
                Log.e(TAG, "❌ FAILED: ConfigManager initialization failed");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ ERROR: " + e.getMessage(), e);
        }
    }
    
    /**
     * Test đơn giản chỉ để gọi initialize()
     * @param context Application context
     */
    public static void testInitializeOnly(Context context) {
        Log.d(TAG, "=== TEST INITIALIZE ONLY ===");
        
        ConfigManager configManager = ConfigManager.getInstance(context);
        Log.d(TAG, "Calling initialize()...");
        
        boolean result = configManager.initialize();
        Log.d(TAG, "Initialize result: " + result);
    }
    
    /**
     * Test force refresh
     * @param context Application context
     */
    public static void testForceRefresh(Context context) {
        Log.d(TAG, "=== TEST FORCE REFRESH ===");
        
        ConfigManager configManager = ConfigManager.getInstance(context);
        
        configManager.forceRefresh(new RemoteConfig.RefreshCallback() {
            @Override
            public void onRefreshSuccess() {
                Log.d(TAG, "✅ Force refresh SUCCESS");
                
                // Test lấy config sau refresh
                NativeConfig config = configManager.getNativeConfig("native_ob_1");
                if (config != null) {
                    Log.d(TAG, "Config after refresh: " + config.getIdAds());
                }
            }
            
            @Override
            public void onRefreshFailed(String error) {
                Log.e(TAG, "❌ Force refresh FAILED: " + error);
            }
        });
    }
}
