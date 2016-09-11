<%@ page contentType="text/html; charset=utf-8" isELIgnored="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="r" uri="/WEB-INF/report.tld"%>
<jsp:useBean id="ctx" type="org.unidal.cat.core.message.page.home.Context" scope="request"/>
<jsp:useBean id="payload" type="org.unidal.cat.core.message.page.home.Payload" scope="request"/>
<jsp:useBean id="model" type="org.unidal.cat.core.message.page.home.Model" scope="request"/>

<r:report>
<jsp:attribute name="navbar">false</jsp:attribute>
<jsp:attribute name="resource">
    <script src="${model.webapp}/js/message.js"></script>
</jsp:attribute>

<jsp:body>
HELLO MESSAGE
</jsp:body>

</r:report>