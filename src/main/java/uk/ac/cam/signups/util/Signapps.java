package uk.ac.cam.signups.util;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.googlecode.htmleasy.HtmleasyProviders;

import uk.ac.cam.signups.controllers.*;

public class Signapps extends Application {
  public Set<Class<?>> getClasses() {
    Set<Class<?>> myServices = new HashSet<Class<?>>();

    // Add controllers
    myServices.add(EventsController.class);
    myServices.add(DeadlinesController.class);
    myServices.add(GroupsController.class);
    myServices.add(HomePageController.class);
    myServices.add(RavenTestController.class);    
    
    // Add Htmleasy Providers
    myServices.addAll(HtmleasyProviders.getClasses());

    return myServices;
  }
}
