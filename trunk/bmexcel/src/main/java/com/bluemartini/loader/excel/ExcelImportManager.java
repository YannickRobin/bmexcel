package com.bluemartini.loader.excel;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.bluemartini.database.BMDatabaseContext;
import com.bluemartini.database.BMDatabaseManager;
import com.bluemartini.dna.BMException;
import com.bluemartini.dna.BMLog;
import com.bluemartini.dna.BMMessages;
import com.bluemartini.dna.DNAList;
import com.bluemartini.dna.DNAListArray;
import com.bluemartini.dna.XDNATransformer;
import com.bluemartini.loader.excel.parser.CSVParser;
import com.bluemartini.loader.excel.parser.ExcelParser;
import com.bluemartini.loader.excel.parser.Parser;
import com.bluemartini.loader.excel.parser.XMLParser;
import com.bluemartini.loader.excel.record.ExcelLineProcessingException;
import com.bluemartini.loader.excel.record.ExcelProcessingException;

/**
 * Class that manages the Excel Import.
 * It can be called from a web application or a command line application
 * 
 * The sequence flow is the following:
 * 
 * com.bluemartini.remotedesktop.htmlapp.RemoteDesktopExcelUpload.execute()
 * #or
 * com.bluemartini.tools.BMExcelLoader.process() [do not exist]
 *	-> ExcelImportManager.<init>
 *		-> ExcelImportConfig.<init>
 * -> ExcelImportManager.parseFile()
 *	-> ExcelParser.<init>
 * 	-> ExcelParser.getRowsSize()
 *	
 *	# for each line batch
 *	-> ExcelParser.getLines()
 *	-> ExcelImportManager.processLines()
 *		#for each lines
 *		-> ExcelLineProcessor.<init>
 *			-> ExcelImportConfig.getProcess();
 *		-> ExcelLineProcessor.parseLine()
 *			#for each processors
 *			-> ExcelRecordProcessor.<init>
 *			-> ExcelRecordProcessor.setRecordValue()
 *			-> ExcelRecordProcessor.setRecordConfig()
 *			-> ExcelRecordProcessor.process()
 *			
 *	-> ExcelParser.close()
 *	-> ExcelImportManager.getSuccessfulLines()
 *  -> ExcelImportManager.getErrorLines()
 * 
 * 
 * 
 * @author Yannick Robin
 *
 */
public class ExcelImportManager {
	
	private ExcelImportConfig importConfig = null;
	private File file = null;
	private DNAListArray errorLines = new DNAListArray();
	private DNAListArray successfulLines = new DNAListArray();
	private int failureLines = 0;
	private static boolean bImportLock = false;
	private int batchSize;
	private int beginLine;
	private int endLine;
	private int rowsSize;
	private Parser parser = null;
    
    public ExcelImportManager(String fileType, String filePath) throws BMException
	{
    	importConfig = new ExcelImportConfig(fileType);
    	File file = new File(filePath);
    	this.file = file;
    	
    	if (!importConfig.getEnabled())
    	{
        	String sMessage = BMMessages.getError("EXCEL_MANAGER_1", null);
    		throw new ExcelProcessingException(sMessage);
    	}
    	
    	checkUploadTime();
    	
		BMLog.log(BMLog.COMPONENT_SYSTEM, 4, "ExcelImportManager.parseFile(): Open Excel file " + file.getName());
	}
    
    public void open() throws BMException
    {
    	if (importConfig.getImportLock() && bImportLock == true)
    	{
        	String sMessage = BMMessages.getError("EXCEL_MANAGER_2", null);    		
    		throw new ExcelProcessingException(sMessage);
    	}
    	else
    		bImportLock = true;
    	
    	int headerLineNumber = importConfig.getHeaderLineNumber();
    	int sheetNumber = importConfig.getSheetNumber();
    	if (file.getName().endsWith(".xls"))
    		parser = new ExcelParser(file, headerLineNumber, sheetNumber);
    	else if (file.getName().endsWith(".xml"))
    		parser = new XMLParser(file);
    	else
    		parser = new CSVParser(file);
    	rowsSize = parser.getRowsSize();
    	BMLog.log(BMLog.COMPONENT_SYSTEM, 4, "ExcelImportManager.parseFile(): # rows = " + rowsSize);
    	
    	if (importConfig.getMaxLines() != -1 && rowsSize > importConfig.getMaxLines())
    	{
			DNAList dnaError = new DNAList();
			dnaError.setString("maxLines", String.valueOf(importConfig.getMaxLines()));
        	String sMessage = BMMessages.getError("EXCEL_MANAGER_3", dnaError);
    		throw new ExcelProcessingException(sMessage);
    	}
    	
    	BMLog.log(BMLog.COMPONENT_SYSTEM, 4, "ExcelImportManager.parseFile(): Parsing...");
    	
    	batchSize = importConfig.getBatchSize();
    	
    	if (batchSize == -1)
    		batchSize = rowsSize;	
    	
    	beginLine = importConfig.getFirstDataLineNumber()-1;
    	endLine = importConfig.getFirstDataLineNumber()-1;
	}
    
