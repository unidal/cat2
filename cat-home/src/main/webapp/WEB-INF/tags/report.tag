<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ attribute name="title"%>
<%@ attribute name="navUrlPrefix"%>
<%@ attribute name="timestamp"%>
<%@ attribute name="subtitle" fragment="true"%>
<%@ attribute name="resource" fragment="true"%>

<res:bean id="res" />

<html lang="en">
<head>
	<title>CAT</title>
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
	<meta charset="utf-8">
	<meta name="description" content="CAT Districuted Realtime Monitoring System">
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
	<link rel="stylesheet" href="${model.webapp}/assets/css/bootstrap.min.css">
	<link rel="stylesheet" href="${model.webapp}/assets/css/font-awesome.min.css">
	<link rel="stylesheet" href="${model.webapp}/assets/css/jquery-ui.min.css">
	<link rel="stylesheet" href="${model.webapp}/assets/css/ace-fonts.css">
	<link rel="stylesheet" href="${model.webapp}/assets/css/ace.min.css" id="main-ace-style">
	<link rel="stylesheet" href="${model.webapp}/assets/css/ace-skins.min.css">
	<link rel="stylesheet" href="${model.webapp}/assets/css/ace-rtl.min.css">
	<link rel="stylesheet" href="${model.webapp}/css/body.css">
	<script src="${model.webapp}/js/jquery-1.7.1.js"></script>
	<script src="${model.webapp}/assets/js/ace-extra.min.js"></script>
	<script src="${model.webapp}/assets/js/bootstrap.min.js"></script>
	<script src="${model.webapp}/js/highcharts.js"></script>
	<script src="${model.webapp}/assets/js/jquery-ui.min.js"></script>
	<script src="${model.webapp}/assets/js/jquery.ui.touch-punch.min.js"></script>
	<script src="${model.webapp}/assets/js/ace-elements.min.js"></script>
	<script src="${model.webapp}/assets/js/ace.min.js"></script>
	<jsp:invoke fragment="resource"/>
</head>
<body class="no-skin">
	<a:header/>
	
	<div class="main-container" id="main-container">
		<a:navbar/>
	
		<div class="main-content" style="padding-top:2px;padding-left:2px;padding-right:8px;">
			<script type="text/javascript">
				try{ace.settings.check('main-container', 'fixed')}catch(e){}
			</script>
			
			<div id="dialog-message" class="hide">
				<p>你确定要删除吗？(不可恢复)</p>
			</div>
			
			<div class="breadcrumbs" id="breadcrumbs">
				<script type="text/javascript">
					try{ace.settings.check('breadcrumbs' , 'fixed')}catch(e){}
				</script>
				<span class="text-danger title">【报表时间】</span>
				<span class="text-success"><jsp:invoke fragment="subtitle"/></span>
				<div class="nav-search nav" id="nav-search">
					<span class="text-danger switch">【<a class="switch" href="${model.baseUri}?op=history&domain=${model.domain}&ip=${model.ipAddress}"><span class="text-danger">切到历史模式</span></a>】</span>
					<c:forEach var="nav" items="${model.navs}">
							&nbsp;[ <a href="${model.baseUri}?date=${model.date}&ip=${model.ipAddress}&step=${nav.hours}&${navUrlPrefix}">${nav.title}</a> ]
						</c:forEach>
						&nbsp;[ <a href="${model.baseUri}?${navUrlPrefix}">now</a> ]&nbsp;
				</div><!-- /.nav-search -->
			</div>
			
			<script>
				function buildHref(domain){
					var href = '<a href="?op=${payload.action.name}&domain='+domain+'&date=${model.date}">&nbsp;[&nbsp;'+domain+'&nbsp;]&nbsp;</a>';
					return href;
				}
				$(document).ready(function() {
					var domains= getcookie('CAT_DOMAINS');
					var domainArray =domains.split("|");
					var html = '';
					var length =domainArray.length;
					
					for(var i=0;i<length;i++){
						var href = buildHref(domainArray[i])
						html+= href;
					}
					$('#frequentNavbar').html(html);
					$("#search_go").bind("click",function(e){
						var newUrl = '${model.baseUri}?op=${payload.action.name}&domain='+$( "#search" ).val() +'&date=${model.date}';
						window.location.href = newUrl;
					});
					$('#wrap_search').submit(
						function(){
							var newUrl = '${model.baseUri}?op=${payload.action.name}&domain='+$( "#search" ).val() +'&date=${model.date}';
							window.location.href = newUrl;
							return false;
						}		
					);
				});
			</script>
			
			<jsp:doBody/>
		</div>
	</div>
</body>
</html>
