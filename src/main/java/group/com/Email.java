package group.com;

import org.apache.commons.validator.routines.EmailValidator;

public class Email {
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Email(String value) {
		if (!EmailValidator.getInstance().isValid(value))
			throw new IllegalArgumentException("Invalid email: " + value);
	this.value = value;
	}

	@Override
	public String toString() {
		return "Email [value=" + value + "]";
	}
}
