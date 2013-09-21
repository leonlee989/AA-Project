<%--
    Document   : processBuy
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
        <title>Process Buy</title>
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
        String tempBidPrice = request.getParameter("bidprice").trim();
        session.setAttribute("bidprice", tempBidPrice);
        int bidPrice = Integer.parseInt(tempBidPrice);

        // submit the buy request
        Bid newBid = new Bid(stock, bidPrice, userId);
        boolean bidIsAccepted = exchangeBean.placeNewBidAndAttemptMatch(newBid);

        // forward to either buySuccess or buyFail depending on returned result
        if (bidIsAccepted){ %>
          <jsp:forward page = "buySuccess.jsp" />
        <%
        } else { %>
          <jsp:forward page = "buyFail.jsp" />
        <%
        }
        %>
    </body>
</html>
