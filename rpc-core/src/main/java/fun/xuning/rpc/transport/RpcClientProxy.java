package fun.xuning.rpc.transport;

import fun.xuning.rpc.entity.RpcRequest;
import fun.xuning.rpc.entity.RpcResponse;
import fun.xuning.rpc.transport.netty.client.NettyClient;
import fun.xuning.rpc.transport.socket.client.SocketClient;
import fun.xuning.rpc.util.RpcMessageChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RpcClientProxy implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);

    private final RpcClient client;

    public RpcClientProxy(RpcClient client) {
        this.client = client;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        logger.info("调用方法: {}#{}", method.getDeclaringClass().getName(), method.getName());
        //根据前面的信息（ip+端口+接口+参数等信息），将其封装为一个request对象
        RpcRequest rpcRequest = new RpcRequest(UUID.randomUUID().toString(), method.getDeclaringClass().getName(),
                method.getName(), args, method.getParameterTypes(), false);
        RpcResponse rpcResponse = null;
        //如果客户端是Netty
        if (client instanceof NettyClient) {
            try {
                //发送request，得到返回的response
                CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) client.sendRequest(rpcRequest);
                //获得保存在future中的服务器返回的数据
                rpcResponse = completableFuture.get();
            } catch (Exception e) {
                logger.error("方法调用请求发送失败", e);
                return null;
            }
        }
        //如果客户端是Socket
        if (client instanceof SocketClient) {
            //发送request，得到返回的response
            rpcResponse = (RpcResponse) client.sendRequest(rpcRequest);
        }
        RpcMessageChecker.check(rpcRequest, rpcResponse);
        return rpcResponse.getData();//返回response中的返回值数据
    }
}

