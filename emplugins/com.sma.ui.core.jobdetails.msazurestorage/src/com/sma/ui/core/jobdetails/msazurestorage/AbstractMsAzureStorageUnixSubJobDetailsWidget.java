package com.sma.ui.core.jobdetails.msazurestorage;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.sma.core.OpconException;
import com.sma.core.api.constants.LSAMType;
import com.sma.core.api.constants.OperatorConstants;
import com.sma.core.api.constants.SystemConstants;
import com.sma.core.api.interfaces.IPersistentJob;
import com.sma.core.api.interfaces.ISpecificJobProperties;
import com.sma.core.api.job.specific.UnixJobProperties;
import com.sma.core.api.users.BatchUser;
import com.sma.core.session.ContextID;
import com.sma.core.util.Util;
import com.sma.ui.core.constants.SharedColors;
import com.sma.ui.core.constants.StringConstants;
import com.sma.ui.core.jobdetails.AbstractSubJobsDetailsWidget;
import com.sma.ui.core.jobdetails.CommandLineTokenizer;
import com.sma.ui.core.messages.IMessageDisplayer;
import com.sma.ui.core.widgets.job.BatchUserSelector;
import com.sma.ui.core.widgets.validation.ValidationException;
import com.sma.ui.core.widgets.validation.ValidationMessage;

public abstract class AbstractMsAzureStorageUnixSubJobDetailsWidget extends AbstractSubJobsDetailsWidget {

	private BatchUserSelector _uidSelector;

	public AbstractMsAzureStorageUnixSubJobDetailsWidget(Composite parent, IMessageDisplayer messageManager, ContextID context) {
		super(parent, messageManager, context);
		setLayout(new GridLayout());

		if (needUserGroupSelector()) {
			createUserGroupSelector(this);
		}
	}

	/**
	 * This method overrides the parent, and if
	 * {@link #needUserGroupSelector()} returns true
	 * initialize the user selector.
	 * <p>
	 * 
	 * @see com.sma.ui.core.jobdetails.AbstractSubJobsDetailsWidget#initializeContents()
	 */
	@Override
	public void initializeContents() {
		if (needUserGroupSelector()) {
			_uidSelector.initializeContents();
			if (getInput() != null && getInput().getSpecificJobProperties() != null) {
				UnixJobProperties jobProperties = (UnixJobProperties) getInput().getSpecificJobProperties();
				try {
					_uidSelector.selectUser(jobProperties.getGroupId() + "/" + jobProperties.getUserId());
				} catch (OpconException e) {
					setErrorMessage("Error initializing user/group selector " + Util.getCauseError(e));
				}
			}
		}
		super.initializeContents();
	}

