package com.javatpoint.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import com.javatpoint.beans.Emp;

@Controller
public class EmpController {
	/*
	 * @Autowired EmpDao dao;// will inject dao from xml file
	 */
	@Value("${emp_host}")
	String empHost;
	
	private boolean setConverters = false;

	private String getEmpURL() {
		if(!setConverters) {
			addConverter();
		}
		return "http://" + empHost + ":9001/api/";
	}
	
	private void addConverter() {
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();        
		//Add the Jackson Message converter
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		
		// Note: here we are making this converter to process any kind of response, 
		// not only application/*json, which is the default behaviour
		converter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));        
		messageConverters.add(converter);  
		restTemplate.setMessageConverters(messageConverters);
		setConverters = true;

	}
	
	
	RestTemplate restTemplate = new RestTemplate();

	/*
	 * It displays a form to input data, here "command" is a reserved request
	 * attribute which is used to display object data into form
	 */
	@RequestMapping("/empform")
	public String showform(Model m) {
		m.addAttribute("command", new Emp());
		return "empform";
	}

	/*
	 * It saves object into database. The @ModelAttribute puts request data into
	 * model object. You need to mention RequestMethod.POST method because default
	 * request is GET
	 */
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String save(@ModelAttribute("emp") Emp emp) {
		//dao.save(emp);
		HttpEntity<Emp> request = new HttpEntity<Emp>(emp);
		restTemplate.postForObject(getEmpURL() + "save", request, Emp.class);
		return "redirect:/viewemp";// will redirect to viewemp request mapping
	}

	/* It provides list of employees in model object */
	@RequestMapping("/viewemp")
	public String viewemp(Model m) {
		ResponseEntity<List> response = restTemplate.getForEntity(getEmpURL() + "all", List.class);

		List<Emp> list = response.getBody();
		// List<Emp> list=dao.getEmployees();
		m.addAttribute("list", list);
		return "viewemp";
	}

	/*
	 * It displays object data into form for the given id. The @PathVariable puts
	 * URL data into variable.
	 */
	@RequestMapping(value = "/editemp/{id}")
	public String edit(@PathVariable int id, Model m) {

		// Emp emp=dao.getEmpById(id);
		Emp emp = restTemplate.getForObject(getEmpURL() + id, Emp.class);
		m.addAttribute("command", emp);
		return "empeditform";
	}

	/* It updates model object. */
	@RequestMapping(value = "/editsave", method = RequestMethod.POST)
	public String editsave(@ModelAttribute("emp") Emp emp) {
		HttpEntity<Emp> request = new HttpEntity<Emp>(emp);
		restTemplate.postForObject(getEmpURL() + "save", request, Emp.class);
		// dao.update(emp);
		return "redirect:/viewemp";
	}

	/* It deletes record for the given id in URL and redirects to /viewemp */
	@RequestMapping(value = "/deleteemp/{id}", method = RequestMethod.GET)
	public String delete(@PathVariable int id) {
		restTemplate.delete(getEmpURL() + "delete/" + id);
		// dao.delete(id);
		return "redirect:/viewemp";
	}
}