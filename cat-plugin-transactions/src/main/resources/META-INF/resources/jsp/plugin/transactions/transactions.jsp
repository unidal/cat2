<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="r" uri="/WEB-INF/report.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<jsp:useBean id="ctx" type="org.unidal.cat.plugin.transactions.report.page.Context" scope="request" />
<jsp:useBean id="payload" type="org.unidal.cat.plugin.transactions.report.page.Payload" scope="request" />
<jsp:useBean id="model"	type="org.unidal.cat.plugin.transactions.report.page.Model" scope="request" />

<r:report>
<jsp:attribute name="subtitle">${w:format(model.report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
<jsp:attribute name="resource">
	<script src="${model.webapp}/js/baseGraph.js"></script>
	<script src="${model.webapp}/js/appendHostname.js"></script>
    <script src="${model.webapp}/js/transaction.js"></script>
</jsp:attribute>
<jsp:body>
	
	<table class='table table-striped table-condensed table-hover' style="width:100%;">
		<tr>
		   <th class="left" colspan="13">
		   	  <input type="text" name="query" id="query" size="40" value="${payload.query}">
		      <input  class="btn btn-primary btn-sm"  value="Filter" onclick="selectByName('${payload.formattedStartTime}','${payload.domain}','${payload.ip}','${payload.type}')" type="submit">
			  支持多个字符串查询，例如sql|url|task，查询结果为包含任一sql、url、task的列。
		   </th>
		</tr>
		<tr>
			<c:choose>
				<c:when test="${empty payload.type}"><th class="left"><a href="?${ctx.query.sort['id']}">Type</a></th></c:when>
				<c:otherwise><th class="left"><a href="?${ctx.query.sort['id']}">Name</a></th></c:otherwise>
			</c:choose>
			<th class="right nowrap"><a href="?${ctx.query.sort['total']}">Total</a></th>
			<th class="right"><a href="?${ctx.query.sort['failure']}">Failure</a></th>
			<th class="right"><a href="?${ctx.query.sort['failurePercent']}">Failure %</a></th>
			<th class="right longText">Sample Link</th>
			<th class="right"><a href="?${ctx.query.sort['min']}">Min</a>(ms)</th>
			<th class="right"><a href="?${ctx.query.sort['max']}">Max</a>(ms)</th>
			<th class="right"><a href="?${ctx.query.sort['avg']}">Avg</a>(ms)</th>
			<th class="right"><a href="?${ctx.query.sort['95line']}">95Line</a>(ms)</th>
			<th class="right"><a href="?${ctx.query.sort['99line']}">99.9Line</a>(ms)</th>
			<th class="right"><a href="?${ctx.query.sort['std']}">Std</a>(ms)</th>
			<th class="right nowrap"><a href="?${ctx.query.sort['total']}">QPS</a></th>
			<c:if test="${not empty payload.type}">
			   <th class="right"><a href="?${ctx.query.sort['total']}">Percent %</a></th>
			</c:if>
		</tr>
		<tr class="graphs"><td colspan="13" style="display:none"><div id="-1" style="display:none"></div></td></tr>
		<c:forEach var="row" items="${model.table.rows}" varStatus="status">
			<c:set var="e" value="${row}"/>
			<c:set var="lastIndex" value="${status.index}"/>
			<tr class="right">
				<c:choose>
					<c:when test="${empty payload.type}">
						<td class="left longText" style="white-space:normal">
							<a href="?${ctx.query.op['graph'].type[e.id]}" class="graph_link" data-status="${status.index}">[:: show ::]</a>
							&nbsp;&nbsp;<a href="?${ctx.query.type[e.id].query['']}">${e.id}</a>
						</td>
					</c:when>
					<c:when test="${not empty payload.type and e.summary}">
						<td class="left longText" style="white-space:normal">
						   <a href="?${ctx.query.op['graph'].name['']}" class="graph_link" data-status="${status.index}">[:: show ::]</a>
						   &nbsp;&nbsp;<a href="?${ctx.query.type[payload.type]}"/>Type: ${w:shorten(payload.type, 120)}</a>
						</td>
					</c:when>
					<c:otherwise>
						<td class="left" style="white-space:normal">
						   <a href="?${ctx.query.op['graph'].name[e.id]}" class="graph_link" data-status="${status.index}">[:: show ::]</a>
						   &nbsp;&nbsp;${w:shorten(e.id, 120)}
						</td>
					</c:otherwise>
				</c:choose>
				<td class="nowrap">${w:format(e.total,'#,###,###,###,##0')}</td>
				<td class="nowrap">${w:format(e.failure,'#,###,###,###,##0')}</td>
				<td class="nowrap">&nbsp;${w:format(e.failurePercent/100,'#.####%')}</td>
				<td class="longText"><a href="${model.webapp}/r/m/${e.sampleMessageId}?${ctx.query}">Log View</a></td>
				<td>${w:format(e.min,'###,##0.#')}</td>
				<td>${w:format(e.max,'###,##0.#')}</td>
				<td class="nowrap">${w:format(e.avg,'###,##0.0')}</td>
				<c:choose>
					<c:when test="${status.index > 0}">
						<td>${w:format(e.line95,'###,##0.0')}</td>
						<td>${w:format(e.line99,'###,##0.0')}</td>
					</c:when>
					<c:otherwise>
						<td>-</td>
						<td>-</td>
					</c:otherwise>
				</c:choose>
				<td>${w:format(e.std,'###,##0.0')}</td>
				<td class="nowrap">${w:format(e.tps,'###,##0.0')}</td>
				<c:if test="${not empty payload.type}">
					<td class="nowrap">${w:format(e.total / (model.table.total eq 0 ? 1 : model.table.total),'0.00%')}</td>
				</c:if>
			</tr>
			<tr class="graphs"><td colspan="13" style="display:none"><div id="${status.index}" style="display:none"></div></td></tr>
		</c:forEach>
	</table>
	<font color="white">${lastIndex}</font>

	<c:choose>
		<c:when test="${not empty payload.type}">
			<table>
				<tr>
					<td><div id="graph" class="pieChart"></div></td>
				</tr>
			</table>
			<script type="text/javascript">
				var data = ${model.table.pieChart.json};
				
				graphPieChart(document.getElementById('graph'), data);
			</script>
		</c:when>
	</c:choose>
</jsp:body>

</r:report>
