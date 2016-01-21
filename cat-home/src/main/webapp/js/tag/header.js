jQuery(function($) {
	//override dialog's title function to allow for HTML titles
	$.widget("ui.dialog", $.extend({}, $.ui.dialog.prototype, {
		_title: function(title) {
			var $title = this.options.title || '&nbsp;'
			if( ("title_html" in this.options) && this.options.title_html == true )
				title.html($title);
			else title.text($title);
		}
	}));

	$(".delete").on('click', function(e) {
		e.preventDefault();
		var anchor = this;
		var dialog = $( "#dialog-message" ).removeClass('hide').dialog({
			modal: true,
			title: "<div class='widget-header widget-header-small'><h4 class='smaller'><i class='ace-icon fa fa-check'></i>CAT提示</h4></div>",
			title_html: true,
			buttons: [ 
				{
					text: "Cancel",
					"class" : "btn btn-xs",
					click: function() {
						$( this ).dialog( "close" ); 
					} 
				},
				{
					text: "OK",
					"class" : "btn btn-primary btn-xs",
					click: function() {
						window.location.href=anchor.href;
					} 
				}
			]
		});
	});
	//tooltips
	$( "#show-option" ).tooltip({
		show: {
			effect: "slideDown",
			delay: 250
		}
	});

	$( "#hide-option" ).tooltip({
		hide: {
			effect: "explode",
			delay: 250
		}
	});
	$( "#open-event" ).tooltip({
		show: null,
		position: {
			my: "left top",
			at: "left bottom"
		},
		open: function( event, ui ) {
			ui.tooltip.animate({ top: ui.tooltip.position().top + 10 }, "fast" );
		}
	});
	
	//Menu
	$( "#menu" ).menu();

	//spinner
	var spinner = $( "#spinner" ).spinner({
		create: function( event, ui ) {
			//add custom classes and icons
			$(this)
			.next().addClass('btn btn-success').html('<i class="ace-icon fa fa-plus"></i>')
			.next().addClass('btn btn-danger').html('<i class="ace-icon fa fa-minus"></i>')
			
			//larger buttons on touch devices
			if('touchstart' in document.documentElement) 
				$(this).closest('.ui-spinner').addClass('ui-spinner-touch');
		}
	});

	//slider example
	$( "#slider" ).slider({
		range: true,
		min: 0,
		max: 500,
		values: [ 75, 300 ]
	});


	//jquery accordion
	$( "#accordion" ).accordion({
		collapsible: true ,
		heightStyle: "content",
		animate: 250,
		header: ".accordion-header"
	}).sortable({
		axis: "y",
		handle: ".accordion-header",
		stop: function( event, ui ) {
			// IE doesn't register the blur when sorting
			// so trigger focusout handlers to remove .ui-state-focus
			ui.item.children( ".accordion-header" ).triggerHandler( "focusout" );
		}
	});
	
	//jquery tabs
	$( "#tabs" ).tabs();
});

function getcookie(objname) {
	var arrstr = document.cookie.split("; ");
	for ( var i = 0; i < arrstr.length; i++) {
		var temp = arrstr[i].split("=");
		if (temp[0] == objname) {
			return temp[1];
		}
	}
	return "";
}
function showDomain() {
	var b = $('#switch').html();
	if (b == '全部') {
		$('.domainNavbar').slideDown();
		$('#switch').html("收起");
	} else {
		$('.domainNavbar').slideUp();
		$('#switch').html("全部");
	}
}
function showFrequent(){
	var b = $('#frequent').html();
	if (b == '常用') {
		$('.frequentNavbar').slideDown();
		$('#frequent').html("收起");
	} else {
		$('.frequentNavbar').slideUp();
		$('#frequent').html("常用");
	}
}