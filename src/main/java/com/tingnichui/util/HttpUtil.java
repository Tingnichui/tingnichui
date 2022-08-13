package com.tingnichui.util;

import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtil {

    /**
     * 最大连接数400
     */
    private static int MAX_CONNECTION_NUM = 800;

    /**
     * 单路由最大连接数80
     */
    private static int MAX_PER_ROUTE = 800;

    /**
     * 向连接池请求连接超时时间设置(单位:毫秒)
     */
    private static int DEFAULT_REQUEST_TIME_OUT = 60000;

    /**
     * 服务端响应超时时间设置(单位:毫秒)
     */
    private static int DEFAULT_CONNECT_TIME_OUT = 60000;

    /**
     * SOCKET连接超时(单位:毫秒)
     */
    private static int DEFAULT_SOCKET_TIME_OUT = 60000;

    private static Object LOCAL_LOCK = new Object();

    /**
     * 连接池管理对象
     */
    private static PoolingHttpClientConnectionManager cm = null;

    /**
     * 功能描述: <br>
     * 初始化连接池管理对象
     *
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private static PoolingHttpClientConnectionManager getPoolManager() {

        synchronized (LOCAL_LOCK) {

            if (null == cm) {
                if (null == cm) {
                    SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
                    try {
                        sslContextBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                                sslContextBuilder.build());
                        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                                .<ConnectionSocketFactory>create().register("https", socketFactory)
                                .register("http", new PlainConnectionSocketFactory()).build();
                        cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
                        cm.setMaxTotal(MAX_CONNECTION_NUM);
                        cm.setDefaultMaxPerRoute(MAX_PER_ROUTE);
                    } catch (Exception e) {

                    }
                }
            }
        }

        return cm;
    }

    /**
     * getHttpClient
     */
    private static CloseableHttpClient getHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom().setRedirectsEnabled(false).setConnectTimeout(DEFAULT_CONNECT_TIME_OUT)
                .setConnectionRequestTimeout(DEFAULT_REQUEST_TIME_OUT).setSocketTimeout(DEFAULT_SOCKET_TIME_OUT)
                .build();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig)
                .setConnectionManager(getPoolManager()).build();
        return httpClient;
    }

    /**
     * getHttpClient 代理
     */
    private static CloseableHttpClient getHttpClient(HttpHost proxy) {
        RequestConfig requestConfig = RequestConfig.custom().setRedirectsEnabled(true).setProxy(proxy).setConnectTimeout(DEFAULT_CONNECT_TIME_OUT)
                .setConnectionRequestTimeout(DEFAULT_REQUEST_TIME_OUT).setSocketTimeout(DEFAULT_SOCKET_TIME_OUT)
                .build();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig)
                .setConnectionManager(getPoolManager()).build();
        return httpClient;
    }

    /**
     * getHttpClient 含有超时时间(单位：毫秒)
     */
    @SuppressWarnings("unused")
    private static CloseableHttpClient getHttpClient(int timeOut) {
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeOut)
                .setConnectionRequestTimeout(timeOut).setSocketTimeout(timeOut).build();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig)
                .setConnectionManager(getPoolManager()).build();
        return httpClient;
    }

    public static String get(String url) {

        String html = null;
        try {
            CloseableHttpClient httpClient = getHttpClient();
            HttpGet get = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(get);
            html = null;
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                html = EntityUtils.toString(entity, "UTF-8");
            }

            //释放连接
            EntityUtils.consume(entity);
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }

        return html;
    }


    public static String get(String url, String charset) throws Exception {

        CloseableHttpClient httpClient = getHttpClient();
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse response = httpClient.execute(get);
        String html = null;
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            html = EntityUtils.toString(entity, charset);
        }

        //释放连接
        EntityUtils.consume(entity);
        response.close();

        return html;
    }

    /**
     * 含代理
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static String get(String url, String host, int port) throws Exception {

        HttpHost proxy = new HttpHost(host, port);
        CloseableHttpClient httpClient = getHttpClient(proxy);
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse response = httpClient.execute(get);
        String respStr = null;
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            respStr = EntityUtils.toString(entity, "UTF-8");
        }

        //释放连接
        EntityUtils.consume(response.getEntity());
        response.close();

        return respStr;
    }

    /**
     * 含代理
     *
     * @param url
     * @param headers
     * @return
     * @throws Exception
     */
