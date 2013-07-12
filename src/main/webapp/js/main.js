$(document).ready(function() {
	slot_id = 0;
	$(".datepicker").datepicker({ dateFormat: "dd/mm/yy"});
	$("#new_slot").click(function(e) {
		e.preventDefault();
		slot_id += 1;

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
});