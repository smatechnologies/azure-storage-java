package com.sma.ui.core.jobdetails.msazurestorage;

import com.sma.ui.core.widgets.base.ComboItem;

public interface MsAzureStorageConstants {
	
	/**
	 * Constants shared by all Windows Jobs
	 */
	int COMMAND_LINE_LIMIT = 4000;

	String INVALID_COMMAND_LINE = "Invalid command line, please go back to the WINDOWS details to fix the command line.";
	String TOO_LONG_COMMAND_LINE = "Invalid command line, total length exceeds " + COMMAND_LINE_LIMIT + " characters.";
	String TOO_LONG_URL = "URL total length exceeds 2000 characters.";
	String TEXTBOX_CANNOT_BE_EMPTY = "{0} cannot be empty.";
	String BOTH_VALUES_CANNOT_BE_EMPTY = "Both {0} and {1} values cannot be empty.";
	String BOTH_VALUES_CANNOT_BE_DEFINED = "Both {0} and {1} values cannot be defined.";
	String VALUE_LESS_THAN_ONE_NOT_ALLOWED = "{0} a value less than 1 is not allowed.";
	String PARSE_JOB_COMMAND_ERROR = "Cannot parse the command line, this does not look like a valid {0}.";
	String WILD_CARDS_NOT_SUPPORTED_ERROR = "Wild cards no supported when both container and local filenames are present.";
	
	ComboItem [] TASKS = new ComboItem[] { new ComboItem ("Create Container", MsAzureStorageEnums.Task.containercreate), 
			new ComboItem ("Delete Container", MsAzureStorageEnums.Task.containerdelete), 
			new ComboItem ("List Containers", MsAzureStorageEnums.Task.containerlist), 
			new ComboItem ("File Arrival", MsAzureStorageEnums.Task.filearrival), 
			new ComboItem ("File Delete", MsAzureStorageEnums.Task.filedelete), 
			new ComboItem ("File Download", MsAzureStorageEnums.Task.filedownload), 
			new ComboItem ("List Files", MsAzureStorageEnums.Task.filelist), 
			new ComboItem ("File Upload", MsAzureStorageEnums.Task.fileupload), 
			new ComboItem ("Unknown", MsAzureStorageEnums.Task.unknown)};

	
	String LOCATION_PATH_TOKEN = "[[AzureStoragePath]]";
	String LOCATION_PATH_TOKEN_UNIX = "[[AzureStoragePathUnix]]";
	String LOCATION_PATH_NAME = "Connector Location";
	String LOCATION_PATH_TOKEN_NAME_TOOLTIP = "The name of a global property that contains the installed location of the Connector";

	String STORAGE_ACCOUNT_TAB_NAME = "Storage Account";

	String STORAGE_ACCOUNT_NAME = "SA Name";
	String STORAGE_ACCOUNT_NAME_TOOLTIP = "The Storage Account name that the request is associated with";
	
	String STORAGE_ACCOUNT_OPERATIONS_TAB_NAME = "Operations";
	String STORAGE_ACCOUNT_OPERATIONS_TASK_NAME = "Task";
	String STORAGE_ACCOUNT_OPERATIONS_TASK_NAME_TOOLTIP = "The Azure operational task to perform";

	String STORAGE_ACCOUNT_INFORMATION_TAB_NAME = "Information";

	String FAILURE_CRITERIA_TAB_NAME = "Failure Criteria";
	
	String TASK_ARGUMENT = "t";
	String ACCOUNT_NAME_ARGUMENT = "sa";
	String ACCESS_KEY_ARGUMENT = "k";
	String CONTAINER_NAME_ARGUMENT = "cn";
	String CONTAINER_PATH_ARGUMENT = "cp";
	String CONTAINER_FILE_NAME_ARGUMENT = "cf";
	String LOCAL_FILE_NAME_ARGUMENT = "lf";
	String FILE_NAME_ARGUMENT = "fn";
	String FILE_OVERWRITE_ARGUMENT = "ov";
	String DIRECTORY_NAME_ARGUMENT = "di";
	String WAIT_TIME_ARGUMENT = "wt";
	String POLL_DELAY_ARGUMENT = "pd";
	String POLL_INTERVAL_ARGUMENT = "pi";
	String FILE_SIZE_STATIC_VALUE_ARGUMENT = "fs";

}

