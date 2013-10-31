<%--
    Document   : processSell
    Created on : Aug 30, 2012, 11:07:00 AM
    Author     : the saboteur
--%>
<%@ page import="aa.*" %>
<jsp:useBean id="exchangeBean" scope="application" class="aa.ExchangeBean" />
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Process Sell</title>
    </head>
    <body>
<%
        // only authenticated users can see this page
        if (session.getAttribute("authenticatedUser")==null){
             %> <jsp:forward page = "login.jsp" /> <%
        }
        String userId = (String) session.getAttribute("userId");
        String stock = request.getParameter("stock").trim();
        session.setAttribute("stock",stock);
        String tempAskPrice = request.getParameter("askprice").trim();
        session.setAttribute("askprice", tempAskPrice);
        int askPrice = Integer.parseInt(tempAskPrice);

        // submit the sell request
        Ask newAsk = new Ask(stock, askPrice, userId);
        exchangeBean.placeNewAskAndAttemptMatch(newAsk);
%>
        <jsp:forward page = "sellSuccess.jsp" />
    </body>
</html>
