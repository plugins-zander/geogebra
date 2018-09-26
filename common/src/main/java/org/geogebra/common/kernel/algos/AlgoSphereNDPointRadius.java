/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

 */

/*
 * AlgoCirclePointRadius.java
 *
 * Created on 15. November 2001, 21:37
 */

package org.geogebra.common.kernel.algos;

import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumberValue;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.kernel.kernelND.GeoQuadricND;
import org.geogebra.common.kernel.kernelND.GeoSegmentND;
import org.geogebra.common.main.Feature;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * 
 * @author Markus + Mathieu
 * 
 * 
 *         Generalization of algo for circle/sphere
 */
public abstract class AlgoSphereNDPointRadius extends AlgoElement {

	private GeoPointND M; // input
	private GeoNumberValue r; // input
	private GeoElement rgeo;
	private GeoQuadricND sphereND; // output

	private int type;
	final static int TYPE_RADIUS = 0;
	final static int TYPE_SEGMENT = 1;

	/**
	 * @param cons
	 *            construction
	 * @param M
	 *            center
	 * @param r
	 *            radius
	 */
	public AlgoSphereNDPointRadius(Construction cons, GeoPointND M,
			GeoNumberValue r) {

		super(cons);

		type = TYPE_RADIUS;

		this.M = M;
		this.r = r;
		rgeo = r.toGeoElement();
		sphereND = createSphereND(cons);

		setInputOutput(); // for AlgoElement

		compute();

		if (kernel.getApplication().has(Feature.GEOMETRIC_DISCOVERY) && r.getLabelSimple() != null) {
			copyExistingStyle();
		}
	}

	private void copyExistingStyle() {
		// Search for the first circle with the same radius and use its color settings

		GeoNumberValue r = this.r;
		if (r == null) {
			return;
		}
		String rl = r.getLabelSimple();
		if (rl == null) {
			return;
		}

		TreeSet<GeoElement> geoSet = cons.getGeoSetConstructionOrder();

		Iterator<GeoElement> it = geoSet.iterator();
		while (it.hasNext()) {
			GeoElement geo = it.next();
			AlgoElement ae = geo.getParentAlgorithm();
			if (ae instanceof AlgoSphereNDPointRadius && !ae.equals(this.sphereND)) {
				AlgoSphereNDPointRadius sphereND2 = (AlgoSphereNDPointRadius) ae;
				GeoNumberValue r2 = sphereND2.r;
				if (r2 != null) {
					String r2l = r2.getLabelSimple();
					if (r2l != null && r2l.equals(rl)) {
						// unsure if all of these are required or there is a simpler way...
						this.sphereND.setObjColor(geo.getObjectColor());
						this.sphereND.setBackgroundColor(geo.getBackgroundColor());
						this.sphereND.setDecorationType(geo.getDecorationType());
						this.sphereND.setAlphaValue(geo.getAlphaValue());
						this.sphereND.setLineOpacity(geo.getLineOpacity());
						this.sphereND.setLineThickness(geo.getLineThickness());
						this.sphereND.setLineType(geo.getLineType());
						return;
					}
				}
			}
		}
	}


	protected AlgoSphereNDPointRadius(Construction cons, GeoPointND M,
			GeoSegmentND rgeo) {

		super(cons);

		type = TYPE_SEGMENT;

		this.M = M;
		this.rgeo = (GeoElement) rgeo;

		sphereND = createSphereND(cons);

		setInputOutput(); // for AlgoElement

		compute();
	}

	/**
	 * return a conic (2D) or a quadric (3D)
	 * 
	 * @param cons1
	 *            construction
	 * @return a conic (2D) or a quadric (3D)
	 */
	abstract protected GeoQuadricND createSphereND(Construction cons1);

	// for AlgoElement
	@Override
	protected void setInputOutput() {
		input = new GeoElement[2];
		input[0] = (GeoElement) M;
		input[1] = rgeo;

		super.setOutputLength(1);
		super.setOutput(0, sphereND);
		setDependencies(); // done by AlgoElement
	}

	/**
	 * @return resulting sphere
	 */
	public GeoQuadricND getSphereND() {
		return sphereND;
	}

	protected GeoPointND getM() {
		return M;
	}

	/**
	 * Method added for LocusEqu project.
	 * 
	 * @return center of sphere.
	 */
	public GeoPointND getCenter() {
		return this.getM();
	}

	protected GeoElement getRGeo() {
		return rgeo;
	}

	/**
	 * Method added for LocusEqu project.
	 * 
	 * @return radius of sphere.
	 */
	public GeoElement getRadiusGeo() {
		return this.getRGeo();
	}

	// compute circle with midpoint M and radius r
	@Override
	public final void compute() {
		switch (type) {
		default:
		case TYPE_RADIUS:
			sphereND.setSphereND(M, r.getDouble());
			break;
		case TYPE_SEGMENT:
			sphereND.setSphereND(M, (GeoSegment) rgeo);
			break;
		}
	}

	protected int getType() {
		return this.type;
	}

	/**
	 * resets the radius value for type TYPE_RADIUS
	 * 
	 * @param newRadius
	 *            the new radius
	 */
	public void setRadius(GeoNumberValue newRadius) {
		if (type == TYPE_RADIUS) {
			this.r = newRadius;
			this.rgeo = newRadius.toGeoElement();
			this.input[1] = rgeo;
			update();
		}
	}
}
