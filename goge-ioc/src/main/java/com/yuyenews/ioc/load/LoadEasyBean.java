package com.yuyenews.ioc.load;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yuyenews.core.annotation.EasyBean;
import com.yuyenews.core.annotation.Resource;
import com.yuyenews.core.model.EasyBeanModel;
import com.yuyenews.core.util.StringUtil;
import com.yuyenews.easy.server.constant.EasySpace;
import com.yuyenews.ioc.factory.BeanFactory;

/**
 * 加载easyBean
 * @author yuye
 *
 */
public class LoadEasyBean {
	
	private static Logger log = LoggerFactory.getLogger(LoadEasyBean.class);
	
	/**
	 * 获取全局存储空间 
	 */
	private static EasySpace constants = EasySpace.getEasySpace();

	/**
	 * 创建easyBean对象，并完成对象注入
	 */
	@SuppressWarnings({ "unchecked" })
	public static void loadBean() {
		try {
			/* 获取所有的bean数据 */
			Object objs = constants.getAttr("easyBeans");
			List<Map<String,Object>> contorls = null;
			if(objs != null) {
				contorls = (List<Map<String,Object>>)objs;
			} else {
				return;
			}
			
			/* 创建bean对象，并保存起来 */
			Object objs2 = constants.getAttr("easyBeanObjs");
			Map<String,EasyBeanModel> easyBeanObjs = new HashMap<>();
			if(objs2 != null) {
				easyBeanObjs = (Map<String,EasyBeanModel>)objs2;
			} 
			for(Map<String,Object> map : contorls) {
				
				Class<?> cls = (Class<?>)map.get("className");
				EasyBean easyBean = (EasyBean)map.get("annotation");
				
				String beanName = easyBean.name();
				if(beanName == null || beanName.equals("")) {
					beanName = StringUtil.getFirstLowerCase(cls.getSimpleName());
				}
				if(easyBeanObjs.get(beanName) == null) {
					EasyBeanModel beanModel = new EasyBeanModel();
					beanModel.setName(beanName);
					beanModel.setCls(cls);
					beanModel.setObj(BeanFactory.createBean(cls));
					easyBeanObjs.put(beanName, beanModel);
				} else {
					throw new Exception("已经存在name为["+beanName+"]的bean了");
				}
			}
			/* 注入对象 */
			iocBean(easyBeanObjs);
		} catch (Exception e) {
			log.error("加载并注入EasyBean的时候出现错误",e);
		} 
	}
	
	/**
	 * easyBean注入
	 * @param easyBeanObjs 对象
	 */
	private static void iocBean(Map<String,EasyBeanModel> easyBeanObjs) {
		
		try {
			for(String key : easyBeanObjs.keySet()) {
				EasyBeanModel easyBeanModel = easyBeanObjs.get(key);
				Object obj = easyBeanModel.getObj();
				Class<?> cls = easyBeanModel.getCls();
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
				/* 保险起见，重新插入数据 */
				easyBeanModel.setCls(cls);
				easyBeanObjs.put(key, easyBeanModel);
			}
			
			constants.setAttr("easyBeanObjs", easyBeanObjs);
		} catch (Exception e) {
			log.error("加载并注入EasyBean的时候出现错误",e);
		} 
	}
}
