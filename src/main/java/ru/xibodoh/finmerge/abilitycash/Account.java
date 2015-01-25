/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge.abilitycash;

import ru.xibodoh.finmerge.AbstractTreeEntity;
import ru.xibodoh.finmerge.Entity;
import ru.xibodoh.finmerge.EntityManager;

public class Account extends AbstractTreeEntity {

	private static final long serialVersionUID = 1710851656219803495L;
	private String tag;
	
	public Account(EntityManager entityManager) {
		this(entityManager, "account");
	}
	
	public Account(EntityManager entityManager, String tag) {
		super(entityManager);
		this.tag = tag;
	}

	@Override
	protected String getFingerPrint(String prefix) {
		if (Entity.TYPE_ACCOUNT.equals(getType())){
			return super.getFingerPrint(prefix);
		}
		StringBuilder sb = new StringBuilder(prefix);
		String glue = "";
		for (AbstractTreeEntity e: getPath()){
			sb.append(glue);
			sb.append(e.get("name"));
			glue = "\\";
		}
		return sb.toString();
	}

	@Override
	public String getType() {
		return "account".equals(tag)?Entity.TYPE_ACCOUNT:Entity.TYPE_ACCOUNT_FOLDER;
	}

	@Override
	public void setType(String type) {
		throw new UnsupportedOperationException("Account entity is not allowed to change type");
	}

	@Override
	public Object clone() {
		throw new UnsupportedOperationException();
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public String toString(){
		if (Entity.TYPE_ACCOUNT.equals(getType())){
			return toString("", "");
		}
		return toString(" path="+getFingerPrint("")+" ", " tag="+tag+" ");
	}
	
}
