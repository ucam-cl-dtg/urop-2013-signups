$(document).ready(function() {


	$(window).resize(function() {
		resizeSidebar();
	});

	$(document).resize(function() {
		resizeSidebar();
	});

	$('.main').resize(function() {
		resizeSidebar();
	});

	//
	// Sidebar dropdowns
	//

	$('.sidebar-navigation-item-header').on('click', function() {
		$(this).next('.sidebar-sub-navigation').slideToggle();
	});

    // Support for .module-loader

    $(document).on("click", ".module-loader", function() {
        var location = $(this).attr('data-target');
        router.navigate(location, {trigger: true});
    });
	//
	// Mobile navigation bar
	//

	$('.mobile-nav-options-button').on('click', function() {
		$('.mobile-nav-wrapper').slideToggle();
	});

	$('.mobile-nav-wrapper .module-loader').on('click', function() {
		$('.mobile-nav-wrapper').slideUp();
	});

});