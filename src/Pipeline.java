import java.awt.Color;
import java.awt.Rectangle;
import java.util.*;

public class Pipeline {
	private Set<Polygon> polygons = new HashSet<Polygon>();
	private Set<Polygon> polygonsVisible = new HashSet<Polygon>();
	
	private Vector3D lightSources;
	//private List<Vector3D> lightSources = new ArrayList<Vector3D>();
	private float[] intensity;
	
	private float[] ambientLight;
	
	private boolean drawWireframe;

	private float rotationX;
	private float rotationY;
	private float rotationZ;


	public Set<Polygon> getPolygons() {
		return polygons;
	}
	
	public void addPolygon(Polygon polygon) {
		polygons.add(polygon);
	}

	public Vector3D getLightSource() {
	//public List<Vector3D> getLightSource() {
		return lightSources;
	}

	public void setLightSource(Vector3D lightSource) {
		lightSource = lightSource.unitVector();
		this.lightSources = lightSource;	
	}
	/*public void addLightSource(Vector3D lightSource) {
		lightSource = lightSource.unitVector();
		this.lightSources.add(lightSource);	
	}*/

	public void setIntensity(int[] intensity) {
		float r = intensity[0] /255f;
		float g = intensity[1] /255f;
		float b = intensity[2] /255f;
		
		this.intensity = new float[] { r, g, b };
	}

	public void setAmbientLight(int[] ambientLight) {
		float r = ambientLight[0] /255f;
		float g = ambientLight[1] /255f;
		float b = ambientLight[2] /255f;
		
		this.ambientLight = new float[] {r, g, b };
	}
	
	public void clear() {
		polygons.clear();
		polygonsVisible.clear();
		
		lightSources = new Vector3D(0, 0, 0); //lightSources.clear();
		intensity = new float[] { 0.5f,  0.5f, 0.5f };
	}
	
	public Color[][] render(){
		removeHiddenPolygons();
		return renderZBuffer();
	}
	
	/* 
	 * Z Buffer Methods 
	 */
	
	public void removeHiddenPolygons() {
		polygonsVisible.clear();
		for(Polygon polygon : polygons){
			Vector3D n = polygon.getNormal();
			if(n.z < 0) {
				polygonsVisible.add(polygon);
			}
		}
	}
	
	public Color computeShading(Polygon polygon){
		Vector3D normal = polygon.getNormal();
		Vector3D d = lightSources;
		/*float sigmaIntensity = 0f;
		for(Vector3D d: lightSources) {
			float costh = normal.cosTheta(d);
			if(costh < 0 || costh > Math.PI) {
				continue;
			}
			sigmaIntensity += (intensity[0] * costh);
		}*/
		
		// ambientLight = ambient light level  (global variable)
		Color reflectance = polygon.getReflectivity();
		float reflectanceR = reflectance.getRed() / 255f;
		float reflectanceG = reflectance.getGreen() / 255f;
		float reflectanceB = reflectance.getBlue() / 255f;
		// intensity = intensity color of incident light (global variable)
					
		float costh = normal.cosTheta(d);
		float r = (ambientLight[0] + intensity[0] * costh) * reflectanceR;
		float g = (ambientLight[1] + intensity[1] * costh) * reflectanceG;
		float b = (ambientLight[2] + intensity[2] * costh) * reflectanceB;
		/*float r = (ambientLight[0] + sigmaIntensity) * reflectanceR;
		float g = (ambientLight[1] + sigmaIntensity) * reflectanceG;
		float b = (ambientLight[2] + sigmaIntensity) * reflectanceB;*/
		return new Color(checkColor(r), checkColor(g), checkColor(b));
	}

