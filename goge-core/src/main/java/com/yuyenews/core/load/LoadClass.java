package com.yuyenews.core.load;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yuyenews.core.annotation.Controller;
import com.yuyenews.core.annotation.EasyBean;
import com.yuyenews.core.util.ReadClass;
import com.yuyenews.easy.server.constant.EasySpace;

/**
 * 获取项目中的所有class
 * 
 * @author yuye
 *
 */
public class LoadClass {
	
	private static Logger log = LoggerFactory.getLogger(LoadClass.class);
	
	private static EasySpace constants = EasySpace.getEasySpace();

	/**
	 * 加载所有bean，包括controller 的class对象
	 * @param packageName
	 */
	public static void loadBeans(String packageName) {

		try {
			Set<String> classList = ReadClass.loadClassList(packageName);
			for (String str : classList) {
				Class<?> cls = Class.forName(str);
				Controller controller = cls.getDeclaredAnnotation(Controller.class);
				EasyBean easyBean = cls.getAnnotation(EasyBean.class);
				if(controller != null) {
					loadController(cls, controller);
				}
				if(easyBean != null) {
					loadEasyBean(cls, easyBean);
				}
				
			}
		} catch (Exception e) {
			log.error("扫描["+packageName+"]包下的类发送错误",e);
		}

	}
	
	/**
	 * 将所有controller存到全局存储空间
	 * @param cls
	 * @param controller
	 */
	@SuppressWarnings("unchecked")
	private static void loadController(Class<?> cls,Controller controller) {
		Object objs = constants.getAttr("contorls");
		List<Map<String,Object>> contorls = new ArrayList<>();
		if(objs != null) {
			contorls = (List<Map<String,Object>>)objs;
		} 
		Map<String,Object> contorl = new HashMap<>();
		contorl.put("className", cls);
		contorl.put("annotation", controller);
		contorls.add(contorl);
		constants.setAttr("contorls", contorls);
	}
	
	/**
	 * 将所有easybean存到全局存储空间
	 * @param cls
	 * @param controller
	 */
	@SuppressWarnings("unchecked")
	private static void loadEasyBean(Class<?> cls,EasyBean easyBean ) {
		Object objs = constants.getAttr("easyBeans");
		List<Map<String,Object>> easyBeans = new ArrayList<>();
		if(objs != null) {
			easyBeans = (List<Map<String,Object>>)objs;
		} 
		Map<String,Object> eb = new HashMap<>();
		eb.put("className", cls);
		eb.put("annotation", easyBean);
		easyBeans.add(eb);
		constants.setAttr("easyBeans", easyBeans);
	}
}
