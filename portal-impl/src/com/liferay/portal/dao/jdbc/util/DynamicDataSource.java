/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.dao.jdbc.util;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.spring.hibernate.SpringHibernateThreadLocalUtil;

import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * @author Dante Wang
 */
public class DynamicDataSource implements DataSource {

	public DynamicDataSource(
		DataSource readDataSource, DataSource writeDataSource) {

		_readDataSource = readDataSource;
		_writeDataSource = writeDataSource;
	}

	@Override
	public Connection getConnection() throws SQLException {
		DataSource dataSource = _getDataSource();

		return dataSource.getConnection();
	}

	@Override
	public Connection getConnection(String userName, String password)
		throws SQLException {

		DataSource dataSource = _getDataSource();

		return dataSource.getConnection(userName, password);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		DataSource dataSource = _getDataSource();

		return dataSource.getLoginTimeout();
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		DataSource dataSource = _getDataSource();

		return dataSource.getLogWriter();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		DataSource dataSource = _getDataSource();

		return dataSource.getParentLogger();
	}

	public DataSource getReadDataSource() {
		return _readDataSource;
	}

	public DataSource getWriteDataSource() {
		return _writeDataSource;
	}

	@Override
	public boolean isWrapperFor(Class<?> clazz) throws SQLException {
		DataSource dataSource = _getDataSource();

		return dataSource.isWrapperFor(clazz);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		DataSource dataSource = _getDataSource();

		dataSource.setLoginTimeout(seconds);
	}

	@Override
	public void setLogWriter(PrintWriter printWriter) throws SQLException {
		DataSource dataSource = _getDataSource();

		dataSource.setLogWriter(printWriter);
	}

	@Override
	public <T> T unwrap(Class<T> clazz) throws SQLException {
		DataSource dataSource = _getDataSource();

		return dataSource.unwrap(clazz);
	}

	private DataSource _getDataSource() {
		if (SpringHibernateThreadLocalUtil.isCurrentTransactionReadOnly()) {
			if (_log.isTraceEnabled()) {
				_log.trace("Returning read data source");
			}

			return _readDataSource;
		}

		if (_log.isTraceEnabled()) {
			_log.trace("Returning write data source");
		}

		return _writeDataSource;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DynamicDataSource.class);

	private final DataSource _readDataSource;
	private final DataSource _writeDataSource;

}