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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.jaamsim.render.RenderUtils;

/**
 * A convex hull that is initialized by a set of points
 * @author Matt.Chudleigh
 *
 */
public class ConvexHull {

	private ArrayList<Vector4d> _verts;

	private boolean _isDegenerate = false;

	private ArrayList<HullFace> _faces = new ArrayList<HullFace>();

	private double _radius;

	public static ConvexHull TryBuildHull(ArrayList<Vector4d> verts, int numAttempts) {
		ArrayList<Vector4d> baseVerts = removeDoubles(verts);

		assert(numAttempts > 0);

		// bad input indicates a hull that will naturally be degenerate, just return the first attempt
		boolean badInput = baseVerts.size() < 3;

		ConvexHull ret = null;
		for (int i = 0; i < numAttempts; ++i) {
			int seed = (int)((double)i/(double)numAttempts);

			ret = new ConvexHull(baseVerts, seed);
			if (!ret._isDegenerate || badInput) {
				return ret;
			}
			// The mesh was degenerate, loop and try again
		}

		// We fell through, so return the last degenerate attempt
		return ret;
	}

	/**
	 * Initialize this hull from the vertices provided. This is an implementation of the QuickHull algorithm (or close enough to it)
	 * @param verts
	 */
	public ConvexHull(ArrayList<Vector4d> baseVerts, int seed) {

		assert(seed >= 0);
		assert(seed < 1);


		if (baseVerts.size() < 3) {
			// This mesh is too small, so just create an empty Hull... or should we throw?
			makeDegenerate(baseVerts);
			return;
		}

		// Start by finding 3 points to build the original faces, this may have a problem if there is only
		// 3 points and all are in a line
		ArrayList<TempHullFace> tempFaces = new ArrayList<TempHullFace>();
		List<Integer> unclaimedPoints = new ArrayList<Integer>();

		// Create two starting faces (both use the same verts but are wound backwards to face in both directions)

		int ind0 = baseVerts.size() * seed;
		Vector4d v0 = baseVerts.get(0);
		double bestDist = 0;
		int ind1 = 0;
		Vector4d temp = new Vector4d();
		for (int i = 0; i < baseVerts.size(); ++i) {
			if (i == ind0) continue;

			// Ind1 is the furthest vertex from ind0
			v0.sub3(baseVerts.get(i), temp);
			double dist = temp.mag3();
			if (dist > bestDist) {
				bestDist = dist;
				ind1 = i;
			}
		}
		// Now ind2 is the vertex furthest from the line of the above two
		bestDist = 0;
		Vector4d dir = new Vector4d();
		v0.sub3(baseVerts.get(ind1), dir);
		dir.normalizeLocal3();
		int ind2 = 0;
		for (int i = 1; i < baseVerts.size(); ++i) {
			if (i == ind0) continue;
			if (i == ind1) continue;

			v0.sub3(baseVerts.get(i), temp);
			dir.cross(temp, temp);
			double dist = temp.mag3();
			if (dist > bestDist) {
				bestDist = dist;
				ind2 = i;
			}
		}

		if (ind1 == ind0 ||
		    ind2 == ind0 ||
		    ind1 == ind2) {
			makeDegenerate(baseVerts);
			return;
		}

		TempHullFace f0 = new TempHullFace(ind0, ind1, ind2, baseVerts);
		TempHullFace f1 = new TempHullFace(ind0, ind2, ind1, baseVerts);
		tempFaces.add(f0);
		tempFaces.add(f1);

		// Make sure the planes do not face each other
		assert(f0.plane.getNormalRef().dot3(f1.plane.getNormalRef()) < 0.9999 );

		boolean planar = true;

		// Assign all the remaining points to either of the faces if that face can 'see' the vertex
		for (int i = 0; i < baseVerts.size(); ++i) {
			double dist = f0.plane.getNormalDist(baseVerts.get(i));

			if (dist > 0.000001) {
				f0.addPoint(i, baseVerts);
				planar = false;
			} else if (dist < -0.000001){
				f1.addPoint(i, baseVerts);
				planar = false;
			} else {
				unclaimedPoints.add(i);
			}
		}

		if (planar) {
			// Damn... for now fall back to a degenerate hull
			makeDegenerate(baseVerts);
			return;
		}

		// Initialization is complete, start the core loop
		while (true) {
			// Find any faces with points assigned to it
			TempHullFace f = null;
			bestDist = 0.001; // A non zero value to quick out if the closest points aren't that far
			for (TempHullFace ft : tempFaces) {
				if (ft.points.size() != 0 && ft.furthestDist > bestDist) {
					f = ft;
					bestDist = ft.furthestDist;
					break;
				}
			}
			if (f == null) {
				// There's no remaining points unassigned, we're done.
				break;
			}

			// Find the point assigned to this face that is the furthest away
			int farInd = f.furthestInd;

			// Remove any faces that can see this point and orphan any points owned by these faces
			Vector4d farVert = baseVerts.get(farInd);

			ArrayList<TempHullFace> deadFaces = new ArrayList<TempHullFace>();

			for (Iterator<TempHullFace> it = tempFaces.iterator(); it.hasNext(); ) {
				TempHullFace tempFace = it.next();

				if (tempFace.plane.getNormalDist(farVert) > -0.000000001) { // Non zero to allow a bit of floating point round off and avoid degenerate faces
					// This face can see this point, and is therefore not part of the hull
					deadFaces.add(tempFace);
					it.remove();
				}

			}

			// The points that are no longer associated with a face
			List<Integer> orphanedPoints = new LinkedList<Integer>();
			orphanedPoints.addAll(unclaimedPoints);
			unclaimedPoints.clear();

			// Find all the open edges left by removing these faces
			ArrayList<HullEdge> edges = new ArrayList<HullEdge>();
			for (TempHullFace df : deadFaces) {
				orphanedPoints.addAll(df.points);

				edges.add(new HullEdge(df.indices[0], df.indices[1]));
				edges.add(new HullEdge(df.indices[1], df.indices[2]));
				edges.add(new HullEdge(df.indices[2], df.indices[0]));
			}

			// Remove double edges (to make sure we have a single loop)
			pruneEdges(edges);

			// Scan the loop to make sure it's a single loop
			if (!scanEdges(edges, baseVerts)) {
				// This hull diverged
				makeDegenerate(baseVerts);
				return;
			}

			ArrayList<TempHullFace> newFaces = new ArrayList<TempHullFace>();

			// Build new faces from the remaining edges and assign all remaining points
			for (HullEdge e : edges) {
				TempHullFace newFace = new TempHullFace(e.ind0, e.ind1, farInd, baseVerts);
				tempFaces.add(newFace);

				newFaces.add(newFace);

			} // end of building new faces

			// Add each orphaned point to the new face it is the furthest away from (by normal distance)
			Iterator<Integer> orph_it = orphanedPoints.iterator();
			while(orph_it.hasNext()) {

				int ind = orph_it.next();

				TempHullFace bestFace = null;
				bestDist = -1;

				for (TempHullFace tf : newFaces) {
					double dist = tf.plane.getNormalDist(baseVerts.get(ind));
					if (dist > 0.000001 && dist > bestDist) {
						bestFace = tf;
						bestDist = dist;
					}
				}
				if (bestFace != null) {
					bestFace.addPoint(ind, baseVerts);
					orph_it.remove();
				}
			}

			// This is the end of the main loop, check that at least one point has been claimed

			if (orphanedPoints.size() == 0) {
				// We have run out of points, so let's just call this good enough
				break;
			}

		} // End of main loop

		_radius = 0.0;

		// Now that we have all the faces we can create a real subset of points we care about
		ArrayList<Vector4d> realVerts = new ArrayList<Vector4d>();
		for (TempHullFace tf : tempFaces) {
			HullFace realFace = new HullFace();
			for (int i = 0; i < 3; ++i) {

				Vector4d oldVert = baseVerts.get(tf.indices[i]);

				int newInd = realVerts.size();

				for (int j = 0; j < realVerts.size(); ++j) {
					if (oldVert.equals(realVerts.get(j))) {
						// This vertex has already be included in the final list
						newInd = j;
					}
				}
				if (newInd == realVerts.size()) {
					// This vertex isn't in the new list, so add it and update the radius
					realVerts.add(oldVert);

					double rad = oldVert.mag3();
					if (rad > _radius) { _radius = rad; }
				}
				realFace.indices[i] = newInd;
			}

			_faces.add(realFace);
		}

		// swap out our vertex list to the real one
		_verts = realVerts;

	} // End of ConvexHull() Constructor

