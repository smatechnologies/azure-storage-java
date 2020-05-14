package com.smatechnologies.msazure.storage.connector.impl;

import java.io.File;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.storage.blob.models.BlobProperties;
import com.smatechnologies.msazure.storage.arguments.ConnectorArguments;
import com.smatechnologies.msazure.storage.enums.Task;
import com.smatechnologies.msazure.storage.interfaces.IAzureStorage;
import com.smatechnologies.msazure.storage.interfaces.IConstants;
import com.smatechnologies.msazure.storage.modules.StorageInformation;
import com.smatechnologies.msazure.storage.routines.Util;

public class StorageConnectorImpl {

	private static final String SeparatorLine =                       "----------------------------------------------------------------------------";
	private static final String StorageAccountHeaderLine =            "Storage Account {0} : ------------------------------------------------------";
	private static final String StorageContainerLine =                "Container : {0}";
	private static final String StorageContainerBlobLine =            "Blob      : {0}";

	private static final String CreateContainerSuccessMsg =           "containercreate ({0}) task successful";
	private static final String CreateContainerFailedMsg =            "containercreate ({0}) task failed";
	private static final String CreateContainerInvalidNameMsg =       "containercreate ({0}) task failed as wild cards in name not valid";

	private static final String DeleteContainerSuccessMsg =           "containerdelete ({0}) task successful";
	private static final String DeleteContainerFailedMsg =            "containerdelete ({0}) task failed";

	private static final String FileArrivalMissingFileNameMsg =       "filearrival task missing file name";
	private static final String FileArrivalMsg =                      "filearrival - file ({0}) has arrived in container ({1})";
	private static final String FileArrivalWaitTimeExpiredMsg =       "filearrival - file has not arrived within the defined wait time";
	private static final String FileArrivalInvalidContainerNameMsg =  "filearrival ({0}) task failed as wild cards in container name not valid";
	private static final String FileArrivalInvalidFileNameMsg =       "filearrival ({0}) task failed as wild cards in file name not valid";

	private static final String FileDeleteMissingFileNameMsg =        "filedelete task missing file name argument";
	private static final String FileDeleteSuccessMsg =                "filedelete ({0}) from container ({1}) task successful";
	private static final String FileDeleteFailedMsg =                 "filedelete ({0}) from container ({1}) task failed";

	private static final String FileDownloadMissingFileNameMsg =      "filedownload task missing file name argument";
	private static final String FileDownloadMissingDirectoryNameMsg = "filedownload task missing directory name argument";
	private static final String FileDownloadSuccessMsg =              "filedownload ({0}) task from ({1}) to ({2}) successful";
	private static final String FileDownloadFailedMsg =               "filedownload ({0}) task from ({1}) to ({2}) successful";

	private static final String FileListMissingFileNameMsg =          "filelist task missing file name argument";

	private static final String FileUploadMissingFileNameMsg =        "fileupload task missing file name argument";
	private static final String FileUploadMissingDirectoryNameMsg =   "fileupload task missing directory name argument";
	private static final String FileUploadSuccessMsg =                "fileupload ({0}) task from ({1}) to ({2}) successful";
	private static final String FileUploadFailedMsg =                 "fileupload ({0}) task from ({1}) to ({2}) failed";

	
	private final static Logger LOG = LoggerFactory.getLogger(StorageConnectorImpl.class);
	private IAzureStorage _IAzureStorage = new AzureStorageImpl();
	private Util _Util = new Util();

	private ScheduledExecutorService executorFileArrival = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> futureFileArrival = null;
	private boolean booleanFileArrival = false;
	private long startWaitTimeFileArrival = 0;
	private long maxWaitTimeFileArrival = 0;
	private long staticFileArrivalTimeEnd = 0;
	private Boolean fileArrivalResult = null;
	private int previousFileSize = 0;
	private boolean isFileStable = false;

