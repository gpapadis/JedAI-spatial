package dit.anonymous.webapp.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;


@Controller
public class CustomErrorController implements ErrorController {
	
	
	@Override
	public String getErrorPath() {
		return "/error";
	}

}
