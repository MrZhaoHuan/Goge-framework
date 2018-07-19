package com.yuyenews.easy.init;

/**
 * 初始化jdbc
 * @author yuye
 *
 */
public class InitJdbc {

	/**
	 * 加载配置
	 */
	public void init() {

		/* 加载mybatis配置 */
		LoadSqlSessionFactory.getLoadSqlSessionFactory();
		
		/* 创建dao对象 */
		LoadDaos.loadDao();
		
	}
}
