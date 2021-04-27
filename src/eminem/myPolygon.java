package eminem;

import java.awt.Dimension;
import java.awt.Polygon;
import java.util.Arrays;

public class myPolygon implements Comparable {
	Polygon polygon;

	public myPolygon(Polygon p) {
		polygon = p;
	}

	public int getArea() {
		Dimension dim = polygon.getBounds().getSize();
		return (dim.height * dim.width);

	}
	@Override
	public boolean equals(Object obj) {
		Polygon p1= this.polygon;
		Polygon p2 = ((myPolygon)obj).polygon;
		return Arrays.equals(p1.xpoints, p2.xpoints) && Arrays.equals(p1.ypoints, p2.ypoints) ;
	}
	
	@Override
	public String toString() {
		String s="";
		for (int j = 0; j < this.polygon.npoints; j++) {
			s += "(" + polygon.xpoints[j] + ","
					+ polygon.ypoints[j] + "),";
		}
		return s.substring(0, s.length()-1);
	}

	@Override
	public int compareTo(Object o) {
		int a1 = this.getArea();
		int a2 = ((myPolygon) o).getArea();

		return ((Integer)a1).compareTo(a2);
	}
}
