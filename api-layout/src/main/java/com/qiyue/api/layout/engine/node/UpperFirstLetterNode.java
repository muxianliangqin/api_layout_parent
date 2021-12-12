package com.qiyue.api.layout.engine.node;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class UpperFirstLetterNode extends AbstractNode {
    private String nodeId;

    public static UpperFirstLetterNode assemble(JSONObject jsonObject) {
        UpperFirstLetterNode upperFirstLetterNode = new UpperFirstLetterNode();
        AbstractNode.assemble(upperFirstLetterNode, jsonObject);
        upperFirstLetterNode.setNodeId(fetchString(jsonObject, "nodeId"));
        return upperFirstLetterNode;
    }

    @Override
    public void execute(Map<String, Object> inputMap, Map<String, Object> outputMap) {
        Object superNodeResult = outputMap.get(this.nodeId);
        JSONObject output = new JSONObject(16);
        if (superNodeResult instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) superNodeResult;
            jsonObject.forEach((k, v) -> output.put(upperFirstLetter(k), v));
        }
        log.info("select node nodeId:{}, output:{}", this.nodeId, output);
        outputMap.put(this.getId(), output);
    }

    public static String upperFirstLetter(String s) {
        char[] chars = s.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return String.valueOf(chars);
    }

}
