package fun.xuning.test;

import fun.xuning.rpc.api.HelloObject;
import fun.xuning.rpc.api.HelloService;
import fun.xuning.rpc.serializer.CommonSerializer;
import fun.xuning.rpc.transport.RpcClientProxy;
import fun.xuning.rpc.transport.socket.client.SocketClient;

public class TestSocketClient {
    public static void main(String[] args) {

        SocketClient socketClient = new SocketClient(CommonSerializer.KRYO_SERIALIZER);
        RpcClientProxy clientProxy = new RpcClientProxy(socketClient);
        HelloService helloService = clientProxy.getProxy(HelloService.class);
        String res = helloService.hello(new HelloObject(001, "徐宁的客户端"));
        System.out.println(res);

    }
}
