/**
 * 
 */
package com.syx.maven.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author lixj
 *
 */
public class PageResultSetExtractor<T> implements ResultSetExtractor<List<T>> {
	private final int startIndex; // 起始行号
	private final int endindex; // 结束行号
	private final RowMapper<T> rowMapper;

	public PageResultSetExtractor(RowMapper<T> rowMapper, int startIndex, int endindex) {
		this.rowMapper = rowMapper;
		this.startIndex = startIndex;
		this.endindex = endindex;
	}

	/**
	 * 处理结果集合,被接口自动调用，该类外边不应该调用
	 */

	public List<T> extractData(ResultSet rs) throws SQLException {
		List<T> result = new ArrayList<T>();
		if(startIndex == 1) {
			;
		} else {
			rs.first();
			rs.relative(startIndex - 2);
		}	
		int count = 0;
		while (rs.next()) {
			result.add(this.rowMapper.mapRow(rs, startIndex + count));
			if (startIndex + count >= endindex) {
				break;
			}
			count++;
		}
		return result;
	}
}
