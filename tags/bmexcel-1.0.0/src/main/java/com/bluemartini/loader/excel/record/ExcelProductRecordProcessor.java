package com.bluemartini.loader.excel.record;

import com.bluemartini.client.BusinessActionClient;
import com.bluemartini.database.DBUtil;
import com.bluemartini.dna.BMContext;
import com.bluemartini.dna.BMException;
import com.bluemartini.dna.BMLog;
import com.bluemartini.dna.BMMessages;
import com.bluemartini.dna.BusinessObject;
import com.bluemartini.dna.DNAList;
import com.bluemartini.remotedesktop.Folder;
import com.bluemartini.remotedesktop.Product;
import com.bluemartini.remotedesktop.util.FolderUtil;
import com.bluemartini.remotedesktop.util.ProductUtil;

/**
 * Excel Record Processor for Product
 * 
 * @author Yannick Robin
 *
 */

public class ExcelProductRecordProcessor extends ExcelDefaultRecordProcessor {	
	
	public void process() throws BMException {
		DNAList dnaRecord = this.getRecord();
		BMLog.log(BMLog.COMPONENT_SYSTEM, 4, "ExcelProductRecordProcessor.process(): Record = " + this.getRecord());
		
		String productCode = dnaRecord.getString("productCode");
		String productFamily = dnaRecord.getString("family");
		String status_cd = dnaRecord.getString("status_cd", "I");		
		String shortDescription = dnaRecord.getString("shortDesc", "");
		Boolean backorderable = dnaRecord.getBoolean("backorderable");
		Integer skuReserveQuantity = dnaRecord.getInteger("skuReserveQuantity");
		Boolean zifEnabled = dnaRecord.getBoolean("zifEnabled", false);
		Boolean approved = dnaRecord.getBoolean("approved", true);
		String newProductCode = dnaRecord.getString("newProductCode");
		
		if (productCode == null)
			throw new ExcelLineProcessingException("Product code is missing");
		
		Folder folder = getProductFamily(productFamily);
		
		Product product = ProductUtil.getProductByCode(productCode);
		if (product != null)
		{
			//update product
			BusinessObject boProduct = (BusinessObject)product.clone();
			if (newProductCode != null)
				boProduct.setString("productCode", newProductCode);
			if (productFamily != null)
				boProduct.setLong("prd_parent_id", folder.getLongID());
			boProduct.setString("shortDesc", shortDescription);
			boProduct.setString("status_cd", status_cd);
			boProduct.setBoolean("backorderable", backorderable);
			boProduct.setInteger("skuReserveQuantity", skuReserveQuantity);
			boProduct.setBoolean("zifEnabled", zifEnabled);
			boProduct.setBoolean("approved", approved);
			
			DBUtil.updateBusinessObject(boProduct);
		}
		else
		{
			//create product			
			if (productFamily == null)
				throw new ExcelLineProcessingException("Product family is missing");
			
			BusinessObject boProduct = BMContext.createBusinessObject("PRODUCT");		
			boProduct.setString("productCode", productCode);
			boProduct.setLong("prd_parent_id", folder.getLongID());
			boProduct.setString("shortDesc", shortDescription);
			boProduct.setString("status_cd", status_cd);
			boProduct.setBoolean("backorderable", backorderable);
			boProduct.setInteger("skuReserveQuantity", skuReserveQuantity);
			boProduct.setBoolean("zifEnabled", zifEnabled);
			boProduct.setBoolean("approved", approved);
			
            try {
            	BusinessActionClient.executeBusinessAction("CreateObject", boProduct);
            }
            catch (BMException bme) {
                if (bme.containsErrorSubstring("OBJECT_PATH_CONFLICT")) {
        			String sMessage = BMMessages.getError("EXCEL_RECORD_PRODUCT_3", null);
                	throw new ExcelLineProcessingException(sMessage);                    	
                }
                throw bme;
            }
		}		
	}
	
	private Folder getProductFamily (String productFamily) throws BMException
	{
		if (productFamily == null)
			return null;
		Folder folder = null;
		try {
			folder = FolderUtil.getFolderDetailByPath(Product.PRODUCT_FOLDER_BIZOBJ_NAME, productFamily);
		} catch (BMException bme)
		{
			if (bme.getError().equals("OBJECT_NOT_VIEWABLE"))
			{
				DNAList dnaError = new DNAList();
				dnaError.setString("folderPath", productFamily);
				String sMessage = BMMessages.getError("EXCEL_RECORD_PRODUCT_1", dnaError);
				throw new ExcelLineProcessingException(sMessage);
			}
			else
				throw new BMException("Folder util exception", bme, null);
		}
		if (folder == null)
		{
			DNAList dnaError = new DNAList();
			dnaError.setString("folderPath", productFamily);
			String sMessage = BMMessages.getError("EXCEL_RECORD_PRODUCT_2", dnaError);
			throw new ExcelLineProcessingException(sMessage);
		}
		return folder;
	}
}