/**
 * Copyright 2007-2008. Chongqing First Information & Network Co., Ltd. All rights reserved.
 * <a>http://www.cqfirst.com.cn</a>
 */
package com.syx.maven.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.SQLWarningException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.syx.maven.page.Page;
import com.syx.maven.util.SqlUtils;

/**
 * 对NamedParameterJdbcTemplate的扩展,以支持Page查询
 * 
 * @author liuhz create 2007-12-4
 */
@SuppressWarnings("unchecked")
public class BaseJdbcTemplate extends NamedParameterJdbcTemplate {
	protected final Log log = LogFactory.getLog(BaseJdbcTemplate.class);
	private JdbcTemplate jdbcTemplate;

	public BaseJdbcTemplate(DataSource dataSource) {
		super(dataSource);
		this.jdbcTemplate = (JdbcTemplate) this.getJdbcOperations();
	}

	public BaseJdbcTemplate(JdbcOperations classicJdbcTemplate) {
		super(classicJdbcTemplate);
		this.jdbcTemplate = (JdbcTemplate) this.getJdbcOperations();
	}

//	public BaseJdbcTemplate(NamedParameterJdbcOperations namedParameterJdbcTemplate) {
//		super(namedParameterJdbcTemplate);
//		this.jdbcTemplate = (JdbcTemplate) this.getJdbcOperations();
//	}

	// =======================================BeanQuery support============================================
	@SuppressWarnings("unchecked")
	public <T> T queryForBean(String sql, Class<T> requiredType, Map args) throws DataAccessException {
		return (T) super.queryForObject(sql, args,
				ParameterizedBeanPropertyRowMapper.newInstance(requiredType));
	}

	@SuppressWarnings("unchecked")
	public <T> T queryForBean(String sql, Class<T> requiredType, SqlParameterSource args) throws DataAccessException {
		return (T) super.queryForObject(sql, args,
				ParameterizedBeanPropertyRowMapper.newInstance(requiredType));
	}

	@SuppressWarnings("unchecked")
	public <T> T queryForBean(String sql, Class<T> requiredType, Object... args) throws DataAccessException {
		return (T) (ObjectUtils.isEmpty(args) ? getJdbcOperations().queryForObject(sql,
				ParameterizedBeanPropertyRowMapper.newInstance(requiredType)) : getJdbcOperations().queryForObject(sql,
				getArguments(args), ParameterizedBeanPropertyRowMapper.newInstance(requiredType)));
	}

	@SuppressWarnings("unchecked")
	public <T> T queryForBean(String sql, ParameterizedRowMapper<T> rm, Map args) throws DataAccessException {
		return (T) super.queryForObject(sql, args, rm);
	}

	@SuppressWarnings("unchecked")
	public <T> T queryForBean(String sql, ParameterizedRowMapper<T> rm, SqlParameterSource args)
			throws DataAccessException {
		return (T) super.queryForObject(sql, args, rm);
	}

	@SuppressWarnings("unchecked")
	public <T> T queryForBean(String sql, ParameterizedRowMapper<T> rm, Object... args) throws DataAccessException {
		return (T) (ObjectUtils.isEmpty(args) ? getJdbcOperations().queryForObject(sql, rm) : getJdbcOperations()
				.queryForObject(sql, getArguments(args), rm));
	}

