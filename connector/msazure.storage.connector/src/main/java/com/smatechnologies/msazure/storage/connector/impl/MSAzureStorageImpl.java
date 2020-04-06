package com.smatechnologies.msazure.storage.connector.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobItem;
import com.smatechnologies.msazure.storage.arguments.ConnectorArguments;
import com.smatechnologies.msazure.storage.config.ConnectorConfig;
import com.smatechnologies.msazure.storage.interfaces.ConnectorConstants;
import com.smatechnologies.msazure.storage.interfaces.IMSAzureStorage;

public class MSAzureStorageImpl implements IMSAzureStorage {

	private static final String StorageAccountDeleteContainerNoMatchingContainersMsg = "containerdelete : no matching containers found for pattern ({0})";
	private static final String StorageAccountDeleteFileNoMatchingFilesMsg = "filedelete : no matching files found for pattern ({0}) in container ({1})";
	private static final String StorageAccountUploadingFileMsg = "Uploading file ({0}) to Blob storage as blob ({1})";
	private static final String StorageAccountFileUploadNoMatchingFilesMsg = "fileupload : no matching files found for pattern ({0}) in directory ({1})";
	private static final String StorageAccountFileDownloadNoMatchingFilesMsg = "filedownload : no matching files found for pattern ({0}) in container ({1})";

	private static final String SeparatorLine =                "----------------------------------------------------------------------------";
	private static final String StorageContainerHeaderLine =   "Container : {0} ------------------------------------------------------------";
	private static final String StorageContainerBlobLine =     "Blob      : {0}";
	private static final String StorageContainerBlobItemLine = " : BlobItem     : name ({0})   type ({1})   timestamp ({2})";

	private final static Logger LOG = LoggerFactory.getLogger(MSAzureStorageImpl.class);
	private static ConnectorConfig _ConnectorConfig = ConnectorConfig.getInstance();
	
	private static final String AZURE_ERROR_EMPTY_BODY = "empty body";
	private static final String AZURE_ERROR_BLOB_DOES_NOT_EXIST = "blob does not exist";
	
	private static final String ALL = "ALL"; 
	
	public List<String> getContainerList(
			ConnectorArguments _ConnectorArguments,
			String connectionString
			) throws Exception {
		
		List<String> containerNames = new ArrayList<String>();
		String pattern = null;
		
		try{
			pattern = _ConnectorArguments.getContainerName();
	       	if(_ConnectorConfig.isDebug()) {	
	       		LOG.info("DEBUG : Get list of containers in storage account (" + _ConnectorArguments.getStorageAccount() + ") Container Argument (" + pattern + ")");
	       	}
	    	// Create a BlobServiceClient object which will be used to create a container client
	    	BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
	    	// List the containers in the storage account.
	    	for (BlobContainerItem blobContainerItem : blobServiceClient.listBlobContainers()) {
		       	if(pattern.equalsIgnoreCase(ALL)) {
		       		// if *, add all names
		    		containerNames.add(blobContainerItem.getName());
			       	if(_ConnectorConfig.isDebug()) {	
			       		LOG.info("DEBUG : Adding container (" + blobContainerItem.getName() + ") to list");
			       	}
		       	} else {
		       		// otherwise add matching names
	   				if(FilenameUtils.wildcardMatch(blobContainerItem.getName(), pattern)) {
			    		containerNames.add(blobContainerItem.getName());
				       	if(_ConnectorConfig.isDebug()) {	
				       		LOG.info("DEBUG : Adding container (" + blobContainerItem.getName() + ") to list");
				       	}
	   				}

		       	}
	    	}
		} catch (Exception ex) {
			throw new Exception(ex);
		}
		return containerNames;
	}	// END : getContainerList

