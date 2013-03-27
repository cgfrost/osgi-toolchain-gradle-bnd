<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %><%-- 
--%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%-- 
--%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<title>Configuration properties sample</title>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
	<link rel="shortcut icon" href="<c:url value="/resources/images/favicon.ico"/>"	/>
	<link rel="stylesheet" href="	<c:url value="/resources/styles/main.css" 	/>"	type="text/css" />
	<link rel="stylesheet" href="	<c:url value="/resources/styles/local.css"	/>"	type="text/css" />
	<link rel="stylesheet" href="	<c:url value="/resources/styles/print.css"	/>"	type="text/css" media="print" />
</head>
<body class="main tundra">
	<div id="page">
		<div id="mini-header">
			<div id="mini-header-left"></div>
			<div id="mini-header-right"></div>
		</div> <!-- /mini-header -->

		<div id="primary-navigation">
			<div id="primary-left">
			</div>
			<img id="left-curve" src="<c:url value="/resources/images/menu-curve-left.png" />"/>
			<div id="primary-right">
				<ul>
					<li><a href="/admin" title="Admin Console">Admin Console</a></li>
					<li><a href="http://www.eclipse.org/virgo" title="Virgo">Virgo</a></li>
				</ul>
			</div>
			<img id="right-curve" src="<c:url value="/resources/images/menu-curve-right.png" />" />
		</div><!-- /primary-navigation -->

		<div id="container">
			<div id="content-no-nav">
				<h1 class="title">Configuration properties sample</h1>
				<p>
					The following properties have been found in 'org.eclipse.virgo.samples.configuration'.
				</p>
				<table id="properties" class="bordered-table">
					<tr>
						<th>Name</th>
						<th>Value</th>
					</tr>
					<c:choose>
						<c:when test="${empty properties}">
							<tr class="name-sublevel1-odd">
								<td id="property_null" colspan="2">No properties have been registered.</td>
							</tr>
						</c:when>
						<c:otherwise>
							<c:forEach var="property" items="${properties}" varStatus="loopStatus">
								<c:set var="rowStyle" value="odd" scope="page" />
								<c:if test="${(loopStatus.index % 2) eq 0}">
									<c:set var="rowStyle" value="even" scope="page" />
								</c:if>
								<tr class="sublevel1-${rowStyle}">
									<td id="property_key">${property.key}</td>
									<td id="property_value">${property.value}</td>
								</tr>
								<c:remove var="rowStyle" />
							</c:forEach>
						</c:otherwise>
					</c:choose>
				</table>
			</div><!-- /content -->
		</div><!-- /container -->

	<div id="footer-wrapper">
		<div id="footer-left">&copy; Copyright 2008, 2013 VMware Inc. Licensed under the Eclipse Public License v1.0.</div>
		<div id="footer-right"></div> 
	</div>
	</div> <!-- /page-->
</body>
</html>

