package com.bluemartini.loader.excel.record;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import com.bluemartini.core.BMUtil;
import com.bluemartini.database.DBUtil;
import com.bluemartini.database.ObjectDBUtil;
import com.bluemartini.dna.BMContext;
import com.bluemartini.dna.BMException;
import com.bluemartini.dna.BMLog;
import com.bluemartini.dna.BMMessages;
import com.bluemartini.dna.BusinessObject;
import com.bluemartini.dna.BusinessObjectArray;
import com.bluemartini.dna.DNABasePair;
import com.bluemartini.dna.DNAList;
import com.bluemartini.remotedesktop.Content;
import com.bluemartini.remotedesktop.util.AnonymousContentUtil;
import com.bluemartini.remotedesktop.util.ContentUtil;
import com.bluemartini.remotedesktop.util.ObjectAttributeUtil;
import com.bluemartini.server.BMClient;
import com.bluemartini.util.AttributeUtil;
import com.bluemartini.util.CISObjectUtil;
import com.bluemartini.util.LocaleUtil;

/**
 * This processor is used to update an object attribute.
 * It expects the following parameters that are configured in import_excel.dna:
 * - Excel column name where the object name (for example, product code) is stored 
 * - Object type (for example, PRODUCT)
 * - Attribute name (for example, ATR_XXX)
 * - Excel column name where the attribute value is stored
 * 
 * @author Yannick Robin
 *
 */
public class ExcelObjectAttributeRecordProcessor extends ExcelDefaultRecordProcessor {
	
	protected String objectName;
	protected String objectType;
	protected String attributeName;
	protected String attributeValueColumName;
	protected DNABasePair attributeValue;
	protected String attributeLocale;
	protected boolean ommitNullAttributeValue = true;
	protected String removeValueChain = "*";
	
	private static final String BLANK_CHAIN = "";
	private static final int TYPE_STRING = 0;
	private static final int TYPE_INTEGER = 1;
	private static final int TYPE_DOUBLE = 2;
	private static final int TYPE_DATETIME = 3;
	private static final int TYPE_BOOLEAN = 4;
	private static final int TYPE_COLLECT = 5;
	private static final int TYPE_LIST = 6;
	private static final int TYPE_CONTENT = 7;
	private static final int TYPE_LONGTEX = 8;
	private static final int TYPE_BITSTR = 9;
	private static final String DEFAULT_DB = "main";
	
	protected void setObjectName() throws BMException
	{		
    	objectName = this.getRecord().getString("objectName");    	
	}
	
	protected void setObjectType() throws BMException
	{
    	objectType = this.getRecord().getString("objectType");    	
	}
	
	protected void setAttributeName() throws BMException
	{		
    	attributeName = this.getRecord().getString("attributeName");
	}
	
	protected void setAttributeColumnName() throws BMException
	{		
		attributeValueColumName = this.getRecord().getString("attributeValueColumnName");
	}
	
	protected void setAttributeValue() throws BMException
	{
    	attributeValue = this.getRecord().getBasePair("attributeValue");
	}
	
	protected void setAttributeLocale() throws BMException
	{		
    	attributeLocale = this.getRecord().getString("attributeLocale");
	}
	
	private void setOmmitNullAttributeValue() throws BMException
	{
		if (this.getRecord().getBoolean("ommitNullAttributeValue")!=null)
			ommitNullAttributeValue = this.getRecord().getBoolean("ommitNullAttributeValue").booleanValue();	
	}
	
	private void setRemoveValueChain() throws BMException
	{
		if (this.getRecord().getBoolean("blankChain")!=null)
			removeValueChain = this.getRecord().getString("removeValueChain");	
	}
	
	protected BusinessObject getAttribute() throws BMException
	{
		BusinessObject boAttribute = AttributeUtil.getAttributeDetails(attributeName);
		if (boAttribute == null)
		{
			DNAList dnaError = new DNAList();
			dnaError.setString("attributeName", attributeName);
			String sMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_ATTRIBUTE_1", dnaError);
			throw new ExcelProcessingException(sMessage);
		}
		return boAttribute;
	}
	
	protected BusinessObject getObject() throws BMException
	{
		BusinessObject boObject = CISObjectUtil.getObjectDetailsByName(DEFAULT_DB, objectType, objectName);
		if (boObject == null)
		{
			DNAList dnaError = new DNAList();
			dnaError.setString("objectName", objectName);
			String sMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_ATTRIBUTE_2", dnaError);
			throw new ExcelLineProcessingException(sMessage);
		}
		return boObject;
	}
	
