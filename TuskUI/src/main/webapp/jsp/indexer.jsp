<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="java.util.List" %>
<%@ page import="org.jboss.tusk.ui.SearchHelper" %>

<%
SearchHelper helper = new SearchHelper();
%>

<html>
<body>
	<h1>Tusk Index Addition Test</h1>
	
	<c:if test="${messageKey ne null}">
		<hr />
		<p>
			Saved index with message key '<c:out value="${messageKey}" />' and indexes '<c:out value="${indexes}" />':
			<br />
			indexResponse='<c:out value="${indexResponse}" />'
		</p>
	</c:if>
	<hr />
	<br />
	<a href="search.html">Search Page</a>
	<!--&nbsp;&nbsp;&nbsp;&nbsp;
	<a href="populate.html">Populate Page</a-->
</body>
</html>
