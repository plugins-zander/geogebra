package geogebra3D.kernel3D;

import geogebra.common.kernel.Construction;
import geogebra.common.kernel.Matrix.Coords;
import geogebra.common.kernel.arithmetic.NumberValue;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoPolygon;
import geogebra.common.kernel.kernelND.GeoConicND;
import geogebra.common.kernel.kernelND.GeoSegmentND;

/**
 * Algo for cylinder/cone from a conic and a height
 * @author mathieu
 *
 */
public abstract class AlgoQuadricLimitedConicHeight extends AlgoElement3D {

	//input
	private GeoConicND bottom;
	private NumberValue height;
	
	//output
	private GeoQuadric3DPart side;
	protected GeoConic3D top;
	private GeoQuadric3DLimited quadric;
	
	/**
	 * 
	 * @param c construction
	 * @param labels labels
	 * @param bottom bottom side
	 * @param height height
	 * @param type type (cylinder/cone)
	 */
	public AlgoQuadricLimitedConicHeight(Construction c, String[] labels, GeoConicND bottom, NumberValue height, int type) {
		super(c);
		
		this.bottom=bottom;
		this.height=height;
		
		quadric=new GeoQuadric3DLimited(c);
		quadric.setType(type);

		input = new GeoElement[] {bottom,(GeoElement) height};
		
		((GeoElement) bottom).addAlgorithm(this);
		((GeoElement) height).addAlgorithm(this);
		
    	// parent of output
        quadric.setParentAlgorithm(this);       
        cons.addToAlgorithmList(this); 
        
		compute();
        
 		AlgoQuadricSide algo = new AlgoQuadricSide(cons, quadric,true);            
		cons.removeFromConstructionList(algo);
		side = (GeoQuadric3DPart) algo.getQuadric();
		
		createTop();

		quadric.setParts(side,bottom,top);

		//output = new GeoElement[] {quadric,bottom,top,side};
		setOutput();
		
		quadric.initLabelsNoBottom(labels);
		quadric.updatePartsVisualStyle();			
	}
	
	/**
	 * sets the output
	 */
	abstract protected void setOutput();
	
	/**
	 * create the top side
	 */
	abstract protected void createTop();
	
	@Override
	public void compute() {
		
		
		Coords o = bottom.getMidpoint3D();	
		
		//TODO cylinder with other conics (than circles)
		double r = bottom.getHalfAxis(0);
		
		double altitude = height.getDouble();
		
		Coords d = bottom.getMainDirection().normalize();
		
		Coords o2 = o.add(d.mul(altitude));
		
		quadric.setDefined();
		
		setQuadric(o,o2,d,r, 0, altitude);

		quadric.calcVolume();
		
	}

	abstract protected void setQuadric(Coords o1, Coords o2, Coords d, double r, double min, double max);

	
	public GeoQuadric3DLimited getQuadric(){
		return quadric;
	}
	
	//compute and update quadric (for helper algos)
	@Override
	public void update() {
        compute();
        quadric.update();
    }

	

	@Override
	protected void getOutputXML(StringBuilder sb){
		super.getOutputXML(sb);

		//append XML for bottom once more, to avoid override of specific properties		
		bottom.getXML(sb);
	
		
	}
}