	protected void setObjectAttribute(BusinessObject boObject, BusinessObject boAttribute) throws BMException
	{
		Integer defaultLocaleID = LocaleUtil.getDefaultLocaleID();
		Integer localeID = defaultLocaleID;
		int attributeExtendedType = AttributeUtil.getExtendedType(boAttribute);
		
		if (AttributeUtil.isMultilingual(boAttribute) && attributeExtendedType != TYPE_LIST)
		{
			if (attributeLocale == null || attributeLocale.equals(""))
			{
				DNAList dnaError = new DNAList();
				dnaError.setString("attributeName", attributeName);
				String sMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_ATTRIBUTE_3", dnaError);
				throw new ExcelLineProcessingException(sMessage);
			}
			else
			{
				localeID = LocaleUtil.getLocaleIDByCode(attributeLocale);
				if (localeID == null)
				{
					DNAList dnaError = new DNAList();
					dnaError.setString("attributeLocale", attributeLocale);
					String sMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_ATTRIBUTE_4", dnaError);
					throw new ExcelLineProcessingException(sMessage);
				}
			}
		}
		
		Long objectID = boObject.getLongID();		
		Long attributeID = boAttribute.getLongID();
        
		//Possible cache issue when the object attribute is removed manually with ED and JMS disabled
		BusinessObject boObjectAttr = ObjectAttributeUtil.getObjectAttributeDetails(objectID, objectType, localeID, attributeID);		
		
		if (boObjectAttr != null && boObjectAttr.getLong("valueSourceID") == objectID)			
		{
			BusinessObject boObjectAttrTemp = (BusinessObject)boObjectAttr.clone();
			updateObjectAttributeValue(boAttribute, boObjectAttrTemp);
			DBUtil.updateBusinessObject(boObjectAttrTemp);
		}
		else
		{
            boObjectAttr = BMContext.createBusinessObject("OBJECT_ATTRIBUTE");
            boObjectAttr.setLong("attributeID", attributeID);
            boObjectAttr.setLong("valueSourceID",objectID);
            String objType = CISObjectUtil.getBusinessObjectType(boObject);
            boObjectAttr.setInteger("valueSourceType",ObjectDBUtil.getObjectTypeID(objType));
            boObjectAttr.setString("valueSourceName",boObject.getObjectName());
            boObjectAttr.setString("attributeName",boAttribute.getObjectName());

			updateObjectAttributeValue(boAttribute, boObjectAttr);
            boObjectAttr.setInteger("localeID",localeID);
            
            BusinessObjectArray boaNewValues = new BusinessObjectArray();
            BusinessObjectArray boaDeleteOverrides = new BusinessObjectArray();
            boaNewValues.addElement(boObjectAttr);            
            ObjectAttributeUtil.setObjectAttributes(boObject, boaNewValues, boaDeleteOverrides);
		}
            
        //If the attribute is multilingual, check a default value exists
		if (AttributeUtil.isMultilingual(boAttribute) && localeID != defaultLocaleID)
		{
			BusinessObject boObjectAttrDefault = ObjectAttributeUtil.getObjectAttributeDetails(objectID, objectType, defaultLocaleID, attributeID);
			//Should we check valueSourceID to control the default value doesn't come from parent??
			if (boObjectAttrDefault == null)
			{
				boObjectAttr.setInteger("localeID",defaultLocaleID);
				DNABasePair dnaPair = boObjectAttr.removeBasePair("value");
				if (dnaPair != null)
				{
					dnaPair.setValue(null);
					boObjectAttr.setBasePair("value", dnaPair);
				
					BusinessObjectArray boaNewValues = new BusinessObjectArray();
					BusinessObjectArray boaDeleteOverrides = new BusinessObjectArray();
					boaNewValues.addElement(boObjectAttr);
					ObjectAttributeUtil.setObjectAttributes(boObject, boaNewValues, boaDeleteOverrides);
				}
			}
		}
        	
	}
	
