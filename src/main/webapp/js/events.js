moduleScripts['signapp']['events'] = {
	'new' : 
		[
			function() {
			  $(".datepicker").datepicker({dateFormat: "dd/mm/yy"});
			
			  $(".form-control-option-button").click(function() {
					var elementToClone = $(this).attr('data-element-to-clone');
				  var target = $(this).attr('data-target');
			
			    var controls = $('.' + elementToClone).clone().get(0).outerHTML;
			    var elem = $(controls).css("display","none");
			    $('.' + target).find(".single-slot-controls").last().after(elem);
			    $('.' + target).find(".single-slot-controls").last().slideDown("fast");
			    elem.find(".datepicker").removeClass("hasDatepicker").removeAttr("id").datepicker({dateFormat: "dd/mm/yy"});
			    $(".delete-time-slot").removeClass("disabled");
			  });
			  
			  $(".add-range").click(function() {
			  	if ($(".range-controls").css("display") == "none") {
			  		$(".range-controls").slideDown("fast");
			  		$(this).text("Remove Range");
			  	} else {
			  		$(".range-controls").slideUp("fast");
			  		$(this).text("Add Range");
			  	}
			  });
			  
			  $(".generate-slots").click(function() {
			  	var parentElem = $(this).parent().parent();
			  	var len = parseInt(parentElem.find("#number_of_slots").val());
			  	var date = parseInt(parentElem.find("#date").val());
			  	var startHour = parseInt(parentElem.find("#hour").val());
			  	var startMinute = parseInt(parentElem.find("#minute").val());
			  	var duration = parseInt(parentElem.find("#duration").val());
			  	var breakDuration = parseInt(parentElem.find("#break").val());
			  	
			  	var singleSlot;
			  	var hour;
			  	var minute;
			  	for(var i = 0; i < len; i++) {
			  		singleSlot = $(".single-slot-controls").clone()
			  		singleSlot.find("input[name='available_dates[]']").val(date);
			  		minute = startMinute + ((duration + breakDuration) * i);
			  		hour = startHour;
			  		while (minute >= 60) {
			  			minute -= 60;
			  			hour++;
			  		}
			  		singleSlot.find("select[name='available_minutes[]']").parent().find("current").val(minute);
			  		singleSlot.find("select[name='available_hours[]']").parent().find("current").val(hour);
			  		singleSlot.find(".button").removeClass("disabled");
			  		$(".time-controls-wrapper").find(".single-slot-controls").last().after(singleSlot.get(0).outerHTML);
			  	}
			  });
			
			  $(".event-type-input").tokenInput("/signapp/events/queryTypes", {
			    theme: "facebook",
			    method: "post",
			    tokenValue: "name",
			    propertyToSearch: "name",
			    min_chars: 2,
			    hintText: "Add a new type",
			    preventDuplicates: true,
			    resultsLimit: 10,
			    resultsFormatter: function(item){ return "<li><div style='display: inline-block; padding-left: 10px;'>"+ item.name + "</div></li>" }
			  });
			  
			  $(this).on("click", ".delete-time-slot", function() {
			  	if ($(".delete-time-slot").length != 1) {
			  		$(this).parent().parent().parent().slideUp("fast", function() {
			  			$(this).remove()
			  			
				  		if($(".delete-time-slot").length == 1) {
				  			$(".delete-time-slot").addClass("disabled");
				  		}
			  		});
			  	}
			  });
			},
			
			function() {
			  $("span#datetime").click(function() {
			    $("div#manual").slideUp("fast", function() {
			      $("div#datetime").slideDown("fast");
			    });
			  });
			
			  $("span#manual").click(function() {
			    $("div#datetime").slideUp("fast", function() {
			      $("div#manual").slideDown("fast");
			    });
			  });
			}
		],
	'show' 	:	
		[
		  function() {
			  	
			}
		]
}