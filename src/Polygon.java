import java.awt.Color;
import java.awt.Rectangle;
import java.util.Arrays;


public class Polygon {
	private Vector3D[] vertices = new Vector3D[3];
	private final Vector3D[] orginalVertices = new Vector3D[3];
	
	private Edge[] edges = new Edge[3];
	
	private final Color reflectivity;
	
	private Vector3D normal;
	//private Rectangle bounds;
	
	public Polygon(float v1x, float v1y, float v1z, float v2x, float v2y, float v2z, 
				   float v3x, float v3y, float v3z, int r, int g, int b) {		
		orginalVertices[0] = new Vector3D(v1x, v1y, v1z);
		orginalVertices[1] = new Vector3D(v2x, v2y, v2z);
		orginalVertices[2] = new Vector3D(v3x, v3y, v3z);
		
		vertices[0] = new Vector3D(v1x, v1y, v1z);
		vertices[1] = new Vector3D(v2x, v2y, v2z);
		vertices[2] = new Vector3D(v3x, v3y, v3z);
		
		edges[0] = new Edge(this, vertices[0], vertices[1]);
		edges[1] = new Edge(this, vertices[1], vertices[2]);
		edges[2] = new Edge(this, vertices[2], vertices[0]);
		
		reflectivity  = new Color(r, g, b);
		
		normal = calculateNormal();
		
		//bounds = getBounds();
	}
	
	public Edge[] getEdges() {
		return edges;
	}

	public Vector3D[] getVertices() {
		return vertices;
	}

	public void setVertices(Vector3D[] vertices) {
		this.vertices = vertices;
	}

	public Vector3D[] getOrginalVertices() {
		return orginalVertices;
	}

	public Color getReflectivity() {
		return reflectivity;
	}

	private Vector3D calculateNormal() {
		Vector3D v1 = vertices[0];
		Vector3D v2 = vertices[1];
		Vector3D v3 = vertices[2];		
		return (v2.minus(v1)).crossProduct((v3.minus(v2)));		
	}

	
	public Vector3D getNormal() {
		return normal;
	}

	public float getMinY(){
		float min = Float.POSITIVE_INFINITY;
		for(int i = 0; i < vertices.length; i++){
			if(vertices[i].y < min) {
				min = vertices[i].y;
			}
		}
		return min;
	}
	
	public float getMaxY(){
		float max = Float.NEGATIVE_INFINITY;
		for(int i = 0; i < vertices.length; i++){
			if(vertices[i].y > max) { 
				max = vertices[i].y;
			}
		}
		return max;
	}
	
	public void apply(Transform transformMaxtrix) {
		for(int i = 0; i < vertices.length; i++){
			vertices[i] = transformMaxtrix.multiply(vertices[i]);
		}	
		edges[0] = new Edge(this, vertices[0], vertices[1]);
		edges[1] = new Edge(this, vertices[1], vertices[2]);
		edges[2] = new Edge(this, vertices[2], vertices[0]);
		normal = calculateNormal(); 
		//bounds = getBounds();
	}
	
	public Rectangle getBounds() {
		float xMax = Float.MIN_VALUE;
		float xMin = Float.MAX_VALUE;
		float yMax = Float.MIN_VALUE;
		float yMin = Float.MAX_VALUE;

		for(Vector3D vertex : getVertices()){
			if(vertex.x > xMax) {
				xMax = vertex.x;
			}
			if(vertex.x < xMin) {
				xMin = vertex.x;
			}
			if(vertex.y > yMax) {
				yMax = vertex.y;
			}
			if(vertex.y < yMin) {
				yMin = vertex.y;
			}
		}
		return new Rectangle(Math.round(xMin), Math.round(yMin), Math.round(xMax - xMin), Math.round(yMax - yMin));

	}

	@Override
	public String toString() {
		return "Polygon [vertices=" + Arrays.toString(vertices)
				+ ", orginalVertices=" + Arrays.toString(orginalVertices)
				+ ", edges=" + Arrays.toString(edges) + ", reflectivity="
				+ reflectivity + "]";
	}

}