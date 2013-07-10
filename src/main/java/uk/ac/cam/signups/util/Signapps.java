package uk.ac.cam.signups.util;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import com.googlecode.htmleasy.HtmleasyProviders;

import uk.ac.cam.signups.controllers.*;

public class Signapps extends Application {
  public Set<Class<?>> getClasses() {
// 	SessionFactoryManager.getInstance();
    Set<Class<?>> myServices = new HashSet<Class<?>>();

    // Add controllers

    // Add Htmleasy Providers
    myServices.addAll(HtmleasyProviders.getClasses());

    return myServices;
  }
}
