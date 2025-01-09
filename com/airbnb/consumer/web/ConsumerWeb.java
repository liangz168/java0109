package com.airbnb.consumer.web;

import com.airbnb.consumer.crawler.AirbnbCrawler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
public class ConsumerWeb {
    private static final Logger log = LoggerFactory.getLogger(ConsumerWeb.class);
    @Autowired
    private AirbnbCrawler airbnbCrawler;

    @PostMapping(path = "/consumerWeb")
    public Map<String, String> consumerWeb(@RequestBody Map<String, String> map){
        try{
            String url = map.get("url");
            String headers = map.get("headers");
            airbnbCrawler.handle(url, headers);
        }catch (Exception e){
            log.error("consumerWeb error:", e);
        }
        return new HashMap<>();
    }

}
