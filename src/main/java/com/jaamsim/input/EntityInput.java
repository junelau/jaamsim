/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2010-2011 Ausenco Engineering Canada Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package com.jaamsim.input;

import java.util.ArrayList;
import java.util.Collections;

import com.jaamsim.basicsim.Entity;

public class EntityInput<T extends Entity> extends Input<T> {

	private Class<T> entClass;
	private Class<? extends T> entSubClass;  // a particular sub-class that can be set at runtime
	private boolean includeSubclasses;  // flag to determine if subclasses are valid
	private ArrayList<Class<? extends Entity>> invalidClasses; // list of invalid classes (including subclasses).  if empty, then all classes are valid

	public EntityInput(Class<T> aClass, String key, String cat, T def) {
		super(key, cat, def);
		entClass = aClass;
		entSubClass = aClass;
		includeSubclasses = true;
		invalidClasses = new ArrayList<>();
	}

	public void setSubClass(Class<? extends T> aClass) {
		if (aClass != entSubClass)
			this.reset();
		entSubClass = aClass;
	}

	@Override
	public void parse(KeywordIndex kw)
	throws InputErrorException {
		Input.assertCount(kw, 1);
		T tmp = Input.parseEntity(kw.getArg(0), entClass);
		if (!isValid(tmp))
			throw new InputErrorException("%s is not a valid entity", tmp.getName());
		value = tmp;
	}

	@Override
	public ArrayList<String> getValidOptions() {
		ArrayList<String> list = new ArrayList<>();
		if (entSubClass == null)
			return list;

		for (T each: Entity.getClonesOfIterator(entSubClass)) {
			if (each.testFlag(Entity.FLAG_GENERATED))
				continue;

			if (!isValid(each))
				continue;

			list.add(each.getName());
		}
		Collections.sort(list);
		return list;
	}

	@Override
	public void getValueTokens(ArrayList<String> toks) {
		if (value == null) return;

		toks.add(value.getName());
	}

	private boolean isValid(T ent) {
		if(! includeSubclasses) {
			if( ent.getClass() != entClass ) {
				return false;
			}
		}

		for( Class<? extends Entity> c : invalidClasses ) {
			if( c.isAssignableFrom( ent.getClass() ) ) {
				return false;
			}
		}

		return true;
	}

	public void setIncludeSubclasses(boolean bool) {
		this.includeSubclasses = bool;
	}

	public void setInvalidClasses(ArrayList<Class<? extends Entity>> classes ) {
		invalidClasses = classes;
	}

}
