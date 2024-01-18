package com.ink.backend.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

/**
 * MD5加密测试
 *
 */
@SpringBootTest
public class Md5Test {

    @Test
    public void passwordTest() {
        String salt = "ink";
        String userPassword = "runi2003";
        String realPassword = DigestUtils.md5DigestAsHex((salt + userPassword).getBytes());
        System.out.println(realPassword);
    }

}
