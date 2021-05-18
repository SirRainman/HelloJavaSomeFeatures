package com.rain.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: rain-java-ideas
 * @description:
 * @author: Rain
 * @create: 2021-03-15 19:57
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Blog {
    private Integer id;
    private Integer userID;
    private String title;
}
