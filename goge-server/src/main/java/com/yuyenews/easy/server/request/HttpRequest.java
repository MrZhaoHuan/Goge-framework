package com.yuyenews.easy.server.request;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yuyenews.easy.server.request.model.FileUpLoad;
import com.yuyenews.easy.server.sessionm.SessionManager;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.CharsetUtil;

/**
 * 请求对象，对原生netty的request的补充
 * @author yuye
 *
 */
public class HttpRequest {
	
	private Logger logger = LoggerFactory.getLogger(HttpRequest.class);
	
	/**
	 * netty原生request
	 */
	private FullHttpRequest httpRequest;
	
	/**
	 * netty原生通道
	 */
	private ChannelHandlerContext ctx;
	
	/**
	 * 请求体
	 */
	private String body;

	/**
	 * 参数
	 */
	private Map<String, Object> paremeters;
	
	/**
	 * 请求的文件
	 */
	private Map<String,FileUpLoad> files;
	
	/**
	 * 构造函数，框架自己用的，程序员用不到，用了也没意义
	 * @param httpRequest 原生请求对象
	 */
	public HttpRequest(FullHttpRequest httpRequest,ChannelHandlerContext ctx) {
		this.body = getBody(httpRequest);
		this.setParemeters(getPams(httpRequest));
		this.httpRequest = httpRequest;
		this.ctx = ctx;
	}
	
	/**
	 * 获取请求方法
	 * @return 请求方法
	 */
	public HttpMethod getMethod() {
		return httpRequest.method();
	}

	/**
	 * 获取要请求的uri
	 * @return 请求方法
	 */
	public String getUri() {
		return httpRequest.uri();
	}
	
	/**
	 * 获取请求头数据
	 * @param key 键
	 * @return 头数据
	 */
	public Object getHeader(String key) {
		return httpRequest.headers().get(key);
	}
	
	/**
	 * 获取请求头
	 * @return 请求头
	 */
	public HttpHeaders getHeaders() {
		return httpRequest.headers();
	}

	/**
	 * 获取请求的参数集
	 * @return 请求参数
	 */
	public Map<String, Object> getParemeters() {
		return paremeters;
	}

	/**
	 * 组装请求的参数
	 * @param paremeters 请求参数
	 */
	private void setParemeters(Map<String, Object> paremeters) {
		Object obj = paremeters.get("files");
		if (obj != null) {
			@SuppressWarnings("unchecked")
			Map<String,FileUpLoad> files = (Map<String,FileUpLoad>) obj;
			this.files = files;
			paremeters.remove("files");
		}

		this.paremeters = paremeters;

	}
	
	/**
	 * 获取单个请求的参数
	 * @param key 键
	 * @return 请求参数
	 */
	@SuppressWarnings("unchecked")
	public Object getParemeter(String key) {
		Object objs = paremeters.get(key);
		if(objs != null) {
			List<Object> lis = (List<Object>)objs;
			return lis.get(0);
		}
		return null;
	}
	
	/**
	 * 获取单个请求的参数
	 * @param key 键
	 * @return 请求参数
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getParemeterValues(String key) {
		Object objs = paremeters.get(key);
		if(objs != null) {
			List<Object> lis = (List<Object>)objs;
			return lis;
		}
		return null;
	}

	/**
	 * 获取请求的文件
	 * @return 文件列表
	 */
	public Map<String,FileUpLoad> getFiles() {
		return files;
	}

	/**
	 * 获取单个请求的文件
	 * 
	 * @param name 名称
	 * @return 单个文件
	 */
	public FileUpLoad getFile(String name) {
		if (files != null && files.size() > 0) {
			return files.get(name);
		} else {
			return null;
		}
	}

	/**
	 * 获取请求的url
	 * @return 请求的路径
	 */
	public String getUrl() {
		return httpRequest.uri();
	}

	/**
	 * 获取请求的body
	 * @return 请求体
	 */
	public String getBody() {
		return body;
	}
	
	/**
	 * 获取netty原生request
	 * @return 原生请求对象
	 */
	public FullHttpRequest getHttpRequest() {
		return httpRequest;
	}

	/**
	 * 获取body参数
	 * 
	 * @param request 请求对象
	 * @return 请求体
	 */
	private String getBody(FullHttpRequest request) {
		ByteBuf buf = request.content();
		return buf.toString(CharsetUtil.UTF_8);
	}

	/**
	 * 将GET, POST所有请求参数转换成Map对象
	 * @param request 原生请求对象
	 * @return 请求参数
	 */
	private Map<String, Object> getPams(FullHttpRequest request) {
		try {
			return new RequestParser(request).parse();
		} catch (IOException e) {
			logger.error("从请求中获取参数，报错",e);
		} 
		return new Hashtable<>();
	}
	
	/**
	 * 获取httpSession
	 * @return session
	 */
	public HttpSession getHttpSession() {
		return SessionManager.getHttpSession(this);
	}
	
	/**
	 * 获取客户端IP
	 * @return
	 */
	public String getIp() {
        String clientIP = String.valueOf(httpRequest.headers().get("X-Forwarded-For"));
        if (clientIP == null || clientIP.equals("null")) {
            InetSocketAddress insocket = (InetSocketAddress) this.ctx.channel().remoteAddress();
            clientIP = insocket.getAddress().getHostAddress();
        }
        return clientIP;
	}
}
