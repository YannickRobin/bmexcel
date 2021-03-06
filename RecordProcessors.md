#summary Record Processor definition.

=Introduction=

For each line of the spreadsheet, all record processors indicated in the configuration file are executed.<br/>
The sections below indicate the definition of the input field expected for each record processor.<br/>
Please note, in the configuration file the field can be:
  * A static value: indicate a fixed value. i.e. {{{PRODUCT}}}
  * An Excel value: indicate an Excel column name (preceded by {{{%}}}). i.e.  {{{%Product code}}}

=Processor list==

  * [RecordProcessors#ExcelProductRecordProcessor ExcelProductRecordProcessor]
  * [RecordProcessors#ExcelObjectAttributeRecordProcessor ExcelObjectAttributeRecordProcessor]
  * [RecordProcessors#ExcelObjectPriceRecordProcessor ExcelObjectPriceRecordProcessor]
  * [RecordProcessors#ExcelMediaUploadRecordProcessor ExcelMediaUploadRecordProcessor]
  * [RecordProcessors#ExcelBMIRecordProcessor ExcelBMIRecordProcessor]
  * [RecordProcessors#ExcelStartWorkflowRecordProcessor ExcelStartWorkflowRecordProcessor]

=!ExcelProductRecordProcessor=

{{{!ExcelProductRecordProcessor}}} inserts a new product or updates an existing product.

==Definition==

|| *Input* || *Required* || *Default* || *Description* ||
||productCode||Yes||-||Product codes must be globally unique||
||family||No||-||The product family must be identified by a full slash-delimited path, starting with /Products.<br/>It is required for product creation.<br/>For existing product, if the product family is indicated, the product is moved.||
||status_cd||No||I||Valid values are defined by the productStatusCode domain: A—Active; I—Inactive.<br/>NOTE: Inactive products are not available for sale on the website.||
||shortDesc||No||-||Short description for the product. It is highly recommended that short descriptions be provided for all products.||
||backorderable||No||-||Back orderable flag.||
||skuReserveQuantity||No||-||Reserve quantity.||
||zifEnabled||No||false||Zero Inventory Filter; this flag filters out products that have no inventory.||
||newProductCode||No||-||To update the product code.||

==Example==

{{{
"class" String "com.bluemartini.loader.excel.record.ExcelProductRecordProcessor",
"input" DNA {
	"productCode" String "%Product Name",
	"family" String "%Product Family",
	"status_cd" String "A",
	"shortDesc" String "%Description short"
}
}}}

=!ExcelObjectAttributeRecordProcessor=

{{{ExcelObjectAttributeRecordProcessor}}} edits the attribute value for a specific object in the database.

==Definition==

|| *Input* || *Required* || *Default* || *Description* || 
||objectType||Yes||-||This field is case sensitive.<br/>Valid values:<br/>ASSORTMENT<br/>ASSORTMENT_FOLDER<br/>BUSINESS_ENTITY<br/>CART_HEADER<br/>CONTENT<br/>CONTENT_FOLDER<br/>MERCHANT<br/>MERCHANT_FOLDER<br/>ORGANIZATION<br/>ORDER<br/>PRODUCT<br/>PRODUCT_FAMILY<br/>PROMOTION<br/>QUOTE<br/>RELATIONSHIP<br/>SHIPMENT<br/>SKU<br/>SUB_ASSORTMENT<br/>USER_ACCOUNT||
||objectName||Yes||-||Must specify an existing object.<br/>For PRODUCT_FAMILY, this is the path, starting with /Products.<br/>For ASSORTMENT, SUB_ASSORTMENT, and ASSORTMENT_FOLDER, this is the path, starting with /Assortments.<br/>For CONTENT and CONTENT_FOLDER, this is the full path, starting with /Content.<br/>For PRODUCT, this is the Product Code.<br/>For SKU, this is the SKU Code.<br/>For ORDER, this is the Order Number.<br/>For USER_ACCOUNT, this is the User ID.<br/>For USER_FOLDER, this will always be "/Customer Attributes"<br/>For SHIPMENT, this is the Source Number (assigned by the fulfillment house).<br/>For RELATIONSHIP, this is the full path through the folder to the object. For example, to override a value on a product named J100 in the /Assortments/Main/!FallSale assortment, use /Assortments/Main/!FallSale/J100 as the Object Name.<br/>For USER_ACCOUNT, this is the User ID.||
||AttributeName||Yes||-||Must specify an existing attribute or property. For an attribute, should start by ‘ATR_’||
||!AttributeValue||Yes||-||The Excel cell must be formatted with the correct type for the attribute in question:<br/>Text for String, Collection, List of choice, Content and Long text attributes<br/>Number for Integer and Double attributes<br/>Date for Datetime attribute<br/>Boolean for Boolean attribute<br/><br/>For List of choice attributes, the value must be specified, not the display label.<br/>For Content, the path must be indicated.<br/>Collection is not supported||
||!AttributeLocale||No||-||For a multilingual attribute, this value is mandatory.||
||ommitNullAttributeValue||No||True||If !AttributeValue is empty. The object attribute is not updated.||
||removeValueChain||No||*||If !AttributeValue is equals to removeValueChain, the object attribute is removed||

==Example==
{{{
"class" String "com.bluemartini.loader.excel.record.ExcelObjectAttributeRecordProcessor",
"input" DNA {
	"objectType" String "PRODUCT",
	"objectName" String "%Product Name",
	"attributeName" String "ATR_Description",
	"attributeValue" String "%Description",
	"attributeLocale" String "en"
}
}}}


=!ExcelObjectPriceRecordProcessor=

{{{ExcelObjectPriceRecordProcessor}}} edits price data for a specific object in the database.

==Definition==

|| *Input* || *Required* || *Default* || *Description* ||
||objectType||Yes||-||This field is case sensitive.<br/>Valid values:<br/>PRODUCT<br>SKU||
||objectName||Yes||-||Must specify an existing object.<br/>For PRODUCT, this is the Product Code.<br/>For SKU, this is the SKU Code.||
||priceListFolder||Yes||-||Must specify the name of an existing price list folder. Lookup is not case sensitive.||
||asOfDate||No||Currently effective price list.||Price is applied to the price list effective on the date supplied.||
||amount||Yes||-||Price amount, denominated in the currency of the specified price list folder.||
||removeValueChain||No||*||If amount is equals to removeValueChain, the object price is removed||

==Example==
{{{
"class" String "com.bluemartini.loader.excel.record.ExcelObjectPriceRecordProcessor",
"input" DNA {
	"objectType" String "PRODUCT",
	"objectName" String "%Product Name",
	" priceListFolder" String "USA_Standard",
	"amount" String "%USA Standard price"
}
}}}

=!ExcelMediaUploadRecordProcessor=

{{{ExcelMediaUploadRecordProcessor}}} imports the image indicated in the database and associates this content to a specific object (or multiple objects) in the database using the attribute indicated. Before importing, images must be available on the server. It will be usually a folder available by ftp to the business users.

==Definition==

|| *Input* || *Required* || *Default* || *Description* ||
||action||Yes||-||S for insert or update<br/>D for delete||
||mediaUploadDir||Yes||-||The folder that contains the image.<br/>The folder must exist.||
||mediaUploadArchive||Yes||-||After import, the image is removed from the upload dir and backed up in the archive folder.<br/>This folder must exist.||
||mediaName||Yes||-||The image name on the disk||
||contentPath||Yes||-||The content folder where the image is imported.<br/>It must start with ‘/Content’.<br/>The content folder must exist.|| 
||contentName||Yes||-||The content name where the image is imported.<br/>If the content already exists, a new revision is created.||
||objectName||Yes||-||Must specify an existing object.<br/>Object Name supports multiple value separated by ‘,’.<br/>For PRODUCT_FAMILY, this is the path, starting with /Products.<br/>For ASSORTMENT, SUB_ASSORTMENT, and ASSORTMENT_FOLDER, this is the path, starting with /Assortments.<br/>For CONTENT and CONTENT_FOLDER, this is the full path, starting with /Content.<br/>For PRODUCT, this is the Product Code.<br/>For SKU, this is the SKU Code.<br/>For RELATIONSHIP, this is the full path through the folder to the object. For example, to override a value on a product named J100 in the /Assortments/Main/FallSale assortment, use /Assortments/Main/FallSale/J100 as the Object Name.||
||objectType||Yes||-||"PRODUCT", //for object attribute||
||attributeName||Yes||-||Must a content item attribute||
||attributeLocale||No||-||For a multilingual content attribute, this value is mandatory.||

==Example==
{{{
"class" String "com.bluemartini.loader.excel.record.ExcelMediaUploadRecordProcessor",
"input" DNA {
      "action" String "S",
	"mediaUploadDir" String "$BMS_HOME/temp/upload",
	"mediaUploadArchive" String "$BMS_HOME/temp/archive",					
      "mediaName" String "%Image name",	
	"contentPath" String "/Content/media/images/products",
	"contentName" String "%Content name",
      "objectName" String "%Product Name",
	"objectType" String "PRODUCT",
	"attributeName" String "ATR_Image_Title",
	"attributeLocale" String "en"    			
}
}}}

=ExcelBMIRecordProcessor=

{{{ExcelBMIRecordProcessor}}} reused BM Import record processor.

==Definition==

|| *Input* || *Required* || *Default* || *Description* ||
||action||Yes||-||BM Import action code.<br/>Usually (I, U, S or D), valid action codes depend on the record type.<br/>See Import File Reference document.||
||recordType||Yes||-||BM Import record type.<br/>See Import File Reference document.||
||field1, field2, …fieldN||No||-||BM Import fields. <br/>See Import File Reference document.||

==Example==
{{{
"class" String "com.bluemartini.loader.excel.record.ExcelBMIRecordProcessor",
"input" DNA {
	"action" String "%Action",
	"recordType" String "PRODUCT",
	"field1" String "%Product code",
	"field2" String "%Status code",
	"field3" String "%product family"
}
}}}

=!ExcelStartWorkflowRecordProcessor=

{{{ExcelStartWorkflowRecordProcessor}}} is included in {{{BM Flow}}} module.

==Definition==

|| *Input* || *Required* || *Default* || *Description* ||
||objectType||||||||
||objectName||||||||
||workflowTemplate||||||||
||workflowName||||||||
||workflowShortDesc||||||||
||workflowDueDt||||||||
||workflowPriority||||||||
||startWorkflow||||||||

==Example==
{{{
"class" String "com.bluemartini.loader.excel.record.ExcelStartWorkflowRecordProcessor",
"input" DNA {
	"objectType" String "PRODUCT",
	"objectName" String "%Product Name",
	"workflowTemplate" String "product_enrichment",
	"workflowName" String "%Workflow project name",
	"workflowShortDesc" String "This is a product enrichment workflow demo",
	"workflowDueDt" String "%Workflow due date",
	"workflowPriority" Integer "3",				
	"startWorkflow" String "%Start workflow"
}
}}}
