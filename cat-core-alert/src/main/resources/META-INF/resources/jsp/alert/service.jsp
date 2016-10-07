<%@ page contentType="text/html; charset=utf-8" isELIgnored="false" trimDirectiveWhitespaces="true" %>
<jsp:useBean id="ctx" type="org.unidal.cat.core.alert.page.service.Context" scope="request"/>
<jsp:useBean id="payload" type="org.unidal.cat.core.alert.page.service.Payload" scope="request"/>
<jsp:useBean id="model" type="org.unidal.cat.core.alert.page.service.Model" scope="request"/>

<xmp>
${model.report}
</xmp>