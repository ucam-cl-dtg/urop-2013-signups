$(document).ready(function() {
	// EVENTS CONTROLLER

	toggle_rows(); // Toggle between row types

	// GROUPS CONTROLLER
	// Ajax autocomplete lookup user

	//Lookup user token input
	//Delete a group
    $("body").on("click", ".group_delete", function() {
        var group_id = $(this).parents("form").attr("id");
        // Ajax delete request
            var deleteData = $.ajax({
                  type: 'DELETE',
                  url: "/signapp/groups/" + group_id,
                  success: function(resultData) {  
                  // Reload the page for now.. can replace this with just redsiplaying div later?
                  location.reload();
                  }
            });
    });

	// DEADLINES CONTROLLER

	// EVENTS CONTROLLER
});

// Toggle between row types
function toggle_rows() {
	$(".main").on("click", "span#datetime", function() {
		$("div#manual").slideUp("fast", function() {
			$("div#datetime").slideDown("fast");
		});
	});

	$(".main").on("click", "span#manual", function() {
		$("div#datetime").slideUp("fast", function() {
			$("div#manual").slideDown("fast");
		});
	});
}