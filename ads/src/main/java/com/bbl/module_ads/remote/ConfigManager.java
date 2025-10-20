package com.bbl.module_ads.remote;

import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * ConfigManager - Helper class để quản lý NativeConfig một cách dễ dàng
 * Cung cấp các phương thức tiện ích để truy xuất config
 */
public class ConfigManager {
    
    private static final String TAG = "ConfigManager";
    private static ConfigManager instance;
    private RemoteConfig remoteConfig;
    
    // Private constructor để implement Singleton pattern
    private ConfigManager(Context context) {
        this.remoteConfig = RemoteConfig.getInstance(context);
    }
    
    /**
     * Lấy instance của ConfigManager (Singleton)
     * @param context Application context
     * @return ConfigManager instance
     */
    public static synchronized ConfigManager getInstance(Context context) {
        if (instance == null) {
            instance = new ConfigManager(context);
        }
        return instance;
    }
    
    /**
     * Khởi tạo ConfigManager và load configs
     * @return true nếu khởi tạo thành công, false nếu có lỗi
     */
    public boolean initialize() {
        return remoteConfig.initialize();
    }

    public String getStringValue(String key) {
        return remoteConfig.getFirebaseRemoteConfig().getString(key);
    }


    public boolean getBooleanValue(String key) {
        return remoteConfig.getFirebaseRemoteConfig().getBoolean(key);
    }

    /**
     * Lấy NativeConfig theo ID
     * @param configId ID của config
     * @return NativeConfig object hoặc null nếu không tìm thấy
     */
    public NativeConfig getNativeConfig(String configId) {
        return remoteConfig.getNativeConfig(configId);
    }
    
    /**
     * Lấy tất cả configs trong group
     * @param groupName Tên group
     * @return List<NativeConfig> hoặc empty list
     */
    public List<NativeConfig> getConfigsByGroup(String groupName) {
        return remoteConfig.getNativeConfigsByGroup(groupName);
    }
    
    /**
     * Lấy config đầu tiên trong group
     * @param groupName Tên group
     * @return NativeConfig đầu tiên hoặc null
     */
    public NativeConfig getFirstConfigInGroup(String groupName) {
        return remoteConfig.getFirstConfigInGroup(groupName);
    }
    
    /**
     * Lấy config ngẫu nhiên trong group
     * @param groupName Tên group
     * @return NativeConfig ngẫu nhiên hoặc null
     */
    public NativeConfig getRandomConfigInGroup(String groupName) {
        return remoteConfig.getRandomConfigInGroup(groupName);
    }
    
    // ========== CÁC PHƯƠNG THỨC TIỆN ÍCH CHO TỪNG GROUP ==========
    
    /**
     * Lấy config cho onboarding screen
     * @param configId ID cụ thể (native_ob_1, native_ob_2, etc.) hoặc null để lấy random
     * @return NativeConfig cho onboarding
     */
    public NativeConfig getOnboardingConfig(String configId) {
        if (configId != null && !configId.isEmpty()) {
            return getNativeConfig(configId);
        }
        return getRandomConfigInGroup("onboarding_group");
    }
    
    /**
     * Lấy config cho language screen
     * @param configId ID cụ thể (language_1, language_2, etc.) hoặc null để lấy random
     * @return NativeConfig cho language
     */
    public NativeConfig getLanguageConfig(String configId) {
        if (configId != null && !configId.isEmpty()) {
            return getNativeConfig(configId);
        }
        return getRandomConfigInGroup("language_group");
    }
    
    /**
     * Lấy config cho permission screen
     * @param configId ID cụ thể (permission_1, permission_2, etc.) hoặc null để lấy random
     * @return NativeConfig cho permission
     */
    public NativeConfig getPermissionConfig(String configId) {
        if (configId != null && !configId.isEmpty()) {
            return getNativeConfig(configId);
        }
        return getRandomConfigInGroup("permission_group");
    }
    
    /**
     * Lấy config cho home screen
     * @param configId ID cụ thể (home_1, home_2, etc.) hoặc null để lấy random
     * @return NativeConfig cho home
     */
    public NativeConfig getHomeConfig(String configId) {
        if (configId != null && !configId.isEmpty()) {
            return getNativeConfig(configId);
        }
        return getRandomConfigInGroup("home_group");
    }
    
    /**
     * Lấy config cho exit screen
     * @param configId ID cụ thể (exit_1, exit_3, etc.) hoặc null để lấy random
     * @return NativeConfig cho exit
     */
    public NativeConfig getExitConfig(String configId) {
        if (configId != null && !configId.isEmpty()) {
            return getNativeConfig(configId);
        }
        return getRandomConfigInGroup("exit_group");
    }
    
    /**
     * Lấy config cho collapsible ads
     * @param configId ID cụ thể (collap_1, collap_2, etc.) hoặc null để lấy random
     * @return NativeConfig cho collapsible
     */
    public NativeConfig getCollapsibleConfig(String configId) {
        if (configId != null && !configId.isEmpty()) {
            return getNativeConfig(configId);
        }
        return getRandomConfigInGroup("collapsible_group");
    }
    
    // ========== CÁC PHƯƠNG THỨC KIỂM TRA ==========
    
    /**
     * Kiểm tra xem config có tồn tại không
     * @param configId ID của config
     * @return true nếu config tồn tại
     */
    public boolean hasConfig(String configId) {
        return remoteConfig.hasConfig(configId);
    }
    
    /**
     * Kiểm tra xem group có tồn tại không
     * @param groupName Tên group
     * @return true nếu group tồn tại
     */
    public boolean hasGroup(String groupName) {
        return remoteConfig.hasGroup(groupName);
    }
    
    /**
     * Kiểm tra trạng thái initialization
     * @return true nếu đã được khởi tạo
     */
    public boolean isInitialized() {
        return remoteConfig.isInitialized();
    }
    
    /**
     * Reload configs từ Firebase Remote Config
     */
    public void reload() {
        remoteConfig.reload();
    }
    
    /**
     * Force refresh config từ Firebase Remote Config
     * @param callback Callback để nhận kết quả
     */
    public void forceRefresh(RemoteConfig.RefreshCallback callback) {
        remoteConfig.forceRefresh(callback);
    }
    
    /**
     * Force refresh config từ Firebase Remote Config (không callback)
     */
    public void forceRefresh() {
        remoteConfig.forceRefresh(null);
    }
    
    // ========== CÁC PHƯƠNG THỨC DEBUG/INFO ==========
    
    /**
     * Lấy tất cả config IDs
     * @return List<String> chứa tất cả config IDs
     */
    public List<String> getAllConfigIds() {
        return remoteConfig.getAllConfigIds();
    }
    
    /**
     * Lấy tất cả group names
     * @return List<String> chứa tất cả group names
     */
    public List<String> getAllGroupNames() {
        return remoteConfig.getAllGroupNames();
    }
    
    /**
     * Log tất cả configs để debug
     */
    public void logAllConfigs() {
        if (!isInitialized()) {
            Log.w(TAG, "ConfigManager not initialized");
            return;
        }
        
        Log.d(TAG, "=== ALL CONFIGS ===");
        for (String groupName : getAllGroupNames()) {
            Log.d(TAG, "Group: " + groupName);
            List<NativeConfig> configs = getConfigsByGroup(groupName);
            for (NativeConfig config : configs) {
                Log.d(TAG, "  - " + config.toString());
            }
        }
    }
}
