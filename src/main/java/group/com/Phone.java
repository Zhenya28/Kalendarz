package group.com;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class Phone {
	private Phonenumber.PhoneNumber phone;
	public Phone(String phone) {
		try {
			//TODO later add countrycode
			this.phone = PhoneNumberUtil.getInstance().parse(phone, "PL");
		} catch (NumberParseException e) {
			e.printStackTrace();
		}
	}
	public Phonenumber.PhoneNumber getPhone() {
		return phone;
	}
	
	public void setPhone(Phonenumber.PhoneNumber phone) {
		this.phone = phone;
	}
	@Override
	public String toString() {
		return "Phone [phone=" + phone + "]";
	}
}
