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
package com.jaamsim.FluidObjects;

import com.jaamsim.Graphics.PolylineInfo;
import com.jaamsim.input.ColourInput;
import com.jaamsim.input.Input;
import com.jaamsim.input.Keyword;
import com.jaamsim.input.Output;
import com.jaamsim.input.ValueInput;
import com.jaamsim.units.DimensionlessUnit;
import com.jaamsim.units.DistanceUnit;

/**
 * FluidPipe is a pipe through which fluid can flow.
 * @author Harry King
 *
 */
public class FluidPipe extends FluidComponent {

	@Keyword(description = "The length of the pipe.",
	         example = "Pipe1 Length { 10.0 m }")
	private final ValueInput lengthInput;

	@Keyword(description = "The height change over the length of the pipe.  " +
			"Equal to (outlet height - inlet height).",
	         example = "Pipe1 HeightChange { 0.0 }")
	private final ValueInput heightChangeInput;

	@Keyword(description = "The roughness height of the inside pipe surface.  " +
			"Used to calculate the Darcy friction factor for the pipe.",
	         example = "Pipe1 Roughness { 0.01 m }")
	private final ValueInput roughnessInput;

	@Keyword(description = "The pressure loss coefficient or 'K-factor' for the pipe.  " +
			"The factor multiplies the dynamic pressure and is applied as a loss at the pipe outlet.",
	         example = "Pipe1 PressureLossCoefficient { 0.5 }")
	private final ValueInput pressureLossCoefficientInput;

	@Keyword(description = "The width of the pipe segments in pixels.",
	         example = "Pipe1 Width { 1 }")
	private final ValueInput widthInput;

	@Keyword(description = "The colour of the pipe, defined using a colour keyword or RGB values.",
	         example = "Pipe1 Colour { red }")
	private final ColourInput colourInput;

	private double darcyFrictionFactor;  // The Darcy Friction Factor for the pipe flow.

	{
		lengthInput = new ValueInput( "Length", "Key Inputs", 1.0d);
		lengthInput.setValidRange( 0.0, Double.POSITIVE_INFINITY);
		lengthInput.setUnitType( DistanceUnit.class );
		this.addInput( lengthInput);

		heightChangeInput = new ValueInput( "HeightChange", "Key Inputs", 0.0d);
		heightChangeInput.setValidRange( Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		heightChangeInput.setUnitType( DistanceUnit.class );
		this.addInput( heightChangeInput);

		roughnessInput = new ValueInput( "Roughness", "Key Inputs", 0.0d);
		roughnessInput.setValidRange( 0.0, Double.POSITIVE_INFINITY);
		roughnessInput.setUnitType( DistanceUnit.class );
		this.addInput( roughnessInput);

		pressureLossCoefficientInput = new ValueInput( "PressureLossCoefficient", "Key Inputs", 0.0d);
		pressureLossCoefficientInput.setValidRange( 0.0, Double.POSITIVE_INFINITY);
		pressureLossCoefficientInput.setUnitType( DimensionlessUnit.class );
		this.addInput( pressureLossCoefficientInput);

		widthInput = new ValueInput("Width", "Key Inputs", 1.0d);
		widthInput.setValidRange(1.0d, Double.POSITIVE_INFINITY);
		widthInput.setUnitType( DimensionlessUnit.class );
		this.addInput(widthInput);

		colourInput = new ColourInput("Colour", "Key Inputs", ColourInput.BLACK);
		this.addInput(colourInput);
		this.addSynonym(colourInput, "Color");
	}

	@Override
	public double calcOutletPressure( double inletPres, double flowAccel ) {

		double dyn = this.getDynamicPressure();  // Note that dynamic pressure is negative for negative velocities
		double pres = inletPres;
		pres -= this.getFluid().getDensityxGravity() * heightChangeInput.getValue();
		if( Math.abs(dyn) > 0.0 && this.getFluid().getViscosity() > 0.0 ) {
			this.setDarcyFrictionFactor();
			pres -= darcyFrictionFactor * dyn * this.getLength() / this.getDiameter();
		}
		else {
			darcyFrictionFactor = 0.0;
		}
		pres -= pressureLossCoefficientInput.getValue() * dyn;
		pres -= flowAccel * this.getFluid().getDensity() * lengthInput.getValue() / this.getFlowArea();
		return pres;
	}

	@Override
	public double getLength() {
		return lengthInput.getValue();
	}

	private void setDarcyFrictionFactor() {

		double reynoldsNumber = this.getReynoldsNumber();

		// Laminar Flow
		if( reynoldsNumber < 2300.0 ) {
			darcyFrictionFactor = this.getLaminarFrictionFactor( reynoldsNumber );
		}
		// Turbulent Flow
		else if( reynoldsNumber > 4000.0 ) {
			darcyFrictionFactor = this.getTurbulentFrictionFactor( reynoldsNumber );
		}
		// Transitional Flow
		else {
			darcyFrictionFactor = 0.5 * ( this.getLaminarFrictionFactor(reynoldsNumber) + this.getTurbulentFrictionFactor(reynoldsNumber) );
		}
	}

	/*
	 * Return the Darcy Friction Factor for a laminar flow.
	 */
	private double getLaminarFrictionFactor( double reynoldsNumber ) {
		return 64.0 / reynoldsNumber;
	}

	/*
	 * Return the Darcy Friction Factor for a turbulent flow.
	 */
	private double getTurbulentFrictionFactor( double reynoldsNumber ) {
		double x = 1.0;  // The present value for x = 1 / sqrt( frictionfactor ).
		double lastx = 0.0;

		double a = ( roughnessInput.getValue() / this.getDiameter() ) / 3.7;
		double b = 2.51 / reynoldsNumber;

		int n = 0;
		while( Math.abs(x-lastx)/lastx > 1.0e-10 && n < 20 ) {
			lastx = x;
			x = -2.0 * Math.log10( a + b*lastx );
			n++;
		}

		if( n >= 20 ) {
			error("Darcy Friction Factor iterations did not converge: lastx = %f  x = %f  n = %d",
			      lastx, x, n);
		}

		return 1.0 / ( x * x );
	}

	@Override
	public void updateForInput( Input<?> in ) {
		super.updateForInput(in);

		// If Points were input, then use them to set the start and end coordinates
		if( in == pointsInput || in == colourInput || in == widthInput ) {
			synchronized(screenPointLock) {
				cachedPointInfo = null;
			}
			return;
		}
	}

	@Override
	public PolylineInfo[] getScreenPoints() {
		synchronized(screenPointLock) {
			if (cachedPointInfo == null) {
				int w = Math.max(1, widthInput.getValue().intValue());
				cachedPointInfo = new PolylineInfo[1];
				cachedPointInfo[0] = new PolylineInfo(pointsInput.getValue(), colourInput.getValue(), w);
			}
			return cachedPointInfo;
		}
	}

	@Output(name = "DarcyFrictionFactor",
	 description = "The Darcy Friction Factor for the pipe.",
	    unitType = DimensionlessUnit.class)
	public double getDarcyFrictionFactor(double simTime) {
		return darcyFrictionFactor;
	}

}
