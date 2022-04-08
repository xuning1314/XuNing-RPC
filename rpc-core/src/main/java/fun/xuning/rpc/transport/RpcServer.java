package fun.xuning.rpc.transport;

import fun.xuning.rpc.serializer.CommonSerializer;

public interface RpcServer {
    int DEFAULT_SERIALIZER = CommonSerializer.KRYO_SERIALIZER;

    void start();

    //向nacos注册服务
    <T> void publishService(T service, String serviceName);
}
