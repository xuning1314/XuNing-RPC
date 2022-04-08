package fun.xuning.rpc.exception;

/**
 * 序列化异常
 *
 * @author xuning
 */
public class SerializeException extends RuntimeException {
    public SerializeException(String msg) {
        super(msg);
    }
}
