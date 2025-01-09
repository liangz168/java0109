package com.airbnb.consumer.crawler;
import com.airbnb.consumer.web.ConsumerWeb;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import java.nio.charset.StandardCharsets;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;



@Component
public class AirbnbCrawler {
    private static final Logger log = LoggerFactory.getLogger(AirbnbCrawler.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;
    public void handle(String url, String headers) throws Exception {
        String result = doGet(url, headers);
        int start = result.indexOf("mapSearchResults");
        int end =  result.indexOf(",\"staysInViewport");
        String data = result.substring(start, end).replace("mapSearchResults\":", "");
        JSONArray mapSearchResults = JSON.parseArray(data);
        for(int i =0; i < mapSearchResults.size(); i ++){
            JSONObject temp = mapSearchResults.getJSONObject(i);
            JSONObject listing = temp.getJSONObject("listing");
            // Hotel name
            String title = listing.getString("title");
            // Star
            String price = temp.getJSONObject("pricingQuote")
                    .getJSONObject("structuredStayDisplayPrice")
                    .getJSONObject("primaryLine")
                    .getString("price");
            JSONObject listingParamOverrides = temp.getJSONObject("listingParamOverrides");
            String checkin = listingParamOverrides.getString("checkin");
            String checkout = listingParamOverrides.getString("checkout");
            int adults = listingParamOverrides.getIntValue("adults");
            int children = listingParamOverrides.getIntValue("children");
            Map<String, String> itemMap = new HashMap<>();
            itemMap.put("title", title);
            String ratingAverage = temp.getString("avgRatingLocalized").split(" ")[0];
            itemMap.put("ratingAverage", ratingAverage);
            if (price == null){
                itemMap.put("price", "");
            }else {
                itemMap.put("price", price.replace("¥ ", "").replace(",", ""));
            }
             itemMap.put("checkin", checkin);
            itemMap.put("checkout", checkout);
            String guestNum = adults + children + "";
            itemMap.put("guestNum", guestNum);
            log.info("itemMap:{}", JSON.toJSONString(itemMap));
            jdbcTemplate.update("insert into t_Airbnb_data (title, rating_average, price, tax_price, checkin, checkout, guest_num) values(?, ?, ?, ?, ?, ?, ?)",
                    title, ratingAverage, price, "", checkin, checkout, guestNum
            );

        }
    }



    private String doGet(String httpUrl, String headers) throws Exception {
        RestTemplate restTemplate= getRestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        JSONObject headersObject = JSON.parseObject(headers);
        for(String key : headersObject.keySet()){
            httpHeaders.add(key, headersObject.getString(key));
        }
        HttpEntity<String> entity = new HttpEntity<String>(null, httpHeaders);
        System.out.println("111");
        return restTemplate.exchange(httpUrl, HttpMethod.GET,entity,String.class).getBody();
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
