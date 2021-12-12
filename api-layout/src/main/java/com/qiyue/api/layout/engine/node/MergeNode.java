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
public class MergeNode extends AbstractNode {
    private List<String> nodeIdList = new ArrayList<>();

    public static MergeNode assemble(JSONObject jsonObject) {
        MergeNode mergeNode = new MergeNode();
        AbstractNode.assemble(mergeNode, jsonObject);
        mergeNode.getNodeIdList().addAll(fetchStringList(jsonObject, "nodeIdList"));
        return mergeNode;
    }

    @Override
    public void execute(Map<String, Object> inputMap, Map<String, Object> outputMap) {
        JSONObject output = new JSONObject(16);
        this.nodeIdList.forEach(k -> {
            if (outputMap.containsKey(k)) {
                JSONObject nodeResult = (JSONObject) outputMap.get(k);
                output.putAll(nodeResult);
            }
        });
        log.info("merge node input:{}, output:{}", nodeIdList, output);
        outputMap.put(this.getId(), output);
    }
}
