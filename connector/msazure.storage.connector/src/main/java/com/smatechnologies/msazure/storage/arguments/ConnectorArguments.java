package com.smatechnologies.msazure.storage.arguments;

import com.beust.jcommander.Parameter;

public class ConnectorArguments {

	private static final String FunctionDescriptionMsg = "(Required) Function to execute";
	private static final String OperationDescriptionMsg = "(Required) Operation to execute";

	private static final String StorageAccountDescriptionMsg = "(Required) Storage Account name";
	private static final String ContainerNameDescriptionMsg = "(Optional) required for blobs, containercreate and containerdelete functions - the name of the container";
	private static final String FileNameDescriptionMsg = "(Optional) required for filedelete, fileupload and filedownload functions - the name of the file";
	private static final String FileDirectoryNameDescriptionMsg = "(Optional) required for fileupload and filedownload functions - the directory that contains the file or where the file should be placed";
	private static final String FileUploadOverwriteMsg = "(Optional) if the file exists on upload, overwrite it (default value false)";

	@Parameter(names="-f", required=true, description = FunctionDescriptionMsg)
	private String function = null;

	@Parameter(names="-o", required=true, description = OperationDescriptionMsg)
	private String operation = null;

	@Parameter(names="-sa", required=true, description = StorageAccountDescriptionMsg)
	private String storageAccount = null;

	@Parameter(names="-cn", required=true, description = ContainerNameDescriptionMsg)
	private String containerName = null;

	@Parameter(names="-fn", required=true, description = FileNameDescriptionMsg)
	private String fileName = null;

	@Parameter(names="-dir", required=true, description = FileDirectoryNameDescriptionMsg)
	private String directoryName = null;

	@Parameter(names="-ov", required=true, description = FileUploadOverwriteMsg)
	private boolean uploadFileOverwrite = false;

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getStorageAccount() {
		return storageAccount;
	}

	public void setStorageAccount(String storageAccount) {
		this.storageAccount = storageAccount;
	}

	public String getContainerName() {
		return containerName;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getDirectoryName() {
		return directoryName;
	}

	public void setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
	}

	public boolean isUploadFileOverwrite() {
		return uploadFileOverwrite;
	}

	public void setUploadFileOverwrite(boolean uploadFileOverwrite) {
		this.uploadFileOverwrite = uploadFileOverwrite;
	}
	
}
