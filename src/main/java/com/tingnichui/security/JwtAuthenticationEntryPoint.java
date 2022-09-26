package com.tingnichui.security;

import com.alibaba.fastjson.JSON;
import com.tingnichui.util.ResultGenerator;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义返回结果：未登录或登录过期
 * @author  Geng Hui
 * @date  2022/9/25 23:49
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Cache-Control","no-cache");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/json;charset=utf-8");
        response.getWriter().write(JSON.toJSONString(ResultGenerator.reLogin(authException.getMessage())));
        response.getWriter().flush();

    }
}
