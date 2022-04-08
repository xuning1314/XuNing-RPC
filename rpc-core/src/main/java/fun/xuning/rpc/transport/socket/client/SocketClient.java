package fun.xuning.rpc.transport.socket.client;

import fun.xuning.rpc.entity.RpcRequest;
import fun.xuning.rpc.entity.RpcResponse;
import fun.xuning.rpc.enumeration.ResponseCode;
import fun.xuning.rpc.enumeration.RpcError;
import fun.xuning.rpc.exception.RpcException;
import fun.xuning.rpc.loadbalancer.LoadBalancer;
import fun.xuning.rpc.loadbalancer.RandomLoadBalancer;
import fun.xuning.rpc.registry.NacosServiceDiscovery;
import fun.xuning.rpc.registry.ServiceDiscovery;
import fun.xuning.rpc.serializer.CommonSerializer;
import fun.xuning.rpc.transport.RpcClient;
import fun.xuning.rpc.transport.socket.util.ObjectReader;
import fun.xuning.rpc.transport.socket.util.ObjectWriter;
import fun.xuning.rpc.util.RpcMessageChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Socket方式远程方法调用的消费者（客户端）
 *
 * @author xuning
 */
public class SocketClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private final ServiceDiscovery serviceDiscovery;

    private final CommonSerializer serializer;

    public SocketClient() {
        this(DEFAULT_SERIALIZER, new RandomLoadBalancer());
    }

    public SocketClient(LoadBalancer loadBalancer) {
        this(DEFAULT_SERIALIZER, loadBalancer);
    }

    public SocketClient(Integer serializer) {
        this(serializer, new RandomLoadBalancer());
    }

    public SocketClient(Integer serializer, LoadBalancer loadBalancer) {
        //创建 从nacos中找服务的 实例，并且指定找服务的负责均衡策略
        this.serviceDiscovery = new NacosServiceDiscovery(loadBalancer);
        this.serializer = CommonSerializer.getByCode(serializer);
    }

    //发送request，网络编程，先序列化，通过Socket将二进制文件传输给服务端
    //创建一个Socket，获取ObjectOutputStream对象，
    // 然后把需要发送的对象传进去即可，接收时获取ObjectInputStream对象，readObject()方法就可以获得一个返回的对象。
    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        //通过nacos查找服务信息，看是否存在服务
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            ObjectWriter.writeObject(outputStream, rpcRequest, serializer);//序列化并且写给客户端
            Object obj = ObjectReader.readObject(inputStream);
            RpcResponse rpcResponse = (RpcResponse) obj;
            if (rpcResponse == null) {
                logger.error("服务调用失败，service：{}", rpcRequest.getInterfaceName());
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            if (rpcResponse.getStatusCode() == null || !ResponseCode.SUCCESS.getCode().equals(rpcResponse.getStatusCode())) {
                logger.error("调用服务失败, service: {}, response:{}", rpcRequest.getInterfaceName(), rpcResponse);
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            RpcMessageChecker.check(rpcRequest, rpcResponse);
            return rpcResponse;//返回response
        } catch (IOException e) {
            logger.error("调用时有错误发生：", e);
            throw new RpcException("服务调用失败: ", e);
        }
    }

}