package com.bbl.module_ads.remote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RemoteConfig chính để quản lý tất cả các config của ứng dụng
 * Bao gồm các group config cho native ads và các config khác
 */
public class RemoteConfig {
    
    // Map chứa các GroupNativeConfig với key là tên group
    private Map<String, GroupNativeConfig> groupNativeConfigs;
    
    // Constructor mặc định
    public RemoteConfig() {
        this.groupNativeConfigs = new HashMap<>();
    }
    
    // Thêm một GroupNativeConfig
    public void addGroupNativeConfig(String groupName, GroupNativeConfig groupConfig) {
        if (groupConfig != null) {
            groupNativeConfigs.put(groupName, groupConfig);
        }
    }
    
    // Lấy GroupNativeConfig theo tên
    public GroupNativeConfig getGroupNativeConfig(String groupName) {
        return groupNativeConfigs.get(groupName);
    }
    
    // Xóa GroupNativeConfig theo tên
    public boolean removeGroupNativeConfig(String groupName) {
        return groupNativeConfigs.remove(groupName) != null;
    }
    
    // Kiểm tra xem có tồn tại group với tên này không
    public boolean hasGroup(String groupName) {
        return groupNativeConfigs.containsKey(groupName);
    }
    
    // Lấy tất cả tên group
    public String[] getAllGroupNames() {
        return groupNativeConfigs.keySet().toArray(new String[0]);
    }
    
    // Lấy số lượng group
    public int getGroupCount() {
        return groupNativeConfigs.size();
    }
    
    // Kiểm tra có group nào không
    public boolean hasAnyGroup() {
        return !groupNativeConfigs.isEmpty();
    }
    
    // Xóa tất cả group
    public void clearAllGroups() {
        groupNativeConfigs.clear();
    }
    
    // Lấy NativeConfig cụ thể từ một group cụ thể
    public NativeConfig getNativeConfig(String groupName, String configName) {
        GroupNativeConfig group = getGroupNativeConfig(groupName);
        return group != null ? group.getNativeConfig(configName) : null;
    }
    
    // Thêm NativeConfig vào một group cụ thể
    public void addNativeConfig(String groupName, String configName, NativeConfig nativeConfig) {
        GroupNativeConfig group = getGroupNativeConfig(groupName);
        if (group == null) {
            group = new GroupNativeConfig(groupName);
            addGroupNativeConfig(groupName, group);
        }
        group.addNativeConfig(configName, nativeConfig);
    }
    
    /**
     * Tìm NativeConfig theo ad_unit_id
     * Tìm kiếm trong tất cả các group và trả về config đầu tiên tìm thấy
     * @param adUnitId Ad unit ID cần tìm
     * @return NativeConfig tương ứng, null nếu không tìm thấy
     */
    public NativeConfig getNativeConfigByAdUnitId(String adUnitId) {
        if (adUnitId == null || adUnitId.isEmpty()) {
            return null;
        }
        
        for (GroupNativeConfig group : groupNativeConfigs.values()) {
            for (NativeConfig config : group.getAllNativeConfigs()) {
                if (adUnitId.equals(config.getIdAds())) {
                    return config;
                }
            }
        }
        return null;
    }
    
    /**
     * Lấy tất cả NativeConfig có cùng ad_unit_id
     * @param adUnitId Ad unit ID cần tìm
     * @return List chứa tất cả NativeConfig có cùng ad_unit_id
     */
    public List<NativeConfig> getAllNativeConfigsByAdUnitId(String adUnitId) {
        List<NativeConfig> result = new ArrayList<>();
        if (adUnitId == null || adUnitId.isEmpty()) {
            return result;
        }
        
        for (GroupNativeConfig group : groupNativeConfigs.values()) {
            for (NativeConfig config : group.getAllNativeConfigs()) {
                if (adUnitId.equals(config.getIdAds())) {
                    result.add(config);
                }
            }
        }
        return result;
    }
    
    /**
     * Tìm NativeConfig theo ad_unit_id và trả về thông tin group và config name
     * @param adUnitId Ad unit ID cần tìm
     * @return Map chứa thông tin: "groupName", "configName", "nativeConfig"
     */
    public Map<String, Object> getNativeConfigInfoByAdUnitId(String adUnitId) {
        Map<String, Object> result = new HashMap<>();
        if (adUnitId == null || adUnitId.isEmpty()) {
            return result;
        }
        
        for (Map.Entry<String, GroupNativeConfig> groupEntry : groupNativeConfigs.entrySet()) {
            String groupName = groupEntry.getKey();
            GroupNativeConfig group = groupEntry.getValue();
            
            for (Map.Entry<String, NativeConfig> configEntry : group.getNativeConfigs().entrySet()) {
                String configName = configEntry.getKey();
                NativeConfig config = configEntry.getValue();
                
                if (adUnitId.equals(config.getIdAds())) {
                    result.put("groupName", groupName);
                    result.put("configName", configName);
                    result.put("nativeConfig", config);
                    return result;
                }
            }
        }
        return result;
    }
    
    /**
     * Kiểm tra xem có tồn tại NativeConfig với ad_unit_id này không
     * @param adUnitId Ad unit ID cần kiểm tra
     * @return true nếu tồn tại, false nếu không
     */
    public boolean hasNativeConfigWithAdUnitId(String adUnitId) {
        return getNativeConfigByAdUnitId(adUnitId) != null;
    }
    
    /**
     * Đếm số lượng NativeConfig có cùng ad_unit_id
     * @param adUnitId Ad unit ID cần đếm
     * @return Số lượng config có cùng ad_unit_id
     */
    public int countNativeConfigsByAdUnitId(String adUnitId) {
        return getAllNativeConfigsByAdUnitId(adUnitId).size();
    }
    
    // Getters và Setters
    public Map<String, GroupNativeConfig> getGroupNativeConfigs() {
        return groupNativeConfigs;
    }
    
    public void setGroupNativeConfigs(Map<String, GroupNativeConfig> groupNativeConfigs) {
        this.groupNativeConfigs = groupNativeConfigs != null ? groupNativeConfigs : new HashMap<>();
    }
    
    @Override
    public String toString() {
        return "RemoteConfig{" +
                "groupCount=" + groupNativeConfigs.size() +
                ", groupNativeConfigs=" + groupNativeConfigs +
                '}';
    }
}
