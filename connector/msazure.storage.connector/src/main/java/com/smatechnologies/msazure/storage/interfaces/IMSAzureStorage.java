package com.smatechnologies.msazure.storage.interfaces;

import java.util.List;

import com.smatechnologies.msazure.storage.arguments.ConnectorArguments;

public interface IMSAzureStorage {
	
	public List<String> getContainerList(ConnectorArguments _ConnectorArguments, String connectionString) throws Exception;
	public List<String> getContainerBlobList(ConnectorArguments _ConnectorArguments, String connectionStringn) throws Exception;
	public boolean createContainer(ConnectorArguments _ConnectorArguments, String connectionStringn) throws Exception;
	public boolean deleteContainer(ConnectorArguments _ConnectorArguments, String connectionString) throws Exception;
	public boolean deleteFile(ConnectorArguments _ConnectorArguments, String connectionString) throws Exception;
	public boolean upLoadFile(ConnectorArguments _ConnectorArguments, String connectionString) throws Exception;
	public boolean downLoadFile(ConnectorArguments _ConnectorArguments, String connectionString) throws Exception;
	
}
