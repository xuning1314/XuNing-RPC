package fun.xuning.rpc.transport;

import fun.xuning.rpc.annotation.Service;
import fun.xuning.rpc.annotation.ServiceScan;
import fun.xuning.rpc.enumeration.RpcError;
import fun.xuning.rpc.exception.RpcException;
import fun.xuning.rpc.provider.ServiceProvider;
import fun.xuning.rpc.registry.ServiceRegistry;
import fun.xuning.rpc.util.ReflectUtil;
import fun.xuning.rpc.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Set;


public abstract class AbstractRpcServer implements RpcServer{

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected String host;
    protected int port;
    protected ServiceRegistry serviceRegistry;
    protected ServiceProvider serviceProvider;



    public void scanServices() {
        String mainClassName = ReflectUtil.getStackTrace();//获得启动类名字
        Class<?> startClass;
        try {
            startClass = Class.forName(mainClassName);
            if (!startClass.isAnnotationPresent(ServiceScan.class)) {
                logger.error("启动类缺少 @ServiceScan 注解");
                throw new RpcException(RpcError.SERVICE_SCAN_PACKAGE_NOT_FOUND);
            }
        } catch (ClassNotFoundException e) {
            logger.error("出现未知错误");
            throw new RpcException(RpcError.UNKNOWN_ERROR);
        }
        String basePackage = startClass.getAnnotation(ServiceScan.class).value();//获得需要扫描的包
        if ("".equals(basePackage)) {
            basePackage = mainClassName.substring(0, mainClassName.lastIndexOf("."));
        }
        Set<Class<?>> classSet = ReflectUtil.getClasses(basePackage);//获得包下所有的类
        for (Class<?> clazz : classSet) {//循环
            if (clazz.isAnnotationPresent(Service.class)) {//判断类上是不是加了service注解
                String serviceName = clazz.getAnnotation(Service.class).name();
                Object obj;
                try {
                    obj = clazz.newInstance();
                    String[] split = clazz.getName().split("\\.");
                    if(split.length>0) {
                        String s = split[split.length - 1];
                        s = s.substring(0,1).toLowerCase().concat(s.substring(1));
                        obj = Utils.getBean(s);
                    }
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("创建 " + clazz + " 时有错误发生");
                    continue;
                }
                if ("".equals(serviceName)) {
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> oneInterface : interfaces) {
                        publishService(obj, oneInterface.getCanonicalName());
                    }
                } else {
                    publishService(obj, serviceName);
                }
            }
        }
    }

    @Override
    public <T> void publishService(T service, String serviceName) {
        //服务类实例对象与服务类名
        serviceProvider.addServiceProvider(service, serviceName);
        //服务类名与ip+端口
        serviceRegistry.register(serviceName, new InetSocketAddress(host, port));
        //start();
    }
}
