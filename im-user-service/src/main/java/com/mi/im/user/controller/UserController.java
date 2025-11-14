package com.mi.im.user.controller;

import com.alibaba.nacos.common.JustForTest;
import com.fasterxml.jackson.annotation.JsonView;
import com.mi.im.api.user.UserService;
import com.mi.im.common.model.User;
import com.mi.im.common.model.dto.UserDTO;
import com.mi.im.common.model.views.Views;
import com.mi.im.user.dao.UserDao;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @className: UserController
 * @Description: TODO
 * @author: zhuyt
 * @date: 25/11/10 9:08
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;


    // 详情页 - 返回详细信息
    @GetMapping("/users/{id}")
    @JsonView(Views.Detail.class)
    public UserDTO getUserDetail(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // 管理员接口 - 返回所有信息
    @GetMapping("/admin/users/{phone}")
    @JsonView(Views.Admin.class)
    public UserDTO getUserForAdmin(@PathVariable String phone) {
        return userService.getUserByPhone(phone);
    }

    @GetMapping("/add")
    public void add() {
        List<User> employees = new ArrayList<>();
        double lat = 39.929986;
        double lon = 116.395645;
        for (int i = 0; i < 10; i++) {
            double max = 0.00001;
            double min = 0.000001;
            Random random = new Random();
            double s = random.nextDouble() % (max - min + 1) + max;
            DecimalFormat df = new DecimalFormat("######0.000000");
            // System.out.println(s);
            String lons = df.format(s + lon);
            String lats = df.format(s + lat);
            Double dlon = Double.valueOf(lons);
            Double dlat = Double.valueOf(lats);

            User employee = new User();
            employee.setEmail("1233213@123.com" + i);
            employee.setUsername("名字" + i);
            employee.setNickname("昵称" + i);
            employee.setPhone("电话" + i);
            employee.setPassword("123456");
            GeoPoint geoPoint = new GeoPoint(dlat+(i/10),dlon+(i/10));
            employee.setLocation(geoPoint);

            employees.add(employee);

        }
        userDao.saveAll(employees);
    }


    @GetMapping("/query")
    public Page<User> query() {
        double lat = 39.929986;
        double lon = 116.395645;
        // 实现了SearchQuery接口，用于组装QueryBuilder和SortBuilder以及Pageable等
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        Pageable pageable = PageRequest.of(0, 50);
        // 分页
        nativeSearchQueryBuilder.withPageable(pageable);

        // 间接实现了QueryBuilder接口
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 以某点为中心，搜索指定范围
        GeoDistanceQueryBuilder distanceQueryBuilder = new GeoDistanceQueryBuilder("location");
        distanceQueryBuilder.point(lat, lon);
        // 定义查询单位：公里
        distanceQueryBuilder.distance("50000000", DistanceUnit.KILOMETERS);
        boolQueryBuilder.filter(distanceQueryBuilder);
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);

        // 按距离升序
        GeoDistanceSortBuilder distanceSortBuilder =
                new GeoDistanceSortBuilder("location", lat, lon);
        distanceSortBuilder.unit(DistanceUnit.KILOMETERS);
        distanceSortBuilder.order(SortOrder.ASC);
        nativeSearchQueryBuilder.withSort(distanceSortBuilder);
        return userDao.search(nativeSearchQueryBuilder.build());
//        NativeSearchQuery query = nativeSearchQueryBuilder.build();
//        SearchHits<User> searchHits =  elasticsearchRestTemplate.search(query, User.class);
//        return  searchHits.stream()
//                .map(SearchHit::getContent)
            //    .collect(Collectors.toList());
    }

}