//    public static String get(String url, Map<String, String> headers, String hostPort) {
//
//        String[] proxys = hostPort.split(":");
//        HttpHost proxy = new HttpHost(proxys[0], Integer.parseInt(proxys[1]));
//        CloseableHttpClient httpClient = getHttpClient(proxy);
//        HttpGet get = new HttpGet(url);
//        //设置参数
//        for (String key : headers.keySet()) {
//            get.setHeader(key, headers.get(key));
//        }
//
//        String respStr = null;
//        try {
//            CloseableHttpResponse response = httpClient.execute(get);
//            respStr = null;
//            HttpEntity entity = response.getEntity();
//
//            if (entity != null) {
//                respStr = EntityUtils.toString(entity, "UTF-8");
//            }
//
//            //释放连接
//            EntityUtils.consume(response.getEntity());
//            response.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return respStr;
//    }

    public static String get(String url, Map<String, String> headers) throws Exception {

        CloseableHttpClient httpClient = getHttpClient();
        HttpGet get = new HttpGet(url);
        //设置参数
        for (String key : headers.keySet()) {
            get.setHeader(key, headers.get(key));
        }

        CloseableHttpResponse response = httpClient.execute(get);
        String respStr = null;
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            respStr = EntityUtils.toString(entity, "UTF-8");
        }

        //释放连接
        EntityUtils.consume(response.getEntity());
        response.close();

        return respStr;
    }

    public static String get(String url, Map<String, String> headers,String chartSet) {

        String respStr = null;
        try {
            CloseableHttpClient httpClient = getHttpClient();
            HttpGet get = new HttpGet(url);
            //设置参数
            for (String key : headers.keySet()) {
                get.setHeader(key, headers.get(key));
            }

            CloseableHttpResponse response = httpClient.execute(get);
            respStr = null;
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                respStr = EntityUtils.toString(entity, chartSet);
            }

            //释放连接
            EntityUtils.consume(response.getEntity());
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return respStr;
    }


    /**
     * @param url             地址
     * @param headers         消息头
     * @param responseHeaders 返回的消息头
     * @return
     * @throws Exception
     */
    public static String get(String url, Map<String, String> headers, List<Map<String, String>> responseHeaders) throws Exception {

        CloseableHttpClient httpClient = getHttpClient();
        HttpGet get = new HttpGet(url);

        //设置参数
        for (String key : headers.keySet()) {
            get.setHeader(key, headers.get(key));
        }

        CloseableHttpResponse response = httpClient.execute(get);

        if (responseHeaders != null) {
            for (Header h : response.getAllHeaders()) {
                Map<String, String> m = new HashMap<>();
                m.put(h.getName(), h.getValue());
                responseHeaders.add(m);
            }
        }

        String respStr = null;
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            respStr = EntityUtils.toString(entity, "UTF-8");
        }

        //释放连接
        EntityUtils.consume(response.getEntity());
        response.close();

        return respStr;
    }

    public static String post(String url) throws Exception {

        CloseableHttpResponse response = null;
        String respStr = null;

        try {

            CloseableHttpClient httpClient = getHttpClient();
            HttpPost post = new HttpPost(url);
            response = httpClient.execute(post);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                respStr = EntityUtils.toString(entity, "UTF-8");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                //释放连接
                EntityUtils.consume(response.getEntity());
                //response.close();
            }
        }

        return respStr;

    }


    /**
     * @param url     地址
     * @param content 发送内容
     * @return
     * @throws Exception
     */
    public static String post(String url, String content) throws Exception {

        CloseableHttpClient httpClient = getHttpClient();
        HttpPost post = new HttpPost(url);
        //设置参数
        StringEntity stringEntity = new StringEntity(content, "UTF-8");

        post.setEntity(stringEntity);
        CloseableHttpResponse response = httpClient.execute(post);
        String respStr = null;
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            respStr = EntityUtils.toString(entity, "UTF-8");
        }

        //释放连接
        EntityUtils.consume(response.getEntity());
        response.close();

        return respStr;
    }

    /**
     * @param url 地址
     * @return
     * @throws Exception
     */
    public static byte[] post4bytes(String url, List<Map<String, String>> responseHeaders) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        HttpPost post = new HttpPost(url);

        CloseableHttpResponse response = httpClient.execute(post);
        if (responseHeaders != null) {
            for (Header h : response.getAllHeaders()) {
                Map<String, String> m = new HashMap<>();
                m.put(h.getName(), h.getValue());
                responseHeaders.add(m);
            }
        }

        byte[] respBytes = null;
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            respBytes = EntityUtils.toByteArray(entity);
        }

        //释放连接
        EntityUtils.consume(response.getEntity());
        response.close();

        return respBytes;
    }

    /**
     * @param url     地址
     * @param content 发送内容
     * @return
     * @throws Exception
     */
    public static byte[] post4bytes(String url, String content) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        HttpPost post = new HttpPost(url);
        //设置参数
        StringEntity stringEntity = new StringEntity(content, "UTF-8");
        post.setEntity(stringEntity);
        CloseableHttpResponse response = httpClient.execute(post);
        byte[] respBytes = null;
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            respBytes = EntityUtils.toByteArray(entity);
        }

        //释放连接
        EntityUtils.consume(response.getEntity());
        response.close();

        return respBytes;
    }

    /**
     * @param url     地址
     * @param content 发送内容
     * @return
     * @throws Exception
     */
    public static byte[] get4bytes(String url, String content) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        HttpGet get = new HttpGet(url);
        //设置参数
        StringEntity stringEntity = new StringEntity(content, "UTF-8");
