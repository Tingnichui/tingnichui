package com.tingnichui.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


public class WechatUtil {


    private String appid;

    private String secret;

    public static void main(String[] args) {
        getAccessToken("wxce80aa60be567bbf", "0bca5218fa507963cbece42a0efbd3ca");
    }

    /**
     * 获取access_token
     */
    public static String getAccessToken(String appId, String appsecret) {
        String re = HttpUtil.get("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + appsecret);
        JSONObject js = JSON.parseObject(re);
        String accessToken = js.getString("access_token");
        return accessToken;
    }

    // 推送消息
//    public void pushMessage(String openId) {
//        //1，配置
//        WxMpInMemoryConfigStorage wxStorage = new WxMpInMemoryConfigStorage();
//        wxStorage.setAppId(appid);
//        wxStorage.setSecret(secret);
//        WxMpService wxMpService = new WxMpServiceImpl();
//        wxMpService.setWxMpConfigStorage(wxStorage);
//        List<WxMpTemplateData> wxMpTemplateDataList = Arrays.asList(new WxMpTemplateData("content", "Tingnichui"));
//        //2,推送消息
//        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
//                .toUser(openId)
//                .templateId("qAo-oGUuAhdnLI2eX11S85byIqui1En3j17I_2AYNRg")
//                .data(wxMpTemplateDataList)
//                .url("http://www.baidu.com")
//                .build();
//        try {
//            wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
//        } catch (Exception e) {
//            System.out.println("推送失败：" + e.getMessage());
//        }
//
//    }

    /**
     * 生成url 获取微信用户code，并重定向获取用户openId
     *
     * @return
     */
    public String getUserCode() {
        String backUrl = "http://chunhui.natappvip.cc/tingnichui/getUserOpenId";
        String getOpenIdUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=" + backUrl + "&response_type=code&scope=snsapi_base&state=STATE#wechat_redirect";
        getOpenIdUrl = getOpenIdUrl.replace("APPID", appid);
        return "redirect:" + getOpenIdUrl;
    }

    /**
     * 获取用户openId
     *
     * @return
     * @throws IOException
     */
    public String getUserOpenId(HttpServletRequest request) {
        //获取code
        String code = request.getParameter("code");
        //换取用户openid
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
        url = url.replace("APPID", appid).replace("SECRET", secret).replace("CODE", code);
        String result = HttpUtil.get(url);
        JSONObject jSONObject = JSONObject.parseObject(result);
        String openid = jSONObject.getString("openid");
        DingdingUtil.sendMsg(openid);
        return openid;
    }


}
