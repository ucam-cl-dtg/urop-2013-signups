package uk.ac.cam.signups.controllers;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.ViewWith;

@Path("/")
public class HomePageController {

	// Index
	@GET @Path("/") @ViewWith("/soy/home_page.index")
	public Map indexHomePage() {
		return ImmutableMap.of();
	}
	
	// DOS Index
	@GET @Path("/DoS") @ViewWith("/soy/home_page.dos")
	public Map dosHomePage() {
		return ImmutableMap.of();
	}
	
	// Admin Index
	@GET @Path("/admin") @ViewWith("/soy/home_page.admin")
	public Map adminHomePage() {
		return ImmutableMap.of();
	}
}