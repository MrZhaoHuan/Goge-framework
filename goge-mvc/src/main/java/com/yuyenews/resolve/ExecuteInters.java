package com.yuyenews.resolve;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yuyenews.base.EasyInters;
import com.yuyenews.core.util.ConfigUtil;
import com.yuyenews.core.util.MatchUtil;
import com.yuyenews.easy.server.request.HttpRequest;
import com.yuyenews.easy.server.request.HttpResponse;

/**
 * 执行拦截器
 * @author yuye
 *
 */
public class ExecuteInters {
	
	private static Logger logger = LoggerFactory.getLogger(ExecuteInters.class);

	/**
	 * 执行拦截器的开始方法
	 * @param list jihe
	 * @param request qingqiu
	 * @param response xiangying
	 * @return duix
	 */
	public static Object executeIntersStart(List<Object> list,HttpRequest request, HttpResponse response) {
		Class<?> clss = null;
		try {
			for(Object obj : list) {
				clss = obj.getClass();
				
				Method method2 = clss.getDeclaredMethod("startRequest", new Class[] { HttpRequest.class, HttpResponse.class });
				Object result = method2.invoke(obj, new Object[] { request, response });
				if(!result.toString().equals(EasyInters.SUCCESS)) {
					return result;
				}
			}
			
			return EasyInters.SUCCESS;
		} catch (Exception e) {
			logger.error("执行拦截器报错，拦截器类型["+clss.getName()+"]",e);
			return errorResult(clss);
		} 
		
	}
	
	/**
	 * 执行拦截器的结束方法
	 * @param list jihe
	 * @param request qingqiu
	 * @param response xiangying
	 * @return duix
	 */
	public static Object executeIntersEnd(List<Object> list,HttpRequest request, HttpResponse response,Object objs) {
		Class<?> clss = null;
		try {
			
			for(Object obj : list) {
				clss = obj.getClass();
				
				Method method2 = clss.getDeclaredMethod("endRequest", new Class[] { HttpRequest.class, HttpResponse.class, Object.class });
				Object result = method2.invoke(obj, new Object[] { request, response, objs });
				if(!result.toString().equals(EasyInters.SUCCESS)) {
					return result;
				}
			}
			
			return EasyInters.SUCCESS;
		} catch (Exception e) {
			logger.error("执行拦截器报错，拦截器类型["+clss.getName()+"]",e);
			return errorResult(clss);
		} 
	}
	
	/**
	 * 获取所有符合条件的拦截器
	 * @param uriEnd uel
	 * @return duix
	 */
	public static List<Object> getInters(String uriEnd){
		
		try {
			List<Object> list = new ArrayList<>();
			
			JSONObject jsonObject = ConfigUtil.getConfig();
			Object obj = jsonObject.get("inters");
			if(obj != null) {
				JSONArray array = JSONArray.parseArray(JSON.toJSONString(obj));
				
				for(int i = 0;i<array.size();i++) {
					
					JSONObject ins = JSONObject.parseObject(array.get(i).toString());
					if(MatchUtil.isMatch(ins.getString("pattern"), uriEnd)){
						Class<?> cls = Class.forName(ins.getString("class"));
						list.add(cls.getDeclaredConstructor().newInstance());
					}
				}
			}
			return list;
		} catch (Exception e) {
			logger.error("读取配置文件中的拦截器报错",e);
			return new ArrayList<>();
		}
	}
	
	/**
	 * 返回错误信息
	 * @param cls
	 * @return
	 */
	private static JSONObject errorResult(Class<?> cls) {
		/* 如果请求方式和controller的映射不一致，则提示客户端 */
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("error_code", 500);
		jsonObject.put("error_info", "执行拦截器报错，拦截器类型["+cls.getName()+"]");
		return jsonObject;
	}
}
