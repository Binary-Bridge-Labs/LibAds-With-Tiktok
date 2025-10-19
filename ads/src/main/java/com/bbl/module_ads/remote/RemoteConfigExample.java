package com.bbl.module_ads.remote;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Ví dụ sử dụng RemoteConfig với NativeConfig từ JSON
 * Minh họa cách load config từ file JSON cho native ads
 */
public class RemoteConfigExample {
    
    public static void main(String[] args) {
        System.out.println("=== BBL LibAds - Native Config JSON Demo ===\n");
        
        // Load config từ JSON string (giả lập)
        String jsonConfig = getJSONConfigString();
        
        // Validate JSON trước khi load
        if (!JSONConfigLoader.validateConfig(jsonConfig)) {
            System.err.println("❌ JSON config không hợp lệ!");
            return;
        }
        
        System.out.println("✅ JSON config hợp lệ, đang load...");
        
        // Load RemoteConfig từ JSON
        RemoteConfig remoteConfig = JSONConfigLoader.loadFromJSONString(jsonConfig);
        
        // Lấy metadata
        Map<String, Object> metadata = JSONConfigLoader.getMetadata(jsonConfig);
        System.out.println("\n📋 Metadata:");
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        
        // === DEMO CÁC METHOD MỚI ===
        System.out.println("\n=== Demo tìm kiếm theo Ad Unit ID ===");
        
        // 1. Tìm NativeConfig theo ad_unit_id
        String testAdUnitId = "ca-app-pub-3940256099942544/2247696110";
        NativeConfig foundConfig = remoteConfig.getNativeConfigByAdUnitId(testAdUnitId);
        if (foundConfig != null) {
            System.out.println("✓ Tìm thấy config cho ad_unit_id: " + testAdUnitId);
            System.out.println("  Layout: " + foundConfig.getLayout());
            System.out.println("  Background: " + foundConfig.getBackgroundColor());
            System.out.println("  CTA Color: " + foundConfig.getCtaColor());
            System.out.println("  Show: " + foundConfig.isShow());
        } else {
            System.out.println("✗ Không tìm thấy config cho ad_unit_id: " + testAdUnitId);
        }
        
        // 2. Lấy tất cả config có cùng ad_unit_id
        List<NativeConfig> allConfigsWithSameId = remoteConfig.getAllNativeConfigsByAdUnitId(testAdUnitId);
        System.out.println("\nSố lượng config có cùng ad_unit_id '" + testAdUnitId + "': " + allConfigsWithSameId.size());
        for (int i = 0; i < allConfigsWithSameId.size(); i++) {
            NativeConfig config = allConfigsWithSameId.get(i);
            System.out.println("  Config " + (i + 1) + ": " + config.getLayout() + " - " + config.getBackgroundColor());
        }
        
        // 3. Lấy thông tin chi tiết về config (bao gồm group và config name)
        Map<String, Object> configInfo = remoteConfig.getNativeConfigInfoByAdUnitId(testAdUnitId);
        if (!configInfo.isEmpty()) {
            System.out.println("\nThông tin chi tiết config:");
            System.out.println("  Group: " + configInfo.get("groupName"));
            System.out.println("  Config Name: " + configInfo.get("configName"));
            NativeConfig config = (NativeConfig) configInfo.get("nativeConfig");
            System.out.println("  Layout: " + config.getLayout());
        }
        
        // 4. Kiểm tra tồn tại và đếm
        boolean hasConfig = remoteConfig.hasNativeConfigWithAdUnitId(testAdUnitId);
        int configCount = remoteConfig.countNativeConfigsByAdUnitId(testAdUnitId);
        System.out.println("\nKiểm tra tồn tại: " + hasConfig);
        System.out.println("Số lượng config: " + configCount);
        
        // 5. Test với ad_unit_id không tồn tại
        String nonExistentId = "ca-app-pub-9999999999999999/9999999999";
        NativeConfig nonExistentConfig = remoteConfig.getNativeConfigByAdUnitId(nonExistentId);
        System.out.println("\nTest với ad_unit_id không tồn tại: " + (nonExistentConfig == null ? "✓ Không tìm thấy" : "✗ Tìm thấy"));
        
        // === DEMO SỬ DỤNG CONFIG TỪ JSON ===
        System.out.println("\n=== Demo sử dụng Config từ JSON ===");
        
        // Demo hiển thị ads với config từ JSON
        demoShowAdsFromJSON(remoteConfig);
        
        // In thông tin tổng quan
        System.out.println("\n=== Thông tin RemoteConfig ===");
        System.out.println("Số lượng group: " + remoteConfig.getGroupCount());
        System.out.println("Danh sách group: " + String.join(", ", remoteConfig.getAllGroupNames()));
        
        // In thông tin LayoutMapper
        System.out.println("\n=== Thông tin LayoutMapper ===");
        System.out.println("Số lượng layout mapping: " + LayoutMapper.getMappingCount());
        System.out.println("Danh sách layout ID: " + String.join(", ", LayoutMapper.getAllLayoutIds()));
        
        // In thông tin từng group
        for (String groupName : remoteConfig.getAllGroupNames()) {
            GroupNativeConfig group = remoteConfig.getGroupNativeConfig(groupName);
            System.out.println("\nGroup: " + groupName);
            System.out.println("Số lượng config: " + group.getConfigCount());
            System.out.println("Danh sách config: " + String.join(", ", group.getAllConfigNames()));
            
            // In chi tiết từng config trong group
            for (String configName : group.getAllConfigNames()) {
                NativeConfig config = group.getNativeConfig(configName);
                int layoutResource = LayoutMapper.getLayoutResource(config.getLayout());
                System.out.println("  - " + configName + ": layout=" + config.getLayout() + 
                                 " (resource=" + layoutResource + "), show=" + config.isShow());
            }
        }
    }
    
