<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="java.util.List" %>
<%@ page import="org.jboss.tusk.ui.PopulateHelper" %>

<%
PopulateHelper helper = new PopulateHelper();
%>

<html>

<script type="text/javascript" src="js/jquery-1.6.4.js"></script>

<script type="text/javascript">
var currPayloadType = '<%=PopulateHelper.PAYLOAD_TYPE_JSON %>';

//populate payload textarea on startup
$(document).ready(function() {
	$('#payloadJson').show();
	$('#payloadXml').hide();

	//change the value of the payload textarea when the payload type is changed
	$("input:radio[name=payloadType]").click(function() {
		currPayloadType = $(this).val();
		if (currPayloadType == '<%=PopulateHelper.PAYLOAD_TYPE_JSON %>') {
			$('#payloadJson').show();
			$('#payloadXml').hide();
		} else {
			$('#payloadJson').hide();
			$('#payloadXml').show();
		}
	});	
});

</script>

<body>
<div style="float:right;"><img src="images/jboss_logo.png" border="0" style="width: 180px; height: 100px;" />
<br /><br />
<a href="search.html">Go to Search Page</a>
</div>

	<h1>Tusk Data Population Form</h1>
	
	<c:if test="${numAdded ne null}">
		<p>
			<strong>Added <c:out value="${numAdded}" /> message<c:if test="${numAdded > 1}">s</c:if>.</strong>
		</p>
		<hr />
		<br />
	</c:if>
	
	<form action="populate.html" method="post">
		<input type="radio" name="payloadType" id="payloadType" value="<%=helper.PAYLOAD_TYPE_JSON %>" <c:if test="${payloadType != helper.PAYLOAD_TYPE_XML}">checked="checked"</c:if>/><%=helper.PAYLOAD_TYPE_JSON %>
		&nbsp;&nbsp;
		<input type="radio" name="payloadType" id="payloadType" value="<%=helper.PAYLOAD_TYPE_XML %>" <c:if test="${payloadType == helper.PAYLOAD_TYPE_XML}">checked="checked"</c:if> /><%=helper.PAYLOAD_TYPE_XML %>
		<br /><br />
		<input type="submit" name="addOne" value="<%= PopulateHelper.ADD_ONE_LABEL %>"/>
		<br /><br />
		or
		<br /><br />
		<input type="submit" name="addMany" value="<%= PopulateHelper.ADD_MANY_LABEL %>"/>
		&nbsp;
		<input type="text" name="howMany" value="10" />
		<br /><br />
		or
		<br /><br />
		<strong>Edit this Message and Add it:</strong>
		<br />
		<textarea rows="5" cols="100" name="payloadJson" id="payloadJson"><%= helper.getRandomJsonPayload() %></textarea>
		<textarea rows="20" cols="100" name="payloadXml" id="payloadXml"><%= helper.getRandomXmlPayload() %></textarea>
		<br />
		<input type="submit" name="addPayload" value="<%= PopulateHelper.ADD_PAYLOAD_LABEL %>" />
		<br /><br /><br />
		<input type="button" value="Start Over" onclick="window.location.href='populate.html'" />
	</form>
	
</body>
</html>
