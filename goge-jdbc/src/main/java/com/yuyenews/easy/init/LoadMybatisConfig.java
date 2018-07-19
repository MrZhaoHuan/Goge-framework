package com.yuyenews.easy.init;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yuyenews.core.util.FileUtil;
import com.yuyenews.easy.server.constant.EasySpace;
import com.yuyenews.easy.util.JdbcConfigUtil;
import com.yuyenews.easy.util.ReadXml;
import com.yuyenews.easy.util.extend.MyDataSourceFactory;

/**
 * 组装myBatis配置文件
 * @author yuye
 *
 */
public class LoadMybatisConfig {
	
	private static Logger logger = LoggerFactory.getLogger(LoadMybatisConfig.class);
	
	private static EasySpace commontSpace = EasySpace.getEasySpace();
	
	private static JSONObject config =  JdbcConfigUtil.getConfig();

	/**
	 * 获取配置文件并以字符串形式返回
	 * @return
	 */
	public static String getConfigStr() {
		try {
			FileUtil.local = String.valueOf(config.get("config-location"));
			String str = FileUtil.readFileString("/"+FileUtil.local);
			if(str == null) {
				str = defaultConfig();
			}
			
			/* 禁止在mybatis配置文件里配置数据源 */
			if(str.indexOf("environment") > -1 || str.indexOf("dataSource") > -1 || str.indexOf("environments") > -1) {
				throw new Exception("不可以在mybatis配置文件里配置数据源");
			}
			
			/* 禁止在mybatis配置文件里配置mappers */
			if(str.indexOf("mappers") > -1 || str.indexOf("mapper") > -1) {
				throw new Exception("不可以在mybatis配置文件里配置mappers");
			}
			
			str = str.replaceAll("</configuration>", "");
			str += getDataSources();
			str += getMappers();
			str += "</configuration>";
			
			return str;
		} catch (Exception e) {
			logger.error("加载mybatis配置文件出错",e);
		}
		
		return null;
	}
	
	/**
	 * 获取所有mapper文件路径，并组装成xml格式的字符串返回
	 * @return
	 */
	private static String getMappers() {
		try {
			String mappers = config.getString("mappers");
			
			Set<String> xmls = ReadXml.loadXmlList(mappers);
			
			StringBuffer buffer = new StringBuffer("<mappers>");
			for(String str : xmls) {
				buffer.append("<mapper resource=\""+str+"\"/>");
			}
			buffer.append("</mappers>");
			return buffer.toString();
		} catch (IOException e) {
			logger.error("加载mybatis配置文件出错",e);
		}
		return "";
	}

	/**
	 * 加载数据源配置
	 * @return
	 */
	private static String getDataSources() {
		try {
			String def = "";
			
			JSONArray array = config.getJSONArray("dataSource");
			
			StringBuffer dataSource = new StringBuffer("<environments default=\"${def}\">")  ;

			List<String> daNames = new ArrayList<>();
			
			for (int i = 0; i < array.size(); i++) {
				
				JSONObject jsonObject = array.getJSONObject(i);
				
				if(!ckDsConfig(jsonObject)) {
					return "";
				}
				
				if(i == 0) {
					def = jsonObject.getString("name");
				}
				
				String type = getDataSourceType(jsonObject.getString("type"));
				
				StringBuffer buffer = new StringBuffer();
				buffer.append("<environment id=\""+jsonObject.getString("name")+"\">");
				buffer.append("<transactionManager type=\"JDBC\"/>");
				buffer.append("<dataSource type=\""+type+"\">");
				for(String key : jsonObject.keySet()) {
					if(!key.equals("name") && !key.equals("type")) {
						buffer.append("<property name=\""+key+"\" value=\""+jsonObject.get(key)+"\"/>");
					}
				}
				buffer.append("</dataSource>");
				buffer.append("</environment>");
				dataSource.append(buffer);
				
				daNames.add(jsonObject.getString("name"));
			}
			
			commontSpace.setAttr("dataSourceNames", daNames);
			commontSpace.setAttr("defaultDataSource", def);

			dataSource.append("</environments>");
			
			return dataSource.toString().replace("${def}", def);
		} catch (Exception e) {
			logger.error("加载mybatis配置文件出错",e);
		}
		
		return "";
	}
	
	/**
	 * 获取数据源类型
	 * @param type
	 * @return
	 */
	private static String getDataSourceType(String type) {
		if(type.equals("com.alibaba.druid.pool.DruidDataSource")) {
			return MyDataSourceFactory.class.getName();
		}
		return type;
	}
	
	/**
	 * 验证数据源配置
	 * @return
	 */
	private static boolean ckDsConfig(JSONObject jsonObject) {
		if(jsonObject.get("type") == null) {
			logger.error("数据源没有指定type");
			return false;
		}
		
		if(jsonObject.get("name") == null) {
			logger.error("数据源没有指定name");
			return false;
		}
		return true;
	}
	
	/**
	 * 默认配置
	 * @return
	 */
	private static String defaultConfig() {
		try {
			
			Object dialect = config.get("dialect");
			
			if(dialect == null) {
				/* 方言 默认mysql */
				dialect = "mysql";
			}
			
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			stringBuffer.append("<!DOCTYPE configuration PUBLIC \"-//mybatis.org//DTD Config 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-config.dtd\">");
			stringBuffer.append("<configuration>");
			stringBuffer.append("<properties>");
			stringBuffer.append("<property name=\"dialect\" value=\""+dialect+"\" />");
			stringBuffer.append("</properties>");
			stringBuffer.append("<plugins>");
			stringBuffer.append("<plugin interceptor=\"com.github.pagehelper.PageHelper\">");
			stringBuffer.append("<property name=\"dialect\" value=\""+dialect+"\" />");
			stringBuffer.append("</plugin>");
			stringBuffer.append("</plugins>");
			stringBuffer.append("</configuration>");
			
			return stringBuffer.toString();
		} catch (Exception e) {
			logger.error("加载mybatis配置文件出错",e);
		}
		
		return "";
	}
}
