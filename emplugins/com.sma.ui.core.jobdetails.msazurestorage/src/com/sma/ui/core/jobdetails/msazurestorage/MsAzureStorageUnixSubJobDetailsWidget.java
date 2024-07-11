package com.sma.ui.core.jobdetails.msazurestorage;

import java.security.InvalidParameterException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.sma.core.OpconException;
import com.sma.core.api.KeyValueData;
import com.sma.core.api.constants.ExitCodeAdvancedConstants;
import com.sma.core.api.constants.SystemConstants;
import com.sma.core.api.interfaces.IPersistentJob;
import com.sma.core.api.interfaces.ISpecificJobProperties;
import com.sma.core.api.job.specific.UnixJobProperties;
import com.sma.core.session.ContextID;
import com.sma.core.util.Util;
import com.sma.ui.core.jobdetails.CommandLineTokenizer;
import com.sma.ui.core.jobdetails.JobDetailsHelper;
import com.sma.ui.core.jobdetails.JobUtil;
import com.sma.ui.core.messages.IMessageDisplayer;
import com.sma.ui.core.widgets.base.CTabFolder2;
import com.sma.ui.core.widgets.base.ComboItem;
import com.sma.ui.core.widgets.base.ItemCombo;
import com.sma.ui.core.widgets.job.ExitCodeAdvancedWidget;
import com.sma.ui.core.widgets.listeners.ControlTokenSelectorListener;
import com.sma.ui.core.widgets.listeners.DirtyKeyAdapter;
import com.sma.ui.core.widgets.listeners.DirtyModifyAdapter;
import com.sma.ui.core.widgets.listeners.DirtySelectionAdapter;
import com.sma.ui.core.widgets.validation.ValidationMessage;

public class MsAzureStorageUnixSubJobDetailsWidget extends AbstractMsAzureStorageUnixSubJobDetailsWidget {

	private final static String COMMAND_SUFFIX = SystemConstants.SLASH + "AzureStorage.sh";

	private static final String LOCPATH_NAME = "Location";
	private static final String LOCPATH_TOOLTIP = "The name of a global property that contains the location of the Azure Storage Connector";
	
	private final String ACCOUNT_NAME = "Account Name";
	private final String ACCOUNT_TOOLTIP = "The Storage Account name that the request is associated with";
	private final String ACCESS_KEY_NAME = "Access Key";
	private final String ACCESS_KEY_TOOLTIP = "The Storage Account access Key";
	private final String TASK_NAME = "Task";
	private final String TASK_TOOLTIP = "The task to perform";
	private final String CONTAINER_NAME = "Container Name";
	private final String CONTAINER_NAME_TOOLTIP = "The name of the container associated with the task (supports wildcards * and ?)";
	private final String CONTAINER_PATH = "Container Folder";
	private final String CONTAINER_PATH_TOOLTIP = "The name of the path within the container where the file should be placed or extracted from";
	private final String CONTAINER_FILE_NAME = "Container File Name";
	private final String CONTAINER_FILE_NAME_TOOLTIP = "The name of the file associated in the container (supports wildcards * and ? for directory downloads and uploads) If wildcards used, local file name is invalid";
	private final String LOCAL_FILE_NAME = "Local File Name";
	private final String LOCAL_FILE_NAME_TOOLTIP = "The name of the local file (supports wildcards * and ? for directory downloads and uploads) If wildcards used, container file name is invalid";
	private final String DIRECTORY_NAME = "Directory Name";
	private final String DIRECTORY_TOOLTIP = "The name of the directory to upload the file(s) from or download file(s) to";
	private final String OVERWRITE_NAME = "Overwrite";
	private final String OVERWRITE_TOOLTIP = "during file upload, indicates if the file exists, it can be overwritten";
	private final String WAIT_TIME_NAME = "Wait Time";
	private final String WAIT_TIME_TOOLTIP = "The maximum time to wait for the file (minutes)";
	private final String STATIC_FILE_SIZE_TIME_NAME = "Static File Size Time";
	private final String STATIC_FILE_SIZE_TIME_TOOLTIP = "The time to wait for file completion";
	private final String POLL_DELAY_TIME_NAME = "Poll Delay";
	private final String POLL_DELAY_TIME_TOOLTIP = "The time to wait for the initial check (seconds)";
	private final String POLL_INTERVAL_TIME_NAME = "Poll Interval";
	private final String POLL_INTERVAL_TIME_TOOLTIP = "The time between checks (seconds)";
	
	private Composite _mainComposite;
	private Composite _mainInfoComposite;
	private Text _locPathText;
	private Text _storageAccountText;
	private Text _accessKeyText;

	private CTabFolder2 _tabFolder;
	private CTabItem _storageActionTab;
	private Label _taskLabel;
	private ItemCombo _taskItemCombo;
	private Text _containerNameText;
	private Text _containerPathText;
	private Text _containerFileNameText;
	private Text _directoryNameText;
	private Text _localFileNameText;
	private Button _overwriteCheckBox;
	private Text _waitTimeText;
	private Text _staticFileSizeTimeText;
	private Text _pollDelayTimeText;
	private Text _pollIntervalTimeText;
	
	private CTabItem _failureCriteriaTab;
	private ExitCodeAdvancedWidget _advancedExitCodeWidget;
	
	public MsAzureStorageUnixSubJobDetailsWidget(Composite parent,
			IMessageDisplayer messageManager, ContextID context) {
		super(parent, messageManager, context);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = layout.marginWidth = 0;
		layout.horizontalSpacing = layout.verticalSpacing = 0;
		this.setLayout(layout);
		
		this.createPart(this);
		addListeners();
	}

	private void createPart(Composite parent) {

		_mainComposite = new Composite(parent, SWT.NONE);
		_mainComposite.setLayout(new GridLayout(1, false));
		_mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		_mainInfoComposite = new Composite(_mainComposite, SWT.NONE);
		_mainInfoComposite.setLayout(new GridLayout(2, false));
		_mainInfoComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		_locPathText = JobUtil.createLabeledText(_mainInfoComposite, LOCPATH_NAME, 0, JobUtil.COLOR_BLUE, JobUtil.COLOR_LIGHT_GREEN, SWT.BORDER, 1);
		_locPathText.setToolTipText(LOCPATH_TOOLTIP);
		
		_storageAccountText = JobUtil.createLabeledText(_mainInfoComposite, ACCOUNT_NAME, 0, JobUtil.COLOR_BLUE, JobUtil.COLOR_LIGHT_GREEN, SWT.BORDER, 1);
		_storageAccountText.setToolTipText(ACCOUNT_TOOLTIP);

		_accessKeyText = JobUtil.createLabeledText(_mainInfoComposite, ACCESS_KEY_NAME, 0, JobUtil.COLOR_BLUE, JobUtil.COLOR_LIGHT_GREEN, SWT.BORDER, 1);
		_accessKeyText.setToolTipText(ACCESS_KEY_TOOLTIP);

		_tabFolder = createTabFolder(_mainComposite);
	}

