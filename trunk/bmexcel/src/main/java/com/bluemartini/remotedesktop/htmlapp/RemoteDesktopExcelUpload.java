package com.bluemartini.remotedesktop.htmlapp;

import java.io.IOException;

import com.bluemartini.dna.BMException;
import com.bluemartini.dna.DNAFile;
import com.bluemartini.dna.DNAList;
import com.bluemartini.dna.DNAListArray;
import com.bluemartini.html.HTMLContext;
import com.bluemartini.html.HTMLResponse;
import com.bluemartini.http.BMServletOutputStream;
import com.bluemartini.remotedesktop.htmlapp.RemoteDesktop;
import com.bluemartini.server.BusinessAction;
import com.bluemartini.loader.excel.ExcelImportManager;
import com.bluemartini.loader.excel.record.ExcelProcessingException;

/**
 *
 * Business action for importing the Excel file
 * @author Yannick Robin
 *
 */
public class RemoteDesktopExcelUpload extends BusinessAction {
	
	private String baseURL = "";
	private BMServletOutputStream out;
	
	public DNAList execute(DNAList dnaIn) throws BMException {
		DNAList dnaFormData = dnaIn.getList("formData");
		HTMLResponse htmlResponse = new HTMLResponse();
		HTMLContext htmlContext = HTMLContext.getContext();
		out = htmlContext.getOutputStream();
				
		RemoteDesktop remotedesktop = RemoteDesktop.getRemoteDesktop();
		baseURL = remotedesktop.getBaseURL();
		
		htmlResponse = importFile(dnaFormData, htmlResponse);
		return htmlResponse;
	}
	
	/**
	 * Retrieve the uploaded file and file type
	 * Use ExcelImportManager to parse and the process the file
	 * @param dnaFormData
	 * @param htmlResponse
	 * @return Successful lines and error lines
	 */
	private HTMLResponse importFile(DNAList dnaFormData, HTMLResponse htmlResponse) throws BMException
	{
		if (!dnaFormData.getBoolean("excel_loader_init", true))
		{
			htmlResponse.setInteger("return", -1);
			htmlResponse.setString("msg", "This file has already been imported");
			return htmlResponse;
		}
		
		DNAFile file = dnaFormData.removeFile("uploadFile");
		if (file == null)
		{
			htmlResponse.setInteger("return", -1);
			htmlResponse.setString("msg", "File is missing");
			return htmlResponse;
		}	
			
		String fileType  = dnaFormData.getBusinessObject("GENERIC").removeString("fileType"); 
		DNAListArray successfulLines = null;
		DNAListArray errorLines = null;
		ExcelImportManager excelImportManager = null;
		try {
			excelImportManager = new ExcelImportManager(fileType, file.getAbsolutePath());
			excelImportManager.open();
			//writeBeginProgressBar();
	    	do
	    	{
		    	excelImportManager.processNextBatch();		    	
		    	int percentage = excelImportManager.getPercentage();
		    	//setPercentageProgressBar(percentage);		    		    	
	    	} while (excelImportManager.isProcessed());
			
			successfulLines = excelImportManager.getSuccessfulLines();
			htmlResponse.setListArray("successful_lines", successfulLines);
			errorLines = excelImportManager.getErrorLines();
			htmlResponse.setListArray("error_lines", errorLines);
			htmlResponse.setInteger("return", 0);
		} catch (ExcelProcessingException epe) {
			htmlResponse.setInteger("return", -1);
			htmlResponse.setString("msg", epe.getMessage());
		} finally {
			if (excelImportManager!=null)
				excelImportManager.close();
			htmlResponse.removeFile("uploadFile");
			htmlResponse.removeString("fileType");
		}
		
		return htmlResponse;
	}
    
	private void writeBeginProgressBar() throws BMException
	{
		try {	
			out.write("<html>\n");
			out.write("<head>\n");
			out.write("	<title>Excel Import Progress Bar</title>\n");
			out.write("	<meta http-equiv=\"content-type\" content=\"text/html; charset=iso-8859-1\" />\n");
			out.write("	<link rel=\"stylesheet\" href=\"<%=strMarketingStyle%>\" type=\"text/css\">\n");
			out.write("	<!-- jsProgressBarHandler prerequisites : prototype.js -->\n");
			out.write("	<script type=\"text/javascript\" src=\"" + baseURL + "js/progress_bar/prototype.js\"></script>\n");
			out.write("	<!-- jsProgressBarHandler core -->\n");
			out.write("	<script type=\"text/javascript\" src=\"" + baseURL + "js/progress_bar/jsProgressBarHandler.js\"></script>\n");
			out.write("\n");
			out.write("</head>\n");
			out.write("\n");
			out.write("<body bgcolor=\"#FFFFFF\" text=\"#000000\" leftmargin=\"20\" topmargin=\"20\">\n");
			out.write("\n");
			out.write("<div class=\"Excel_Upload_Title\">Excel Import</div>\n");
			out.write("\n");
			out.write("	<div style=\"width:800px;margin : 0 auto; text-align:left;\" >\n");
			out.write("\n");
			out.write("			<span style=\"color:#006600;font-weight:bold;\">Loading...</span> <br/>\n");
			out.write("			<span id=\"progress_bar\">[ Loading Progress Bar ]</span>\n");
			out.write("			<br /><br />\n");
			out.write("\n");
			out.write("			<script type=\"text/javascript\">\n");
			out.write("\n");
			out.write("			// Default Options\n");
			out.write("			var defaultOptions = {\n");
			out.write("				animate		: true,											     // Animate the progress? - default: true\n");
			out.write("				showText	: true,											     // show text with percentage in next to the progressbar? - default : true\n");
			out.write("				width		: 120,											     // Width of the progressbar - don't forget to adjust your image too!!!\n");
			out.write("				boxImage	: '" + baseURL + "media/images/progress_bar/percentImage.png',		 // boxImage : image around the progress bar\n");
			out.write("				barImage	: '" + baseURL + "media/images/progress_bar/percentImage_back.png', // Image to use in the progressbar. Can be an array of images too.\n");
			out.write("				height		: 12											     // Height of the progressbar - don't forget to adjust your image too!!!\n");
			out.write("			}\n");
			out.write("\n");
			out.write("			manualPB = new JS_BRAMUS.jsProgressBar(\n");
			out.write("								$('progress_bar'),\n");
			out.write("								0,\n");
			out.write("								{\n");
			out.write("									barImage	: Array(\n");
			out.write("										'" + baseURL + "media/images/progress_bar/percentImage_back4.png',\n");
			out.write("										'" + baseURL + "media/images/progress_bar/percentImage_back3.png',\n");
			out.write("										'" + baseURL + "media/images/progress_bar/percentImage_back2.png',\n");
			out.write("										'" + baseURL + "media/images/progress_bar/percentImage_back1.png'\n");
			out.write("									)\n");
			out.write("								}\n");
			out.write("							);\n");
			out.write("			</script>\n");
			out.write("\n");
			out.flush();
		}catch(IOException e)
		{
			throw new BMException("Unable to write in the servlet stream", e, null);
		}
	}
	
	private void setPercentageProgressBar(int percentage) throws BMException
	{
		try {
			out.write("			<script type=\"text/javascript\">\n");
			out.write("					manualPB.setPercentage('" + percentage + "');\n");
			out.write("\n");
			out.write("			</script>\n");
			out.flush();
		}catch(IOException e)
		{
			throw new BMException("Unable to write in the servlet stream", e, null);
		}
	}

}