	public boolean processTaskRequest(
			ConnectorArguments _ConnectorArguments,
			StorageInformation storageInformation
			) throws Exception, Exception {
		
		boolean success = false;
				
		try{
			
			
	    	Task task = Task.valueOf(_ConnectorArguments.getTask());
			
			switch (task) {
			
				case containercreate:
			       	if((_ConnectorArguments.getContainerName().contains(IConstants.Characters.ASTERIX)) ||
			       			(_ConnectorArguments.getContainerName().contains(IConstants.Characters.QUESTION_MARK))) {
		        		LOG.error(MessageFormat.format(CreateContainerInvalidNameMsg,_ConnectorArguments.getContainerName()));
		        		return false;
			       	} 
					success = _IAzureStorage.createContainer(_ConnectorArguments, storageInformation.getConnection());
		        	if(success) {
						LOG.info(StorageConnectorImpl.class.getSimpleName(),MessageFormat.format(CreateContainerSuccessMsg,_ConnectorArguments.getContainerName()));
		        	} else {
		        		LOG.error(MessageFormat.format(CreateContainerFailedMsg,_ConnectorArguments.getContainerName()));
		        	}
					break;
					
				case containerdelete:
					success = _IAzureStorage.deleteContainer(_ConnectorArguments, storageInformation.getConnection());
		        	if(success) {
		        		LOG.info(StorageConnectorImpl.class.getSimpleName(),MessageFormat.format(DeleteContainerSuccessMsg,_ConnectorArguments.getContainerName()));
		        	} else {
		        		LOG.error(MessageFormat.format(DeleteContainerFailedMsg,_ConnectorArguments.getContainerName()));
		        	}
					break;
					
				case containerlist:
					List<String> containerNames = _IAzureStorage.getContainerList(_ConnectorArguments, storageInformation.getConnection());
		        	LOG.info(SeparatorLine);
		        	LOG.info(MessageFormat.format(StorageAccountHeaderLine,_ConnectorArguments.getStorageAccount()));
					if(!containerNames.isEmpty()) {
						for(String name : containerNames) {
				        	LOG.info(MessageFormat.format(StorageContainerLine,name));
						}
					}
		        	LOG.info(StorageConnectorImpl.class.getSimpleName(),SeparatorLine);
		        	success = true;
					break;

				case filearrival:
					fileArrivalResult = null;
					if(_ConnectorArguments.getFileName() == null) {
						LOG.info(FileArrivalMissingFileNameMsg);
						return false;
					}
			       	if((_ConnectorArguments.getContainerName().contains(IConstants.Characters.ASTERIX)) ||
			       			(_ConnectorArguments.getContainerName().contains(IConstants.Characters.QUESTION_MARK))) {
		        		LOG.error(MessageFormat.format(FileArrivalInvalidContainerNameMsg,_ConnectorArguments.getContainerName()));
		        		return false;
			       	} 
		       		if((_ConnectorArguments.getFileName().contains(IConstants.Characters.ASTERIX)) ||
		       			(_ConnectorArguments.getFileName().contains(IConstants.Characters.QUESTION_MARK))) {
		       			LOG.error(MessageFormat.format(FileArrivalInvalidFileNameMsg,_ConnectorArguments.getFileName()));
		        		return false;
			       	} 
					if(_ConnectorArguments.getFileArrivalMaximumWaitTime() != null) {
						startWaitTimeFileArrival = System.currentTimeMillis();
						int waitValue = _ConnectorArguments.getFileArrivalMaximumWaitTime()  * 60 * 1000;
						maxWaitTimeFileArrival = startWaitTimeFileArrival + waitValue;
					} else {
						maxWaitTimeFileArrival = 0;
					}
					previousFileSize = 0;
					isFileStable = false;
					processFileArrivalCheck(_ConnectorArguments);
					success = fileArrivalResult;
					break;

				case filedelete:
					if(_ConnectorArguments.getFileName() == null) {
						LOG.error(FileDeleteMissingFileNameMsg);
						return false;
					}
					success = _IAzureStorage.deleteFile(_ConnectorArguments, storageInformation.getConnection());
		        	if(success) {
		        		LOG.info(MessageFormat.format(FileDeleteSuccessMsg,_ConnectorArguments.getFileName(), _ConnectorArguments.getContainerName()));
		        	} else {
		        		LOG.error(MessageFormat.format(FileDeleteFailedMsg,_ConnectorArguments.getFileName(), _ConnectorArguments.getContainerName()));
		        	}
					break;

				case filedownload:
					if(_ConnectorArguments.getFileName() == null) {
						LOG.error(FileDownloadMissingFileNameMsg);
						return false;
					}
					if(_ConnectorArguments.getDirectoryName() == null) {
						LOG.error(FileDownloadMissingDirectoryNameMsg);
						return false;
					}
					String downloadFileName = _ConnectorArguments.getDirectoryName() + File.separator + _ConnectorArguments.getFileName();
					success = _IAzureStorage.downLoadFile(_ConnectorArguments, storageInformation.getConnection());
		        	if(success) {
		        		LOG.info(MessageFormat.format(FileDownloadSuccessMsg,downloadFileName, _ConnectorArguments.getContainerName(), _ConnectorArguments.getDirectoryName()));
		        	} else {
						LOG.error(MessageFormat.format(FileDownloadFailedMsg,downloadFileName, _ConnectorArguments.getContainerName(), _ConnectorArguments.getDirectoryName()));
		        	}
					break;

				case filelist:
					if(_ConnectorArguments.getFileName() == null) {
			        	LOG.info(MessageFormat.format(FileListMissingFileNameMsg,_ConnectorArguments.getContainerName()));
						return false;
					}
					List<String> blobNames = _IAzureStorage.getContainerBlobList(_ConnectorArguments, storageInformation.getConnection());
					if(!blobNames.isEmpty()) {
						for(String name : blobNames) {
				        	LOG.info(MessageFormat.format(StorageContainerBlobLine,name));
						}
					}
		        	success = true;
					break;

				case fileupload:
					if(_ConnectorArguments.getFileName() == null) {
						LOG.error(FileUploadMissingFileNameMsg);
						return false;
					}
					if(_ConnectorArguments.getDirectoryName() == null) {
			        	LOG.error(FileUploadMissingDirectoryNameMsg);
						return false;
					}
					String uploadFileName = _ConnectorArguments.getDirectoryName() + File.separator + _ConnectorArguments.getFileName();
					success = _IAzureStorage.upLoadFile(_ConnectorArguments, storageInformation.getConnection());
		        	if(success) {
		        		LOG.info(MessageFormat.format(FileUploadSuccessMsg,uploadFileName, _ConnectorArguments.getDirectoryName(), _ConnectorArguments.getContainerName()));
		        	} else {
		        		LOG.error(MessageFormat.format(FileUploadFailedMsg,uploadFileName, _ConnectorArguments.getDirectoryName(), _ConnectorArguments.getContainerName()));
		        	}
					break;

			}
		} catch (Exception ex) {
			throw new Exception(ex);
		}
		return success;
	}	// END : processOperationsRequest
	
