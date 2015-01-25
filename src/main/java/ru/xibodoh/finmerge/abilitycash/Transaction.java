/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge.abilitycash;

import java.util.Iterator;

import ru.xibodoh.finmerge.Entity;
import ru.xibodoh.finmerge.EntityManager;

@Deprecated
public class Transaction extends EntityImpl {

	protected Transaction(EntityManager entityManager) {
		super(entityManager, Entity.TYPE_TRANSACTIONS);
	}
	
	@Override
	protected String getFingerPrint(String prefix) {
		String type = getType();
		StringBuilder fingerPrint = new StringBuilder(type);
		// TODO check keyDefenitions.contains(type)
		Iterator<String> keys = keys();
		while(keys.hasNext()){
			String key = keys.next();
			Object value = getValue(key);
			if (value instanceof Entity){
				fingerPrint.append('{').append(((Entity) value).getFingerPrint()).append('}');
			} else {
				fingerPrint.append(value);
			}
		}
		return fingerPrint.toString();	
	}
}