	public List<String> getContainerBlobList(
			ConnectorArguments _ConnectorArguments,
			String connectionString
			) throws Exception {
		
		List<String> output = new ArrayList<String>();
		String containerPattern = null;
		String blobPattern = null;
		int maxLength = SeparatorLine.length();
		
		try{
			DateTimeFormatter displayBlobCreatedTimeFormatter = DateTimeFormatter.ofPattern(ConnectorConstants.LOG_DATE_TIME_FORMAT); 
			containerPattern = _ConnectorArguments.getContainerName();
			blobPattern = _ConnectorArguments.getFileName();
			output.add(SeparatorLine);
	       	if(_ConnectorConfig.isDebug()) {	
	       		LOG.info("DEBUG : Get list of blobs (" + blobPattern + ") in container (" + containerPattern + ") for storage account (" + _ConnectorArguments.getStorageAccount() + ")");
	       	}
	    	// Create a BlobServiceClient object which will be used to create a container client
	    	BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
	    	// get matching containers
	    	for (BlobContainerItem blobContainerItem : blobServiceClient.listBlobContainers()) {
		       	if(containerPattern.equalsIgnoreCase(ALL)) {
			    	// get all containers
		       		String containerHeader = MessageFormat.format(StorageContainerHeaderLine, blobContainerItem.getName());
		       		if(containerHeader.length() > maxLength) {
			       		output.add(containerHeader.substring(0,maxLength));
		       		} else {
			       		output.add(containerHeader);
		       		}
			    	BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(blobContainerItem.getName().toLowerCase());
			    	for (BlobItem blobItem : containerClient.listBlobs()) {
			    		if(blobPattern.equalsIgnoreCase(ALL)) {
				       		// get all blobs
				    		if(_ConnectorConfig.isDebug()) {	
				    			LOG.info("DEBUG : Adding blob (" + blobItem.getName() + ") to container (" + blobContainerItem.getName() + ") list");
					       	}
				    		output.add(MessageFormat.format(StorageContainerBlobLine, blobItem.getName()));
			    		} else {
			    			// get matching blobs
			   				if(FilenameUtils.wildcardMatch(blobItem.getName(), blobPattern)) {
					    		if(_ConnectorConfig.isDebug()) {	
					    			LOG.info("DEBUG : Adding blob (" + blobItem.getName() + ") to container (" + blobContainerItem.getName() + ") list");
						       	}
					    		OffsetDateTime display = blobItem.getProperties().getCreationTime();
					    		output.add(MessageFormat.format(StorageContainerBlobItemLine, blobItem.getName(),
					    				blobItem.getProperties().getBlobType().name(),
					    				displayBlobCreatedTimeFormatter.format(display)));
			   				}
			    		}
			    	}
			    	
		       	} else {
		       		// otherwise add matching names
		       		// get matching containers
	   				if(FilenameUtils.wildcardMatch(blobContainerItem.getName(), containerPattern)) {
			       		String containerHeader = MessageFormat.format(StorageContainerHeaderLine, blobContainerItem.getName());
			       		if(containerHeader.length() > maxLength) {
				       		output.add(containerHeader.substring(0,maxLength));
			       		} else {
				       		output.add(containerHeader);
			       		}
			       		// get matching blobs
				    	BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(blobContainerItem.getName().toLowerCase());
				    	for (BlobItem blobItem : containerClient.listBlobs()) {
				    		if(blobPattern.equalsIgnoreCase(ALL)) {
					       		// get all blobs
					    		if(_ConnectorConfig.isDebug()) {	
					    			LOG.info("DEBUG : Adding blob (" + blobItem.getName() + ") to container (" + blobContainerItem.getName() + ") list");
						       	}
					    		output.add(MessageFormat.format(StorageContainerBlobLine, blobItem.getName()));
				    		} else {
				    			// get matching blobs
				   				if(FilenameUtils.wildcardMatch(blobItem.getName(), blobPattern)) {
						    		if(_ConnectorConfig.isDebug()) {	
						    			LOG.info("DEBUG : Adding blob (" + blobItem.getName() + ") to container (" + blobContainerItem.getName() + ") list");
							       	}
						    		output.add(MessageFormat.format(StorageContainerBlobLine, blobItem.getName()));
				   				}
				    		}
				    	}
	   				}
		       	}
	    	}
			output.add(SeparatorLine);
		} catch (Exception ex) {
			throw new Exception(ex);
		}
		return output;
	}	// END : getContainerBlobList

	public boolean createContainer(
			ConnectorArguments _ConnectorArguments,
			String connectionString
			) throws Exception {
		
		boolean success = false;
		
		try{
	       	if(_ConnectorConfig.isDebug()) {	
	       		LOG.info("DEBUG : Create container (" + _ConnectorArguments.getContainerName() + ") in storage account (" + _ConnectorArguments.getStorageAccount() + ")");
	       	}
	    	// Create a BlobServiceClient object which will be used to create a container client
	    	BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
	    	// Create the container and return success
	    	blobServiceClient.createBlobContainer(_ConnectorArguments.getContainerName());
	    	success = true;
		} catch (Exception ex) {
			throw new Exception(ex);
		}
		return success;
	}	// END : createContainer

