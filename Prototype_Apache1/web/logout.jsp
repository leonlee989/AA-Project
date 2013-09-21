<%-- 
    Document   : logout
    Created on : Aug 30, 2012, 11:26:55 AM
    Author     : the saboteur
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<%
session.setAttribute("userId", null);
session.setAttribute("authenticatedUser", null);
%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Logout</title>
    </head>
    <body>
        You have successfully logged out. <br/>
        Click <a href="login.jsp">here</a> to log in again.
    </body>
</html>
