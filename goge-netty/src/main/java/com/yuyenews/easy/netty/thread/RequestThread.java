package com.yuyenews.easy.netty.thread;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.yuyenews.easy.server.constant.EasySpace;
import com.yuyenews.easy.server.request.HttpRequest;
import com.yuyenews.easy.server.request.HttpResponse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * 处理请求的线程
 * @author yuye
 *
 */
public class RequestThread implements Runnable {
	
	private Logger log = LoggerFactory.getLogger(RequestThread.class);

	/**
	 * netty的request对象
	 */
	private FullHttpRequest httpRequest;

	private ChannelHandlerContext ctx;
	
	public void setHttpRequest(FullHttpRequest httpRequest) {
		this.httpRequest = httpRequest;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	public void run() {

		try {
			
			/* 获取全局存储空间 */
			EasySpace constants = EasySpace.getEasySpace();
			/* 从存储空间里获取核心servlet的全限名 */
			String className = constants.getAttr("core").toString();
			
			/* 组装httprequest对象 */
			HttpRequest request = new HttpRequest(httpRequest,ctx);
			
			/* 组装httpresponse对象 */
			HttpResponse response = new HttpResponse(ctx);
			
			/* 通过反射执行核心servlet */
			Class<?> cls = Class.forName(className);
			Object object = cls.getDeclaredConstructor().newInstance();
			Method helloMethod = cls.getDeclaredMethod("doRequest", new Class[] { HttpRequest.class ,HttpResponse.class});
			Object result = helloMethod.invoke(object, new Object[] { request ,response});
			if(result != null && result.toString().equals("no")) {
				return;
			}
			/* 将控制层返回的数据，转成json字符串返回 */
			response.send(JSON.toJSONString(result));
			
		} catch (Exception e) {
			log.error("处理请求的时候出错",e);
		} finally {
			// 释放请求
			httpRequest.release();
		}
	}
}
