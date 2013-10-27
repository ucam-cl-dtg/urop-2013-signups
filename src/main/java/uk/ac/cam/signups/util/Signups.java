package uk.ac.cam.signups.util;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.htmleasy.HtmleasyProviders;

import uk.ac.cam.signups.controllers.*;

public class Signups extends Application {

	private static Logger log = LoggerFactory.getLogger(Signups.class);
	public static final String APPLICATION_NAME = "signups";

	public Set<Class<?>> getClasses() {
		Set<Class<?>> myServices = new HashSet<Class<?>>();

		// Add controllers
		log.debug("Adding controllers to main application");
		myServices.add(EventsController.class);

		myServices.add(ExceptionHandler.class);

		// Add Htmleasy Providers
		log.debug("Adding Htmleasy providers");
		myServices.addAll(HtmleasyProviders.getClasses());

		return myServices;
	}
}