	private CTabFolder2 createTabFolder(Composite parent) {

		_tabFolder = new CTabFolder2(parent, SWT.NONE);
		_tabFolder.setLayout(new GridLayout(1, false));
		_tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		_tabFolder.applyFormStyle();

		// create tabs
		_storageActionTab = createStorageAccountTab(_tabFolder);
		_failureCriteriaTab = createFailureCriteriaTab(_tabFolder);

		// show the job details tab
		_tabFolder.setSelection(_storageActionTab);

		return _tabFolder;
	}
	
	private CTabItem createStorageAccountTab(CTabFolder tabFolder) {

		Composite _composite = JobDetailsHelper.createComposite(tabFolder, 1, false);
		_composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Group operationsGroup = JobDetailsHelper.createGroup(_composite, SystemConstants.EMPTY_STRING, 2, false);
		operationsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		_taskLabel = new Label(operationsGroup, SWT.TRAIL);
		_taskLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));
		_taskLabel.setForeground(JobUtil.COLOR_BLUE);
		_taskLabel.setText(TASK_NAME);

		_taskItemCombo = new ItemCombo(operationsGroup, SWT.READ_ONLY | SWT.DROP_DOWN);
		_taskItemCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		_taskItemCombo.setItems(Arrays.asList(MsAzureStorageConstants.TASKS));
		_taskItemCombo.setToolTipText(TASK_TOOLTIP);

		_containerNameText = JobUtil.createLabeledText(operationsGroup,CONTAINER_NAME,0,JobUtil.COLOR_BLUE,JobUtil.COLOR_LIGHT_GREEN, SWT.SINGLE | SWT.BORDER, 1);
		_containerNameText.setToolTipText(CONTAINER_NAME_TOOLTIP);

		_containerPathText = JobUtil.createLabeledText(operationsGroup,CONTAINER_PATH,0,JobUtil.COLOR_BLUE,JobUtil.COLOR_LIGHT_GREEN, SWT.SINGLE | SWT.BORDER, 1);
		_containerPathText.setToolTipText(CONTAINER_PATH_TOOLTIP);

		_containerFileNameText = JobUtil.createLabeledText(operationsGroup,CONTAINER_FILE_NAME,0,JobUtil.COLOR_BLUE,JobUtil.COLOR_LIGHT_GREEN, SWT.SINGLE | SWT.BORDER, 1);
		_containerFileNameText.setToolTipText(CONTAINER_FILE_NAME_TOOLTIP);
		
		Group fileUploadDownloadGroup = JobDetailsHelper.createGroup(_composite, "File Upload/Download options", 2, false);
		fileUploadDownloadGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));

		_directoryNameText = JobUtil.createLabeledText(fileUploadDownloadGroup,DIRECTORY_NAME,0,JobUtil.COLOR_BLUE,JobUtil.COLOR_LIGHT_GREEN, SWT.SINGLE | SWT.BORDER, 1);
		_directoryNameText.setToolTipText(DIRECTORY_TOOLTIP);

		_localFileNameText = JobUtil.createLabeledText(fileUploadDownloadGroup,LOCAL_FILE_NAME,0,JobUtil.COLOR_BLUE,JobUtil.COLOR_LIGHT_GREEN, SWT.SINGLE | SWT.BORDER, 1);
		_localFileNameText.setToolTipText(LOCAL_FILE_NAME_TOOLTIP);

		_overwriteCheckBox = new Button(fileUploadDownloadGroup, SWT.CHECK);
		_overwriteCheckBox.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 1));
		_overwriteCheckBox.setText(OVERWRITE_NAME);
		_overwriteCheckBox.setToolTipText(OVERWRITE_TOOLTIP);

		Group fileArrivalGroup = JobDetailsHelper.createGroup(_composite, "File Arrival options", 2, false);
		fileArrivalGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));

		_waitTimeText = JobUtil.createLabeledText(fileArrivalGroup,WAIT_TIME_NAME,0,JobUtil.COLOR_BLUE,JobUtil.COLOR_LIGHT_GREEN, SWT.SINGLE | SWT.BORDER, 1);
		_waitTimeText.setToolTipText(WAIT_TIME_TOOLTIP);

		_staticFileSizeTimeText = JobUtil.createLabeledText(fileArrivalGroup,STATIC_FILE_SIZE_TIME_NAME,0,JobUtil.COLOR_BLUE,JobUtil.COLOR_LIGHT_GREEN, SWT.SINGLE | SWT.BORDER, 1);
		_staticFileSizeTimeText.setToolTipText(STATIC_FILE_SIZE_TIME_TOOLTIP);

		_pollDelayTimeText = JobUtil.createLabeledText(fileArrivalGroup,POLL_DELAY_TIME_NAME,0,JobUtil.COLOR_BLUE,JobUtil.COLOR_LIGHT_GREEN, SWT.SINGLE | SWT.BORDER, 1);
		_pollDelayTimeText.setToolTipText(POLL_DELAY_TIME_TOOLTIP);

		_pollIntervalTimeText = JobUtil.createLabeledText(fileArrivalGroup,POLL_INTERVAL_TIME_NAME,0,JobUtil.COLOR_BLUE,JobUtil.COLOR_LIGHT_GREEN, SWT.SINGLE | SWT.BORDER, 1);
		_pollIntervalTimeText.setToolTipText(POLL_INTERVAL_TIME_TOOLTIP);
		
		_storageActionTab = JobUtil.createTabItem(tabFolder, _composite, "Storage Action");
		
		return _storageActionTab;
	}
	
	private CTabItem createFailureCriteriaTab(CTabFolder tabFolder) {

		Composite failureCriteriaTab = new Composite(tabFolder, SWT.NONE);
		failureCriteriaTab.setLayout(new GridLayout(2, false));

		Group failureCriteriaAdvanced = new Group(failureCriteriaTab, SWT.NONE);
		failureCriteriaAdvanced.setLayout(new GridLayout());
		failureCriteriaAdvanced.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		
		_advancedExitCodeWidget = new ExitCodeAdvancedWidget(failureCriteriaAdvanced, this.getMessageDisplayer(), ExitCodeAdvancedConstants.MINIMUM_ROWS_TO_DISPLAY,
				ExitCodeAdvancedConstants.MAXIMUM_ROWS_TO_DISPLAY);
		_advancedExitCodeWidget.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		_failureCriteriaTab = JobUtil.createTabItem(tabFolder,	failureCriteriaTab, MsAzureStorageConstants.FAILURE_CRITERIA_TAB_NAME);
		return _failureCriteriaTab;
	}

	private void addListeners() {
		_locPathText.addKeyListener(new DirtyKeyAdapter(this));
		_storageAccountText.addKeyListener(new DirtyKeyAdapter(this));
		_accessKeyText.addKeyListener(new DirtyKeyAdapter(this));
		_taskItemCombo.addSelectionListener(new DirtySelectionAdapter(this));
		_containerNameText.addModifyListener(new DirtyModifyAdapter(this));
		_containerPathText.addModifyListener(new DirtyModifyAdapter(this));
		_containerFileNameText.addModifyListener(new DirtyModifyAdapter(this));
		_directoryNameText.addModifyListener(new DirtyModifyAdapter(this));
		_localFileNameText.addModifyListener(new DirtyModifyAdapter(this));
		_overwriteCheckBox.addSelectionListener(new DirtySelectionAdapter(this));
		_waitTimeText.addModifyListener(new DirtyModifyAdapter(this));
		_staticFileSizeTimeText.addModifyListener(new DirtyModifyAdapter(this));
		_pollDelayTimeText.addModifyListener(new DirtyModifyAdapter(this));
		_pollIntervalTimeText.addModifyListener(new DirtyModifyAdapter(this));
		
		_taskItemCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				switch (getSelectedTask()) {

				case containercreate:
					_containerNameText.setEnabled(true);
					_containerPathText.setEnabled(false);
					_containerFileNameText.setEnabled(false);
					_directoryNameText.setEnabled(false);
					_localFileNameText.setEnabled(false);
					_overwriteCheckBox.setEnabled(false);
					_waitTimeText.setEnabled(false);
					_staticFileSizeTimeText.setEnabled(false);
					_pollDelayTimeText.setEnabled(false);
					_pollIntervalTimeText.setEnabled(false);
					break;
					
				case containerdelete:
					_containerNameText.setEnabled(true);
					_containerPathText.setEnabled(false);
					_containerFileNameText.setEnabled(false);
					_directoryNameText.setEnabled(false);
					_localFileNameText.setEnabled(false);
					_overwriteCheckBox.setEnabled(false);
					_waitTimeText.setEnabled(false);
					_staticFileSizeTimeText.setEnabled(false);
					_pollDelayTimeText.setEnabled(false);
					_pollIntervalTimeText.setEnabled(false);
					break;

				case containerlist:
					_containerNameText.setEnabled(true);
					_containerPathText.setEnabled(false);
					_containerFileNameText.setEnabled(false);
					_directoryNameText.setEnabled(false);
					_localFileNameText.setEnabled(false);
					_overwriteCheckBox.setEnabled(false);
					_waitTimeText.setEnabled(false);
					_staticFileSizeTimeText.setEnabled(false);
					_pollDelayTimeText.setEnabled(false);
					_pollIntervalTimeText.setEnabled(false);
					break;

				case filearrival:
					_containerNameText.setEnabled(true);
					_containerPathText.setEnabled(true);
					_containerFileNameText.setEnabled(true);
					_directoryNameText.setEnabled(false);
					_localFileNameText.setEnabled(false);
					_overwriteCheckBox.setEnabled(false);
					_waitTimeText.setEnabled(true);
					_staticFileSizeTimeText.setEnabled(true);
					_pollDelayTimeText.setEnabled(true);
					_pollIntervalTimeText.setEnabled(true);
					break;

				case filedelete:
					_containerNameText.setEnabled(true);
					_containerPathText.setEnabled(true);
					_containerFileNameText.setEnabled(true);
					_directoryNameText.setEnabled(false);
					_localFileNameText.setEnabled(false);
					_overwriteCheckBox.setEnabled(false);
					_waitTimeText.setEnabled(false);
					_staticFileSizeTimeText.setEnabled(false);
					_pollDelayTimeText.setEnabled(false);
					_pollIntervalTimeText.setEnabled(false);
					break;

				case filedownload:
					_containerNameText.setEnabled(true);
					_containerPathText.setEnabled(true);
					_containerFileNameText.setEnabled(true);
					_directoryNameText.setEnabled(true);
					_localFileNameText.setEnabled(true);
					_overwriteCheckBox.setEnabled(false);
					_waitTimeText.setEnabled(false);
					_staticFileSizeTimeText.setEnabled(false);
					_pollDelayTimeText.setEnabled(false);
					_pollIntervalTimeText.setEnabled(false);
					break;

				case filelist:
					_containerNameText.setEnabled(true);
					_containerPathText.setEnabled(true);
					_containerFileNameText.setEnabled(true);
					_directoryNameText.setEnabled(false);
					_localFileNameText.setEnabled(false);
					_overwriteCheckBox.setEnabled(false);
					_waitTimeText.setEnabled(false);
					_staticFileSizeTimeText.setEnabled(false);
					_pollDelayTimeText.setEnabled(false);
					_pollIntervalTimeText.setEnabled(false);
					break;

				case fileupload:
					_containerNameText.setEnabled(true);
					_containerPathText.setEnabled(true);
					_containerFileNameText.setEnabled(true);
					_directoryNameText.setEnabled(true);
					_localFileNameText.setEnabled(true);
					_overwriteCheckBox.setEnabled(true);
					_waitTimeText.setEnabled(false);
					_staticFileSizeTimeText.setEnabled(false);
					_pollDelayTimeText.setEnabled(false);
					_pollIntervalTimeText.setEnabled(false);
					break;

				case unknown:
					break;

				}
			}
		});

		_advancedExitCodeWidget.addDirtyListener(this);
		
		new ControlTokenSelectorListener(_locPathText, getContextID());
	
	}	

	@Override
	public void setDefaults() {

		setSendDirtyEvents(false);
		_locPathText.setText(MsAzureStorageConstants.LOCATION_PATH_TOKEN_UNIX);
		_storageAccountText.setText(SystemConstants.EMPTY_STRING);
		_accessKeyText.setText(SystemConstants.EMPTY_STRING);
		_taskItemCombo.setSelection(MsAzureStorageEnums.Task.containerlist, true);
		_taskItemCombo.removeItem(new ComboItem ("Unknown", MsAzureStorageEnums.Task.unknown));
		_containerNameText.setText(SystemConstants.EMPTY_STRING);
		_containerPathText.setText(SystemConstants.EMPTY_STRING);
		_containerFileNameText.setText(SystemConstants.EMPTY_STRING);
		_directoryNameText.setText(SystemConstants.EMPTY_STRING);
		_localFileNameText.setText(SystemConstants.EMPTY_STRING);
		_overwriteCheckBox.setSelection(false);
		_waitTimeText.setText("0");
		_staticFileSizeTimeText.setText("5");
		_pollDelayTimeText.setText("5");
		_pollIntervalTimeText.setText("3");

		_advancedExitCodeWidget.setDefaults();
		
		_containerNameText.setEnabled(true);
		_containerPathText.setEnabled(false);
		_containerFileNameText.setEnabled(false);
		_directoryNameText.setEnabled(false);
		_localFileNameText.setEnabled(false);
		_overwriteCheckBox.setEnabled(false);
		_waitTimeText.setEnabled(false);
		_staticFileSizeTimeText.setEnabled(false);
		_pollDelayTimeText.setEnabled(false);
		_pollIntervalTimeText.setEnabled(false);

		setSendDirtyEvents(true);
	}	
	
	@Override
	String getParameters() {
		return SystemConstants.EMPTY_STRING;
	}

	@Override
	protected String getStartImage() {
		StringBuilder builder = new StringBuilder();
		MsAzureStorageEnums.Task task = getSelectedTask();
		String taskName = task.name();
		builder.append(JobUtil.autoQuote(_locPathText.getText().trim() + COMMAND_SUFFIX, true));
		builder.append(SystemConstants.VERTICAL_TAB);
		// set storage account
		builder.append(SystemConstants.SIGN_MINUS);
		builder.append(MsAzureStorageConstants.ACCOUNT_NAME_ARGUMENT);
		builder.append(SystemConstants.VERTICAL_TAB);
		builder.append(SystemConstants.QUOTE);
		builder.append(_storageAccountText.getText());
		builder.append(SystemConstants.QUOTE);
		builder.append(SystemConstants.VERTICAL_TAB);
		builder.append(SystemConstants.SIGN_MINUS);
		builder.append(MsAzureStorageConstants.ACCESS_KEY_ARGUMENT);
		builder.append(SystemConstants.VERTICAL_TAB);
		builder.append(SystemConstants.QUOTE);
		builder.append(_accessKeyText.getText());
		builder.append(SystemConstants.QUOTE);
		builder.append(SystemConstants.VERTICAL_TAB);
		builder.append(SystemConstants.SIGN_MINUS);
		builder.append(MsAzureStorageConstants.TASK_ARGUMENT);
		builder.append(SystemConstants.VERTICAL_TAB);
		builder.append(SystemConstants.QUOTE);
		builder.append(taskName);
		builder.append(SystemConstants.QUOTE);
		builder.append(SystemConstants.VERTICAL_TAB);
		if(taskName.equalsIgnoreCase("containercreate")) {
			builder.append(SystemConstants.SIGN_MINUS);
			builder.append(MsAzureStorageConstants.CONTAINER_NAME_ARGUMENT);
			builder.append(SystemConstants.VERTICAL_TAB);
			builder.append(SystemConstants.QUOTE);
			builder.append(_containerNameText.getText());
			builder.append(SystemConstants.QUOTE);
			builder.append(SystemConstants.VERTICAL_TAB);
		} else if(taskName.equalsIgnoreCase("containerdelete")) {
			builder = appendContainerName(builder, _containerNameText.getText());
		} else if(taskName.equalsIgnoreCase("containerlist")) {
			builder = appendContainerName(builder, _containerNameText.getText());
		} else if(taskName.equalsIgnoreCase("filearrival")) {
			builder = appendContainerName(builder, _containerNameText.getText());
			if(_containerPathText.getText().length() > 0) {
				builder.append(SystemConstants.SIGN_MINUS);
				builder.append(MsAzureStorageConstants.CONTAINER_PATH_ARGUMENT);
				builder.append(SystemConstants.VERTICAL_TAB);
				builder.append(SystemConstants.QUOTE);
				builder.append(_containerPathText.getText());
				builder.append(SystemConstants.QUOTE);
				builder.append(SystemConstants.VERTICAL_TAB);
			}
			builder = appendContainerFileName(builder, _containerFileNameText.getText());
			builder.append(SystemConstants.SIGN_MINUS);
			builder.append(MsAzureStorageConstants.FILE_SIZE_STATIC_VALUE_ARGUMENT);
			builder.append(SystemConstants.VERTICAL_TAB);
			builder.append(SystemConstants.QUOTE);
			builder.append(_staticFileSizeTimeText.getText());
			builder.append(SystemConstants.QUOTE);
			builder.append(SystemConstants.VERTICAL_TAB);
			builder.append(SystemConstants.SIGN_MINUS);
			builder.append(MsAzureStorageConstants.POLL_DELAY_ARGUMENT);
			builder.append(SystemConstants.VERTICAL_TAB);
			builder.append(SystemConstants.QUOTE);
			builder.append(_pollDelayTimeText.getText());
			builder.append(SystemConstants.QUOTE);
			builder.append(SystemConstants.VERTICAL_TAB);
			builder.append(SystemConstants.SIGN_MINUS);
			builder.append(MsAzureStorageConstants.POLL_INTERVAL_ARGUMENT);
			builder.append(SystemConstants.VERTICAL_TAB);
			builder.append(SystemConstants.QUOTE);
			builder.append(_pollIntervalTimeText.getText());
			builder.append(SystemConstants.QUOTE);
			builder.append(SystemConstants.VERTICAL_TAB);
			builder.append(SystemConstants.SIGN_MINUS);
			builder.append(MsAzureStorageConstants.WAIT_TIME_ARGUMENT);
			builder.append(SystemConstants.VERTICAL_TAB);
			builder.append(SystemConstants.QUOTE);
			builder.append(_waitTimeText.getText());
			builder.append(SystemConstants.QUOTE);
			builder.append(SystemConstants.VERTICAL_TAB);
		} else if(taskName.equalsIgnoreCase("filedelete")) {
			builder = appendContainerName(builder, _containerNameText.getText());
			if(!_containerNameText.getText().equals(SystemConstants.ASTERISK)) {
				if(_containerPathText.getText().length() > 0) {
					builder.append(SystemConstants.SIGN_MINUS);
					builder.append(MsAzureStorageConstants.CONTAINER_PATH_ARGUMENT);
					builder.append(SystemConstants.VERTICAL_TAB);
					builder.append(SystemConstants.QUOTE);
					builder.append(_containerPathText.getText());
					builder.append(SystemConstants.QUOTE);
					builder.append(SystemConstants.VERTICAL_TAB);
				}
			}
			builder = appendContainerFileName(builder, _containerFileNameText.getText());
		} else if(taskName.equalsIgnoreCase("filedownload")) {
			builder = appendContainerName(builder, _containerNameText.getText());
			if(!_containerNameText.getText().equals(SystemConstants.ASTERISK)) {
				if(_containerPathText.getText().length() > 0) {
					builder.append(SystemConstants.SIGN_MINUS);
					builder.append(MsAzureStorageConstants.CONTAINER_PATH_ARGUMENT);
					builder.append(SystemConstants.VERTICAL_TAB);
					builder.append(SystemConstants.QUOTE);
					builder.append(_containerPathText.getText());
					builder.append(SystemConstants.QUOTE);
					builder.append(SystemConstants.VERTICAL_TAB);
				}
			}
			builder = appendContainerFileName(builder, _containerFileNameText.getText());
			builder.append(SystemConstants.SIGN_MINUS);
			builder.append(MsAzureStorageConstants.DIRECTORY_NAME_ARGUMENT);
			builder.append(SystemConstants.VERTICAL_TAB);
			builder.append(SystemConstants.QUOTE);
			builder.append(_directoryNameText.getText());
			builder.append(SystemConstants.QUOTE);
			builder.append(SystemConstants.VERTICAL_TAB);
			if(_localFileNameText.getText().length() > 0) {
				builder = appendLocalFileName(builder, _localFileNameText.getText());
			}
		} else if(taskName.equalsIgnoreCase("fileList")) {
			builder = appendContainerName(builder, _containerNameText.getText());
			if(!_containerNameText.getText().equals(SystemConstants.ASTERISK)) {
				if(_containerPathText.getText().length() > 0) {
					builder.append(SystemConstants.SIGN_MINUS);
					builder.append(MsAzureStorageConstants.CONTAINER_PATH_ARGUMENT);
					builder.append(SystemConstants.VERTICAL_TAB);
					builder.append(SystemConstants.QUOTE);
					builder.append(_containerPathText.getText());
					builder.append(SystemConstants.QUOTE);
					builder.append(SystemConstants.VERTICAL_TAB);
				}
			}
			builder = appendContainerFileName(builder, _containerFileNameText.getText());
		} else {
			builder = appendContainerName(builder, _containerNameText.getText());
			if(!_containerNameText.getText().equals(SystemConstants.ASTERISK)) {
				if(_containerPathText.getText().length() > 0) {
					builder.append(SystemConstants.SIGN_MINUS);
					builder.append(MsAzureStorageConstants.CONTAINER_PATH_ARGUMENT);
					builder.append(SystemConstants.VERTICAL_TAB);
					builder.append(SystemConstants.QUOTE);
					builder.append(_containerPathText.getText());
					builder.append(SystemConstants.QUOTE);
					builder.append(SystemConstants.VERTICAL_TAB);
				}
			}
			if(_containerFileNameText.getText().length() > 0) {
				builder = appendContainerFileName(builder, _containerFileNameText.getText());
			}
			builder.append(SystemConstants.SIGN_MINUS);
			builder.append(MsAzureStorageConstants.DIRECTORY_NAME_ARGUMENT);
			builder.append(SystemConstants.VERTICAL_TAB);
			builder.append(SystemConstants.QUOTE);
			builder.append(_directoryNameText.getText());
			builder.append(SystemConstants.QUOTE);
			builder.append(SystemConstants.VERTICAL_TAB);
			builder = appendLocalFileName(builder, _localFileNameText.getText());
			if(_overwriteCheckBox.getSelection()) {
				builder.append(SystemConstants.SIGN_MINUS);
				builder.append(MsAzureStorageConstants.FILE_OVERWRITE_ARGUMENT);
				builder.append(SystemConstants.VERTICAL_TAB);
			}
		}
		return builder.toString();
	}

	private StringBuilder appendContainerName(
			StringBuilder builder, 
			String containerName
			) {
		
		builder.append(SystemConstants.SIGN_MINUS);
		builder.append(MsAzureStorageConstants.CONTAINER_NAME_ARGUMENT);
		builder.append(SystemConstants.VERTICAL_TAB);
		builder.append(SystemConstants.QUOTE);
		if(containerName.equals(SystemConstants.ASTERISK)) {
			builder.append("ALL");
		} else {
			builder.append(containerName);
		}
		builder.append(SystemConstants.QUOTE);
		builder.append(SystemConstants.VERTICAL_TAB);
		return builder;
	}
	
	private StringBuilder appendContainerFileName(
			StringBuilder builder, 
			String fileName
			) {
		
		builder.append(SystemConstants.SIGN_MINUS);
		builder.append(MsAzureStorageConstants.CONTAINER_FILE_NAME_ARGUMENT);
		builder.append(SystemConstants.VERTICAL_TAB);
		builder.append(SystemConstants.QUOTE);
		if(fileName.equals(SystemConstants.ASTERISK)) {
			builder.append("ALL");
		} else {
			builder.append(fileName);
		}
		builder.append(SystemConstants.QUOTE);
		builder.append(SystemConstants.VERTICAL_TAB);
		return builder;
	}

	private StringBuilder appendLocalFileName(
			StringBuilder builder, 
			String fileName
			) {
		
		builder.append(SystemConstants.SIGN_MINUS);
		builder.append(MsAzureStorageConstants.LOCAL_FILE_NAME_ARGUMENT);
		builder.append(SystemConstants.VERTICAL_TAB);
		builder.append(SystemConstants.QUOTE);
		if(fileName.equals(SystemConstants.ASTERISK)) {
			builder.append("ALL");
		} else {
			builder.append(fileName);
		}
		builder.append(SystemConstants.QUOTE);
		builder.append(SystemConstants.VERTICAL_TAB);
		return builder;
	}

	@Override
	protected void initializeContents(ISpecificJobProperties jobProperties)
			throws OpconException {

		UnixJobProperties unixDetails = (UnixJobProperties) jobProperties;

		try {
			if (unixDetails != null) {
				String startImage = unixDetails.getStartImage() + SystemConstants.SPACE + unixDetails.getParameters();
				// basic check first
				if (!startImage.contains(COMMAND_SUFFIX)) {
					MessageFormat.format(MsAzureStorageConstants.PARSE_JOB_COMMAND_ERROR,"Azure Storage");
				}
				parseStartImage(startImage);
				
				_advancedExitCodeWidget.initializeContents();
				if (getInput() != null && getInput().getSpecificJobProperties() != null) {
					try {
						List<KeyValueData> environmentKeysAndValues = new ArrayList<KeyValueData>();
						UnixJobProperties _jobProperties = (UnixJobProperties) getInput().getSpecificJobProperties();
						_advancedExitCodeWidget.setInput(_jobProperties.getExitCodeAdvancedRows());
					} catch (OpconException e) {
						setErrorMessage("Error initializing user selector " + Util.getCauseError(e));
					}
				}
			} else {
				setDefaults();
			}
		} catch (ParseException e) {
			throw new OpconException(e);
		}
	}
	
	private void parseStartImage(String startImage) throws ParseException {

		// extract the location property from the commandline and display this
		int endProperty = startImage.indexOf(SystemConstants.SLASH);

		if (endProperty > -1) {
			_locPathText.setText(startImage.substring(1, endProperty));
		}
		// remove command from commandLine
		startImage = startImage.replace(MsAzureStorageConstants.LOCATION_PATH_TOKEN_UNIX + COMMAND_SUFFIX,
						SystemConstants.EMPTY_STRING).trim();

		Options options = new Options();
		options.addOption(MsAzureStorageConstants.TASK_ARGUMENT, true, SystemConstants.EMPTY_STRING);
		options.addOption(MsAzureStorageConstants.ACCOUNT_NAME_ARGUMENT, true, SystemConstants.EMPTY_STRING);
		options.addOption(MsAzureStorageConstants.ACCESS_KEY_ARGUMENT, true, SystemConstants.EMPTY_STRING);
		options.addOption(MsAzureStorageConstants.CONTAINER_NAME_ARGUMENT, true, SystemConstants.EMPTY_STRING);
		options.addOption(MsAzureStorageConstants.FILE_NAME_ARGUMENT, true, SystemConstants.EMPTY_STRING);
		options.addOption(MsAzureStorageConstants.DIRECTORY_NAME_ARGUMENT, true, SystemConstants.EMPTY_STRING);
		options.addOption(MsAzureStorageConstants.WAIT_TIME_ARGUMENT, true, SystemConstants.EMPTY_STRING);
		options.addOption(MsAzureStorageConstants.POLL_DELAY_ARGUMENT, true, SystemConstants.EMPTY_STRING);
		options.addOption(MsAzureStorageConstants.POLL_INTERVAL_ARGUMENT, true, SystemConstants.EMPTY_STRING);
		options.addOption(MsAzureStorageConstants.FILE_SIZE_STATIC_VALUE_ARGUMENT, true, SystemConstants.EMPTY_STRING);
		options.addOption(MsAzureStorageConstants.FILE_OVERWRITE_ARGUMENT, false, SystemConstants.EMPTY_STRING);
		options.addOption(MsAzureStorageConstants.CONTAINER_PATH_ARGUMENT, true, SystemConstants.EMPTY_STRING);
		options.addOption(MsAzureStorageConstants.CONTAINER_FILE_NAME_ARGUMENT, true, SystemConstants.EMPTY_STRING);
		options.addOption(MsAzureStorageConstants.LOCAL_FILE_NAME_ARGUMENT, true, SystemConstants.EMPTY_STRING);
		
		String[] arguments = CommandLineTokenizer.tokenize(startImage, true);
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, arguments);
		String task = null;
		String cname = null;
		String cpath = null;
		String fname = null;
		String cfname = null;
		String lfname = null;
		
		if(cmd.hasOption(MsAzureStorageConstants.ACCOUNT_NAME_ARGUMENT)) {
			_storageAccountText.setText(removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.ACCOUNT_NAME_ARGUMENT)));
		}
		if(cmd.hasOption(MsAzureStorageConstants.ACCESS_KEY_ARGUMENT)) {
			_accessKeyText.setText(removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.ACCESS_KEY_ARGUMENT)));
		}
		if(cmd.hasOption(MsAzureStorageConstants.TASK_ARGUMENT)) {
			task = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.TASK_ARGUMENT));
			_taskItemCombo.setSelection(getTaskfromName(task), true);
			_taskItemCombo.setEnabled(false);
		}
		switch (getSelectedTask()) {
		
			case containercreate:
				if(cmd.hasOption(MsAzureStorageConstants.CONTAINER_NAME_ARGUMENT)) {
					cname = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.CONTAINER_NAME_ARGUMENT)); 
					_containerNameText.setText(checkNameforAll(cname));
				}
				_containerNameText.setEnabled(true);
				_containerPathText.setEnabled(false);
				_containerFileNameText.setEnabled(false);
				_directoryNameText.setEnabled(false);
				_localFileNameText.setEnabled(false);
				_overwriteCheckBox.setEnabled(false);
				_waitTimeText.setEnabled(false);
				_staticFileSizeTimeText.setEnabled(false);
				_pollDelayTimeText.setEnabled(false);
				_pollIntervalTimeText.setEnabled(false);
				break;
				
			case containerdelete:
				if(cmd.hasOption(MsAzureStorageConstants.CONTAINER_NAME_ARGUMENT)) {
					cname = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.CONTAINER_NAME_ARGUMENT)); 
					_containerNameText.setText(checkNameforAll(cname));
				}
				_containerNameText.setEnabled(true);
				_containerPathText.setEnabled(false);
				_containerFileNameText.setEnabled(false);
				_directoryNameText.setEnabled(false);
				_localFileNameText.setEnabled(false);
				_overwriteCheckBox.setEnabled(false);
				_waitTimeText.setEnabled(false);
				_staticFileSizeTimeText.setEnabled(false);
				_pollDelayTimeText.setEnabled(false);
				_pollIntervalTimeText.setEnabled(false);
				break;

			case containerlist:
				if(cmd.hasOption(MsAzureStorageConstants.CONTAINER_NAME_ARGUMENT)) {
					cname = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.CONTAINER_NAME_ARGUMENT)); 
					_containerNameText.setText(checkNameforAll(cname));
				}
				_containerNameText.setEnabled(true);
				_containerPathText.setEnabled(false);
				_containerFileNameText.setEnabled(false);
				_directoryNameText.setEnabled(false);
				_localFileNameText.setEnabled(false);
				_overwriteCheckBox.setEnabled(false);
				_waitTimeText.setEnabled(false);
				_staticFileSizeTimeText.setEnabled(false);
				_pollDelayTimeText.setEnabled(false);
				_pollIntervalTimeText.setEnabled(false);
				break;
				
			case filearrival:
				if(cmd.hasOption(MsAzureStorageConstants.CONTAINER_NAME_ARGUMENT)) {
					cname = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.CONTAINER_NAME_ARGUMENT)); 
					_containerNameText.setText(checkNameforAll(cname));
				}				
				if(cmd.hasOption(MsAzureStorageConstants.CONTAINER_PATH_ARGUMENT)) {
					cpath = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.CONTAINER_PATH_ARGUMENT)); 
					_containerPathText.setText(cpath);
				}
				if(cmd.hasOption(MsAzureStorageConstants.FILE_NAME_ARGUMENT)) {
					fname = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.FILE_NAME_ARGUMENT)); 
					_containerFileNameText.setText(checkNameforAll(fname));
				}
				if(cmd.hasOption(MsAzureStorageConstants.CONTAINER_FILE_NAME_ARGUMENT)) {
					cfname = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.CONTAINER_FILE_NAME_ARGUMENT)); 
					_containerFileNameText.setText(checkNameforAll(cfname));
				}
				if(cmd.hasOption(MsAzureStorageConstants.WAIT_TIME_ARGUMENT)) {
					_waitTimeText.setText(removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.WAIT_TIME_ARGUMENT)));
				}
				if(cmd.hasOption(MsAzureStorageConstants.FILE_SIZE_STATIC_VALUE_ARGUMENT)) {
					_staticFileSizeTimeText.setText(removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.FILE_SIZE_STATIC_VALUE_ARGUMENT)));
				}
				if(cmd.hasOption(MsAzureStorageConstants.POLL_DELAY_ARGUMENT)) {
					_pollDelayTimeText.setText(removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.POLL_DELAY_ARGUMENT)));
				}
				if(cmd.hasOption(MsAzureStorageConstants.POLL_INTERVAL_ARGUMENT)) {
					_pollIntervalTimeText.setText(removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.POLL_INTERVAL_ARGUMENT)));
				}
				_containerNameText.setEnabled(true);
				_containerPathText.setEnabled(true);
				_containerFileNameText.setEnabled(true);
				_directoryNameText.setEnabled(false);
				_localFileNameText.setEnabled(false);
				_overwriteCheckBox.setEnabled(true);
				_waitTimeText.setEnabled(true);
				_staticFileSizeTimeText.setEnabled(true);
				_pollDelayTimeText.setEnabled(true);
				_pollIntervalTimeText.setEnabled(true);
				break;

			case filedelete:
				if(cmd.hasOption(MsAzureStorageConstants.CONTAINER_NAME_ARGUMENT)) {
					cname = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.CONTAINER_NAME_ARGUMENT)); 
					_containerNameText.setText(checkNameforAll(cname));
				}				
				if(cmd.hasOption(MsAzureStorageConstants.CONTAINER_PATH_ARGUMENT)) {
					cpath = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.CONTAINER_PATH_ARGUMENT)); 
					_containerPathText.setText(cpath);
				}				
				if(cmd.hasOption(MsAzureStorageConstants.FILE_NAME_ARGUMENT)) {
					fname = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.FILE_NAME_ARGUMENT)); 
					_containerFileNameText.setText(checkNameforAll(fname));
				}
				if(cmd.hasOption(MsAzureStorageConstants.CONTAINER_FILE_NAME_ARGUMENT)) {
					cfname = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.CONTAINER_FILE_NAME_ARGUMENT)); 
					_containerFileNameText.setText(checkNameforAll(cfname));
				}
				_containerNameText.setEnabled(true);
				_containerPathText.setEnabled(true);
				_containerFileNameText.setEnabled(true);
				_directoryNameText.setEnabled(false);
				_localFileNameText.setEnabled(false);
				_overwriteCheckBox.setEnabled(false);
				_waitTimeText.setEnabled(false);
				_staticFileSizeTimeText.setEnabled(false);
				_pollDelayTimeText.setEnabled(false);
				_pollIntervalTimeText.setEnabled(false);
				break;
				
			case filedownload:
				if(cmd.hasOption(MsAzureStorageConstants.CONTAINER_NAME_ARGUMENT)) {
					cname = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.CONTAINER_NAME_ARGUMENT)); 
					_containerNameText.setText(checkNameforAll(cname));
				}				
				if(cmd.hasOption(MsAzureStorageConstants.CONTAINER_PATH_ARGUMENT)) {
					cpath = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.CONTAINER_PATH_ARGUMENT)); 
					_containerPathText.setText(cpath);
				}				
				if(cmd.hasOption(MsAzureStorageConstants.FILE_NAME_ARGUMENT)) {
					fname = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.FILE_NAME_ARGUMENT)); 
					_containerFileNameText.setText(checkNameforAll(fname));
				}
				if(cmd.hasOption(MsAzureStorageConstants.CONTAINER_FILE_NAME_ARGUMENT)) {
					cfname = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.CONTAINER_FILE_NAME_ARGUMENT)); 
					_containerFileNameText.setText(checkNameforAll(cfname));
				}
				if(cmd.hasOption(MsAzureStorageConstants.DIRECTORY_NAME_ARGUMENT)) {
					_directoryNameText.setText(removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.DIRECTORY_NAME_ARGUMENT)));
				}
				if(cmd.hasOption(MsAzureStorageConstants.LOCAL_FILE_NAME_ARGUMENT)) {
					lfname = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.LOCAL_FILE_NAME_ARGUMENT)); 
					_localFileNameText.setText(checkNameforAll(lfname));
				}
				_containerNameText.setEnabled(true);
				_containerPathText.setEnabled(true);
				_containerFileNameText.setEnabled(true);
				_directoryNameText.setEnabled(true);
				_localFileNameText.setEnabled(true);
				_overwriteCheckBox.setEnabled(false);
				_waitTimeText.setEnabled(false);
				_staticFileSizeTimeText.setEnabled(false);
				_pollDelayTimeText.setEnabled(false);
				_pollIntervalTimeText.setEnabled(false);
				break;
				
			case filelist:
				if(cmd.hasOption(MsAzureStorageConstants.CONTAINER_NAME_ARGUMENT)) {
					cname = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.CONTAINER_NAME_ARGUMENT)); 
					_containerNameText.setText(checkNameforAll(cname));
				}				
				if(cmd.hasOption(MsAzureStorageConstants.CONTAINER_PATH_ARGUMENT)) {
					cpath = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.CONTAINER_PATH_ARGUMENT)); 
					_containerPathText.setText(cpath);
				}				
				if(cmd.hasOption(MsAzureStorageConstants.FILE_NAME_ARGUMENT)) {
					fname = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.FILE_NAME_ARGUMENT)); 
					_containerFileNameText.setText(checkNameforAll(fname));
				}
				if(cmd.hasOption(MsAzureStorageConstants.CONTAINER_FILE_NAME_ARGUMENT)) {
					cfname = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.CONTAINER_FILE_NAME_ARGUMENT)); 
					_containerFileNameText.setText(checkNameforAll(cfname));
				}
				_containerNameText.setEnabled(true);
				_containerPathText.setEnabled(true);
				_containerFileNameText.setEnabled(true);
				_directoryNameText.setEnabled(false);
				_localFileNameText.setEnabled(false);
				_overwriteCheckBox.setEnabled(false);
				_waitTimeText.setEnabled(false);
				_staticFileSizeTimeText.setEnabled(false);
				_pollDelayTimeText.setEnabled(false);
				_pollIntervalTimeText.setEnabled(false);
				break;
				
			case fileupload:
				if(cmd.hasOption(MsAzureStorageConstants.CONTAINER_NAME_ARGUMENT)) {
					cname = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.CONTAINER_NAME_ARGUMENT)); 
					_containerNameText.setText(checkNameforAll(cname));
				}				
				if(cmd.hasOption(MsAzureStorageConstants.CONTAINER_PATH_ARGUMENT)) {
					cpath = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.CONTAINER_PATH_ARGUMENT)); 
					_containerPathText.setText(cpath);
				}				
				if(cmd.hasOption(MsAzureStorageConstants.FILE_NAME_ARGUMENT)) {
					fname = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.FILE_NAME_ARGUMENT)); 
					_localFileNameText.setText(checkNameforAll(fname));
				}
				if(cmd.hasOption(MsAzureStorageConstants.LOCAL_FILE_NAME_ARGUMENT)) {
					lfname = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.LOCAL_FILE_NAME_ARGUMENT)); 
					_localFileNameText.setText(checkNameforAll(lfname));
				}
				if(cmd.hasOption(MsAzureStorageConstants.CONTAINER_FILE_NAME_ARGUMENT)) {
					cfname = removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.CONTAINER_FILE_NAME_ARGUMENT)); 
					_containerFileNameText.setText(checkNameforAll(cfname));
				}
				if(cmd.hasOption(MsAzureStorageConstants.DIRECTORY_NAME_ARGUMENT)) {
					_directoryNameText.setText(removeLeadingTrailingDoubleQuotes(cmd.getOptionValue(MsAzureStorageConstants.DIRECTORY_NAME_ARGUMENT)));
				}
				if(cmd.hasOption(MsAzureStorageConstants.FILE_OVERWRITE_ARGUMENT)) {
					_overwriteCheckBox.setSelection(true);
				} else {
					_overwriteCheckBox.setSelection(false);
				}
				_containerNameText.setEnabled(true);
				_containerPathText.setEnabled(true);
				_containerFileNameText.setEnabled(true);
				_directoryNameText.setEnabled(true);
				_localFileNameText.setEnabled(true);
				_overwriteCheckBox.setEnabled(true);
				_waitTimeText.setEnabled(false);
				_staticFileSizeTimeText.setEnabled(false);
				_pollDelayTimeText.setEnabled(false);
				_pollIntervalTimeText.setEnabled(false);
				break;

		}
	}

	public ValidationMessage doSave(IProgressMonitor monitor, IPersistentJob toSave) throws OpconException {
		ValidationMessage msg = super.doSave(monitor, toSave);
		if (msg != null) {
			return msg;
		}

		final UnixJobProperties unixJob = (UnixJobProperties) toSave.getSpecificJobProperties();
		monitor.beginTask("Advanced Failure Criteria Properties", 5);

		unixJob.setExitCodeAdvancedOperators(_advancedExitCodeWidget.getExitCodeAdvancedOperators());
		monitor.worked(1);

		unixJob.setExitCodeAdvancedValues(_advancedExitCodeWidget.getExitCodeAdvancedValues());
		monitor.worked(1);

		unixJob.setExitCodeAdvancedEndValues(_advancedExitCodeWidget.getExitCodeAdvancedEndValues());
		monitor.worked(1);

		unixJob.setExitCodeAdvancedResults(_advancedExitCodeWidget.getExitCodeAdvancedResults());
		monitor.worked(1);

		unixJob.setExitCodeAdvancedComparators(_advancedExitCodeWidget.getExitCodeAdvancedComparators());
		monitor.worked(1);

		monitor.done();

		return null;
	}

	
	private MsAzureStorageEnums.Task getSelectedTask() {
		final ComboItem comboItem = _taskItemCombo.getSelectedItem();
		if(comboItem == null) {
			return MsAzureStorageEnums.Task.unknown;
		}
		return (MsAzureStorageEnums.Task) comboItem.data;
	}

	private String removeLeadingTrailingDoubleQuotes(String input) {
		
		String removed = input.trim();
		if(removed.length() > 2) {
			if (removed.substring(0, 1).equals(SystemConstants.QUOTE)) {
				removed = removed.substring(1, removed.length());
			}
			if (removed.substring(removed.length() - 1, removed.length()).equals(
					SystemConstants.QUOTE)) {
				removed = removed.substring(0, removed.length() - 1);
			}
		} 
		return removed;
	}

	private String checkNameforAll(
			String checkName
			) {
		String name = null;
		
		if(checkName.equals("ALL")) {
			name = SystemConstants.ASTERISK;
		} else {
			name = checkName;
		}
		return name;
	}

	private MsAzureStorageEnums.Task getTaskfromName(String name) {
		MsAzureStorageEnums.Task [] tasks = MsAzureStorageEnums.Task.values();
		for (int i = 0; i < tasks.length; i++) {
			if (name.equals(tasks[i].name())){
				return tasks[i];
			}
		}
		throw new InvalidParameterException("Function " + name + " Not Found");
	}

}
