<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="/jsp/rdp/commons/tag_libs.jsp" %>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8">
<%
	String strFlag = request.getParameter("flag");
    String strReturn ="";
    String strfq = request.getParameter("fq");
    String strTableName = request.getParameter("tableName");

    if (strfq == null)
    {
    	strfq = "";
    }
    if (strTableName == null)
    {
        strTableName = "";
    }
    
    if ("query".equals(strFlag))
    {

        strReturn = com.rongzer.rdp.hbase.service.HBaseTestService.query(strTableName,strfq);
    }
    
%>
<script>
    function submitForm()
    {
        document.all.form1.submit();
    }
    
    function query()
    {
       document.all.flag.value="query";
       submitForm();
    }
    
  </script>
</head>

<body>
<form id="form1" name="form1" action="testHBase.jsp" method="post" width="100%">
<input type="hidden" name="flag" id="flag">
   表名：<input id="tableName" name="tableName" type="text" value='<%=strTableName%>' style="width:60%"><br><br>
    过滤：<input id="fq" name="fq" type="text" value='<%=strfq%>' style="width:60%">(key=value;key1=value1;...)<br><br>
	<input type="button" value="搜索" onclick="javascript:query();"> 
	<br>      
</form>
<%=strReturn%>
</body>
</html> 