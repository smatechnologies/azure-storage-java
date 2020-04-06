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
import com.smatechnologies.msazure.storage.enums.Function;
import com.smatechnologies.msazure.storage.interfaces.ConnectorConstants;
import com.smatechnologies.msazure.storage.modules.StorageInformation;
import com.smatechnologies.msazure.storage.routines.Util;


public class StorageConnector {

	private static final String StorageAccountMismatchMsg = "Storage Account {0} mismatch, not found in configuration";

	private static final String SeperatorLineMsg =                              "-------------------------------------------------------------------------------------------------------";
	private static final String ClientVersionDisplayMsg =                       "Storage Connector                 : Version {0}";
	private static final String FunctionMsg =                                   "-f   (function)                   : {0}";
	private static final String OperationMsg =                                  "-o   (operation)                  : {0}";
	private static final String StorageAccountMsg =                             "-sa  (storage account)            : {0}";
	private static final String ContainerNameMsg =                              "-cn  (container name)             : {0}";
	private static final String FileNameMsg =                                   "-fn  (file name)                  : {0}";
	private static final String DirectoryNameMsg =                              "-dir (directory name)             : {0}";
	private static final String UploadFileOverwriteMsg =                        "-ov  (upload file overwrite)      : {0}";
	
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
    		// get the arguments
			jcConnectorArguments = JCommander.newBuilder()
					.addObject(_ConnectorArguments)
					.build();
			jcConnectorArguments.parse(args);
			// go get information from config file
			workingDirectory = System.getProperty(ConnectorConstants.SYSTEM_USER_DIRECTORY);
			// go get information from config file
			configFileName = workingDirectory + File.separator + ConnectorConstants.CONFIG_FILE_NAME;
        	Preferences iniPrefs = new IniPreferences(new Ini(new File(configFileName)));
        	// insert values into  configuration
        	_ConnectorConfig = _Util.setConfigurationValues(iniPrefs, _ConnectorConfig, configFileName);
			LOG.info(SeperatorLineMsg);
			LOG.info(MessageFormat.format(ClientVersionDisplayMsg,ConnectorConstants.SOFTWARE_VERSION));
			LOG.info(SeperatorLineMsg);
        	StorageInformation info = _ConnectorConfig.getStorageInformation(_ConnectorArguments.getStorageAccount().toLowerCase());
        	if(info == null) {
	        	LOG.error(MessageFormat.format(StorageAccountMismatchMsg, _ConnectorArguments.getStorageAccount()));
        		System.exit(1);
        	}
        	LOG.info(MessageFormat.format(FunctionMsg,_ConnectorArguments.getFunction()));
        	LOG.info(MessageFormat.format(OperationMsg,_ConnectorArguments.getOperation()));
        	LOG.info(MessageFormat.format(StorageAccountMsg,_ConnectorArguments.getStorageAccount()));
        	
        	Function function = Function.valueOf(_ConnectorArguments.getFunction());
			
			switch (function) {
			
				case operations:
					if(_ConnectorArguments.getContainerName() != null) {
			        	LOG.info(MessageFormat.format(ContainerNameMsg,_ConnectorArguments.getContainerName()));
					}
					if(_ConnectorArguments.getFileName() != null) {
						LOG.info(MessageFormat.format(FileNameMsg,_ConnectorArguments.getFileName()));
					}
					if(_ConnectorArguments.getDirectoryName() != null) {
						LOG.info(MessageFormat.format(DirectoryNameMsg,_ConnectorArguments.getDirectoryName()));
					}
		        	LOG.info(MessageFormat.format(UploadFileOverwriteMsg,_ConnectorArguments.isUploadFileOverwrite()));
		        	result = _StorageConnectorImpl.processOperationsRequest(_ConnectorArguments, info);
		        	break;
	        	
				case information:
					if(_ConnectorArguments.getContainerName() != null) {
						LOG.info(MessageFormat.format(ContainerNameMsg,_ConnectorArguments.getContainerName()));
					}
					if(_ConnectorArguments.getFileName() != null) {
						LOG.info(MessageFormat.format(FileNameMsg,_ConnectorArguments.getFileName()));
					}
		        	result = _StorageConnectorImpl.processInformationRequest(_ConnectorArguments, info);
					break;
			
			}
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
