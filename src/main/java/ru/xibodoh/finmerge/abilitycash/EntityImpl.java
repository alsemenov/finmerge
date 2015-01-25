/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge.abilitycash;

import java.util.Iterator;

import ru.xibodoh.finmerge.AbstractEntity;
import ru.xibodoh.finmerge.Entity;
import ru.xibodoh.finmerge.EntityManager;

public class EntityImpl extends AbstractEntity {

	private String type;
	
//	protected EntityImpl(EntityManager entityManager) {
//		super(entityManager);
//	}

	public EntityImpl(EntityManager entityManager, String type) {
		super(entityManager);
		this.type = type;
	}

	@Override
	public String getId() {
		return getFingerPrint("");
	}

	@Override
	public void setId(String id) {
		throw new UnsupportedOperationException("AbilityCash entities do not support external id");
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return toString("", "");
//		StringBuilder sb = new StringBuilder("<type=");
//		sb.append(getType());
//		Iterator<String> keys = keys();
//		while(keys.hasNext()){
//			sb.append(' ');
//			String key = keys.next();
//			String value = get(key);
//			sb.append(key).append('=').append(value);
//		}
//		sb.append('>');
//		return sb.toString();
	}

	@Override
	public Entity getParent() {
		return null;
	}
	
}