    /**
     * Demo hiển thị ads với config từ JSON
     */
    private static void demoShowAdsFromJSON(RemoteConfig remoteConfig) {
        System.out.println("\n🎯 Demo hiển thị ads với config từ JSON:");
        
        // Lấy tất cả config có isShow = true
        for (String groupName : remoteConfig.getAllGroupNames()) {
            GroupNativeConfig group = remoteConfig.getGroupNativeConfig(groupName);
            
            for (String configName : group.getAllConfigNames()) {
                NativeConfig config = group.getNativeConfig(configName);
                
                if (config.isShow()) {
                    System.out.println("\n📱 Hiển thị " + groupName + "/" + configName + ":");
                    System.out.println("  Ad Unit ID: " + config.getIdAds());
                    System.out.println("  Layout: " + config.getLayout());
                    
                    int layoutResource = LayoutMapper.getLayoutResource(config.getLayout());
                    System.out.println("  Layout Resource: " + layoutResource);
                    
                    System.out.println("  Background: " + config.getBackgroundColor());
                    System.out.println("  CTA Color: " + config.getCtaColor());
                    System.out.println("  Stroke: " + config.getStroke());
                    
                    // Giả lập hiển thị ads
                    System.out.println("  ✅ Ads đã được hiển thị với config này!");
                } else {
                    System.out.println("\n⏸️  Bỏ qua " + groupName + "/" + configName + " (isShow = false)");
                }
            }
        }
    }
    
    /**
     * Lấy JSON config string (giả lập đọc từ file)
     * Trong thực tế sẽ đọc từ res/raw/native_ads_config.json
     */
    private static String getJSONConfigString() {
        return "{\n" +
                "  \"native_ads_config\": {\n" +
                "    \"banner_group\": {\n" +
                "      \"banner_small\": {\n" +
                "        \"idAds\": \"ca-app-pub-3940256099942544/2247696110\",\n" +
                "        \"layout\": \"ads_native_banner_layout\",\n" +
                "        \"backgroundColor\": \"#FFFFFF\",\n" +
                "        \"ctaColor\": \"#FF5722\",\n" +
                "        \"isShow\": true,\n" +
                "        \"stroke\": \"#E0E0E0\"\n" +
                "      },\n" +
                "      \"banner_large\": {\n" +
                "        \"idAds\": \"ca-app-pub-3940256099942544/2247696111\",\n" +
                "        \"layout\": \"ads_native_banner_layout_2\",\n" +
                "        \"backgroundColor\": \"#F5F5F5\",\n" +
                "        \"ctaColor\": \"#2196F3\",\n" +
                "        \"isShow\": true,\n" +
                "        \"stroke\": \"#CCCCCC\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"interstitial_group\": {\n" +
                "      \"interstitial_fullscreen\": {\n" +
                "        \"idAds\": \"ca-app-pub-3940256099942544/1033173712\",\n" +
                "        \"layout\": \"ads_native_interstitial_layout\",\n" +
                "        \"backgroundColor\": \"#000000\",\n" +
                "        \"ctaColor\": \"#FFC107\",\n" +
                "        \"isShow\": true,\n" +
                "        \"stroke\": \"#333333\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"fullscreen_group\": {\n" +
                "      \"fullscreen_1\": {\n" +
                "        \"idAds\": \"ca-app-pub-3940256099942544/1043173714\",\n" +
                "        \"layout\": \"ads_native_fullscreen1_layout\",\n" +
                "        \"backgroundColor\": \"#2C3E50\",\n" +
                "        \"ctaColor\": \"#E74C3C\",\n" +
                "        \"isShow\": true,\n" +
                "        \"stroke\": \"#34495E\"\n" +
                "      },\n" +
                "      \"fullscreen_2\": {\n" +
                "        \"idAds\": \"ca-app-pub-3940256099942544/1043173715\",\n" +
                "        \"layout\": \"ads_native_fullscreen2_layout\",\n" +
                "        \"backgroundColor\": \"#8E44AD\",\n" +
                "        \"ctaColor\": \"#F39C12\",\n" +
                "        \"isShow\": false,\n" +
                "        \"stroke\": \"#9B59B6\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"layout_mappings\": {\n" +
                "    \"ads_native_banner_layout\": \"R.layout.ads_native_banner_layout\",\n" +
                "    \"ads_native_banner_layout_2\": \"R.layout.ads_native_banner_layout_2\",\n" +
                "    \"ads_native_interstitial_layout\": \"R.layout.ads_native_interstitial_layout\",\n" +
                "    \"ads_native_fullscreen1_layout\": \"R.layout.ads_native_fullscreen1_layout\",\n" +
                "    \"ads_native_fullscreen2_layout\": \"R.layout.ads_native_fullscreen2_layout\"\n" +
                "  },\n" +
                "  \"metadata\": {\n" +
                "    \"version\": \"1.0\",\n" +
                "    \"last_updated\": \"2024-01-15\",\n" +
                "    \"description\": \"Native Ads Configuration for BBL LibAds\",\n" +
                "    \"total_groups\": 3,\n" +
                "    \"total_configs\": 5\n" +
                "  }\n" +
                "}";
    }
}
