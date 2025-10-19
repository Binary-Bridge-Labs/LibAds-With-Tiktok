package com.bbl.module_ads.remote;

import android.content.Context;
import android.util.Log;

import com.bbl.module_ads.R;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RemoteConfig - Quản lý và truy xuất các NativeConfig từ Firebase Remote Config
 * Cung cấp các phương thức để lấy config theo ID hoặc group
 */
public class RemoteConfig {
    
    private static final String TAG = "RemoteConfig";
    private static final String FIREBASE_CONFIG_KEY = "native_ads_config";
    private static RemoteConfig instance;
    private Context context;
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private Map<String, NativeConfig> configMap;
    private Map<String, List<NativeConfig>> groupConfigMap;
    private boolean isInitialized = false;
    private boolean isFirebaseInitialized = false;
    
    // Private constructor để implement Singleton pattern
    private RemoteConfig(Context context) {
        this.context = context.getApplicationContext();
        this.configMap = new HashMap<>();
        this.groupConfigMap = new HashMap<>();
        initializeFirebaseRemoteConfig();
    }
    
    /**
     * Lấy instance của RemoteConfig (Singleton)
     * @param context Application context
     * @return RemoteConfig instance
     */
    public static synchronized RemoteConfig getInstance(Context context) {
        if (instance == null) {
            instance = new RemoteConfig(context);
        }
        return instance;
    }
    
    /**
     * Khởi tạo Firebase Remote Config
     */
    private void initializeFirebaseRemoteConfig() {
        try {
            firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            
            // Cấu hình Firebase Remote Config settings
            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(3600) // 1 hour cache
                    .build();
            
            firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
            
            // Set default values từ local JSON file (fallback)
            String defaultJson = loadJsonFromRaw();
            if (defaultJson != null && !defaultJson.isEmpty()) {
                Map<String, Object> defaults = new HashMap<>();
                defaults.put(FIREBASE_CONFIG_KEY, defaultJson);
                firebaseRemoteConfig.setDefaultsAsync(defaults);
            }
            
            isFirebaseInitialized = true;
            Log.d(TAG, "Firebase Remote Config initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase Remote Config", e);
            isFirebaseInitialized = false;
        }
    }
    
