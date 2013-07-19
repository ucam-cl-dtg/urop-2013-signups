$(document).ready(function() {
	// EVENTS CONTROLLER

	type_adder(); // Add new type
	date_adder(); // Add new date field
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

// Add new date field
function date_adder() {
	$(".main").on("click","#new_slot", function(e) {
		e.preventDefault();

		// Create date time picker
		datetimepicker = "<div>";
		datetimepicker += "<input type='text' class='datepicker' name='available_dates[]'/>"; // Date
		// Sensible hours
		datetimepicker += "<select name='available_hours[]'>";
		for ( var i = 6; i < 24; i++) {
			datetimepicker += "<option>" + i + "</option>";
		}
		datetimepicker += "</select>:";
		// Minutes
		datetimepicker += "<select name='available_minutes[]'>";
		for ( var i = 0; i < 60; i++) {
			datetimepicker += "<option>" + i + "</option>";
		}
		datetimepicker += "</select></div>";

		$(datetimepicker).insertBefore($(this).parent());
		$(".datepicker").datepicker({dateFormat : "dd/mm/yy"});
	});
}

// Add new type
function type_adder() {
	$(".main").on(
	    "click",
	    "#new_type",
	    function(e) {
		    e.preventDefault();

		    $("<div><input type='text' name='types[]'></div>").insertBefore(
		        $(this).parent());
	    });
}

// Toggle between row types
function toggle_rows() {
	$(".main").on("click", "input[type='radio']#datetime", function() {
		$(".manual_rows").slideUp("fast", function() {
			$(".datetime_rows").slideDown("fast");
		});
	});

	$(".main").on("click", "input[type='radio']#manual", function() {
		$(".datetime_rows").slideUp("fast", function() {
			$(".manual_rows").slideDown("fast");
		});
	});
}