	// =======================================PageQuery Support============================================
	/**
	 * 支持游标的查询
	 */
	public Object queryScrollAble(final String sql, final ResultSetExtractor rse) throws DataAccessException {
		Assert.notNull(sql, "SQL must not be null");
		Assert.notNull(rse, "ResultSetExtractor must not be null");
		if (log.isDebugEnabled()) {
			log.debug("Executing SQL query [" + sql + "]");
		}

		class QueryStatementCallback implements StatementCallback, SqlProvider {
			public Object doInStatement(Statement stmt) throws SQLException {
				ResultSet rs = null;
				try {
					rs = stmt.executeQuery(sql);
					ResultSet rsToUse = rs;
					if (jdbcTemplate.getNativeJdbcExtractor() != null) {
						rsToUse = jdbcTemplate.getNativeJdbcExtractor().getNativeResultSet(rs);
					}
					return rse.extractData(rsToUse);
				} finally {
					JdbcUtils.closeResultSet(rs);
				}
			}

			public String getSql() {
				return sql;
			}
		}

		class ScrollAbleConnectionCallback implements ConnectionCallback {
			private StatementCallback action = new QueryStatementCallback();

			public Object doInConnection(Connection conn) throws SQLException, DataAccessException {
				Statement stmt = null;
				try {
					stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					applyStatementSettings(stmt);
					Statement stmtToUse = stmt;
					if (jdbcTemplate.getNativeJdbcExtractor() != null) {
						stmtToUse = jdbcTemplate.getNativeJdbcExtractor().getNativeStatement(stmt);
					}
					Object result = action.doInStatement(stmtToUse);
					handleWarnings(stmt.getWarnings());
					return result;
				} catch (SQLException ex) {
					JdbcUtils.closeStatement(stmt);
					stmt = null;
					throw jdbcTemplate.getExceptionTranslator().translate("StatementCallback", sql, ex);
				} finally {
					JdbcUtils.closeStatement(stmt);
				}
			}
		}

		return this.jdbcTemplate.execute(new ScrollAbleConnectionCallback());
	}

	/**
	 * 不带参数分页查询
	 * 
	 * @param sql SQL字串
	 * @param pageNo 当前页
	 * @param pageSize 页大小
	 */
	public Page pagedQuery(String sql, int pageNo, int pageSize) {
		return pagedQuery(sql, pageNo, pageSize, -1);
	}

	/**
	 * 不带参数分页查询
	 * 
	 * @param sql SQL字串
	 * @param pageNo 当前页
	 * @param pageSize 页大小
	 * @param totalCount 指定总数
	 */
	public Page pagedQuery(String sql, int pageNo, int pageSize, int totalCount) {
		Assert.notNull(sql, "SQL must not be null");
		if (totalCount == -1) {
			String countSqlString = "select count(*) " + SqlUtils.removeSelect(SqlUtils.removeOrders(sql));
			totalCount = super.getJdbcOperations().queryForInt(countSqlString);
		}

		if (totalCount < 1) {
			return new Page();
		}

		sql = SqlUtils.limitSelect(sql, pageNo * pageSize);
		return (Page) queryScrollAble(sql, new PagedRowMapperResultSetExtractor(new ColumnMapRowMapper(), pageNo,
				pageSize, totalCount));
	}

	/**
	 * 带参数分页查询
	 * 
	 * @param sql SQL字串
	 * @param pageNo 当前页
	 * @param pageSize 页大小
	 * @param args 参数
	 */
	public Page pagedQuery(String sql, int pageNo, int pageSize, Object... args) {
		return pagedQuery(sql, pageNo, pageSize, -1, args);
	}

	/**
	 * 带参数分页查询
	 * 
	 * @param sql SQL字串
	 * @param pageNo 当前页
	 * @param pageSize 页大小
	 * @param totalCount 记录总数
	 * @param args 参数
	 */
	public Page pagedQuery(String sql, int pageNo, int pageSize, int totalCount, Object... args) {
		Assert.notNull(sql, "SQL must not be null");
		if (totalCount == -1) {
			String countSqlString = "select count(*) " + SqlUtils.removeSelect(SqlUtils.removeOrders(sql));
			totalCount = super.getJdbcOperations().queryForInt(countSqlString, args);
		}

		if (totalCount < 1) {
			return new Page();
		}

		PagedRowMapperResultSetExtractor extractor = new PagedRowMapperResultSetExtractor(new ColumnMapRowMapper(),
				pageNo, pageSize, totalCount);

		sql = SqlUtils.limitSelect(sql, pageNo * pageSize);
		if (ObjectUtils.isEmpty(args)) {
			return (Page) queryScrollAble(sql, extractor);
		} else {
			ArgPreparedStatementSetter setter = new ArgPreparedStatementSetter(getArguments(args));
			ScrollAblePreparedStatementCreator creator = new ScrollAblePreparedStatementCreator(sql);

			return (Page) this.jdbcTemplate.query(creator, setter, extractor);
		}
	}
	
