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
import com.azure.storage.blob.models.BlobProperties;
import com.smatechnologies.msazure.storage.arguments.ConnectorArguments;
import com.smatechnologies.msazure.storage.config.ConnectorConfig;
import com.smatechnologies.msazure.storage.interfaces.IConstants;
import com.smatechnologies.msazure.storage.interfaces.IAzureStorage;
import com.smatechnologies.msazure.storage.modules.StorageInformation;

public class AzureStorageImpl implements IAzureStorage {

	private static final String StorageAccountDeleteContainerNoMatchingContainersMsg = "containerdelete : no matching containers found for pattern ({0})";
	private static final String StorageAccountDeleteFileNoMatchingFilesMsg = "filedelete : no matching files found for pattern ({0}) in container ({1})";
	private static final String StorageAccountUploadingFileMsg = "Uploading file ({0}) to Blob storage as blob ({1})";
	private static final String StorageAccountFileUploadNoMatchingFilesMsg = "fileupload : no matching files found for pattern ({0}) in directory ({1})";
	private static final String StorageAccountFileDownloadNoMatchingFilesMsg = "filedownload : no matching files found for pattern ({0}) in container ({1})";

	private static final String SeparatorLine =                "----------------------------------------------------------------------------";
	private static final String StorageContainerHeaderLine =   "Container : {0} ------------------------------------------------------------";
	private static final String StorageContainerBlobLine =     "Blob      : {0}";
	private static final String StorageContainerBlobItemLine = " : BlobItem     : name ({0})   type ({1})   timestamp ({2})";

	private final static Logger LOG = LoggerFactory.getLogger(AzureStorageImpl.class);
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

	public BlobProperties checkIfFileExists(
			ConnectorArguments _ConnectorArguments
			) throws Exception {

		String containerFileName = null;
		BlobProperties blobProperties = null;
		
		try{
			if(_ConnectorArguments.getContainerPath() != null) {
				containerFileName = _ConnectorArguments.getContainerPath() + IConstants.Characters.SLASH + _ConnectorArguments.getContainerFileName();
			} else {
				containerFileName = _ConnectorArguments.getContainerFileName();
			}
			LOG.info("Check if File (" + containerFileName + ") exists in container (" + _ConnectorArguments.getContainerName() + ") in storage account (" + _ConnectorArguments.getStorageAccount() + ")");
	       	// get connection String
        	StorageInformation storageInformation = new StorageInformation();
        	storageInformation.setName(_ConnectorArguments.getStorageAccount());
        	storageInformation.setConnection(_ConnectorArguments.getAccessKey());
	    	// Create a BlobServiceClient object which will be used to create a container client
	    	BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(storageInformation.getConnection()).buildClient();
	    	// get the container client object
	    	BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(_ConnectorArguments.getContainerName());
	    	// Get a reference to a blob
			BlobClient blobClient = containerClient.getBlobClient(containerFileName);
			blobProperties = blobClient.getProperties();
		} catch (com.azure.storage.blob.models.BlobStorageException azex) {
			System.out.println(azex.getMessage());
			if(azex.getMessage().contains(AZURE_ERROR_EMPTY_BODY)) {
				LOG.info("File (" + _ConnectorArguments.getContainerFileName() + ") not found in container (" + containerFileName + ") in storage account (" + _ConnectorArguments.getStorageAccount() + ")");
				return null;
			} else if(azex.getMessage().contains("BlobNotFound")) {
				return null;
			} else {
				throw new Exception(azex);
			}
		} catch (Exception ex) {
			throw new Exception(ex);
		}
		return blobProperties;
	}	// END : checkIfFileExists

