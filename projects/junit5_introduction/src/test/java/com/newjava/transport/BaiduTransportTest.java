package com.newjava.transport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BaiduTransportTest {
    private BaiduTransport baiduTransport;

    @BeforeEach
    void setUp() {
        baiduTransport = new BaiduTransport();
    }

    @Test
    void testQuery() {
        System.out.println(baiduTransport.query("hello"));
    }
}