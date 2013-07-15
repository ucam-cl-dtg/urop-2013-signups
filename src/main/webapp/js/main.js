$(document).ready(function() {
	// EVENTS CONTROLLER
	
	type_adder(); 	// Add new type 
	date_adder(); 	// Add new date field
	toggle_rows();	// Toggle between row types
	
	// GROUPS CONTROLLER
	var testData = {crsid:"hp343",name:"holly"};
	request = $.ajax({
		url: "/groups/queryCRSID",
		type: "post",
		dataType: 'json',
		success: function() { alert("test response"); }, data: testData});
	
	
	// DEADLINES CONTROLLER

	// EVENTS CONTROLLER
});

// Add new date field 
function date_adder() {
	$(".datepicker").datepicker({ dateFormat: "dd/mm/yy"});
	$("#new_slot").click(function(e) {
		e.preventDefault();

		// Create date time picker
		datetimepicker = "<div>";
		datetimepicker += "<input class='datepicker' name='available_dates[]'/>"; // Date
		// Sensible hours 
		datetimepicker += "<select name='available_hours[]'>";
		for(var i = 6; i < 24; i++) {
			datetimepicker += "<option>" + i + "</option>";
		}
		datetimepicker += "</select>:";
		// Minutes
		datetimepicker += "<select name='available_minutes[]'>";
		for(var i = 0; i < 60; i++) {
			datetimepicker += "<option>" + i + "</option>";
		}
		datetimepicker += "</select></div>";
		
		$(datetimepicker).insertBefore($(this).parent());
		$(".datepicker").datepicker({ dateFormat: "dd/mm/yy"});
	});
}

// Add new type
function type_adder() {
	$("#new_type").click(function(e) {
		e.preventDefault();
		
		$("<div><input type='text' name='types[]'></div>").insertBefore($(this).parent());
	});
}

// Toggle between row types
function toggle_rows() {
	$("input[type='radio']#datetime").click(function() {
		$(".manual_rows").slideUp("fast", function() {
			$(".datetime_rows").slideDown("fast");
		});
	});

	$("input[type='radio']#manual").click(function() {
		$(".datetime_rows").slideUp("fast", function() {
			$(".manual_rows").slideDown("fast");
		});
	});
}