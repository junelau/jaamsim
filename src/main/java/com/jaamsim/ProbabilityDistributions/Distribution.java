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
package com.jaamsim.ProbabilityDistributions;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.Samples.SampleProvider;
import com.jaamsim.basicsim.Entity;
import com.jaamsim.basicsim.Simulation;
import com.jaamsim.events.EventManager;
import com.jaamsim.input.Input;
import com.jaamsim.input.InputAgent;
import com.jaamsim.input.InputErrorException;
import com.jaamsim.input.IntegerInput;
import com.jaamsim.input.Keyword;
import com.jaamsim.input.Output;
import com.jaamsim.input.UnitTypeInput;
import com.jaamsim.input.ValueInput;
import com.jaamsim.ui.EditBox;
import com.jaamsim.ui.FrameBox;
import com.jaamsim.units.DimensionlessUnit;
import com.jaamsim.units.Unit;
import com.jaamsim.units.UserSpecifiedUnit;

/**
 * ProbablityDistribution is the super-class for the various probability distributions implemented in JaamSim.
 * @author Harry King
 *
 */
public abstract class Distribution extends DisplayEntity
implements SampleProvider {

	@Keyword(description = "The unit type that the distribution returns values in.",
	         exampleList = {"DistanceUnit"})
	protected final UnitTypeInput unitType;

	@Keyword(description = "Seed for the random number generator.  Must be an integer >= 0. "
			+ "The RandomSeed keyword works together with the GlobalSubstreamSeed keyword for Simulation "
			+ "to determine the random sequence. The GlobalSubsteamSeed keyword allows the user "
			+ "to change all the random sequences in a model with a single input.",
			 exampleList = {"547"})
	private final IntegerInput randomSeedInput;

	@Keyword(description = "Minimum value that can be returned.  Smaller values are rejected and resampled.",
	         exampleList = {"0.0"})
	protected final ValueInput minValueInput;

	@Keyword(description = "Maximum value that can be returned.  Larger values are rejected and resampled.",
	         exampleList = {"200.0"})
	protected final ValueInput maxValueInput;

	private int sampleCount;
	private double sampleSum;
	private double sampleSquaredSum;
	private double sampleMin;
	private double sampleMax;

	private double lastSample = 0;

	{
		unitType = new UnitTypeInput("UnitType", "Key Inputs", UserSpecifiedUnit.class);
		unitType.setRequired(true);
		this.addInput(unitType);

		randomSeedInput = new IntegerInput("RandomSeed", "Key Inputs", -1);
		randomSeedInput.setValidRange(0, Integer.MAX_VALUE);
		randomSeedInput.setRequired(true);
		randomSeedInput.setDefaultText(EditBox.NONE);
		this.addInput(randomSeedInput);

		minValueInput = new ValueInput("MinValue", "Key Inputs", Double.NEGATIVE_INFINITY);
		minValueInput.setUnitType(UserSpecifiedUnit.class);
		this.addInput(minValueInput);

		maxValueInput = new ValueInput("MaxValue", "Key Inputs", Double.POSITIVE_INFINITY);
		maxValueInput.setUnitType(UserSpecifiedUnit.class);
		this.addInput(maxValueInput);
	}

	public Distribution() {}

	@Override
	public void validate() {
		super.validate();

		// The maximum value must be greater than the minimum value
		if( maxValueInput.getValue() <= minValueInput.getValue() ) {
			throw new InputErrorException( "The input for MaxValue must be greater than that for MinValue.");
		}
	}

	@Override
	public void earlyInit() {
		super.earlyInit();

		// Initialise the sample statistics
		sampleCount = 0;
		sampleSum = 0.0;
		sampleSquaredSum = 0.0;
		sampleMin = Double.POSITIVE_INFINITY;
		sampleMax = Double.NEGATIVE_INFINITY;

		lastSample = getMeanValue(0);
	}

	@Override
	public void updateForInput(Input<?> in) {
		super.updateForInput(in);

		if (in == unitType) {
			setUnitType(getUnitType());
			FrameBox.reSelectEntity();  // Update the units in the Output Viewer
			return;
		}
	}

	@Override
	public void setInputsForDragAndDrop() {
		super.setInputsForDragAndDrop();

		// Find the largest seed used so far
		int seed = 0;
		for (Distribution dist : Entity.getClonesOfIterator(Distribution.class)) {
			seed = Math.max(seed, dist.getStreamNumber());
		}

		// Set the random number seed next unused value
		InputAgent.applyArgs(this, "RandomSeed", String.format("%s", seed+1));
	}

	@Override
	public Class<? extends Unit> getUserUnitType() {
		return unitType.getUnitType();
	}

	/**
	 * Select the next sample from the probability distribution.
	 */
	protected abstract double getNextSample();

	@Override
	public Class<? extends Unit> getUnitType() {
		return unitType.getUnitType();
	}

	protected void setUnitType(Class<? extends Unit> ut) {
		minValueInput.setUnitType(ut);
		maxValueInput.setUnitType(ut);
	}

	protected int getStreamNumber() {
		return randomSeedInput.getValue();
	}

	public static int getSubstreamNumber() {
		return Simulation.getSubstreamNumber();
	}

	/**
	 * Returns the next sample from the probability distribution.
	 */
	@Output(name = "Value",
	        description = "The last sampled value in the distribution.",
	        unitType = UserSpecifiedUnit.class)
	@Override
	public final double getNextSample(double simTime) {
		// If we are not in a model context, do not perturb the distribution by sampling,
		// instead simply return the last sampled value
		if (!EventManager.hasCurrent()) {
			return lastSample;
		}

		// Loop until the select sample falls within the desired min and max values
		double nextSample;
		do {
			nextSample = this.getNextSample();
		}
		while (nextSample < this.minValueInput.getValue() ||
		       nextSample > this.maxValueInput.getValue());

		lastSample = nextSample;

		// Collect statistics on the sampled values
		sampleCount++;
		sampleSum += nextSample;
		sampleSquaredSum += nextSample * nextSample;
		sampleMin = Math.min(sampleMin, nextSample);
		sampleMax = Math.max(sampleMax, nextSample);
		return nextSample;
	}

	@Override
	public double getMinValue() {
		return minValueInput.getValue();
	}

	@Override
	public double getMaxValue() {
		return maxValueInput.getValue();
	}

	/**
	 * Returns the mean value for the distribution calculated from the inputs.  It is NOT the mean of the sampled values.
	 */
	protected abstract double getMeanValue();

	/**
	 * Returns the standard deviation for the distribution calculated from the inputs.  It is NOT the standard deviation of the sampled values.
	 */
	protected abstract double getStandardDeviation();

	@Output( name="CalculatedMean",
			 description="The mean of the probability distribution calculated directly from the inputs.  " +
					"It is NOT the mean of the sampled values.  The inputs for MinValue and MaxValue are ignored.",
			 unitType=UserSpecifiedUnit.class)
	@Override
	public double getMeanValue( double simTime ) {
		return this.getMeanValue();
	}

	@Output( name="CalculatedStandardDeviation",
			 description="The standard deviation of the probability distribution calculated directly from the inputs.  " +
					"It is NOT the standard deviation of the sampled values.  The inputs for MinValue and MaxValue are ignored.",
			 unitType=UserSpecifiedUnit.class)
	public double getStandardDeviation( double simTime ) {
		return this.getStandardDeviation();
	}

	@Output( name="NumberOfSamples",
			 description="The number of times the probability distribution has been sampled.",
			 unitType=DimensionlessUnit.class)
	public int getNumberOfSamples( double simTime ) {
		return sampleCount;
	}

	@Output( name="SampleMean",
			 description="The mean of the values sampled from the probability distribution.",
			 unitType=UserSpecifiedUnit.class)
	public double getSampleMean( double simTime ) {
		return sampleSum / sampleCount;
	}

	@Output( name="SampleStandardDeviation",
			 description="The standard deviation of the values sampled from the probability distribution.",
			 unitType=UserSpecifiedUnit.class)
	public double getSampleStandardDeviation( double simTime ) {
		double sampleMean = sampleSum / sampleCount;
		return Math.sqrt( sampleSquaredSum/sampleCount - sampleMean*sampleMean );
	}

	@Output( name="SampleMin",
			 description="The minimum of the values sampled from the probability distribution.",
			 unitType=UserSpecifiedUnit.class)
	public double getSampleMin( double simTime ) {
		return sampleMin;
	}

	@Output( name="SampleMax",
			 description="The maximum of the values sampled from the probability distribution.",
			 unitType=UserSpecifiedUnit.class)
	public double getSampleMax( double simTime ) {
		return sampleMax;
	}
}
