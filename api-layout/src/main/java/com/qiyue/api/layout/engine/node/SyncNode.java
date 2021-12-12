package com.qiyue.api.layout.engine.node;

import com.alibaba.fastjson.JSONObject;
import com.qiyue.api.layout.engine.FlowExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Data
@Slf4j
public class SyncNode extends AbstractNode {

    public static SyncNode assemble(JSONObject jsonObject) {
        SyncNode syncNode = new SyncNode();
        AbstractNode.assemble(syncNode, jsonObject);
        return syncNode;
    }

    @Override
    public void execute(Map<String, Object> inputMap, Map<String, Object> outputMap) {
        log.debug("sync node parallel execute");
        FlowExecutor.executeSync(this, inputMap, outputMap);
    }

    @Override
    public void executeChildren(Map<String, Object> inputMap, Map<String, Object> outputMap) {
        log.debug("sync node's children have been executed");
    }
}
