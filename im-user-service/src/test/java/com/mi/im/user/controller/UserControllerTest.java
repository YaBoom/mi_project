package com.mi.im.user.controller;

import com.mi.im.common.model.User;
import com.mi.im.user.dao.UserDao;
import com.mi.im.user.mapper.UserMapper;
import org.elasticsearch.common.geo.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {
    @Autowired
    private UserDao userDao;

    @Test
    public void testAdd() {
        List<User> employees = new ArrayList<>();
        double lat = 39.929986;
        double lon = 116.395645;
        for(int i = 100000; i < 1000000; i++){
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
            employee.setEmail("1233213@123.com"+i);
            employee.setUsername("名字" + i);
            employee.setNickname("昵称" + i);
            employee.setPhone("电话"  + i);
            employee.setPassword("123456");
            employee.setLocation(dlat + "," + dlon);

            employees.add(employee);

        }
        userDao.saveAll(employees);
    }

}