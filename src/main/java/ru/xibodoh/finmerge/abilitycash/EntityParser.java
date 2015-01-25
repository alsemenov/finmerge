/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge.abilitycash;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ru.xibodoh.finmerge.Entity;

class EntityParser extends DefaultHandler {
	
	private static final String TAG_TRANSFER = "transfer";
	private static final String TAG_EXPORT_OPTIONS = "export-options";
	private static final String TAG_INCOME = "income";
	private static final String TAG_EXPENSE = "expense";
	private static final String TAG_INCOME_ACCOUNT = "income-account";
	private static final String TAG_EXPENSE_ACCOUNT = "expense-account";
	private static final String TAG_TRANSACTION = "transaction";
	private static final String TAG_SINGLE_TREE = "single-tree";
	private static final String TAG_EXPENSE_TREE = "expense-tree";
	private static final String TAG_INCOME_TREE = "income-tree";
	private static final String TAG_CATEGORY = "category";
	private static final String TAG_CLASSIFIER = "classifier";
	private static final String TAG_FOLDER = "folder";
	private static final String TAG_ACCOUNT = "account";
	private static final String TAG_NAME = "name";
	private static final String TAG_CURRENCY = "currency";
	private StringBuilder data = new StringBuilder();
	private String entityType = null;
	private Entity entity = null;
	private String entityTag = null;
	private final AbilityCashXMLFile entityManager;
	private String accountName, accountCurrency;
	private boolean insideAccountReference = false;
	private Map<String, Account> accounts = new HashMap<String, Account>();
	private Map<String, Classifier> classifiers = new HashMap<String, Classifier>();	
	private String classifierTreeType = Classifier.TREE_TYPE_ROOT;
	//private String classifierName = null;
	private String classifierKey = null;
	private Classifier classifier = null;
	
	private static Map<String,String> typeTags = new HashMap<String, String>();

	
	static {
		typeTags.put("currencies", Entity.TYPE_CURRENCY);
		typeTags.put("rates", Entity.TYPE_CURRENCY_EXCHANGE_RATE);
		typeTags.put("accounts", Entity.TYPE_ACCOUNT);
		typeTags.put("account-plans", Entity.TYPE_ACCOUNT_FOLDER);
		typeTags.put("classifiers", TAG_CLASSIFIER);
		typeTags.put("transactions", Entity.TYPE_TRANSACTIONS);
	}
	
	EntityParser(AbilityCashXMLFile entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		data.append(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		if (typeTags.containsKey(qName)){
			entityType = null;
		} else if (entityTag!=null && entityTag.equals(qName)){
			entityManager.loaded(entity);
			if (entity instanceof Account){
				accounts.put(entity.get(TAG_NAME)+entity.get(TAG_CURRENCY), (Account) entity);
			} else if (entity instanceof Classifier){
				classifiers.put(entity.get("singular-name"), (Classifier) entity);
			}
			entity = null;
			entityTag = null;
		}else if (TAG_EXPORT_OPTIONS.equals(qName)){
			entityManager.loaded(entity);
			entity = null;	
		} else if (entity!=null){
			if ((entity instanceof Account)) {
				if (TAG_FOLDER.equals(qName)){
					entityManager.loaded(entity);
					entity = ((Account) entity).getParent();
				} else if (TAG_ACCOUNT.equals(qName) && insideAccountReference){
					insideAccountReference = false;
					// TODO find account via EntityManager
					((Account) entity).addChild(accounts.get(accountName+accountCurrency));
					accountName = null;
					accountCurrency = null;
				} else if (TAG_NAME.equals(qName)){
					if (insideAccountReference){
						accountName = data.toString();
					} else {
						entity.set(qName, data.toString());
					}
				} else if (TAG_CURRENCY.equals(qName)){
					if (insideAccountReference){
						accountCurrency = data.toString();
					} else {
						entity.set(qName, data.toString());
					}
				} else {
					entity.set(qName, data.toString());
				}
			} else if (entity instanceof Classifier){
				 if (TAG_CATEGORY.equals(qName)){
					 entityManager.loaded(entity);
					 entity = ((Classifier) entity).getParent();
				 } else if (!qName.endsWith("-tree")){
					 entity.set(qName, data.toString());
				 }
			} else if (Entity.TYPE_TRANSACTIONS.equals(entity.getType())){
				if (TAG_EXPENSE_ACCOUNT.equals(qName) || TAG_INCOME_ACCOUNT.equals(qName)){
					insideAccountReference = false;
					entity.set(qName, accounts.get(accountName+accountCurrency).getId()); // TODO check if account is not found
				} else if (insideAccountReference){ 
					if (TAG_NAME.equals(qName)){
						accountName = data.toString();					
					} else if (TAG_CURRENCY.equals(qName)){
						accountCurrency = data.toString();
					}
				} else if (classifierKey!=null){
					if (TAG_NAME.equals(qName)){
						classifier = (Classifier) classifier.getChild("name", data.toString());
					} else if (TAG_CATEGORY.equals(qName)){
						entity.set(classifierKey, classifier.getId());
						classifierKey = null;
						classifier = null;
					}
				} else if (TAG_EXPENSE.equals(qName) || TAG_INCOME.equals(qName) || TAG_TRANSFER.equals(qName)){
					entity.set("cashflow", qName);
				} else if (!TAG_CATEGORY.equals(qName)){
					entity.set(qName, data.toString());
				} 
			} else {
				entity.set(qName, data.toString());
			}
		}
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		entityType = null;
		entity = null;
		data.delete(0, data.length());
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		data.delete(0, data.length());
		if (typeTags.containsKey(qName)){
			entityType = typeTags.get(qName);
		} else
		if (entityType!=null && entity==null){
			if (Entity.TYPE_ACCOUNT.equals(entityType)){
				entity = new Account(entityManager);
			} else if (Entity.TYPE_ACCOUNT_FOLDER.equals(entityType)){
				entity = new Account(entityManager, qName);
			} else if (TAG_CLASSIFIER.equals(entityType)){
				classifierTreeType = Classifier.TREE_TYPE_ROOT;
				entity = new Classifier(entityManager, classifierTreeType);
			} else {
				entity = new EntityImpl(entityManager, entityType);
			}
			entityTag = qName;
		} else 
		if ((entity instanceof Account)) {
			if (TAG_FOLDER.equals(qName)){		
				Account e = new Account(entityManager, qName);
				((Account) entity).addChild(e);
				entity = e;
			} else if (TAG_ACCOUNT.equals(qName)){
				insideAccountReference = true;
			}
		} else if (entity instanceof Classifier){
			if (TAG_INCOME_TREE.equals(qName) || TAG_EXPENSE_TREE.equals(qName) || TAG_SINGLE_TREE.equals(qName)){
				classifierTreeType = qName;
			} else if (TAG_CATEGORY.equals(qName)){
				Classifier c = new Classifier(entityManager, classifierTreeType);
				((Classifier) entity).addChild(c);
				entity = c;
			}			
		} else if (entity!=null && Entity.TYPE_TRANSACTIONS.equals(entity.getType())){
			if (TAG_EXPENSE_ACCOUNT.equals(qName) || TAG_INCOME_ACCOUNT.equals(qName)){
				insideAccountReference = true;
			} else if (TAG_CATEGORY.equals(qName) && attributes.getValue("classifier")!=null){
				classifierKey = attributes.getValue("classifier");
				classifier = classifiers.get(classifierKey);
			}
		} else if (TAG_EXPORT_OPTIONS.equals(qName)){
			entity = new EntityImpl(entityManager, Entity.TYPE_FILE_METADATA);
		}
	}
	
}
