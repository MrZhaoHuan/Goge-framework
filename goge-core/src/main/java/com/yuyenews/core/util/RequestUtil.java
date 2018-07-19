package com.yuyenews.core.util;

import com.yuyenews.easy.server.request.HttpRequest;

/**
 * 请求工具类
 * @author yuye
 *
 */
public class RequestUtil {

	/**
	 * 从uri中提取最末端
	 * @param request 请求
	 * @return string
	 */
	public static String getUriName(HttpRequest request) {
		/* 获取路径 */
		String uri = request.getUri();
		if(uri.indexOf("?")>-1) {
			uri = uri.substring(0,uri.indexOf("?"));
		}
		return uri;
	}
}
