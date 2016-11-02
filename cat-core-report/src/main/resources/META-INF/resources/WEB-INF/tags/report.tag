<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="r" uri="/WEB-INF/report.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ attribute name="title"%>
<%@ attribute name="navUrlPrefix"%>
<%@ attribute name="timestamp"%>
<%@ attribute name="subtitle" fragment="true" required="false"%>
<%@ attribute name="resource" fragment="true" required="false"%>

<%@ attribute name="head" required="false" rtexprvalue="true"%>
<%@ attribute name="menu" required="false" rtexprvalue="true"%>
<%@ attribute name="navbar" required="false" rtexprvalue="true"%>

<html lang="en">
<head>
   <title>CAT</title>
   <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
   <meta charset="utf-8">
   <meta name="description" content="CAT Districuted Realtime Monitoring System">
   <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
   <link rel="stylesheet" href="${model.webapp}/assets/css/bootstrap.min.css">
   <link rel="stylesheet" href="${model.webapp}/assets/css/font-awesome.min.css">
   <link rel="stylesheet" href="${model.webapp}/assets/css/jquery-ui.min.css">
   <link rel="stylesheet" href="${model.webapp}/assets/css/ace-fonts.css">
   <link rel="stylesheet" href="${model.webapp}/assets/css/ace.min.css" id="main-ace-style">
   <link rel="stylesheet" href="${model.webapp}/assets/css/ace-skins.min.css">
   <link rel="stylesheet" href="${model.webapp}/assets/css/ace-rtl.min.css">
   <link rel="stylesheet" href="${model.webapp}/css/body.css">
   <script src="${model.webapp}/js/jquery-1.7.1.js"></script>
   <script src="${model.webapp}/assets/js/ace-extra.min.js"></script>
   <script src="${model.webapp}/assets/js/bootstrap.min.js"></script>
   <script src="${model.webapp}/js/highcharts.js"></script>
   <script src="${model.webapp}/assets/js/jquery-ui.min.js"></script>
   <script src="${model.webapp}/assets/js/jquery.ui.touch-punch.min.js"></script>
   <script src="${model.webapp}/assets/js/ace-elements.min.js"></script>
   <script src="${model.webapp}/assets/js/ace.min.js"></script>

   <jsp:invoke fragment="resource"/>
</head>

<body class="no-skin">
   <c:choose>
      <c:when test="${param.fullscreen eq 'true'}">
         <div class="main-container" id="main-container">
            <div class="main-content" style="padding-top:2px;padding-left:2px;padding-right:8px;">
               <jsp:doBody/>
            </div>
         </div>
      </c:when>
      <c:otherwise>
         <c:if test="${head ne 'false'}">
            <r:report-header/>
         </c:if>
         
         <div class="main-container" id="main-container">
            <c:if test="${menu ne 'false'}">
               <r:report-menu/>
            </c:if>
               
            <div class="main-content" style="padding-top:2px;padding-left:2px;padding-right:8px;">
               <c:if test="${navbar ne 'false'}">
                  <r:report-navbar/>
               </c:if>

               <jsp:doBody/>
            </div>
         </div>
      </c:otherwise>
   </c:choose>
</body>

</html>
