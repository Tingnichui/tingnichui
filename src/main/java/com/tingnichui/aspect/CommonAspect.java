package com.tingnichui.aspect;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.tingnichui.annotation.RedisLock;
import com.tingnichui.pojo.bo.WebLog;
import com.tingnichui.util.DingdingUtil;
import com.tingnichui.util.RedisUtil;
import com.tingnichui.util.ResultGenerator;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author  Geng Hui
 * @date  2022/9/21 17:25
 */
@Slf4j
@Aspect
@Component
public class CommonAspect {

    @Resource
    private RedisUtil redisUtil;

    @Pointcut("execution(* com.tingnichui.controller.*.*(..))")
    public void webLog() {
    }

    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MDC.put("processId", IdUtil.simpleUUID());
        long startTime = System.currentTimeMillis();
        //获取当前请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        //记录请求信息(通过Logstash传入Elasticsearch)
        WebLog webLog = new WebLog();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        webLog.setMethodSignature(signature.toString());
        if (method.isAnnotationPresent(ApiOperation.class)) {
            ApiOperation log = method.getAnnotation(ApiOperation.class);
            webLog.setDescription(log.value());
        }
        String urlStr = request.getRequestURL().toString();
        webLog.setBasePath(StrUtil.removeSuffix(urlStr, URLUtil.url(urlStr).getPath()));
        webLog.setUsername(request.getRemoteUser());
        webLog.setIp(request.getRemoteAddr());
        webLog.setMethod(request.getMethod());
        webLog.setParameter(getParameter(method, joinPoint.getArgs()));
        webLog.setResult(result);
        webLog.setSpendTime((int) (endTime - startTime));
        webLog.setStartTime(startTime);
        webLog.setUri(request.getRequestURI());
        webLog.setUrl(request.getRequestURL().toString());
//        Map<String,Object> logMap = new HashMap<>();
//        logMap.put("url",webLog.getUrl());
//        logMap.put("method",webLog.getMethod());
//        logMap.put("parameter",webLog.getParameter());
//        logMap.put("spendTime",webLog.getSpendTime());
//        logMap.put("description",webLog.getDescription());
        log.info("{}", JSONUtil.parse(webLog));
//        log.info(JSONUtil.parse(webLog).toString());
//        log.info(Markers.appendEntries(logMap), JSONUtil.parse(webLog).toString());
        return result;
    }

    /**
     * 根据方法和传入的参数获取请求参数
     */
    private Object getParameter(Method method, Object[] args) {
        List<Object> argList = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            //将RequestBody注解修饰的参数作为请求参数
            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
            if (requestBody != null) {
                argList.add(args[i]);
            }
            //将RequestParam注解修饰的参数作为请求参数
            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
            if (requestParam != null) {
                Map<String, Object> map = new HashMap<>();
                String key = parameters[i].getName();
                if (!StrUtil.isEmpty(requestParam.value())) {
                    key = requestParam.value();
                }
                map.put(key, args[i]);
                argList.add(map);
            }
        }
        if (argList.size() == 0) {
            return null;
        } else if (argList.size() == 1) {
            return argList.get(0);
        } else {
            return argList;
        }
    }


    /**
     * redis锁
     * @param point
     * @return
     */
    @Around("@annotation(com.tingnichui.annotation.RedisLock))")
    public Object redisLock(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        // 获取方法上的注解
        RedisLock redisLock = method.getAnnotation(RedisLock.class);
        String redisKey = redisLock.key();
        // 获取方法全路径、方法名、方法参数做为redis key
        if (StringUtils.isBlank(redisKey)) {
            StringBuilder methodInfo = new StringBuilder("RedisLock|");
            methodInfo.append(signature.getDeclaringTypeName());
            methodInfo.append(".");
            methodInfo.append(signature.getName());
            Object[] args = point.getArgs();
            for (int k = 0; k < args.length; k++) {
                if (!args[k].getClass().isPrimitive()) {
                    // 获取的是封装类型而不是基础类型
                    methodInfo.append(args[k].getClass().getName());
                }
            }
            redisKey = methodInfo.toString();
        }
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
            log.error(method.getName() + "执行异常", throwable);
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
//    @Around("execution(* com.tingnichui.controller.*.*(..))")
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
//    @Around("execution(* com.tingnichui.service.*.*(..))")
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
