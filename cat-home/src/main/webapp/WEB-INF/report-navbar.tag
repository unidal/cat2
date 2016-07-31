<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>

<div id="sidebar" class="sidebar responsive">
	<script type="text/javascript">
		try{ace.settings.check('sidebar', 'fixed')}catch(e){}
	</script>
	<div class="sidebar-shortcuts" id="sidebar-shortcuts">
		<div class="sidebar-shortcuts-large" id="sidebar-shortcuts-large">
			<button class="btn btn-success" id="tab_realtime">
				<i class="ace-icon fa fa-signal"></i>&nbsp;&nbsp;实时
			</button>
			<button class="btn btn-grey" id="tab_offtime">
				<i class="ace-icon fa fa-film"></i>&nbsp;&nbsp;离线
			</button>
			<!-- #section:basics/sidebar.layout.shortcuts -->
			<button class="btn btn-warning" id="tab_document">
				<i class="ace-icon fa fa-users"></i>&nbsp;&nbsp;文档
			</button>
			<button class="btn btn-danger" id="tab_config">
				<i class="ace-icon fa fa-cogs"></i>&nbsp;&nbsp;配置
			</button>
		</div>
		<div class="sidebar-shortcuts-mini" id="sidebar-shortcuts-mini">
			<span class="btn btn-success"></span>
			<span class="btn btn-info"></span>
			<span class="btn btn-warning"></span>
			<span class="btn btn-danger"></span>
		</div>
	</div>
	<ul class="nav nav-list" style="top: 0px;">
		<li id="Dashboard_report" class="hsub">
			<a href="#" class="dropdown-toggle">
			<i class="menu-icon fa fa-tachometer"></i> <span class="menu-text">Dashboard</span>
				<b class="arrow fa fa-angle-down"></b>
			</a><b class="arrow"></b>
			<ul class="submenu">
				<li id="dashbord_system"><a href="${model.webapp}/r/top?op=view&domain=${model.domain}">
					<i class="menu-icon fa fa-caret-right"></i>报错大盘</a>
					<b class="arrow"></b></li>
				<li id="dashbord_metric"><a href="${model.webapp}/r/metric?op=dashboard&domain=${model.domain}">
					<i class="menu-icon fa fa-caret-right"></i>业务大盘</a>
					<b class="arrow"></b></li>
				<li id="dashbord_network"><a href="${model.webapp}/r/network?op=dashboard&domain=${model.domain}">
					<i class="menu-icon fa fa-caret-right"></i>网络大盘</a>
					<b class="arrow"></b></li>
				<li id="dashbord_application"><a href="${model.webapp}/r/dependency?op=dashboard&domain=${model.domain}">
					<i class="menu-icon fa fa-caret-right"></i>应用大盘</a>
					<b class="arrow"></b></li>
				<li id="dashbord_database"><a href="${model.webapp}/r/storage?op=dashboard&domain=${model.domain}">
					<i class="menu-icon fa fa-caret-right"></i>数据库大盘</a>
					<b class="arrow"></b></li>
				<li id="dashbord_cache"><a href="${model.webapp}/r/storage?op=dashboard&domain=${model.domain}&type=Cache">
					<i class="menu-icon fa fa-caret-right"></i>缓存大盘</a>
					<b class="arrow"></b></li>
			</ul>
		</li>
		<li id="Web_report" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon fa fa-globe"></i> <span class="menu-text">Web</span>
				<b class="arrow fa fa-angle-down"></b>
		</a> <b class="arrow"></b>
			<ul class="submenu">
				<li id="web_trend"><a href="${model.webapp}/r/web?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=view&op=view">
					<i class="menu-icon fa fa-caret-right"></i>URL访问趋势</a>
					<b class="arrow"></b></li>
				<li id="web_piechart"><a href="${model.webapp}/r/web?op=piechart&domain=${model.domain}">
					<i class="menu-icon fa fa-caret-right"></i>URL访问分布</a>
					<b class="arrow"></b></li>
				<li id="web_problem"><a href="${model.webapp}/r/web?op=problem&domain=${model.domain}">
					<i class="menu-icon fa fa-caret-right"></i>JS错误日志</a>
					<b class="arrow"></b></li>
			</ul>
		</li>
		<li id="App_report" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon glyphicon glyphicon-phone"></i> <span class="menu-text">App</span>
				<b class="arrow fa fa-angle-down"></b>
		</a> <b class="arrow"></b>
			<ul class="submenu">
				<li id="trend"><a href="${model.webapp}/r/app?op=view&showActivity=false&domain=${model.domain}">
					<i class="menu-icon fa fa-caret-right"></i>API访问趋势</a>
					<b class="arrow"></b></li>
				<li id="accessPiechart"><a href="${model.webapp}/r/app?op=piechart&domain=${model.domain}">
					<i class="menu-icon fa fa-caret-right"></i>访问量分布</a>
					<b class="arrow"></b></li>
				<li id="speed"><a href="${model.webapp}/r/app?op=speed&domain=${model.domain}">
					<i class="menu-icon fa fa-caret-right"></i>APP页面测速</a>
					<b class="arrow"></b></li>
				<li id="statistics"><a href="${model.webapp}/r/app?op=statistics&domain=${model.domain}&type=all">
					<i class="menu-icon fa fa-caret-right"></i>每天报表统计</a>
					<b class="arrow"></b></li>
				<li id="connTrend"><a href="${model.webapp}/r/app?op=connLinechart&domain=${model.domain}">
					<i class="menu-icon fa fa-caret-right"></i>长连访问趋势</a>
					<b class="arrow"></b></li>
				<li id="connPiechart"><a href="${model.webapp}/r/app?op=connPiechart&domain=${model.domain}">
					<i class="menu-icon fa fa-caret-right"></i>长连访问分布</a>
					<b class="arrow"></b></li>
				<li id="crashLog"><a href="${model.webapp}/r/app?op=crashLog&domain=${model.domain}">
					<i class="menu-icon fa fa-caret-right"></i>Crash日志</a>
					<b class="arrow"></b></li>
				<li id="traceLog"><a href="http://mobile-tracer-web01.nh/" target="_blank">
					<i class="menu-icon fa fa-caret-right"></i>dpid跟踪日志</a>
					<b class="arrow"></b></li>
			</ul>
		</li>
		<li id="Transaction_report" >
			<a href="${model.webapp}/r/t?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
				<i class="menu-icon glyphicon glyphicon-time"></i>
				<span class="menu-text">Transaction</span>
			</a>
		</li>
		<li id="Event_report" >
			<a href="${model.webapp}/r/e?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
				<i class="menu-icon fa fa-flag"></i>
				<span class="menu-text">Event</span>
			</a>
		</li>					
		<li id="Problem_report" >
			<a href="${model.webapp}/r/p?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
				<i class="menu-icon fa fa-bug"></i>
				<span class="menu-text">Problem</span>
			</a>
		</li>			
		<li id="Heartbeat_report" >
			<a href="${model.webapp}/r/h?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
				<i class="menu-icon  fa fa-heart"></i>
				<span class="menu-text">Heartbeat</span>
			</a>
		</li>		
		<li id="Cross_report" >
			<a href="${model.webapp}/r/cross?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
				<i class="menu-icon  glyphicon glyphicon-random"></i>
				<span class="menu-text">Cross</span>
			</a>
		</li>
		<li id="Cache_report" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon glyphicon glyphicon-flash"></i> <span class="menu-text">Cache</span>
				<b class="arrow fa fa-angle-down"></b>
			</a> <b class="arrow"></b>
			<ul class="submenu">
				<li id="cache_operation"><a href="${model.webapp}/r/storage?id=memcached&type=Cache&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}">
					<i class="menu-icon fa fa-caret-right"></i>访问趋势</a>
					<b class="arrow"></b></li>
				<li id="cache_info"><a href="${model.webapp}/r/cache?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=view">
					<i class="menu-icon fa fa-caret-right"></i>访问情况</a>
					<b class="arrow"></b></li>
			</ul>
		</li>
		<li id="Database_report" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon fa fa-lemon-o"></i> <span class="menu-text">Database</span>
				<b class="arrow fa fa-angle-down"></b>
			</a> <b class="arrow"></b>
			<ul class="submenu">
				<li id="database_operation"><a href="${model.webapp}/r/storage?id=cat&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}">
					<i class="menu-icon fa fa-caret-right"></i>访问趋势</a>
					<b class="arrow"></b></li>
				<li id="database_system"><a href="${model.webapp}/r/database?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=view">
					<i class="menu-icon fa fa-caret-right"></i>系统指标</a>
					<b class="arrow"></b></li>
			</ul>
		</li>
		<li id="Dependency_report" class="hsub"><a href="${model.webapp}/r/dependency?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=view&op=view" class="dropdown-toggle"> <i class="menu-icon glyphicon glyphicon-road"></i> <span class="menu-text">Dependency</span>
				<b class="arrow fa fa-angle-down"></b>
		</a> <b class="arrow"></b>
			<ul class="submenu">
				<li id="dependency_trend"><a href="${model.webapp}/r/dependency?op=lineChart&domain=${model.domain}&date=${model.date}">
					<i class="menu-icon fa fa-caret-right"></i>趋势图</a>
					<b class="arrow"></b></li>
				<li id="dependency_topo"><a href="${model.webapp}/r/dependency?op=dependencyGraph&domain=${model.domain}&date=${model.date}">
					<i class="menu-icon fa fa-caret-right"></i>拓扑图</a>
					<b class="arrow"></b></li>
				
			</ul>
		</li>
		<li id="Matrix_report" >
			<a href="${model.webapp}/r/matrix?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
				<i class="menu-icon  fa  fa-flask"></i>
				<span class="menu-text">Matrix</span>
			</a>
		</li>
		<li id="State_report" >
			<a href="${model.webapp}/r/state?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
				<i class="menu-icon fa fa-bar-chart-o"></i>
				<span class="menu-text">State</span>
			</a>
		</li>
		<li id="System_report" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon fa fa-gavel"></i> <span class="menu-text">System</span>
				<b class="arrow fa fa-angle-down"></b>
		</a> <b class="arrow"></b>
			<ul class="submenu">
				<li id="system_cdn"><a href="${model.webapp}/r/cdn?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
					<i class="menu-icon fa fa-caret-right"></i>CDN监控</a>
					<b class="arrow"></b></li>
				<li id="system_network"><a href="${model.webapp}/r/network?op=metric&product=&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}">
					<i class="menu-icon fa fa-caret-right"></i>网络监控</a>
					<b class="arrow"></b></li>
				<li id="system_paas"><a href="${model.webapp}/r/system?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
					<i class="menu-icon fa fa-caret-right"></i>PAAS监控</a>
					<b class="arrow"></b></li>
				<li id="system_alteration"><a href="${model.webapp}/r/alteration?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}">
					<i class="menu-icon fa fa-caret-right"></i>线上变更</a>
					<b class="arrow"></b></li>
				<li id="system_alert"><a href="${model.webapp}/r/alert?domain=${model.domain}&op=${payload.action.name}">
					<i class="menu-icon fa fa-caret-right"></i>告警信息</a>
					<b class="arrow"></b></li>
				<li id="system_activity"><a href="${model.webapp}/r/activity?domain=${model.domain}&op=${payload.action.name}">
					<i class="menu-icon fa fa-caret-right"></i>活动大盘</a>
					<b class="arrow"></b></li>
			</ul>
		</li>
	</ul>
	<!-- #section:basics/sidebar.layout.minimize -->
	<div class="sidebar-toggle sidebar-collapse" id="sidebar-collapse">
		<i class="ace-icon fa fa-angle-double-left" data-icon1="ace-icon fa fa-angle-double-left" data-icon2="ace-icon fa fa-angle-double-right"></i>
	</div>

	<!-- /section:basics/sidebar.layout.minimize -->
	<script type="text/javascript">
		try{ace.settings.check('sidebar' , 'collapsed')}catch(e){}
	</script>
	
	<script  type="text/javascript">
		$(document).ready(function() {
			$("#tab_realtime").click(function(){
				window.location.href = "${model.webapp}/r/t?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}";
			});
			$("#tab_offtime").click(function(){
				window.location.href = "${model.webapp}/r/statistics?op=service&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&op=${payload.action.name}";
			});
			$("#tab_document").click(function(){
				window.location.href = "${model.webapp}/doc?${ctx.query}";
			});
			$("#tab_config").click(function(){
				window.location.href = "${model.webapp}/s/config?op=projects";
			});
		});
		$("#tab_realtime").addClass("disabled");
	</script>
</div>

