package uk.ac.cam.signups.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cl.dtg.teaching.api.DashboardApi.DashboardApiWrapper;
import uk.ac.cam.cl.dtg.teaching.api.NotificationApi.NotificationApiWrapper;
import uk.ac.cam.signups.exceptions.AuthorizationException;
import uk.ac.cam.signups.models.User;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

public class ApplicationController {
	// Logger
	private static Logger log = LoggerFactory
			.getLogger(ApplicationController.class);

	// Raven session
	@Context
	HttpServletRequest sRequest;

	protected User initialiseUser() {

		// This will extract the CRSID of the current user and return it:
		log.debug("Getting crsid from raven");
		String crsid = (String) sRequest.getSession().getAttribute(
				"RavenRemoteUser");

		// Register or return the user
		return User.registerUser(crsid);
	}

	// temporary for testing
	protected User initialiseSpecifiedUser(String crsid) {
		// Register or return the user
		return User.registerUser(crsid);
	}

	protected void authenticate(User u) throws AuthorizationException {
		User cu = initialiseUser();
		if (!cu.equals(u))
			throw new AuthorizationException(cu);
	}

	protected String getDashboardUrl() {
		return sRequest.getSession().getServletContext()
				.getInitParameter("dashboardUrl");
	}

	protected String getApiKey() {
		return sRequest.getSession().getServletContext()
				.getInitParameter("apiKey");
	}

	protected NotificationApiWrapper getNotificationApiWrapper() {
		return new NotificationApiWrapper(getDashboardUrl(), getApiKey());
	}

	protected DashboardApiWrapper getDashboardApiWrapper() {
		return new DashboardApiWrapper(getDashboardUrl(), getApiKey());
	}
}