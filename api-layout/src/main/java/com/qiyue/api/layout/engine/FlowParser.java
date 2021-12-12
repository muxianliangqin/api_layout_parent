package com.qiyue.api.layout.engine;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qiyue.api.layout.engine.node.AbstractNode;
import com.qiyue.api.layout.engine.node.AsyncNode;
import com.qiyue.api.layout.engine.node.SyncNode;
import com.qiyue.api.layout.model.param.ApiLayoutParam;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Slf4j
public class FlowParser {

    public static final String PREFIX_SQUARE_BRACKET = "[";
    public static final String PREFIX_COMMA = ",";
    public static final String SUFFIX_SQUARE_BRACKET = "]";
    public static final Pattern START_PATTERN = Pattern.compile("[(\\[,]");
    public static final Pattern END_PATTERN = Pattern.compile("[)\\]]");

    public static AbstractNode parse(JSONObject jsonObject) {
        AbstractNode node = parseNode(jsonObject);
        parseChildren(node, jsonObject);
        return node;
    }

    public static AbstractNode parseNode(JSONObject jsonObject) {
        String type = jsonObject.getString("type");
        Function<JSONObject, AbstractNode> function = NodeTypeEnum.getFunction(type);
        if (Objects.nonNull(function)) {
            return function.apply(jsonObject);
        }
        return null;
    }

    public static void parseChildren(AbstractNode parent, JSONObject jsonObject) {
        if (jsonObject.containsKey("children")) {
            JSONArray jsonArray = jsonObject.getJSONArray("children");
            jsonArray.forEach(k -> parseSub(parent, jsonObject));
        }
    }

    public static void parseSub(AbstractNode parent, JSONObject jsonObject) {
        AbstractNode node = parseNode(jsonObject);
        parent.getChildren().add(node);
        parseChildren(node, jsonObject);
    }

    public static AbstractNode parseFromFrontParam(ApiLayoutParam apiLayoutParam) {
        String rule = apiLayoutParam.getRule();
        Map<String, Map<String, Object>> nodeMap = apiLayoutParam.assembleNodeMap();
        AsyncNode root = new AsyncNode();
        root.setId(apiLayoutParam.getId());
        root.setDesc(apiLayoutParam.getDesc());
        root.setType("async");
        Matcher startMatcher = START_PATTERN.matcher(rule);
        Matcher endMatcher = END_PATTERN.matcher(rule);
        ParseResult parseResult = new ParseResult(0, false, false);
        parseRule(rule, root, nodeMap, startMatcher, endMatcher, parseResult);
        return root;
    }

    public static ParseResult parseRule(String rule, AbstractNode parent, Map<String, Map<String, Object>> nodeMap,
                                        Matcher startMatcher, Matcher endMatcher, ParseResult parseResult) {
        int start = parseResult.getIndex();
        while (startMatcher.find(start) && endMatcher.find(start)) {
            if (parseResult.isSyncEnd()) {
                parseResult.setSync(false);
                parseResult.setSyncEnd(false);
                return parseResult;
            }
            if (parseResult.isSync()) {
                AbstractNode asyncNode = new AsyncNode();
                asyncNode.setId("async" + start);
                asyncNode.setType("async");
                asyncNode.setDesc("async node");
                parent.getChildren().add(asyncNode);
                ParseResult parseResult1 = new ParseResult(startMatcher.start(), false, false);
                parseResult = parseRule(rule, asyncNode, nodeMap, startMatcher, endMatcher, parseResult1);
                start = parseResult.getIndex();
                continue;
            }
            String suffixSymbol = startMatcher.group();
            String prefixSymbol = endMatcher.group();
            if (PREFIX_COMMA.equals(suffixSymbol)) {
                if (parseResult.isSync()) {
                    parseResult.setIndex(startMatcher.end());
                    parseResult.setSyncEnd(true);
                    return parseResult;
                } else {
                    start = startMatcher.end();
                    continue;
                }
            }
            if (SUFFIX_SQUARE_BRACKET.equals(prefixSymbol)) {
                parseResult.setSyncEnd(true);
                parseResult.setIndex(endMatcher.end());
                return parseResult;
            }
            if (PREFIX_SQUARE_BRACKET.equals(suffixSymbol)) {
                AbstractNode syncNode = new SyncNode();
                syncNode.setId("sync" + start);
                syncNode.setType("sync");
                syncNode.setDesc("sync node");
                parent.getChildren().add(syncNode);
                ParseResult parseResult1 = new ParseResult(startMatcher.end(), true, false);
                parseResult = parseRule(rule, syncNode, nodeMap, startMatcher, endMatcher, parseResult1);
                start = parseResult.getIndex();
                continue;
            }
            int subStart = startMatcher.end();
            int subEnd = endMatcher.start();
            String id = rule.substring(subStart, subEnd);
            AbstractNode node = assemble(nodeMap, id);
            parent.getChildren().add(node);
            start = endMatcher.end();
        }
        parseResult.setIndex(start);
        return parseResult;
    }

    public static AbstractNode assemble(Map<String, Map<String, Object>> nodeMap, String id) {
        Assert.isTrue(nodeMap.containsKey(id), "nodeMap must has key:" + id);
        Map<String, Object> nodeData = nodeMap.get(id);
        Assert.isTrue(nodeData.containsKey("type"), "nodeData must has key:type");
        Function<JSONObject, AbstractNode> function = NodeTypeEnum.getFunction((String) nodeData.get("type"));
        Assert.notNull(function, "type:" + nodeData.get("type") + " must identity assemble function");
        return function.apply(JSON.parseObject(JSON.toJSONString(nodeData)));
    }

    @Data
    static class ParseResult {
        private int index;
        private boolean isSync;
        private boolean syncEnd;

        public ParseResult(int index, boolean isSync, boolean syncEnd) {
            this.index = index;
            this.isSync = isSync;
            this.syncEnd = syncEnd;
        }
    }

    public static String parseFromFile(String filePath) throws IOException {
        File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + filePath);
        return readFromFile(file);
    }

    public static String readFromFile(File file) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int index;
            while ((index = fileInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, index);
            }
            return byteArrayOutputStream.toString("UTF-8");
        }
    }

    public static void main(String[] args) throws IOException {
        String jsonStr = FlowParser.parseFromFile("example1.json");
        ApiLayoutParam apiLayoutParam = JSON.parseObject(jsonStr, ApiLayoutParam.class);
        FlowExecutor flowExecutor = new FlowExecutor(apiLayoutParam, true);
        try {
            flowExecutor.execute();
            Object res = flowExecutor.getResult();
            log.info("res:{}", res);
        } finally {
            flowExecutor.shutdown();
        }
    }
}
