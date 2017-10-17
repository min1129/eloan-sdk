<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>  
    
<%@ include file="/jsp/rdp/commons/tag_libs.jsp" %>
<% 
    List<String> lisCert = com.rongzer.utils.RBCUtils.lisCert();
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8">
<title><%=com.rongzer.rdp.common.service.RDPUtil.getSysConfig("rdp.system.title")%></title>
<link href="${ctx}/resource/css/system/${skinid}/skin/style.css" rel="stylesheet" type="text/css" id="skin"/>
<link href="${ctx}/webresources/skin/vista/artDialog/artDialog.css" rel="stylesheet" type="text/css" id="skin"/>
<script type="text/javascript" src="${ctx}/resource/libs/js/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="${ctx}/resource/libs/js/login.js"></script>
<script type="text/javascript" src="${ctx}/webresources/component/artDialog/artDialog.js"></script>
<script type="text/javascript" src="${ctx}/webresources/component/artDialog/jquery.artDialog.js"></script>
<script type="text/javascript" src="${ctx}/webresources/component/artDialog/plugins/iframeTools.js"></script>
<!--居中显示start-->
<script type="text/javascript" src="${ctx}/resource/libs/js/method/center-plugin.js"></script>
<!--居中显示end-->
<style>
/*提示信息*/	
#cursorMessageDiv {
	position: absolute;
	z-index: 99999;
	border: solid 1px #cc9933;
	background: #ffffcc;
	padding: 2px;
	margin: 0px;
	display: none;
	line-height:150%;
}
/*提示信息*/
</style>
</head>
<body class="login_body" onload="showLogin();">

	<div class="login_top">
		<div class="content">
		</div>
	</div>
	<div class="login_center">
		<div class="content">
			<div class="left"></div>
			<div class="center">
				<div class="top"></div>
				<div class="middle">
					<form id="loginForm" action="${ctx}/login.htm" class="login_form" method="post">
						<table>
                            <tr>
                                <td class="td1">证&nbsp;&nbsp;&nbsp;书</td>
                                <td class="td2">
                                    <select id="certList" name="certList" style="width:220px;font-size:16px;color:#202020;height:29px;line-height:29px;margin:0px;border:1px #ccc solid;padding:0px;">
                                        <% for (int i=0;i<lisCert.size();i++){%>
                                        <option value="<%=lisCert.get(i)%>"><%=lisCert.get(i)%></option>
                                        <%}%>
                                    </select>
                                </td>
                            </tr>
							<tr>
								<td class="td1">用户名</td>
								<td class="td2"><input type="text"  id="username" name="username" class="input_user" /></td>
                            </tr>
							<tr>
								<td class="td1">密&nbsp;&nbsp;&nbsp;码</td>
								<td class="td2"><input type="password" name="password" id="password" class="input_pwd" autocomplete="off"/></td>
							</tr>
                            <tr>
                                <td></td>
                                <td class="td2" colspan="2"><a class="login_btn" onclick="login()"></a></td>
                            </tr>
							<tr>
								<td></td>
								<td class="td2"><span class="login_info" style="display:none;">用户名或密码不正确</span></td>								
							</tr>
						</table>
						<div class="clear"></div>
					</form>
				</div>
				<!-- <div class="bottom"></div> -->
					
			</div>
			<div class="right"></div>
			<div class="clear"></div>
		</div>
	</div>
<script>
	$(function(){
		//居中
		 $('.login_main').center();
		 document.getElementById("username").focus();
		 $("#username").keydown(function(event){
		 	if(event.keyCode==13){ 
				login();
			}
		 });
		 $("#password").keydown(function(event){
		 	if(event.keyCode==13){
				login();
			}
		 });
		 
	})

	//登录
	function login() {
		var errorMsg = "";
		var loginName = document.getElementById("username");
		var password = document.getElementById("password");
		if(!loginName.value){
			errorMsg += "&nbsp;&nbsp;用户名不能为空!";
		}
		if(!password.value){
			errorMsg += "&nbsp;&nbsp;密码不能为空!";
		}

		if(errorMsg != ""){
			$(".login_info").html(errorMsg);
			$(".login_info").show();
		}
		else{
			$(".login_info").show();
			$(".login_info").html("&nbsp;&nbsp;正在登录中...");
			//登录处理
			loginUtil(loginName.value,password.value,false);
		}
	}
	function loginUtil(loginName,password,obj){
		$.post("${ctx}/login.htm", 
			  {"username":loginName,"password":password,"relogin":obj},
			  function(result){
				  result = $.parseJSON(eval('('+result+')'));
				  if(result == null){
					  $(".login_info").html("&nbsp;&nbsp;登录失败！");
					  return false;
				  }
				  if(result.status=="true"||result.status==true){
				        //登录成功，设置证书
				       $.ajax({
	                               url: "rbc/setCurrentCert.htm",
	                               type:"POST",
	                               dataType:"text",
	                               data:{"certName":document.getElementById("certList").value},
	                               success: function(responsedata){      
	                                    $(".login_info").html("&nbsp;&nbsp;登录成功，正在转到主页...");
                                        window.location="${ctx}/"+result.url;                           
	                               }
	                           });
	                           

				  }
				  else{
				  	 if(result.code=="4"){
				  	 	art.dialog.confirm("当前用户已登录，是否重新登录？",function(){loginUtil(loginName,password,true);},function(){$(".login_info").html("");});
				  	 }else{
				  	 	var msg  = result.code == "1" ? "用户名或密码不能为空" : result.code == "2" ? "没有相关用户" : "用户名或密码不正确";
				  	 	$(".login_info").html("&nbsp;&nbsp;"+msg);
				  	 }
				     
				  }
				  
			  },"text");
	}
	function showLogin()
	{
	  if (top.location.href != self.location.href) {
	    top.location.href=self.location.href;
	  }
	}
</script>
</body>
</html>