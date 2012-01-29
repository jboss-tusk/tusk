package org.jboss.tusk.ui;

import org.springframework.web.servlet.mvc.AbstractController;

public abstract class AbstractTuskController extends AbstractController {

	
	protected boolean isEmpty(String val) {
		return val == null || "".equals(val);
	}
	
}