	public boolean deleteContainer(
			ConnectorArguments _ConnectorArguments,
			String connectionString
			) throws Exception {
		
		boolean success = false;
		List<String> containerList = new ArrayList<String>();
		
		try{
			containerList.clear();
	       	if((_ConnectorArguments.getContainerName().contains(ConnectorConstants.STAR_CHARACTER)) ||
	       			(_ConnectorArguments.getContainerName().contains(ConnectorConstants.QUESTION_MARK_CHARACTER))){
	       		// we have wildcards, so get item list from container
	       		List<String> containers = getContainerList(_ConnectorArguments, connectionString);
	       		if(!containers.isEmpty()) {
	       			for(String container : containers) {
	       				if(FilenameUtils.wildcardMatch(container, _ConnectorArguments.getFileName())) {
	       					containerList.add(container);
	       				}
	       			}
	       		}
	       	} else {
				containerList.add(_ConnectorArguments.getContainerName());
	       	}
	    	// Create a BlobServiceClient object which will be used to create a container client
	    	BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
	    	// get the delete the containers
	    	if(!containerList.isEmpty()) {
	    		for(String containername : containerList) {
	    			if(_ConnectorConfig.isDebug()) {	
	    	       		LOG.info("DEBUG : Delete container (" + _ConnectorArguments.getContainerName() + ") in storage account (" + _ConnectorArguments.getStorageAccount() + ")");
	    	       	}
	    	    	// get the container client object and delete it
	    	    	BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containername);
	    	    	containerClient.delete();
	    		}	    		
	    	} else {
	       		LOG.info(MessageFormat.format(StorageAccountDeleteContainerNoMatchingContainersMsg, _ConnectorArguments.getContainerName()));
	    	}
	    	success = true;
		} catch (Exception ex) {
			throw new Exception(ex);
		}
		return success;
	}	// END : deleteContainer

	public boolean deleteFile(
			ConnectorArguments _ConnectorArguments,
			String connectionString
			) throws Exception {
		
		boolean success = false;
		List<String> fileList = new ArrayList<String>();
		
		try{
			fileList.clear();
	       	if((_ConnectorArguments.getFileName().contains(ConnectorConstants.STAR_CHARACTER)) ||
	       			(_ConnectorArguments.getFileName().contains(ConnectorConstants.QUESTION_MARK_CHARACTER))){
	       		// we have wildcards, so get item list from container
	       		List<String> items = getContainerBlobList(_ConnectorArguments, connectionString);
	       		if(!items.isEmpty()) {
	       			for(String item : items) {
	       				if(FilenameUtils.wildcardMatch(item, _ConnectorArguments.getFileName())) {
	       					fileList.add(item);
	       				}
	       			}
	       		}
	       	} else {
				fileList.add(_ConnectorArguments.getFileName());
	       	}
	    	// Create a BlobServiceClient object which will be used to create a container client
	    	BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
	    	// get the container client object and delete it
	    	BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(_ConnectorArguments.getContainerName());
	    	if(!fileList.isEmpty()) {
	    		for(String filename : fileList) {
			       	if(_ConnectorConfig.isDebug()) {	
			       		LOG.info("DEBUG : Delete file (" + filename + ") from container (" + _ConnectorArguments.getContainerName() + ") in storage account (" + _ConnectorArguments.getStorageAccount() + ")");
			       	}
			    	// Get a reference to a blob
					BlobClient blobClient = containerClient.getBlobClient(filename);
					blobClient.delete();
	    		}	    		
	    	} else {
	       		LOG.info(MessageFormat.format(StorageAccountDeleteFileNoMatchingFilesMsg, _ConnectorArguments.getFileName(),_ConnectorArguments.getContainerName()));
	    		
	    	}
	    	success = true;
		} catch (com.azure.storage.blob.models.BlobStorageException azex) {
			if(azex.getMessage().contains(AZURE_ERROR_BLOB_DOES_NOT_EXIST)) {
	       		LOG.error(MSAzureStorageImpl.class.getSimpleName(),MessageFormat.format(StorageAccountDeleteFileNoMatchingFilesMsg, _ConnectorArguments.getFileName(), _ConnectorArguments.getContainerName()));
	       		return false;
			} else {
				throw new Exception(azex);
			}
		} catch (Exception ex) {
			throw new Exception(ex);
		}
		return success;
	}	// END : deleteFile

	public boolean upLoadFile(
			ConnectorArguments _ConnectorArguments,
			String connectionString
			) throws Exception {
		
		boolean success = false;
		List<String> fileList = new ArrayList<String>();
		
		try{
	       	if((_ConnectorArguments.getFileName().contains(ConnectorConstants.STAR_CHARACTER)) ||
	       			(_ConnectorArguments.getFileName().contains(ConnectorConstants.QUESTION_MARK_CHARACTER))){
	       		// we have wildcards, so get item list from container
	       		fileList = getMatchingFiles(_ConnectorArguments.getDirectoryName(), _ConnectorArguments.getFileName());
	       	} else {
				fileList.add(_ConnectorArguments.getFileName());
	       	}
	    	// Create a BlobServiceClient object which will be used to create a container client
	    	BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
	    	// get the container client object
	    	BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(_ConnectorArguments.getContainerName());
	    	if(!fileList.isEmpty()) {
	    		for(String filename : fileList) {
	    			String fullFileName = _ConnectorArguments.getDirectoryName() + File.separator + filename;
	    	       	if(_ConnectorConfig.isDebug()) {	
	    	       		LOG.info("DEBUG : Upload File (" + fullFileName + ") into container (" + _ConnectorArguments.getContainerName() + ") in storage account (" + _ConnectorArguments.getStorageAccount() + ")");
	    	       	}
		    	// Get a reference to a blob
				BlobClient blobClient = containerClient.getBlobClient(_ConnectorArguments.getFileName());
	       		LOG.info(MessageFormat.format(StorageAccountUploadingFileMsg, fullFileName, blobClient.getBlobUrl()));
				// Upload the file
				blobClient.uploadFromFile(fullFileName, _ConnectorArguments.isUploadFileOverwrite());
	    		}
	    	} else {
	       		LOG.error(MSAzureStorageImpl.class.getSimpleName(),MessageFormat.format(StorageAccountFileUploadNoMatchingFilesMsg, _ConnectorArguments.getFileName(),_ConnectorArguments.getDirectoryName()));
	    		
	    	}
	    	success = true;
		} catch (Exception ex) {
			throw new Exception(ex);
		}
		return success;
	}	// END : upLoadFile

	public boolean downLoadFile(
			ConnectorArguments _ConnectorArguments,
			String connectionString
			) throws Exception {
		
		boolean success = false;
		List<String> fileList = new ArrayList<String>();
		String fullFileName = null;
		
		try{
			fileList.clear();
	       	if((_ConnectorArguments.getFileName().contains(ConnectorConstants.STAR_CHARACTER)) ||
	       			(_ConnectorArguments.getFileName().contains(ConnectorConstants.QUESTION_MARK_CHARACTER))){
	       		// we have wildcards, so get item list from container
	       		List<String> items = getContainerBlobList(_ConnectorArguments, connectionString);
	       		if(!items.isEmpty()) {
	       			for(String item : items) {
	       				if(FilenameUtils.wildcardMatch(item, _ConnectorArguments.getFileName())) {
	       					fileList.add(item);
	       				}
	       			}
	       		}
	       	} else {
				fileList.add(_ConnectorArguments.getFileName());
	       	}
	    	// Create a BlobServiceClient object which will be used to create a container client
	    	BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
	    	// get the container client object
	    	BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(_ConnectorArguments.getContainerName());
	    	if(!fileList.isEmpty()) {
	    		for(String filename : fileList) {
					fullFileName = _ConnectorArguments.getDirectoryName() + File.separator + filename;
					if(_ConnectorConfig.isDebug()) {	
			       		LOG.info("DEBUG : Download File (" + fullFileName + ") from container (" + _ConnectorArguments.getContainerName() + ") in storage account (" + 
			       				_ConnectorArguments.getStorageAccount() + ")");
			       	}
			    	// Get a reference to a blob
					BlobClient blobClient = containerClient.getBlobClient(filename);
					// Download the file
					blobClient.downloadToFile(fullFileName);
	    		}	    		
		    	success = true;
	    	} else {
	       		LOG.info(MessageFormat.format(StorageAccountFileDownloadNoMatchingFilesMsg, _ConnectorArguments.getFileName(), _ConnectorArguments.getContainerName()));
		    	success = false;
	    	}
		} catch (com.azure.storage.blob.models.BlobStorageException azex) {
			if(azex.getMessage().contains(AZURE_ERROR_EMPTY_BODY)) {
	       		LOG.error(MessageFormat.format(StorageAccountFileDownloadNoMatchingFilesMsg, _ConnectorArguments.getFileName(), _ConnectorArguments.getContainerName()));
				// delete the file if it exists
	       		deleteFile(fullFileName);
	       		return false;
			} else {
				throw new Exception(azex);
			}
		} catch (Exception ex) {
			throw new Exception(ex);
		}
		return success;
	}	// END : downLoadFile

	private List<String> getMatchingFiles(
			String directory,
			String pattern
			) throws Exception {
		
		List<String> filenames = new ArrayList<String>();
		
		try {
			File folder = new File(directory);
			File[] files = folder.listFiles();
	        for (File file : files) {
   				if(FilenameUtils.wildcardMatch(pattern, file.getName())) {
   					filenames.add(file.getName());
   				}
	        }
		} catch (Exception ex) {
			throw new Exception(ex);
		}
		return filenames;
	}	// END : getMatchingFiles
		
	private void deleteFile(
			String fileName
			) throws Exception {

		try {
			Path path = Paths.get(fileName);
			Files.deleteIfExists(path);
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}	// END : deleteFile

}