	private void updateObjectAttributeValue(BusinessObject boAttribute, BusinessObject boObjectAttr) throws BMException
	{
		// If value is "*", remove the value
		if (attributeValue.getType() == DNABasePair.TYPE_STRING)
		{
			String sAttributeValue = (String) attributeValue.getValue();
			if (sAttributeValue != null && sAttributeValue.equals(removeValueChain))
			{
				boObjectAttr.removeBasePair("value");
				return;
			}
		}
		
		// If value is not "*"
		try {
			DNAList dnaErrorMessage = new DNAList();
			dnaErrorMessage.setString("attributeValueColumName", attributeValueColumName);
			int attributeExtendedType = AttributeUtil.getExtendedType(boAttribute);
		    switch (attributeExtendedType) {
	        case TYPE_STRING:
	        	if (attributeValue.getType() != DNABasePair.TYPE_STRING)
	        	{
	        		String errorMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_ATTRIBUTE_5", dnaErrorMessage);
	        		throw new ExcelLineProcessingException(errorMessage);
	        	}
	        	String sAttributeValue = (String) attributeValue.getValue();
	        	try {
	        		int iLenght = sAttributeValue.getBytes("UTF-8").length;
		        	if (iLenght > 1024)
		        	{
		        		String errorMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_ATTRIBUTE_6", dnaErrorMessage);
		        		throw new ExcelLineProcessingException(errorMessage);
		        	}
        		} catch (UnsupportedEncodingException ex) {
        		  ex.printStackTrace();
        		}
	        	boObjectAttr.setString("value" , sAttributeValue);
	            break;
	        case TYPE_INTEGER:
	        	if (attributeValue.getType() != DNABasePair.TYPE_DOUBLE)
	        	{
	        		String errorMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_ATTRIBUTE_7", dnaErrorMessage);
	        		throw new ExcelLineProcessingException(errorMessage);
	        	}
	        	
	        	try {
	        		Double dIntAttributeValue = (Double)attributeValue.getValue();
	        		Integer iAttributeValue = new Integer(dIntAttributeValue.intValue());
	        		boObjectAttr.setInteger("value" , iAttributeValue);
	        	}catch (NumberFormatException e2)
	    		{
	        		String errorMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_ATTRIBUTE_8", dnaErrorMessage);
	    			throw new ExcelLineProcessingException(errorMessage);
	    		}
	            break;
	        case TYPE_DOUBLE:
	        	if (attributeValue.getType() != DNABasePair.TYPE_DOUBLE)
	        	{
	        		String errorMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_ATTRIBUTE_7", dnaErrorMessage);
	        		throw new ExcelLineProcessingException(errorMessage);
	        	}
	        	Double dAttributeValue = (Double)attributeValue.getValue();
	        	boObjectAttr.setDouble("value" , dAttributeValue);
	            break;
	        case TYPE_DATETIME:
	        	if (attributeValue.getType() != DNABasePair.TYPE_DATETIME)
	        	{
	        		String errorMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_ATTRIBUTE_9", dnaErrorMessage);
	        		throw new ExcelLineProcessingException(errorMessage);
	        	}
	        	Calendar calAttributeValue = (Calendar)attributeValue.getValue();
	        	boObjectAttr.setDateTime("value" , calAttributeValue);
	        	break;
	        case TYPE_BOOLEAN:
	        	if (attributeValue.getType() != DNABasePair.TYPE_BOOLEAN)
	        	{
	        		String errorMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_ATTRIBUTE_10", dnaErrorMessage);
	        		throw new ExcelLineProcessingException(errorMessage);
	        	}
	        	Boolean bAttributeValue = (Boolean) attributeValue.getValue();
	        	boObjectAttr.setBoolean("value" , bAttributeValue);
	            break;
	        case TYPE_COLLECT:
    			DNAList dnaError = new DNAList();
    			dnaError.setString("attributeName", attributeName);
    			String sMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_ATTRIBUTE_11", dnaErrorMessage);
	        	throw new ExcelProcessingException(sMessage);
	        case TYPE_LIST:
	        	if (attributeValue.getType() != DNABasePair.TYPE_STRING)
	        	{
	        		String errorMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_ATTRIBUTE_5", null);
	        		throw new ExcelLineProcessingException(errorMessage);
	        	}
	        	
	        	Integer localeID = LocaleUtil.getDefaultLocaleID();
	        	Long domID = boAttribute.getLong("atr_dom_id");
	            DNAList dnaIn = new DNAList();
	            dnaIn.setString("type", "ATTRIBUTE_DOMAIN_VALUE");
	            dnaIn.setString("where", "DMV_DOM_ID=" + domID + " AND DMV_LOC_ID=" + localeID);
	            DNAList dnaOut = BMClient.executeBusinessAction("GetObjects", dnaIn);
	            BusinessObjectArray boaDomainValue = dnaOut.removeBusinessObjectArray("ATTRIBUTE_DOMAIN_VALUE_ARRAY");
	            
	            boolean isValid = false;
	            Long domainValueID = null;
	        	sAttributeValue = (String) attributeValue.getValue();
	            for (int i=0; i < boaDomainValue.size(); i++)
	            {
	            	BusinessObject boDomainValue = boaDomainValue.elementAt(i);
	            	String domainValue = boDomainValue.getString("value");
	            	if (sAttributeValue.equals(domainValue))
	            	{
	            		//domainValueID = boDomainValue.getLongID();
	            		domainValueID = boDomainValue.getLong("dmv_id");
	            		isValid = true;
	            		break;
	            	}
	            }
	            
	            if (!isValid)
	            {
	    			dnaError = new DNAList();
	    			dnaError.setString("attributeValueColumName", attributeValueColumName);
	    			sMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_ATTRIBUTE_12", dnaErrorMessage);
	            	throw new ExcelLineProcessingException(sMessage);
	            }
	            
	            if (domainValueID == null)
	            {
	    			dnaError = new DNAList();
	    			dnaError.setString("attributeValueColumName", attributeValueColumName);
	    			sMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_ATTRIBUTE_13", dnaErrorMessage);
	            	throw new ExcelLineProcessingException(sMessage);
	            }
	            
	            boObjectAttr.removeBasePair("value");
	            boObjectAttr.setString("value" , sAttributeValue);      
	        	break;
	        case TYPE_CONTENT:
	        	if (attributeValue.getType() != DNABasePair.TYPE_STRING)
	        	{
	    			dnaError = new DNAList();
	    			dnaError.setString("attributeValueColumName", attributeValueColumName);
	    			sMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_ATTRIBUTE_5", dnaErrorMessage);
	        		throw new ExcelLineProcessingException(sMessage);
	        	}
	        	
	    		String contentPath = (String)attributeValue.getValue();
	        	Content boContent = ContentUtil.getContentByName(contentPath);
	        	
	    		if (boContent == null)
	    		{
	    			dnaError = new DNAList();
	    			dnaError.setString("contentPath", contentPath);
	    			sMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_ATTRIBUTE_14", dnaError);
	    			throw new ExcelLineProcessingException(sMessage);
	    		}
	        	
                Long contentID = boContent.getLongID();
                boObjectAttr.removeBasePair("value");
                boObjectAttr.setLong("value", contentID);	            
	            break;
	        case TYPE_LONGTEX:
	        	if (attributeValue.getType() != DNABasePair.TYPE_STRING)
	        	{
	    			dnaError = new DNAList();
	    			dnaError.setString("attributeValueColumName", attributeValueColumName);
	    			sMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_ATTRIBUTE_5", dnaErrorMessage);
	        		throw new ExcelLineProcessingException(sMessage);
	        	}
	        	String LTAAttributeValue = (String) attributeValue.getValue();      		        	
	    		LTAAttributeValue = BMUtil.replaceString(LTAAttributeValue, "\n", "<p>");
	    			    		
	    		//AnonymousContentUtil will convert in UTF8 blob
	    		BusinessObject boLTAContent = AnonymousContentUtil.createContent(LTAAttributeValue, null);
	            if (boLTAContent != null) {
	                Long ltaID = boLTAContent.getLongID();
	                boObjectAttr.removeBasePair("value");
	                boObjectAttr.setLong("value", ltaID);
	            }	        	
	        	break;
	        case TYPE_BITSTR:
    			dnaError = new DNAList();
    			dnaError.setString("attributeName", attributeName);
    			sMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_ATTRIBUTE_15", dnaError);
	        	throw new ExcelProcessingException(sMessage);       	
	        default:
	            break;
		    }
		}catch (ClassCastException e)
		{
			DNAList dnaError = new DNAList();
			dnaError.setString("attributeValueColumName", attributeValueColumName);
			String sMessage = BMMessages.getError("EXCEL_RECORD_OBJECT_ATTRIBUTE_16", dnaError);
			throw new ExcelLineProcessingException(sMessage);
		}
		
	}
	
