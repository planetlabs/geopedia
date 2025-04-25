<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@include file="seo.jsp" %>
<!--[if IE 9]>
<html xmlns="http://www.w3.org/1999/xhtml" id="IE9">
<![endif]-->
<!--[if lte IE 8]>
<html xmlns="http://www.w3.org/1999/xhtml" id="IE">
<![endif]-->
<!--[if !IE]> -->
<html xmlns="http://www.w3.org/1999/xhtml" id="noIE">
<!-- <![endif]-->
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <meta name="gwt:property" content="locale=<%=locale%>">
    <meta name="description" id="opis" content="<%=description%>">
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <link href='http://fonts.googleapis.com/css?family=Lilita+One' rel='stylesheet' type='text/css'>
	<title><%=title%></title>
    <script type="text/javascript" language="javascript" src="@GWT.main.modulename@/@GWT.main.modulename@.nocache.js"></script>
    <script type="text/javascript" src="externalJS/tiny_mce/tiny_mce.js"></script>
    <script type="text/javascript" src="internalJS/esprima.js"></script>
    <script type="text/javascript" src="internalJS/uglifyjs.1.2.5.js"></script>
    <script type="text/javascript" src="internalJS/utility.js"></script>
    
    <link rel="shortcut icon" href="favicon.ico" />
  </head>

  <body>
	<div id="params" params="<%=initialParams%>"></div>
	@GWT.googlemaps.script@
	
    <!-- OPTIONAL: include this if you want history support -->
    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>
    
    <noscript>
    <%=body%>
    </noscript>
    <div id="ieCanvasHolder"></div>
   	<div id="mainLoading"></div>
  </body>
</html>
