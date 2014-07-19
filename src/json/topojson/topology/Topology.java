package json.topojson.topology;

import java.util.HashMap;

import json.algorithm.DouglasPeucker;
import json.geojson.objects.Point;
import json.topojson.algorithm.ArcMap;
import json.topojson.geom.Object;

public class Topology {

	String type;
	Transform transform;
	HashMap<String,Object> objects;
	java.lang.Object[][][] arcs;
	transient boolean _quantized;
	
	public Topology(){
		type = "Topology";
		transform = null;
		arcs = null;
		_quantized = false;
	}
	
	public void addObject(String iName, Object iObject){
		if (objects==null) objects = new HashMap<String,Object>();
		objects.put(iName, iObject);
	}
	
	public void setArcs(ArcMap iArcMap){
		
		int max = -1;
		if (objects!=null) {
			
			for (Object aObject:objects.values()){
				int index = aObject.findMaxArcIndex();
				if (index>max) max=index;
			}
			max++;
		} else {
			max = iArcMap._arcs.size();
		}
		
		arcs = new java.lang.Object[max][][];
		
		int na = 0;
		for (Arc aArc : iArcMap._arcs) {
			
			arcs[na] = new java.lang.Object[aArc._points.length][];
					
			for (int i=0; i<aArc._points.length; i++){
				arcs[na][i] = new java.lang.Object[2];
				arcs[na][i][0] = new Double( aArc._points[i]._x );
				arcs[na][i][1] = new Double( aArc._points[i]._y );
			}
			
			na++;
			if (na>=max) break;
			
		}
		
	}
	
	public void simplify(int iFact){
		
		
		for (int i=0; i<arcs.length; i++){
			
			Point[] aPs = new Point[arcs[i].length];
			
			int n=0;
			for (java.lang.Object[] position:arcs[i]){
				aPs[n] = new Point((Double) position[0],(Double) position[1]); 
				n++;
			}
			
			aPs = DouglasPeucker.GDouglasPeucker(aPs, iFact);
			
			n=0;
			arcs[i] = new java.lang.Object[aPs.length][];
			for (int j=0; j<aPs.length; j++){
				
				arcs[i][j] = new java.lang.Object[2];
				arcs[i][j][0] = (Double) aPs[j].x;
				arcs[i][j][1] = (Double) aPs[j].y; 
				
			}
			
		}
		
	}
	
	public void quantize(double iPowTen){
		
		if (!_quantized) {
			
			double aX = 0;
			double aY = 0;
			int n=0;
			
			for (java.lang.Object[][] arc:arcs){
				for (java.lang.Object[] position:arc){
					aX += (Double) position[0]; 
					aY += (Double) position[1];
					n++;
				}
			}
			
			if (n>0) {
				aX = aX/n;
				aY = aY/n;
			}
			
			// Quantize
			double scale = Math.pow(10, iPowTen);
			for (java.lang.Object[][] arc:arcs){
				for (java.lang.Object[] position:arc){
					position[0] = new Integer((int) (((Double) position[0] - aX)*scale)); 
					position[1] = new Integer((int) (((Double) position[1] - aY)*scale));
					n++;
				}
			}
			
			// Delta compute
			for (java.lang.Object[][] arc:arcs){
				
				for (int i=arc.length-1; i>0; i--){
					
					arc[i][0] = (Integer) arc[i][0]-(Integer) arc[i-1][0]; 
					arc[i][1] = (Integer) arc[i][1]-(Integer) arc[i-1][1];
					
				}
			}
			
			double[] scales = new double[] { 1/scale, 1/scale };
			double[] translates = new double[] { aX, aY };
			
			transform = new Transform(scales, translates);
			
		}
		
	}

}