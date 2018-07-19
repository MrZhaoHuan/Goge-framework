package com.yuyenews.easy.proxy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.SqlSession;

import com.yuyenews.core.annotation.DataSource;
import com.yuyenews.core.util.ThreadUtil;
import com.yuyenews.easy.init.LoadSqlSessionFactory;
import com.yuyenews.easy.server.constant.EasySpace;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * 代理类
 * @author yuye
 *
 */
public class MappersProxy implements MethodInterceptor {
	
	private EasySpace easySpace = EasySpace.getEasySpace();

	private LoadSqlSessionFactory loadSqlSessionFactory = LoadSqlSessionFactory.getLoadSqlSessionFactory();
	
	private Enhancer enhancer;
	
	private Class<?> cls;
	
	/**
	 * 获取代理对象
	 * @param clazz  bean的class
	 * @param list aop类的class
	 * @return 对象
	 */
	public Object getProxy(Class<?> clazz) {
		
		this.cls = clazz;
		
		enhancer = new Enhancer();
		// 设置需要创建子类的类
		enhancer.setSuperclass(clazz);
		enhancer.setCallback(this);
		// 通过字节码技术动态创建子类实例
		return enhancer.create();
	}
	
	
	/**
	 * 绑定代理
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
		
		/* 获取当前线程中的sqlSession */
		Object obj =  easySpace.getAttr(ThreadUtil.getThreadIdToTraction());
		
		/* 用来执行sql的sqlSession */
		SqlSession session = null;
		
		/* 是否需要手动关闭sqlSession（默认不需要） */
		Boolean flag = false;
		
		/* 返回数据 */
		Object result = null;
		
		/* 获取数据源名称 */
		String dataSourceName = getDataSourceName(method);
		
		if(obj != null) {
			/* 如果当前线程中有sqlSession 则从当前线程中获取sqlSession */
			Map<String,SqlSession> sqlSessions = (Map<String,SqlSession>)obj;
			session = sqlSessions.get(dataSourceName);
		} else {
			/* 否则 手动获取（这种情况，当执行完以后需要手动关闭sqlSession） */
			session = loadSqlSessionFactory.getSqlSession(dataSourceName, false);
			flag = true;
		}
		
		/* 获取要执行的sql的ID */
		String statement = this.cls.getName()+"." + method.getName();
		
		/* 根据要执行的sql的ID 获取这条sql的类型是select还是update */
		SqlCommandType tag = session.getConfiguration().getMappedStatement(statement).getSqlCommandType();
		String commType = tag.toString().toUpperCase();
		if(commType.equals("SELECT")) {
			/* 如果是select，则判断方法的返回值是不是list */
			Class<?> returnType = method.getReturnType();
			if(returnType.getName().equals(List.class.getName())) {
				/* 如果方法的返回值是list，则执行selectList方法 */
				if(args != null && args[0] != null) {
					result = session.selectList(statement, args[0]);
				} else {
					result = session.selectList(statement);
				}
			} else {
				/* 如果不是list，则执行selectOne方法 */
				if(args != null && args[0] != null) {
					result = session.selectOne(statement, args[0]);
				} else {
					result = session.selectOne(statement);
				}
			}
			
		} else if(commType.equals("UPDATE") || commType.equals("INSERT") || commType.equals("DELETE")) {
			/* 如果要执行的sql是update类型（增删改），则执行update方法 */
			if(args != null && args[0] != null) {
				result = session.update(statement, args[0]);
			} else {
				result = session.update(statement);
			}
			
			if(flag) {
				/* 如果sqlSession是手动获取的，那么执行完以后要立刻提交事务 */
				session.commit();
			}
		}
		
		if(flag) {
			/* 手工关闭sqlSession 节约回收的开销 */
			session.close();
		}
		
		return result;
	}
	
	/**
	 * 获取数据源名称
	 * @param method
	 * @return
	 */
	private String getDataSourceName(Method method) {
		String dataSourceName = null;
		DataSource dataSource = method.getAnnotation(DataSource.class);
		if(dataSource != null) {
			/* 如果dao的方法上游DataSource注解，则使用注解中的数据源名称 */
			dataSourceName = dataSource.name();
		} else {
			/* 否则使用默认数据源名称 */
			dataSourceName = easySpace.getAttr("defaultDataSource").toString();
		}
		return dataSourceName;
	}
}
