package com.bluemartini.loader.excel.record;

import java.io.File;

import com.bluemartini.core.BMSystem;
import com.bluemartini.core.BMUtil;
import com.bluemartini.dna.BMContext;
import com.bluemartini.dna.BMException;
import com.bluemartini.dna.BMLog;
import com.bluemartini.dna.BMMessages;
import com.bluemartini.dna.BMThreadManager;
import com.bluemartini.dna.BusinessObject;
import com.bluemartini.dna.DNAList;
import com.bluemartini.htmlapp.HTMLAssortmentUtil;
import com.bluemartini.loader.BMImport;
import com.bluemartini.loader.ImportConstants;
import com.bluemartini.loader.RecordDef;
import com.bluemartini.loader.RecordProcessor;

/**
 * Excel Record Processor for Media Upload
 * 
 * @author Yannick Robin
 *
 */
public class ExcelBMIRecordProcessor extends ExcelDefaultRecordProcessor {
	
    private void validateConfig(BusinessObject boRecord) throws BMException
	{
	    String sRecordType = boRecord.getString("recordType");
	    RecordDef recordDef = BMImport.getLoaderConfig().getRecordDef(sRecordType);
	    String sObjectType = recordDef.getString("businessObjectType");
	    if(sObjectType == null)
	        return;
	    com.bluemartini.dna.BusinessObjectDef boDef = BMContext.getBusinessObjectDefIfPresent(sObjectType);
	    if(boDef == null)
	    {
	        DNAList dna = new DNAList();
	        dna.setString("objectType", sObjectType);
	        throw new BMException("LOADER_INVALID_CONFIG", dna);
	    } else
	    {
	        return;
	    }
	}
	    
	public void process() throws BMException {
		DNAList dnaRecord = this.getRecord();
		BMLog.log(BMLog.COMPONENT_SYSTEM, 4, "ExcelMediaUploadRecordProcessor.process(): Record = " + this.getRecord());
		
		String sBMSHome =  BMSystem.getProperty("BMS_CONFIG_HOME");		
	    String sUserConfig = sBMSHome + File.separatorChar + "core" + File.separatorChar + "config" + File.separatorChar + "appcommon" + File.separatorChar + ImportConstants.IMPORT_CONFIG;
	    BMImport.readConfig(sUserConfig);
	    
		BusinessObject boRecord = BMContext.createBusinessObject("IMPORT_RECORD");
		
		boRecord.setString("action", dnaRecord.getString("action"));
        boRecord.setString("recordType", dnaRecord.getString("recordType"));
        
        String sRecordType = boRecord.getString("recordType");
        if (sRecordType == null || sRecordType.equals(""))
        {
        	String sMessage = BMMessages.getError("EXCEL_RECORD_BMI_1", null);
        	throw new ExcelLineProcessingException(sMessage);
        }
        
        int iRecordSize = BMImport.getLoaderConfig().getRecordSize(sRecordType);

        for(int j = 0; j < iRecordSize; j++)
        {
        	String fieldName = "field" + j;
        	String fieldValue =  dnaRecord.getAsString(fieldName);
        	boRecord.setString(fieldName,fieldValue);
        }
                
        RecordProcessor processor = BMImport.getLoaderConfig().getRecordProcessor(sRecordType);
        BMImport.iVersion_ = BMThreadManager.getCurrentVersionID();
        if(processor != null)
        {
            try
            {
            	validateConfig(boRecord);
                validateConfig(boRecord);
                processor.processRecord(boRecord);
            }catch (BMException bme)
            {
    			DNAList dnaError = new DNAList();
    			dnaError.setString("bme", bme.getMessage());
            	String sMessage = BMMessages.getError("EXCEL_RECORD_BMI_2", dnaError);
            	throw new ExcelLineProcessingException(sMessage);
            }
        }
        else
        {
        	String sMessage = BMMessages.getError("EXCEL_RECORD_BMI_3", null);
        	throw new ExcelLineProcessingException(sMessage);
        }
	}
}