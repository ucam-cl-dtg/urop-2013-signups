$(document).ready(function() {

	//
	// Essential event listeners
	//

	$(document).on('click', '.expand-sub-panel', function() {
		$(this).closest('.list-panel').siblings('.sub-panel').slideToggle();
	});

	$(document).on('click', '.upload-marked-work', function() {
		$('.upload-marked-work-form').slideToggle();
	});

	$(document).on('click', '.star-question-button', function() {
		alert('This question (set) has been added to your favourites');
	});

	$(document).on('click', '.remove-question-from-set', function() {
		$(this).closest('.panel-wrapper').slideUp();
	});

	$(document).on('click', '.question-to-add-to-set', function() {
		$(this).children('.list-panel').toggleClass('success');
	});

	$(document).on('click', '.delete-member', function() {
		$(this).closest('.member-controls').slideUp();
	});

	//
	// Form module loader
	//

	$(document).on('click', '.form-control-option-button', function() {
		var elementToClone = $(this).attr('data-element-to-clone');
		var target = $(this).attr('data-target');

		var controls = $('.' + elementToClone).clone().removeClass('hide').get(0).outerHTML;
		$('.' + target).append(controls);
	});

});