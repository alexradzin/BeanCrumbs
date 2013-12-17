package com.beanpath.poc;

/**
 * This class extends {@link Phone} but does not define any property.
 * @author alexr
 */
public class Fax extends Phone {
	public Fax() {
		super();
		setType(Phone.Type.FAX);
	}
}
