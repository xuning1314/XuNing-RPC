package fun.xuning.rpc.transport.socket.server;

import fun.xuning.rpc.factory.ThreadPoolFactory;
import fun.xuning.rpc.handler.RequestHandler;
import fun.xuning.rpc.hook.ShutdownHook;
import fun.xuning.rpc.provider.ServiceProviderImpl;
import fun.xuning.rpc.registry.NacosServiceRegistry;
import fun.xuning.rpc.serializer.CommonSerializer;
import fun.xuning.rpc.transport.AbstractRpcServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class SocketServer extends AbstractRpcServer {
    private final ExecutorService threadPool;
    private final CommonSerializer serializer;
    private final RequestHandler requestHandler = new RequestHandler();

    public SocketServer(String host, int port) {
        this(host, port, DEFAULT_SERIALIZER);
    }

    public SocketServer(String host, int port, Integer serializer) {
        this.host = host;
        this.port = port;
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
        this.serviceRegistry = new NacosServiceRegistry();
        this.serviceProvider = new ServiceProviderImpl();
        this.serializer = CommonSerializer.getByCode(serializer);
        scanServices();
    }

    //监听端口，获得客户端发来的request
    @Override
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(host, port));
            logger.info("服务器启动……");
            ShutdownHook.getShutdownHook().addClearAllHook();
            Socket socket;
            //将拿到的序列化的request给SocketRequestHandlerThread消费，其反序列化后其会调用requestHandler，其又会调用server本地服务并且返回结果
            while ((socket = serverSocket.accept()) != null) {
                logger.info("消费者连接: {}:{}", socket.getInetAddress(), socket.getPort());
                //创建一个线程执行，执行我们的逻辑
                threadPool.execute(new SocketRequestHandlerThread(socket, requestHandler, serializer));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("服务器启动时有错误发生:", e);
        }
    }
}
