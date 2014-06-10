/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.riftsaw.engine.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Jeff Yu
 * 
 */
public class DatabaseInitialiser {

	private static Log log = LogFactory.getLog(DatabaseInitialiser.class);

	private DataSource datasource;
	
	private String existsSql = "SELECT * FROM ODE_JOB";
	
	private String sqlFiles;
	
	private TransactionManager txm;
	
	private String hibernateDialect;
	
	private boolean useEOL = false;

	public DatabaseInitialiser(DataSource ds, TransactionManager manager, String dialect) {
		datasource = ds;
		txm = manager;
		hibernateDialect = dialect;
	}

	protected void initDatabase() throws Exception {
		Connection conn = datasource.getConnection();
		boolean load = false;

		Statement st = conn.createStatement();
		ResultSet rs = null;
		try {
			rs = st.executeQuery(existsSql.trim());
			rs.close();
		} catch (SQLException e) {
			load = true;
		}
		st.close();
		if (!load) {
			log.info(datasource + " datasource is already initialized");
			return;
		}
		
		sqlFiles = getSqlFile(hibernateDialect);
		
		log.info("Initializing " + datasource + " from listed sql files " + sqlFiles);
		String[] list = sqlFiles.split(",");
		for (String sql : list) {
			executeSql(sql.trim(), conn);
		}
	}

	public void executeSql(String resource, Connection conn) {
		try {
			txm.begin();

			log.debug("Execute SQL from resource: " + resource);

			URL url = getClass().getClassLoader().getResource(resource);

			log.debug("Execute SQL from resource URL: " + url);

			String sql = getStringFromStream(url.openStream());
			sql = sql.replaceAll("(?m)^--([^\n]+)?$", ""); // Remove all
															// commented lines
			final String[] statements;
			if (useEOL) {
				statements = sql.split("[\n;]");
			} else {
				statements = sql.split(";");
			}
			for (String statement : statements) {
				if ((statement == null) || ("".equals(statement.trim()))) {
				} else {
					Statement sqlStatement = conn.createStatement();
					try {
						sqlStatement.executeUpdate(statement);
					} finally {
						sqlStatement.close();
					}
				}
			}

			txm.commit();
		} catch (Throwable t) {
			if (txm != null)
				try {
					txm.rollback();
				} catch (SystemException e) {
					//
				}
			throw new RuntimeException("Failed to create database", t);
		}
	}

	private String getStringFromStream(InputStream is) throws Exception {
		byte[] bytes = readStream(is);
		return new String(bytes, "UTF-8");

	}

	/**
	 * Read the supplied InputStream and return as an array of bytes.
	 * 
	 * @param stream
	 *            The stream to read.
	 * @return The stream contents in an array of bytes.
	 */
	public static byte[] readStream(InputStream stream) {
		if (stream == null) {
			throw new IllegalArgumentException(
					"null 'stream' arg passed in method call.");
		}

		ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
		byte[] buffer = new byte[256];
		int readCount = 0;

		try {
			while ((readCount = stream.read(buffer)) != -1) {
				outBuffer.write(buffer, 0, readCount);
			}
		} catch (IOException e) {
			throw new IllegalStateException("Error reading stream.", e);
		}

		return outBuffer.toByteArray();
	}
	
	private String getSqlFile(String hibernateDialect) {
		if (hibernateDialect.indexOf("H2Dialect") != -1)
			return "sqls/h2.sql";
		if (hibernateDialect.indexOf("MySQLInnoDBDialect") != -1) 
			return "sqls/mysql.sql";
		if (hibernateDialect.indexOf("DB2Dialect") != -1) 
			return "sqls/db2.sql";
		if (hibernateDialect.indexOf("Oracle") != -1) 
			return "sqls/oracle.sql";
		if (hibernateDialect.indexOf("PostgreSQLDialect") != -1) 
			return "sqls/postgres.sql";
		if (hibernateDialect.indexOf("SQLServerDialect") != -1) 
			return "sqls/sqlserver.sql";
		if (hibernateDialect.indexOf("SybaseDialect") != -1) 
			return "sqls/sybase.sql";
		throw new RuntimeException("Couldn't find any corresponding sql file for " + hibernateDialect);
	}

}
