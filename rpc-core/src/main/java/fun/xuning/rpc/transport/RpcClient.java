package fun.xuning.rpc.transport;

import fun.xuning.rpc.entity.RpcRequest;
import fun.xuning.rpc.serializer.CommonSerializer;

public interface RpcClient {
    int DEFAULT_SERIALIZER = CommonSerializer.KRYO_SERIALIZER;
    Object sendRequest(RpcRequest rpcRequest);
}
