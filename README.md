# 📂 File Cabinet Information Retrieval API

## Overview
This SOAP API allows you to retrieve information from a specified file cabinet and returns the data in XML format. 

## Parameters
To use this API, you need to provide the following parameters:
- **DocuWare URL**: The URL of the DocuWare server.
- **Username**: Your DocuWare username.
- **Password**: Your DocuWare password.
- **File Cabinet ID**: The ID of the file cabinet you want to query.
- **Search Criteria**: The criteria to filter the search results.

## Usage
Once all the parameters are entered, the API will return information from the specified file cabinet based on your search criteria.

## Example Request
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:doc="http://www.docuware.com/">
   <soapenv:Header/>
   <soapenv:Body>
      <doc:GetFileCabinetInfo>
         <doc:DocuWareURL>https://your-docuware-url</doc:DocuWareURL>
         <doc:Username>your-username</doc:Username>
         <doc:Password>your-password</doc:Password>
         <doc:FileCabinetID>your-file-cabinet-id</doc:FileCabinetID>
         <doc:SearchCriteria>your-search-criteria</doc:SearchCriteria>
      </doc:GetFileCabinetInfo>
   </soapenv:Body>
</soapenv:Envelope>
```

## Example Response
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
   <soapenv:Body>
      <GetFileCabinetInfoResponse>
         <FileCabinetInfo>
            <!-- XML data here -->
         </FileCabinetInfo>
      </GetFileCabinetInfoResponse>
   </soapenv:Body>
</soapenv:Envelope>
```
