<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="d" uri="/WEB-INF/document.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="ctx" type="org.unidal.cat.core.document.page.Context" scope="request" />
<jsp:useBean id="payload" type="org.unidal.cat.core.document.page.Payload" scope="request" />
<jsp:useBean id="model" type="org.unidal.cat.core.document.page.Model" scope="request" />

<d:document>

<c:set var="doc" value="${payload.document}"/>
<c:choose>
   <c:when test="${doc.tabbed}">
      <div class="tab-content">
         <div class="tabbable">
            <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" style="height: 50px;">
              <c:forEach var="f" items="${doc.features}" varStatus="status">
                 <li class="${status.index eq 0 ? 'active' : ''}">
                    <a href="#${doc.id}_${f.id}" data-toggle="tab"><strong>${f.title}</strong></a>
                 </li>
              </c:forEach>
            </ul>
         </div>
         <div class="tab-content">
            <c:forEach var="f" items="${doc.features}" varStatus="status">
              <div class="tab-pane ${status.index eq 0 ? 'active' : ''}" id="${doc.id}_${f.id}"><jsp:include page="${f.url}" flush="true"/></div>
            </c:forEach>
         </div>
      </div>
   </c:when>
   <c:otherwise>
      <div style="padding:20px"> 
         <c:forEach var="f" items="${payload.document.features}">
            <!-- ${f.title} -->
            <a name="${f.id}"></a>
            <div><jsp:include page="${f.url}" flush="true"/></div>
         </c:forEach>
      </div>
   </c:otherwise>
</c:choose>

</d:document>