	public BlobProperties checkIfWildCardFileExists(
			ConnectorArguments _ConnectorArguments
			) throws Exception {

		String containerFileName = null;
		BlobProperties blobPproperties = null;
		String blobPattern = null;
		String blobNameExists = null;
		
		try{
			if(_ConnectorArguments.getContainerPath() != null) {
				containerFileName = _ConnectorArguments.getContainerPath() + IConstants.Characters.SLASH + _ConnectorArguments.getContainerFileName();
			} else {
				containerFileName = _ConnectorArguments.getContainerFileName();
			}
			blobPattern = _ConnectorArguments.getContainerFileName();
			LOG.info("Check if File (" + containerFileName + ") exists in container (" + _ConnectorArguments.getContainerName() + ") in storage account (" + _ConnectorArguments.getStorageAccount() + ")");
	       	// get connection String
        	StorageInformation storageInformation = new StorageInformation();
        	storageInformation.setName(_ConnectorArguments.getStorageAccount());
        	storageInformation.setConnection(_ConnectorArguments.getAccessKey());
	    	// Create a BlobServiceClient object which will be used to create a container client
	    	BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(storageInformation.getConnection()).buildClient();
	    	// get the container client object
	    	BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(_ConnectorArguments.getContainerName());
	    	// Get the list of blobs in the container
	    	for (BlobItem blobItem : containerClient.listBlobs()) {
	    		if(_ConnectorArguments.getContainerPath() != null) {
	    			if(blobItem.getName().startsWith(_ConnectorArguments.getContainerPath())) {
	    				// extract blob name
		       			int lastForwardSlash = blobItem.getName().lastIndexOf(IConstants.Characters.SLASH);
		       			if(lastForwardSlash > -1) {
		       				String blobname = blobItem.getName().substring(lastForwardSlash + 1, blobItem.getName().length());
			   				if(FilenameUtils.wildcardMatch(blobname, blobPattern)) {
					    		if(_ConnectorConfig.isDebug()) {	
					    			LOG.info("DEBUG : Found blob (" + blobItem.getName() + ") in container (" + containerClient.getBlobContainerName() + ")");
						       	}
					    		blobNameExists = blobItem.getName();
					    		break;
			   				}
		       			}
	    			}
	    		} else {
    				// check for blob in root 
	    			if(!blobItem.getName().contains("/")) {
		   				if(FilenameUtils.wildcardMatch(blobItem.getName(), blobPattern)) {
				    		if(_ConnectorConfig.isDebug()) {	
				    			LOG.info("DEBUG : Found blob (" + blobItem.getName() + ") in container (" + containerClient.getBlobContainerName() + ")");
					       	}
				    		blobNameExists = blobItem.getName();
				    		break;
		   				}
    				}
    			}
	    	}
	    	if(blobNameExists != null) {
				BlobClient blobClient = containerClient.getBlobClient(blobNameExists);
				blobPproperties = blobClient.getProperties();
	    	}
		} catch (com.azure.storage.blob.models.BlobStorageException azex) {
			System.out.println(azex.getMessage());
			if(azex.getMessage().contains(AZURE_ERROR_EMPTY_BODY)) {
				LOG.info("File (" + _ConnectorArguments.getContainerFileName() + ") not found in container (" + containerFileName + ") in storage account (" + _ConnectorArguments.getStorageAccount() + ")");
				return null;
			} else if(azex.getMessage().contains("BlobNotFound")) {
				return null;
			} else {
				throw new Exception(azex);
			}
		} catch (Exception ex) {
			throw new Exception(ex);
		}
		return blobPproperties;
}	// END : checkIfFileExists

