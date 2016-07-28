<%@ page pageEncoding="UTF-8" %>

<h4 class="text-success">CAT总体介绍</h4>
<h5>CAT(Central Application Tracking)是基于Java开发的实时应用监控平台，为大众点评网提供了全面的监控服务和决策支持。
</h5>
<h5>CAT作为大众点评网基础监控组件，它已经在中间件框架（MVC框架，RPC框架，数据库框架，缓存框架等）中得到广泛应用，为点评各业务线提供系统的性能指标、健康状况、基础告警等。</h5>
</br>
<h4 class="text-success">CAT目前现状</h4>
	<ul>
		<li>集成中间件产品（RPC、SQL、Cache等）</li>
		<li>10台CAT物理监控集群</li>
		<li>1000+ 业务应用（包括部分.net以及Job）</li>
		<li>3000+ 应用服务器</li>
		<li>30TB 消息，~250亿消息（每天）</li>
	</ul>
	<br/>
<h4 class="text-success">CAT监控大盘</h4>
<div>
	<a id="navmetricDashboard" class="btn btn-sm btn-primary" target="_blank" href="/cat/r/top?op=view">系统报错大盘</a>
	<a id="navdashboard" class="btn btn-sm btn-primary" target="_blank" href="/cat/r/dependency?op=dashboard">应用监控大盘</a>
	<a id="navbussiness" class="btn btn-sm btn-primary" target="_blank" href="/cat/r/metric?op=dashboard">业务监控大盘</a>
	<a id="navbussiness" class="btn btn-sm btn-primary" target="_blank" href="/cat/r/network?op=dashboard">网络监控大盘</a>
	<a id="navbussiness" class="btn btn-sm btn-primary" target="_blank" href="/cat/r/storage?op=dashboard">数据库监控大盘</a>
</div>
</br>
<h4 class="text-success">CAT其他环境</h4>
<div>
	<a class="btn btn-sm btn-primary" href="http://cat.qa.dianpingoa.com/cat/r/">测试环境</a>
	<a class="btn btn-sm btn-primary" href="http://ppe.cat.dp/cat/r/">PPE环境</a>
	<a class="btn btn-sm btn-primary" href="http://cat.dianpingoa.com/cat/r/">生产环境</a>
</div>
</br>
