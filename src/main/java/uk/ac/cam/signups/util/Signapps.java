package uk.ac.cam.signups.util;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.htmleasy.HtmleasyProviders;

import uk.ac.cam.signups.controllers.*;

public class Signapps extends Application {
	
  private static Logger log = LoggerFactory.getLogger(Signapps.class);
	
  public Set<Class<?>> getClasses() {
    Set<Class<?>> myServices = new HashSet<Class<?>>();
    
    // Add controllers    
    log.debug("Adding controllers to main application");
    myServices.add(EventsController.class);
    myServices.add(DeadlinesController.class);
    myServices.add(GroupsController.class);
    myServices.add(HomePageController.class);
    
    // Add Htmleasy Providers
    log.debug("Adding Htmleasy providers");
    myServices.addAll(HtmleasyProviders.getClasses());

    return myServices;
  }
}
