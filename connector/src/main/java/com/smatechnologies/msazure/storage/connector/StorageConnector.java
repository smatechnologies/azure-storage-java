package com.smatechnologies.msazure.storage.connector;

import java.io.File;
import java.text.MessageFormat;
import java.util.prefs.Preferences;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.smatechnologies.msazure.storage.arguments.ConnectorArguments;
import com.smatechnologies.msazure.storage.config.ConnectorConfig;
import com.smatechnologies.msazure.storage.connector.impl.StorageConnectorImpl;
import com.smatechnologies.msazure.storage.enums.Task;
import com.smatechnologies.msazure.storage.interfaces.IConstants;
import com.smatechnologies.msazure.storage.modules.StorageInformation;
import com.smatechnologies.msazure.storage.routines.Util;


public class StorageConnector {

	private static final String StorageAccountMismatchMsg = "Storage Account {0} mismatch, not found in configuration";

	private static final String SeperatorLineMsg =                              "-------------------------------------------------------------------------------------------------------";
	private static final String ClientVersionDisplayMsg =                       "Storage Connector                    : Version {0}";
	private static final String TaskMsg =                                       "-t   (task)                          : {0}";
	private static final String StorageAccountMsg =                             "-sa  (storage account)               : {0}";
	private static final String ContainerNameMsg =                              "-cn  (container name)                : {0}";
	private static final String ContainerPathMsg =                              "-cp  (container path)                : {0}";
	private static final String ContainerFileNameMsg =                          "-cf  (container file name)           : {0}";
	private static final String DirectoryNameMsg =                              "-di  (directory name)                : {0}";
	private static final String LocalFileNameMsg =                              "-lf  (local file name)               : {0}";
	private static final String UploadFileOverwriteMsg =                        "-ov  (upload file overwrite)         : {0}";
	private static final String FileArrivalWaitTimeMsg =                        "-wt  (file arrival wait time)        : {0}";
	private static final String FileArrivalPollDelayMsg =                       "-pd  (file arrival poll delay)       : {0}";
	private static final String FileArrivalPollIntervalMsg =                    "-pi  (file arrival poll interval)    : {0}";
	private static final String FileArrivalStaticFileSizeMsg =                  "-fs  (File arrival static size time) : {0}";
	
	private final static Logger LOG = LoggerFactory.getLogger(StorageConnector.class);
	private static ConnectorConfig _ConnectorConfig = ConnectorConfig.getInstance();
	private static Util _Util = new Util();
	
	public static void main(String[] args) {
		StorageConnectorImpl _StorageConnectorImpl = new StorageConnectorImpl();
		ConnectorArguments _ConnectorArguments = new ConnectorArguments();
		JCommander jcConnectorArguments = null;
		String workingDirectory = null;
		String configFileName = null;
		boolean result = false;
		
		try {
			// set supported TLS protocols 
			System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
    		// get the arguments
			jcConnectorArguments = JCommander.newBuilder()
					.addObject(_ConnectorArguments)
					.build();
			jcConnectorArguments.parse(args);
			// go get information from config file
			workingDirectory = System.getProperty(IConstants.General.SYSTEM_USER_DIRECTORY);
			// go get information from config file
			configFileName = workingDirectory + File.separator + IConstants.ConfigValues.CONFIG_FILE_NAME;
        	Preferences iniPrefs = new IniPreferences(new Ini(new File(configFileName)));
        	// insert values into  configuration
        	_ConnectorConfig = _Util.setConfigurationValues(iniPrefs, _ConnectorConfig, configFileName);
			LOG.info(SeperatorLineMsg);
			LOG.info(MessageFormat.format(ClientVersionDisplayMsg,IConstants.General.SOFTWARE_VERSION));
			LOG.info(SeperatorLineMsg);
//        	StorageInformation info = _ConnectorConfig.getStorageInformation(_ConnectorArguments.getStorageAccount().toLowerCase());
        	StorageInformation info = new StorageInformation();
        	info.setName(_ConnectorArguments.getStorageAccount());
        	info.setConnection(_ConnectorArguments.getAccessKey());
        	LOG.info(MessageFormat.format(StorageAccountMsg,_ConnectorArguments.getStorageAccount()));
        	LOG.info(MessageFormat.format(TaskMsg,_ConnectorArguments.getTask()));
        	LOG.info(MessageFormat.format(ContainerNameMsg,_ConnectorArguments.getContainerName()));
 			if(_ConnectorArguments.getContainerPath() != null) {
	        	LOG.info(MessageFormat.format(ContainerPathMsg,_ConnectorArguments.getContainerPath()));
			}
 			if(_ConnectorArguments.getContainerFileName() != null) {
	        	LOG.info(MessageFormat.format(ContainerFileNameMsg,_ConnectorArguments.getContainerFileName()));
			}
			if(_ConnectorArguments.getDirectoryName() != null) {
				LOG.info(MessageFormat.format(DirectoryNameMsg,_ConnectorArguments.getDirectoryName()));
			}
			if(_ConnectorArguments.getLocalFileName() != null) {
				LOG.info(MessageFormat.format(LocalFileNameMsg,_ConnectorArguments.getLocalFileName()));
			}
        	LOG.info(MessageFormat.format(UploadFileOverwriteMsg,_ConnectorArguments.isUploadFileOverwrite()));
			LOG.info(MessageFormat.format(FileArrivalPollDelayMsg,_ConnectorArguments.getFileArrivalPollDelay()));
			LOG.info(MessageFormat.format(FileArrivalPollIntervalMsg,_ConnectorArguments.getFileArrivalPollInterval()));
			LOG.info(MessageFormat.format(FileArrivalWaitTimeMsg,_ConnectorArguments.getFileArrivalPollDelay()));
			LOG.info(MessageFormat.format(FileArrivalStaticFileSizeMsg,_ConnectorArguments.getFileArrivalPollDelay()));
        	result = _StorageConnectorImpl.processTaskRequest(_ConnectorArguments, info);
			if(!result) {
				System.exit(1);
			}
		} catch (com.beust.jcommander.ParameterException pe) {
			jcConnectorArguments.usage();
			System.exit(1);
		} catch (Exception ex) {
			LOG.error( _Util.getExceptionDetails(ex));
			System.exit(1);
		}
		System.exit(0);
	}

}
