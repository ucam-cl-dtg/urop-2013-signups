// Globals
SOY_GLOBALS = {
	url_prefix : CONTEXT_PATH,
	api_prefix : CONTEXT_PATH + "/api"
};

/*
 * Demo Routes:
 * 
 * The first term must be the route from where the template will get the data.
 * The second term must be either a string representing the template name or a
 * function that returns the template name. The function will receive the json
 * returned by the request as the first parameter.
 * 
 * $(document).ready(function() { router = Router({ "test(/:id)": "main.test",
 * //For getting params in get requests "search?:params" :
 * "main.searchtemplate", "tester": function(json) { return json['isSupervisor'] ?
 * "a" : "b";} // Use the last line to redirect unmatched routes to an error
 * page "*undefined": "errors.notfound" }) })
 */
function supportRedirect(templateName) {
	return function(json) {
		if (json.redirect)
			router.navigate(json.redirect, {
				trigger : true
			});
		return templateName;
	};
}

$(document).ready(function() {
	router = Router({
		"" : "signups.events.index",
		"events" : "signups.events.index",
		"events/:obfuscatedId" : "signups.events.show",
		"events/new" : "signups.events.new",
		"events/dos" : "signups.events.dos"

	// For getting params in get requests
	// Use the last line to redirect unmatched routes to an error page
	// "*undefined": "errors.notfound"
	});
});
