package com.bluemartini.loader.excel;

import com.bluemartini.dna.BMContext;
import com.bluemartini.dna.BMException;
import com.bluemartini.dna.DNAList;
import com.bluemartini.dna.DNAListArray;
import com.bluemartini.remotedesktop.Content;


/**
 *
 * 
 * @author Yannick Robin
 *
 */
public class ExcelImportConfig {

	private static DNAList dnaExcelExportConfig_;
	private String fileType = null;
	
	public ExcelImportConfig(String fileType) throws BMException
	{
		this.fileType = fileType;
		dnaExcelExportConfig_ = getConfig();
	}
	
    public static DNAList getConfig() throws BMException
	{	
    	if (dnaExcelExportConfig_ == null || BMContext.getAppConfig().getInteger("configCheckSeconds", -1) != -1)
    	{
	    	//Load and merge config file
		    StringBuffer sb = new StringBuffer();
		    dnaExcelExportConfig_ = BMContext.loadAndMergeDNAsFromModules("import_excel.dna", sb);
		    if(dnaExcelExportConfig_.size() == 0)
		    {
		    	DNAList dnaParams_ = new DNAList();
		    	dnaParams_.setString("errorParam", "import_excel.dna");
		        throw new BMException("BMEXPORT_CONFIG_FILE_ERROR", dnaParams_);
		    }
    	}
    	return dnaExcelExportConfig_;
	}
    
    public int getMaxLines() throws BMException
    {
    	return dnaExcelExportConfig_.getInteger("max_lines", -1);
    }
    
    public int getMaxFailedLines() throws BMException
    {
    	return dnaExcelExportConfig_.getInteger("max_failed_lines", 100);
    }
    
    private DNAList getFileType() throws BMException
    {
    	DNAList dnaConfig = dnaExcelExportConfig_.getList("file_types").getList(fileType);
    	if (dnaConfig == null)
    		throw new BMException("The file type '" + fileType + "' is missing in import_excel.dna");
    	return dnaConfig;
    }
    
    public DNAListArray getProcess() throws BMException
    {
    	DNAListArray dnaConfig = getFileType().getListArray("process");
    	if (dnaConfig == null)
    		throw new BMException("process is not defined in import_excel.dna");
    	return dnaConfig;
    }

	public int getSheetNumber() throws BMException
	{
		return getFileType().getInteger("sheet_nb", 0);
	}
	
    public int getHeaderLineNumber() throws BMException
    {
    	return getFileType().getInteger("header_line_nb", 1);
    }
    
    public int getFirstDataLineNumber() throws BMException
    {
    	return getFileType().getInteger("first_data_line_nb", 2);
    }
    
    public Content getTemplate() throws BMException
    {
    	String template = getFileType().getString("template");
    	
    	if (template != null)
    	{
    		return Content.getContentByName(template);
    	}
    	else
    	{
    		return null;
    	}
    }
    
	public int getBatchSize() throws BMException {
		return dnaExcelExportConfig_.getInteger("batch_size", 100);
	}

	public boolean getImportLock() throws BMException {
		return dnaExcelExportConfig_.getBoolean("import_lock", false);
	}

	public void setEnabled(boolean bEnabled) throws BMException {
		dnaExcelExportConfig_.setBoolean("enabled", bEnabled);
	}
	
	public boolean getEnabled() throws BMException {			
		return dnaExcelExportConfig_.getBoolean("enabled", true);
	}
	
	public DNAList getUploadTime() throws BMException {
		
		DNAList dnaUploadTimeBegin = new DNAList();
		dnaUploadTimeBegin.setInteger("hh", 0);
		dnaUploadTimeBegin.setInteger("mm", 0);
		dnaUploadTimeBegin.setInteger("ss", 0);
		
		DNAList dnaUploadTimeEnd = new DNAList();
		dnaUploadTimeEnd.setInteger("hh", 23);
		dnaUploadTimeEnd.setInteger("mm", 59);
		dnaUploadTimeEnd.setInteger("ss", 59);
		
		DNAList dnaUploadTime = getFileType().getList("dnaUploadTime");
		
		if (dnaUploadTime == null)
		{
			dnaUploadTime = new DNAList();
		    dnaUploadTime.setList("begin", dnaUploadTimeBegin);
		    dnaUploadTime.setList("end", dnaUploadTimeEnd);
		}
		else
		{
			DNAList dnaTime = dnaUploadTime.getList("begin");
			if (dnaTime == null)
				dnaUploadTime.setList("begin", dnaUploadTimeBegin);
			else
			{
				if (dnaTime.getInteger("hh") == null || dnaTime.getInteger("hh").intValue() < 0 || dnaTime.getInteger("hh").intValue() > 24)
					dnaTime.setInteger("hh", dnaUploadTimeBegin.getInteger("hh"));
				if (dnaTime.getInteger("mm") == null || dnaTime.getInteger("mm").intValue() < 0 || dnaTime.getInteger("mm").intValue() > 60)
					dnaTime.setInteger("mm", dnaUploadTimeBegin.getInteger("mm"));
				if (dnaTime.getInteger("ss") == null || dnaTime.getInteger("ss").intValue() < 0 || dnaTime.getInteger("ss").intValue() > 60)
					dnaTime.setInteger("ss", dnaUploadTimeBegin.getInteger("ss"));
			}
			
			dnaTime = dnaUploadTime.getList("end");
			if ( dnaTime == null)
				dnaUploadTime.setList("end", dnaUploadTimeBegin);
			else
			{
				if (dnaTime.getInteger("hh") == null || dnaTime.getInteger("hh").intValue() < 0 || dnaTime.getInteger("hh").intValue() > 24)
					dnaTime.setInteger("hh", dnaUploadTimeEnd.getInteger("hh"));
				if (dnaTime.getInteger("mm") == null || dnaTime.getInteger("mm").intValue() < 0 || dnaTime.getInteger("mm").intValue() > 60)
					dnaTime.setInteger("mm", dnaUploadTimeEnd.getInteger("mm"));
				if (dnaTime.getInteger("ss") == null || dnaTime.getInteger("ss").intValue() < 0 || dnaTime.getInteger("ss").intValue() > 60)
					dnaTime.setInteger("ss", dnaUploadTimeEnd.getInteger("ss"));
			}
		}		
		return dnaUploadTime;
	}

	public void setUploadTime(DNAList dnaUploadTime) throws BMException {
		getFileType().setList("dnaUploadTime", dnaUploadTime);
	}

}
