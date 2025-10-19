package com.bbl.module_ads.remote;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * JSONConfigLoader - Class để đọc và parse config từ file JSON
 * Hỗ trợ load native ads config từ JSON file
 */
public class JSONConfigLoader {
    
    /**
     * Load RemoteConfig từ JSON string
     * @param jsonString JSON string chứa config
     * @return RemoteConfig object được load từ JSON
     */
    public static RemoteConfig loadFromJSONString(String jsonString) {
        RemoteConfig remoteConfig = new RemoteConfig();
        
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject nativeAdsConfig = jsonObject.getJSONObject("native_ads_config");
            
            // Load các group config
            Iterator<String> groupKeys = nativeAdsConfig.keys();
            while (groupKeys.hasNext()) {
                String groupName = groupKeys.next();
                JSONObject groupObject = nativeAdsConfig.getJSONObject(groupName);
                
                GroupNativeConfig groupConfig = new GroupNativeConfig(groupName);
                
                // Load các config trong group
                Iterator<String> configKeys = groupObject.keys();
                while (configKeys.hasNext()) {
                    String configName = configKeys.next();
                    JSONObject configObject = groupObject.getJSONObject(configName);
                    
                    NativeConfig nativeConfig = parseNativeConfig(configObject);
                    groupConfig.addNativeConfig(configName, nativeConfig);
                }
                
                remoteConfig.addGroupNativeConfig(groupName, groupConfig);
            }
            
            // Load layout mappings nếu có
            if (jsonObject.has("layout_mappings")) {
                JSONObject layoutMappings = jsonObject.getJSONObject("layout_mappings");
                loadLayoutMappings(layoutMappings);
            }
            
        } catch (JSONException e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
        }
        
        return remoteConfig;
    }
    
    /**
     * Load RemoteConfig từ InputStream
     * @param inputStream InputStream chứa JSON data
     * @return RemoteConfig object được load từ JSON
     */
    public static RemoteConfig loadFromInputStream(InputStream inputStream) {
        StringBuilder jsonString = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error reading input stream: " + e.getMessage());
            e.printStackTrace();
            return new RemoteConfig();
        }
        
        return loadFromJSONString(jsonString.toString());
    }
    
    /**
     * Parse NativeConfig từ JSONObject
     * @param configObject JSONObject chứa config data
     * @return NativeConfig object
     */
    private static NativeConfig parseNativeConfig(JSONObject configObject) throws JSONException {
        String idAds = configObject.optString("idAds", "");
        String layout = configObject.optString("layout", "");
        String backgroundColor = configObject.optString("backgroundColor", "#FFFFFF");
        String ctaColor = configObject.optString("ctaColor", "#000000");
        boolean isShow = configObject.optBoolean("isShow", true);
        String stroke = configObject.optString("stroke", "#CCCCCC");
        
        return new NativeConfig(idAds, layout, backgroundColor, ctaColor, isShow, stroke);
    }
    
    /**
     * Load layout mappings vào LayoutMapper
     * @param layoutMappings JSONObject chứa layout mappings
     */
    private static void loadLayoutMappings(JSONObject layoutMappings) {
        try {
            Iterator<String> keys = layoutMappings.keys();
            while (keys.hasNext()) {
                String layoutId = keys.next();
                String layoutResource = layoutMappings.getString(layoutId);
                
                // Parse R.layout.xxx thành resource ID (giả sử)
                int resourceId = parseLayoutResource(layoutResource);
                LayoutMapper.addLayoutMapping(layoutId, resourceId);
            }
        } catch (JSONException e) {
            System.err.println("Error loading layout mappings: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Parse layout resource string thành resource ID
     * Đây là method giả lập, trong thực tế sẽ cần logic phức tạp hơn
     * @param layoutResource String như "R.layout.ads_native_banner_layout"
     * @return Resource ID (giả lập)
     */
    private static int parseLayoutResource(String layoutResource) {
        // Giả lập resource ID dựa trên tên layout
        // Trong thực tế, bạn sẽ cần sử dụng Android Resource system
        if (layoutResource.contains("banner_layout")) return 1001;
        if (layoutResource.contains("banner_layout_2")) return 1002;
        if (layoutResource.contains("medium_layout")) return 1003;
        if (layoutResource.contains("interstitial_layout")) return 1004;
        if (layoutResource.contains("video_layout")) return 1005;
        if (layoutResource.contains("fullscreen1_layout")) return 1006;
        if (layoutResource.contains("fullscreen2_layout")) return 1007;
        if (layoutResource.contains("unify_small_layout")) return 1008;
        if (layoutResource.contains("unify_medium_layout")) return 1009;
        if (layoutResource.contains("unify_large_layout")) return 1010;
        return 0;
    }
    
    /**
     * Lấy metadata từ JSON
     * @param jsonString JSON string
     * @return Map chứa metadata
     */
    public static Map<String, Object> getMetadata(String jsonString) {
        Map<String, Object> metadata = new HashMap<>();
        
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            if (jsonObject.has("metadata")) {
                JSONObject metadataObject = jsonObject.getJSONObject("metadata");
                Iterator<String> keys = metadataObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = metadataObject.get(key);
                    metadata.put(key, value);
                }
            }
        } catch (JSONException e) {
            System.err.println("Error parsing metadata: " + e.getMessage());
            e.printStackTrace();
        }
        
        return metadata;
    }
    
    /**
     * Validate JSON config structure
     * @param jsonString JSON string cần validate
     * @return true nếu valid, false nếu không
     */
    public static boolean validateConfig(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            
            // Kiểm tra có native_ads_config không
            if (!jsonObject.has("native_ads_config")) {
                System.err.println("Missing 'native_ads_config' section");
                return false;
            }
            
            JSONObject nativeAdsConfig = jsonObject.getJSONObject("native_ads_config");
            Iterator<String> groupKeys = nativeAdsConfig.keys();
            
            while (groupKeys.hasNext()) {
                String groupName = groupKeys.next();
                JSONObject groupObject = nativeAdsConfig.getJSONObject(groupName);
                Iterator<String> configKeys = groupObject.keys();
                
                while (configKeys.hasNext()) {
                    String configName = configKeys.next();
                    JSONObject configObject = groupObject.getJSONObject(configName);
                    
                    // Kiểm tra các field bắt buộc
                    if (!configObject.has("idAds") || !configObject.has("layout")) {
                        System.err.println("Missing required fields in config: " + groupName + "/" + configName);
                        return false;
                    }
                }
            }
            
            return true;
            
        } catch (JSONException e) {
            System.err.println("Invalid JSON structure: " + e.getMessage());
            return false;
        }
    }
}
