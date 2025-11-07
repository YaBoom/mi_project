package com.mi.im.search.service.impl;

import com.mi.im.search.service.SearchService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
}