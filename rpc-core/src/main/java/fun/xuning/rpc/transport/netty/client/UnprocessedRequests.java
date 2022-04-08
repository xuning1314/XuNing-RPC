package fun.xuning.rpc.transport.netty.client;

import fun.xuning.rpc.entity.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UnprocessedRequests {
    //用于存放未被服务端处理的请求（建议限制 map 容器大小，避免未处理请求过多 OOM)。
    private static ConcurrentHashMap<String, CompletableFuture<RpcResponse>> unprocessedResponseFutures = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse> future) {
        unprocessedResponseFutures.put(requestId, future);
    }

    public void remove(String requestId) {
        unprocessedResponseFutures.remove(requestId);
    }

    public void complete(RpcResponse rpcResponse) {
        CompletableFuture<RpcResponse> future = unprocessedResponseFutures.remove(rpcResponse.getRequestId());
        if (null != future) {
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }

}

