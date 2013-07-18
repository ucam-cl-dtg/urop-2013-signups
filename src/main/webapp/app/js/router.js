var BASE_PATH = "/";

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
        loadModule($('.main'), window.location.hash, value);
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
    return window.location.protocol + "//" + window.location.host + BASE_PATH + location;
}

function getRouteParams() {
    var fragment = Backbone.history.fragment,
         routes = _.map(Backbone.history.handlers, function(x) { return x.route });
    var matched = _.find(routes, function(handler) {
        return handler.test(fragment);
    });


    return router._extractParameters(matched, Backbone.history.fragment);
}

function getTemplate(name) {
    var names = name.split('.');
    var res = window;
    for (var i=0; i<names.length; i++) {
        res = res[names[i]];
    }
    return res;
}

function asyncLoad(elems) {
    elems.each(function(i) {
       var elem = $(elems[i]),
           data_path = getLocation(elem.attr("data-path")),
           template_name = elem.attr("template-name");
       $.get(data_path, function(json) {
            applyTemplate(elem, template_name, json);
       }).fail(function(err) {
            console.log(err);
            applyTemplate(elem, template_name, {});
       })
    });
}

function applyTemplate(elem, template, data) {
    var templateFunc;
    if (typeof template == "string") {
        templateFunc = getTemplate(template);
    } else if(typeof template == "function") {
        templateFunc = getTemplate(template(data));
    }

    elem.html(templateFunc(data));
    asyncLoad(elem.find(".async-loader"));
}

//
// template can either be a string with the name of the template
// or a function that returns the name of the template.

function loadModule(elem, location, template) {
   var location = getLocation(location);

   $.get(location, function(data) {
       applyTemplate(elem, template, data);
       postModuleLoad();
   }).fail(function() {
       elem.html('<h3>Error: could not load ' + location + '</h3>');
       postModuleLoad();
   });
}
