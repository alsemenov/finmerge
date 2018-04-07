/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

public interface EntityManager extends Iterable<Entity> {

	String getFileName();
	
	Entity getById(String type, String id);
	
	Entity getByFingerPrint(String fingerPrint);
	
	String add(Entity entity);
	
	Entity remove(Entity entity);

	int remove(Collection<Entity> entities);
	
	MetaData getMetaData();
	
	Map<String, String> getReferenceTypes();
	
	Map<String, String[]> getFingerPrintDefinitions();

	boolean contains(Entity entity);
	
	List<Entity> unique(EntityManager entityManager);
	
	List<Entity> common(EntityManager entityManager);
	
	void merge(EntityManager backupFile);

	void update(EntityManager otherFile);
	
	Collection<Entity> added();
	
	Collection<Entity> deleted();
	
	// TODO narrow exceptions list
	void save(File file) throws IOException, XMLStreamException, FactoryConfigurationError;
}
