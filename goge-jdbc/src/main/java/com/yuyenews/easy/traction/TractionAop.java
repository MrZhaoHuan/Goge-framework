package com.yuyenews.easy.traction;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yuyenews.aop.base.BaseAop;
import com.yuyenews.core.util.ThreadUtil;
import com.yuyenews.easy.server.constant.EasySpace;

/**
 * 事务管理aop
 * 
 * 目前用了一个很笨的方法
 * 一下次获取所有数据源的连接，然后一下子提交所有数据源的连接，或者一下子回滚所有数据源的连接
 * 
 * @author yuye
 *
 */
public class TractionAop implements BaseAop {

	private Logger logger = LoggerFactory.getLogger(TractionAop.class);

	private static EasySpace commontSpace = EasySpace.getEasySpace();
	
	/**
	 * 获取数据库连接，并设置为不自动提交
	 * 
	 * 将获取到的连接 放到缓存中
	 * 
	 * @param args canshu
	 */
	@SuppressWarnings("unchecked")
	public void startMethod(Object[] args) {
		try {
			Map<String,SqlSessionFactory> maps = (Map<String,SqlSessionFactory>)commontSpace.getAttr("sqlSessionFactorys");
			
			Map<String,SqlSession> sqlSessions = new HashMap<>();
			
			for(String key : maps.keySet()) {
				sqlSessions.put(key, maps.get(key).openSession(false));
			}
			
			commontSpace.setAttr(ThreadUtil.getThreadIdToTraction(), sqlSessions);
		} catch (Exception e) {
			logger.error("开启事务出错",e);
		}
	}

	/**
	 * 从缓存中获取当前线程的数据库连接，并提交事务
	 * 
	 * @param args canshu
	 */
	public void endMethod(Object[] args) {
		try {
			@SuppressWarnings("unchecked")
			Map<String,SqlSession> sqlSessions = (Map<String,SqlSession>)commontSpace.getAttr(ThreadUtil.getThreadIdToTraction());

			for(String key : sqlSessions.keySet()) {
				SqlSession session = sqlSessions.get(key);
				session.commit();
				session.close();
			}
		} catch (Exception e) {
			logger.error("提交事务出错",e);
		} finally {
			commontSpace.remove(ThreadUtil.getThreadIdToTraction());
		}
		
	}

	/**
	 * 从缓存中获取当前线程的数据库连接，并回滚事务
	 */
	public void exp() {
		try {
			@SuppressWarnings("unchecked")
			Map<String,SqlSession> sqlSessions = (Map<String,SqlSession>)commontSpace.getAttr(ThreadUtil.getThreadIdToTraction());

			for(String key : sqlSessions.keySet()) {
				SqlSession session = sqlSessions.get(key);
				session.rollback();
				session.close();
			}
			
		} catch (Exception e) {
			logger.error("回滚事务出错",e);
		} finally {
			commontSpace.remove(ThreadUtil.getThreadIdToTraction());
		}
	}

}
