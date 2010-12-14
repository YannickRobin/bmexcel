package com.bluemartini.loader.excel.record;

import java.io.File;
import java.util.StringTokenizer;

import com.bluemartini.app.BMV;
import com.bluemartini.client.BusinessActionClient;
import com.bluemartini.core.BMUtil;
import com.bluemartini.database.DBUtil;
import com.bluemartini.dna.BMContext;
import com.bluemartini.dna.BMException;
import com.bluemartini.dna.BMLog;
import com.bluemartini.dna.BMMessages;
import com.bluemartini.dna.BusinessObject;
import com.bluemartini.dna.BusinessObjectArray;
import com.bluemartini.dna.DNAList;
import com.bluemartini.dna.DataDictionary;
import com.bluemartini.dna.DataElement;
import com.bluemartini.remotedesktop.Content;
import com.bluemartini.remotedesktop.Folder;
import com.bluemartini.remotedesktop.util.FolderUtil;
import com.bluemartini.remotedesktop.util.ObjectAttributeUtil;
import com.bluemartini.remotedesktop.util.ProductUtil;
import com.bluemartini.security.BMAccessControl;
import com.bluemartini.util.AttributeUtil;
import com.bluemartini.util.CISObjectUtil;
import com.bluemartini.util.ContentFolderUtil;
import com.bluemartini.util.LocaleUtil;

/**
 * Excel Record Processor for Media Upload
 * 
 * @author Yannick Robin
 *
 */
public class ExcelMediaUploadRecordProcessor extends ExcelDefaultRecordProcessor {

	protected String objectName = null;
	protected BusinessObjectArray boaObjects = null;
	protected static int FILE_MAX_LENGTH = 1024*1024;	
	protected static String DEFAULT_DELIM = ";";
	private static final String DEFAULT_DB = "main";	
	
	public void process() throws BMException {
		DNAList dnaRecord = this.getRecord();
		BMLog.log(BMLog.COMPONENT_SYSTEM, 4, "ExcelMediaUploadRecordProcessor.process(): Record = " + this.getRecord());
		
		String mediaName = dnaRecord.getString("mediaName");
		if (mediaName == null || mediaName.equals(""))
			return;
		
		String action = dnaRecord.getString("action");				
		if (action == null)
			throw new ExcelLineProcessingException("Action is missing");
		
		String folderPath = dnaRecord.getString("contentPath");
		Folder folder = null;
		try {
			folder = FolderUtil.getFolderDetailByPath(Content.CONTENT_FOLDER_BIZOBJ_NAME, folderPath);
		} catch (BMException bme)
		{
			if (bme.getError().equals("OBJECT_NOT_VIEWABLE"))
			{
				DNAList dnaError = new DNAList();
				dnaError.setString("folderPath", folderPath);
				String sMessage = BMMessages.getError("EXCEL_RECORD_MEDIA_UPLOAD_1", dnaError);
				throw new ExcelProcessingException(sMessage);
			}
			else
				throw new BMException("Folder util exception", bme, null);
		}
		if (folder == null)
		{
			DNAList dnaError = new DNAList();
			dnaError.setString("folderPath", folderPath);
			String sMessage = BMMessages.getError("EXCEL_RECORD_MEDIA_UPLOAD_2", dnaError);
			throw new ExcelProcessingException(sMessage);
		}
		String contentPath = folder.getPath() + "/" + getContentName();
		
		if ( !(action.equals("S") || action.equals("D")) )
		{
			String sMessage = BMMessages.getError("EXCEL_RECORD_MEDIA_UPLOAD_3", null);
			throw new ExcelLineProcessingException(sMessage);
		}
			
		
		if (action.equals("S"))
		{			
			String mediaUploadDir = dnaRecord.getString("mediaUploadDir");
			File dirUpload = new File(mediaUploadDir);
			
			if (!dirUpload.exists())
			{
				DNAList dnaError = new DNAList();
				dnaError.setString("mediaUploadDir", mediaUploadDir);
				String sMessage = BMMessages.getError("EXCEL_RECORD_MEDIA_UPLOAD_4", dnaError);
				throw new ExcelProcessingException(sMessage);
			}
			
			String mediaArchiveDir = dnaRecord.getString("mediaUploadArchive");
			File dirArchive = new File(mediaArchiveDir);
			
			if (!dirArchive.exists())
			{
				DNAList dnaError = new DNAList();
				dnaError.setString("mediaArchiveDir", mediaArchiveDir);
				String sMessage = BMMessages.getError("EXCEL_RECORD_MEDIA_UPLOAD_5", dnaError);				
				throw new ExcelProcessingException(sMessage);
			}
			
			String sFilePath = BMUtil.fixPath(mediaUploadDir + File.separatorChar + mediaName);		
			File file = new File(sFilePath);
			checkMediaValidity(file);	
			
			importContent(file, folder, getContentName());
			
			BusinessObjectArray boaObjectLoop = getObjects();		
			for (int i=0; i<boaObjectLoop.size(); i++)
			{
				BusinessObject boObject = boaObjectLoop.elementAt(i);
				associateObjectToContent(boObject.getObjectName(), contentPath);
			}		
			moveFileToArchive(file, dirArchive);
		}
		else
		{
			BusinessObjectArray boaObjectLoop = getObjects();		
			for (int i=0; i<boaObjectLoop.size(); i++)
			{
				BusinessObject boObject = boaObjectLoop.elementAt(i);
				unassociateObjectToContent(boObject);
			}	
		}
		
	}
	
