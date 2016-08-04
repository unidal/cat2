<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<div class="breadcrumbs" id="breadcrumbs">
   <span class="text-danger title">【报表时间】</span>
   <span class="text-success">${w:format(model.report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.report.endTime,'yyyy-MM-dd HH:mm:ss')}</span>
   <div class="nav-search nav" id="nav-search">
      <c:choose>
         <c:when test="${ctx.period.name eq 'hour'}">
               <span class="text-danger switch">【<a class="switch" href="?${ctx.query.period['day'].step['']}"><span class="text-danger">切到历史模式</span></a>】</span>
               <c:forEach var="bar" items="${ctx.timeBars}">
                  &nbsp;[ <a href="?${ctx.query.date[payload.formattedStartTime].step[bar.step]}">${bar.title}</a> ]
               </c:forEach>
         </c:when>
         <c:otherwise>
               <span class="text-danger switch">【<a class="switch" href="?${ctx.query.period[''].step['']}"><span class="text-danger">切到小时模式</span></a>】</span>
               <c:forEach var="bar" items="${ctx.timeBars}">
                  &nbsp;[ <a href="?${ctx.query.period[bar.title]}" class="${model.period.name eq bar.title ? 'current' : '' }">${bar.title}</a> ]
               </c:forEach>
               &nbsp;[ <a href="?${ctx.query.date[payload.formattedStartTime].step[-1]}">${ctx.activeTimeBar.last}</a> ]
               &nbsp;[ <a href="?${ctx.query.date[payload.formattedStartTime].step[1]}">${ctx.activeTimeBar.next}</a> ]
         </c:otherwise>
      </c:choose>
      &nbsp;[ <a href="?${ctx.query.step[''].date['']}">now</a> ]&nbsp;
   </div><!-- /.nav-search -->
</div>

<table class="groups">
   <tr class="left">
      <th> 
         <c:forEach var="group" items="${model.groups}">
               &nbsp;[&nbsp;
                  <a href="?${ctx.query.op['groupReport'].group[group]}">${group}</a>
               &nbsp;]&nbsp;
          </c:forEach>
      </th>
   </tr>
</table>

<table class="machines">
	<tr class="left">
		<th>&nbsp;[&nbsp;
		    <c:choose>
				<c:when test="${payload.ip eq 'All'}">
					<a href="?${ctx.query.ip['']}" class="current">All</a>
				</c:when>
				<c:otherwise>
					<a href="?${ctx.query.ip['']}">All</a>
				</c:otherwise>
			</c:choose> &nbsp;]&nbsp;
			<c:forEach var="ip" items="${model.ips}">
			    &nbsp;[&nbsp;
   	  		    <c:choose>
					<c:when test="${payload.ip eq ip}">
						<a href="?${ctx.query.ip[ip]}" class="current">${ip}</a>
					</c:when>
					<c:otherwise>
						<a href="?${ctx.query.ip[ip]}">${ip}</a>
					</c:otherwise>
				</c:choose>
   	 		    &nbsp;]&nbsp;
			 </c:forEach>
		</th>
	</tr>
</table>


