moduleScripts['signapp']['events'] = {
	'new' : 
		[
			function() {
			  $(".datepicker").datepicker({dateFormat: "dd/mm/yy"});
			
			  $(".form-control-option-button").click(function() {
					var elementToClone = $(this).attr('data-element-to-clone');
				  var target = $(this).attr('data-target');
			
			    var controls = $('.' + elementToClone).clone().get(0).outerHTML;
			    console.log(controls);
			    var elem = $(controls).css("display","none");
			    $('.' + target).find(".single-slot-controls").last().after(elem);
			    $('.' + target).find(".single-slot-controls").last().slideDown("fast");
			    elem.find(".datepicker").removeClass("hasDatepicker").removeAttr("id").datepicker({dateFormat: "dd/mm/yy"});
			    $(".delete-time-slot").removeClass("disabled");
			  });
			  
			  $(".add-range").click(function() {
			  	$(".range-controls").slideDown("fast");
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