package com.yuyenews.ioc.factory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yuyenews.aop.proxy.CglibProxy;
import com.yuyenews.core.annotation.EasyAop;
import com.yuyenews.core.annotation.EasyAopType;
import com.yuyenews.core.annotation.Traction;
import com.yuyenews.core.model.EasyBeanModel;
import com.yuyenews.easy.server.constant.EasySpace;

/**
 * bean工厂
 * @author yuye
 *
 */
public class BeanFactory {
	
	private static Logger log = LoggerFactory.getLogger(BeanFactory.class);
	
	/**
	 * 创建bean
	 * @param className lei
	 * @return duixiang
	 */
	public static Object createBean(Class<?> className) {
		try {
			
			Map<String,Class<?>> list = new HashMap<>();
			
			/* 判断当前类中有没有方法有 aop注解 */
			getAopClass(className,list);
			
			/* 如果有aop注解，则通过动态代理来创建bean */
			if(list != null && list.size()>0) {
				CglibProxy cglibProxy = new CglibProxy();
				return cglibProxy.getProxy(className, list);
			} else {
				/* 如果没有aop注解，则直接new一个bean */
				return className.getDeclaredConstructor().newInstance();
			}
			
		} catch (Exception e) {
			log.error("创建["+className.getName()+"]类型的bean对象出现错误",e);
		} 
		return null;
	}

	/**
	 * 获取aop类
	 * @param className lei
	 * @param list jihe
	 * @throws Exception cuowu
	 */
	private static void getAopClass(Class<?> className,Map<String,Class<?>> list) throws Exception {
		
		EasyAopType allEasyAop = className.getAnnotation(EasyAopType.class);
		
		Method[] methods = className.getMethods();
		for(Method method : methods) {
			EasyAop easyAop = method.getAnnotation(EasyAop.class);
			Traction traction = method.getAnnotation(Traction.class);
			
			/* 校验同一个方法上不能同时存在aop和trac注解 */
			if(easyAop != null && traction != null) {
				log.error(className.getName()+"类中的["+method.getName()+"]方法同时存在EasyAop和Traction注解");
				throw new Exception(className.getName()+"类中的["+method.getName()+"]方法同时存在EasyAop和Traction注解");
			}
			
			/* 如果类的AOP注解不为空，那么将注解中的监听类 添加到集合中 */
			if(allEasyAop != null) {
				list.put(method.getName(),allEasyAop.className());
			}
			
			/* 如果方法上也有AOP注解，那么以方法上的为准 */
			if(easyAop != null) {
				list.put(method.getName(),easyAop.className());
			} else if(traction != null) {
				Class<?> aopClass = Class.forName("com.yuyenews.easy.traction.TractionAop");
				list.put(method.getName(),aopClass);
			}
		}
	}
	
	
	/**
	 * 获取bean
	 * @param name mingc
	 * @return duix
	 */
	@SuppressWarnings("unchecked")
	public static Object getBean(String name) {
		
		try {
			EasySpace constants = EasySpace.getEasySpace();
			
			Object objs2 = constants.getAttr("easyBeanObjs");
			Map<String,EasyBeanModel> easyBeanObjs = new HashMap<>();
			if(objs2 != null) {
				easyBeanObjs = (Map<String,EasyBeanModel>)objs2;
			} 
			
			return easyBeanObjs.get(name).getObj();
		} catch (Exception e) {
			log.error("找不到name为["+name+"]的bean",e);
		}
		
		return null;
	}
}
