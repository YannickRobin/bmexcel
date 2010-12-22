package com.bluemartini.loader.excel.record;

import com.bluemartini.dna.BMException;
import com.bluemartini.dna.BMLog;
import com.bluemartini.dna.BMMessages;
import com.bluemartini.dna.DNAList;

/**
 * Default Record processor
 * 
 * @author Yannick Robin
 *
 */
public class ExcelDefaultRecordProcessor implements ExcelRecordProcessor {
	
	private DNAList dnaRecord = null;
	
	public void setRecord(DNAList dnaRecord) throws BMException {
		this.dnaRecord = dnaRecord;
	}
	
	public DNAList getRecord() throws BMException {
		return dnaRecord;
	}
	
	public void process() throws BMException {
		BMLog.log(BMLog.COMPONENT_SYSTEM, 4, "ExcelDefaultRecordProcessor.process(): Record = " + dnaRecord);
	}
	
	protected Long getLongRecord(String columnName) throws BMException
	{
		return getLongRecord(columnName, true);
	}
	
	protected Long getLongRecord(String columnName, boolean mandatory) throws BMException
	{
		Long longValue = null;
		
		Double dValue = getRecord().getDouble(columnName);
		
		if (dValue == null)
		{
			DNAList dnaError = new DNAList();
			dnaError.setString("columnName", columnName);
			String sMessage = BMMessages.getError("EXCEL_RECORD_DEFAULT_1", dnaError);
			throw new ExcelLineProcessingException(sMessage);
		}
		
		try {
			longValue = new Long(dValue.longValue());
    	}catch (NumberFormatException e2)
		{
			DNAList dnaError = new DNAList();
			dnaError.setString("columnName", columnName);
			String sMessage = BMMessages.getError("EXCEL_RECORD_DEFAULT_2", dnaError);
			throw new ExcelLineProcessingException(sMessage);
		}
    	
    	return longValue;
	}
	
	protected Integer getIntegerRecord(String columnName) throws BMException
	{
		return getIntegerRecord(columnName, true);
	}
	
	protected Integer getIntegerRecord(String columnName, boolean mandatory) throws BMException
	{
		Integer intValue = null;
		
		Double dValue = getRecord().getDouble(columnName);
		
		if (dValue == null)
		{
			if (mandatory)
			{
				DNAList dnaError = new DNAList();
				dnaError.setString("columnName", columnName);
				String sMessage = BMMessages.getError("EXCEL_RECORD_DEFAULT_1", dnaError);
				throw new ExcelLineProcessingException(sMessage);
			}
			else
				return null;
		}
		
		try {
			intValue = new Integer(dValue.intValue());
    	}catch (NumberFormatException e2)
		{
			DNAList dnaError = new DNAList();
			dnaError.setString("columnName", columnName);
			String sMessage = BMMessages.getError("EXCEL_RECORD_DEFAULT_2", dnaError);
			throw new ExcelLineProcessingException(sMessage);
		}
    	
    	return intValue;
	}
	
}
