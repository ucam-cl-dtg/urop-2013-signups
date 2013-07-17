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