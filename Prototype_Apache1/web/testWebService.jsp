<%-- 
    Document   : testWebService
    Created on : Oct 20, 2013, 2:03:54 PM
    Author     : Melvrick
--%>
<%@ page import="aa.*" %>
<jsp:useBean id="exchangeBean" scope="application" class="aa.ExchangeBean" />
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Test Web Service</title>
    </head>
    <body>
<%
        boolean status = exchangeBean.sendToBackOffice("my test string");
%>
    Status of sending to Back Office: <%=status%>
    </body>
</html>

