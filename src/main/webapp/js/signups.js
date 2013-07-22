moduleScripts['signapp'] = {
  'events' : {
    'new' : [
      function() {
        $(".datepicker").datepicker({dateFormat: "dd/mm/yy"});

        $(document).on('click', '.form-control-option-button', function() {
	   		  var elementToClone = $(this).attr('data-element-to-clone');
		  	  var target = $(this).attr('data-target');

          var controls = $('.' + elementToClone).clone().get(0).outerHTML;
          var elem = $(controls);
          $('.' + target).append(elem);
          elem.find(".datepicker").removeClass("hasDatepicker").removeAttr("id").datepicker({dateFormat: "dd/mm/yy"});
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
          resultsFormatter: function(item){ return "<li>" + "<div style='display: inline-block; padding-left: 10px;'><div class='full_name'>" + item.name + "</div></div></li>" }
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

    'show' : [
      function() {

      }
    ]
	},

  'groups' : {
    'index' : [
      function() {
        $(".group_delete").click(function() {
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
      }
    ],

    'edit' : [
      function() {

      }
    ]
  }
}