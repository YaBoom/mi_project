package com.mi.im.search.service.impl;

import com.mi.im.api.search.SearchService;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * 搜索服务实现类
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient elasticsearchClient;

    @Value("${im.search.highlight.pre-tags}")
    private String highlightPreTags;

    @Value("${im.search.highlight.post-tags}")
    private String highlightPostTags;

    @Value("${im.search.max-result-size}")
    private int maxResultSize;

    @Override
    public List<Map<String, Object>> searchMessages(String keyword, Long userId, int page, int pageSize) throws IOException {
        SearchRequest searchRequest = new SearchRequest("im_messages");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        
        // 构建查询条件
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.multiMatchQuery(keyword, "content", "senderName"));
        boolQuery.filter(QueryBuilders.termQuery("userId", userId));
        
        searchSourceBuilder.query(boolQuery);
        
        // 设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("content");
        highlightBuilder.preTags(highlightPreTags);
        highlightBuilder.postTags(highlightPostTags);
        searchSourceBuilder.highlighter(highlightBuilder);
        
        // 设置分页
        searchSourceBuilder.from((page - 1) * pageSize);
        searchSourceBuilder.size(pageSize);
        searchSourceBuilder.trackTotalHits(true);
        
        searchRequest.source(searchSourceBuilder);
        
        // 执行搜索
        SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
        
        // 处理结果
        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            results.add(sourceAsMap);
        }
        
        return results;
    }

    @Override
    public List<Map<String, Object>> searchUsers(String keyword, Long currentUserId, int page, int pageSize) throws IOException {
        SearchRequest searchRequest = new SearchRequest("im_users");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        
        // 构建查询条件
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.multiMatchQuery(keyword, "username", "nickname", "phone"));
        boolQuery.mustNot(QueryBuilders.termQuery("id", currentUserId));
        
        searchSourceBuilder.query(boolQuery);
        
        // 设置分页
        searchSourceBuilder.from((page - 1) * pageSize);
        searchSourceBuilder.size(pageSize);
        
        searchRequest.source(searchSourceBuilder);
        
        // 执行搜索
        SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
        
        // 处理结果
        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            results.add(sourceAsMap);
        }
        
        return results;
    }

    @Override
    public List<Map<String, Object>> searchGroups(String keyword, Long userId, int page, int pageSize) throws IOException {
        SearchRequest searchRequest = new SearchRequest("im_groups");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        
        // 构建查询条件
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.multiMatchQuery(keyword, "name", "description"));
        boolQuery.filter(QueryBuilders.termQuery("members.userId", userId));
        
        searchSourceBuilder.query(boolQuery);
        
        // 设置分页
        searchSourceBuilder.from((page - 1) * pageSize);
        searchSourceBuilder.size(pageSize);
        
        searchRequest.source(searchSourceBuilder);
        
        // 执行搜索
        SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
        
        // 处理结果
        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            results.add(sourceAsMap);
        }
        
        return results;
    }

    @Override
    public List<Map<String, Object>> searchNearbyUsers(Long currentUserId, Double latitude, Double longitude, Double distance, int page, int pageSize) throws IOException {
        SearchRequest searchRequest = new SearchRequest("im_users");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        
        // 构建查询条件
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        
        // 排除自己
        boolQuery.mustNot(QueryBuilders.termQuery("userId", currentUserId.toString()));
        
        // 地理位置查询 - 查找指定距离内的用户
        GeoDistanceQueryBuilder geoDistanceQuery = QueryBuilders.geoDistanceQuery("location")
                .point(latitude, longitude)
                .distance(distance, DistanceUnit.KILOMETERS)
                .geoDistance(GeoDistance.ARC); // 使用球面距离计算
        
        boolQuery.filter(geoDistanceQuery);
        searchSourceBuilder.query(boolQuery);
        
        // 按距离排序
        GeoDistanceSortBuilder geoDistanceSort = SortBuilders.geoDistanceSort("location")
                .point(latitude, longitude)
                .unit(DistanceUnit.KILOMETERS)
                .order(SortOrder.ASC);
        searchSourceBuilder.sort(geoDistanceSort);
        
        // 设置分页
        searchSourceBuilder.from((page - 1) * pageSize);
        searchSourceBuilder.size(pageSize);
        
        // 计算并返回距离信息
        searchSourceBuilder.fetchSource(true);
        
        searchRequest.source(searchSourceBuilder);
        
        // 执行搜索
        SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
        
        // 处理结果
        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            
            // 添加距离信息
            Object[] sortValues = hit.getSortValues();
            if (sortValues != null && sortValues.length > 0) {
                // 格式化距离，保留两位小数
                Double distanceValue = (Double) sortValues[0];
                sourceAsMap.put("distance", String.format("%.2f", distanceValue));
                sourceAsMap.put("distanceValue", distanceValue);
            }
            
            // 移除敏感信息
            sourceAsMap.remove("password");
            sourceAsMap.remove("email");
            
            results.add(sourceAsMap);
        }
        
        return results;
    }

    @Override
    public boolean updateUserLocation(String userId, Double latitude, Double longitude) throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("im_users", userId);
        
        // 创建地理位置对象
        Map<String, Object> locationMap = new HashMap<>();
        locationMap.put("lat", latitude);
        locationMap.put("lon", longitude);
        
        // 创建更新内容
        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("latitude", latitude);
        updateFields.put("longitude", longitude);
        updateFields.put("location", locationMap);
        updateFields.put("updateTime", System.currentTimeMillis());
        
        // 使用脚本更新或upsert（如果文档不存在则插入）
        updateRequest.doc(updateFields).upsert(updateFields);
        
        try {
            elasticsearchClient.update(updateRequest, RequestOptions.DEFAULT);
            return true;
        } catch (IOException e) {
            // 记录错误日志
            System.err.println("更新用户位置失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public int batchUpdateUserLocations(Map<String, Map<String, Double>> userLocations) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        long currentTime = new Date().getTime();
        
        for (Map.Entry<String, Map<String, Double>> entry : userLocations.entrySet()) {
            String userId = entry.getKey();
            Map<String, Double> locationData = entry.getValue();
            
            Double latitude = locationData.get("latitude");
            Double longitude = locationData.get("longitude");
            
            if (latitude != null && longitude != null) {
                UpdateRequest updateRequest = new UpdateRequest("im_users", userId);
                
                // 创建地理位置对象
                Map<String, Object> locationMap = new HashMap<>();
                locationMap.put("lat", latitude);
                locationMap.put("lon", longitude);
                
                // 创建更新内容
                Map<String, Object> updateFields = new HashMap<>();
                updateFields.put("latitude", latitude);
                updateFields.put("longitude", longitude);
                updateFields.put("location", locationMap);
                updateFields.put("updateTime", currentTime);
                
                updateRequest.doc(updateFields).upsert(updateFields);
                bulkRequest.add(updateRequest);
            }
        }
        
        if (bulkRequest.requests().isEmpty()) {
            return 0;
        }
        
        BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        
        // 返回成功更新的数量
        return bulkRequest.requests().size() - bulkResponse.getItems().length;
    }
}