package geogebra.common.kernel.commands;


import geogebra.common.kernel.Kernel;
import geogebra.common.kernel.arithmetic.Command;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoLine;
import geogebra.common.kernel.geos.GeoPoint2;
import geogebra.common.kernel.geos.GeoVector;
import geogebra.common.main.MyError;


/**
 * Line[ <GeoPoint>, <GeoPoint> ] 
 * Line[ <GeoPoint>, <GeoVector> ] 
 * Line[ <GeoPoint>, <GeoLine> ]
 */
public class CmdLine extends CommandProcessor {
	/**
	 * Create new command processor
	 * 
	 * @param kernel
	 *            kernel
	 */
	public CmdLine(Kernel kernel) {
		super(kernel);
	}
	
@Override
public GeoElement[] process(Command c) throws MyError {
    int n = c.getArgumentNumber();
    boolean[] ok = new boolean[n];
    GeoElement[] arg;

    switch (n) {
        case 2 :
            arg = resArgs(c);

            // line through two points
            if ((ok[0] = (arg[0] .isGeoPoint()))
                && (ok[1] = (arg[1] .isGeoPoint()))) {
                GeoElement[] ret =
                    {
                         kernelA.Line(
                            c.getLabel(),
                            (GeoPoint2) arg[0],
                            (GeoPoint2) arg[1])};
                return ret;
            }

            // line through point with direction vector
            else if (
                (ok[0] = (arg[0] .isGeoPoint()))
                    && (ok[1] = (arg[1] .isGeoVector()))) {
                GeoElement[] ret =
                    {
                         kernelA.Line(
                            c.getLabel(),
                            (GeoPoint2) arg[0],
                            (GeoVector) arg[1])};
                return ret;
            }

            // line through point parallel to another line
            else if (
                (ok[0] = (arg[0] .isGeoPoint()))
                    && (ok[1] = (arg[1] .isGeoLine()))) {
                GeoElement[] ret =
                    {
                         kernelA.Line(
                            c.getLabel(),
                            (GeoPoint2) arg[0],
                            (GeoLine) arg[1])};
                return ret;
            }

            // syntax error
            else {
                throw argErr(app, c.getName(), getBadArg(ok,arg));
            }

        default :
            throw argNumErr(app, c.getName(), n);
    }
}
}
