<%@ page contentType="text/html; charset=utf-8" isELIgnored="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="r" uri="/WEB-INF/report.tld"%>
<jsp:useBean id="ctx" type="org.unidal.cat.core.message.page.home.Context" scope="request"/>
<jsp:useBean id="payload" type="org.unidal.cat.core.message.page.home.Payload" scope="request"/>
<jsp:useBean id="model" type="org.unidal.cat.core.message.page.home.Model" scope="request"/>

<r:report>
   <jsp:attribute name="head">${payload.header}</jsp:attribute>
   <jsp:attribute name="menu">${payload.header}</jsp:attribute>
   <jsp:attribute name="navbar">false</jsp:attribute>
   <jsp:attribute name="resource">
       <link rel="stylesheet" href="${model.webapp}/css/message.css">
       <script src="${model.webapp}/js/message.js"></script>
   </jsp:attribute>

   <jsp:body>
      <c:choose>
         <c:when test="${empty model.html}">
            <div class="error">Sorry, the message is not found. It could be missing or even not created at all.</div>
         </c:when>
         <c:otherwise>
            <c:if test="${payload.header}">
               <c:choose>
                  <c:when test="${payload.waterfall}">
                     <div>&nbsp;&nbsp;<a href="?${ctx.query.waterfall['']}">Text</a>&nbsp;&nbsp;&nbsp;&nbsp;Graph</div>
                  </c:when>
                  <c:otherwise>
                     <div>&nbsp;&nbsp;Text&nbsp;&nbsp;&nbsp;&nbsp;<a href="?${ctx.query.waterfall['true']}">Graph</a></div>
                  </c:otherwise>
               </c:choose>
            </c:if>
            ${model.html}
         </c:otherwise>
      </c:choose>
   </jsp:body>
</r:report>
