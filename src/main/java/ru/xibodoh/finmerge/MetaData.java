/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge;

import java.util.Iterator;

public interface MetaData {
	int getVersion();
	String getFileName();
	void setFileName(String fileName);
	String[] getParents();
	void setParents(String[] parents);
	
	Object get(String key);
	Iterator<String> keys();
}
