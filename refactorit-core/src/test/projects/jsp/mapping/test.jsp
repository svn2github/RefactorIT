<%@ page session="false" import="com.myco.Company, com.MyClass"%>
<%
// title of the page
  MyClass tmp = new MyClass("Hello, World!");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title><%= title %></title>
</head>

<body bgcolor=white>

<jsp:useBean
  id="customer"
  class="com.myco.Customer"
  type="com.myco.Company"
  >
    <jsp:setProperty name="customer" property="name" value="Sun"/>
</jsp:useBean>

<h1><%= title %></h1>

<%@ taglib uri="http://www.aqris.com/tags" prefix="aqris"%>

<%
if (isShown) {
  tmp.test(a);
} else {
  tmp.test(a, b);
}
%>

<%= customer.getName() %>

<aqris:button name="Submit">
  <a href="">
  <%= title %>
  </a>
</aqris:button>



</body>
