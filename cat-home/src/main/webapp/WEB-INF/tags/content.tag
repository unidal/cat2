<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ attribute name="subtitle" fragment="true"%>

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