	public Page pagedQueryMySql(String sql, int pageNo, int pageSize, int totalCount, Class<?> requiredType, Object... args){
		if (totalCount == -1) {
			String countSqlString = "select count(*) " + SqlUtils.removeSelect(SqlUtils.removeOrders(sql));
			totalCount = super.getJdbcOperations().queryForInt(countSqlString, args);
		}
		if (totalCount < 1) {
			return new Page();
		}
		List results=new ArrayList();
		int start=(pageNo-1)*pageSize;
		int end =start+pageSize;
		sql=sql+" limit "+start+","+end;
		results=super.getJdbcOperations().query(sql, ParameterizedBeanPropertyRowMapper.newInstance(requiredType),args);
		Page page=new Page(results,start,totalCount,pageSize);	
		return page;
	}
	
	public Page pagedQueryMySql(String sql, int pageNo, int pageSize,Class<?> requiredType, Object... args){
		Page page=pagedQueryMySql(sql, pageNo, pageSize, -1, requiredType, args);	
		return page;
	}
	

	/**
	 * 带参数分页查询
	 * 
	 * @param sql SQL字串
	 * @param pageNo 当前页
	 * @param pageSize 页大小
	 * @param totalCount 记录总数
	 * @param args 参数
	 * @param requiredType 每行数据返回类型
	 * @return
	 */
	public Page pagedQuery(String sql, int pageNo, int pageSize, int totalCount, Class<?> requiredType, Object... args) {
		Assert.notNull(sql, "SQL must not be null");
		if (totalCount == -1) {
			String countSqlString = "select count(*) " + SqlUtils.removeSelect(SqlUtils.removeOrders(sql));
			totalCount = super.getJdbcOperations().queryForInt(countSqlString, args);
		}

		if (totalCount < 1) {
			return new Page();
		}

		PagedRowMapperResultSetExtractor extractor = new PagedRowMapperResultSetExtractor(
				ParameterizedBeanPropertyRowMapper.newInstance(requiredType), pageNo, pageSize, totalCount);

		sql = SqlUtils.limitSelect(sql, pageNo * pageSize);
		if (ObjectUtils.isEmpty(args)) {
			return (Page) queryScrollAble(sql, extractor);
		} else {
			ArgPreparedStatementSetter setter = new ArgPreparedStatementSetter(getArguments(args));
			ScrollAblePreparedStatementCreator creator = new ScrollAblePreparedStatementCreator(sql);

			return (Page) this.jdbcTemplate.query(creator, setter, extractor);
		}
	}
	
	/**
	 * 带参数分页查询
	 * 
	 * @param sql SQL字串
	 * @param pageNo 当前页
	 * @param pageSize 页大小
	 * @param totalCount 记录总数
	 * @param args 参数
	 * @param requiredType 每行数据返回类型
	 * @return
	 */
	public Page pagedQueryCommand(String command,String sql, int pageNo, int pageSize, int totalCount, Class<?> requiredType, Object... args) {
		Assert.notNull(sql, "SQL must not be null");
		if (totalCount == -1) {
			String countSqlString = "select count(*) " + SqlUtils.removeSelect(SqlUtils.removeOrders(sql));
			totalCount = super.getJdbcOperations().queryForInt(countSqlString, args);
		}

		if (totalCount < 1) {
			return new Page();
		}

		PagedRowMapperResultSetExtractor extractor = new PagedRowMapperResultSetExtractor(
				ParameterizedBeanPropertyRowMapper.newInstance(requiredType), pageNo, pageSize, totalCount);

		sql = SqlUtils.limitSelectCommand(sql, pageNo * pageSize,command);
		if (ObjectUtils.isEmpty(args)) {
			return (Page) queryScrollAble(sql, extractor);
		} else {
			ArgPreparedStatementSetter setter = new ArgPreparedStatementSetter(getArguments(args));
			ScrollAblePreparedStatementCreator creator = new ScrollAblePreparedStatementCreator(sql);

			return (Page) this.jdbcTemplate.query(creator, setter, extractor);
		}
	}

