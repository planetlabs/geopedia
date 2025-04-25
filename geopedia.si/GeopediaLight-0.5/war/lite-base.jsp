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
	<title><%=title%></title>
    <script type="text/javascript" language="javascript" src="SVNgeopedia/SVNgeopedia.nocache.js"></script>
    <script type="text/javascript" src="externalJS/tiny_mce/tiny_mce.js"></script>
    <link rel="shortcut icon" href="favicon.ico" />
    <link rel="stylesheet" href="css/style.css" />
  </head>

  <body>
	<div id="params" params="<%=initialParams%>"></div>
	<script src="http://maps.google.com/maps?gwt=1&amp;file=api&amp;v=2&amp;key=ABQIAAAATpDQLsXSGRF6EsH2FNtoahRe7-XIA79eYLCH_0RazhLwhZynDxQTJmd9rWRt6S9UOdZzqLhD6SAFEA"></script>   
	<script type="text/javascript">
		var _gaq = _gaq || [];
		_gaq.push([ '_setAccount', 'UA-1716984-1' ]);
		_gaq.push([ '_setDomainName', '.geopedia.si' ]);
		_gaq.push([ '_trackPageview' ]);
	
		(function() {
			var ga = document.createElement('script');
			ga.type = 'text/javascript';
			ga.async = true;
			ga.src = ('https:' == document.location.protocol ? 'https://ssl'
					: 'http://www')
					+ '.google-analytics.com/ga.js';
			var s = document.getElementsByTagName('script')[0];
			s.parentNode.insertBefore(ga, s);
		})();
	</script>
    <!-- OPTIONAL: include this if you want history support -->
    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>
    
    <noscript>
    <%=body%>
    </noscript>
   	<div id="ieCanvasHolder"></div>
   	<div id="mainLoading"></div>
  </body>
</html>
