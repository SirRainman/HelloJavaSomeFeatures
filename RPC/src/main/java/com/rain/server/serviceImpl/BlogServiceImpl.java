package com.rain.server.serviceImpl;

import com.rain.common.Blog;

/**
 * @program: rain-java-ideas
 * @description:
 * @author: Rain
 * @create: 2021-03-15 20:00
 **/
public class BlogServiceImpl {
    public Blog getBlogByID(Integer id) {
        return Blog.builder().id(id).title("题太简单了").userID(123456).build();
    }
}