	private float checkColor(float color) {
		if(color < 0) { 
			return 0;
		}
		if(color > 1) {
			return 1;
		}
		return color;
	}
	
 
	public float[][] computeEdgeLists(Polygon polygon) {	
		int minY = Math.round(polygon.getMinY());
		int maxY = Math.round(polygon.getMaxY());
		
		float[][] edgeList = new float[GUI.CANVAS_HEIGHT][4];
		for(int i = 0; i < edgeList.length; i++) {
			edgeList[i] = new float[] { Float.POSITIVE_INFINITY, 
										Float.POSITIVE_INFINITY, 
										Float.NEGATIVE_INFINITY, 
										Float.POSITIVE_INFINITY };
		}

		for(Edge edge : polygon.getEdges()) {
			Vector3D va, vb;
			if(drawWireframe) {
				if(edge.a.y == minY) {
					va = edge.a;
					vb = edge.b;
				} else if(edge.a.y == maxY) {
					va = edge.b;
					vb = edge.a;
				} else {
					va = edge.b;
					vb = edge.a;
				}
			} else {
				if(edge.a.y < edge.b.y) {
					va = edge.a;
					vb = edge.b;
				} else {
					va = edge.b;
					vb = edge.a;
				} 
			}
			
			float mx = (vb.x - va.x)/(vb.y - va.y);
			float mz = (vb.z - va.z)/(vb.y - va.y);
			float x = va.x;
			float z = va.z;
			
			int i = Math.round(va.y);
			int maxi = Math.round(vb.y);
			
			do {
				try {
					if(x < edgeList[i][0]) { // Left Edge list
						edgeList[i][0] = x;
						edgeList[i][1] = z;
					}
					
					if (x > edgeList[i][2]) { // Right Edge list
						edgeList[i][2] = x;
						edgeList[i][3] = z;
					}
					
					i++;
					x = x + mx;
					z = z + mz;
				} catch (ArrayIndexOutOfBoundsException e) {
					i++;
					x = x + mx;
					z = z + mz;
				}
			} while(i < maxi);
			
			/* COMMENTED OUT - CAUSING TEARING
			if(x < edgeList[maxi][0]) { // Left Edge list
				edgeList[maxi][0] = x;
				edgeList[maxi][1] = z;
			}
			
			if (x > edgeList[maxi][2]) { // Right Edge list
				edgeList[maxi][2] = x;
				edgeList[maxi][3] = z;
			}*/
		}		
		return edgeList;
	}	
	
	public Color[][] renderZBuffer() {
		Color[][] ZBufferC = new Color[GUI.CANVAS_WIDTH][GUI.CANVAS_HEIGHT];
		float[][] ZBufferD = new float[GUI.CANVAS_WIDTH][GUI.CANVAS_HEIGHT];
		
		for(int i = 0; i < ZBufferC.length; i++) {
			for(int j = 0; j < ZBufferC[i].length; j++) {
				ZBufferC[i][j] = Color.LIGHT_GRAY;
				ZBufferD[i][j] = Float.POSITIVE_INFINITY;
			}
		}
		
		if(drawWireframe) {
			polygonsVisible.clear();
			polygonsVisible.addAll(polygons);
		}
		
		for (Polygon polygon: polygonsVisible) {
			float[][] edgeList = computeEdgeLists(polygon);
			Color shading = computeShading(polygon);
			
			for(int y = 0; y < edgeList.length; y++) {
				float xLeft = edgeList[y][0];
				float xRight = edgeList[y][2];
				float zLeft = edgeList[y][1];
				float zRight = edgeList[y][3];
				
				int x = Math.round(xLeft);
				float z = zLeft;
				
				float mz = (zRight - zLeft) / (xRight - xLeft);
				
				while( x <= Math.round(xRight)) {
					if(x >= 0 && x < GUI.CANVAS_WIDTH && z < ZBufferD[x][y]) {
						ZBufferD[x][y] = z;
						ZBufferC[x][y] = shading;
					}
					x++;
					z = z + mz;
				}

			}
		}
		return ZBufferC;
	}
	
	/*
	 * Translation Methods
	 */
	public void runFirstTimeTransforms() {
		// Translate Scale 
		Transform transform = Transform.identity();	
		apply(transform);
		
		Transform transforms = scaling();
		apply(transforms);	
		
		Transform transformt = translate();
		apply(transformt);
	
	}

	private void apply(Transform transform){
		for(Polygon polygon : polygons) {
			polygon.apply(transform);
		}
	}
	
