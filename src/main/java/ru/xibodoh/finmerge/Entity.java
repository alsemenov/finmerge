/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;


public interface Entity extends Cloneable {

	// simple types
	public final String TYPE_CURRENCY = "currency";
	public final String TYPE_ATTRIBUTES = "attributes";
	public final String TYPE_CATEGORY = "category";
	public final String TYPE_LOCATIONS = "locations";
	public final String TYPE_PROJECT = "project";
	public final String TYPE_PAYEE = "payee";

	// compound types
	public final String TYPE_ACCOUNT = "account"; // ref currency
	public final String TYPE_CATEGORY_ATTRIBUTE = "category_attribute"; // ref category ref attributes
	public final String TYPE_TRANSACTION_ATTRIBUTE = "transaction_attribute"; // ref transactions ref attributes
	public final String TYPE_BUDGET = "budget"; // ref category ref currency ref budget ref project
	public final String TYPE_TRANSACTIONS = "transactions"; // ref account x2 category project location payee
	public final String TYPE_CURRENCY_EXCHANGE_RATE = "currency_exchange_rate"; // ref currency x2 

	public final String TYPE_ACCOUNT_FOLDER = "account_folder"; // ref account
	public final String TYPE_FILE_METADATA = "metadata";
	public final String TYPE_SMS_TEMPLATE = "sms_template";
	
	// primitive types
	public final String TYPE_DATETIME_LONG = "datetime_long";
	public final String TYPE_DATETIME_TEXT = "datetime_text";
	public final String TYPE_AMOUNT_LONG = "amount_long";
	public final String TYPE_AMOUNT_DOUBLE = "amount_double";

	public String getId();
	
	public void  setId(String id);
	
	public String getFingerPrint();
		
	public Entity getParent();
	
	public String getType();
	
	public String get(String key);
	
	public Object getValue(String key);
	
	public String getValueType(String key);
	
	public void set(String key, String value);
	
	public Iterator<String> keys();
	
	public EntityManager getEntityManager();
	
	public void setEntityManager(EntityManager entityManager);
	
	public Object clone();
	
	public int getReferenceCount();
	
	public void updateReferenceCount(int increment);
	
	public void setReferenceCount(int value);
	
//	public void read(BufferedReader br) throws IOException;
	
//	public void write(BufferedWriter bw) throws IOException;
	
}
