<!-- BEING EXCEL LOADER -->

<!-- REMOTE DESKTOP: HIDDEN FIELDS -->
<%@ include file="/include/remote_desktop_fields.jsp" %>

<%
    String sExcelLoaderLabel = BMMessages.getLabel("manager_excelLoaderManager");
    String sExcelLoaderIconPath = BMMessages.getRelativeLabelIconPath("manager_excelLoaderManager");
	com.bluemartini.loader.excel.ExcelFileDynamicDomainListener.setupList();
	dnaFormData.setBoolean("excel_loader_init", true);
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
<TD HEIGHT="21" ALIGN="left" VALIGN="middle" COLSPAN="3"><FONT FACE="arial,sans-serif,helvetica" CLASS="title"><B>Select your spreadsheet, the file type and click <I>Import</I></B></FONT></TD>
</TR>
<TR>
<TD NOWRAP HEIGHT="21" ALIGN="left" VALIGN="top" COLSPAN="2"><INPUT TYPE="file" NAME="uploadFile"></TD>
</TR>
<TR>
<TD NOWRAP HEIGHT="21" ALIGN="left" VALIGN="top" COLSPAN="3"><SELECT NAME="fileType" DATAELEMENT="fileType"></SELECT></TD>
</TR>
<TR>
<TD NOWRAP HEIGHT="21" ALIGN="left" VALIGN="top" COLSPAN="3"><IMG ID="inprogress" SRC="images/inprogress.gif" STYLE="visibility:hidden"></TD>
</TR>
</TABLE>

<BR>
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
<TR>
<TD VALIGN="top" ALIGN="left">
<IMG SRC="images/spacer.gif" WIDTH="5" HEIGHT="5"><BR><INPUT TYPE="submit" NAME="Submit" VALUE="Import" onclick="document.getElementById('inprogress').style.visibility='visible'">
</TD>
</TR>
<!-- End List -->
</TABLE>
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