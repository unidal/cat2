<%@ page contentType="text/html; charset=utf-8" isELIgnored="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="/WEB-INF/config.tld"%>
<jsp:useBean id="ctx" type="org.unidal.cat.core.alert.page.update.Context" scope="request"/>
<jsp:useBean id="payload" type="org.unidal.cat.core.alert.page.update.Payload" scope="request"/>
<jsp:useBean id="model" type="org.unidal.cat.core.alert.page.update.Model" scope="request"/>

<c:config>
   <br>
   
   <div class="error">
   <w:errors>
      <w:error code="config.update.error">Error while updating alert config(\${name})! Message: <xmp>\${exception}</xmp></w:error> 
   </w:errors>
   </div>
   
   <form method="POST">
      <input type="hidden" name="op" value="edit">
      <div>Alert(${payload.name}) Configuration(in XML):</div>
      <textarea name="content" style="height: 480px; width: 1024px">${model.content}</textarea>
      <br><input type="submit" class="btn btn-primary" name="update" value="Update">
      &nbsp;&nbsp;&nbsp;&nbsp;<a href="?op=view" class="btn btn-default" role="button">Back</a>
   </form>

</c:config>