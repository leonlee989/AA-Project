<%-- 
    Document   : index
    Created on : Aug 30, 2012, 8:53:23 AM
    Author     : the saboteur
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login</title>
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
    
    <BODY OnLoad="document.myform.id.focus();">
        <FORM name="myform" METHOD=POST ACTION="processLogin.jsp" onsubmit="return checkForm(this)"> 
            <table border="1">
                <thead>
                    <tr>
                        <th>Login</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>UserID</td>
                        <td><INPUT TYPE=TEXT NAME=id SIZE=10></td>
                    </tr>
                    <tr>
                        <td>Password</td>
                        <td><INPUT TYPE=TEXT NAME=password SIZE=10 VALUE="password"></td>
                    </tr>
                    <tr>
                        <td></td>
                        <td><INPUT TYPE=SUBMIT value="Login"></td>
                    </tr>
                </tbody>
            </table>

        </FORM>

    </BODY>
</html>
