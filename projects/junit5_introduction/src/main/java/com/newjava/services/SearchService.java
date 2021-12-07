package com.newjava.services;

import com.newjava.transport.BaiduTransport;

import java.util.List;

public class SearchService {

    private BaiduTransport transport;

    public SearchService(BaiduTransport transport) {
        this.transport = transport;
    }

    public List<String> search(String query) {
        validateQuery(query);
        String response = transport.query(query);
        return extractValuableInformation(response);
    }

    private List<String> extractValuableInformation(String response) {
        return List.of();
    }

    private void validateQuery(String query) {

    }
}
