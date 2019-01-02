package com.scj.pic_server.controller;


import com.alibaba.fastjson.JSONObject;
import com.scj.pic_server.config.OkHttp3Util;
import com.scj.pic_server.util.JedisUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Controller
public class HtmlController {
    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("hello")
    @ResponseBody
    public String sayHello(){
    System.out.println("打印输出");
        return "hello";
    }

    @RequestMapping("/404")
    public String  to404(){
        System.out.println("打印输出404页面");
        return "404-2";
    }
    @RequestMapping("/redis144")
    @ResponseBody
    public String redis144(){
        JedisUtils jedisUtils =new JedisUtils("47.98.153.144","scj19890606");
        String result=jedisUtils.get("1111111111111");
        System.out.println(result);
        return result;
    }
    @RequestMapping("/redis37")
    @ResponseBody
    public String redis37(){
         OkHttpClient client = new OkHttpClient();
          client = new OkHttpClient().newBuilder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                // .sslSocketFactory(new XZTLSSocketFactory())
                .build();

            String url="https://www.ip.cn/";
            Request request = new Request.Builder().url(url.trim()).addHeader("type_", "1").get().build();
            Response response = null;
            String responseUrl="";
            try {
                response = client.newCall(request).execute();
                responseUrl = response.body().string();

            } catch (IOException e) {

            }


        return response.body().toString();


    }


}
