package fun.xuning.rpc.provider;

/**
 *
 * @author xuning
 */
public interface ServiceProvider {


    <T> void addServiceProvider(T service, String serviceName);

    Object getServiceProvider(String serviceName);

}