	protected BusinessObjectArray getObjects() throws BMException
	{
		if (boaObjects != null)
			return boaObjects;
		
		boaObjects = new BusinessObjectArray();
		String objectName = getRecord().getString("objectName");
		if (objectName == null)
		{
			String sMessage = BMMessages.getError("EXCEL_RECORD_MEDIA_UPLOAD_6", null);	
			throw new ExcelLineProcessingException(sMessage);
		}
		
		String objectDelimiter = getRecord().getString("objectDelimiter");				
		if (objectDelimiter == null)
			objectDelimiter = DEFAULT_DELIM;
		
	    StringTokenizer parser = new StringTokenizer(objectName, objectDelimiter);
	    while (parser.hasMoreTokens()) {
	    	String uniqueObjectName = parser.nextToken();
	    	String objectType = getRecord().getString("objectType");
	    	BusinessObject boObject = CISObjectUtil.getObjectDetailsByName(DEFAULT_DB, objectType, uniqueObjectName);	
			if (boObject == null)
			{
				DNAList dnaError = new DNAList();
				dnaError.setString("uniqueObjectName", uniqueObjectName);
				String sMessage = BMMessages.getError("EXCEL_RECORD_MEDIA_UPLOAD_7", dnaError);	
				throw new ExcelLineProcessingException(sMessage);
			}
			
			boaObjects.addElement(boObject);
	    }	    
	    return boaObjects;
	}
	
	protected String getContentName() throws BMException
	{
		DNAList dnaRecord = this.getRecord();
		String contentName = dnaRecord.getString("contentName");
		if (contentName == null)
		{
			String sMessage = BMMessages.getError("EXCEL_RECORD_MEDIA_UPLOAD_8", null);	
			throw new ExcelLineProcessingException(sMessage);
		}
		
		return contentName;
	}
	
	protected void checkMediaValidity(File file) throws BMException
	{
		if (!file.exists() || !file.isFile())
		{
			DNAList dnaError = new DNAList();
			dnaError.setString("fileName", file.getName());
			String sMessage = BMMessages.getError("EXCEL_RECORD_MEDIA_UPLOAD_9", dnaError);			
			throw new ExcelLineProcessingException(sMessage);
		}
		
		if (file.length() > FILE_MAX_LENGTH)
		{
			DNAList dnaError = new DNAList();
			dnaError.setString("fileName", file.getName());
			String sMessage = BMMessages.getError("EXCEL_RECORD_MEDIA_UPLOAD_10", dnaError);	
			throw new ExcelLineProcessingException(sMessage);
		}
	}
	
	protected String getFileExtension() throws BMException
	{
		String mediaName = getRecord().getString("mediaName");
		String fileExtension = mediaName.substring(mediaName.lastIndexOf('.')+1);
		return fileExtension;
	}
	
