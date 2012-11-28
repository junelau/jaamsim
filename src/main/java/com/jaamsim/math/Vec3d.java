/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2012 Ausenco Engineering Canada Inc.
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
package com.jaamsim.math;

public class Vec3d extends Vec2d {

public double z;

/**
 * Construct a Vec3d initialized to (0,0,0);
 */
public Vec3d() {
	x = 0.0d;
	y = 0.0d;
	z = 0.0d;
}

/**
 * Construct a Vec3d initialized to (v.x, v.y, v.z);
 * @param v the Vec3d containing the initial values
 * @throws NullPointerException if v is null
 */
public Vec3d(Vec3d v) {
	x = v.x;
	y = v.y;
	z = v.z;
}

/**
 * Construct a Vec3d initialized to (x, y, z);
 * @param x the initial x value
 * @param y the initial y value
 * @param z the initial z value
 */
public Vec3d(double x, double y, double z) {
	this.x = x;
	this.y = y;
	this.z = z;
}

/**
 * Set this Vec3d with the values (v.x, v.y, v.z);
 * @param v the Vec3d containing the values
 * @throws NullPointerException if v is null
 */
public void set3(Vec3d v) {
	this.x = v.x;
	this.y = v.y;
	this.z = v.z;
}

/**
 * Set this Vec3d with the values (x, y, z);
 */
public void set3(double x, double y, double z) {
	this.x = x;
	this.y = y;
	this.z = z;
}

/**
 * Add v to this Vec3d: this = this + v
 * @throws NullPointerException if v is null
 */
public void add3(Vec3d v) {
	this.x = this.x + v.x;
	this.y = this.y + v.y;
	this.z = this.z + v.z;
}

/**
 * Add v1 to v2 into this Vec3d: this = v1 + v2
 * @throws NullPointerException if v1 or v2 are null
 */
public void add3(Vec3d v1, Vec3d v2) {
	this.x = v1.x + v2.x;
	this.y = v1.y + v2.y;
	this.z = v1.z + v2.z;
}

/**
 * Returns a new Vec3d initialized to v1 + v2
 * @throws NullPointerException if v1 or v2 are null
 */
public static final Vec3d getAdd3(Vec3d v1, Vec3d v2) {
	Vec3d tmp = new Vec3d(v1);
	tmp.add3(v2);
	return tmp;
}

/**
 * Subtract v from this Vec3d: this = this - v
 * @throws NullPointerException if v is null
 */
public void sub3(Vec3d v) {
	this.x = this.x - v.x;
	this.y = this.y - v.y;
	this.z = this.z - v.z;
}

/**
 * Subtract v2 from v1 into this Vec3d: this = v1 - v2
 * @throws NullPointerException if v1 or v2 are null
 */
public void sub3(Vec3d v1, Vec3d v2) {
	this.x = v1.x - v2.x;
	this.y = v1.y - v2.y;
	this.z = v1.z - v2.z;
}

/**
 * Returns a new Vec3d initialized to v1 - v2
 * @throws NullPointerException if v1 or v2 are null
 */
public static final Vec3d getSub3(Vec3d v1, Vec3d v2) {
	Vec3d tmp = new Vec3d(v1);
	tmp.sub3(v2);
	return tmp;
}

/**
 * Multiply the elements of this Vec3d by v: this = this * v
 * @throws NullPointerException if v is null
 */
public void mul3(Vec3d v) {
	this.x = this.x * v.x;
	this.y = this.y * v.y;
	this.z = this.z * v.z;
}

/**
 * Multiply the elements of v1 and v2 into this Vec3d: this = v1 * v2
 * @throws NullPointerException if v1 or v2 are null
 */
public void mul3(Vec3d v1, Vec3d v2) {
	this.x = v1.x * v2.x;
	this.y = v1.y * v2.y;
	this.z = v1.z * v2.z;
}

/**
 * Returns a new Vec3d initialized to v1 * v2
 * @throws NullPointerException if v1 or v2 are null
 */
public static final Vec3d getMul3(Vec3d v1, Vec3d v2) {
	Vec3d tmp = new Vec3d(v1);
	tmp.mul3(v2);
	return tmp;
}

/**
 * Set this Vec3d to the minimum of this and v: this = min(this, v)
 * @throws NullPointerException if v is null
 */
public void min3(Vec3d v) {
	this.x = Math.min(this.x, v.x);
	this.y = Math.min(this.y, v.y);
	this.z = Math.min(this.z, v.z);
}

/**
 * Set this Vec3d to the minimum of v1 and v2: this = min(v1, v2)
 * @throws NullPointerException if v is null
 */
public void min3(Vec3d v1, Vec3d v2) {
	this.x = Math.min(v1.x, v2.x);
	this.y = Math.min(v1.y, v2.y);
	this.z = Math.min(v1.z, v2.z);
}

/**
 * Returns a new Vec3d initialized to min(v1, v2)
 * @throws NullPointerException if v1 or v2 are null
 */
public static final Vec3d getMin3(Vec3d v1, Vec3d v2) {
	Vec3d tmp = new Vec3d(v1);
	tmp.min3(v2);
	return tmp;
}

/**
 * Set this Vec3d to the maximum of this and v: this = max(this, v)
 * @throws NullPointerException if v is null
 */
public void max3(Vec3d v) {
	this.x = Math.max(this.x, v.x);
	this.y = Math.max(this.y, v.y);
	this.z = Math.max(this.z, v.z);
}

/**
 * Set this Vec3d to the maximum of v1 and v2: this = max(v1, v2)
 * @throws NullPointerException if v is null
 */
public void max3(Vec3d v1, Vec3d v2) {
	this.x = Math.max(v1.x, v2.x);
	this.y = Math.max(v1.y, v2.y);
	this.z = Math.max(v1.z, v2.z);
}

/**
 * Returns a new Vec3d initialized to max(v1, v2)
 * @throws NullPointerException if v1 or v2 are null
 */
public static final Vec3d getMax3(Vec3d v1, Vec3d v2) {
	Vec3d tmp = new Vec3d(v1);
	tmp.max3(v2);
	return tmp;
}

/**
 * Return the 3-component dot product of v1 and v2
 * Internal helper to help with dot, mag and magSquared
 */
private final double _dot3(Vec3d v1, Vec3d v2) {
	double ret;
	ret  = v1.x * v2.x;
	ret += v1.y * v2.y;
	ret += v1.z * v2.z;
	return ret;
}

/**
 * Return the 3-component dot product of this Vec3d with v
 * @throws NullPointerException if v is null
 */
public double dot3(Vec3d v) {
	return _dot3(this, v);
}

/**
 * Return the 3-component magnitude of this Vec3d
 */
public double mag3() {
	return Math.sqrt(_dot3(this, this));
}

/**
 * Return the 3-component magnitude squared of this Vec3d
 */
public double magSquare3() {
	return _dot3(this, this);
}

/**
 * Normalize the first three components in-place
 *
 * If the Vec has a zero magnitude or contains NaN or Inf, this sets
 * all components but the last to zero, the last component is set to one.
 */
public void normalize3() {
	double mag = _dot3(this, this);
	if (nonNormalMag(mag)) {
		assert false;
		this.x = 0.0d;
		this.y = 0.0d;
		this.z = 1.0d;
		return;
	}

	mag = Math.sqrt(mag);
	this.x = this.x / mag;
	this.y = this.y / mag;
	this.z = this.z / mag;
}

/**
 * Set the first three components to the normalized values of v
 *
 * If the Vec has a zero magnitude or contains NaN or Inf, this sets
 * all components but the last to zero, the last component is set to one.
 * @throws NullPointerException if v is null
 */
public void normalize3(Vec3d v) {
	double mag = _dot3(v, v);
	if (nonNormalMag(mag)) {
		assert false;
		this.x = 0.0d;
		this.y = 0.0d;
		this.z = 1.0d;
	}
	else {
		mag = Math.sqrt(mag);
		this.x = v.x / mag;
		this.y = v.y / mag;
		this.z = v.z / mag;
	}
}

/**
 * Scale the first three components of this Vec: this = scale * this
 */
public void scale3(double scale) {
	this.x = this.x * scale;
	this.y = this.y * scale;
	this.z = this.z * scale;
}

/**
 * Scale the first three components of v into this Vec: this = scale * v
 * @throws NullPointerException if v is null
 */
public void scale3(double scale, Vec3d v) {
	this.x = v.x * scale;
	this.y = v.y * scale;
	this.z = v.z * scale;
}

/**
 * Returns a new Vec3d initialized to scale * v
 * @throws NullPointerException if v is null
 */
public static final Vec3d getScale3(double scale, Vec3d v) {
	Vec3d tmp = new Vec3d(v);
	tmp.scale3(scale);
	return tmp;
}

/**
 * Multiply v by m and store into this Vec: this = m x v
 * @throws NullPointerException if m or v are null
 */
public void mult3(Mat4d m, Vec3d v) {
	double _x = m.d00 * v.x + m.d01 * v.y + m.d02 * v.z;
	double _y = m.d10 * v.x + m.d11 * v.y + m.d12 * v.z;
	double _z = m.d20 * v.x + m.d21 * v.y + m.d22 * v.z;

	this.x = _x;
	this.y = _y;
	this.z = _z;
}

/**
 * Multiply m by v and store into this Vec: this = v x m
 * @throws NullPointerException if m or v are null
 */
public void mult3(Vec3d v, Mat4d m) {
	double _x = v.x * m.d00 + v.y * m.d10 + v.z * m.d20;
	double _y = v.x * m.d01 + v.y * m.d11 + v.z * m.d21;
	double _z = v.x * m.d02 + v.y * m.d12 + v.z * m.d22;

	this.x = _x;
	this.y = _y;
	this.z = _z;
}

/**
 * Set this Vec3d to the cross product of this and v: this = this X v
 * @throws NullPointerException if v is null
 */
public void cross3(Vec3d v) {
	// Use temp vars to deal with this passed in as the argument
	double _x = this.y * v.z - this.z * v.y;
	double _y = this.z * v.x - this.x * v.z;
	double _z = this.x * v.y - this.y * v.x;

	this.x = _x;
	this.y = _y;
	this.z = _z;
}

/**
 * Set this Vec3d to the cross product of v1 and v2: this = v1 X v2
 * @throws NullPointerException if v1 or v2 are null
 */
public void cross3(Vec3d v1, Vec3d v2) {
	// Use temp vars to deal with this passed in as the argument
	double _x = v1.y * v2.z - v1.z * v2.y;
	double _y = v1.z * v2.x - v1.x * v2.z;
	double _z = v1.x * v2.y - v1.y * v2.x;

	this.x = _x;
	this.y = _y;
	this.z = _z;
}

/**
 * Returns a new Vec3d initialized to v1 X v2
 * @throws NullPointerException if v1 or v2 are null
 */
public static final Vec3d getCross3(Vec3d v1, Vec3d v2) {
	Vec3d tmp = new Vec3d(v1);
	tmp.cross3(v2);
	return tmp;
}
}
