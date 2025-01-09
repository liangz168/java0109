package com.airbnb.consumer.crawler;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.util.List;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;


public class AirbnbCrawler {
    public List<String> handle(String url) throws Exception {
        String res = doGet("https://www.airbnb.com/s/Europe/homes?tab_id=home_tab&refinement_paths%5B%5D=%2Fhomes&flexible_trip_lengths%5B%5D=one_week&monthly_start_date=2024-12-01&monthly_length=3&monthly_end_date=2025-03-01&price_filter_input_type=0&channel=EXPLORE&place_id=ChIJhdqtz4aI7UYRefD8s-aZ73I&date_picker_type=calendar&source=structured_search_input_header&search_type=filter_change");
        System.out.println(res);
        return null;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("111");
        new AirbnbCrawler().handle(null);
    }


    public static String doGet(String httpUrl) throws Exception {
        RestTemplate restTemplate= getRestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X x.y;rv:42.0) Gecko/20100101 Firefox/42.0");
        HttpEntity<String> entity = new HttpEntity<String>(null, httpHeaders);
        System.out.println("111");
        String result=restTemplate.exchange(httpUrl, HttpMethod.GET,entity,String.class).getBody();
        int start = result.indexOf("mapSearchResults");
        int end =  result.indexOf(",\"staysInViewport");
        String data = result.substring(start, end).replace("mapSearchResults\":", "");
        JSONArray mapSearchResults = JSON.parseArray(data);
        for(int i =0; i < mapSearchResults.size(); i ++){
            JSONObject temp = mapSearchResults.getJSONObject(i);
            JSONObject item = temp.getJSONObject("listing");
            // Hotel name
            String title = item.getString("title");
            // Star
            String ratingAverage = item.getString("ratingAverage");
            //String ratingAverage = item.getString("ratingAverage");
            System.out.println(title);
        }

        return "";
    }

    /**
     * 构造RestTemplate
     *
     * @return
     * @throws Exception
     */
    public static RestTemplate getRestTemplate() throws Exception {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        // 超时
        factory.setConnectionRequestTimeout(5000);
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(createIgnoreVerifySSL(),
                // 指定TLS版本
                null,
                // 指定算法
                null,
                // 取消域名验证
                new HostnameVerifier() {
                    @Override
                    public boolean verify(String string, SSLSession ssls) {
                        return true;
                    }
                });
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        factory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(factory);
        // 解决中文乱码问题
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

    private static SSLContext createIgnoreVerifySSL() throws Exception {
        SSLContext sc = SSLContext.getInstance("TLS");

        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                                           String paramString) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                                           String paramString) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        sc.init(null, new TrustManager[] { trustManager }, null);
        return sc;
    }
}