    /**
     * Khởi tạo và load config từ Firebase Remote Config
     * @return true nếu load thành công, false nếu có lỗi
     */
    public boolean initialize() {
        if (isInitialized) {
            return true;
        }
        
        if (!isFirebaseInitialized) {
            Log.e(TAG, "Firebase Remote Config not initialized");
            return false;
        }
        
        try {
            Log.d(TAG, "Starting Firebase Remote Config initialization...");
            
            // Load config hiện tại trước (có thể là cached hoặc default)
            loadConfigFromFirebase();
            
            // Fetch config mới từ Firebase (async)
            firebaseRemoteConfig.fetchAndActivate()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Firebase Remote Config fetch successful");
                            loadConfigFromFirebase();
                        } else {
                            Log.e(TAG, "Firebase Remote Config fetch failed", task.getException());
                            // Fallback to local config
                            loadConfigFromLocal();
                        }
                    });
            
            isInitialized = true;
            Log.d(TAG, "RemoteConfig initialized successfully with " + configMap.size() + " configs");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing RemoteConfig", e);
            // Fallback to local config
            loadConfigFromLocal();
            return isInitialized;
        }
    }
    
    /**
     * Load config từ Firebase Remote Config
     */
    private void loadConfigFromFirebase() {
        try {
            Log.d(TAG, "Loading config from Firebase Remote Config...");
            String jsonString = firebaseRemoteConfig.getString(FIREBASE_CONFIG_KEY);
            Log.d(TAG, "Firebase config string length: " + (jsonString != null ? jsonString.length() : "null"));
            
            if (jsonString != null && !jsonString.isEmpty()) {
                Log.d(TAG, "Firebase config string preview: " + jsonString.substring(0, Math.min(100, jsonString.length())) + "...");
                parseJsonConfig(jsonString);
                Log.d(TAG, "✅ Config loaded from Firebase Remote Config successfully");
            } else {
                Log.w(TAG, "⚠️ Firebase Remote Config returned empty string, using local fallback");
                loadConfigFromLocal();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading config from Firebase", e);
            loadConfigFromLocal();
        }
    }
    
    /**
     * Load config từ local JSON file (fallback)
     */
    private void loadConfigFromLocal() {
        try {
            Log.d(TAG, "Loading config from local JSON file (fallback)...");
            String jsonString = loadJsonFromRaw();
            if (jsonString != null && !jsonString.isEmpty()) {
                Log.d(TAG, "Local JSON string length: " + jsonString.length());
                parseJsonConfig(jsonString);
                Log.d(TAG, "✅ Config loaded from local JSON file (fallback) successfully");
            } else {
                Log.e(TAG, "❌ Failed to load JSON from local resources");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading config from local", e);
        }
    }
    
    /**
     * Load JSON string từ raw resources
     * @return JSON string hoặc null nếu có lỗi
     */
    private String loadJsonFromRaw() {
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.native_ads_config);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            return new String(buffer, "UTF-8");
        } catch (IOException e) {
            Log.e(TAG, "Error reading JSON from raw resources", e);
            return null;
        }
    }
    
    /**
     * Parse JSON config và tạo các NativeConfig objects
     * @param jsonString JSON string từ file
     */
    private void parseJsonConfig(String jsonString) {
        try {
            Log.d(TAG, "Parsing JSON config...");
            Gson gson = new Gson();
            JsonObject rootObject = JsonParser.parseString(jsonString).getAsJsonObject();
            JsonObject nativeAdsConfig = rootObject.getAsJsonObject("native_ads_config");
            
            if (nativeAdsConfig == null) {
                Log.e(TAG, "❌ native_ads_config not found in JSON");
                return;
            }
            
            Log.d(TAG, "Found native_ads_config, parsing groups...");
            int totalConfigs = 0;
            
            // Parse từng group
            for (String groupName : nativeAdsConfig.keySet()) {
                JsonObject groupObject = nativeAdsConfig.getAsJsonObject(groupName);
                List<NativeConfig> groupConfigs = new ArrayList<>();
                
                Log.d(TAG, "Parsing group: " + groupName);
                
                // Parse từng config trong group
                for (String configId : groupObject.keySet()) {
                    JsonObject configObject = groupObject.getAsJsonObject(configId);
                    
                    NativeConfig config = gson.fromJson(configObject, NativeConfig.class);
                    
                    // Lưu vào map với key là configId
                    configMap.put(configId, config);
                    
                    // Thêm vào group list
                    groupConfigs.add(config);
                    
                    totalConfigs++;
                    Log.d(TAG, "  ✅ Loaded config: " + configId + " -> " + config.toString());
                }
                
                // Lưu group configs
                groupConfigMap.put(groupName, groupConfigs);
                Log.d(TAG, "Group " + groupName + " has " + groupConfigs.size() + " configs");
            }
            
            Log.d(TAG, "✅ JSON parsing completed! Total configs: " + totalConfigs);
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing JSON config", e);
        }
    }
    
    /**
     * Lấy NativeConfig theo ID
     * @param configId ID của config (ví dụ: "native_ob_1", "language_1", etc.)
     * @return NativeConfig object hoặc null nếu không tìm thấy
     */
    public NativeConfig getNativeConfig(String configId) {
        if (!isInitialized) {
            Log.w(TAG, "RemoteConfig not initialized. Call initialize() first.");
            return null;
        }
        
        NativeConfig config = configMap.get(configId);
        if (config == null) {
            Log.w(TAG, "Config not found for ID: " + configId);
        }
        
        return config;
    }
    
    /**
     * Lấy tất cả configs trong một group
     * @param groupName Tên group (ví dụ: "onboarding_group", "language_group", etc.)
     * @return List<NativeConfig> hoặc empty list nếu không tìm thấy
     */
    public List<NativeConfig> getNativeConfigsByGroup(String groupName) {
        if (!isInitialized) {
            Log.w(TAG, "RemoteConfig not initialized. Call initialize() first.");
            return new ArrayList<>();
        }
        
        List<NativeConfig> configs = groupConfigMap.get(groupName);
        if (configs == null) {
            Log.w(TAG, "Group not found: " + groupName);
            return new ArrayList<>();
        }
        
        return new ArrayList<>(configs);
    }
    
    /**
     * Lấy config đầu tiên trong group (có thể dùng làm default)
     * @param groupName Tên group
     * @return NativeConfig đầu tiên hoặc null nếu group trống
     */
    public NativeConfig getFirstConfigInGroup(String groupName) {
        List<NativeConfig> configs = getNativeConfigsByGroup(groupName);
        return configs.isEmpty() ? null : configs.get(0);
    }
    
    /**
     * Lấy config ngẫu nhiên trong group
     * @param groupName Tên group
     * @return NativeConfig ngẫu nhiên hoặc null nếu group trống
     */
    public NativeConfig getRandomConfigInGroup(String groupName) {
        List<NativeConfig> configs = getNativeConfigsByGroup(groupName);
        if (configs.isEmpty()) {
            return null;
        }
        
        int randomIndex = (int) (Math.random() * configs.size());
        return configs.get(randomIndex);
    }
    
    /**
     * Lấy tất cả config IDs có sẵn
     * @return List<String> chứa tất cả config IDs
     */
    public List<String> getAllConfigIds() {
        if (!isInitialized) {
            Log.w(TAG, "RemoteConfig not initialized. Call initialize() first.");
            return new ArrayList<>();
        }
        
        return new ArrayList<>(configMap.keySet());
    }
    
    /**
     * Lấy tất cả group names có sẵn
     * @return List<String> chứa tất cả group names
     */
    public List<String> getAllGroupNames() {
        if (!isInitialized) {
            Log.w(TAG, "RemoteConfig not initialized. Call initialize() first.");
            return new ArrayList<>();
        }
        
        return new ArrayList<>(groupConfigMap.keySet());
    }
    
    /**
     * Kiểm tra xem config có tồn tại không
     * @param configId ID của config
     * @return true nếu config tồn tại, false nếu không
     */
    public boolean hasConfig(String configId) {
        return isInitialized && configMap.containsKey(configId);
    }
    
    /**
     * Kiểm tra xem group có tồn tại không
     * @param groupName Tên group
     * @return true nếu group tồn tại, false nếu không
     */
    public boolean hasGroup(String groupName) {
        return isInitialized && groupConfigMap.containsKey(groupName);
    }
    
    /**
     * Reset và reload config từ Firebase (có thể dùng khi cần refresh)
     */
    public void reload() {
        configMap.clear();
        groupConfigMap.clear();
        isInitialized = false;
        initialize();
    }
    
    /**
     * Force fetch config mới từ Firebase Remote Config
     * @param callback Callback để nhận kết quả
     */
    public void forceRefresh(RefreshCallback callback) {
        if (!isFirebaseInitialized) {
            Log.e(TAG, "Firebase Remote Config not initialized");
            if (callback != null) {
                callback.onRefreshFailed("Firebase Remote Config not initialized");
            }
            return;
        }
        
        firebaseRemoteConfig.fetch(0) // Force fetch (ignore cache)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firebaseRemoteConfig.activate()
                                .addOnCompleteListener(activateTask -> {
                                    if (activateTask.isSuccessful()) {
                                        loadConfigFromFirebase();
                                        Log.d(TAG, "Config refreshed from Firebase successfully");
                                        if (callback != null) {
                                            callback.onRefreshSuccess();
                                        }
                                    } else {
                                        Log.e(TAG, "Failed to activate Firebase Remote Config", activateTask.getException());
                                        if (callback != null) {
                                            callback.onRefreshFailed("Failed to activate config");
                                        }
                                    }
                                });
                    } else {
                        Log.e(TAG, "Failed to fetch Firebase Remote Config", task.getException());
                        if (callback != null) {
                            callback.onRefreshFailed("Failed to fetch config");
                        }
                    }
                });
    }
    
    /**
     * Interface callback cho refresh operation
     */
    public interface RefreshCallback {
        void onRefreshSuccess();
        void onRefreshFailed(String error);
    }
    
    /**
     * Kiểm tra trạng thái initialization
     * @return true nếu đã được khởi tạo, false nếu chưa
     */
    public boolean isInitialized() {
        return isInitialized;
    }
}
