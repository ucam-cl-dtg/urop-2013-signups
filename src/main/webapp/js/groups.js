moduleScripts['signapp']['groups'] = {
	'index' : 
		[
			function() {
			  $(".group_delete").click(function() {
			  var group_id = $(this).parents("form").attr("id");
			  // Ajax delete request
			      var deleteData = $.ajax({
			            type: 'DELETE',
			            url: "/signapp/groups/" + group_id,
                        success: function(resultData) {
                            $("#"+group_id).hide(2000, function() {
                                $(this).remove();
                            });
                        }
			      });
			  });
			  
			  $(".member_token_input").tokenInput("/signapp/groups/queryCRSID", {
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
			  
			  $(".exgroup_token_input").tokenInput("/signapp/groups/queryGroup", {
			    method: "post",
			    tokenValue: "id",
			    propertyToSearch: "name",
			    theme: "facebook",
			    tokenLimit : 1,
			    
			    resultsFormatter: function(item){ return "<li>" + "<div style='display: inline-block; padding-left: 10px;'><div class='full_name'>" + item.name + "</div><div class='email'>" + item.description + "</div></div></li>" },
			    tokenFormatter: function(item) { return "<li><p>" + item.name + "</p></li>" },
			  });
			}
		]
}