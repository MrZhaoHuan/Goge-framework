package com.yuyenews.easy.server.constant;

import java.util.Hashtable;
import java.util.Map;

/**
 * 全局存储空间
 * @author yuye
 *
 */
public class EasySpace {

	private static EasySpace constants;
	
	private Map<String, Object> map = new Hashtable<>();

	private EasySpace() {
	}

	public static EasySpace getEasySpace() {
		if (constants == null) {
			constants = new EasySpace();
		}

		return constants;
	}
	
	/**
	 * 往Constants里添加数据
	 * @param key 键
	 * @param value 值
	 */
	public void setAttr(String key,Object value) {
		map.put(key, value);
	}
	
	/**
	 * 从Constants里获取数据
	 * @param key 键
	 * @return 值
	 */
	public Object getAttr(String key) {
		return map.get(key);
	}
	
	/**
	 * 移除元素
	 * @param key
	 */
	public void remove(String key) {
		map.remove(key);
	}
}
