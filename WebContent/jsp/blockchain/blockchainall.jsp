<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>  
<%@ page import="com.rongzer.rdp.common.util.*"%>  
<%@ page import="com.rongzer.rbc.manage.biz.*"%>  

<%
String txId = request.getParameter("txid");
String block = request.getParameter("block");
Map<String,Object> mapBlock = null;
if (txId == null ||txId.length()<1)
{
    int nBlock =  StringUtil.toInt(block);
    
    mapBlock = BlockView.getBlockInfo(nBlock);
}else
{
    mapBlock = BlockView.getBlockInfo(txId);
}

int nStart = (Integer)mapBlock.get("nStart");
int nEnd = (Integer)mapBlock.get("nEnd");
int nHeight = (Integer)mapBlock.get("nHeight");
int nBlock = (Integer)mapBlock.get("nBlock");
String blockInfo =(String)mapBlock.get("blockInfo");
String stateHash =(String)mapBlock.get("stateHash");
String previousBlockHash =(String)mapBlock.get("previousBlockHash");
String transactionId =(String)mapBlock.get("transactionId");
List<Map<String,String>> lisTran =(List<Map<String,String>>)mapBlock.get("data");
int nTranNum = 0;
if (lisTran != null)
{
    nTranNum = lisTran.size();
}
String preuuid =(String)mapBlock.get("preuuid");
String datahash =(String)mapBlock.get("datahash");
String time =(String)mapBlock.get("time");

 %>
     

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ include file="/jsp/rdp/commons/tag_libs.jsp" %>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>区块链查询</title>
<link href="${ctx}/resource/css/rbc.css" rel="stylesheet" type="text/css" />
</head>
<body>
	<div class="rbc">
		<div class="left">
<%if (nStart >1) {%>		
            <a class="btn" href="blockchainall.jsp?block=1">首页</a>
			<a class="btn page" href="blockchainall.jsp?block=<%=nStart-20%>">上一页</a>
<%}%>
<% for (int i=nStart;i<=nEnd;i++){%>
    <%if (i == nBlock) {%>            
            <a class="curr" href="blockchainall.jsp?block=<%=i%>"><%=i %><i></i></a>
	<%}else{%>
            <a href="blockchainall.jsp?block=<%=i%>"><%=i %></a>
	<%}%>
            
<%}%>

<%if (nEnd <nHeight) {%>        

			<a class="btn page" href="blockchainall.jsp?block=<%=nStart+20%>">下一页</a>
            <a class="btn end" href="blockchainall.jsp?block=<%=nHeight%>">尾页</a>
<% }%>
		</div>
		<div class="right">
			<div class="title" style="background:url();">
				<p>原始信息</p>
			</div>
			<pre class="content"><%=blockInfo%></pre>
			<div class="title">
				<p>区块信息</p>
			</div>
			<div class="info">
				<span>序号：<%=nBlock%></span>
				
				<span>时间：<%=time%></span>
				
				<span class="hash">State Hash：<%=stateHash%></span>
				
				<span class="hash">Previous Block Hash：<%=previousBlockHash%></span>
				
			</div>
			<div class="title">
				<p>交易列表(交易数：<%=nTranNum %>)<p>
			</div>
			<table class="cls-data-table" width="100%">
			<tr>
             <td class="cls-data-th-list" width="15%">execute time</th>
			 <td class="cls-data-th-list" width="15%">chaincode</th>
             <td class="cls-data-th-list" width="65%" align="left">args</th>
            </tr>
<%
for (Map<String,String> mapTran :lisTran)
{
String args = mapTran.get("args");
String shortargs = args;
if (args != null && args.length()>200)
{
    shortargs = args.substring(0,200)+"...";
}
%>            
            <tr>
              <td class="cls-data-td-list" nowrap><%=mapTran.get("trantime")%></td>
              <td class="cls-data-td-list"><%=mapTran.get("chaincodeID")%></td>
              <td  class="cls-data-td-list"><a title='<%=args%>'><%=shortargs%></a></td>
            </tr>
<%
}
%>	
			</table>
			 
		</div>
	</div>
</body>
</html>