	/*
	 * @see com.bluemartini.app.BMV
	 */
	private void importContent(File file, Folder folder, String contentName) throws BMException
	{
		boolean bForce = true;
		String sStatus_ = "A";
		String comment = "Imported with Excel upload";
		
        String fileName = file.getName();
        DataDictionary dataDict = BMContext.getDictionary();
        DataElement dataElem = dataDict.getDataElement("contentName");
        DNAList ret = dataElem.validateAndReturnDNA(fileName);
        if(!ret.getBoolean("valid", false))
        {
        	throw new ExcelLineProcessingException("Invalid content type");
        } else
        {
            BusinessObject boContent = ContentFolderUtil.readContentFromFile(file.getAbsolutePath(), folder);
            boContent.setString("name", contentName);
            if(sStatus_ != null)
                boContent.setString("status_cd", sStatus_);
            String sObjectType = boContent.getBusinessObjectType();
            com.bluemartini.dna.BusinessObjectDef def = BMContext.getBusinessObjectDef(sObjectType);
            String sPrivName = "";
            if(def != null)
                sPrivName = def.getString("privilegeName");
            if(sPrivName != null && !BMAccessControl.canCreate(sPrivName))
            {
    			DNAList dnaError = new DNAList();
    			dnaError.setString("fileName", file.getName());
    			String sMessage = BMMessages.getError("EXCEL_RECORD_MEDIA_UPLOAD_11", dnaError);
                throw new ExcelLineProcessingException(sMessage);
            }
            BusinessObject boNewMedia = boContent.removeBusinessObject("media");
            if(!BMV.getContentDetails(boContent))
            {
                //Changed because the error: Attempted to insert a base pair owned by another list (media)
                boContent.setBusinessObjectRef("media", boNewMedia);
                boContent.setBusinessObject("media", boNewMedia);
                try {
                	BusinessActionClient.executeBusinessAction("CreateObject", boContent);
                }
                catch (BMException bme) {
                    if (bme.containsErrorSubstring("OBJECT_PATH_CONFLICT")) {
            			String sMessage = BMMessages.getError("EXCEL_RECORD_MEDIA_UPLOAD_12", null);
                    	throw new ExcelLineProcessingException(sMessage);                    	
                    }
                    throw bme;
                }                
            } else
            if(bForce)
            {
                if(!boContent.getBoolean("coFlag", false))
                {               	
                    boNewMedia.setString("comment", comment);
                    boNewMedia.setLong("contentID", boContent.getLongID());
                    boNewMedia.setString("mimeType", boContent.getString("mimeType"));
                    DNAList dnaCreateMedia = new DNAList();
                    dnaCreateMedia.setBusinessObjectRef(boNewMedia);
                    dnaCreateMedia.setBoolean("refresh", false);
                    //Changed because the error: Attempted to insert a base pair owned by another list (media)
                    //boContent.setBusinessObjectRef("media", boNewMedia);
                    boContent.setBusinessObject("media", boNewMedia);
                    try {
                    	BusinessActionClient.executeBusinessAction("CreateObject", dnaCreateMedia);
                    }
                    catch (BMException bme) {
                        if (bme.containsErrorSubstring("OBJECT_PATH_CONFLICT")) {
                			String sMessage = BMMessages.getError("EXCEL_RECORD_MEDIA_UPLOAD_12", null);
                        	throw new ExcelLineProcessingException(sMessage);
                        }
                        throw bme;
                    }
                } else
                {
                    String sMsg = "Content locked by " + boContent.getString("coUserName") + " on " + boContent.getString("coHost") + "";
                    throw new ExcelLineProcessingException(sMsg);
                }
            } else
            {
    			String sMessage = BMMessages.getError("EXCEL_RECORD_MEDIA_UPLOAD_13", null);
            	throw new ExcelLineProcessingException(sMessage);
            }
        }
	}
	
	private void associateObjectToContent(String objectName, String contentPath) throws BMException
	{
		ExcelObjectAttributeRecordProcessor processor = new ExcelObjectAttributeRecordProcessor();
		DNAList record = this.getRecord();
		record.setString("objectName", objectName);
		record.setString("attributeValue", contentPath);	
		processor.setRecord(record);
		processor.process();
	}
	
	private void unassociateObjectToContent(BusinessObject boObject) throws BMException
	{
		String attributeName = this.getRecord().getString("attributeName");
		if (attributeName == null)
			throw new ExcelLineProcessingException("Attribute name is missing");
		
		BusinessObject boAttribute = AttributeUtil.getAttributeDetails(attributeName);
		if (boAttribute == null)
		{
			DNAList dnaError = new DNAList();
			dnaError.setString("attributeName", attributeName);
			String sMessage = BMMessages.getError("EXCEL_RECORD_MEDIA_UPLOAD_14", dnaError);	
			throw new ExcelLineProcessingException(sMessage);
		}
		
		String attributeLocale = this.getRecord().getString("locale");		
		Integer defaultLocaleID = LocaleUtil.getDefaultLocaleID();
		Integer localeID = defaultLocaleID;

		if (AttributeUtil.isMultilingual(boAttribute))
		{
			if (attributeLocale == null || attributeLocale.equals(""))			
			{
				DNAList dnaError = new DNAList();
				dnaError.setString("attributeName", attributeName);
				String sMessage = BMMessages.getError("EXCEL_RECORD_MEDIA_UPLOAD_15", dnaError);
				throw new ExcelProcessingException(sMessage);
			}
			else
				localeID = LocaleUtil.getLocaleIDByCode(attributeLocale);
		}
		String objectType = CISObjectUtil.getBusinessObjectType(boObject);
				 
		BusinessObject boObjectAttr = ObjectAttributeUtil.getObjectAttributeDetail(boObject.getLongID(), objectType, localeID, boAttribute.getLongID());
		if (boObjectAttr != null)
			DBUtil.deleteBusinessObject(boObjectAttr);

	}
	
	private void moveFileToArchive(File file, File dirArchive) throws BMException
	{
		File fileDest = new File(dirArchive, file.getName());
		if (fileDest.exists())
			fileDest.delete();
			
		boolean success = file.renameTo(fileDest);
	    if (!success)
	    {
			String sMessage = BMMessages.getError("EXCEL_RECORD_MEDIA_UPLOAD_16", null);
	    	throw new ExcelLineProcessingException(sMessage);
	    }
	}
}