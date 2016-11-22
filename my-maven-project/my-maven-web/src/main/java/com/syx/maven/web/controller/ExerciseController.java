package com.syx.maven.web.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.syx.maven.domain.TOrder;
import com.syx.maven.service.ExerciseService;

@Controller
@RequestMapping(value = "test")
public class ExerciseController {

	@Autowired
	private ExerciseService exerciseService;
	
	@RequestMapping(value = "testA")
	public ModelAndView testA(Model model){
		
		List<Map<String, Object>> list = exerciseService.getOrderListJdbc();
		List<TOrder> list2 = exerciseService.getOrderList();
		model.addAttribute("testOrder1", list.get(0));
		model.addAttribute("testOrder2", list2.get(0));
		model.addAttribute("test", "这是一个testA");
		return new ModelAndView("testA");
	}
	
}
