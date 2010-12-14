/**
 * 
 */
package com.bluemartini.loader.excel;

import com.bluemartini.dna.BMException;
import com.bluemartini.dna.BMMessages;
import com.bluemartini.dna.DNABasePair;
import com.bluemartini.dna.DNAList;
import com.bluemartini.dna.DNAListArray;
import com.bluemartini.dna.DNAListIterator;
import com.bluemartini.loader.excel.record.ExcelProcessingException;
import com.bluemartini.loader.excel.record.ExcelRecordProcessor;

/**
 * Process the excel line with each processors defined in import_excel.dna 
 * 
 * @author Yannick Robin
 *
 */
public class ExcelLineProcessor {

	private DNAList dnaImportLine;
	private DNAListArray dnaProcess;
	
	public ExcelLineProcessor(DNAList dnaImportLine, ExcelImportConfig importConfig_) throws BMException {
		this.dnaImportLine = dnaImportLine;
		dnaProcess = importConfig_.getProcess();
	}
	
	private ExcelRecordProcessor getExcelRecordProcessor(DNAList dnaProcessor) throws BMException {
		
    	String sProcessorClassName = dnaProcessor.getString("class");
    	if (sProcessorClassName == null)
    		throw new BMException("'class' is missing for the processor '" + dnaProcessor + "' in import_excel.dna");    	
		
		ExcelRecordProcessor excelRecordProcessor = null;
		
		try {
	        Class cls = Class.forName(sProcessorClassName);
	        excelRecordProcessor = (ExcelRecordProcessor) cls.newInstance();
	    } catch (Exception e) {
	    	throw new BMException("Fail to instantiate processor class " + sProcessorClassName, e, null);
		}  

		return excelRecordProcessor;
	}
	
	public void parseLine() throws BMException {		
		for (int i=0; i<dnaProcess.size(); i++)
		{
			DNAList dnaProcessor = dnaProcess.elementAt(i);
			ExcelRecordProcessor excelRecordProcessor = getExcelRecordProcessor(dnaProcessor);
			
	    	DNAList dnaInput = dnaProcessor.getList("input");
	    	if (dnaInput == null)
	    	{
	        	String sMessage = BMMessages.getError("EXCEL_LINE_PROCESSOR_1", null);
	    		throw new ExcelProcessingException(sMessage);
	    	}
			
			DNAList dnaRecord = getRecord(dnaInput, dnaImportLine);
			excelRecordProcessor.setRecord(dnaRecord);
			excelRecordProcessor.process();
		}
	}
	
	private DNAList getRecord(DNAList dnaInput, DNAList dnaImportLine) throws BMException
	{
		//Be careful if you forget to clone, you will change dnaConfig!!! 
		DNAList dnaInputCopy = (DNAList) dnaInput.clone();
		
		DNAListIterator iter = dnaInput.iterator();
		while (iter.hasNext())
		{
			String name = iter.nextName();
			String column = dnaInputCopy.getString(name);
			if (column != null && column.startsWith("%"))
			{
				column = column.substring(1); //remove %
				dnaInputCopy.setString(name + "ColumnName", column);
				DNABasePair baseImportLine = dnaImportLine.getBasePair(column);
				
		    	if (baseImportLine == null)
		    	{
		   			DNAList dnaError = new DNAList();
	    			dnaError.setString("column", column);		    		
		        	String sMessage = BMMessages.getError("EXCEL_LINE_PROCESSOR_2", dnaError);
		    		throw new ExcelProcessingException(sMessage);
		    	}
		    	
		    	dnaInputCopy.setBasePair(name, baseImportLine);				
			}
		}
		return dnaInputCopy;
	}
}
