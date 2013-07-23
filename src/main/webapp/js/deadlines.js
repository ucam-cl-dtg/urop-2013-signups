moduleScripts['signapp']['deadlines'] = {
	'index' : 
		[
	    function() {
	          // Create deadline
              $(".datepicker").datepicker({dateFormat: "dd/mm/yy"});
              
    	      $(".deadline_user_token_input").tokenInput("/signapp/groups/queryCRSID", {
    	        method: "post",
    	        tokenValue: "crsid",
    	        propertyToSearch: "crsid",
    	        theme: "facebook",
    	        minChars: 3,
    	        hintText: "Search for a user",
    	        resultsLimit: 10,
    	        preventDuplicates: true,
    	               
    	        resultsFormatter: function(item){ return "<li>" + "<div style='display: inline-block; padding-left: 10px;'><div class='full_name'>" + item.name + " (" + item.crsid + ")</div><div class='email'>" + item.crsid + "@cam.ac.uk</div></div></li>" },
    	        tokenFormatter: function(item) { return "<li><p>" + item.name + " (" + item.crsid + ")</p></li>" },                           
    	      });
    	             
    	      $(".deadline_group_token_input").tokenInput("/signapp/deadlines/queryGroup", {
    	        method: "post",
    	        tokenValue: "group_id",
    	        propertyToSearch: "group_name",
    	        theme: "facebook",
    	        hintText: "Search your groups",
    	        resultsLimit: 10,
    	        preventDuplicates: true           
    	      });
    	      
              // Delete deadline
              $(".deadline_delete").click(function() {
              var deadline_id = $(this).parents("form").attr("id");
              // Ajax delete request
                  var deleteData = $.ajax({
                        type: 'DELETE',
                        url: "/signapp/deadlines/" + deadline_id,
                        success: function(resultData) {
                            $("#"+deadline_id).hide(2000, function() {
                                $(this).remove();
                            });
                            $("#d_"+deadline_id).hide(2000, function() {
                                $(this).remove();
                            });
                        }
                  });
              });
        
	    }        
	  ],
    'edit' : 
      [
        function() {
              $(".datepicker").datepicker({dateFormat: "dd/mm/yy"});
        }        
      ]
}