package com.bluemartini.loader.excel.record;

import java.util.Calendar;
import com.bluemartini.core.Constants;
import com.bluemartini.database.BMDatabase;
import com.bluemartini.database.BMDatabaseManager;
import com.bluemartini.database.DBUtil;
import com.bluemartini.dna.BMContext;
import com.bluemartini.dna.BMCurrencies;
import com.bluemartini.dna.BMException;
import com.bluemartini.dna.BMLog;
import com.bluemartini.dna.BMMessages;
import com.bluemartini.dna.BusinessObject;
import com.bluemartini.dna.Currency;
import com.bluemartini.dna.DNABasePair;
import com.bluemartini.dna.DNAList;
import com.bluemartini.remotedesktop.ObjectPrice;
import com.bluemartini.remotedesktop.PriceListFolder;
import com.bluemartini.remotedesktop.util.PricingUtil;
import com.bluemartini.util.CISObjectUtil;

/**
 * This processor is used to update an object price.
 * 
 * objectName
 * objectType
 * priceListFolder
 * asOfDate
 * amount
 * removeValueChain

 * 
 * @author Yannick Robin
 *
 */
public class ExcelObjectPriceRecordProcessor extends ExcelDefaultRecordProcessor {
	
	protected String removeValueChain_ = "*";
	private static final String DEFAULT_DB = "main";
	
	protected BusinessObject getObject(String objectType, String objectName) throws BMException
	{
		BusinessObject boObject = CISObjectUtil.getObjectDetailsByName(DEFAULT_DB, objectType, objectName);
		if (boObject == null)
		{
			DNAList dnaError = new DNAList();
			dnaError.setString("objectName", objectName);
			String sMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_PRICE_1", dnaError);
			throw new ExcelLineProcessingException(sMessage);
		}
		return boObject;
	}
	
    /**
     * Get the currency price list for given folder.
     *
     * @param  folderID
     *
     * @return  the Price List business object
     *
     * @exception  com.bluemartini.dna.BMException  if a database error occurs
     */
    public static BusinessObject getPriceList(Long folderID, Calendar asOfDate) throws BMException {

        if (folderID == null)
            return null;
        BusinessObject boPriceList = null;
        // Select the price list for the given folder that has a valid
        // begin date and end date.
        BMDatabase db = BMDatabaseManager.getCurrentDB();

        String sSQLAsOfDate = DBUtil.formatDateTime(asOfDate);
        StringBuffer where = new StringBuffer();
        where.append("PRL_PARENT_ID = ");
        where.append(folderID);
        where.append(" AND PRL_BEGIN_DT <= ");
        where.append(sSQLAsOfDate);
        where.append(" AND PRL_END_DT >= ");
        where.append(sSQLAsOfDate);
        where.append(" AND PRL_STATUS_CD <> ");
        where.append("'" + Constants.STATUS_DELETED + "'");
        
        boPriceList = DBUtil.selectBusinessObject("PRICE_LIST", where.toString());

        if (boPriceList == null)
            return null;

        return boPriceList;
    }
	
	public void process() throws BMException {
		DNAList dnaRecord = this.getRecord();
		BMLog.log(BMLog.COMPONENT_SYSTEM, 4, "ExcelObjectPriceRecordProcessor.process(): Record = " + this.getRecord());
		
		String objectName = dnaRecord.getString("objectName");
		String objectType = dnaRecord.getString("objectType");
		String priceListFolderName = dnaRecord.getString("priceListFolder");
		
		DNABasePair dnaBaseAsOfDate = dnaRecord.getBasePair("asOfDate");
    	if (dnaBaseAsOfDate != null && dnaBaseAsOfDate.getType() != DNABasePair.TYPE_DATETIME)
    	{
			String sMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_PRICE_2", null);
    		throw new ExcelLineProcessingException(sMessage);
    	}
    	Calendar asOfDate = dnaRecord.getDateTime("asOfDate");
    	
    	Currency amount = null;
    	boolean bRemoveValue = false;
		DNABasePair dnaBaseAmount = dnaRecord.getBasePair("amount");
		if (dnaBaseAmount == null || dnaBaseAmount.getValue() == null)
			return;
		
    	if (dnaBaseAmount.getType() != DNABasePair.TYPE_DOUBLE)
    	{
			String removeValueChain = dnaRecord.getString("removeValueChain", removeValueChain_);
			String sAmount = dnaRecord.getString("amount");
			if (sAmount.equals(removeValueChain))
				bRemoveValue = true;
			else
			{
				String sMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_PRICE_3", null);
				throw new ExcelLineProcessingException(sMessage);
			}
    	}
    	else
    	{
    		double dbAmount = dnaRecord.getDouble("amount");
    		amount = new Currency(dbAmount);
    		if (amount == null)
    			return;
    	}
		
		if (objectName == null)
		{
			String sMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_PRICE_4", null);
			throw new ExcelLineProcessingException(sMessage);
		}
		
		if (objectType == null)
		{
			String sMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_PRICE_5", null);
			throw new ExcelLineProcessingException(sMessage);
		}
		
		if (priceListFolderName == null)
		{
			String sMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_PRICE_6", null);
			throw new ExcelLineProcessingException(sMessage);
		}
		
		if (asOfDate == null)
			asOfDate = Calendar.getInstance();
		
		BusinessObject boObject = getObject(objectType, objectName);
		PriceListFolder priceListFolder = PricingUtil.getPriceListFolderByName(priceListFolderName);
		if (priceListFolder == null)
		{
			DNAList dnaError = new DNAList();
			dnaError.setString("priceListFolderName", priceListFolderName);
			String sMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_PRICE_7", dnaError);
			throw new ExcelLineProcessingException(sMessage);
		}
		
		BusinessObject boPriceList = getPriceList(priceListFolder.getLongID(), asOfDate);
		if (boPriceList == null)
		{
			DNAList dnaError = new DNAList();
			dnaError.setString("priceListFolderName", priceListFolderName);
			dnaError.setDate("asOfDate", asOfDate);
			String sMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_PRICE_8", dnaError);
			throw new ExcelLineProcessingException(sMessage);
		}

		String currencyCode = priceListFolder.getCurrencyCode();
        int scale = BMCurrencies.getScale(currencyCode);
        int rounding = BMCurrencies.getRounding(currencyCode);
        if (!bRemoveValue)
        	amount = amount.setScale(scale, rounding);
		
		ObjectPrice price = PricingUtil.getObjectPrice(boObject.getLongID(), boPriceList.getLongID());
		
		if (bRemoveValue)
		{
			if (price != null)
				DBUtil.deleteBusinessObject(price);
			return;
		}
		
		if (price != null) {
			Currency currentAmount = price.getPriceAmount();
			if (!amount.equals(currentAmount)) {
				// Update amount			
				BusinessObject boPrice = (BusinessObject)price.clone();
				boPrice.setCurrency("amount", amount);
				DBUtil.updateBusinessObject(boPrice);
			}
		} else {
			// Insert new price
			BusinessObject boPrice = BMContext.createBusinessObject("OBJECT_PRICE");
			boPrice.setCurrency("amount", amount);
			boPrice.setString("currencyCode", currencyCode);
			boPrice.setLong("objectID", boObject.getLongID());
			boPrice.setString("objectStatus_cd", "A");
			boPrice.setString("priceListFolderName", priceListFolderName);
			boPrice.setLong("priceListFolderID", priceListFolder.getLongID());
			boPrice.setLong("priceListID", boPriceList.getLongID());			
			DBUtil.insertBusinessObject(boPrice);
		}		
	}
}
