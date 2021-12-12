package com.qiyue.api.layout.engine.node;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class HttpNode extends AbstractNode {

    public static final Pattern PLACEHOLDER = Pattern.compile("(\\{\\w+})");

    private String url;
    private String method;

    public HttpNode() {
        super();
        this.method = "get";
    }

    public static HttpNode assemble(JSONObject jsonObject) {
        HttpNode httpNode = new HttpNode();
        AbstractNode.assemble(httpNode, jsonObject);
        httpNode.setUrl(fetchString(jsonObject, "url"));
        if (jsonObject.containsKey("method")) {
            httpNode.setMethod(fetchString(jsonObject, "method"));
        } else {
            httpNode.setMethod("get");
        }
        return httpNode;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Map<String, Object> inputMap, Map<String, Object> outputMap) {
        Map<String, Object> input = (Map<String, Object>) inputMap.get(this.getId());
        if (input.containsKey("$nodeId")) {
            String nodeId = (String) input.get("$nodeId");
            Map<String, Object> superNodeResult = (Map<String, Object>) outputMap.get(nodeId);
            if (Objects.isNull(superNodeResult)) {
                log.error("outputMap not exists this key:{}", input);
                return;
            }
            input.putAll(superNodeResult);
        }
        Object output = http(input);
        log.info("http node input:{}, output:{}", input, output);
        outputMap.put(this.getId(), output);
    }

    public Object http(Map<String, Object> input) {
        String result = null;
        if ("get".equals(this.method)) {
            String getUrl = replace(this.url, input);
            Request request = new Request.Builder().url(getUrl).get().build();
            OkHttpClient okHttpClient = new OkHttpClient().newBuilder().build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                ResponseBody body = response.body();
                if (Objects.isNull(body)) {
                    log.error("http:get:{} response is null", getUrl);
                    throw new IllegalArgumentException("http:get:{} response is null" + getUrl);
                }
                result = body.string();
            } catch (IOException e) {
                e.printStackTrace();
                log.error("request http[get]:" + getUrl + "throw exception:" + ExceptionUtils.getMessage(e));
            }
        }
        return JSON.parse(result);
    }

    public String replace(String getUrl, Map<String, Object> input) {
        Matcher matcher = PLACEHOLDER.matcher(getUrl);
        while (matcher.find()) {
            String repl = matcher.group();
            String key = repl.substring(1, repl.length() - 1);
            if (input.containsKey(key)) {
                getUrl = getUrl().replace(repl, (String) input.get(key));
            } else {
                log.warn("Placeholder:{} has no replacement", key);
            }
        }
        return getUrl;
    }
}
