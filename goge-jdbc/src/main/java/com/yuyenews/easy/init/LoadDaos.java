package com.yuyenews.easy.init;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.yuyenews.core.model.EasyBeanModel;
import com.yuyenews.core.util.ReadClass;
import com.yuyenews.core.util.StringUtil;
import com.yuyenews.easy.proxy.MappersProxy;
import com.yuyenews.easy.server.constant.EasySpace;
import com.yuyenews.easy.util.JdbcConfigUtil;

public class LoadDaos {
	
	private static Logger log = LoggerFactory.getLogger(LoadDaos.class);
	
	/**
	 * 获取全局存储空间 
	 */
	private static EasySpace constants = EasySpace.getEasySpace();
	
	private static JSONObject config =  JdbcConfigUtil.getConfig();

	/**
	 * 创建dao对象
	 */
	@SuppressWarnings("unchecked")
	public static void loadDao() {
		try {
			String daos = config.getString("daos");
			Set<String> classList = ReadClass.loadClassList(daos);
			
			/* 创建bean对象，并保存起来 */
			Object objs2 = constants.getAttr("easyBeanObjs");
			Map<String,EasyBeanModel> easyBeanObjs = new HashMap<>();
			if(objs2 != null) {
				easyBeanObjs = (Map<String,EasyBeanModel>)objs2;
			} else {
				constants.setAttr("easyBeanObjs",easyBeanObjs);
			}
			
			for(String str : classList) {
				Class<?> cls = Class.forName(str);
				
				String beanName = StringUtil.getFirstLowerCase(cls.getSimpleName());
				if(easyBeanObjs.get(beanName) == null) {
					EasyBeanModel beanModel = new EasyBeanModel();
					beanModel.setName(beanName);
					beanModel.setCls(cls);
					beanModel.setObj(getObject(cls));
					easyBeanObjs.put(beanName, beanModel);
				} else {
					throw new Exception("已经存在name为["+beanName+"]的bean了");
				}
			}
			
		} catch (Exception e) {
			log.error("加载并注入EasyBean的时候出现错误",e);
		} 
	}

	/**
	 * 获取代理对象
	 * @param clazz
	 * @return
	 */
	private static Object getObject(Class<?> clazz) {
		MappersProxy mappersProxy = new MappersProxy();
		return mappersProxy.getProxy(clazz);
	}
}
