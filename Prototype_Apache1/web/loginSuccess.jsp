<%-- 
    Document   : loginSuccess
    Created on : Aug 30, 2012, 11:11:08 AM
    Author     : the saboteur
--%>

        <%
        // only users can see this page
        if (session.getAttribute("authenticatedUser")==null){
             %> <jsp:forward page = "login.jsp" /> <%
        }
        String userId = (String) session.getAttribute("userId");
        %>

<html>
  <head>
    <title></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  </head>
  <body>
    Your ID is: <%=userId%><br/>
    You have successfully logged in<br/>
    Functions available:<br/>
    <ul>
        <li><a href="buy.jsp">Buy</a></li>
        <li><a href="sell.jsp">Sell</a></li>
        <li><a href="logout.jsp">Log out</a></li>
    </ul>

  </body>
</html>
