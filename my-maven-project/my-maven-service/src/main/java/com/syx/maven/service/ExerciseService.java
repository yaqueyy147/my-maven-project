package com.syx.maven.service;

import java.util.List;
import java.util.Map;

import com.syx.maven.domain.TOrder;

public interface ExerciseService {

	public List<TOrder> getOrderList();
	
	public List<Map<String, Object>> getOrderListJdbc();
	
}
