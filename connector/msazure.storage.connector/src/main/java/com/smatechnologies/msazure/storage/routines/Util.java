package com.smatechnologies.msazure.storage.routines;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import org.ini4j.Ini;

import com.smatechnologies.msazure.storage.config.ConnectorConfig;
import com.smatechnologies.msazure.storage.connector.impl.StorageConstants;
import com.smatechnologies.msazure.storage.interfaces.ConnectorConstants;
import com.smatechnologies.msazure.storage.modules.StorageInformation;

public class Util {

	public String getExceptionDetails(
			Exception e
			) {
		
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String exceptionDetails = sw.toString();
		return exceptionDetails;
	}

	public ConnectorConfig setConfigurationValues(
			Preferences iniPrefs,
			ConnectorConfig _ConnectorConfig,
			String configFileName
			) throws Exception {
		
		try {
			// get the GENERAL definitions
			String checkIfDebugSet = iniPrefs.node(ConnectorConstants.CONNECTOR_HEADER).get(ConnectorConstants.CONNECTOR_DEBUG, null);
        	if(checkIfDebugSet.equalsIgnoreCase(ConnectorConstants.DEBUG_ON)) {
        		_ConnectorConfig.setDebug(true);
        	} else {
        		_ConnectorConfig.setDebug(false);
        	}
        	// get the STORAGE ACCOUNT definitions
			List<StorageInformation> storageAccounts = new ArrayList<StorageInformation>();
			String[] accounts = getStorageAccountsFromConfigurationFile(configFileName, StorageConstants.STORAGE_HEADER);
			if(accounts != null) {
				for(String account : accounts) {
					String[] values = tokenizeString(account, false, StorageConstants.EQUALS);
					StorageInformation storageInformation = new StorageInformation();
					storageInformation.setName(values[0].toLowerCase().trim());
					storageInformation.setConnection(values[1]);
					storageAccounts.add(storageInformation);
				}
			}
			if(!storageAccounts.isEmpty()) {
				_ConnectorConfig.loadStorageInformation(storageAccounts);
			}

		} catch (Exception ex) {
			throw new Exception(ex);
		}
		return _ConnectorConfig;
	}	// END : setConfigurationValues

	private String[] getStorageAccountsFromConfigurationFile(String fileName, String header) throws Exception {
		String[] accounts = null;
		
		try {
			File iniFile = new File(fileName);
			Ini iniConfiguration = new Ini();
			iniConfiguration.load(iniFile);
			Ini.Section accountDefinitions = iniConfiguration.get(header);
			if(accountDefinitions != null) {
				accounts = accountDefinitions.getAll(StorageConstants.STORAGE_NAME, String[].class);
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
		return accounts;
	}

	public String[] tokenizeString(String parameters, boolean keepQuote, String delimiter) {
		final char QUOTE = StorageConstants.QUOTE.toCharArray()[0];
		final char BACK_SLASH = StorageConstants.BACKSLASH.toCharArray()[0];
		char prevChar = 0;
		char currChar = 0;
		StringBuffer sb = new StringBuffer(parameters.length());

		if (!keepQuote) {
			for (int i = 0; i < parameters.length(); i++) {
				if (i > 0) {
					prevChar = parameters.charAt(i - 1);
				}
				currChar = parameters.charAt(i);

				if (currChar != QUOTE || (currChar == QUOTE && prevChar == BACK_SLASH)) {
					sb.append(parameters.charAt(i));
				}
			}

			if (sb.length() > 0) {
				parameters = sb.toString();
			}
		}
		return parameters.split(delimiter);
	}	// END : tokenizeString


}
