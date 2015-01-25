/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge.abilitycash;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import ru.xibodoh.finmerge.Entity;
import ru.xibodoh.finmerge.EntityManager;

@Deprecated
public class AccountFolder extends EntityImpl implements Iterable<Entity>{


	private static final long serialVersionUID = 2283705048128182427L;
	private final String tag; // will be used for converting to XML, root and children have different tags
	
	public AccountFolder(EntityManager entityManager, String tag) {
		super(entityManager, Entity.TYPE_ACCOUNT_FOLDER);
		this.tag = tag;
	}
	
	protected AccountFolder parent = null;
	protected LinkedList<Entity> children = null;
	

	public AccountFolder getParent() {
		return parent;
	}
	
	public void setParent(AccountFolder parent){
		this.parent = parent;
	}
	
	public void addChild(Entity child){
		if (child.getEntityManager()!=this.getEntityManager()){
			throw new IllegalArgumentException("child belongs to different entity manager");
		}
		if (children==null){
			children = new LinkedList<Entity>();
		}
		children.add(child);
		if (child instanceof AccountFolder){
			((AccountFolder)child).parent = this;
		}
	}
	
	public Iterator<Entity> iterator() {
		if (children!=null){
			return Collections.unmodifiableList(children).iterator();
		}
		return Collections.<Entity>emptyIterator();
	}
	
	protected String getPath() {
		StringBuilder s = new StringBuilder(get("name"));
		AccountFolder p = this.parent;
		while (p!=null){
			s.insert(0, '\\');
			s.insert(0, p.get("name"));
			p = p.parent;
		}
		return s.toString();
	}

	@Override
	protected String getFingerPrint(String prefix) {
		return prefix + getPath();
	}

	@Override
	public String toString() {
		return "<tag="+tag+" path="+getPath()+">";
	}

	public String getTag() {
		return tag;
	}

}
