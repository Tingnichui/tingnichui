package com.tingnichui.util;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Geng Hui
 * @date 2022/8/13 0:17
 */
public class DingdingUtil {

    public static String url = "https://oapi.dingtalk.com/robot/send?access_token=699bc7911894f4aba002fcddbde27440ee552742605067c7a55c238e8e2dbf51";


    public static void sendMsg(String content){
        HashMap<String, Object> text = new HashMap<>();
        text.put("content", "Tingnichui\n" + content);

        HashMap<String, Object> params = new HashMap<>();
        params.put("msgtype", "text");
        params.put("text", text);
        Map<String,String> headers = new HashMap<>();
        headers.put("Content-type", "application/json; charset=utf-8");
        HttpUtil.post(url, JSON.toJSONString(params) ,headers);
    }




}
