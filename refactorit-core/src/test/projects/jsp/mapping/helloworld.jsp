<%@ page session="false"
    import="java.util.Date, org.apache.log4j.Category"
    extends="AbstractHelloWorldServlet"
 %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<%!
  private static final Category cat =
    Category.getInstance("servlet");
%>

<%! boolean isValidCharacter(char c) { return Character.isLetter(c); }%>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Hello, World!</title>
</head>
<%@ include file="helloworld.jsf"%>

Time is <%= new Date() %>
<% int t = new Integer(13).intValue();
   out.println(t + new Double(t));
%>
</body>
