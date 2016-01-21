<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ attribute name="resource" fragment="true"%>

<a:base_with_nav>
<jsp:attribute name="resource">
	<jsp:invoke fragment="resource"/>
</jsp:attribute>
<jsp:body>
	<a:navbar>
		<jsp:doBody/>
	</a:navbar>
</jsp:body>
</a:base_with_nav>


