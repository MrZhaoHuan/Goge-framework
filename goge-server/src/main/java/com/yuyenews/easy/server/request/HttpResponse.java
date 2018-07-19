package com.yuyenews.easy.server.request;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yuyenews.easy.server.constant.EasySpace;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

/**
 * 响应对象，对netty原生response的扩展
 * 
 * 暂时没有提供response的支持
 * 
 * @author yuye
 *
 */
public class HttpResponse {
	
	private Logger logger = LoggerFactory.getLogger(HttpResponse.class);

	private ChannelHandlerContext ctx;
	
	/**
	 * 响应头
	 */
	private Map<String, String> header;
	
	/**
	 * 构造函数，框架自己用的，程序员用不到，用了也没意义
	 * 
	 * @param ctx 请求
	 */
	public HttpResponse(ChannelHandlerContext ctx) {
		this.ctx = ctx;
		this.header = new HashMap<>();
	}

	/**
	 * 设置响应头
	 * 
	 * @param key 键
	 * @param value 值
	 */
	public void setHeader(String key, String value) {
		this.header.put(key, value);
	}

	/**
	 * 响应数据
	 * 
	 * @param context
	 *            消息
	 */
	public void send(String context) {
		send(context, HttpResponseStatus.OK);
	}


	/**
	 * 响应数据
	 * 
	 * @param context
	 *            消息
	 * @param status
	 *            状态
	 */
	public void send(String context, HttpResponseStatus status) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
				Unpooled.copiedBuffer(context, CharsetUtil.UTF_8));

		crossDomain(response);

		if (header != null) {
			for (String key : header.keySet()) {
				response.headers().set(key, header.get(key));
			}
		}
		
		Object contentType = getConfig().get("content_type");
		if(contentType == null) {
			contentType = "text/json; charset=UTF-8";
		}
		
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType.toString());
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}
	
	/**
	 * 文件下载
	 * @param file 要下载的文件
	 */
	@Deprecated
	public void sendFile(File file) {
		logger.warn("sendFile方法还不能用哦，正在开发中....");
	}
	
	/**
	 * 文件下载
	 * @param file 要下载的文件
	 */
	@Deprecated
	public void sendFile(byte[] file) {
		logger.warn("sendFile方法还不能用哦，正在开发中....");
	}
	
	/**
	 * 文件下载
	 * @param file 要下载的文件
	 */
	@Deprecated
	public void sendFile(InputStream file) {
		logger.warn("sendFile方法还不能用哦，正在开发中....");
	}
	
	/**
	 * 设置跨域
	 * 
	 */
	private void crossDomain(FullHttpResponse response) {
		JSONObject jsonObject = getConfig();
		Object object = jsonObject.get("cross_domain");
		if (object != null) {
			JSONObject ob = JSONObject.parseObject(JSON.toJSONString(object));

			response.headers().set("Access-Control-Allow-Origin", ob.get("origin").toString());
			response.headers().set("Access-Control-Allow-Methods", ob.get("methods").toString());
			response.headers().set("Access-Control-Max-Age", ob.get("maxAge").toString());
			response.headers().set("Access-Control-Allow-Headers", "x-requested-with,Cache-Control,Pragma,Content-Type,Token");
			response.headers().set("Access-Control-Allow-Credentials", "true");
		}
	}

	/**
	 * 获取配置文件
	 * 
	 * @return 配置文件对象
	 */
	private JSONObject getConfig() {
		EasySpace constants = EasySpace.getEasySpace();
		Object obj = constants.getAttr("config");
		if (obj != null) {
			JSONObject jsonObject = (JSONObject) obj;
			return jsonObject;
		}

		return new JSONObject();
	}
}
