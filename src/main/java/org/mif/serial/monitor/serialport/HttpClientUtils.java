package org.mif.serial.monitor.serialport;


import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.mif.serial.monitor.Constants;
import org.mif.serial.monitor.vo.EquipmentVO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HttpClientUtils {

    private final int DEFAULT_TIMEOUT = 30000;


    private static HttpClientUtils ins;

    private HttpClient client;
    private int timeout = DEFAULT_TIMEOUT;

    private static int maxConnTotal = 200;   //最大不要超过1000
    private static int maxConnPerRoute = 100;//实际的单个连接池大小，

    private HttpClientUtils() {
        if (client == null) {
            client = HttpClients.createDefault();
        }
    }

    public static HttpClientUtils getInstance() {
        if (ins == null) {
            synchronized (HttpClientUtils.class) {
                if (ins == null) {
                    ins = new HttpClientUtils();
                }
            }
        }
        return ins;
    }


    public String doGetWithJsonResult(String uri) {
        String json = null;
        HttpResponse response = null;
        try {
            HttpGet request = new HttpGet(uri);
            RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(timeout)
                    .setConnectTimeout(timeout).setSocketTimeout(timeout).build();
            request.setConfig(config);
            response = client.execute(request);
            System.out.println("Response status code: " + response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (response != null) {
                    EntityUtils.consume(response.getEntity());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return json;
    }


    public String doPostWithJsonResult(String uri, Map<String, String> paramMap) {
        String json = null;
        HttpResponse response = null;
        try {
            HttpPost request = new HttpPost(uri);
            RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(timeout)
                    .setConnectTimeout(timeout).setSocketTimeout(timeout).build();
            request.setConfig(config);
            List<NameValuePair> params = new ArrayList<NameValuePair>(0);
            if (paramMap != null && !paramMap.isEmpty()) {
                for (String key : paramMap.keySet()) {
                    params.add(new BasicNameValuePair(key, paramMap.get(key)));
                }
                request.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            }
            response = client.execute(request);
            System.out.println("Response status code: " + response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                System.out.println("Payload : " + json);
            }
            request.releaseConnection();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (response != null) {
                    EntityUtils.consume(response.getEntity());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return json;
    }


    public String doPost(String url, String jsonStr) {
        URL u = null;
        HttpURLConnection con = null;

        try {
            u = new URL(url);
            con = (HttpURLConnection) u.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);
            //application/x-www-form-urlencoded
            //con.setRequestProperty("content-type", "*/*");
            con.setConnectTimeout(timeout);
            con.setReadTimeout(timeout);
            //con.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            if (jsonStr != null && !"".equals(jsonStr)) {
                OutputStreamWriter osw = new OutputStreamWriter(
                        con.getOutputStream(), "UTF-8");
                System.out.println("即将发送参数:" + jsonStr);
                osw.write(jsonStr);
                osw.flush();
                osw.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        // 读取返回内容
        StringBuffer buffer = new StringBuffer();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(
                    con.getInputStream(), "UTF-8"));
            String temp = "";
            while ((temp = br.readLine()) != null) {
                buffer.append(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String result = buffer.toString();
        System.out.println("Payload: " + result);
        return result;
    }

    /**
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String doPost(String url, String jsonStr, int timeout) {
        setTimeout(timeout);
        return doPost(url, jsonStr);
    }


    public List<EquipmentVO> getPlcList() {
        String result = doPost(Constants.REMOTE_HTTP + "/api/plcList", null, 3000);
        System.out.println("result=" + result);
        List<EquipmentVO> equipmentVOS = JSON.parseArray(result, EquipmentVO.class);
        if (CollectionUtils.isEmpty(equipmentVOS)) {
            return null;
        }
        return equipmentVOS;
    }

    public EquipmentVO getPlcDetail(String equipNo) {
        Map map = new HashMap();
        map.put("equipNo", equipNo);

        String result = doPostWithJsonResult(Constants.REMOTE_HTTP + "/api/plcDetail", map);
        System.out.println("result=" + result);
        EquipmentVO equipmentVO = JSON.parseObject(result, EquipmentVO.class);
        return equipmentVO;
    }

    public String login(String userName, String password) {
        Map map = new HashMap();
        map.put("userName", userName);
        map.put("password", password);
        String result = doPostWithJsonResult(Constants.REMOTE_HTTP + "/api/login", map);
        System.out.println("login result =" + result);
        return result;
    }
}
