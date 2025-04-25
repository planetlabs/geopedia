
<%
String initialParams = request.getParameter("params");
if(initialParams == null){
	initialParams = "T105";
}
String locale = request.getParameter("locale");
if(locale == null){
	locale = "si";
}
%>

<%@include file="lite-base.jsp" %>