    public void close() throws BMException
    {
    	bImportLock = false;
    	BMLog.log(BMLog.COMPONENT_SYSTEM, 4, "ExcelImportManager.parseFile(): Closing...");
		if (parser != null)
			parser.close();
		BMLog.log(BMLog.COMPONENT_SYSTEM, 4, "ExcelImportManager.parseFile(): Closed...");    	
    }
    
    public void checkUploadTime() throws BMException
    {
    	DNAList dnaUploadTime = importConfig.getUploadTime();
    	DNAList dnaBeginTime = dnaUploadTime.getList("begin");
    	DNAList dnaEndTime = dnaUploadTime.getList("end");
    	
    	Calendar now = new GregorianCalendar();
    	int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);
    	
    	Date beginDate;
    	Date endDate;
    	Date nowDate = now.getTime();
    	
        try {
        	SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
			beginDate = (Date)formatter.parse(year + "." + month + "." + day + "." + dnaBeginTime.getInteger("hh") + "." + dnaBeginTime.getInteger("mm") + "." + dnaBeginTime.getInteger("ss"));
			endDate = (Date)formatter.parse(year + "." + month + "." + day + "." + dnaEndTime.getInteger("hh") + "." + dnaEndTime.getInteger("mm") + "." + dnaEndTime.getInteger("ss"));		
        } catch (ParseException e) {
			throw new BMException("Unable to parse the upload time date");
		}
        
        if (nowDate.before(beginDate) || nowDate.after(endDate))
        {        	
            SimpleDateFormat displayableFormatter = new SimpleDateFormat("HH:mm:ss z");        	
			DNAList dnaError = new DNAList();
			dnaError.setString("beginDate", displayableFormatter.format(beginDate));
			dnaError.setString("endDate", displayableFormatter.format(endDate));
        	String sMessage = BMMessages.getError("EXCEL_BMI_4", dnaError);
        	throw new ExcelProcessingException(sMessage);
        }        
    }
    
	public void processNextBatch() throws BMException {
    	beginLine = endLine;
    	endLine = beginLine + batchSize;
    	
    	if (endLine > rowsSize)
    		endLine = rowsSize;
				    	
    	processLines(beginLine, endLine);
	}
	
	public boolean isProcessed() throws BMException {
    	return endLine != rowsSize;
	}
	
	public int getPercentage() {
		int percentage = Math.round(endLine * 100f/rowsSize);
		return percentage;
	}

    public void processLines(int beginLine, int endLine) throws BMException
    {
    	DNAListArray importLines = parser.getLines(beginLine, endLine);
    	
    	BMLog.log(BMLog.COMPONENT_SYSTEM, 4, "ExcelImportManager.processLines(): Processing lines... " + importLines);
    	
    	for (int i=0; i<importLines.size(); i++)
    	{
    		DNAList dnaImportLineInput = importLines.elementAt(i);
    		BMLog.log(BMLog.COMPONENT_SYSTEM, 4, "ExcelImportManager.processLines(): Processing line ... " + dnaImportLineInput);
    		
    		ExcelLineProcessor lineProcessor = new ExcelLineProcessor(dnaImportLineInput, importConfig);
    		//@TODO set a defaut database in the config file and push this default db here
    		//in case ExcelImportManager is not instancied from RD.
    		BMDatabaseContext dbContext = BMDatabaseManager.getDBContext();
    		dbContext.beginDBTransaction();
    		try {
    		    
    			lineProcessor.parseLine();
    			dbContext.commitDBTransaction();

    			DNAList dnaSuccessfulLine = new DNAList();
    			dnaSuccessfulLine.setInteger("line", i + beginLine + 1);
    			dnaSuccessfulLine.setList("input_line", dnaImportLineInput);
    			successfulLines.addElement(dnaSuccessfulLine);
    		} catch (ExcelLineProcessingException excelException) {
    			
    			dbContext.rollbackDBTransaction();
    			
    			DNAList dnaErrorLine = new DNAList();
    			dnaErrorLine.setInteger("line", i + beginLine + 1);
    			dnaErrorLine.setList("input_line", dnaImportLineInput);
    			dnaErrorLine.setString("error_message", excelException.getMessage());
    			errorLines.addElement(dnaErrorLine);
    			failureLines++;
    			if (failureLines++ > importConfig.getMaxFailedLines())
    			{
    				DNAList dnaError = new DNAList();
    				dnaError.setString("maxLines", ""+importConfig.getMaxFailedLines());
    	        	String sMessage = BMMessages.getError("EXCEL_MANAGER_5", dnaError);
    	    		throw new ExcelProcessingException(sMessage);    				
    			}
    		} catch (ExcelProcessingException epe)
    		{
    			dbContext.rollbackDBTransaction();
    			throw epe;
    		}
    		catch (Throwable e)
    		{
    			dbContext.rollbackDBTransaction();
    			throw new BMException("Excel parsing low level error", e, null);
    		}
    	}
    	BMLog.log(BMLog.COMPONENT_SYSTEM, 4, "ExcelImportManager.processLines(): End processing lines... " + importLines);
    }
    
    public DNAListArray getSuccessfulLines()
    {
    	return successfulLines;
    }
    
    public DNAListArray getErrorLines()
    {
    	return errorLines;
    }

}
