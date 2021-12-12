package com.qiyue.api.layout.engine;

import com.alibaba.fastjson.JSONObject;
import com.qiyue.api.layout.engine.node.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public enum NodeTypeEnum {
    ASYNC("async", AsyncNode::assemble),
    SYNC("http", SyncNode::assemble),
    HTTP("http", HttpNode::assemble),
    MERGE("merge", MergeNode::assemble),
    SELECT("select", SelectNode::assemble),
    RENAME("rename", RenameNode::assemble),
    UPPER_FIRST_LETTER("upperFirstLetter", UpperFirstLetterNode::assemble),
    ;
    private static final Map<String, NodeTypeEnum> TYPE_MAP = new ConcurrentHashMap<>();

    static {
        Arrays.stream(NodeTypeEnum.values()).forEach(k -> TYPE_MAP.put(k.getType(), k));
    }

    private final String type;
    private final Function<JSONObject, AbstractNode> function;

    NodeTypeEnum(String type, Function<JSONObject, AbstractNode> function) {
        this.type = type;
        this.function = function;
    }

    public String getType() {
        return type;
    }

    public Function<JSONObject, AbstractNode> getFunction() {
        return function;
    }

    public static Function<JSONObject, AbstractNode> getFunction(String type) {
        NodeTypeEnum anEnum = TYPE_MAP.get(type);
        if (Objects.nonNull(anEnum)) {
            return anEnum.getFunction();
        }
        return null;
    }


}
