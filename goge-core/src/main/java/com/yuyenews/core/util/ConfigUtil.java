package com.yuyenews.core.util;

import com.alibaba.fastjson.JSONObject;
import com.yuyenews.easy.server.constant.EasySpace;

/**
 * 配置文件工具类
 * @author yuye
 *
 */
public class ConfigUtil {
	
	private static EasySpace constants = EasySpace.getEasySpace();

	/**
	 * 获取配置信息
	 * @return json
	 */
	public static JSONObject getConfig() {
		Object obj = constants.getAttr("config");
		if(obj != null) {
			JSONObject jsonObject = (JSONObject)obj;
			
			return jsonObject;
		}
		
		return null;
	}
}
