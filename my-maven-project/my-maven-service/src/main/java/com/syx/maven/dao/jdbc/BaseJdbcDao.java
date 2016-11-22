/**
 * Copyright 2007-2008. Chongqing First Information & Network Co., Ltd. All rights reserved.
 * <a>http://www.cqfirst.com.cn</a>
 */
package com.syx.maven.dao.jdbc;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * 扩展SimpleJdbcDaoSupport,以支持Page查询
 * @author liuhz create 2007-12-4
 */
public class BaseJdbcDao extends JdbcDaoSupport {
	public static final long MAX_ROW = 20000;
	private BaseJdbcTemplate baseJdbcTemplate;

	@Override
	protected void initTemplateConfig() {
		this.baseJdbcTemplate = new BaseJdbcTemplate(getJdbcTemplate());
	}

	public BaseJdbcTemplate getBaseJdbcTemplate() {
		return baseJdbcTemplate;
	}

	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		return this.baseJdbcTemplate;
	}
}
