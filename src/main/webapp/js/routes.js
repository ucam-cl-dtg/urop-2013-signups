/* Demo Routes:

The first term must be the route from where the template will get the data.
The second term must be either a string representing the template name or
a function that returns the template name. The function will receive the json returned
by the request as the first parameter.

$(document).ready(function() {
    router = Router({
        "test(/:id)": "main.test",
        //For getting params in get requests
        "search?:params" : "main.searchtemplate",
        "tester": function(json) { return json['isSupervisor'] ? "a" : "b";}
        // Use the last line to redirect unmatched routes to an error page
        "*undefined": "errors.notfound"
    })
})
*/
function supportRedirect(templateName) {
    return function(json) {
        if (json.redirect) 
            router.navigate(json.redirect, {trigger: true});
        return templateName;
    }
}

$(document).ready(function() {
    router = Router({
        "signapp/groups": "signapp.groups.index",
        "signapp/groups/error/:type": "signapp.groups.index",
        "signapp/deadlines" : "signapp.deadlines.index",
        "signapp/deadlines/:id/edit" : supportRedirect("signapp.deadlines.edit"),
        "signapp/deadlines/error/:type": "signapp.deadlines.index",
        "signapp/": "signapp.home_page.index",
        "signapp/groups/:id/edit" : "signapp.groups.edit",
        "signapp/events/:id" : "signapp.events.show",
        "signapp/events/new" : "signapp.events.new",

        //For getting params in get requests
        // Use the last line to redirect unmatched routes to an error page
        //"*undefined": "errors.notfound"
    });
});