	/**
	 * 带参数分页查询
	 * 
	 * @param sql SQL字串
	 * @param pageNo 当前页
	 * @param pageSize 页大小
	 * @param args 参数
	 * @param requiredType 每行数据返回类型
	 * @return
	 */
	public Page pagedQuery(String sql, int pageNo, int pageSize, Class<?> requiredType, Object... args) {

		return pagedQuery(sql, pageNo, pageSize, -1, requiredType, args);

	}

	// ============================================================================================
	private static class ScrollAblePreparedStatementCreator implements PreparedStatementCreator, SqlProvider {
		private final String sql;

		public ScrollAblePreparedStatementCreator(String sql) {
			Assert.notNull(sql, "SQL must not be null");
			this.sql = sql;
		}

		public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
			return con.prepareStatement(this.sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		}

		public String getSql() {
			return this.sql;
		}

	}

	private static class ArgPreparedStatementSetter implements PreparedStatementSetter, ParameterDisposer {

		private final Object[] args;

		/**
		 * Create a new ArgPreparedStatementSetter for the given arguments.
		 * 
		 * @param args the arguments to set
		 */
		public ArgPreparedStatementSetter(Object[] args) {
			this.args = args;
		}

		public void setValues(PreparedStatement ps) throws SQLException {
			if (this.args != null) {
				for (int i = 0; i < this.args.length; i++) {
					Object arg = this.args[i];
					if (arg instanceof SqlParameterValue) {
						SqlParameterValue paramValue = (SqlParameterValue) arg;
						StatementCreatorUtils.setParameterValue(ps, i + 1, paramValue, paramValue.getValue());
					} else {
						StatementCreatorUtils.setParameterValue(ps, i + 1, SqlTypeValue.TYPE_UNKNOWN, arg);
					}
				}
			}
		}

		public void cleanupParameters() {
			StatementCreatorUtils.cleanupParameters(this.args);
		}
	}

	/**
	 * Prepare the given JDBC Statement (or PreparedStatement or CallableStatement), applying statement settings such as
	 * fetch size, max rows, and query timeout.
	 * 
	 * @param stmt the JDBC Statement to prepare
	 * @throws SQLException if thrown by JDBC API
	 * @see #setFetchSize
	 * @see #setMaxRows
	 * @see #setQueryTimeout
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#applyTransactionTimeout
	 */
	private void applyStatementSettings(Statement stmt) throws SQLException {
		int fetchSize = this.jdbcTemplate.getFetchSize();
		if (fetchSize > 0) {
			stmt.setFetchSize(fetchSize);
		}
		int maxRows = this.jdbcTemplate.getMaxRows();
		if (maxRows > 0) {
			stmt.setMaxRows(maxRows);
		}
		DataSourceUtils.applyTimeout(stmt, this.jdbcTemplate.getDataSource(), this.jdbcTemplate.getQueryTimeout());
	}

	/**
	 * Throw an SQLWarningException if we're not ignoring warnings, else log the warnings (at debug level).
	 * 
	 * @param warning the warnings object from the current statement. May be <code>null</code>, in which case this
	 *            method does nothing.
	 * @throws SQLWarningException if not ignoring warnings
	 * @see org.springframework.jdbc.SQLWarningException
	 */
	private void handleWarnings(SQLWarning warning) throws SQLWarningException {
		if (warning != null) {
			if (this.jdbcTemplate.isIgnoreWarnings()) {
				if (log.isDebugEnabled()) {
					SQLWarning warningToLog = warning;
					while (warningToLog != null) {
						log.debug("SQLWarning ignored: SQL state '" + warningToLog.getSQLState() + "', error code '"
								+ warningToLog.getErrorCode() + "', message [" + warningToLog.getMessage() + "]");
						warningToLog = warningToLog.getNextWarning();
					}
				}
			} else {
				throw new SQLWarningException("Warning not ignored", warning);
			}
		}
	}

	/**
	 * Considers an Object array passed into a varargs parameter as collection of arguments rather than as single
	 * argument.
	 */
	private Object[] getArguments(Object[] varArgs) {
		if (varArgs.length == 1 && varArgs[0] instanceof Object[]) {
			return (Object[]) varArgs[0];
		} else {
			return varArgs;
		}
	}
}
