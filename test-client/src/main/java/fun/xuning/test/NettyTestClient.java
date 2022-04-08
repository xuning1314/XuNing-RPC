package fun.xuning.test;

import fun.xuning.rpc.api.ByeService;
import fun.xuning.rpc.api.HelloObject;
import fun.xuning.rpc.api.HelloService;
import fun.xuning.rpc.serializer.CommonSerializer;
import fun.xuning.rpc.transport.RpcClient;
import fun.xuning.rpc.transport.RpcClientProxy;
import fun.xuning.rpc.transport.netty.client.NettyClient;

public class NettyTestClient {

    public static void main(String[] args) {
        RpcClient client = new NettyClient(CommonSerializer.PROTOBUF_SERIALIZER);
        RpcClientProxy rpcClientProxy = new RpcClientProxy(client);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject(12, "This is a message");
        String res = helloService.hello(object);
        System.out.println(res);
        ByeService byeService = rpcClientProxy.getProxy(ByeService.class);
        System.out.println(byeService.bye("Netty"));
    }

}