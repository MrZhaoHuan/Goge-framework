package com.yuyenews.easy.netty.server;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yuyenews.easy.netty.thread.RequestThread;
import com.yuyenews.easy.netty.thread.ThreadPool;
import com.yuyenews.easy.server.request.HttpResponse;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * 接收netty服务
 * @author yuye
 *
 */
public class EasyServerHandler extends ChannelHandlerAdapter {

	private Logger log = LoggerFactory.getLogger(EasyServerHandler.class);

	/**
	 * 接收并处理 客户端请求
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		FullHttpRequest httpRequest = null;
		
		try {
			if (msg instanceof FullHttpRequest) {
				
				httpRequest = (FullHttpRequest) msg;

				/* 用新线程处理请求 */
				RequestThread requestThread = new RequestThread();
				requestThread.setHttpRequest(httpRequest);
				requestThread.setCtx(ctx);
				ThreadPool.execute(requestThread);
			} else {
				HttpResponse response = new HttpResponse(ctx);
				response.send("未知请求!", HttpResponseStatus.BAD_REQUEST);
			}
			

		} catch (Exception e) {
			log.error("处理请求失败!", e);
			/* 已经通过线程中的finally 释放请求了，所以这里，在出异常的时候，才释放 */
			try {
				httpRequest.release();
			} catch (Exception e2) {
			}
		}
	}

	

	/**
	 * 建立连接时，返回消息
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush("客户端" + InetAddress.getLocalHost().getHostName() + "成功与服务端建立连接！ ");
		super.channelActive(ctx);
	}
}
