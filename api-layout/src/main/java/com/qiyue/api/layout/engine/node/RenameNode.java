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
public class RenameNode extends AbstractNode {
    private String nodeId;
    private List<String> keyList = new ArrayList<>();
    private List<String> renameToList = new ArrayList<>();

    public static RenameNode assemble(JSONObject jsonObject) {
        RenameNode mergeNode = new RenameNode();
        AbstractNode.assemble(mergeNode, jsonObject);
        mergeNode.setNodeId(fetchString(jsonObject, "nodeId"));
        mergeNode.getKeyList().addAll(fetchStringList(jsonObject, "keyList"));
        mergeNode.getRenameToList().addAll(fetchStringList(jsonObject, "renameToList"));
        return mergeNode;
    }

    @Override
    public void execute(Map<String, Object> inputMap, Map<String, Object> outputMap) {
        JSONObject superNodeResult = (JSONObject) outputMap.get(this.nodeId);
        JSONObject output = new JSONObject(16);
        int num = Math.min(this.keyList.size(), this.renameToList.size());
        List<String> hasRenamedList = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            output.put(renameToList.get(i), superNodeResult.get(this.keyList.get(i)));
            hasRenamedList.add(this.keyList.get(i));
        }
        superNodeResult.forEach((k, v) -> {
            if (!hasRenamedList.contains(k)) {
                output.put(k, v);
            }
        });
        log.info("rename node nodeId:{}, keyList:{}, renameToList:{}, output:{}", this.nodeId, this.keyList, this.renameToList, output);
        outputMap.put(this.getId(), output);
    }
}
