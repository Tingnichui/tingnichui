package com.tingnichui.interceptor;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.tingnichui.annotation.RedisLock;
import com.tingnichui.util.DingdingUtil;
import com.tingnichui.util.RedisUtil;
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
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Aspect
@Component
public class AOP {

    @Resource
    private RedisUtil redisUtil;

    /**
     * redis锁
     * @param point
     * @return
     */
    @Around("@annotation(com.tingnichui.annotation.RedisLock))")
    public Object redisLock(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        RedisLock redisLock = method.getAnnotation(RedisLock.class);
        String redisKey = redisLock.key();
        // 加锁
        boolean lock = false;
        try {
            lock = redisUtil.setCacheObject(redisKey, redisLock.value(), redisLock.expire(), redisLock.timeUnit());
            if (!lock) {
                return ResultGenerator.genFailResult("请稍后再试");
            }
            Object proceed = point.proceed();
            return proceed;
        } catch (Throwable throwable) {
            log.error(method.getName() + "执行异常",throwable);
            return ResultGenerator.genFailResult("系统异常");
        } finally {
            if (lock) {
                redisUtil.deleteObject(redisKey);
            }
        }
    }


    /**
     * controller日志
     **/
    @Around("execution(* com.tingnichui.controller.*.*(..))")
    public Object controllerAround(ProceedingJoinPoint point) {
        //执行链条ID
        MDC.put("processId", IdUtil.simpleUUID());
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, String[]> parameter = request.getParameterMap();
        Map<String, String> parameterMap = new HashMap<>(16);

        if (parameter.isEmpty()) {
            parameterMap = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        } else {
            for (String key : parameter.keySet()) {
                parameterMap.put(key, StringUtils.join(parameter.get(key), ","));
            }
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
        long startTime = System.currentTimeMillis();
        Object obj = null;
        try {
            obj = point.proceed();
            return obj;
        } catch (Throwable throwable) {
            log.error("CONTROLLER异常", throwable);
            DingdingUtil.sendMsg(DateUtil.now() + "-CONTROLLER异常");
            return ResultGenerator.genErrorResult("B001", "系统错误");
        } finally {
            long diffTimeMillis = System.currentTimeMillis() - startTime;
            // 出参日志
            String responseLog = obj == null ? null : JSON.toJSONString(obj);
            log.info("controller." + method.getName() + "|耗时={}|入参={}，出参={}|" + className, diffTimeMillis, requestLog, responseLog);
        }

    }

    /**
     * service日志
     **/
    @Around("execution(* com.tingnichui.service.*.*(..))")
    public Object serviceAround(ProceedingJoinPoint point) {
        //执行链条ID
        MDC.put("processId", IdUtil.simpleUUID());
        Object[] args = point.getArgs();
        //入参日志
        String requestLog = JSON.toJSONString(args);
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
            log.error("SERVICE异常", throwable);
            DingdingUtil.sendMsg(DateUtil.now() + "-SERVICE异常");
            return ResultGenerator.genErrorResult("B001", "系统错误");
        } finally {
            long diffTimeMillis = System.currentTimeMillis() - currentTimeMillis;
            // 出参日志
            String responseLog = obj == null ? null : JSON.toJSONString(obj);
            log.info("service." + method.getName() + "|耗时={}|入参={}，出参={}|" + className, diffTimeMillis, requestLog, responseLog);
        }
    }

    /**
     * dao日志
     **/
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
            log.info("dao." + method.getName() + "|耗时={}|入参={}，出参={}|" + className, diffTimeMillis, requestLog, responseLog);

        }

    }
}
