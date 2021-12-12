package com.qiyue.api.layout.engine;

import com.qiyue.api.layout.engine.node.AbstractNode;
import com.qiyue.api.layout.engine.node.SyncNode;
import com.qiyue.api.layout.model.param.ApiLayoutParam;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

@Data
@Slf4j
public class FlowExecutor {
    private ApiLayoutParam apiLayoutParam;
    private Map<String, Object> inputMap = new ConcurrentHashMap<>();
    private Map<String, Object> outputMap = new ConcurrentHashMap<>(32);
    private AbstractNode node;
    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();

    private static final ThreadPoolExecutor SYNC_EXECUTOR = new ThreadPoolExecutor(PROCESSORS + 1, PROCESSORS * 4,
            10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10),
            new CustomizableThreadFactory(" sync-pool-thread-"));

    public FlowExecutor(ApiLayoutParam apiLayoutParam, boolean reparse) {
        this.apiLayoutParam = apiLayoutParam;
        String id = apiLayoutParam.getId();
        if (Objects.isNull(this.node) || reparse || !id.equals(node.getId())) {
            this.node = FlowParser.parseFromFrontParam(apiLayoutParam);
        }
        this.getInputMap(apiLayoutParam.assembleNodeMap());
    }

    public void execute() {
        this.node.invoke(inputMap, outputMap);
    }

    public static void executeSync(SyncNode syncNode, Map<String, Object> inputMap,
                                   Map<String, Object> outputMap) {
        CompletableFuture<?>[] futures = new CompletableFuture[syncNode.getChildren().size()];
        for (int i = 0; i < syncNode.getChildren().size(); i++) {
            AbstractNode node = syncNode.getChildren().get(i);
            CompletableFuture<?> future = CompletableFuture.runAsync(
                    () -> node.invoke(inputMap, outputMap),
                    SYNC_EXECUTOR);
            futures[i] = future;
        }
        try {
            CompletableFuture.allOf(futures).get(3, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public void shutdown() {
        SYNC_EXECUTOR.shutdown();
    }

    public void getInputMap(Map<String, Map<String, Object>> nodeMap) {
        nodeMap.forEach((k, v) -> {
            if (v.containsKey("input")) {
                this.inputMap.put(k, v.get("input"));
            }
        });
    }

    public Object getResult() {
        AbstractNode lastNode = this.node;
        while (CollectionUtils.isNotEmpty(lastNode.getChildren())) {
            lastNode = lastNode.getChildren().get(lastNode.getChildren().size() - 1);
        }
        return outputMap.get(lastNode.getId());
    }
}
