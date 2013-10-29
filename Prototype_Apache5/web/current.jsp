<%--
    Document   : current
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
        <title>Show Current Stats</title>
    </head>


     <BODY>
            <table border="1">
                <thead>
                    <tr>
                        <th>Stats for SMU</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>Latest Price</td>
                        <td><%=exchangeBean.getLatestPrice("smu")%></td>
                    </tr>
                    <tr>
                        <td>Current Highest Bid</td>
                        <td><%=exchangeBean.getHighestBidPrice("smu")%></td>
                    </tr>
                    <tr>
                        <td>Current Lowest Ask</td>
                        <td><%=exchangeBean.getLowestAskPrice("smu")%></td>
                    </tr>
                </tbody>
            </table>

            <br/><br/>

            <table border="1">
                <thead>
                    <tr>
                        <th>Stats for NUS</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>Latest Price</td>
                        <td><%=exchangeBean.getLatestPrice("nus")%></td>
                    </tr>
                    <tr>
                        <td>Current Highest Bid</td>
                        <td><%=exchangeBean.getHighestBidPrice("nus")%></td>
                    </tr>
                    <tr>
                        <td>Current Lowest Ask</td>
                        <td><%=exchangeBean.getLowestAskPrice("nus")%></td>
                    </tr>
                </tbody>
            </table>

            <br/><br/>

            <table border="1">
                <thead>
                    <tr>
                        <th>Stats for NTU</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>Latest Price</td>
                        <td><%=exchangeBean.getLatestPrice("ntu")%></td>
                    </tr>
                    <tr>
                        <td>Current Highest Bid</td>
                        <td><%=exchangeBean.getHighestBidPrice("ntu")%></td>
                    </tr>
                    <tr>
                        <td>Current Lowest Ask</td>
                        <td><%=exchangeBean.getLowestAskPrice("ntu")%></td>
                    </tr>
                </tbody>
            </table>

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