	protected void processFileArrivalCheck(
			ConnectorArguments _ConnectorArguments
			) throws Exception {
		
		try {
		    final Runnable checkFileAvailability = new Runnable() {
		         public void run() { 
		        	 try {
		        		 fileArrival(_ConnectorArguments);
					} catch (Exception ex) {
						try {
							LOG.error(_Util.getExceptionDetails(ex));
						} catch (Exception e) {
						}
					} 
		         }
	       };
	       futureFileArrival = executorFileArrival.scheduleWithFixedDelay(checkFileAvailability, _ConnectorArguments.getFileArrivalPollDelay(), _ConnectorArguments.getFileArrivalPollInterval(), TimeUnit.SECONDS);				
	       booleanFileArrival = false;;
	       waitForFileArrivalCompletion();
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} // END : processFileArrivalCheck
	
	private void fileArrival(
			ConnectorArguments _ConnectorArguments
			) throws Exception {
		
		try {
			BlobProperties blobProperties = _IAzureStorage.checkIfFileExists(_ConnectorArguments);
			// check maximum wait time
			if(maxWaitTimeFileArrival > 0) {
				LOG.debug("fileArrival : checking if maximum wait time expired");
				// we got a wait time so check if this has expired
				long currentTimeValue = System.currentTimeMillis();
				if(currentTimeValue > maxWaitTimeFileArrival) {
					// timeout so stop poll process
					LOG.info(FileArrivalWaitTimeExpiredMsg);
		        	fileArrivalResult = false;
		        	fileArrivalCompleted();
					futureFileArrival.cancel(true);
				}
			}
			if(blobProperties != null) {
				// check if file has been processed - created timestamp must be equal to or greater than startWaitTimeFileAvailable
				boolean wasProcessed = checkIfFileWasProcessed(blobProperties);
				if(!wasProcessed) {
					long size = blobProperties.getBlobSize();
					int fileSize = (int) (long) size;

					LOG.debug("fileAvailable : returned fileSize (" + String.valueOf(fileSize) + ") previous fileSize (" + String.valueOf(previousFileSize) + ")");
					if(previousFileSize == fileSize) {
						if(!isFileStable) {
							LOG.info("fileAvailable : setting stable counter");
							isFileStable = true;
							// set staticFileTimeEnd
							long endCurrentTimeValue = System.currentTimeMillis();
							int staticTime = _ConnectorArguments.getFileArrivalFileSizeStaticValue()  * 1000;
							staticFileArrivalTimeEnd = endCurrentTimeValue + staticTime;
						}
						long staticTimeValue = System.currentTimeMillis();
						if(staticTimeValue > staticFileArrivalTimeEnd) {
							// we have the file
							LOG.info(MessageFormat.format(FileArrivalMsg, _ConnectorArguments.getFileName(),
				        			_ConnectorArguments.getContainerName()));
				        	fileArrivalResult = true;
				        	fileArrivalCompleted();
							futureFileArrival.cancel(true);
						}
					} else {
						previousFileSize = fileSize;
						if(isFileStable) {
							LOG.debug("fileAvailable : reset stable indicator");
							isFileStable = false;
						}
					}
				} else {
					LOG.debug("fileAvailable : file not found");

				}
			} else {
	       		LOG.debug("fileAvailable : file not found");

			}
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}	// END : fileAvailable

	private synchronized void waitForFileArrivalCompletion(
			) throws Exception {
		
		try {
			while(!booleanFileArrival) {
				wait();
			}
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} 	// END : waitForFileArrivalCompletion

	private synchronized void fileArrivalCompleted(
			) throws Exception {
		
		try {
			booleanFileArrival = true;
			notify();
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} 	// END : fileAvailableCompleted

	private boolean checkIfFileWasProcessed(
			BlobProperties blobProperties
			) throws Exception {
		
		boolean processed = true;
		
		try {
			OffsetDateTime fileCreatedDateOffset = blobProperties.getCreationTime();
			Instant fileCreatedInstant = fileCreatedDateOffset.toInstant();
			Date fileCreatedDate = Date.from(fileCreatedInstant);
			long createdDate = fileCreatedDate.getTime();
			if(createdDate >= startWaitTimeFileArrival) {
				processed = false;
			}
		} catch (Exception ex) {
			throw new Exception(ex);
		}
		return processed;
	}

}
