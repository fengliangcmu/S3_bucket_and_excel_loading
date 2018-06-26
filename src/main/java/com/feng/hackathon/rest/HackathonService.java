package com.feng.hackathon.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feng.hackathon.utils.Log;

@Path("/hackathonService")
public class HackathonService extends BaseService {

	// upload excel file and save into db via odata service
	// URL : localhost:8080/rest/hackathonService/uploadExcelData
	// form-data in request body:  uploadFile(file) & odataUrl
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/uploadExcelData")
	public Response insertOdata(@FormDataParam("odataUrl") String odataUrl,
			@FormDataParam("uploadFile") InputStream fileInputStream,
			@FormDataParam("uploadFile") FormDataContentDisposition fileFormDataContentDisposition) {
		Log.enter("### Trying to upload file : " + fileFormDataContentDisposition.getFileName());
		String outMessageOk = "### data inserted into sales table via odata service";
		String outMessageError = "###Data Insertion has an issue. One or more entries are not inserted. ";
		String notHandledItems = "Following items failed: ";
		Boolean errorFlag = false;

		Workbook workbook = null;
		URL obj = null;
		HttpsURLConnection con = null;
		if (!isUrlValid(odataUrl)) {
			return ok("Your Odata URL is not valid, nothing is done.");
		}

		String name = fileFormDataContentDisposition.getFileName();
		int idx = name.lastIndexOf('.');
		if (!(name.substring(idx + 1, name.length()).equals("xlsx")
				|| name.substring(idx + 1, name.length()).equals("xls"))) {
			return ok("File is not excel format, nothing is done.");
		}

		try {

			// obj = new
			// URL("https://odata_v2.hostname/xsodata/sales.xsodata/sales");
			obj = new URL(odataUrl);
			File tempFile = File.createTempFile(name.substring(0, idx), name.substring(idx + 1, name.length()));
			tempFile.deleteOnExit();
			FileOutputStream out = new FileOutputStream(tempFile);
			IOUtils.copy(fileInputStream, out);

			FileInputStream file = new FileInputStream(tempFile);
			workbook = new XSSFWorkbook(file);
			Sheet sheet = workbook.getSheetAt(0);

			// handle first row as header row
			Row headerRow = sheet.getRow(0);
			HashMap<Integer, String> columnIdxMap = new HashMap<Integer, String>();
			for (Cell cell : headerRow) {
				columnIdxMap.put(cell.getColumnIndex(), getCellValue(cell));
			}

			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				// make request body
				String json = buildJsonString(sheet.getRow(i), columnIdxMap);
				// force container to use this version to talk to odata server
				// via https
				// System.setProperty("https.protocols",
				// "TLSv1,TLSv1.1,TLSv1.2");
				System.setProperty("https.protocols", "TLSv1.2");
				// our odata api only support insertion 1 by 1, so needs to open
				// multiple times.
				con = (HttpsURLConnection) obj.openConnection();
				con.setRequestMethod("POST");
				con.setDoOutput(true);
				con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

				OutputStream os = con.getOutputStream();
				os.write(json.getBytes());
				os.flush();
				int responseCode = con.getResponseCode();
				if (!(responseCode == 200 || responseCode == 201)) {
					errorFlag = true;
					notHandledItems = notHandledItems + "ResponseCode :" + responseCode + ". Item info : " + json
							+ ". Error message : " + con.getResponseMessage() + "\\n";
				}
				con.disconnect(); // may be reused but just want to close now.
									// Also it the odata api we have only
									// supports single line insertion.
			}

		} catch (Exception e) {
			handleException(e);
		} finally {
			Log.exit();
			if (workbook != null) {
				try {
					workbook.close();
				} catch (Exception e) {
					handleException(e);
				}
			}
		}

