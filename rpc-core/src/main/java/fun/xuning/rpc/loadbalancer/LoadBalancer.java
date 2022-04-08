package fun.xuning.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * @author xuning
 */
public interface LoadBalancer {

    Instance select(List<Instance> instances);

}
