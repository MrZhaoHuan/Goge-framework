package com.yuyenews.resolve;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yuyenews.core.annotation.Controller;
import com.yuyenews.core.annotation.EasyMapping;
import com.yuyenews.core.annotation.Resource;
import com.yuyenews.core.model.EasyBeanModel;
import com.yuyenews.easy.server.constant.EasySpace;
import com.yuyenews.resolve.model.EasyMappingModel;

/**
 * 加载所有的controller，并完成注入
 * @author yuye
 *
 */
public class LoadController {
	
	private static Logger log = LoggerFactory.getLogger(LoadController.class);
	
	/**
	 * 获取全局存储空间 
	 */
	private static EasySpace constants = EasySpace.getEasySpace();

	/**
	 * 创建controller对象，并将服务层对象注入进去
	 */
	@SuppressWarnings("unchecked")
	public static void loadContrl() {
		
		try {
			Map<String,EasyMappingModel> controlObjects = new HashMap<>();
			
			/* 获取所有的controller数据 */
			Object objs = constants.getAttr("contorls");
			List<Map<String,Object>> contorls = null;
			if(objs != null) {
				contorls = (List<Map<String,Object>>)objs;
			} else {
				return;
			}
			
			Map<String,EasyBeanModel> easyBeanObjs = getEasyBeans();
			
			for(Map<String,Object> map : contorls) {
				
				Class<?> cls = (Class<?>)map.get("className");
				Controller control = (Controller)map.get("annotation");
				
				/*
				 * 由于controller里只允许注入easybean，所以不需要等controller都创建好了再注入
				 * 直接 迭代一次 就给一个controller注入一次
				 */
				Object obj = iocControl(cls,control,easyBeanObjs);
				
				if(obj != null) {
					/* 获取controller的所有方法 */
					Method[] methods = cls.getMethods();
					for(Method method : methods) {
						EasyMapping easyMapping = method.getAnnotation(EasyMapping.class);
						if(easyMapping != null) {
							EasyMappingModel easyMappingModel = new EasyMappingModel();
							easyMappingModel.setObject(obj);
							easyMappingModel.setRequestMetohd(easyMapping.method());
							easyMappingModel.setMethod(method.getName());
							easyMappingModel.setCls(cls);
							controlObjects.put(easyMapping.value(), easyMappingModel);
						}
					}
				}
			}
			
			constants.setAttr("controlObjects", controlObjects);
		} catch (Exception e) {
			log.error("加载controller并注入的时候报错",e);
		}
	}
	
	/**
	 * 往controller对象中注入easybean
	 * @param cls lei
	 * @param control kongzhi
	 * @param easyBeanObjs duix
	 * @return duix
	 */
	private static Object iocControl(Class<?> cls,Controller control,Map<String,EasyBeanModel> easyBeanObjs) {
		
		try {
			
			Object obj = cls.getDeclaredConstructor().newInstance();
			
			/* 获取对象属性，完成注入 */
			Field[] fields = cls.getDeclaredFields();
			for(Field f : fields){
				Resource resource = f.getAnnotation(Resource.class);
				if(resource!=null){
					f.setAccessible(true);
					
					String filedName = resource.name();
					if(filedName == null || filedName.equals("")) {
						filedName = f.getName();
					}
					
					EasyBeanModel beanModel = easyBeanObjs.get(filedName);
					if(beanModel!=null){
						f.set(obj, beanModel.getObj());
						log.info(cls.getName()+"的属性"+f.getName()+"注入成功");
					}else{
						throw new Exception("不存在name为"+filedName+"的easyBean");
					}
				}
			}
			
			return obj;
		} catch (Exception e) {
			log.error("创建controller并注入的时候报错",e);
		} 
		
		return null;
	}
	
	/**
	 * 获取所有的easybean
	 * @return duix
	 */
	@SuppressWarnings("unchecked")
	private static Map<String,EasyBeanModel> getEasyBeans() {
		Object objs2 = constants.getAttr("easyBeanObjs");
		Map<String,EasyBeanModel> easyBeanObjs = new HashMap<>();
		if(objs2 != null) {
			easyBeanObjs = (Map<String,EasyBeanModel>)objs2;
		}
		
		return easyBeanObjs;
	}
}
