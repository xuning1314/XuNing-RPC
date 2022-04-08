package fun.xuning.rpc.transport.netty.server;

import fun.xuning.rpc.entity.RpcRequest;
import fun.xuning.rpc.entity.RpcResponse;
import fun.xuning.rpc.factory.SingletonFactory;
import fun.xuning.rpc.handler.RequestHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty中处理RpcRequest的Handler
 *
 * @author xuning
 */
//接受客户端发来的数据，类似于Socket中的监听端口接数据
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private final RequestHandler requestHandler;

    public NettyServerHandler() {
        this.requestHandler = SingletonFactory.getInstance(RequestHandler.class);
    }

    //处理接收到的request
    //读取从客户端消息，然后调用目标服务的目标方法并返回给客户端
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        try {
            if(msg.getHeartBeat()) {
                logger.info("接收到客户端心跳包...");
                return;
            }
            logger.info("服务器接收到请求: {}", msg);
            //处理接收到的request，进入handle根据request信息反射服务器本地接口方法，返回一个返回值
            Object result = requestHandler.handle(msg);
            if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                //将返回值resule封装成response
                ctx.writeAndFlush(RpcResponse.success(result, msg.getRequestId()));
            } else {
                logger.error("通道不可写");
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("处理过程调用时有错误发生:");
        cause.printStackTrace();
        ctx.close();
    }

    // Netty 心跳机制相关。保证客户端和服务端的连接不被断掉，避免重连。
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {// 如果读通道处于空闲状态，说明没有接收到心跳命令
                logger.info("长时间未收到心跳包，断开连接...");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}
