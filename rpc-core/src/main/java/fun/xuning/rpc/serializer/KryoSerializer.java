package fun.xuning.rpc.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fun.xuning.rpc.entity.RpcRequest;
import fun.xuning.rpc.entity.RpcResponse;
import fun.xuning.rpc.enumeration.SerializerCode;
import fun.xuning.rpc.exception.SerializeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Kryo序列化器
 *
 * @author xuning
 */
public class KryoSerializer implements CommonSerializer {

    private static final Logger logger = LoggerFactory.getLogger(KryoSerializer.class);

    //防止线程不安全，将Kryo放入ThreadLocal中
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);
        kryo.setReferences(true);
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             //先创建一个 Output 对象（Kryo 框架的概念）
             Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            //将对象写入 Output 中
            kryo.writeObject(output, obj);
            kryoThreadLocal.remove();
            //调用 Output 对象的 toBytes() 方法即可获得对象的字节数组
            return output.toBytes();
        } catch (Exception e) {
            logger.error("序列化时有错误发生:", e);
            throw new SerializeException("序列化时有错误发生");
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             //先创建一个 input 对象（Kryo 框架的概念）
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            //将对象写入 input对象 中的数据根据clazz反序列化为对应的对象
            Object o = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return o;
        } catch (Exception e) {
            logger.error("反序列化时有错误发生:", e);
            throw new SerializeException("反序列化时有错误发生");
        }
    }

    @Override
    public int getCode() {
        return SerializerCode.valueOf("KRYO").getCode();
    }
}
