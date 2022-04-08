package fun.xuning.rpc.transport.netty.client;

import fun.xuning.rpc.entity.RpcRequest;
import fun.xuning.rpc.entity.RpcResponse;
import fun.xuning.rpc.enumeration.RpcError;
import fun.xuning.rpc.exception.RpcException;
import fun.xuning.rpc.factory.SingletonFactory;
import fun.xuning.rpc.loadbalancer.LoadBalancer;
import fun.xuning.rpc.loadbalancer.RandomLoadBalancer;
import fun.xuning.rpc.registry.NacosServiceDiscovery;
import fun.xuning.rpc.registry.ServiceDiscovery;
import fun.xuning.rpc.serializer.CommonSerializer;
import fun.xuning.rpc.transport.RpcClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public class NettyClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private static final EventLoopGroup group;
    private static final Bootstrap bootstrap;

    //初始化EventLoopGroup, Bootstrap等资源
    static {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class);
    }

    private final ServiceDiscovery serviceDiscovery;
    private final CommonSerializer serializer;

    private final UnprocessedRequests unprocessedRequests;

    public NettyClient() {
        this(DEFAULT_SERIALIZER, new RandomLoadBalancer());
    }
    public NettyClient(LoadBalancer loadBalancer) {
        this(DEFAULT_SERIALIZER, loadBalancer);
    }
    public NettyClient(Integer serializer) {
        this(serializer, new RandomLoadBalancer());
    }
    public NettyClient(Integer serializer, LoadBalancer loadBalancer) {
        this.serviceDiscovery = new NacosServiceDiscovery(loadBalancer);
        this.serializer = CommonSerializer.getByCode(serializer);
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    //发送request
    @Override
    public CompletableFuture<RpcResponse> sendRequest(RpcRequest rpcRequest) {
        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        // 返回值
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
        try {
            // 通过 rpcRequest的信息 从lookupService方法中向nacos获得服务端地址
            InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
            // 通过ChannelProvider类与参数inetSocketAddress, serializer（服务地址，序列化方式）获得channel
            //channel可以进行网络传输操作
            Channel channel = ChannelProvider.get(inetSocketAddress, serializer);
            if (!channel.isActive()) {
                group.shutdownGracefully();
                return null;
            }
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            //channel将request写给服务器
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future1 -> {
                if (future1.isSuccess()) {
                    logger.info(String.format("客户端发送消息: %s", rpcRequest.toString()));
                } else {
                    future1.channel().close();
                    resultFuture.completeExceptionally(future1.cause());
                    logger.error("发送消息时有错误发生: ", future1.cause());
                }
            });
        } catch (InterruptedException e) {
            unprocessedRequests.remove(rpcRequest.getRequestId());
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        return resultFuture;
    }

}