	public void process() throws BMException {
		BMLog.log(BMLog.COMPONENT_SYSTEM, 4, "ExcelObjectAttributeRecordProcessor.process(): Import Line Data = " + this.getRecord());
		
		setObjectName();
		setObjectType();
		setAttributeName();
		setAttributeValue();
		setAttributeColumnName();
		setAttributeLocale();
		setOmmitNullAttributeValue();
		setRemoveValueChain();		
		
		BMLog.log(BMLog.COMPONENT_SYSTEM, 3, "ExcelObjectAttributeRecordProcessor.process(): objectName  = " + objectName);	
		BMLog.log(BMLog.COMPONENT_SYSTEM, 3, "ExcelObjectAttributeRecordProcessor.process(): attributeName  = " + attributeName);
		BMLog.log(BMLog.COMPONENT_SYSTEM, 3, "ExcelObjectAttributeRecordProcessor.process(): attributeValue  = " + attributeValue);
		
		if (ommitNullAttributeValue)
		{
			//if null or blank, ommit the cell
			if (attributeValue.getValue() == null)
				return;
			
			if (attributeValue.getType() == DNABasePair.TYPE_STRING)
			{
				String sAttributeValue = (String) attributeValue.getValue();
				if (sAttributeValue.equals(BLANK_CHAIN))
					return;
			}
		}
		
		BusinessObject boObject = getObject();
		BusinessObject boAttribute = getAttribute();
		
		setObjectAttribute(boObject, boAttribute);
	}
	
}
