/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

public abstract class AbstractEntity extends LinkedHashMap<String, String> implements Entity {

	private static final long serialVersionUID = 933596479318019542L;
	private final static Logger logger = Logger.getLogger(AbstractEntity.class.getName());
	private static final SimpleDateFormat longDateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS");
	private static final SimpleDateFormat shortDateParser = new SimpleDateFormat("yyyy-MM-dd");
	
	protected EntityManager entityManager;
	protected int referenceCounter = 0;
	
	
	public AbstractEntity(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		assert entityManager!=null;
		this.entityManager = entityManager;
	}

	public abstract String getId();
	
	public abstract void setId(String id);
	
	public abstract String getType();
	
	public abstract void setType(String type);
	
	public String get(String key) {
		return super.get(key);
	}
	public void set(String key, String value) {
		super.put(key, value);
	}
	public Iterator<String> keys() {
		return keySet().iterator();
	}

	public abstract Object clone();
	
	
	@Override
	public int getReferenceCount() {
		return referenceCounter;
	}

	@Override
	public void updateReferenceCount(int increment) {
		referenceCounter += increment;
	}

	@Override
	public void setReferenceCount(int value) {
		referenceCounter = value;
	}

	public String getValueType(String key){
		return entityManager.getReferenceTypes().get(key);
	}
	
	public Object getValue(String key) {
		String value = get(key);
		String valueType = getValueType(key);
		if (Entity.TYPE_DATETIME_LONG.equals(valueType)){
			return new Date(Long.parseLong(value));
		} else if (Entity.TYPE_DATETIME_TEXT.equals(valueType) && value!=null){
			try {
				if (value.indexOf('T')>=0){
					return longDateParser.parse(value);
				} else {
					return shortDateParser.parse(value);
				}
			} catch (ParseException e) {
				logger.warning("Invalid datetime value '"+value+"' in entity "+this);
				logger.throwing(this.getClass().getName(), "getValue", e);
			}
		} else if (Entity.TYPE_AMOUNT_LONG.equals(valueType)){
			return Long.parseLong(value); // Financisto uses value 0, when field is not used 
		} else if (Entity.TYPE_AMOUNT_DOUBLE.equals(valueType)){
			// AbilityCash writes money as double with 4 digits after dot
			// convert it to long
			if (value==null){
				return Long.valueOf(0L); // Financisto uses value 0, when field is not used 
			}
			int i = value.indexOf('.');
			if (i>=0){
				value = value.substring(0,i)+value.substring(i+1, i+3);
			}
			return Long.parseLong(value);			
		} else if (valueType!=null){
			return entityManager.getById(valueType, value);			
		}
		return value;
	}
	
	public String getFingerPrint() {
		return getFingerPrint(getType());
	}

	protected String getFingerPrint(String prefix){
		StringBuilder fingerPrint = new StringBuilder(prefix);
		// TODO check keyDefenitions.contains(type)
		for (String key: entityManager.getFingerPrintDefinitions().get(getType())) {
			Object value = getValue(key);
			if (value instanceof Entity){
				fingerPrint.append('{').append(((Entity) value).getFingerPrint()).append('}');
			} else if (value instanceof Date){
				fingerPrint.append(((Date) value).getTime());				
			} else {
				fingerPrint.append(value);
			}
		}
		return fingerPrint.toString();
	}
	
	protected String toString(String prefix, String suffix) {
		StringBuilder sb = new StringBuilder("<type=");
		sb.append(getType());
		sb.append(prefix);
		Iterator<String> keys = keys();
		while(keys.hasNext()){
			sb.append(' ');
			String key = keys.next();
			String value = get(key);
			sb.append(key).append('=').append(value);
		}
		sb.append(suffix);
		sb.append('>');
		return sb.toString();
	}
	
}
