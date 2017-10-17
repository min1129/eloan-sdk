<%@ page language="Java" import="java.util.*,java.io.*" pageEncoding="UTF-8"%>
<%@ page import="com.rongzer.rbc.manage.biz.ChainCodeSource,com.rongzer.rdp.common.util.ZipUtil" %>
<%@ page import="java.util.zip.*,org.apache.commons.io.*" %>
<%@ page import="com.rongzer.utils.RBCUtils,com.rongzer.rbc.manage.util.*" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8 "/>
	<title>查看PDF文件</title>
</head>
<%
	out.clear();
	out = pageContext.pushBody();
	response.setContentType("application/pdf");

	try {
		String id = request.getParameter("id").toString();
		String version = request.getParameter("version").toString();
		String newFailPath =  request.getParameter("newFailPath").toString();
		String fileName = CommonUtil.unicode2String(newFailPath);
		String certName  = request.getParameter("certName");
		String[] certNames = certName.split(":");
        com.rongzer.blockchain.security.SecurityCert securityCert = RBCUtils.getSecurityCert(certNames[0], certNames[1]);
        
		//取zip流
		byte[] bSource = ChainCodeSource.getChainCodeSource(securityCert, id, version);
		//解压zip文件
		byte[] fileContent = null;
		try {
			InputStream is = new  ByteArrayInputStream(bSource);
			ZipInputStream zipInput = new ZipInputStream(is);
			for (ZipEntry entry; (entry = zipInput.getNextEntry()) != null;) {
				String entryName = entry.getName();
				if (entryName.equals(fileName)) {
					fileContent = IOUtils.toByteArray(zipInput);  
					break;
				}
			}
			zipInput.closeEntry();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		DataOutputStream temps = new DataOutputStream(response.getOutputStream());
		InputStream in = new ByteArrayInputStream(fileContent);
		byte[] b = new byte[2048];
		while ((in.read(b)) != -1) {
			temps.write(b);
			temps.flush();
		}
		in.close();
		temps.close();
	} catch (Exception e) {
		e.printStackTrace();
	}
%>

<body>
	<br>
</body>
</html>