	private void applyLightSources(Transform transform) {
		if(polygons.size() > 0) {
			/*for(int i = 0; i < lightSources.size(); i++) {
				lightSources.set(i, transform.multiply(lightSources.get(i)));		
			}*/
			lightSources = transform.multiply(lightSources);
		}
	}

	public void rotateX(float th) {
		if(rotationX < 0f) {
			rotationX = 0;
		} else if(rotationX >= (Math.PI*2f)) { 
			rotationX = (float) (Math.PI*2);
		} else {
			th = rotationX - th;
			rotationX = rotationX - th;
		}
		
		apply(Transform.newXRotation(th));
		applyLightSources(Transform.newXRotation(th));
		
		apply(translate());
		applyLightSources(translate());
		
		apply(scaling());
		applyLightSources(scaling());
	}
	
	
	public void rotateY(float th) {
		if(rotationY < 0f) {
			rotationY = 0;
		} else if(rotationY >= (Math.PI*2f)) { 
			rotationY = (float) (Math.PI*2);
		} else {
			th = rotationY - th;
			rotationY = rotationY - th;
		}
		
		apply(Transform.newYRotation(th));
		applyLightSources(Transform.newYRotation(th));
		
		apply(translate());
		applyLightSources(translate());
		
		apply(scaling());
		applyLightSources(scaling());
		
	}

	public void rotateZ(float th) {
		if(rotationZ < 0f) {
			rotationZ = 0;
		} else if(rotationZ >= (Math.PI*2f)) { 
			rotationZ = (float) (Math.PI*2);
		} else {
			th = rotationZ - th;
			rotationZ = rotationZ - th;
		}
		
		apply(Transform.newZRotation(th));
		applyLightSources(Transform.newZRotation(th));
		
		apply(translate());
		applyLightSources(translate());
		
		apply(scaling());
		applyLightSources(scaling());

	}
	
	private Transform translate() {
		Rectangle bounds = getBounds();
		
		float xCenter = (float) (bounds.getX() + (bounds.getWidth()/2));
		float yCenter = (float) (bounds.getY() + (bounds.getHeight()/2));

		float dx = (GUI.CANVAS_WIDTH/2) - xCenter;
		float dy = (GUI.CANVAS_HEIGHT/2) - yCenter;

		
		//System.out.println("xDist:"+dx+" yDist:"+dy);
		
		return Transform.newTranslation(dx, dy, 0f);
	}	
	
	private Transform scaling() {
		Rectangle bounds = getBounds();

		float xScale = (float) ((GUI.CANVAS_WIDTH-20) / (bounds.getWidth()));
		float yScale = (float) ((GUI.CANVAS_HEIGHT-20) / (bounds.getHeight()));
		
		float scale = Math.min(xScale, yScale);
		//System.out.println("Math.min("+xScale+"," +yScale+");");
		
		return Transform.newScale(scale, scale, scale);
	}
	
	public Rectangle getBounds(){
		float xMax = Float.MIN_VALUE;
		float xMin = Float.MAX_VALUE;
		float yMax = Float.MIN_VALUE;
		float yMin = Float.MAX_VALUE;

		for(Polygon polygon : polygons) {
			Rectangle pBounds = polygon.getBounds();
			int boundsMinX = (int) pBounds.getX();
			int boundsMinY = (int) pBounds.getY();
			int boundsMaxX = (int) pBounds.getMaxX();
			int boundsMaxY = (int) pBounds.getMaxY();
			
			if(boundsMaxX > xMax) {
				xMax = boundsMaxX;
			}
			if(boundsMinX < xMin) {
				xMin = boundsMinX;
			}
			if(boundsMaxY > yMax) {
				yMax = boundsMaxY;
			}
			if(boundsMinY < yMin) {
				yMin = boundsMinY;
			}
		}	
		return new Rectangle(Math.round(xMin), Math.round(yMin), Math.round(xMax - xMin), Math.round(yMax - yMin));
	}

	public void setDrawWireframe(boolean drawWireframe) {
		this.drawWireframe = drawWireframe;
		
	}
}
