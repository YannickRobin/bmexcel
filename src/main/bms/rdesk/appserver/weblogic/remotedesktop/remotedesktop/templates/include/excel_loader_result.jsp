<!-- BEING EXCEL LOADER -->

<!-- REMOTE DESKTOP: HIDDEN FIELDS -->
<%@ include file="/include/remote_desktop_fields.jsp" %>

<%
    String sExcelLoaderLabel = BMMessages.getLabel("manager_excelLoaderManager");
    String sExcelLoaderIconPath = BMMessages.getRelativeLabelIconPath("manager_excelLoaderManager");
	dnaFormData.setBoolean("excel_loader_init", false);
	
	int iSuccessfulLines = 0;
	DNAListArray successfulLines = dnaFormData.removeListArray("successful_lines");
	if (successfulLines != null)
		iSuccessfulLines = successfulLines.size();
		
	int iErrorLines = 0;
	DNAListArray errorLines = dnaFormData.removeListArray("error_lines");
	if (errorLines != null)
		iErrorLines = errorLines.size();
	
	int iReturnCode = 0;
	Integer returnCode = dnaFormData.removeInteger("return");
	if (returnCode != null)
		iReturnCode = returnCode.intValue();
	String errorMessage = dnaFormData.removeString("msg");
%>


<!-- title, top form elements -->
<table WIDTH="100%" HEIGHT="32" BGCOLOR="#E6E6CC" CELLSPACING="0" 
CELLPADDING="0" BORDER="0">
<tr>
<td HEIGHT="10" COLSPAN="4"><img SRC="images/spacer.gif" WIDTH="10" HEIGHT="10"></td>
</tr>
<tr VALIGN="middle">
<td WIDTH="10"><img SRC="images/spacer.gif" WIDTH="10" HEIGHT="10"></td>
<td><img SRC="<%= sExcelLoaderIconPath %>" WIDTH="16" HEIGHT="16"></td>
<td WIDTH="100%">&nbsp;<SPAN CLASS="title"><b><%= sExcelLoaderLabel %></b></SPAN></td>
<td WIDTH="10"><img SRC="images/spacer.gif" WIDTH="10" HEIGHT="10"></td>
</tr>
<tr>
<td WIDTH="5" HEIGHT="5" COLSPAN="4"><img SRC="images/spacer.gif" WIDTH="5" HEIGHT="5"></td>
</tr>
<tr>
<td HEIGHT="1" BGCOLOR="#CCCC99" COLSPAN="4"><img SRC="images/spacer.gif" WIDTH="1" HEIGHT="1"></td>
</tr>
<tr>
<td HEIGHT="1" BGCOLOR="#999966" COLSPAN="4"><img SRC="images/spacer.gif" WIDTH="1" HEIGHT="1"></td>
</tr>
</table>
<!-- end title, top form elements -->

<TABLE WIDTH="100%" BGCOLOR="#CCCC99" CELLSPACING="0" CELLPADDING="10" BORDER="0">
<TR>
<TD>

<!-- begin content body -->

<TABLE WIDTH="100%" BGCOLOR="#FFFFFF" CELLSPACING="0" CELLPADDING="5" BORDER="0">
<TR BGCOLOR="#E6E6CC">
<TD HEIGHT="21" ALIGN="left" VALIGN="middle" COLSPAN="3"><FONT FACE="arial,sans-serif,helvetica" CLASS="title"><B>Excel import result</B></FONT></TD>
</TR>

<%
if (iReturnCode == -1)
{
%>
	<TR><TD>The Excel file processing failed because of the following error: <%=errorMessage%>.</TD></TR>
<%
}else{
%>
<TR><td><%=iSuccessfulLines%> line(s) imported successfully.</TR></TD>
<TR><td><%=iErrorLines%> line(s) failed.</TR></TD>
<%
}
%>
</TABLE>

<%
if (iErrorLines > 0)
{
%>
<BR>
<TABLE WIDTH="100%" BGCOLOR="#CCCC99" CELLSPACING="1" CELLPADDING="3" BORDER="0">

<!-- Begin header -->
<tr BGCOLOR="#E6E6CC" VALIGN="middle">
<td HEIGHT="21"><img SRC="images/spacer.gif" WIDTH="1" HEIGHT="1"></td>
<td WIDTH="25%" NOWRAP><span CLASS="text"><b>Line</b></span></td>
<td WIDTH="75%" NOWRAP><span CLASS="text"><b>Error message</b></span></td>
</tr>
<!-- End header -->

<%
	for (int i=0; i<errorLines.size();i++)
	{
		DNAList dnaErrorLine = errorLines.elementAt(i);
%>
  
    <!-- Begin List -->
	<tr BGCOLOR="#FFFFFF" VALIGN="middle">
	<td HEIGHT="21"><img SRC="<%=sExcelLoaderIconPath%>" WIDTH="16" HEIGHT="16" BORDER="0"></td>
	<td><span CLASS="text"><%=dnaErrorLine.getInteger("line")%></span></td>
	<td><span CLASS="text"><%=dnaErrorLine.getString("error_message")%></span></td>
	</tr>

<%
    }
%>
</TABLE>
<%
}
%>
<!-- end content body -->

</TD>
</TR>
</TABLE>
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
<TR>
<TD HEIGHT="1" BGCOLOR="#999966"><IMG SRC="images/spacer.gif" WIDTH="1" HEIGHT="1"></TD>
</TR>
<TR>
<TD HEIGHT="1" BGCOLOR="#003366"><IMG SRC="images/spacer.gif" WIDTH="1" HEIGHT="1"></TD>
</TR>
</TABLE>

<!-- END EXCEL WORKFLOW -->


</FORM>

</BODY>

</HTML>