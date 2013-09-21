<%--
    Document   : buySuccess
    Created on : Sep 3, 2012, 3:51:21 AM
    Author     : the saboteur
--%>



<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Buy Success</title>

<%
        // only authenticated users can see this page
        if (session.getAttribute("authenticatedUser")==null){
             %> <jsp:forward page = "login.jsp" /> <%
        }
        String userId = (String) session.getAttribute("userId");
        String stock = (String)session.getAttribute("stock");
        String bidPrice = (String)session.getAttribute("bidprice");
%>

  </head>

   <BODY>
        <FORM name="myform">
            <table border="1">
                <thead>
                    <tr>
                        <th>Acknowledge Buy Order Received</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>User ID</td>
                        <td><%=userId%></td>
                    </tr>
                    <tr>
                        <td>Quantity</td>
                        <td>1000</td>
                    </tr>
                    <tr>
                        <td>Stock to buy</td>
                        <td><%=stock%></td>

                    </tr>
                    <tr>
                        <td>Bid Price</td>
                        <td><%=bidPrice%></td>
                    </tr>

                </tbody>
            </table>
        </FORM>
    <hr/>
    Functions available:<br/>
    <ul>
        <li><a href="buy.jsp">Buy</a></li>
        <li><a href="sell.jsp">Sell</a></li>
        <li><a href="logout.jsp">Log out</a></li>
        <li><a href="current.jsp">Current stats</a></li>
        <li><a href="viewOrders.jsp">Unfulfilled orders</a></li>
    </ul>
    </BODY>
</html>
