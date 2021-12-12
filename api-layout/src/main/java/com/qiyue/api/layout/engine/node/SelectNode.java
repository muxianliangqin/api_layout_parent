package com.qiyue.api.layout.engine.node;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class SelectNode extends AbstractNode {
    private String nodeId;
    private List<String> keyList = new ArrayList<>();

    public static SelectNode assemble(JSONObject jsonObject) {
        SelectNode selectNode = new SelectNode();
        AbstractNode.assemble(selectNode, jsonObject);
        selectNode.setNodeId(fetchString(jsonObject, "nodeId"));
        selectNode.getKeyList().addAll(fetchStringList(jsonObject, "keyList"));
        return selectNode;
    }

    @Override
    public void execute(Map<String, Object> inputMap, Map<String, Object> outputMap) {
        Object obj = outputMap.get(this.nodeId);
        JSONObject output = new JSONObject(16);
        if (obj instanceof JSONObject) {
            JSONObject superNodeResult = (JSONObject) obj;
            this.keyList.forEach(k -> {
                if (superNodeResult.containsKey(k)) {
                    output.put(k, superNodeResult.get(k));
                }
            });
        }
        log.info("select node nodeId:{}, keyList:{}, output:{}", this.nodeId, this.keyList, output);
        outputMap.put(this.getId(), output);
    }
}
