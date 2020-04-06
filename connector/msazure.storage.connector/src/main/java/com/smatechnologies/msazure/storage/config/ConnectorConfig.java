package com.smatechnologies.msazure.storage.config;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.smatechnologies.msazure.storage.modules.StorageInformation;

public class ConnectorConfig {

	private static ConnectorConfig _ConnectorConfig = null;

	private boolean debug = false;
	private List<String> storageInformationList = new ArrayList<String>(); 
	Hashtable<String, StorageInformation> htblStorageAccounts = new Hashtable<String, StorageInformation>(); 
	
	protected ConnectorConfig() {
	}

	public static ConnectorConfig getInstance() {
		if(_ConnectorConfig == null) {
			_ConnectorConfig = new ConnectorConfig();
		}
		return _ConnectorConfig;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public StorageInformation getStorageInformation(String key) {
		return htblStorageAccounts.get(key);
	}

	public List<String> getStorageInformationList() {
		return this.storageInformationList;
	}

	public void loadStorageInformation(List<StorageInformation> storageList) throws Exception {
	
		try {
			for(StorageInformation storage : storageList) {
				htblStorageAccounts.put(storage.getName(),storage);
				storageInformationList.add(storage.getName());
			}
		} catch (Exception ex){
			throw new Exception(ex);
		}
	} // END : loadStorageInformation

}
