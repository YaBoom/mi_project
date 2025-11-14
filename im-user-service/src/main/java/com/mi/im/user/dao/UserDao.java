package com.mi.im.user.dao;

import com.mi.im.common.model.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

/**
 * @className: UserDao
 * @Description: TODO
 * @author: zhuyt
 * @date: 25/11/11 17:06
 */
@Component
public interface UserDao extends ElasticsearchRepository<User,String> {
}
