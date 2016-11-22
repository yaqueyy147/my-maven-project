package com.syx.maven.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.syx.maven.dao.TOrderDao;
import com.syx.maven.domain.TOrder;
import com.syx.maven.service.ExerciseService;

@Service("exerciseService")
public class ExerciseServiceImpl implements ExerciseService {

	@Resource
	private TOrderDao tOrderDao;

	@Resource
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<TOrder> getOrderList() {
		
		List<TOrder> list = tOrderDao.list();
		return list;
	}

	@Override
	public List<Map<String, Object>> getOrderListJdbc() {
		String sql = "select * from t_order order by order_id desc";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
		return list;
	}

}
