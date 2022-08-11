package com.tingnichui.util;

import cn.hutool.core.codec.Base64;
import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URLEncoder;
import java.util.Map;

@Component
public class BaiduUtil {


    @Value("${baidu_api_key}")
    private String clientId;

    @Value("${baidu_secret_key}")
    private String clientSecre;

    private String accesstoken;

    private BaiduUtil() {
    }

    public String accurate() {
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";
        try {
            // 本地文件路径
            String imgStr = Base64.encode(new File("C:\\Users\\abc\\Desktop\\pic\\微信截图_20220523203026.png"));;
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");
            String param = "image=" + imgParam;
            String result = HttpUtil.post(url + "?access_token=" + getAccessToken(), param);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private String getAccessToken() throws Exception {
        if (StringUtils.isBlank(accesstoken)){
            String result = HttpUtil.get("https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecre);
            Map<String,String> resultMap = (Map<String, String>) JSON.parse(result);
            accesstoken = resultMap.get("access_token");
        }
        return accesstoken;
    }




}
