package com.airbnb.center.web;

import com.airbnb.consumer.web.ConsumerWeb;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class CenterWeb {
    private static final Logger log = LoggerFactory.getLogger(CenterWeb.class);
    private ConcurrentHashMap<String, String> consumerMap = new ConcurrentHashMap();
    private RestTemplate restTemplate = new RestTemplate();

    @PostMapping(path = "/centerWeb")
    public Map<String, String> centerWeb(@RequestBody Map<String, String> map){
        log.info("centerWeb:{}", JSON.toJSONString(map));
        String type = map.get("type");
        if(type.equals("add_consumer")){
            consumerMap.put(map.get("ip"), System.currentTimeMillis() + "");
        }else if (type.equals("producer_send")){
            if(consumerMap.isEmpty()){
                return new HashMap<>();
            }
            String ip = getConsumer();
            Map<String, String> res = restTemplate.postForObject("http://" + ip + "/consumerWeb", map, Map.class);
        }
        return new HashMap<>();
    }


    private String getConsumer(){
        List<String> list = new ArrayList<>(consumerMap.keySet());
        return list.get(new Random().nextInt(list.size()));
    }

}
