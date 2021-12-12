package com.qiyue.api.layout.engine.node;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Data
public abstract class AbstractNode {

    private String id;
    private String desc;
    private String type;
    private List<AbstractNode> children;

    protected AbstractNode() {
        this.children = new ArrayList<>();
    }

    public static void assemble(AbstractNode abstractNode, JSONObject jsonobject) {
        abstractNode.setId(jsonobject.getString("id"));
        abstractNode.setDesc(jsonobject.getString("desc"));
        abstractNode.setType(jsonobject.getString("type"));
    }

    public static String fetchString(JSONObject jsonObject, String key) {
        String value = jsonObject.getString(key);
        Assert.notNull(value, "jsonObject must has key:" + key);
        return value;
    }

    public static List<String> fetchStringList(JSONObject jsonObject, String key) {
        JSONArray value = jsonObject.getJSONArray(key);
        Assert.notNull(value, "jsonObject must has key:" + key);
        return value.stream().map(Object::toString).collect(Collectors.toList());
    }

    public abstract void execute(Map<String, Object> inputMap,
                                 Map<String, Object> outputMap);

    public void executeChildren(Map<String, Object> inputMap,
                                Map<String, Object> outputMap) {
        this.getChildren().forEach(k -> k.execute(inputMap, outputMap));
    }

    public void invoke(Map<String, Object> inputMap,
                       Map<String, Object> outputMap) {
        try {
            this.execute(inputMap, outputMap);
        } catch (Exception e) {
            log.error("nodeId:{}, desc:{} \nException:{} ", this.getId(), this.getDesc(), ExceptionUtils.getMessage(e));
        }
        this.executeChildren(inputMap, outputMap);

    }
}
