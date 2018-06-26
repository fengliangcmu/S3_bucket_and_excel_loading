# S3_bucket_and_excel_loading
###  restful services to show: 
#### 1. how to load excel file and insert into DB via odata service; 
#### 2. how to upload,delete,download files from Amazon S3 bucket.

1. upload excel file and save into db via odata service
URL : localhost:8080/rest/hackathonService/uploadExcelData
post form-data in request body:  uploadFile(file) & odataUrl
  
2. download any file from S3 bucket
URL : localhost:8080/rest/hackathonService/downloadFile
post form-data in request body:  fileName, bucket, s3AccessKeyId, s3SecretAccessKey, s3Region
  
3. upload any file from S3 bucket
URL : localhost:8080/rest/hackathonService/uploadFile
post form-data in request body:  uploadFile(file type), bucket, s3AccessKeyId, s3SecretAccessKey, s3Region
  
4. delete any file from S3 bucket
URL : localhost:8080/rest/hackathonService/deleteFile
post form-data in request body:  fileName, bucket, s3AccessKeyId, s3SecretAccessKey, s3Region