		if (errorFlag) {
			return ok(outMessageError + notHandledItems);
		}
		return ok(outMessageOk);
	}

	private boolean isUrlValid(String url) {
		/* Try creating a valid URL */
		try {
			new URL(url).toURI();
			return true;
		}

		// If there was an Exception
		// while creating URL object
		catch (Exception e) {
			return false;
		}
	}

	private String getCellValue(Cell cell) {
		switch (cell.getCellTypeEnum()) {
		case STRING:
			return cell.getStringCellValue();
		case NUMERIC:
			return String.valueOf(cell.getNumericCellValue());
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		default:
			return "";
		}
	}

	private String buildJsonString(Row row, HashMap<Integer, String> columnIdxMap) {
		StringBuilder rBody = new StringBuilder();
		rBody.append("{");
		for (int i = 0; i < row.getLastCellNum(); i++) {
			rBody.append("\"" + columnIdxMap.get(i) + "\"" + ":");
			rBody.append("\"" + getCellValue(row.getCell(i)) + "\"");
			if (i != row.getLastCellNum() - 1)
				rBody.append(",");
		}
		rBody.append("}");
		return rBody.toString();
	}

	// download any file from S3 bucket
	// URL : localhost:8080/rest/hackathonService/downloadFile
	// form-data in request body:  fileName, bucket, s3AccessKeyId, s3SecretAccessKey, s3Region
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	// @Produces("application/vnd.ms-excel")
	@Path("/downloadFile")
	public Response downloadFile(@FormDataParam("fileName") String fileName, @FormDataParam("bucket") String bucket,
			@FormDataParam("s3AccessKeyId") String s3AccessKeyId,
			@FormDataParam("s3SecretAccessKey") String s3SecretAccessKey, @FormDataParam("s3Region") String s3Region) {

		ResponseBuilder response = null;
		String mimeType;
		try {
			HashMap<String, String> s3Info = getS3Credentials(bucket, s3AccessKeyId, s3SecretAccessKey, s3Region);
			AWSCredentials credentials = new BasicAWSCredentials(s3Info.get("S3_ACCESS_KEY_ID"),
					s3Info.get("S3_SECRET_ACCESS_KEY"));
			AmazonS3 s3client = AmazonS3ClientBuilder.standard()
					.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(s3Info.get("S3_REGION"))
					.build();
			S3Object s3object = s3client.getObject(s3Info.get("S3_BUCKET"), fileName);
			S3ObjectInputStream inputStream = s3object.getObjectContent();
			File tempFile = new File(fileName);
			FileUtils.copyInputStreamToFile(inputStream, tempFile);

			mimeType = new MimetypesFileTypeMap().getContentType(tempFile);
			response = Response.ok((Object) tempFile, mimeType);
			response.header("Content-Disposition", "attachment; filename=" + fileName);
		} catch (Exception e) {
			handleException(e);
		} finally {
			Log.exit();
		}
		return response.build();
	}

	// upload any file from S3 bucket
	// URL : localhost:8080/rest/hackathonService/uploadFile
	// form-data in request body:  uploadFile(file type), bucket, s3AccessKeyId, s3SecretAccessKey, s3Region
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/uploadFile")
	public Response uploadFile(@FormDataParam("bucket") String bucket,
			@FormDataParam("s3AccessKeyId") String s3AccessKeyId,
			@FormDataParam("s3SecretAccessKey") String s3SecretAccessKey, @FormDataParam("s3Region") String s3Region,
			@FormDataParam("uploadFile") InputStream fileInputStream,
			@FormDataParam("uploadFile") FormDataContentDisposition fileFormDataContentDisposition) {
		Log.enter("### Trying to upload file : " + fileFormDataContentDisposition.getFileName());
		try {
			HashMap<String, String> s3Info = getS3Credentials(bucket, s3AccessKeyId, s3SecretAccessKey, s3Region);
			AWSCredentials credentials = new BasicAWSCredentials(s3Info.get("S3_ACCESS_KEY_ID"),
					s3Info.get("S3_SECRET_ACCESS_KEY"));
			AmazonS3 s3client = AmazonS3ClientBuilder.standard()
					.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(s3Info.get("S3_REGION"))
					.build();

			String name = fileFormDataContentDisposition.getFileName();
			int idx = name.lastIndexOf('.');
			File tempFile = File.createTempFile(name.substring(0, idx), name.substring(idx + 1, name.length()));
			tempFile.deleteOnExit();
			FileOutputStream out = new FileOutputStream(tempFile);
			IOUtils.copy(fileInputStream, out);
			s3client.putObject(s3Info.get("S3_BUCKET"), fileFormDataContentDisposition.getFileName(), tempFile);

		} catch (Exception e) {
			handleException(e);
		} finally {
			Log.exit();
		}
		return ok("### uploaded file: " + fileFormDataContentDisposition.getFileName());
	}

	// delete any file from S3 bucket
	// URL : localhost:8080/rest/hackathonService/deleteFile
	// form-data in request body:  fileName, bucket, s3AccessKeyId, s3SecretAccessKey, s3Region
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/deleteFile")
	public Response deleteFile(@FormDataParam("fileName") String fileName, @FormDataParam("bucket") String bucket,
			@FormDataParam("s3AccessKeyId") String s3AccessKeyId,
			@FormDataParam("s3SecretAccessKey") String s3SecretAccessKey, @FormDataParam("s3Region") String s3Region) {
		Log.enter("### Tring to delete file: " + fileName);
		try {

			HashMap<String, String> s3Info = getS3Credentials(bucket, s3AccessKeyId, s3SecretAccessKey, s3Region);
			AWSCredentials credentials = new BasicAWSCredentials(s3Info.get("S3_ACCESS_KEY_ID"),
					s3Info.get("S3_SECRET_ACCESS_KEY"));
			AmazonS3 s3client = AmazonS3ClientBuilder.standard()
					.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(s3Info.get("S3_REGION"))
					.build();
			String objkeyArr[] = { fileName };

			DeleteObjectsRequest delObjReq = new DeleteObjectsRequest(s3Info.get("S3_BUCKET")).withKeys(objkeyArr);
			s3client.deleteObjects(delObjReq);
		} catch (Exception e) {
			handleException(e);
		} finally {
			Log.exit();
		}
		return ok("File deleted: " + fileName);
	}

	private HashMap<String, String> getS3Credentials(String bucket, String s3AccessKeyId, String s3SecretAccessKey,
			String s3Region) {

		HashMap<String, String> res = new HashMap<String, String>();
		String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
		System.out.println("#@@@@@@@@@@@ VCAP_SERVICES" + VCAP_SERVICES);
		// get S3 bucket connection info from VCAP_SERVICES
		try {
			if (VCAP_SERVICES != null && (!VCAP_SERVICES.equals(""))) {
				JsonFactory factory = new JsonFactory();
				ObjectMapper mapper = new ObjectMapper(factory);
				JsonNode rootNode = mapper.readTree(VCAP_SERVICES);
				JsonNode credentialsNode = findNode(findNode(rootNode, "objectstore"), "credentials");
				JsonNode bucketNode = findNode(credentialsNode, "bucket");
				JsonNode access_key_idNode = findNode(credentialsNode, "access_key_id");
				JsonNode secret_access_keyNode = findNode(credentialsNode, "secret_access_key");
				JsonNode hostNode = findNode(credentialsNode, "host");
				String bucketNodeValue = bucketNode.asText();
				String access_key_idNodeValue = access_key_idNode.asText();
				String secret_access_keyNodeValue = secret_access_keyNode.asText();
				String hostNodeValue = hostNode.asText();
				int start = hostNodeValue.indexOf('-') + 1;
				int end = hostNodeValue.indexOf('.');
				hostNodeValue = hostNodeValue.substring(start, end);

				System.out.println("#@@@@@@@@@@@ bucketNodeValue" + bucketNodeValue);
				System.out.println("#@@@@@@@@@@@ access_key_idNodeValue" + access_key_idNodeValue);
				System.out.println("#@@@@@@@@@@@ secret_access_keyNodeValue" + secret_access_keyNodeValue);

				// no need for host, defined already on the top;

				if (!(bucketNodeValue == null || bucketNodeValue.equals("") || access_key_idNodeValue == null
						|| access_key_idNodeValue.equals("") || secret_access_keyNodeValue == null
						|| secret_access_keyNodeValue.equals(""))) {
					res.put("S3_BUCKET", bucketNodeValue);
					res.put("S3_ACCESS_KEY_ID", access_key_idNodeValue);
					res.put("S3_SECRET_ACCESS_KEY", secret_access_keyNodeValue);
					res.put("S3_REGION", hostNodeValue);
				}
			}

		} catch (Exception e) {
			handleException(e);
		} finally {
			if (!(s3AccessKeyId == null || s3AccessKeyId.equals("") || s3SecretAccessKey == null
					|| s3SecretAccessKey.equals("") || s3Region == null || s3Region.equals("") || bucket == null
					|| bucket.equals(""))) {
				res.put("S3_BUCKET", bucket);
				res.put("S3_ACCESS_KEY_ID", s3AccessKeyId);
				res.put("S3_SECRET_ACCESS_KEY", s3SecretAccessKey);
				res.put("S3_REGION", s3Region);
			}
		}
		return res;
	}

	private JsonNode findNode(JsonNode rootNode, String nodeName) {
		if (rootNode == null)
			return null;
		if (rootNode.isArray()) {
			return rootNode.findValue(nodeName);
		} else {
			Iterator<Map.Entry<String, JsonNode>> fieldsIterator = rootNode.fields();
			while (fieldsIterator.hasNext()) {
				Map.Entry<String, JsonNode> field = fieldsIterator.next();
				if (field.getKey().equals(nodeName)) {
					return field.getValue();
				}
			}
		}
		return null;
	}
}
