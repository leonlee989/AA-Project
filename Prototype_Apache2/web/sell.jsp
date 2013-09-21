<%--
    Document   : sell
    Created on : Aug 30, 2012, 10:53:20 AM
    Author     : the saboteur
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<%
        // only authenticated users can see this page
        if (session.getAttribute("authenticatedUser")==null){
             %> <jsp:forward page = "login.jsp" /> <%
        }
        String userId = (String) session.getAttribute("userId");
%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Buy</title>
        <script type="text/javascript">
            function checkForm(form) {
                var idCheck = checkId(form.elements['id']);
                return idCheck;
            }
            function checkId(input) {
                var check = input.value.length >= 1;
                input.style.borderColor = check ? 'black' : 'red';
                myform.id.focus();
                return check;
            }
        </script>
    </head>

    <BODY OnLoad="document.myform.bidprice.focus();">
        <FORM name="myform" METHOD=POST ACTION="processSell.jsp" onsubmit="return checkForm(this)">
            <table border="1">
                <thead>
                    <tr>
                        <th>Sell</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>UserID</td>
                        <td><%=userId%></td>
                    </tr>
                    <tr>
                        <td>Quantity</td>
                        <td>1000 (fixed)</td>
                    </tr>
                    <tr>
                        <td>Stock to sell</td>
                        <td>
                            <select name="stock">
                                <option value="smu" selected="selected">smu</option>
                                <option value="nus">nus</option>
                                <option value="ntu">ntu</option>
                            </select>
                        </td>

                    </tr>
                    <tr>
                        <td>Ask Price</td>
                        <td><INPUT TYPE=TEXT NAME=askprice SIZE="3" VALUE="10">(please enter a whole number between 1 and 100 only)</td>
                    </tr>
                    <tr>
                        <td></td>
                        <td><INPUT TYPE=SUBMIT value="Place sell order"></td>
                    </tr>
                </tbody>
            </table>

        </FORM>

    </BODY>
</html>

