package com.mi.im.search.controller;

import com.mi.im.api.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GEO搜索控制器，提供地理位置相关的搜索接口
 */
@RestController
@RequestMapping("/api/geo")
public class GeoSearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 搜索附近的用户
     * 
     * @param userId 用户ID
     * @param latitude 纬度
     * @param longitude 经度
     * @param distance 距离（单位：公里）
     * @param page 页码
     * @param pageSize 每页大小
     * @return 附近用户列表
     */
    @GetMapping("/nearby-users")
    public ResponseEntity<List<Map<String, Object>>> searchNearbyUsers(
            @RequestParam("userId") Long userId,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam(defaultValue = "5.0") Double distance,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        try {
            List<Map<String, Object>> results = searchService.searchNearbyUsers(
                    userId, latitude, longitude, distance, page, pageSize);
            return ResponseEntity.ok(results);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * 更新用户地理位置
     * 
     * @param userId 用户ID
     * @param latitude 纬度
     * @param longitude 经度
     * @return 更新结果
     */
    @PostMapping("/update-location")
    public ResponseEntity<Map<String, Boolean>> updateUserLocation(
            @RequestParam("userId") String userId,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude) {
        try {
            boolean success = searchService.updateUserLocation(userId, latitude, longitude);
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", success);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 批量更新用户地理位置
     * 
     * @param userLocations 用户ID和地理位置信息的映射
     * @return 更新结果
     */
    @PostMapping("/batch-update-location")
    public ResponseEntity<Map<String, Object>> batchUpdateUserLocations(
            @RequestBody Map<String, Map<String, Double>> userLocations) {
        try {
            int updatedCount = searchService.batchUpdateUserLocations(userLocations);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("updatedCount", updatedCount);
            response.put("totalCount", userLocations.size());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}