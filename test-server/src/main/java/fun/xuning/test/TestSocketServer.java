package fun.xuning.test;

import fun.xuning.rpc.annotation.ServiceScan;
import fun.xuning.rpc.serializer.CommonSerializer;
import fun.xuning.rpc.transport.socket.server.SocketServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ServiceScan
public class TestSocketServer {
    public static void main(String[] args) {

        SpringApplication.run(TestSocketServer.class,args);
        SocketServer socketServer = new SocketServer("127.0.0.1", 9889, CommonSerializer.KRYO_SERIALIZER);
        socketServer.start();
    }
}
