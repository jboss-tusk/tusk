<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="java.util.List" %>
<%@ page import="org.jboss.tusk.ui.SearchHelper" %>

<%
SearchHelper helper = new SearchHelper();
%>

<html>
<body>
	<h1>Tusk Message Population Form</h1>
	<form action="populate.html" method="post">
		<!--input type="text" name="key" />
		<br /-->
		<textarea rows="10" cols="40" name="message"></textarea>
		<br />
		<br />
		<input type="submit" />
		&nbsp;&nbsp;&nbsp;&nbsp;
		<input type="button" value="Clear" onclick="window.location.href='populate.html'" />
	</form>
	
	<c:if test="${key ne null}">
		<hr />
		<p>
			Saved message with key '<c:out value="${key}" />' in data store:
			<br />
			<pre>
<c:out value="${message}" />
			</pre>
		</p>
	</c:if>
	<hr />
	<br />
	<a href="search.html">Search Page</a>
</body>
</html>
