package com.airbnb;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;



@SpringBootApplication
@ComponentScan("com.airbnb")
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static RestTemplate restTemplate = new RestTemplate();
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    public static void main(String[] args) {
        String runType="";
        String dataPath="";
        String queue="";
        for (String temp : args){
            if (temp.contains("runType")){
                runType = temp.split("=")[1];
            }
            if (temp.contains("dataPath")){
                dataPath = temp.split("=")[1];
            }
            if (temp.contains("queue")){
                queue = temp.split("=")[1];
            }
        }
        log.info("runType:{}", runType);
        log.info("dataPath:{}", dataPath);
        log.info("queue:{}", queue);
        if (runType != null && runType.equals("producer")) {
            doProducer(dataPath, queue);
        }else if(runType != null && runType.equals("consumer")) {
            doConsumer(queue);
        }
        if(!runType.equals("producer")){
            SpringApplication.run(Main.class, args);
        }
    }


    private static void doConsumer(String queue) {
        Map<String, String> map = new HashMap<>();
        InetAddress localHost = null;
        try {
            localHost = InetAddress.getLocalHost();
        } catch (UnknownHostException ignored) {
        }
        String ipAddress = localHost.getHostAddress();
        map.put("type", "add_consumer");
        map.put("ip", ipAddress+":8083");
        Map<String, String> res = restTemplate.postForObject("http://" + queue+"/centerWeb", map, Map.class);
        log.info("res:{}", JSON.toJSONString(res));
    }


        private static void doProducer(String dataPath, String queue) {
        StringBuffer sb = new StringBuffer();
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(dataPath), "Utf-8");
            int ch = 0;
            while (true) {
                if (!((ch = reader.read()) != -1)) break;
                sb.append((char) ch);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if(reader != null){
                    reader.close();
                }
            } catch (IOException ignored) {
            }
        }
        String jsonStr = sb.toString();
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        if(jsonObject != null && jsonObject.containsKey("tasks")){
            JSONArray tasks = jsonObject.getJSONArray("tasks");
            for (int i=0; i < tasks.size(); i ++){
                JSONObject task = tasks.getJSONObject(i);
                String url = task.getString("url");
                String headers = task.getString("headers");
                Map<String, String> map = new HashMap<>();
                map.put("url", url);
                map.put("headers", headers);
                map.put("type", "producer_send");
                Map<String, String> res = restTemplate.postForObject("http://" + queue+"/centerWeb", map, Map.class);
                log.info("res:{}", JSON.toJSONString(res));
            }
        }


    }

}
