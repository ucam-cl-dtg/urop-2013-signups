moduleScripts['signups']['events'] = {
	'new' : [
			function() {
				// Prevent creating event twice on double click
				var submitted = false;

				$("#map-toggle").click(function(e) {
					e.preventDefault();
					if ($(this).data("show") == "true") {
						$(this).data("show", "false");
						$("#iframe-wrapper").addClass("overflow-hidden");
						$(this).text("Show on map");
					} else {
						$(this).data("show", "true");
						$("#iframe-wrapper").removeClass("overflow-hidden");
						$(this).text("Hide map");

						// Clever hack to prevent map going blank when location
						// is not
						// annotated.
						$("iframe").css("width", "100%");
					}
				});

				$("input[type='submit']").click(function(e) {
					if (!submitted) {
						submitted = true;
					} else {
						e.preventDefault();
					}
				});

				$("form").ajaxForm({
					success : function(data) {
						applyTemplate($('.main'), "signups.events.new", data);
					},
					error : function(data) {
						displayError(data, $('.main'));
					}
				});

				$(".datepicker").datepicker({
					dateFormat : "dd/mm/yy",
					showOptions : {
						direction : "up"
					}
				});
				$("input[name='location']")
						.autocomplete(
								{
									minLength : 2,
									source : function(request, response) {
										$
												.getJSON(
														"http://map.cam.ac.uk/v6.json",
														{
															partial : "partial",
															"q" : request.term,
															limit : "10"
														})
												.done(
														function(data) {
															response($
																	.map(
																			data,
																			function(
																					item) {
																				return {
																					label : (item.prefix ? item.prefix
																							+ " "
																							: "")
																							+ item.name,
																					value : (item.prefix ? item.prefix
																							+ " "
																							: "")
																							+ item.name
																				};
																			}));
														});
									},
									select : function(event, resp) {
										$("input[name='location']").val(
												resp.item.label).text(
												resp.item.label);
										$("iframe").attr(
												"src",
												"http://map.cam.ac.uk/"
														+ resp.item.label);
									}
								});

				if (loc = $("input[name='location']").val()) {
					$("input[name='room']").removeAttr("disabled");
					$("iframe").attr("src", $("iframe").attr("src") + loc);
				}

				$("input[name='location']").keyup(
						function() {
							if ($(this).val() != "") {
								$("input[name='room']").removeAttr("disabled");
							} else {
								$("input[name='room']").attr("disabled",
										"disabled").val("");
							}
						});

				$("input[name='room']").autocomplete({
					minLength : 2,
					source : function(request, response) {
						$.getJSON(prepareURL("events/queryRooms"), {
							"qroom" : request.term,
							"qbuilding" : $("input[name='location']").val()
						}).done(function(data) {
							response($.map(data, function(item) {
								return {
									value : item.room,
									label : item.room
								};
							}));
						});
					}
				});

				$(".form-control-option-button")
						.click(
								function() {
									var elementToClone = $(this).attr(
											'data-element-to-clone');
									var target = $(this).attr('data-target');

									var controls = $("#datetime").find(
											'.' + elementToClone).clone()
											.get(0).outerHTML;
									var elem = $(controls).css("display",
											"none");
									$('.' + target).find(
											".single-slot-controls").last()
											.after(elem);
									$('.' + target).find(
											".single-slot-controls").last()
											.slideDown("fast");
									elem.find(".datepicker").removeClass(
											"hasDatepicker").removeAttr("id")
											.datepicker({
												dateFormat : "dd/mm/yy"
											});
									$(".delete-time-slot").removeClass(
											"disabled");
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

				$(".generate-slots")
						.click(
								function() {
									var parentElem = $(".range-controls");
									var len = parseInt(parentElem.find(
											"#number_of_slots").val());
									var date = parentElem.find("#date").val();
									var startHour = parseInt(parentElem.find(
											"#hour").val());
									var startMinute = parseInt(parentElem.find(
											"#minute").val());
									// set default duration to 15
									var duration = parseInt(parentElem.find(
											"#duration").val()) || 15;
									// set default break to 0
									var breakDuration = parseInt(parentElem
											.find("#break").val()) || 0;

									var singleSlot;
									var hour;
									var minute;
									var minuteSet;
									var hourSet;

									var templateElement = $("#datetime").find(
											".single-slot-controls").first();

									for (var i = 0; i < len; i++) {
										singleSlot = templateElement.clone();

										// Calculate times
										minute = startMinute
												+ ((duration + breakDuration) * i);
										hour = startHour
												+ Math.floor(minute / 60);
										minute %= 60;

										// Set necessary attributes
										if (hour < 24) {
											minuteSet = (minute < 10 ? "0" : "")
													+ minute;
											hourSet = (hour < 10 ? "0" : "")
													+ hour;
											singleSlot
													.find(
															"select[name='available_minutes[]']")
													.val(minuteSet).parent()
													.find(".current").val(
															minuteSet).text(
															minuteSet);
											singleSlot
													.find(
															"select[name='available_hours[]']")
													.val(hourSet).parent()
													.find(".current").val(
															hourSet).text(
															hourSet);
											$(".time-controls-wrapper").find(
													".single-slot-controls")
													.last().after(singleSlot);
											singleSlot.find(".datepicker")
													.removeClass(
															"hasDatepicker")
													.removeAttr("id")
													.datepicker({
														dateFormat : "dd/mm/yy"
													}).val(date).text(date);
										}
										$(".button").removeClass("disabled");
									}
								});

				var wasFilled = false;
				$(".event-type-input")
						.tokenInput(
								prepareURL("events/queryTypes"),
								{
									theme : "facebook",
									method : "post",
									tokenValue : "name",
									propertyToSearch : "name",
									min_chars : 2,
									hintText : "Add a new type",
									preventDuplicates : true,
									resultsLimit : 10,
									allowFreeTagging : true,
									resultsFormatter : function(item) {
										return "<li><div style='display: inline-block; padding-left: 10px;'>"
												+ item.name + "</div></li>";
									},
									onAdd : function(item) {
										wasFilled = true;
									}
								});

				if (data = (target = $("input[name='types']"))
						.data("prepopulate")) {
					$.each(data.split(","), function(i, v) {
						target.tokenInput("add", {
							name : v
						});
					});
				}

				$("input[name='types']").parent().find("ul input").attr(
						"placeholder", "Type");

				if ($("input[name='types']").parent().hasClass("error")) {
					$("ul.token-input-list-facebook").addClass("error");
				}

				$(this)
						.on(
								"click",
								".delete-time-slot",
								function() {
									if ($(".delete-time-slot").length != 1) {
										$(this)
												.parents(
														".single-slot-controls")
												.slideUp(
														"fast",
														function() {
															$(this).remove();
															if ($(".delete-time-slot").length == 1) {
																$(
																		".delete-time-slot")
																		.addClass(
																				"disabled");
															}
														});
									}
								});

				// Fix jqueryInput bug when tab is pressed on empty field
				// It should proceed to the next field.
				$("input#token-input-").keydown(function(e) {
					if (e.which == 9 && !wasFilled) {
						$("input[name='location']").get(0).select();
						$("input[name='location']").get(0).focus();
						wasFilled = false;
					} else if (e.which == 13 || e.which == 9) {
						wasFilled = false;
					}
				});
			},

			function() {
				$("label#datetime-type").click(function() {
					$("div#manual").slideUp("fast", function() {
						$("div#datetime").slideDown("fast");
					});
				});

				$("label#manual-type").click(function() {
					$("div#datetime").slideUp("fast", function() {
						$("div#manual").slideDown("fast");
					});
				});
			} ],
	'show' : [ function() {
		// Delete event
		$("#delete-event").click(
				function() {
					if (confirm("Are you sure you want to delete?")) {
						var obfuscatedId = /events\/(\w+)/
								.exec(window.location.pathname)[1];
						$.ajax({
							url : prepareURL("events/" + obfuscatedId),
							type : "DELETE"
						}).done(function(data) {
							if (data.error != undefined) {
								errorNotification(data.error);
							}
						});
					}
				});

		$("form").ajaxForm(function(data) {
			applyTemplate($('.main'), "signups.events.show", data);
		});
		
		// Auto submit the form whenever someone changes the type of a booking
		$("form select").change(function() { $("form").ajaxSubmit(); });

		$("#map-toggle").click(function(e) {
			e.preventDefault();
			if ($(this).data("show") == "true") {
				$(this).data("show", "false");
				$("#iframe-wrapper").addClass("overflow-hidden");
				$(this).text("Show on map");
			} else {
				$(this).data("show", "true");
				$("#iframe-wrapper").removeClass("overflow-hidden");
				$(this).text("Hide map");

				// Clever hack to prevent map going blank when location is not
				// annotated.
				$("iframe").css("width", "100%");
			}
		});

		var info_element = $("#show");
		var current_crsid = info_element.attr("currentcrsid");
		var current_name = info_element.attr("currentname");
		var current_college = info_element.attr("currentcollege");

		$(".slot-nonfreeformfield")
				.each(
						function(i) {
							$(this)
									.tokenInput(
											[ {
												crsid : current_crsid,
												name : current_name,
												collegename : current_college,
											} ],
											{
												theme : "facebook",
												tokenValue : "crsid",
												propertyToSearch : "crsid",
												minChars : 0,
												searchDelay : 0,
												hintText : "Type a CRSID",
												preventDuplicates : true,
												resultsLimit : 10,
												searchingText : '',
												animateDropdown : false,
												tokenLimit : 1,
												resultsFormatter : function(
														item) {
													return getTemplateResult(
															"signups.events.slotDropDownItem",
															{
																"name" : item.name,
																"crsid" : item.crsid
															});
												},
												tokenFormatter : function(item) {
													return getTemplateResult(
															"signups.events.slotValue",
															{
																"name" : item.name,
																"crsid" : item.crsid,
																"collegename" : item.collegename,
															});
												},
												onAdd : function(item) {
													if (!$(this).data(
															"existing")) {
														$(this).prev().css(
																"background",
																"#f5ffbe");

														setTimeout(
																function() {
																	$("form")
																			.ajaxSubmit();
																}, 5);
													}
												},
												onDelete : function(item) {
													setTimeout(function() {
														$("form").ajaxSubmit();
													}, 5);
												}
											});

							// This is needed to hide any dropdowns which have
							// been left laying around by asynchronous form
							// reloads
							$(".token-input-dropdown-facebook").hide();

							if ($(this).data("crsid") != "") {
								$(this).tokenInput("add", {
									crsid : $(this).data("crsid"),
									name : $(this).data("name"),
									collegename : $(this).data("collegename")
								});
							}
						});

		$(".slot-field")
				.each(
						function(i) {
							$(this)
									.tokenInput(
											prepareURL("events/queryCRSID"),
											{
												theme : "facebook",
												method : "post",
												tokenValue : "crsid",
												propertyToSearch : "crsid",
												minChars : 2,
												searchDelay : 300,
												hintText : "Type a CRSID",
												preventDuplicates : true,
												resultsLimit : 10,
												tokenLimit : 1,
												resultsFormatter : function(
														item) {
													return getTemplateResult(
															"signups.events.slotDropDownItem",
															{
																"name" : item.name,
																"crsid" : item.crsid
															});
												},
												tokenFormatter : function(item) {
													return getTemplateResult(
															"signups.events.slotValue",
															{
																"name" : item.name,
																"crsid" : item.crsid,
																"collegename" : item.collegename,
															});
												},
												onAdd : function(item) {
													if (!$(this).data(
															"existing")) {
														$(this).prev().css(
																"background",
																"#f5ffbe");
														setTimeout(
																function() {
																	$("form")
																			.ajaxSubmit();
																}, 5);
													}
												},
												onDelete : function(item) {
													setTimeout(function() {
														$("form").ajaxSubmit();
													}, 5);
												}
											});

							// This is needed to hide any dropdowns which have
							// been left laying around by asynchronous form
							// reloads
							$(".token-input-dropdown-facebook").hide();

							if ($(this).data("crsid") != "") {
								$(this).tokenInput("add", {
									crsid : $(this).data("crsid"),
									name : $(this).data("name"),
									collegename : $(this).data("collegename")
								});
							}
						});

		$(".token-input-token-facebook").each(function(i) {
			$(this).parent().draggable({
				revert : true,
				revertDuration : 200,
				zIndex : 99999,
				stop : function() {
					if ($(this).next().val() == "") {
						$(this).next().tokenInput("clear");
						$(this).removeClass("draggable-custom");
						$(this).draggable("destroy");
					}
					$(this).effect("highlight", 700);
				}
			}).addClass("draggable-custom");
		});

		$("ul.token-input-list-facebook")
				.droppable(
						{
							drop : function(event, ui) {
								if ($(this).find(".token-input-token-facebook").length != 0) {
									var nameCrsidDraggable = $(ui.draggable)
											.find("p").html().split(/<br>/);
									var nameCrsidDroppable = $(this).find("p")
											.html().split(/<br>/);

									$(ui.draggable).next().tokenInput("clear")
											.tokenInput("add", {
												name : nameCrsidDroppable[0],
												crsid : nameCrsidDroppable[1]
											});
									$(ui.draggable).next().val(
											nameCrsidDroppable[1]);
									$(this).next().val(nameCrsidDraggable[1]);
									$(this).next().tokenInput("clear")
											.tokenInput("add", {
												name : nameCrsidDraggable[0],
												crsid : nameCrsidDraggable[1]
											});
								} else {
									var nameCrsid = $(ui.draggable).find("p")
											.html().split(/<br>/);
									$(this).next().tokenInput("add", {
										name : nameCrsid[0],
										crsid : nameCrsid[1]
									});
									$(this).next().val(
											$(ui.draggable).next().val());
									$(ui.draggable).next().val("");
									$(this)
											.draggable(
													{
														revert : true,
														revertDuration : 200,
														zIndex : 99999,
														stop : function() {
															if ($(this).next()
																	.val() == "") {
																$(this)
																		.next()
																		.tokenInput(
																				"clear");
																$(this)
																		.removeClass(
																				"draggable-custom");
																$(this)
																		.draggable(
																				"destroy");
															}
														}
													}).addClass(
													"draggable-custom");
								}

								$(this).effect("highlight", 700);
							}
						});

		$("#load-history").click(
				function() {
					var page = parseInt($(".history-item").length / 10);
					var obfuscatedId = /events\/(\w+)/
							.exec(window.location.pathname)[1];
					$.getJSON(prepareURL("events/queryEventHistory"), {
						page : page,
						id : obfuscatedId
					}).done(
							function(data) {
								$.each(data["list"], function() {
									applyTemplate($("#load-history"),
											"signups.events.historyItem", {
												notification : this
											}, "before");
								});

								if (data["echausted"]) {
									$("#load-history").remove();
								}
							});
				});
	} ],

	'index' : [ function() {
		$("#load-archived, #load-created, #load-signed-up, #load-no-time")
				.click(
						function(e) {
							e.preventDefault();
							var idName = $(this).attr("id");

							var mode;
							if (idName == "load-created") {
								mode = "created";
							} else if (idName == "load-archived") {
								mode = "archive";
							} else if (idName == "load-signed-up") {
								mode = "contemporary";
							} else if (idName == "load-no-time") {
								mode = "no-time";
							} else {
								mode = "created";
							}

							var page = parseInt($(this).parent().parent()
									.parent().find(".button.medium.radius").length / 10);
							var target = $(this).parent().parent();
							var cloneOuter = target.prev().clone().get(0).outerHTML;
							var clone;

							$
									.getJSON(prepareURL("events/queryEvents"),
											{
												mode : mode,
												page : page
											})
									.done(
											function(data) {

												$
														.each(
																data["data"],
																function(index,
																		obj) {
																	clone = $(cloneOuter);
																	if (mode == "created") {
																		clone
																				.find(
																						"a")
																				.attr(
																						"href",
																						"events/"
																								+ obj["id"])
																				.text(
																						obj["title"]);
																	} else {
																		clone
																				.find(
																						"a")
																				.attr(
																						"href",
																						"events/"
																								+ obj["eventSummary"]["id"])
																				.find(
																						".title")
																				.text(
																						obj["eventSummary"]["title"]);
																	}

																	if (mode == "contemporary"
																			|| mode == "archive") {
																		clone
																				.find(
																						"div.date")
																				.text(
																						obj["dateDisplay"]);
																	}

																	target
																			.before(clone);
																});

												if (data["exhausted"]) {
													target
															.slideUp(
																	"fast",
																	function() {
																		$(this)
																				.remove();
																	});
												}
											});
						});
	} ],

	'dos' : [ function() {
		var partial = /\w+$/.exec(window.location.search);
		partial = partial ? partial[0] : null;

		if (partial) {
			$("input[name='partial']").val(partial).text(partial);
			$("#search").find(".columns:nth-child(2)").append(
					"<a class='button medium clear' href='"
							+ window.location.pathname + "'>Clear</a>");
		}

		$("#pupils")
				.on(
						"click",
						".load",
						function() {
							var loadButton = $(this);
							var page = parseInt($(this).parent()
									.find("a.event").length / 10);
							var crsid = $(this).data("crsid");
							var target = (page == 0) ? loadButton : loadButton
									.prev();
							$
									.getJSON(
											prepareURL("events/queryIndividualsEvents"),
											{
												crsid : crsid,
												page : page
											})
									.done(
											function(data) {
												$
														.each(
																data["data"],
																function(i, obj) {
																	target
																			.before("<a class='event' href='events/"
																					+ obj["eventSummary"]["obfuscatedId"]
																					+ "'>"
																					+ obj["eventSummary"]["title"]
																					+ "</a><br>");
																});

												if (data["exhausted"]) {
													loadButton.remove();
												} else {
													if (page == 0) {
														loadButton
																.before("<br>");
													}
													loadButton
															.text("Load more");
												}
											});
						});

		$(".more-students").click(
				function() {
					var loadButton = $(this);
					var page = parseInt($(this).parent().parent().prev().find(
							".pupil").length / 10);
					$.getJSON(prepareURL("events/queryPupils"), {
						page : page,
						partial : partial
					}).done(
							function(data) {
								$.each(data["pupils"], function() {
									applyTemplate($("#pupils"),
											"signups.events.pupil", {
												pupil : this
											}, "append");
								});

								if (data["exhausted"]) {
									loadButton.remove();
								}
							});
				});

		$("#search").find("input[type='text']").autocomplete({
			minLength : 2,
			source : function(request, response) {
				$.getJSON(prepareURL("events/queryPupilsCRSIDs"), {
					"q" : $("#search").find("input[type='text']").val()
				}).done(function(data) {
					response($.map(data, function(item) {
						return {
							value : item.crsid,
							label : item.crsid
						};
					}));
				});
			}
		});
	} ]
};