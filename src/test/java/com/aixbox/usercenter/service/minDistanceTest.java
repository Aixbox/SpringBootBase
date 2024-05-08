package com.aixbox.usercenter.service;

import com.aixbox.usercenter.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * @author 魔王Aixbox
 * @version 1.0
 */
@SpringBootTest
public class minDistanceTest {

    @Test
    public void test(){

        List<String> list1 = Arrays.asList("java", "qq", "男");
        List<String> list2 = Arrays.asList("java", "qq", "女");
        List<String> list3 = Arrays.asList("java", "微信", "女");
        int i = AlgorithmUtils.minDistance(list1, list2);
        int b = AlgorithmUtils.minDistance(list1, list3);
        System.out.println(i);
        System.out.println(b);
    }

}
