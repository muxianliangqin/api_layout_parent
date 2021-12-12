package com.qiyue.api.layout.controller;

import com.alibaba.fastjson.JSON;
import com.qiyue.api.layout.dao.ApiTaskDao;
import com.qiyue.api.layout.engine.FlowExecutor;
import com.qiyue.api.layout.entity.ApiTaskEntity;
import com.qiyue.api.layout.model.param.ApiLayoutParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("apiLayout")
@RestController
public class ApiLayoutController {
    private final ApiTaskDao apiTaskDao;

    public ApiLayoutController(ApiTaskDao apiTaskDao) {
        this.apiTaskDao = apiTaskDao;
    }


    @PostMapping("run")
    public Object run(@RequestBody ApiLayoutParam apiLayoutParam) {
        FlowExecutor flowExecutor = new FlowExecutor(apiLayoutParam, true);
        flowExecutor.execute();
        Object res = flowExecutor.getResult();
        log.info("res:{}", res);
        return res;
    }

    @PostMapping("save")
    public ApiTaskEntity save(@RequestBody ApiLayoutParam apiLayoutParam) {
        ApiTaskEntity apiTaskEntity = apiTaskDao.findByTaskId(apiLayoutParam.getId()).orElseGet(ApiTaskEntity::new);
        apiTaskEntity.setTaskId(apiLayoutParam.getId());
        apiTaskEntity.setTaskDesc(apiLayoutParam.getDesc());
        apiTaskEntity.setTaskInfo(JSON.toJSONString(apiLayoutParam));
        return apiTaskDao.save(apiTaskEntity);
    }

    @GetMapping("apiTasks")
    public List<ApiTaskEntity> apiTasks() {
        return apiTaskDao.findAll();
    }
}
