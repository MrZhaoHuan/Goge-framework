package com.yuyenews.core.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件帮助
 * @author yuye
 *
 */
public class FileUtil {
	
	private static Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	public static String local = null;

	/**
	 * 根据文件路径 获取文件中的字符串内容
	 * @param path 路径
	 * @return
	 */
	public static String readFileString(String path) {
		
		try {
			InputStream inputStream = FileUtil.class.getResourceAsStream(path);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));  
			StringBuffer sb = new StringBuffer();  
			String str = "";
			while ((str = reader.readLine()) != null)  
			{  
			    sb.append(str);  
			}  
			return sb.toString();
		} catch (Exception e) {
			if(local == null) {
				logger.error("",e);
			} else {
				logger.warn("自定义mybatis配置文件加载失败或者不存在，将自动使用默认配置");
			}
		}
		return null;
	}
}
