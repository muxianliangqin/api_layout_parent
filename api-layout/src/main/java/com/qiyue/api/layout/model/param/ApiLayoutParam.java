package com.qiyue.api.layout.model.param;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ApiLayoutParam {
    private String rule;
    private String id;
    private String desc;
    private List<Map<String, Object>> nodeDataList = new ArrayList<>();

    public Map<String, Map<String, Object>> assembleNodeMap() {
        return nodeDataList.stream().collect(Collectors.toMap(k -> (String) k.get("id"), k -> k));
    }
}
