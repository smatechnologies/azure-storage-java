package com.smatechnologies.msazure.storage.connector.impl;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smatechnologies.msazure.storage.arguments.ConnectorArguments;
import com.smatechnologies.msazure.storage.enums.InformationTask;
import com.smatechnologies.msazure.storage.enums.OperationTask;
import com.smatechnologies.msazure.storage.interfaces.IMSAzureStorage;
import com.smatechnologies.msazure.storage.modules.StorageInformation;

public class StorageConnectorImpl {

	private static final String SeparatorLine =              "----------------------------------------------------------------------------";
	private static final String StorageAccountHeaderLine =   "Storage Account {0} : ------------------------------------------------------";
	private static final String StorageContainerLine =       "Container : {0}";
	private static final String StorageContainerBlobLine =   "Blob      : {0}";

	private static final String StorageAccountBlobsMissingContainerNameMsg = "{0} : blobs operation missing container name";
	private static final String StorageAccountCreateMissingContainerNameMsg = "containercreate operation missing container name";
	private static final String StorageAccountCreateContainerSuccessMsg = "containercreate ({0}) operation successful";
	private static final String StorageAccountCreateContainerFailedMsg = "containercreate ({0}) operation failed";
	private static final String StorageAccountDeleteMissingContainerNameMsg = "containerdelete operation missing container name";
	private static final String StorageAccountDeleteContainerSuccessMsg = "containerdelete ({0}) operation successful";
	private static final String StorageAccountDeleteContainerFailedMsg = "containerdelete ({0}) operation failed";
	private static final String StorageAccountFileUploadMissingContainerNameMsg = "{0} : fileupload operation missing container name";
	private static final String StorageAccountFileUploadMissingFileNameMsg = "{0} : fileupload operation missing file name";
	private static final String StorageAccountFileUploadMissingDirectoryNameMsg = "{0} : fileupload operation missing directory name";
	private static final String StorageAccountFileUploadSuccessMsg = "fileupload ({0}) operation successful";
	private static final String StorageAccountFileUploadFailedMsg = "fileupload ({0}) operation failed";
	private static final String StorageAccountFileDownloadMissingContainerNameMsg = "filedownload operation missing container name";
	private static final String StorageAccountFileDownloadMissingFileNameMsg = "filedownload operation missing file name";
	private static final String StorageAccountFileDownloadMissingDirectoryNameMsg = "filedownload operation missing directory name";
	private static final String StorageAccountFileDownloadSuccessMsg = "filedownload ({0}) operation successful";
	private static final String StorageAccountFileDownloadFailedMsg = "filedownload ({0}) operation failed";
	private static final String StorageAccountFileDeleteMissingContainerNameMsg = "filedelete operation missing container name";
	private static final String StorageAccountFileDeleteMissingFileNameMsg = "filedelete operation missing file name";
	private static final String StorageAccountFileDeleteSuccessMsg = "filedelete ({0}) from container ({1}) operation successful";
	private static final String StorageAccountFileDeleteFailedMsg = "filedelete ({0}) from container ({1}) operation failed";
	
	private final static Logger LOG = LoggerFactory.getLogger(StorageConnectorImpl.class);
	private IMSAzureStorage _IMSAzureStorage = new MSAzureStorageImpl();

