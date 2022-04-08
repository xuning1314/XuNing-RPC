package fun.xuning.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import fun.xuning.rpc.enumeration.RpcError;
import fun.xuning.rpc.exception.RpcException;
import fun.xuning.rpc.loadbalancer.LoadBalancer;
import fun.xuning.rpc.loadbalancer.RandomLoadBalancer;
import fun.xuning.rpc.util.NacosUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

public class NacosServiceDiscovery implements ServiceDiscovery{
    private static final Logger logger = LoggerFactory.getLogger(NacosServiceDiscovery.class);

    private final LoadBalancer loadBalancer;

    public NacosServiceDiscovery(LoadBalancer loadBalancer) {
        if(loadBalancer == null) this.loadBalancer = new RandomLoadBalancer();
        else this.loadBalancer = loadBalancer;
    }
    //根据提供的服务和服务地址注册到nacos中
    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try {
            List<Instance> instances = NacosUtil.getAllInstance(serviceName);
            if(instances.size() == 0) {
                logger.error("找不到对应的服务: " + serviceName);
                throw new RpcException(RpcError.SERVICE_NOT_FOUND);
            }
            //负载均衡选择服务
            //实现了两种负载均衡算法，轮询与随机
            Instance instance = loadBalancer.select(instances);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            logger.error("获取服务时有错误发生:", e);
        }
        return null;
    }
}