//		get.setEntity(stringEntity);
        CloseableHttpResponse response = httpClient.execute(get);
        byte[] respBytes = null;
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            respBytes = EntityUtils.toByteArray(entity);
        }

        //释放连接
        EntityUtils.consume(response.getEntity());
        response.close();

        return respBytes;
    }

    /**
     * @param url     地址
     * @param content 内容
     * @param headers 消息头
     * @return
     * @throws Exception
     */
    public static String post(String url, String content, Map<String, String> headers) {

        String respStr = null;
        try {
            CloseableHttpClient httpClient = getHttpClient();
            HttpPost post = new HttpPost(url);
            //设置参数
            StringEntity stringEntity = new StringEntity(content, "UTF-8");
            post.setEntity(stringEntity);
            for (String key : headers.keySet()) {
                post.setHeader(key, headers.get(key));
            }
            CloseableHttpResponse response = httpClient.execute(post);

            respStr = null;
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                respStr = EntityUtils.toString(entity, "UTF-8");
            }

            //释放连接
            EntityUtils.consume(response.getEntity());
            response.close();
        } catch (UnsupportedCharsetException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return respStr;
    }

    /**
     * @param url             地址
     * @param content         内容
     * @param headers         消息头
     * @param responseHeaders 返回的消息头
     * @return
     * @throws Exception
     */
    public static String post(String url, String content, Map<String, String> headers, List<Map<String, String>> responseHeaders) throws Exception {

        CloseableHttpClient httpClient = getHttpClient();
        HttpPost post = new HttpPost(url);
        //设置参数
        StringEntity stringEntity = new StringEntity(content, "UTF-8");
        post.setEntity(stringEntity);
        for (String key : headers.keySet()) {
            post.setHeader(key, headers.get(key));
        }

        CloseableHttpResponse response = httpClient.execute(post);

        if (responseHeaders != null) {
            for (Header h : response.getAllHeaders()) {
                Map<String, String> m = new HashMap<>();
                m.put(h.getName(), h.getValue());
                responseHeaders.add(m);
            }
        }

        String respStr = null;
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            respStr = EntityUtils.toString(entity, "UTF-8");
        }

        //释放连接
        EntityUtils.consume(response.getEntity());
        response.close();

        return respStr;
    }

    /**
     * post文件
     *
     * @param url
     * @return
     */
    public static String postFile(String url, String filePath) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        HttpPost post = new HttpPost(url);
        //设置参数
        HttpEntity requestEntity = MultipartEntityBuilder.create().addBinaryBody("file", new File(filePath)).build();
        post.setEntity(requestEntity);

        CloseableHttpResponse response = httpClient.execute(post);

        String respStr = null;
        HttpEntity responseEntity = response.getEntity();

        if (responseEntity != null) {
            respStr = EntityUtils.toString(responseEntity, "UTF-8");
        }

        //释放连接
        EntityUtils.consume(response.getEntity());
        response.close();

        return respStr;
    }

    /**
     * 带微信支付证书post 提现
     *
     * @param url
     * @param content
     * @return
     * @throws Exception
     */
    public static String postWxPay4Cash(String url, String content, String wxMchid, String apiclientCert) throws Exception {

        InputStream instream = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            instream = new ByteArrayInputStream(Files.readAllBytes(Paths.get(apiclientCert)));
            keyStore.load(instream, wxMchid.toCharArray());
            SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, wxMchid.toCharArray()).build();
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslcontext);
            CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();

            HttpPost post = new HttpPost(url);
            //设置参数
            StringEntity stringEntity = new StringEntity(content, "UTF-8");
            post.setEntity(stringEntity);
            CloseableHttpResponse response = httpClient.execute(post);
            String respStr = null;
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                respStr = EntityUtils.toString(entity, "UTF-8");
            }

            //释放连接
            EntityUtils.consume(response.getEntity());
            response.close();

            return respStr;

        } finally {
            if (instream != null) instream.close();
        }

    }


    /**
     * post文件
     *
     * @param url
     * @param file
     * @return
     */
    public static String postFile(String url, String file, FileBody fileBody, Map<String, String> headers) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        HttpPost post = new HttpPost(url);
        for (String key : headers.keySet()) {
            post.setHeader(key, headers.get(key));
        }

        //设置参数
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addPart(file, fileBody);
        HttpEntity reqEntity = builder.build();
        post.setEntity(reqEntity);
        CloseableHttpResponse response = httpClient.execute(post);

        String respStr = null;
        HttpEntity responseEntity = response.getEntity();

        if (responseEntity != null) {
            respStr = EntityUtils.toString(responseEntity, "UTF-8");
        }

        //释放连接
        EntityUtils.consume(response.getEntity());
        response.close();

        return respStr;
    }

    /**
     * post文件（携带参数）
     *
     * @param url
     * @param file
     * @param fileBody
     * @param headers
     * @param param
     * @return
     * @throws Exception
     */
    public static String postFile(String url, String file, FileBody fileBody, Map<String, String> headers, Map<String, String> param) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        HttpPost post = new HttpPost(url);
        for (String key : headers.keySet()) {
            post.setHeader(key, headers.get(key));
        }
        //设置参数
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        for (Map.Entry<String, String> key : param.entrySet()) {
            builder.addTextBody(key.getKey(), key.getValue());
        }
        builder.addPart(file, fileBody);
        HttpEntity reqEntity = builder.build();
        post.setEntity(reqEntity);
        CloseableHttpResponse response = httpClient.execute(post);

        String respStr = null;
        HttpEntity responseEntity = response.getEntity();

        if (responseEntity != null) {
            respStr = EntityUtils.toString(responseEntity, "UTF-8");
        }

        //释放连接
        EntityUtils.consume(response.getEntity());
        response.close();

        return respStr;
    }

    /**
     * yszf
     * 待优化
     */
    public static String sendPostFile(String url, Map<String, String> paramMap, File file) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        //如果返回403 增加一下代码模拟浏览器
        //httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:82.0) Gecko/20100101 Firefox/82.0");;

        CloseableHttpClient client = HttpClientBuilder.create().build();

        MultipartEntityBuilder entity = MultipartEntityBuilder.create()
                .setContentType(ContentType.MULTIPART_FORM_DATA)
                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .addBinaryBody("file", new FileInputStream(file), ContentType.DEFAULT_BINARY, file.getName()) //uploadFile对应服务端类的同名属性<File类型>
                .setCharset(Charset.forName("utf-8"));

        for (String key : paramMap.keySet()) {
            String value = paramMap.get(key);
            entity.addTextBody(key, value);
        }

        httpPost.setEntity(entity.build());
        HttpResponse httpResponse = client.execute(httpPost);

        HttpEntity resEntity = httpResponse.getEntity();
        return null == resEntity ? "" : EntityUtils.toString(resEntity, "utf-8");
    }

}