	public boolean processOperationsRequest(
			ConnectorArguments _ConnectorArguments,
			StorageInformation storageInformation
			) throws Exception, Exception {
		
		boolean success = false;
		
		try{
			
	    	OperationTask task = OperationTask.valueOf(_ConnectorArguments.getOperation());
			
			switch (task) {
			
				case containercreate:
					if(_ConnectorArguments.getContainerName() == null) {
			        	LOG.error(StorageAccountCreateMissingContainerNameMsg);
						return false;
					}
					success = _IMSAzureStorage.createContainer(_ConnectorArguments, storageInformation.getConnection());
		        	if(success) {
						LOG.info(StorageConnectorImpl.class.getSimpleName(),MessageFormat.format(StorageAccountCreateContainerSuccessMsg,_ConnectorArguments.getContainerName()));
		        	} else {
		        		LOG.error(MessageFormat.format(StorageAccountCreateContainerFailedMsg,_ConnectorArguments.getContainerName()));
		        	}
					break;
					
				case containerdelete:
					if(_ConnectorArguments.getContainerName() == null) {
						LOG.error(StorageAccountDeleteMissingContainerNameMsg);
						return false;
					}
					success = _IMSAzureStorage.deleteContainer(_ConnectorArguments, storageInformation.getConnection());
		        	if(success) {
		        		LOG.info(StorageConnectorImpl.class.getSimpleName(),MessageFormat.format(StorageAccountDeleteContainerSuccessMsg,_ConnectorArguments.getContainerName()));
		        	} else {
		        		LOG.error(MessageFormat.format(StorageAccountDeleteContainerFailedMsg,_ConnectorArguments.getContainerName()));
		        	}
					break;

				case filedelete:
					if(_ConnectorArguments.getContainerName() == null) {
						LOG.error(StorageAccountFileDeleteMissingContainerNameMsg);
						return false;
					}
					if(_ConnectorArguments.getFileName() == null) {
						LOG.error(StorageAccountFileDeleteMissingFileNameMsg);
						return false;
					}
					success = _IMSAzureStorage.deleteFile(_ConnectorArguments, storageInformation.getConnection());
		        	if(success) {
		        		LOG.info(StorageConnectorImpl.class.getSimpleName(),MessageFormat.format(StorageAccountFileDeleteSuccessMsg,_ConnectorArguments.getFileName(), _ConnectorArguments.getContainerName()));
		        	} else {
		        		LOG.error(MessageFormat.format(StorageAccountFileDeleteFailedMsg,_ConnectorArguments.getFileName(), _ConnectorArguments.getContainerName()));
		        	}
					break;

				case filedownload:
					if(_ConnectorArguments.getContainerName() == null) {
						LOG.error(StorageAccountFileDownloadMissingContainerNameMsg);
						return false;
					}
					if(_ConnectorArguments.getFileName() == null) {
						LOG.error(StorageAccountFileDownloadMissingFileNameMsg);
						return false;
					}
					if(_ConnectorArguments.getDirectoryName() == null) {
						LOG.error(StorageConnectorImpl.class.getSimpleName(),StorageAccountFileDownloadMissingDirectoryNameMsg);
						return false;
					}
					String downloadFileName = _ConnectorArguments.getDirectoryName() + File.separator + _ConnectorArguments.getFileName();
					success = _IMSAzureStorage.downLoadFile(_ConnectorArguments, storageInformation.getConnection());
		        	if(success) {
		        		LOG.info(MessageFormat.format(StorageAccountFileDownloadSuccessMsg,downloadFileName));
		        	} else {
						LOG.error(MessageFormat.format(StorageAccountFileDownloadFailedMsg,downloadFileName));
		        	}
					break;

				case fileupload:
					if(_ConnectorArguments.getContainerName() == null) {
						LOG.error(StorageAccountFileUploadMissingContainerNameMsg);
						return false;
					}
					if(_ConnectorArguments.getFileName() == null) {
						LOG.error(StorageAccountFileUploadMissingFileNameMsg);
						return false;
					}
					if(_ConnectorArguments.getDirectoryName() == null) {
			        	LOG.error(StorageAccountFileUploadMissingDirectoryNameMsg);
						return false;
					}
					String uploadFileName = _ConnectorArguments.getDirectoryName() + File.separator + _ConnectorArguments.getFileName();
					success = _IMSAzureStorage.upLoadFile(_ConnectorArguments, storageInformation.getConnection());
		        	if(success) {
		        		LOG.info(MessageFormat.format(StorageAccountFileUploadSuccessMsg,uploadFileName));
		        	} else {
		        		LOG.error(MessageFormat.format(StorageAccountFileUploadFailedMsg,uploadFileName));
		        	}
					break;

			}
		} catch (Exception ex) {
			throw new Exception(ex);
		}
		return success;
	}	// END : processOperationsRequest
	
	public boolean processInformationRequest(
			ConnectorArguments _ConnectorArguments,
			StorageInformation storageInformation
			) throws Exception, Exception {
		
		boolean success = false;
		
		try{
			
	    	InformationTask task = InformationTask.valueOf(_ConnectorArguments.getOperation());
			
			switch (task) {
			
				case containers:
					List<String> containerNames = _IMSAzureStorage.getContainerList(_ConnectorArguments, storageInformation.getConnection());
		        	LOG.info(SeparatorLine);
		        	LOG.info(MessageFormat.format(StorageAccountHeaderLine,_ConnectorArguments.getStorageAccount()));
					if(!containerNames.isEmpty()) {
						for(String name : containerNames) {
				        	LOG.info(MessageFormat.format(StorageContainerLine,name));
						}
					}
		        	LOG.info(StorageConnectorImpl.class.getSimpleName(),SeparatorLine);
					
					break;
					
				case blobs:
					if(_ConnectorArguments.getContainerName() == null) {
			        	LOG.info(MessageFormat.format(StorageAccountBlobsMissingContainerNameMsg,_ConnectorArguments.getContainerName()));
						return false;
					}
					List<String> blobNames = _IMSAzureStorage.getContainerBlobList(_ConnectorArguments, storageInformation.getConnection());
					if(!blobNames.isEmpty()) {
						for(String name : blobNames) {
				        	LOG.info(MessageFormat.format(StorageContainerBlobLine,name));
						}
					}
					break;

			}
			success = true;
		} catch (Exception ex) {
			throw new Exception(ex);
		}
		return success;
	}	// END : processInformationRequest
	
}
