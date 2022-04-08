package fun.xuning.rpc.handler;

import fun.xuning.rpc.entity.RpcRequest;
import fun.xuning.rpc.entity.RpcResponse;
import fun.xuning.rpc.enumeration.ResponseCode;
import fun.xuning.rpc.provider.ServiceProvider;
import fun.xuning.rpc.provider.ServiceProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 进行过程调用的处理器
 *
 * @author xuning
 */
public class RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private static final ServiceProvider serviceProvider;

    static {
        serviceProvider = new ServiceProviderImpl();
    }

    public Object handle(RpcRequest rpcRequest) {
        Object service = serviceProvider.getServiceProvider(rpcRequest.getInterfaceName());//通过接口名去得到其实现类
        //通过request信息和实现类来反射调用相应的实现方法
        return invokeTargetMethod(rpcRequest, service);
    }

    //根据request，通过反射，来调用本地的服务方法，得到后返回方法返回值Object
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            logger.info("服务:{} 成功调用方法:{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND, rpcRequest.getRequestId());
        }
        return result;
    }

}
