package fun.xuning.rpc.transport.netty.server;

import fun.xuning.rpc.codec.CommonDecoder;
import fun.xuning.rpc.codec.CommonEncoder;
import fun.xuning.rpc.hook.ShutdownHook;
import fun.xuning.rpc.provider.ServiceProviderImpl;
import fun.xuning.rpc.registry.NacosServiceRegistry;
import fun.xuning.rpc.serializer.CommonSerializer;
import fun.xuning.rpc.transport.AbstractRpcServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class NettyServer extends AbstractRpcServer {

    private final CommonSerializer serializer;

    public NettyServer(String host, int port) {
        this(host, port, DEFAULT_SERIALIZER);
    }

    public NettyServer(String host, int port, Integer serializer) {
        this.host = host;
        this.port = port;
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
        this.serializer = CommonSerializer.getByCode(serializer);
        scanServices();
    }

    @Override
    public void start() {
        ShutdownHook.getShutdownHook().addClearAllHook();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //服务端辅助启动类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //将线程池放入辅助启动类实例
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)//通过工厂方法实例化一个通道channel
                    .handler(new LoggingHandler(LogLevel.INFO))
                    //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 256)
                    // 是否开启 TCP 底层心跳机制
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {//处理类handle
                            // 30 秒之内没有收到客户端请求的话就关闭连接
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS))
                                    .addLast(new CommonEncoder(serializer))//配置编码器（有序列化）
                                    .addLast(new CommonDecoder())//解码器（有反序列化）
                                    //NettyServerHandler 和 NettyClientHandler 都分别位于服务器端和客户端责任链的尾部，
                                    //直接和 RpcServer 对象或 RpcClient 对象打交道，而无需关心字节序列的情况。
                                    .addLast(new NettyServerHandler());//配置handle，handle处理接收到的request
                        }
                    });
            //绑定服务器，该实例提供有关io操作的结果和操作状态
            ChannelFuture future = serverBootstrap.bind(host, port).sync();
            // 等待服务端监听端口关闭
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            logger.error("启动服务器时有错误发生: ", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}