package com.newjava.transport;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class BaiduTransport {
    public String query(String query) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://www.baidu.com/s?wd="+query)).build();
        HttpResponse.BodyHandler<String> responseBodyHandler = HttpResponse.BodyHandlers.ofString();
        try {
            return client.send(request, responseBodyHandler).body();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
