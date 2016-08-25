<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="ctx" type="org.unidal.cat.plugin.event.report.page.Context" scope="request" />
<jsp:useBean id="payload" type="org.unidal.cat.plugin.event.report.page.Payload" scope="request" />
<jsp:useBean id="model"	type="org.unidal.cat.plugin.event.report.page.Model" scope="request" />

<script src="${model.webapp}/js/jquery-1.7.1.js"></script>
<script src="${model.webapp}/js/highcharts.js"></script>
<script src="${model.webapp}/js/baseGraph.js"></script>
<script src="${model.webapp}/js/event.js"></script>

<svg version="1.1" width="980" height="200" xmlns="http://www.w3.org/2000/svg">
  ${model.graph.barCharts.hits}
  ${model.graph.barCharts.failures}
</svg>

<c:if test="${payload.ip eq 'All'}">
<table class='table table-hover table-striped table-condensed' style="width:100%">
	<tr><td colspan="8"><h4 style="text-align:center" class='text-center text-info'>Distribution by IP</h4></td></tr>
	<tr>
		<th class="right">IP</th>
		<th class="right">Total</th>
		<th class="right">Failure</th>
		<th class="right">Failure%</th>
	</tr>
	<c:forEach var="item" items="${model.graph.distributions}" varStatus="status">
		<tr class=" right">
			<td>${item.ip}</td>
			<td>${w:format(item.totalCount,'#,###,###,###,##0')}</td>
			<td>${w:format(item.failCount,'#,###,###,###,##0')}</td>
			<td>${w:format(item.failPercent/100,'0.0000%')}</td>
		</tr>
	</c:forEach>
</table>

<div id="distributionChart" class="pieChart"></div>
<div id="distributionChartMeta" style="display:none">${model.graph.pieChart.json}</div>
</c:if>

<script type="text/javascript">
	var distribution = ${model.graph.pieChart.json};
	
	if (distribution) {
		graphPieChart(document.getElementById('distributionChart'), distribution);
	}
</script>
<br>
