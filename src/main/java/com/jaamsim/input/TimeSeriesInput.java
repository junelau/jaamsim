/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2013 Ausenco Engineering Canada Inc.
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

import com.jaamsim.Samples.TimeSeriesConstantDouble;
import com.jaamsim.Samples.TimeSeriesProvider;
import com.jaamsim.basicsim.Entity;
import com.jaamsim.datatypes.DoubleVector;
import com.jaamsim.units.DimensionlessUnit;
import com.jaamsim.units.Unit;
import com.jaamsim.units.UserSpecifiedUnit;

public class TimeSeriesInput extends Input<TimeSeriesProvider> {
	private Class<? extends Unit> unitType = DimensionlessUnit.class;

	public TimeSeriesInput(String key, String cat, TimeSeriesProvider def) {
		super(key, cat, def);
	}

	public void setUnitType(Class<? extends Unit> u) {
		if (u != unitType)
			this.reset();
		unitType = u;
		if (defValue instanceof TimeSeriesConstantDouble)
			((TimeSeriesConstantDouble)defValue).setUnitType(unitType);
	}

	@Override
	public void parse(KeywordIndex kw)
	throws InputErrorException {
		Input.assertCountRange(kw, 1, 2);

		// Try to parse as a constant value
		try {
			DoubleVector tmp = Input.parseDoubles(kw, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, unitType);
			Input.assertCount(tmp, 1);
			value = new TimeSeriesConstantDouble(unitType, tmp.get(0));
			return;
		}
		catch (InputErrorException e) {}

		// If not a constant, try parsing a TimeSeriesProvider
		Input.assertCount(kw, 1);
		Entity ent = Input.parseEntity(kw.getArg(0), Entity.class);
		TimeSeriesProvider s = Input.castImplements(ent, TimeSeriesProvider.class);
		if( s.getUnitType() != UserSpecifiedUnit.class )
			Input.assertUnitsMatch(unitType, s.getUnitType());
		value = s;
	}

	@Override
	public ArrayList<String> getValidOptions() {
		ArrayList<String> list = new ArrayList<>();
		for (Entity each : Entity.getClonesOfIterator(Entity.class, TimeSeriesProvider.class)) {
			TimeSeriesProvider tsp = (TimeSeriesProvider)each;
			if (tsp.getUnitType() == unitType)
				list.add(each.getName());
		}
		Collections.sort(list);
		return list;
	}

	@Override
	public void getValueTokens(ArrayList<String> toks) {
		if (value == null) return;

		if (value instanceof TimeSeriesConstantDouble) {
			super.getValueTokens(toks);
			return;
		}
		else {
			toks.add(((Entity)value).getName());
		}
	}
}