	/**
	 * Creates the user/group selector.
	 */
	private void createUserGroupSelector(Composite parent) {
		Composite selector = new Composite(this, SWT.NONE);
		selector.setLayout(new GridLayout(2, false));
		selector.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Label label = new Label(selector, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText("Group Id / User Id");
		label.setForeground(SharedColors.getSystemColor(SWT.COLOR_BLUE));
		
		_uidSelector = new BatchUserSelector(selector, getMessageDisplayer(), getContextID(), LSAMType.UNIX);
		_uidSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		addListeners();
	}

	/**
	 * Creates necessary listeners
	 */
	private void addListeners() {
		_uidSelector.addDirtyListener(this);		
	}
	
	/**
	 * This implementation check the content of the user
	 * ID/group ID. CLients can override but should call the
	 * superclass.
	 * 
	 * @see com.sma.ui.core.widgets.interfaces.IJobTypeDetailsWidget#checkContents()
	 */
	public ValidationMessage checkContents() {
		if (needUserGroupSelector()) {
			if (getBatchUserControl().getSelectedUser() == null) {
				return new ValidationMessage(getBatchUserControl().getCombo(), MessageFormat.format(StringConstants.Msg_IsRequired, "Group Id / User Id"), IMessageProvider.ERROR,
					getBatchUserControl().getCombo());
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see com.sma.ui.core.widgets.interfaces.IJobTypeDetailsWidget#doSave(org.eclipse.core.runtime.IProgressMonitor, com.sma.core.api.interfaces.IPersistentJob)
	 */
	public ValidationMessage doSave(IProgressMonitor monitor, IPersistentJob toSave) throws OpconException {
		final UnixJobProperties unixJob = (UnixJobProperties) toSave.getSpecificJobProperties();
		monitor.beginTask("Unix Job Properties", 11);

		unixJob.setGroupId(getGroupId());
		monitor.worked(1);

		unixJob.setUserId(getUserId());
		monitor.worked(1);

		unixJob.setNICEValue(getNICEValue());
		monitor.worked(1);

		unixJob.setPreRun(getPrerun());
		monitor.worked(1);

		unixJob.setExitCodeOperators(Arrays.asList(new OperatorConstants[] { OperatorConstants.NE }));
		monitor.worked(1);

		unixJob.setExitCodeValues(Arrays.asList(new Integer[] { 0 }));
		monitor.worked(1);

		unixJob.setSignalOperators(Collections.<OperatorConstants> emptyList());
		monitor.worked(1);

		unixJob.setSignalValues(Collections.<Integer> emptyList());
		monitor.worked(1);

		unixJob.setStartImage(getStartImage());
		monitor.worked(1);

		unixJob.setParameters(getParameters());
		monitor.worked(1);

		unixJob.setCoreDump(getCoreDump());
		monitor.worked(1);

		monitor.done();

		return null;
	}

	/**
	 * Utility method to parse the command line using Apache
	 * Commons CLI. Uses the {@link PosixParser} and
	 * {@link CommandLineTokenizer} to split the command
	 * line.
	 * 
	 * @param commandLine
	 *            the command line string
	 * @param options
	 *            the lidt of options to recognize
	 * @return a {@link CommandLine} object to work with the
	 *         command line
	 * @throws ParseException
	 *             on error.
	 */
	protected CommandLine parseCommandLine(String commandLine, Options options, boolean keepQuote) throws ParseException {
		String[] arguments = CommandLineTokenizer.tokenize(commandLine, keepQuote);
		CommandLineParser parser = new PosixParser();
		return parser.parse(options, arguments);
	}

	/**
	 * @return the batch selector control.
	 */
	protected BatchUserSelector getBatchUserControl() {
		return _uidSelector;
	}

	/**
	 * split the GROUP/USER and return each part.
	 * 
	 * @param partIndex
	 *            0 index will return the Group id, 1 will
	 *            return the User id
	 */
	protected String getBatchUserInfoPart(int partIndex) {
		if (_uidSelector == null) {
			return SystemConstants.EMPTY_STRING;
		}

		BatchUser batchUserInfo = _uidSelector.getSelectedUser();
		if (batchUserInfo == null) {
			return SystemConstants.EMPTY_STRING;
		}

		String groupIdUserId = batchUserInfo.getUserDescription();
		if (groupIdUserId.length() == 0) {
			return SystemConstants.EMPTY_STRING;
		}

		String[] guid = groupIdUserId.split("/");
		if (partIndex > guid.length) {
			return SystemConstants.EMPTY_STRING;
		}

		return guid[partIndex];
	}

	/**
	 * You should override this method to create or not
	 * create the User/Group selector widget.
	 * 
	 * @return <code>true</code> by default.
	 */
	protected boolean needUserGroupSelector() {
		return true;
	}

	/**
	 * You should override this method if don't want to
	 * validate if parameter field has been manually
	 * modified.
	 * 
	 * @return <code>true</code> by default.
	 */
	protected boolean checkParameterField() {
		return true;
	}

	/**
	 * Get the Group Id. if {@link #needUserGroupSelector()}
	 * return <code>false</code> then you should override
	 * this method or it will return an empty string.
	 * 
	 * @return the Group Id as of selected in the selector.
	 *         if {@link #needUserGroupSelector()} return
	 *         <code>false</code> then you should override
	 *         this method or it will return an empty
	 *         string.
	 */
	protected String getGroupId() {
		return getBatchUserInfoPart(0);
	}

	/**
	 * Get the user Id. if {@link #needUserGroupSelector()}
	 * return <code>false</code> then you should override
	 * this method or it will return an empty string.
	 * 
	 * @return the user Id as of selected in the selector.
	 *         if {@link #needUserGroupSelector()} return
	 *         <code>false</code> then you should override
	 *         this method or it will return an empty
	 *         string.
	 */
	protected String getUserId() {
		return getBatchUserInfoPart(1);
	}

	/**
	 * Get the NICE value. You can override this method.
	 * 
	 * @return 0 by default.
	 */
	protected Integer getNICEValue() {
		return 0;
	}

	/**
	 * Get the Core Dump value. You can override this
	 * method.
	 * 
	 * @return <code>true</code> by default.
	 */
	protected boolean getCoreDump() {
		return true;
	}

	/**
	 * Get the Prerun value. You can override this method.
	 * 
	 * @return the empty string by default.
	 */
	protected String getPrerun() {
		return SystemConstants.EMPTY_STRING;
	}

	/**
	 * This method is called when the job is saved. You
	 * should construct and return the parameters.
	 * 
	 * @return the constructed parameters.
	 */
	abstract String getParameters();

	/* (non-Javadoc)
	 * @see com.sma.ui.core.jobdetails.AbstractSubJobsDetailsWidget#checkUnhauthorizedFields(com.sma.core.api.interfaces.ISpecificJobProperties)
	 */
	@Override
	protected void checkUnhauthorizedFields(ISpecificJobProperties jobProperties) throws ValidationException {
		UnixJobProperties unixJob = (UnixJobProperties) jobProperties;
		StringBuilder builder = new StringBuilder();
		try {
			if (!unixJob.getPreRun().equals(getPrerun())) {
				builder.append("- Prerun information has been entered\n");
			}
//			if (checkParameterField()) {
//				if (!unixJob.getParameters().equals(getParameters())) {
//					builder.append("- Parameters has been entered\n");
//				}
//			}
			if (unixJob.getNICEValue() != getNICEValue()) {
				builder.append("- Nice Value has been modified\n");
			}
			
			// TODO : allow sub-type developers to customize default values for those criterias
			List<Integer> exitCodes = unixJob.getExitCodeValues();
			List<OperatorConstants> operators = unixJob.getExitCodeOperators();
			if (((exitCodes.size() > 0 && exitCodes.get(0) != 0) || exitCodes.size() > 1) ||
				((operators.size() > 0 && !operators.get(0).equals(OperatorConstants.NE)) || operators.size() > 1)) {
				builder.append("- Failure Criteria has been modified\n");
			}
			if (builder.length() > 0) {
				throw new ValidationException(builder.toString());
			}

		} catch (OpconException e) {
			throw new ValidationException(e.getMessage());
		}
	}

	/**
	 * This method is called when the job is saved. You
	 * should construct and return the command line.
	 * 
	 * @return the constructed command line.
	 */
	abstract protected String getStartImage();

	public boolean isUsingFailureCriteriaAdvanced() {
		return true;
	}

}