	public List<String> getContainerBlobList(
			ConnectorArguments _ConnectorArguments,
			String connectionString
			) throws Exception {
		
		List<String> output = new ArrayList<String>();
		String containerPattern = null;
		String blobPattern = null;
		int maxLength = SeparatorLine.length();
		
		try{
			DateTimeFormatter displayBlobCreatedTimeFormatter = DateTimeFormatter.ofPattern(IConstants.General.LOG_DATE_TIME_FORMAT); 
			containerPattern = _ConnectorArguments.getContainerName();
			blobPattern = _ConnectorArguments.getContainerFileName();
			output.add(SeparatorLine);
	       	if(_ConnectorConfig.isDebug()) {	
	       		if(_ConnectorArguments.getContainerPath() != null) {
	       			LOG.info("DEBUG : Get list of blobs (" + blobPattern + ") in container (" + containerPattern + ") path (" + _ConnectorArguments.getContainerPath() + ") for storage account (" + _ConnectorArguments.getStorageAccount() + ")");
	       		} else {
	       			LOG.info("DEBUG : Get list of blobs (" + blobPattern + ") in container (" + containerPattern + ") for storage account (" + _ConnectorArguments.getStorageAccount() + ")");
	       		}
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
			    		if(_ConnectorArguments.getContainerPath() != null) {
			    			if(blobItem.getName().startsWith(_ConnectorArguments.getContainerPath())) {
			    				// get all blobs in the path
					    		if(_ConnectorConfig.isDebug()) {	
					    			LOG.info("DEBUG : Adding blob (" + blobItem.getName() + ") to container (" + blobContainerItem.getName() + ") list");
						       	}
					    		output.add(MessageFormat.format(StorageContainerBlobLine, blobItem.getName()));
				       		} else {
			    				// check for specific blobs
				       			// get blob name
				       			int lastForwardSlash = blobItem.getName().lastIndexOf(IConstants.Characters.SLASH);
				       			if(lastForwardSlash > -1) {
				       				String blobname = blobItem.getName().substring(lastForwardSlash + 1, blobItem.getName().length());
					   				if(FilenameUtils.wildcardMatch(blobname, blobPattern)) {
							    		if(_ConnectorConfig.isDebug()) {	
							    			LOG.info("DEBUG : Adding blob (" + blobItem.getName() + ") to container (" + blobContainerItem.getName() + ") list");
								       	}
							    		output.add(MessageFormat.format(StorageContainerBlobLine, blobItem.getName()));
					   				}
				       			}
				       		}
			    		} else if(blobPattern.equalsIgnoreCase(ALL)) {
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
				    		if(_ConnectorArguments.getContainerPath() != null) {
				    			if(blobItem.getName().startsWith(_ConnectorArguments.getContainerPath())) {
						       		if(blobPattern.equalsIgnoreCase(ALL)) {
					    				// get all blobs in the path
							    		if(_ConnectorConfig.isDebug()) {	
							    			LOG.info("DEBUG : Adding blob (" + blobItem.getName() + ") to container (" + blobContainerItem.getName() + ") list");
								       	}
							    		output.add(MessageFormat.format(StorageContainerBlobLine, blobItem.getName()));
						       		} else {
					    				// check for specific blobs
						       			// get blob name
						       			int lastForwardSlash = blobItem.getName().lastIndexOf(IConstants.Characters.SLASH);
						       			if(lastForwardSlash > -1) {
						       				String blobname = blobItem.getName().substring(lastForwardSlash + 1, blobItem.getName().length());
							   				if(FilenameUtils.wildcardMatch(blobname, blobPattern)) {
									    		if(_ConnectorConfig.isDebug()) {	
									    			LOG.info("DEBUG : Adding blob (" + blobItem.getName() + ") to container (" + blobContainerItem.getName() + ") list");
										       	}
									    		output.add(MessageFormat.format(StorageContainerBlobLine, blobItem.getName()));
							   				}
						       			}
						       		}
				    			}
				    		} else if(blobPattern.equalsIgnoreCase(ALL)) {
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
	       	if((_ConnectorArguments.getContainerName().contains(IConstants.Characters.ASTERIX)) ||
	       			(_ConnectorArguments.getContainerName().contains(IConstants.Characters.QUESTION_MARK))){
	       		// we have wildcards, so get item list from container
	       		List<String> containers = getContainerList(_ConnectorArguments, connectionString);
	       		if(!containers.isEmpty()) {
	       			for(String container : containers) {
	       				if(FilenameUtils.wildcardMatch(container, _ConnectorArguments.getContainerFileName())) {
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
		String containerFileName = null;
		
		try{
			fileList.clear();
			if(_ConnectorArguments.getContainerPath() != null) {
				containerFileName = _ConnectorArguments.getContainerPath() + IConstants.Characters.SLASH + _ConnectorArguments.getContainerFileName();
			} else {
				containerFileName = _ConnectorArguments.getContainerFileName();
			}
			if((_ConnectorArguments.getContainerFileName().contains(IConstants.Characters.ASTERIX)) ||
	       			(_ConnectorArguments.getContainerFileName().contains(IConstants.Characters.QUESTION_MARK))){
	       		// we have wildcards, so get item list from container
	       		List<String> items = getContainerBlobListNames(_ConnectorArguments);
	       		if(!items.isEmpty()) {
	       			for(String item : items) {
	       				if(FilenameUtils.wildcardMatch(item, containerFileName)) {
	       					fileList.add(item);
	       				}
	       			}
	       		}
	       	} else {
				fileList.add(containerFileName);
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
	       		LOG.info(MessageFormat.format(StorageAccountDeleteFileNoMatchingFilesMsg, _ConnectorArguments.getContainerFileName(),_ConnectorArguments.getContainerName()));
	    		
	    	}
	    	success = true;
		} catch (com.azure.storage.blob.models.BlobStorageException azex) {
			if(azex.getMessage().contains(AZURE_ERROR_BLOB_DOES_NOT_EXIST)) {
	       		LOG.error(MessageFormat.format(StorageAccountDeleteFileNoMatchingFilesMsg, _ConnectorArguments.getContainerFileName(), _ConnectorArguments.getContainerName()));
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
	       	if((_ConnectorArguments.getLocalFileName().contains(IConstants.Characters.ASTERIX)) ||
	       			(_ConnectorArguments.getLocalFileName().contains(IConstants.Characters.QUESTION_MARK))){
	       		// we have wildcards, so get item list from container
	       		fileList = getMatchingFiles(_ConnectorArguments.getDirectoryName(), _ConnectorArguments.getLocalFileName());
	       	} else {
				fileList.add(_ConnectorArguments.getLocalFileName());
	       	}
	    	// Create a BlobServiceClient object which will be used to create a container client
	    	BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
	    	// get the container client object
	    	BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(_ConnectorArguments.getContainerName());
	    	if(!fileList.isEmpty()) {
	    		for(String filename : fileList) {
	    			String fullFileName = _ConnectorArguments.getDirectoryName() + File.separator + filename;
	    			String containerFileName = null;
	    			if(_ConnectorArguments.getContainerFileName() != null) {
	    				containerFileName = _ConnectorArguments.getContainerFileName();
	    			} else {
	    				containerFileName = filename;
	    			}
	    			if(_ConnectorArguments.getContainerPath() != null) {
	    				containerFileName = _ConnectorArguments.getContainerPath() + IConstants.Characters.SLASH + containerFileName;
		    	       	if(_ConnectorConfig.isDebug()) {	
		    	       		LOG.info("DEBUG : Upload File (" + fullFileName + ") into container (" + _ConnectorArguments.getContainerName() + "/" + _ConnectorArguments.getContainerPath() + ") in storage account (" + _ConnectorArguments.getStorageAccount() + ")");	
	    				}
	    			}
	    	       	if(_ConnectorConfig.isDebug()) {	
	    	       		LOG.info("DEBUG : Upload File (" + fullFileName + ") into container (" + _ConnectorArguments.getContainerName() + ") in storage account (" + _ConnectorArguments.getStorageAccount() + ") filename (" + containerFileName + ")");
	    	       	}
			    	// Get a reference to a blob
					BlobClient blobClient = containerClient.getBlobClient(containerFileName);
		       		LOG.info(MessageFormat.format(StorageAccountUploadingFileMsg, fullFileName, blobClient.getBlobUrl()));
					// Upload the file
					blobClient.uploadFromFile(fullFileName, _ConnectorArguments.isUploadFileOverwrite());
	    		}
		    	success = true;
	    	} else {
	       		LOG.error(MessageFormat.format(StorageAccountFileUploadNoMatchingFilesMsg, _ConnectorArguments.getLocalFileName(),_ConnectorArguments.getDirectoryName()));
		    	success = false;
	    	}
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
		String containerFileName = null;
		
		try{
			fileList.clear();
			if(_ConnectorArguments.getContainerPath() != null) {
				containerFileName = _ConnectorArguments.getContainerPath() + IConstants.Characters.SLASH + _ConnectorArguments.getContainerFileName();
			} else {
				containerFileName = _ConnectorArguments.getContainerFileName();
			}
			if((_ConnectorArguments.getContainerFileName().contains(IConstants.Characters.ASTERIX)) ||
	       			(_ConnectorArguments.getContainerFileName().contains(IConstants.Characters.QUESTION_MARK))){
	       		// we have wildcards, so get item list from container
	       		List<String> items = getContainerBlobListNames(_ConnectorArguments);
	       		if(!items.isEmpty()) {
	       			for(String item : items) {
	       				if(FilenameUtils.wildcardMatch(item, containerFileName)) {
	       					fileList.add(item);
	       				}
	       			}
	       		}
	       	} else {
				fileList.add(containerFileName);
	       	}
	    	// Create a BlobServiceClient object which will be used to create a container client
	    	BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
	    	// get the container client object
	    	BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(_ConnectorArguments.getContainerName());
	    	if(!fileList.isEmpty()) {
	    		for(String filename : fileList) {
					fullFileName = _ConnectorArguments.getDirectoryName() + File.separator + filename;
					fullFileName = fullFileName.replaceAll(IConstants.Characters.SLASH, IConstants.Characters.UNDERSCORE);
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
	       		LOG.info(MessageFormat.format(StorageAccountFileDownloadNoMatchingFilesMsg, _ConnectorArguments.getContainerFileName(), _ConnectorArguments.getContainerName()));
		    	success = false;
	    	}
		} catch (com.azure.storage.blob.models.BlobStorageException azex) {
			if(azex.getMessage().contains(AZURE_ERROR_EMPTY_BODY)) {
	       		LOG.error(MessageFormat.format(StorageAccountFileDownloadNoMatchingFilesMsg, _ConnectorArguments.getContainerFileName(), _ConnectorArguments.getContainerName()));
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
   				if(FilenameUtils.wildcardMatch(file.getName(), pattern)) {
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

	private List<String> getContainerBlobListNames(
			ConnectorArguments _ConnectorArguments
			) throws Exception {
		
		List<String> names = new ArrayList<String>();
		String blobPattern = null;
		int maxLength = SeparatorLine.length();
		
		try{
			DateTimeFormatter displayBlobCreatedTimeFormatter = DateTimeFormatter.ofPattern(IConstants.General.LOG_DATE_TIME_FORMAT); 
			if(_ConnectorArguments.getContainerPath() != null) {
				blobPattern = _ConnectorArguments.getContainerPath() + IConstants.Characters.SLASH + _ConnectorArguments.getContainerFileName();
			} else {
				blobPattern = _ConnectorArguments.getContainerFileName();
			}
	       	// get connection String
	       	StorageInformation storageInformation = _ConnectorConfig.getStorageInformation(_ConnectorArguments.getStorageAccount().toLowerCase());
	    	// Create a BlobServiceClient object which will be used to create a container client
	    	BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(storageInformation.getConnection()).buildClient();
	    	// get matching blobs in container
	    	BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(_ConnectorArguments.getContainerName().toLowerCase());
	    	for (BlobItem blobItem : containerClient.listBlobs()) {
	    		if(blobPattern.equalsIgnoreCase(ALL)) {
		       		// get all blobs
	    			LOG.debug("Adding blob (" + blobItem.getName() + ") from container (" + _ConnectorArguments.getContainerName() + ") to list");
		    		names.add(blobItem.getName());
	    		} else {
	    			// get matching blobs
	   				if(FilenameUtils.wildcardMatch(blobItem.getName(), blobPattern)) {
		    			LOG.debug("Adding blob (" + blobItem.getName() + ") from container (" + _ConnectorArguments.getContainerName() + ") to list");
			    		names.add(blobItem.getName());
	   				}
	    		}
	    	}
		} catch (Exception ex) {
			throw new Exception(ex);
		}
		return names;
	}	// END : getContainerBlobListNames

}
