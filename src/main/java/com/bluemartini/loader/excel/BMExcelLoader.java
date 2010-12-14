package com.bluemartini.loader.excel;

import com.bluemartini.dna.BMException;
import com.bluemartini.dna.BMLog;
import com.bluemartini.dna.CommandLine;
import com.bluemartini.dna.DNAFile;
import com.bluemartini.dna.DNAList;
import com.bluemartini.dna.DNAListArray;
import com.bluemartini.loader.excel.record.ExcelProcessingException;
import com.bluemartini.util.MainApp;

public class BMExcelLoader extends MainApp {

	private static String FILE = "file";
	private static String TYPE = "type";
	
    /** Need database */
    public boolean isDatabaseInitRequired() { return true; }


    /** Need login */
    public boolean isLoginRequired() { return true; }
    
	public BMExcelLoader()
	{
		super("BMExcelLoader","core/config/bmexcelloader.appconfig.dna");
	}
	
    /** Main application entry point */
    public static void main(String[] args) {
        (new BMExcelLoader()).mainImpl(args);
    }
    
    /** Set up applications options */
    protected void setupOptions() {
        setEnvironmentOption(true, true);
        
        CommandLine.addStringOption(FILE);
        CommandLine.setDescription(FILE, "Excel file to upload");
        CommandLine.setRequired(FILE);
        
        CommandLine.addStringOption(TYPE);
        CommandLine.setDescription(TYPE, "File type");
        CommandLine.setRequired(TYPE);
        
        super.setupOptions();
    }
    
    /** Application method */
    protected void run() throws BMException {
    	DNAFile file = new DNAFile(dnaOpt_.getString(FILE));
    	String type = dnaOpt_.getString(TYPE);
    	DNAList dnaResponse = importFile(file, type);
    	BMLog.log(BMLog.COMPONENT_SYSTEM, 0, dnaResponse.toString());
    }
    
	private DNAList importFile(DNAFile file, String fileType) throws BMException
	{
		DNAList dnaResponse = new DNAList();
		DNAListArray successfulLines = null;
		DNAListArray errorLines = null;
		ExcelImportManager excelImportManager = null;
		try {
			excelImportManager = new ExcelImportManager(fileType, file.getAbsolutePath());
			excelImportManager.open();
	    	do
	    	{
		    	excelImportManager.processNextBatch();		    	
		    	int percentage = excelImportManager.getPercentage();
	    	} while (excelImportManager.isProcessed());
			
			successfulLines = excelImportManager.getSuccessfulLines();
			dnaResponse.setListArray("successful_lines", successfulLines);
			errorLines = excelImportManager.getErrorLines();
			dnaResponse.setListArray("error_lines", errorLines);
			dnaResponse.setInteger("return", 0);
		} catch (ExcelProcessingException epe) {
			dnaResponse.setInteger("return", -1);
			dnaResponse.setString("msg", epe.getMessage());
		} finally {
			if (excelImportManager!=null)
				excelImportManager.close();
		}
		
		return dnaResponse;
	}    
}