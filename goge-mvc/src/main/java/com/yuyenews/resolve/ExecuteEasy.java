package com.yuyenews.resolve;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.yuyenews.base.EasyInters;
import com.yuyenews.core.util.RequestUtil;
import com.yuyenews.easy.server.request.HttpRequest;
import com.yuyenews.easy.server.request.HttpResponse;
import com.yuyenews.resolve.model.EasyMappingModel;

import io.netty.handler.codec.http.HttpMethod;

/**
 * 执行器
 * 
 * @author yuye
 *
 */
public class ExecuteEasy {

	private Logger log = LoggerFactory.getLogger(ExecuteEasy.class);

	private static ExecuteEasy executeEasy;

	private ExecuteEasy() {
	}

	public static ExecuteEasy getExecuteEasy() {
		if (executeEasy == null) {
			executeEasy = new ExecuteEasy();
		}
		return executeEasy;
	}

	/**
	 * 执行controller
	 * @param easyMappingModel duix
	 * @param method fangfa
	 * @param request qingqiu
	 * @param response xiangying
	 * @return duix
	 */
	public Object execute(EasyMappingModel easyMappingModel, HttpMethod method, HttpRequest request, HttpResponse response) {

		try {
			
			if(easyMappingModel == null) {
				/* 如果请求方式和controller的映射不一致，则提示客户端 */
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("error_code", 404);
				jsonObject.put("error_info", "服务器上没有相应的接口");
				return jsonObject;
			}
			
			String strMethod = method.name().toString().toLowerCase();

			String mathodReuest = easyMappingModel.getRequestMetohd().name().toLowerCase();
			
			if (strMethod.equals(mathodReuest)) {

				/* 获取拦截器 并执行 控制层执行前的方法 */
				String uriEnd = RequestUtil.getUriName(request);
				List<Object> list = ExecuteInters.getInters(uriEnd);
				Object inres = ExecuteInters.executeIntersStart(list,request, response);
				if(!inres.toString().equals(EasyInters.SUCCESS)) {
					return inres;
				}
				
				Object obj = easyMappingModel.getObject();
				Class<?> cls = easyMappingModel.getCls();
				Method method2 = cls.getDeclaredMethod(easyMappingModel.getMethod(), new Class[] { HttpRequest.class, HttpResponse.class });
				Object result = method2.invoke(obj, new Object[] { request, response });
				
				/* 执行拦截器 在控制层执行后的方法 */
				Object inres2 = ExecuteInters.executeIntersEnd(list,request, response,result);
				if(!inres2.toString().equals(EasyInters.SUCCESS)) {
					return inres2;
				}
				
				return result;
			} else {
				/* 如果请求方式和controller的映射不一致，则提示客户端 */
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("error_code", 404);
				jsonObject.put("error_info", "此接口的请求方式为[" + mathodReuest + "]");
				return jsonObject;
			}
		} catch (Exception e) {
			log.error("执行控制层的时候报错",e);
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("error_code", 500);
			jsonObject.put("error_info", "执行控制层的时候报错");
			return jsonObject;
		}
	}
	
}
