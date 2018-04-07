/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge.abilitycash;

import ru.xibodoh.finmerge.AbstractTreeEntity;
import ru.xibodoh.finmerge.EntityManager;

public class Classifier extends AbstractTreeEntity {

	private static final long serialVersionUID = -4744838468967901256L;

	public static final String TREE_TYPE_ROOT = "root";
	public static final String TREE_TYPE_INCOME = "income-tree";
	public static final String TREE_TYPE_EXPENSE = "expense-tree";
	public static final String TREE_TYPE_SINGLE = "single-tree";
	public static final String TYPE_CLASSIFIER = "classifier";
	
	
	private String type;
	private final String treeType;
	protected Classifier parent = null;
	
	public Classifier(EntityManager entityManager, String treeType) {
		super(entityManager);
		this.type = TYPE_CLASSIFIER;
		this.treeType = treeType;
	}

	public Classifier(EntityManager entityManager, String type, String treeType) {
		super(entityManager);
		this.type = type;
		this.treeType = treeType;
	}
	
	@Override
	protected String getFingerPrint(String prefix) {
//		TODO add expense/income/none flag into finger print 
		StringBuilder sb = new StringBuilder(prefix);
		for(AbstractTreeEntity e: getPath()) {
			sb.append('\\');
			String name = e.get("name");
			if (name==null){
				name = e.get("singular-name");
//				sb.append(e.get("singular-name")).append('\\').append(treeType);
			} 
//				else { 
			
				sb.append(name);
//			}
		}
		return sb.toString();
	}

	public String getTreeType() {
		return treeType;
	}

	/**
	 * returns the default tree type, that should be used for children of this classifier instance.
	 * 
	 * @return {@link #TREE_TYPE_SINGLE} if there are no children or no children with expense/income type; {@link #TREE_TYPE_EXPENSE} otherwise
	 */
	@Deprecated
	public String getDefaultTreeType(){
		if (TREE_TYPE_ROOT.equals(treeType)){
			if (children==null || children.size()==0){
				return TREE_TYPE_SINGLE;
			}
			for (AbstractTreeEntity e: this){
				if (TREE_TYPE_SINGLE.equals(((Classifier) e).getTreeType())){
					return TREE_TYPE_SINGLE;
				}
			}
			return TREE_TYPE_EXPENSE;
		}
		return getTreeType();
	}
	@Deprecated
	public AbstractTreeEntity getDefaultChild(){
		if (!TREE_TYPE_ROOT.equals(treeType) ){
			return null;
		}
		for (AbstractTreeEntity e: this){
			if (TREE_TYPE_SINGLE.equals(((Classifier) e).getTreeType()) ||
					TREE_TYPE_EXPENSE.equals(((Classifier) e).getTreeType())){
				return e;
			}
		}
		return null;
	}
	
	public AbstractTreeEntity getChild(String treeType){
		if (treeType==null){
			treeType = TREE_TYPE_EXPENSE;
		}
		for (AbstractTreeEntity e: this){
			if (TREE_TYPE_SINGLE.equals(((Classifier) e).getTreeType()) ||
					treeType.equals(((Classifier) e).getTreeType())){
				return e;
			}
		}
		return null;
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
	public String toString() {
		String prefix = " path="+getFingerPrint("")+" treeType="+treeType+" ";
		return toString(prefix, "");	
	}

	@Override
	public Object clone() {
		throw new UnsupportedOperationException();
	}
}