	/**
	 * Remove any doubles from the list and return a new, possibly shorter list
	 * @param orig
	 * @return
	 */
	private static ArrayList<Vector4d> removeDoubles(List<Vector4d> orig) {
		ArrayList<Vector4d> ret = new ArrayList<Vector4d>();
		if (orig.size() == 0) {
			return ret;
		}

		List<Vector4d> copy = new ArrayList<Vector4d>(orig);

		Collections.sort(copy, Vector4d.COMP);


		ret.add(copy.get(0));
		int outIndex = 0; // An updated index of the last element of the returned set, this may be
		for (int index = 1;index < copy.size(); ++index) {
			if (!copy.get(index).equals(ret.get(outIndex))) {
				// We have not seen this vector before
				ret.add(copy.get(index));
				++outIndex;
			}
		}

		return ret;
	}

	// Remove any edge that is back tracked over, alter 'edges' in place
	private void pruneEdges(ArrayList<HullEdge> edges) {
		for (int i = 0; i < edges.size(); ) {

			HullEdge e0 = edges.get(i);
			boolean keepEdge = true;

			for (int j = i;  j < edges.size(); ++j) {

				HullEdge e1 = edges.get(j);

				if (e0.ind0 == e1.ind1 && e0.ind1 == e1.ind0) {
					keepEdge = false;
					edges.remove(j); // Remove j, which is always higher than i so this should be safe
					break;
				}
			}

			if (keepEdge) {
				++i;
			} else {
				edges.remove(i);
			}
		}
	}

