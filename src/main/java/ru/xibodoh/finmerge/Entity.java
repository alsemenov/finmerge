/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge;

import java.util.Iterator;


public interface Entity extends Cloneable {

	// simple types
	String TYPE_CURRENCY = "currency";
	String TYPE_ATTRIBUTES = "attributes";
	String TYPE_CATEGORY = "category";
	String TYPE_LOCATIONS = "locations";
	String TYPE_PROJECT = "project";
	String TYPE_PAYEE = "payee";

	// compound types
	String TYPE_ACCOUNT = "account"; // ref currency
	String TYPE_CATEGORY_ATTRIBUTE = "category_attribute"; // ref category ref attributes
	String TYPE_TRANSACTION_ATTRIBUTE = "transaction_attribute"; // ref transactions ref attributes
	String TYPE_BUDGET = "budget"; // ref category ref currency ref budget ref project
	String TYPE_TRANSACTIONS = "transactions"; // ref account x2 category project location payee
	String TYPE_CURRENCY_EXCHANGE_RATE = "currency_exchange_rate"; // ref currency x2

	String TYPE_ACCOUNT_FOLDER = "account_folder"; // ref account
	String TYPE_FILE_METADATA = "metadata";
	String TYPE_SMS_TEMPLATE = "sms_template";
	
	// primitive types
	String TYPE_DATETIME_LONG = "datetime_long";
	String TYPE_DATETIME_TEXT = "datetime_text";
	String TYPE_AMOUNT_LONG = "amount_long";
	String TYPE_AMOUNT_DOUBLE = "amount_double";

	String getId();
	
	void  setId(String id);
	
	String getFingerPrint();
		
	Entity getParent();
	
	String getType();
	
	String get(String key);
	
	Object getValue(String key);
	
	String getValueType(String key);
	
	void set(String key, String value);
	
	Iterator<String> keys();
	
	EntityManager getEntityManager();
	
	void setEntityManager(EntityManager entityManager);
	
	Object clone();
	
	int getReferenceCount();
	
	void updateReferenceCount(int increment);
	
	void setReferenceCount(int value);
	
//	public void read(BufferedReader br) throws IOException;
	
//	public void write(BufferedWriter bw) throws IOException;
	
}
