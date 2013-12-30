package com.beanpath.poc;

@com.beancrumbs.processor.Crumbed(com.beancrumbs.nullsafe.NullSafeAccess.class)
public class ArticleAccessor extends Article {
	private final Article instance;
	public ArticleAccessor(Article instance) {
		this.instance = instance;
	}
	@Override
	public java.lang.String getTitle() {
		return instance == null ? null : instance.getTitle();
	}
	@Override
	public com.beanpath.poc.Magazine getMagazine() {
		return new com.beanpath.poc.MagazineAccessor(instance == null ? null : instance.getMagazine());
	}
	@Override
	public java.lang.String getAuthor() {
		return instance == null ? null : instance.getAuthor();
	}
}