	private boolean scanEdges(ArrayList<HullEdge> edges, ArrayList<Vector4d> verts) {
		ArrayList<Integer> seenIndices = new ArrayList<Integer>();

		double[] edgeLengths = new double[edges.size()];
		int i = 0;
		Vector4d temp = new Vector4d();
		for (HullEdge e : edges) {
			if (!seenIndices.contains(e.ind0))
				seenIndices.add(e.ind0);
			if (!seenIndices.contains(e.ind1))
				seenIndices.add(e.ind1);

			verts.get(e.ind0).sub3(verts.get(e.ind1), temp);
			edgeLengths[i++] = temp.mag3();
		}


		//assert(seenIndices.size() == edges.size());
		return seenIndices.size() == edges.size();

	}

	/**
	 * Return the list of faces (a list of triplets of indices into the vertices) for this mesh
	 * @return
	 */
	public List<HullFace> getFaces() {
		return _faces;
	}

	public List<Vector4d> getVertices() {

		return _verts;
	}


	public double getRadius() {
		return _radius;
	}

	public boolean collides(Vector4d point, Transform trans) {
		// Simply use the AABB formed from the points for the degenerate cases
		if (_isDegenerate) {
			AABB aabb = getAABB(trans.getMatrixRef());
			return aabb.collides(point);
		}

		Transform inv = new Transform();
		trans.inverse(inv);

		// P is the point in hull space
		Vector4d p = new Vector4d();
		inv.apply(point, p);

		for (HullFace f : _faces) {
			Plane plane = faceToPlane(f);
			double dist = plane.getNormalDist(p);
			if (dist > 0) {
				// This point is outside at least one plane, so there is not intersection
				return false;
			}
		}
		return true;
	}

