<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ attribute name="subtitle" fragment="true"%>

<div class="main-content" style="padding-top:2px;padding-left:2px;padding-right:8px;">

<div id="dialog-message" class="hide">
	<p>你确定要删除吗？(不可恢复)</p>
</div>

<div class="breadcrumbs" id="breadcrumbs">
	<span class="text-danger title">【报表时间】</span>
	<span class="text-success"><jsp:invoke fragment="subtitle"/></span>
	<div class="nav-search nav" id="nav-search">
		<c:choose>
			<c:when test="${ctx.period.name eq 'hour'}">
         		<span class="text-danger switch">【<a class="switch" href="?${ctx.query.period['day'].step['']}"><span class="text-danger">切到历史模式</span></a>】</span>
         		<c:forEach var="nav" items="${model.navs}">
         			&nbsp;[ <a href="?${ctx.query.step[nav.hours]}">${nav.title}</a> ]
         		</c:forEach>
			</c:when>
			<c:otherwise>
         		<span class="text-danger switch">【<a class="switch" href="?${ctx.query.period[''].step['']}"><span class="text-danger">切到小时模式</span></a>】</span>
         		<c:forEach var="nav" items="${model.historyNavs}">
         			&nbsp;[ <a href="?${ctx.query.period[nav.title]}" class="${ctx.period.name eq nav.title ? 'current' : '' }">${nav.title}</a> ]
         		</c:forEach>
         		&nbsp;[ <a href="?${ctx.query.step[-1]}">${model.currentNav.last}</a> ]
         		&nbsp;[ <a href="?${ctx.query.step[1]}">${model.currentNav.next}</a> ]
			</c:otherwise>
		</c:choose>
		&nbsp;[ <a href="?${ctx.query.step[''].date['']}">now</a> ]&nbsp;
	</div><!-- /.nav-search -->
</div>

<script>
	function buildHref(domain){
		return '<a href="?${ctx.query.domain['']}&domain='+domain+'">&nbsp;[&nbsp;'+domain+'&nbsp;]&nbsp;</a>';
	}
	
	try{ace.settings.check('main-container', 'fixed');}catch(e){}
	try{ace.settings.check('breadcrumbs' , 'fixed');}catch(e){}

	$(document).ready(function() {
		var str= getcookie('CAT_DOMAINS');
		var domains = 'a|b|c|d'.split('|');//str.split("|");
		var html = '';
		
		for(var i in domains){
			html+= '&nbsp;[&nbsp;<a href="?${ctx.query.domain['']}&domain='+domains[i]+'">'+domains[i]+'</a>&nbsp;]&nbsp;';
		}
		$('#frequentNavbar').html(html);
		$("#search_go").bind("click",function(e){
			window.location.href = '?${ctx.query.domain['']}&domain='+$("#search").val();
		});
		$('#wrap_search').submit(function(){
			window.location.href = '?${ctx.query.domain['']}&domain='+$( "#search" ).val();
			return false;
		});
	});
</script>

<jsp:doBody/>

</div>