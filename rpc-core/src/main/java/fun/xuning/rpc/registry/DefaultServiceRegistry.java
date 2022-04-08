//package fun.xuning.rpc.registry;
//
//import fun.xuning.rpc.enumeration.RpcError;
//import fun.xuning.rpc.exception.RpcException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class DefaultServiceRegistry implements ServiceRegistry2 {
//
//    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceRegistry.class);
//
//    //存服务接口的类名，用来判断类是否注册过
//    private static final Set<String> registeredService = ConcurrentHashMap.newKeySet();
//
//    //存接口与服务对象的映射
//    private static final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
//
//    @Override
//    public <T> void registry(T service) {
//        String serviceName = service.getClass().getCanonicalName();
//        if (registeredService.contains(serviceName)) return;
//        registeredService.add(serviceName);
//        Class<?>[] interfaces = service.getClass().getInterfaces();
//        if (interfaces.length == 0) {
//            throw new RpcException(RpcError.SERVICE_NOT_IMPLEMENT_ANY_INTERFACE);
//        }
//        for (Class<?> anInterface : interfaces) {
//            serviceMap.put(anInterface.getCanonicalName(), service);
//        }
//        logger.info("向接口: {} 注册服务: {}", interfaces, serviceName);
//    }
//
//    @Override
//    public Object getService(String serviceName) {
//        Object service = serviceMap.get(serviceName);
//        if (service == null) {
//            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
//        }
//        return service;
//    }
//}
