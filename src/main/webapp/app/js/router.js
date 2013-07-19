function Router (routes) {
    var router = new Backbone.Router;

    var route, value;
    for (route in routes) {
        value = routes[route];
        config(router, route, value);
    }

    initializeHistory(router);
    return router;
}

function initializeHistory(router) {
    var ok = Backbone.history.start();
    // Current page wasn't matched by the router
    if (! ok ) {
        $('.main').html("<h3> Error: could not match route: " + window.location.hash + "</h3>");
    }
}

// Create a proper Backbone route from a given route
function config (router, route, value) {
    if ((typeof value != "string") && (typeof value != "function"))
        throw new Error("Unsupported type for route: " + route);

    return router.route(route, route, function(){
        loadModule(window.location.hash, value);
    });
}


//
// Sidebar resize. Should create separate file for resizeSidebar and postModuleLoad
//

function resizeSidebar() {
    var sidebarHeight = Math.max($('.main').outerHeight(), $(window).height() - $('.sidebar').offset().top);
    $('.sidebar').height(sidebarHeight);
}

function postModuleLoad () {
  resizeSidebar();
  $(document).foundation();
}

function getLocation(location) {
    if (location[0] == "#")
        location = location.slice(1);
    return window.location.protocol + "//" + window.location.host + "/" + location;
}

function getTemplate(name) {
    var names = name.split('.');
    var res = window;
    for (var i=0; i<names.length; i++) {
        res = res[names[i]];
    }
    return res;
}

function applyTemplate(template, data) {
    var templateFunc;
    if (typeof template == "string") {
        templateFunc = getTemplate(template);
    } else if(typeof template == "function") {
        templateFunc = getTemplate(template(data));
    }

    $(".main").html(templateFunc(data));
}

//
// template can either be a string with the name of the template
// or a function that returns the name of the template.

function loadModule(location, template) {
   var location = getLocation(location);
   $('.main').html('<h3>Loading...</h3>');
   $.get(location, function(data) {
       applyTemplate(template, data);
       postModuleLoad();
   }).fail(function() {
       $('.main').html('<h3>Error: could not load ' + location + '</h3>');
       postModuleLoad();
   });
}
