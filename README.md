# Purpose

BM Excel is a web interface to import catalog data using complex spreadsheets (template, macro, column formatting, images) with dynamic comprehensive error messages (using JExcelApi library).<br/>
The goal is to cover BM Import limitations for business users.<p/>

Here are the main features:
* Provide a web Interface.
* Accept and manipulate Excel spreadsheets without transformation so it supports Excel template, macro, column formatting, etc...
* Support custom spreadsheet template: you define your spreadsheet format by configuration file.
* Display comprehensive and user-friendly error messages.
* Execute multiple processors for each spreadsheet line. With BM Import to load a product with 9 attributes, you need 10 lines, with BM Excel, you need only one line. 
* Import images by batch and associates images to products or other BM objects.

# Release notes
[bmexcel-1.0.0-bin.zip](http://code.google.com/p/bmexcel/downloads/detail?name=bmexcel-1.0.0-bin.zip): Tested with BM 10.1

# Installation
  * Unzip `bmexcel-x.x.x-bin.zip` into your BMS home
  * Restart EAC server and Edesk server
  * With ED client, add `ExcelLoaderManagerAccess` privilege to your Rdesk user
  * Modify `remotedesktop.appconfig.dna`:
    * Add  `$BMS_CONFIG_HOME/core/config/excel_loader` to the module list (`module_paths` > `rdesk`)
    * Add `excelLoaderManager` to the manager list (`app` > `managers`)
  * Copy JExcelApi library (`jxl.jar`) into `<BMS_HOME>\thirdparty\classes`
  * If you use !WebSphere
    * move `<BMS_HOME>\rdesk\appserver\weblogic\remotedesktop\remotedesktop\*.*` to `<BMS_HOME>\rdesk\appserver\websphere\remotedesktop.ear\remotedesktop.war`
    * Edit `<BMS_HOME\appserver\websphere\config\cells\WASDevCell\nodes\WASDevNode\servers\WASDevServer\libraries.xml` and add JExcelApi to thirdparty libraries:
```
  <libraries:Library xmi:id="Library_2" name="BMSThirdParty">
    <classPath>${BMS_HOME}/thirdparty/classes/jxl.jar</classPath>
    ...
  </libraries:Library>
```
  * Restart Rdesk
  * Login to Rdesk with your Rdesk user and check there is `Excel Loader` tab

# Usage

## RemoteDesktop
Business users must have the privilege `ExcelLoaderManagerAccess` to have access to the `Excel Loader` tab inside !RemoteDesktop.

![bmexcel1](/bmexcel/wiki/images/bmexcel1.png)
_Figure 1-BM Excel Loader import page_

![bmexcel2](/bmexcel/wiki/images/bmexcel2.png)
_Figure 2-BM Excel Loader import result page_

## Command line
It is not the primary usage of BM Excel but it supports import by command line with the following parameters:

|Argument|Description|
|--------|-----------|
|-u|Excel loader user. He must have the privilege `ExcelLoaderManagerAccess`.|
|-p|Excel loader user password|
|-env|Environment name|
|-type|Excel file type|
|-file|File to upload|

i.e.:
```
bms excelloader -u susan -p martini -env BPADev -type BPA_UPLOAD_DEMO -file BPA_UPLOAD_DEMO.xls
```


# Configuration
[import_excel.dna](/bmexcel/src/main/bms/core/config/excel_loader/import_excel.dna) is the configuration file of BM Excel.

## General settings
Here are the general settings of the import:

|Excel loader settings|Required|Default|Description|
|---------------------|--------|-------|-----------|
|enabled|No|true|Not used in this version|
|max_lines|No|-1|If the spreadsheet contains more line than the maximum, it is not imported.|
|max_failed_lines|No|100|After x error lines, the import is stopped|
|Batch_size|No|100|The lines are imported by batch. This setting indicates the size of the batch.|
|import_lock|No|false|If enabled, only one import can be done simultaneously.|

## File type
For each spreadsheet template you provide to the business users, you have to add a DNA with the following properties: 

|File type settings| Required | Default | Description |
|------------------|----------|---------|-------------|
|description | Yes | - | File type description |
|sheet_nb | No | 0 | For Excel file with multiple sheets, you can indicate which one to load. |
|header_line_nb | No | 1 | The line that contains the header. |
|first_data_line_nb | No | 2 | The first line where data begins. |
|dnaUploadTime | No| No | In case, you want to limit the load to a specific time window. |
|process | Yes | - | The process DNA list defines the list of the processor to execute for each line (see record processors section) |

## Record processors

Here are the default record processors:
  * [ExcelProductRecordProcessor](bmexcel/src/main/java/com/bluemartini/loader/excel/record/ExcelProductRecordProcessor.java)
  * [ExcelObjectAttributeRecordProcessor](bmexcel/src/main/java/com/bluemartini/loader/excel/record/ExcelObjectAttributeRecordProcessor.java)
  * [ExcelObjectPriceRecordProcessor](bmexcel/src/main/java/com/bluemartini/loader/excel/record/ExcelObjectPriceRecordProcessor.java)
  * [ExcelMediaUploadRecordProcessor](bmexcel/src/main/java/com/bluemartini/loader/excel/record/ExcelMediaUploadRecordProcessor.java)
  * [ExcelBMIRecordProcessor](bmexcel/src/main/java/com/bluemartini/loader/excel/record/ExcelBMIRecordProcessor.java)
  * [ExcelStartWorkflowRecordProcessor](bmexcel/src/main/java/com/bluemartini/loader/excel/record/ExcelStartWorkflowRecordProcessor.java)

# High Level Design

## Java Excel API library
We use Java Excel API (a.k.a. JXL API) allows users to read, write, create, and modify sheets in an Excel (.xls) workbook at runtime. It doesn't support .xlsx format.

## Code sequence flow

```
com.bluemartini.remotedesktop.htmlapp.RemoteDesktopExcelUpload.execute()
#or
com.bluemartini.tools.BMExcelLoader.process()
  -> ExcelImportManager.<init>
      -> ExcelImportConfig.<init>
-> ExcelImportManager.parseFile()
  -> ExcelParser.<init>
  -> ExcelParser.getRowsSize()
   
  # for each line batch
  -> ExcelParser.getLines()
  -> ExcelImportManager.processLines()
      #for each lines
      -> ExcelLineProcessor.<init>
          -> ExcelImportConfig.getProcess();
      -> ExcelLineProcessor.parseLine()
          #for each processors
          -> ExcelRecordProcessor.<init>
          -> ExcelRecordProcessor.setRecordValue()
          -> ExcelRecordProcessor.setRecordConfig()
          -> ExcelRecordProcessor.process()
           
  -> ExcelParser.close()
  -> ExcelImportManager.getSuccessfulLines()
  -> ExcelImportManager.getErrorLines()
```

## File type
See above.

## Record Processors

Here are the default record processors:
  * [ExcelProductRecordProcessor](bmexcel/src/main/java/com/bluemartini/loader/excel/record/ExcelProductRecordProcessor.java)
  * [ExcelObjectAttributeRecordProcessor](bmexcel/src/main/java/com/bluemartini/loader/excel/record/ExcelObjectAttributeRecordProcessor.java)
  * [ExcelObjectPriceRecordProcessor](bmexcel/src/main/java/com/bluemartini/loader/excel/record/ExcelObjectPriceRecordProcessor.java)
  * [ExcelMediaUploadRecordProcessor](bmexcel/src/main/java/com/bluemartini/loader/excel/record/ExcelMediaUploadRecordProcessor.java)
  * [ExcelBMIRecordProcessor](bmexcel/src/main/java/com/bluemartini/loader/excel/record/ExcelBMIRecordProcessor.java)
  * [ExcelStartWorkflowRecordProcessor](bmexcel/src/main/java/com/bluemartini/loader/excel/record/ExcelStartWorkflowRecordProcessor.java)

To implement your own processor, you need to implement [ExcelProductRecordProcessor](bmexcel/src/main/java/com/bluemartini/loader/excel/record/ExcelRecordProcessor.java).

## Parser
The objective here is to support different file types.

[Parser](bmexcel/src/main/java/com/bluemartini/loader/excel/parser/Parser.java) is an interface with the following methods that needs to be implemented:
- getLines()
- getRowsSize()
- close()

By default BMExcel supports the following providers:
- [CSVParser](bmexcel/src/main/java/com/bluemartini/loader/excel/parser/CSVParser.java)
- [ExcelParser](bmexcel/src/main/java/com/bluemartini/loader/excel/parser/ExcelParser.java)
- [XMLParser](bmexcel/src/main/java/com/bluemartini/loader/excel/parser/XMLParser.java)

## ExcelLineProcessingException
By throwing an [ExcelLineProcessingException](bmexcel/src/main/java/com/bluemartini/loader/excel/record/ExcelLineProcessingException.java) in your processor, you can specify a comprehensive business error.

Here is an example:
```
throw new ExcelLineProcessingException("Product code is missing");
```
