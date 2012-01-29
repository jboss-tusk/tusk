<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.jboss.tusk.ui.SearchHelper" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>

<%
SearchHelper helper = new SearchHelper();
%>

<html>

<head>
<style type="text/css" title="currentStyle">
	@import "css/demo_page.css"; 
	@import "css/demo_table.css";
</style>

<script type="text/javascript" src="js/jquery-1.6.4.js"></script>
<script type="text/javascript" src="js/jquery.dataTables.js"></script>
<script type="text/javascript" charset="utf-8">
$(document).ready(function() {
    $('#resultTable').dataTable( {
        "bSort": false,
        "sPaginationType": "full_numbers",
        "sScrollX": "100%",
        "sScrollXInner": "110%",
        "bScrollCollapse": true
    } );
} );
</script>
</head>

<body>
<div style="float:right;"><img src="images/bcbsfl_logo.jpg" border="0" style="width: 150px; height: 150px;" /></div>
	<h1>Tusk Search Form</h1>
	<form action="search.html" method="post">
		<%= helper.getFieldOptions("field1", request) %>
		&nbsp;&nbsp;
		<input type="text" name="term1" value="<c:out value="${term1}" />" />
		<br />
		<%= helper.getFieldOptions("field2", request) %>
		&nbsp;&nbsp;
		<input type="text" name="term2" value="<c:out value="${term2}" />" />
		<br />
		<%= helper.getFieldOptions("field3", request) %>
		&nbsp;&nbsp;
		<input type="text" name="term3" value="<c:out value="${term3}" />" />
		<br />
		<br />
		<input type="radio" name="operator" value="and" <c:if test="${operator != 'or'}">checked="checked"</c:if> />And
		&nbsp;&nbsp;
		<input type="radio" name="operator" value="or" <c:if test="${operator == 'or'}">checked="checked"</c:if>/>Or
		<br />
		<br />
		<input type="submit" />
		&nbsp;&nbsp;&nbsp;&nbsp;
		<input type="button" value="Clear" onclick="window.location.href='search.html'" />
	</form>
	
	<c:if test="${fn:length(results) gt 0}">
		<hr />
		<p>
			Got <c:out value="${fn:length(results)}" /> Result(s)
			<br /><br />
			<!--table style="border:1px solid black;" class="display" border="1" id="resultTable"-->
			<table class="display" id="resultTable">
				<thead>
				<tr>
					<th>ID</th>
					<th>Message</th>
				</tr>
				</thead>
				<tbody>
				<%
				List<String> results = (List<String>)request.getAttribute("results");
				Map<String, String> messages = (Map<String, String>)request.getAttribute("messages");
				for (String msgId : results) {
					out.print("<tr><td>" + msgId + "</td><td><pre>" + StringEscapeUtils.escapeHtml(messages.get(msgId)) + "</pre></td></tr>");
				}
				%>
				</tbody>
			</table>
		</p>
	</c:if>
	<!--hr />
	<br />
	<a href="populate.html">Test Data Population Page</a-->
</body>
</html>
