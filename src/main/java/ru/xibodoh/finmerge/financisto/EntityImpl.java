/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge.financisto;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

class EntityImpl extends LinkedHashMap<String, String> implements Entity{
	
	private static final Map<String, String[]> fpDefenitions = new HashMap<String, String[]>();
	private static final Map<String, String> refTypes = new HashMap<String, String>();
	static {
		fpDefenitions.put(TYPE_ACCOUNT, new String[]{"title", "currency_id"});
		refTypes.put("currency_id", Entity.TYPE_CURRENCY);
		
		fpDefenitions.put(TYPE_ATTRIBUTES, new String[]{"type", "name"});
		
		fpDefenitions.put(TYPE_BUDGET, new String[]{"title", "category_id", "currency_id", "start_date", "end_date", "amount","project_id"});
		refTypes.put("category_id", TYPE_CATEGORY);
		refTypes.put("project_id", TYPE_PROJECT);
		
		fpDefenitions.put(TYPE_CATEGORY, new String[]{"title","type"});

		fpDefenitions.put(Entity.TYPE_CATEGORY_ATTRIBUTE, new String[]{"category_id", "attribute_id"});
		refTypes.put("attribute_id", TYPE_ATTRIBUTES);
		
		fpDefenitions.put(TYPE_CURRENCY, new String[]{"name"});
		
		fpDefenitions.put(TYPE_CURRENCY_EXCHANGE_RATE, new String[]{"from_currency_id", "to_currency_id", "rate_date", "rate"});
		refTypes.put("from_currency_id", TYPE_CURRENCY);
		refTypes.put("to_currency_id", TYPE_CURRENCY);
		
		fpDefenitions.put(TYPE_LOCATIONS, new String[]{"name"});
		
		fpDefenitions.put(TYPE_PAYEE, new String[]{"title"});
		
		fpDefenitions.put(TYPE_PROJECT, new String[]{"title"});
		
		fpDefenitions.put(TYPE_TRANSACTION_ATTRIBUTE, new String[]{"transaction_id", "attribute_id"});
		refTypes.put("transaction_id", Entity.TYPE_TRANSACTIONS);
		
		fpDefenitions.put(TYPE_TRANSACTIONS, new String[]{"from_account_id","to_account_id","category_id","project_id","location_id","from_amount","to_amount","datetime","payee_id","note"});
		refTypes.put("from_account_id", TYPE_ACCOUNT);
		refTypes.put("to_account_id", TYPE_ACCOUNT);
		refTypes.put("location_id", TYPE_LOCATIONS);
		refTypes.put("payee_id", TYPE_PAYEE);		
		
		refTypes.put("parent_budget_id", TYPE_BUDGET);
		refTypes.put("last_location_id", TYPE_LOCATIONS);
		refTypes.put("last_project_id", TYPE_PROJECT);
		refTypes.put("original_currency_id", TYPE_CURRENCY);
		refTypes.put("last_category_id", TYPE_CATEGORY);
		
	};

	protected EntityManager entityManager;
	protected int referenceCounter = 0;
	
	public EntityImpl(EntityManager entityManager){
		assert entityManager!=null;
		this.entityManager = entityManager;
	}
	
	public String getId() {		
		return get("_id");
	}

	public void setId(String id){
		set("_id", id);
	}
	public String getFingerPrint() {
		String type = getType();
		StringBuilder fingerPrint = new StringBuilder(type);
		// TODO check keyDefenitions.contains(type)
		for (String key: fpDefenitions.get(type)) {
			Object value = getValue(key);
			if (value instanceof Entity){
				fingerPrint.append('{').append(((Entity) value).getFingerPrint()).append('}');
			} else {
				fingerPrint.append(value);
			}
		}
		return fingerPrint.toString();
	}

	public String getType() {
		return get("$ENTITY");
	}

	public String get(String key) {
		return super.get(key);
	}
	
	public String getValueType(String key){
		return refTypes.get(key);
	}
	
	public Object getValue(String key) {
		String value = get(key);
		
		if (key.endsWith("_id") && !"_id".equals(key)){
			return entityManager.getById(getValueType(key), value);			
		}		
		return value;
	}

	public void set(String key, String value) {
		super.put(key, value);
	}

	public Iterator<String> keys() {
		return keySet().iterator();
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		assert entityManager!=null;
		this.entityManager = entityManager;
	}

	@Override
	public Object clone() {
		EntityImpl result = new EntityImpl(entityManager);
		result.putAll(this);
		return result;
	}

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

}
