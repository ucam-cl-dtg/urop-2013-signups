#Supervisions front-end template

The template is broken down into 4 seperate sections/folders:

* `js` - contains all of the JavaScript
* `modules` - contains all of the HTML templates that get AJAXed into the webapp
* `scss` - contains all stylesheets
* `css` - contains the rendered SASS.

### How to deal with the SASS

You should NOT edit the contents of the `css` folder directly. 

Instead you should edit the contents of the `scss` folder and then run `main.scss` through a compiler SASS (e.g. CodeKit for OS X).

All of the other SASS files are included in this file - there is no need to compile any of the others.

The front-end framework we are using is called Foundation . You can find the documentation [here](http://foundation.zurb.com/docs/)

### How to load modules

All elements with the class `module-loader` have an event listener bound to them, which loads the file with their `data-target` attribute name from the `modules` directory on click.

For example, to load the content of `modules/home/login.html`:

	<div class="module-loader" data-target="home/login">When I am clicked, login.html will be loaded</div>
	
To modify the AJAX, look in `main.js`

# Routing system

## How to set up.

1. Clone the git repository into the webapp folder and call it `app` (name is not important).

2. Create the file `webapp/js/routes.js`. This file will contains all the routes of your application.
An example routes.js can be found inside `app/js/routes.js`

3. Now you need to include the soy templates you will be using inside `app/index.html`. There should be a comment in the html marking
 where you should add the soy templates. The following code will include the templates inside the namespace `main`.

    <script src="/soy/js/1/main.js"></script>

 Some very important tips. In order for a file to be included it needs to have the name `someName.js.soy` not `someName.soy`. Also **DO NOT**
 try to include the namespace `shared`.


## Defining Routes
The most basic example of a route is

    "test" : "main.index"

When `#test` is accessed a GET request will be made to `/test` and the json returned by that page will be given to the template `main.index`;

You can specify wildcards in your routes. For example:

     "/question/:id" : "main.index"

You can also specify parts of the route as optional by using round brackets. For example

    "/question(/:id)" : "main.index"

Theoretically you should also be able to pass a regular expression as the route but I haven't tested that yet.


## Specifing the templates

The easiest thing to do is to just pass the template name as a string.
But you can also pass a function that given the data from the requests it gives back template name.

## Loading templates from javascript

The following function might be usefull if you want to load templates directly from javascript.

`loadModule(elem, location, template)`  - This will load the template inside of the jquery element elem using the data
    from location.
    
* elem - A JQUERY element in the page.
* location - The location where to get the data from
* template - This can be either a string or a function that takes the json as the first param

`applyTemplate(elem, template, data)` - This will load the template inside of the jquery element elem using.
* elem - A JQUERY element in the page
* template - This can be either a string or a function that takes the json as the first param.
* data - The data that is to be passed to the template

`getTemplate(templateName) ` - This converts a template name to the actual template function.

## Tips and tricks

### Be carefull with the last `/`

Consider  `"/question/:id": "question.show"`. This will match `/question/1` but will not match `/question/1/`

### Using the params from the route in the template

Consider the route `/question/:id`. Lets assume you need to use the value of the id in the template. You could do something like this

    "/question/:id": function (json) {
        json.id = getRouteParams()[0]; // getRoutesParams returns a list of the values matched by wild cards.
        return "question.show";
    }

getRouteParams() returns a list of the values matched by the wild cards. For example for `question/:id/comment/:commentId` and the path
`question/3/comment/4` getRouteParams() will return `[3, 4]`.

If you find yourself writing the above functions a lot of time you could try to refactor it into something like

    function idInjector(templateName) {
        return function(json) {
           json.id = getRouteParams()[0];
           return templateName;
        }
    }

And then

    "question/:id": idInjector("question.show")

## Experimental

Currently we have the problem that for each page we do a single request to the server. That means that the controller
must return all the necessary information in one go. This can quickly become ridiculous if we need something like a list of questions,
 the comments for each question, and the users who liked each comment.

I have added an experimental feature to try to fix this. This equivalent to using `call` in the soy template but you can also specify
where to get the data from.

    <div class="async-loader" data-path="/comments/1" template-name="comments.show">
        Some content that will be replaced later.
    </div>

Keep in mind this thing is just something experimental.


## Current problems / bugs

### Unable to use a soy template that requires no data.

A request is always sent to the server and some data is expected back.
