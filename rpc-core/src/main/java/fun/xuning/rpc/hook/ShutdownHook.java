package fun.xuning.rpc.hook;

import fun.xuning.rpc.factory.ThreadPoolFactory;
import fun.xuning.rpc.util.NacosUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author xuning
 */
public class ShutdownHook {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);

    private static final ShutdownHook shutdownHook = new ShutdownHook();

    public static ShutdownHook getShutdownHook() {
        return shutdownHook;
    }

    //钩子，在服务端关闭之前，关闭其对应服务
    public void addClearAllHook() {
        logger.info("关闭后将自动注销所有服务");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {//Runtime对象是jvm虚拟机运行时环境，在jvm关闭之前会执行他
            NacosUtil.clearRegistry();
            ThreadPoolFactory.shutDownAll();
        }));
    }

}
