package com.yuyenews.easy.util.extend;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSourceFactory;

public class MyDataSourceFactory extends DruidDataSourceFactory implements DataSourceFactory {
	
	private Logger logger = LoggerFactory.getLogger(MyDataSourceFactory.class);

	protected Properties properties;

	@Override
	public void setProperties(Properties props) {
		this.properties = props;
	}

	@Override
	public DataSource getDataSource() {
		try {
			return createDataSource(properties);
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

}
