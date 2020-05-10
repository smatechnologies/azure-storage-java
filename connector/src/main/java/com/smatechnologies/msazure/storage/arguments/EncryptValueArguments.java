package com.smatechnologies.msazure.storage.arguments;

import com.beust.jcommander.Parameter;

public class EncryptValueArguments {

	public static final String ValueArgumentDescriptionMsg = "Text string to encrypt";

	@Parameter(names="-v", required=true, description = ValueArgumentDescriptionMsg)
	private String value = null;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
