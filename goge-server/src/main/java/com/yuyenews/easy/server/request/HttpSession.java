package com.yuyenews.easy.server.request;

import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

/**
 * session
 * 
 * @author yuye
 *
 */
public class HttpSession implements Serializable {

	private static final long serialVersionUID = 1284233317791873433L;

	private Map<String, Object> map;

	/**
	 * 创建时间
	 */
	private Date date;

	public HttpSession() {
		map = new Hashtable<>();
		date = new Date();
	}

	public void setAttr(String key, Object value) {
		this.map.put(key, value);
	}

	public Object getAttr(String key) {
		return map.get(key);
	}

	public Date getDate() {
		return date;
	}

}
