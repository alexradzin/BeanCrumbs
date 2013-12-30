package com.beanpath.poc;

@com.beancrumbs.processor.Crumbed(com.beancrumbs.nullsafe.NullSafeAccess.class)
public class MagazineAccessor extends Magazine {
	private final Magazine instance;
	public MagazineAccessor(Magazine instance) {
		this.instance = instance;
	}
	@Override
	public java.lang.String getName() {
		return instance == null ? null : instance.getName();
	}
}
