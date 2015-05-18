public class Edge {
	public final Vector3D a;
	public final Vector3D b;
	public final Polygon polygon;
	
	public Edge(Polygon polygon, Vector3D a, Vector3D b) {
		this.polygon = polygon;
		this.a = a;
		this.b = b;
	}
}