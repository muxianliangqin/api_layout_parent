package com.qiyue.api.layout.engine.node;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class AsyncNode extends AbstractNode {

    public static AsyncNode assemble(JSONObject jsonObject) {
        AsyncNode asyncNode = new AsyncNode();
        AbstractNode.assemble(asyncNode, jsonObject);
        return asyncNode;
    }

    @Override
    public void execute(Map<String, Object> inputMap, Map<String, Object> outputMap) {
        log.debug("async node nothing to do");
    }
}
