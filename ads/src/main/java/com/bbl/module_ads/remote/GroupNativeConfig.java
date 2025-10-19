package com.bbl.module_ads.remote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Group quản lý nhiều NativeConfig
 * Cho phép tổ chức và quản lý các config native ads theo nhóm
 */
public class GroupNativeConfig {
    
    // Tên của group
    private String groupName;
    
    // Map chứa các NativeConfig với key là tên config
    private Map<String, NativeConfig> nativeConfigs;
    
    // Constructor mặc định
    public GroupNativeConfig() {
        this.groupName = "";
        this.nativeConfigs = new HashMap<>();
    }
    
    // Constructor với tên group
    public GroupNativeConfig(String groupName) {
        this.groupName = groupName;
        this.nativeConfigs = new HashMap<>();
    }
    
    // Thêm một NativeConfig vào group
    public void addNativeConfig(String configName, NativeConfig nativeConfig) {
        if (nativeConfig != null) {
            nativeConfigs.put(configName, nativeConfig);
        }
    }
    
    // Lấy NativeConfig theo tên
    public NativeConfig getNativeConfig(String configName) {
        return nativeConfigs.get(configName);
    }
    
    // Xóa NativeConfig theo tên
    public boolean removeNativeConfig(String configName) {
        return nativeConfigs.remove(configName) != null;
    }
    
    // Kiểm tra xem có tồn tại config với tên này không
    public boolean hasConfig(String configName) {
        return nativeConfigs.containsKey(configName);
    }
    
    // Lấy tất cả tên config trong group
    public List<String> getAllConfigNames() {
        return new ArrayList<>(nativeConfigs.keySet());
    }
    
    // Lấy tất cả NativeConfig trong group
    public List<NativeConfig> getAllNativeConfigs() {
        return new ArrayList<>(nativeConfigs.values());
    }
    
    // Lấy số lượng config trong group
    public int getConfigCount() {
        return nativeConfigs.size();
    }
    
    // Kiểm tra group có rỗng không
    public boolean isEmpty() {
        return nativeConfigs.isEmpty();
    }
    
    // Xóa tất cả config trong group
    public void clearAllConfigs() {
        nativeConfigs.clear();
    }
    
    // Getters và Setters
    public String getGroupName() {
        return groupName;
    }
    
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    public Map<String, NativeConfig> getNativeConfigs() {
        return nativeConfigs;
    }
    
    public void setNativeConfigs(Map<String, NativeConfig> nativeConfigs) {
        this.nativeConfigs = nativeConfigs != null ? nativeConfigs : new HashMap<>();
    }
    
    @Override
    public String toString() {
        return "GroupNativeConfig{" +
                "groupName='" + groupName + '\'' +
                ", configCount=" + nativeConfigs.size() +
                ", nativeConfigs=" + nativeConfigs +
                '}';
    }
}
