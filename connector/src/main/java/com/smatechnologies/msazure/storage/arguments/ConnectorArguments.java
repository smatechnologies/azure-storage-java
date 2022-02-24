package com.smatechnologies.msazure.storage.arguments;

import com.beust.jcommander.Parameter;

public class ConnectorArguments {

	private static final String TaskDescriptionMsg = "(Required) The task to execute";
	private static final String StorageAccountDescriptionMsg = "(Required) Storage Account name";
	private static final String ContainerNameDescriptionMsg = "(Required) required for all tasks - the name of the container";
	private static final String ContainerPathDescriptionMsg = "(Optional) the path name of a file(s) within the container";
	private static final String ContainerFileNameDescriptionMsg = "(Optional) required for filedelete, fileupload, filelist and filedownload functions - the name of the file in the container";
	private static final String LocalFileNameDescriptionMsg = "(Optional) required for filedelete, fileupload, filelist and filedownload functions - the name of the file on the server";
	private static final String FileDirectoryNameDescriptionMsg = "(Optional) required for fileupload and filedownload functions - the directory that contains the file or where the file should be placed";
	private static final String FileUploadOverwriteMsg = "(Optional) if the file exists on upload, overwrite it (default value false)";
	private static final String FileArrivalMaximumWaitMsg = "(Optional) The maximum time to wait for a file to Arrive in mins (default null - wait indefinitely)";
	private static final String FileArrivalPollDelayMsg = "(Optional) The maximum time to wait before file check in seconds (default 5)";
	private static final String FileArrivalPollIntervalMsg = "(Optional) The maximum time to wait between file checks in seconds (default 3)";
	private static final String FileArrivalStaticFileSizeTimeMsg = "(Optional) The wait time for file size comparison to determine if the file has arrived in seconds (default 5)";

	@Parameter(names="-t", required=true, description = TaskDescriptionMsg)
	private String task = null;

	@Parameter(names="-sa", required=true, description = StorageAccountDescriptionMsg)
	private String storageAccount = null;

	@Parameter(names="-cn", required=true, description = ContainerNameDescriptionMsg)
	private String containerName = null;

	@Parameter(names="-cp", description = ContainerPathDescriptionMsg)
	private String containerPath = null;

	@Parameter(names="-cf", description = ContainerFileNameDescriptionMsg)
	private String containerFileName = null;

	@Parameter(names="-lf", description = LocalFileNameDescriptionMsg)
	private String localFileName = null;

	@Parameter(names="-di", description = FileDirectoryNameDescriptionMsg)
	private String directoryName = null;

	@Parameter(names="-ov", description = FileUploadOverwriteMsg)
	private boolean uploadFileOverwrite = false;
	
	@Parameter(names="-wt", description = FileArrivalMaximumWaitMsg)
	private Integer fileArrivalMaximumWaitTime = null;

	@Parameter(names="-pd", description = FileArrivalPollDelayMsg)
	private Integer fileArrivalPollDelay = 5;

	@Parameter(names="-pi", description = FileArrivalPollIntervalMsg)
	private Integer fileArrivalPollInterval = 3;

	@Parameter(names="-fs", description = FileArrivalStaticFileSizeTimeMsg)
	private Integer fileArrivalFileSizeStaticValue = 5;


	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
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

	public String getContainerPath() {
		return containerPath;
	}

	public void setContainerPath(String containerPath) {
		this.containerPath = containerPath;
	}

	public String getContainerFileName() {
		return containerFileName;
	}

	public void setContainerFileName(String containerFileName) {
		this.containerFileName = containerFileName;
	}

	public String getLocalFileName() {
		return localFileName;
	}

	public void setLocalFileName(String localFileName) {
		this.localFileName = localFileName;
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

	public Integer getFileArrivalMaximumWaitTime() {
		return fileArrivalMaximumWaitTime;
	}

	public void setFileArrivalMaximumWaitTime(Integer fileArrivalMaximumWaitTime) {
		this.fileArrivalMaximumWaitTime = fileArrivalMaximumWaitTime;
	}

	public Integer getFileArrivalPollDelay() {
		return fileArrivalPollDelay;
	}

	public void setFileArrivalPollDelay(Integer fileArrivalPollDelay) {
		this.fileArrivalPollDelay = fileArrivalPollDelay;
	}

	public Integer getFileArrivalPollInterval() {
		return fileArrivalPollInterval;
	}

	public void setFileArrivalPollInterval(Integer fileArrivalPollInterval) {
		this.fileArrivalPollInterval = fileArrivalPollInterval;
	}

	public Integer getFileArrivalFileSizeStaticValue() {
		return fileArrivalFileSizeStaticValue;
	}

	public void setFileArrivalFileSizeStaticValue(Integer fileArrivalFileSizeStaticValue) {
		this.fileArrivalFileSizeStaticValue = fileArrivalFileSizeStaticValue;
	}
	
}
