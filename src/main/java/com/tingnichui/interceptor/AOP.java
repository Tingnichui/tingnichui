package com.tingnichui.interceptor;

import com.alibaba.fastjson.JSON;
import com.tingnichui.util.ResultGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
@Slf4j
@Aspect
@Component
public class AOP {


    /** controller日志 **/
    @Around("execution(* com.tingnichui.controller.*.*(..))")
    public Object controllerAround(ProceedingJoinPoint point) {
        //执行链条ID
        MDC.put("processId", UUID.randomUUID().toString().replaceAll("-",""));
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, String[]> parameter = request.getParameterMap();
        Map<String,String> parameterMap = new HashMap<>();
        for (String key : parameter.keySet()){
            parameterMap.put(key, StringUtils.join(parameter.get(key),","));
        }
        if (request.getContentType() != null && request.getContentType().toLowerCase().contains("application/json") && point.getArgs().length > 0) {
            parameterMap.put("requestBody", JSON.toJSONString(point.getArgs()[0]));
        }
        //入参日志
        String requestLog = JSON.toJSONString(parameterMap);
        //执行方法名
        MethodSignature method = (MethodSignature) point.getSignature();
        StringBuilder className = new StringBuilder();
        className.append(method.getDeclaringTypeName());
        className.append(".");
        className.append(method.getName());
        //
        long currentTimeMillis = System.currentTimeMillis();
        Object obj = null;
        try {
            obj = point.proceed();
            return obj;
        } catch (Throwable throwable) {
            log.error("系统异常",throwable);
            return ResultGenerator.genErrorResult("B001","系统错误");
        } finally {
            long diffTimeMillis = System.currentTimeMillis() - currentTimeMillis;
            // 出参日志
            String responseLog = obj == null ? null : JSON.toJSONString(obj);
            log.info("controller." + method.getName() + "|耗时={}|入参={}，出参={}|" + className,diffTimeMillis,requestLog,responseLog);
        }

    }

    /** service日志 **/
    @Around("execution(* com.tingnichui.service.*.*(..))")
    public Object serviceAround(ProceedingJoinPoint point) {
        //执行链条ID
        MDC.put("processId", UUID.randomUUID().toString().replaceAll("-",""));
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, String[]> parameter = request.getParameterMap();
        Map<String,String> parameterMap = new HashMap<>();
        for (String key : parameter.keySet()){
            parameterMap.put(key, StringUtils.join(parameter.get(key),","));
        }
        if (request.getContentType() != null && request.getContentType().toLowerCase().contains("application/json") && point.getArgs().length > 0) {
            parameterMap.put("requestBody", JSON.toJSONString(point.getArgs()[0]));
        }
        //入参日志
        String requestLog = JSON.toJSONString(parameterMap);
        //执行方法名
        MethodSignature method = (MethodSignature) point.getSignature();
        StringBuilder className = new StringBuilder();
        className.append(method.getDeclaringTypeName());
        className.append(".");
        className.append(method.getName());
        //
        long currentTimeMillis = System.currentTimeMillis();
        Object obj = null;
        try {
            obj = point.proceed();
            return obj;
        } catch (Throwable throwable) {
            log.error("系统异常",throwable);
            return ResultGenerator.genErrorResult("B001","系统错误");
        } finally {
            long diffTimeMillis = System.currentTimeMillis() - currentTimeMillis;
            // 出参日志
            String responseLog = obj == null ? null : JSON.toJSONString(obj);
            log.info("service." + method.getName() + "|耗时={}|入参={}，出参={}|" + className,diffTimeMillis,requestLog,responseLog);
        }
    }

    /** dao日志 **/
    @Around("execution(* com.tingnichui.dao.*.*(..))")
    public Object daoAround(ProceedingJoinPoint point) throws Throwable {
        //方法名
        MethodSignature method = (MethodSignature) point.getSignature();
        StringBuilder className = new StringBuilder();
        className.append(method.getDeclaringTypeName());
        className.append(".");
        className.append(method.getName());
        long currentTimeMillis = System.currentTimeMillis();
        Object obj = null;
        try {
            obj = point.proceed();
            return obj;
        } finally {
            long diffTimeMillis = System.currentTimeMillis() - currentTimeMillis;
            Object[] args = point.getArgs();
            String requestLog = JSON.toJSONString(args);//入参
            String responseLog = obj == null ? null : JSON.toJSONString(obj);//出参
            log.info("dao." + method.getName() + "|耗时={}|入参={}，出参={}|" + className,diffTimeMillis,requestLog,responseLog);

        }

    }
}
