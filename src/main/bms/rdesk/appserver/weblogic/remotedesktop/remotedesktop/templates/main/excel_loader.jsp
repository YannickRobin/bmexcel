<HTML>
<%
// Copyright (C) 2000 Blue Martini Software, Inc.
// All right reserved.

%>
<HEAD>

<%@ include file="/include/base.jsp" %>
<%@ include file="/include/active_item_setup.jsp" %>

<TITLE><%= activeManagerPageTitle %></TITLE>

<%@ include file="/include/stylesheet.jsp" %>

<%
dnaFormData.setString("REMOTEDESKTOP_HELP_TOPIC_ID", "/excel_loader");
%>

</HEAD>
<BODY TEXT="#000000" BGCOLOR="#336699" LINK="#000099" VLINK="#000099" MARGINHEIGHT="10" MARGINWIDTH="10">

<FORM METHOD="POST" NAME="upload_excel_file_wait" ENCTYPE="multipart/form-data" SECURE="<%= remoteDesktop.isSecure() %>">

<%@ include file="/include/logobar.jsp" %>

<!-- tabs -->
<%@ include file="/include/manager_tabs.jsp" %>

<%@ include file="/include/excel_loader.jsp" %>

</FORM>

</BODY>
</HTML>
