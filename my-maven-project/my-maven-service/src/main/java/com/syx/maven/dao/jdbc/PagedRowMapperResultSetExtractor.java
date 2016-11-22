/**
 * Copyright 2007-2008. Chongqing First Information & Network Co., Ltd. All rights reserved.
 * <a>http://www.cqfirst.com.cn</a>
 */
package com.syx.maven.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import com.syx.maven.page.Page;

/**
 * 页映射:当前页,页大小,记录数
 * 
 * @author liuhz create 2007-12-8
 */
@SuppressWarnings("unchecked")
public class PagedRowMapperResultSetExtractor implements ResultSetExtractor {
	private final RowMapper rowMapper;
	private final int pageNo;
	private final int pageSize;
	private final int totalCount;

	public PagedRowMapperResultSetExtractor(RowMapper rowMapper, int totalCount) {
		this.rowMapper = rowMapper;
		this.pageNo = 1;
		this.pageSize = Page.DEFAULT_PAGE_SIZE;
		this.totalCount = totalCount;
	}

	public PagedRowMapperResultSetExtractor(RowMapper rowMapper, int pageNo, int pageSize, int totalCount) {
		this.rowMapper = rowMapper;
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		this.totalCount = totalCount;
	}

	public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
		if (this.totalCount < 1)
			return new Page();

		List results = new ArrayList(this.pageSize);
		int startIndex = Page.getStartOfPage(this.pageNo, this.pageSize);
		int rowNum = 0;
		rs.absolute(startIndex);
		while (rs.next() && rowNum < this.pageSize) {
			results.add(this.rowMapper.mapRow(rs, startIndex + rowNum++));
		}
		return new Page(results, startIndex, this.totalCount, this.pageSize);
	}

}
