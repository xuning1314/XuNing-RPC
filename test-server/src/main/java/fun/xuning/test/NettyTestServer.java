//package fun.xuning.test;
//
//import fun.xuning.rpc.api.HelloService;
//import fun.xuning.rpc.registry.DefaultServiceRegistry;
//import fun.xuning.rpc.registry.ServiceRegistry2;
//import fun.xuning.rpc.transport.netty.server.NettyServer;
//
//public class NettyTestServer {
//    public static void main(String[] args) {
//        HelloService helloService = new HelloServiceImpl();
//        ServiceRegistry2 registry = new DefaultServiceRegistry();
//        registry.register(helloService);
//        NettyServer server = new NettyServer();
//        server.start(9999);
//    }
//}