	public double collisionDistance(Ray r, Transform trans) {
		return collisionDistance(r, trans, Vector4d.ONES);
	}

	/**
	 * Check for collision with the provided ray, will return the distance to a collision, with a negative number being returned for no collsion
	 * @param r
	 * @param trans
	 * @return - distance to collision, or -1 if no collision
	 */
	public double collisionDistance(Ray r, Transform trans, Vector4d scale) {

		// Simply use the AABB formed from the points for the degenerate cases
		if (_isDegenerate) {
			AABB aabb = getAABB(RenderUtils.mergeTransAndScale(trans, scale));
			return aabb.collisionDist(r);
		}

		Matrix4d transMat = RenderUtils.getInverseWithScale(trans, scale);

		// hullRay is the point in hull space
		Ray hullRay = r.transform(transMat);

		double back = Double.MAX_VALUE;
		double front = -Double.MAX_VALUE;

		for (HullFace f : _faces) {
			Plane plane = faceToPlane(f);

			boolean bBackFace = plane.backFaceCollision(hullRay);

			double dist = plane.collisionDist(hullRay);

			if (Double.isInfinite(dist)) {
				continue; // Parallel ray and plane
			}

			if ( bBackFace && dist <  back) {  back = dist; }
			if (!bBackFace && dist > front) { front = dist; }
		}

		// We now know the extreme points and can figure out if this collides or not
		if (back < front) { return -1.0; } // No collision

		if (back < 0 && front < 0) { return -1.0; } // Collision behind the start of the ray

		if (front < 0) {
			// The ray starts from inside the object
			return 0.0;
		}

		return front;
	}

	/**
	 * A class that represents a single face of the hull during hull construction. The 'points' member is the list of external
	 * points not yet encompasses by the hull and are visible by this temporary face
	 * @author Matt.Chudleigh
	 *
	 */
	private class TempHullFace {
		public int[] indices = new int[3];
		public Plane plane;
		public double furthestDist = 0;
		public int furthestInd = 0;
		public ArrayList<Integer> points = new ArrayList<Integer>();

		public TempHullFace(int i0, int i1, int i2, ArrayList<Vector4d> verts) {
			indices[0] = i0;
			indices[1] = i1;
			indices[2] = i2;
			plane = new Plane(verts.get(indices[0]),
			                  verts.get(indices[1]),
			                  verts.get(indices[2]));

		}
		public void addPoint(int ind, ArrayList<Vector4d> verts) {
			Vector4d v = verts.get(ind);
			double dist = plane.getNormalDist(v);
			if (dist >= furthestDist) {
				furthestDist = dist;
				furthestInd = ind;
			}
			assert(dist > -0.000001);
			points.add(ind);
		}

	}

	/**
	 * The main hull face storage class, simply a list of indices
	 * @author Matt.Chudleigh
	 *
	 */
	public class HullFace {
		public int[] indices = new int[3];
	}

	// A really dumb class to store edges
	private class HullEdge {
		int ind0;
		int ind1;
		HullEdge(int i0, int i1) {
			ind0 = i0; ind1 = i1;
		}
	}

	private Plane faceToPlane(HullFace f) {
		return new Plane(_verts.get(f.indices[0]),
                         _verts.get(f.indices[1]),
                         _verts.get(f.indices[2]));

	}

	private void makeDegenerate(ArrayList<Vector4d> vs) {
		_isDegenerate = true;
		_verts = vs;
		_faces = new ArrayList<HullFace>();
		// Figure out a radius
		_radius = 0;
		for (Vector4d v : vs) {
			if (v.mag3() > _radius) {
				_radius = v.mag3();
			}
		}
	}

	/**
	 * Get an world space AABB if this hull were transformed by 't'
	 * @param t
	 * @return
	 */
	public AABB getAABB(Matrix4d mat) {
		return new AABB(_verts, mat);
	}
}
