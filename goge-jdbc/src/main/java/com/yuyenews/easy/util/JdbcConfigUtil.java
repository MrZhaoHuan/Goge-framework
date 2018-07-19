package com.yuyenews.easy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yuyenews.core.util.ConfigUtil;

/**
 * 加载配置数据
 * 
 * @author yuye
 *
 */
public class JdbcConfigUtil {

	private static Logger log = LoggerFactory.getLogger(JdbcConfigUtil.class);

	/**
	 * 获取配置信息
	 * 
	 * @return 配置信息
	 */
	public static JSONObject getConfig() {
		try {
			JSONObject jsonObject = ConfigUtil.getConfig();
			
			if (jsonObject != null) {

				JSONObject jdbc = JSONObject.parseObject(JSON.toJSONString(jsonObject.get("jdbc")));
				
				return jdbc;
			}
		} catch (Exception e) {
			log.error("jdbc模块读取配置文件失败",e);
		}
		return new JSONObject();
	}
}
