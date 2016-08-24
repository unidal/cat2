<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<jsp:useBean id="ctx" type="org.unidal.cat.plugin.events.report.page.Context" scope="request" />
<jsp:useBean id="payload" type="org.unidal.cat.plugin.events.report.page.Payload" scope="request" />
<jsp:useBean id="model"	type="org.unidal.cat.plugin.events.report.page.Model" scope="request" />

<script src="${model.webapp}/js/jquery-1.7.1.js"></script>
<script src="${model.webapp}/js/highcharts.js"></script>
<script src="${model.webapp}/js/baseGraph.js"></script>
<script src="${model.webapp}/js/event.js"></script>

<table>
	<tr>
		<td><div id="responseTrend" class="graph"></div></td>
		<td><div id="hitTrend" class="graph"></div></td>
		<td><div id="errorTrend" class="graph"></div></td>
	</tr>
	<tr>
		<td style="display:none">
			<div id="responseTrendMeta">${model.graph.lineCharts.average.json}</div>
			<div id="hitTrendMeta">${model.graph.lineCharts.hits.json}</div>
			<div id="errorTrendMeta">${model.graph.lineCharts.failures.json}</div>
		</td>
	</tr>
</table>

<c:if test="${payload.ip eq 'All'}">
<table class='table table-hover table-striped table-condensed' style="width:100%">
	<tr><td colspan="8"><h4 style="text-align:center" class='text-center text-info'>Distribution by IP</h4></td></tr>
	<tr>
		<th class="right">IP</th>
		<th class="right">Total</th>
		<th class="right">Failure</th>
		<th class="right">Failure%</th>
		<th class="right">Min(ms)</th>
		<th class="right">Max(ms)</th>
		<th class="right">Avg(ms)</th>
		<th class="right">Std(ms)</th>
	</tr>
	<c:forEach var="item" items="${model.graph.distributions}" varStatus="status">
		<tr class=" right">
			<td>${item.ip}</td>
			<td>${w:format(item.totalCount,'#,###,###,###,##0')}</td>
			<td>${w:format(item.failCount,'#,###,###,###,##0')}</td>
			<td>${w:format(item.failPercent/100,'0.0000%')}</td>
			<td>${w:format(item.min,'###,##0.#')}</td>
			<td>${w:format(item.max,'###,##0.#')}</td>
			<td>${w:format(item.avg,'###,##0.0')}</td>
			<td>${w:format(item.std,'###,##0.0')}</td>
		</tr>
	</c:forEach>
</table>

<div id="distributionChart" class="pieChart"></div>
<div id="distributionChartMeta" style="display:none">${model.graph.pieChart.json}</div>
</c:if>

<script type="text/javascript">
	var responseTrendData = ${model.graph.lineCharts.average.json};
	var hitTrendData = ${model.graph.lineCharts.hits.json};
	var errorTrendData = ${model.graph.lineCharts.failures.json};

	graphLineChart(document.getElementById('responseTrend'),responseTrendData);
	graphLineChart(document.getElementById('hitTrend'),hitTrendData);
	graphLineChart(document.getElementById('errorTrend'),errorTrendData);
	
	var distribution = ${model.graph.pieChart.json};
	
	if (distribution) {
		graphPieChart(document.getElementById('distributionChart'), distribution);
	}
</script>
<br>