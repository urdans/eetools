package com.urdans.eetools.controllers;


import eecalcs.ConductorProperties;
import eecalcs.SystemACVoltages;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class MainController {

	@RequestMapping(value = "", method = RequestMethod.GET)
	public String index(Model model){
		return "index/index";
	}

	@RequestMapping(value = "basictools/voltagedrop", method = RequestMethod.GET)
	public String basicToolsVoltageDrop(Model model){
		model.addAttribute("acvoltages", SystemACVoltages.getVoltages());
		model.addAttribute("wiresizes", ConductorProperties.wireSizeFullName);
		return "basictools/voltagedrop";
	}

/*TODO
*  Implement maximum voltage drop calculator*/

	@RequestMapping(value = "basictools/conductorsizingpervoltagedrop", method = RequestMethod.GET)
	public String basicToolsMaxVoltageDrop(Model model){
		model.addAttribute("acvoltages", SystemACVoltages.getVoltages());
//		model.addAttribute("wiresizes", ConductorProperties.wireSizeFullName);
		return "basictools/conductorsizingpervoltagedrop";
	}


}
