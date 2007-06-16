package org.newdawn.slick.geom;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Image;
import org.newdawn.slick.ShapeFill;
import org.newdawn.slick.opengl.Texture;

/**
 * @author Mark Bernard
 *
 * Use this class to render shpaes directly to OpenGL.  Allows you to bypass the Graphics class.
 */
public final class ShapeRenderer {
    /**
     * Draw the outline of the given shape.  Only the vertices are set.  
     * The colour has to be set independently of this method.
     * 
     * @param shape The shape to draw.
     */
    public static final void draw(Shape shape) {
        Texture t = Texture.getLastBind();
        Texture.bindNone();
        
        float points[] = shape.getPoints();
        
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for(int i=0;i<points.length;i+=2) {
            GL11.glVertex2f(points[i], points[i + 1]);
        }
        
        if (shape.closed()) {
        	GL11.glVertex2f(points[0], points[1]);
        }
        
        GL11.glEnd();
        
        if (t == null) {
        	Texture.bindNone();
        } else {
        	t.bind();
        }
    }

    /**
     * Draw the outline of the given shape.  Only the vertices are set.  
     * The colour has to be set independently of this method.
     * 
     * @param shape The shape to draw.
     * @param fill The fill to apply
     */
    public static final void draw(Shape shape, ShapeFill fill) {
        float points[] = shape.getPoints();
        
        Texture t = Texture.getLastBind();
        Texture.bindNone();

        float center[] = shape.getCenter();
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for(int i=0;i<points.length;i+=2) {
            fill.colorAt(shape, points[i]-center[0], points[i + 1]-center[1]).bind();
            Vector2f offset = fill.getOffsetAt(shape, points[i], points[i + 1]);
            GL11.glVertex2f(points[i] + offset.x, points[i + 1] + offset.y);
        }
        
        if (shape.closed()) {
	        fill.colorAt(shape, points[0]-center[0], points[1]-center[1]).bind();
	        Vector2f offset = fill.getOffsetAt(shape, points[0], points[1]);
	        GL11.glVertex2f(points[0] + offset.x, points[1] + offset.y);
        }
        GL11.glEnd();
        
        if (t == null) {
        	Texture.bindNone();
        } else {
        	t.bind();
        }
    }
    
    /**
     * Check there are enough points to fill
     * 
     * @param shape THe shape we're drawing
     * @return True if the fill is valid
     */
    public static boolean validFill(Shape shape) {
    	if (shape.getTriangles() == null) {
    		return false;
    	}
        return shape.getTriangles().getTriangleCount() != 0;
    }

    /**
     * Draw the the given shape filled in.  Only the vertices are set.  
     * The colour has to be set independently of this method.
     * 
     * @param shape The shape to fill.
     */
    public static final void fill(Shape shape) {
    	if (!validFill(shape)) {
    		return;
    	}
    	
        Texture t = Texture.getLastBind();
        Texture.bindNone();
        
    	fill(shape, new PointCallback() {
    		/**
    		 * @see org.newdawn.slick.geom.ShapeRenderer.PointCallback#preRenderPoint(float, float)
    		 */
			public float[] preRenderPoint(float x, float y) {
				// do nothing, we're just filling the shape this time
				return null;
			}
    	});
        
        if (t == null) {
        	Texture.bindNone();
        } else {
        	t.bind();
        }
    }
    
    /**
     * Draw the the given shape filled in.  Only the vertices are set.  
     * The colour has to be set independently of this method.
     * 
     * @param shape The shape to fill.
     * @param callback The callback that will be invoked for each shape point
     */
    private static final void fill(Shape shape, PointCallback callback) {
    	Triangulator tris = shape.getTriangles();

        GL11.glBegin(GL11.GL_TRIANGLES);
        for (int i=0;i<tris.getTriangleCount();i++) {
        	for (int p=0;p<3;p++) {
        		float[] pt = tris.getTrianglePoint(i, p);
        		float[] np = callback.preRenderPoint(pt[0],pt[1]);
        		
        		if (np == null) {
        			GL11.glVertex2f(pt[0],pt[1]);
        		} else {
        			GL11.glVertex2f(np[0],np[1]);
        		}
        	}
        }
        GL11.glEnd();
    }

    /**
     * Draw the the given shape filled in with a texture.  Only the vertices are set.  
     * The colour has to be set independently of this method.
     * 
     * @param shape The shape to texture.
     * @param image The image to tile across the shape
     */
    public static final void texture(Shape shape, Image image) {
    	texture(shape, image, 0.01f, 0.01f);
    }

    /**
     * Draw the the given shape filled in with a texture.  Only the vertices are set.  
     * The colour has to be set independently of this method. This method is required to 
     * fit the texture once across the shape.
     * 
     * @param shape The shape to texture.
     * @param image The image to tile across the shape
     */
    public static final void textureFit(Shape shape, Image image) {
    	textureFit(shape, image);
    }
    
    /**
     * Draw the the given shape filled in with a texture.  Only the vertices are set.  
     * The colour has to be set independently of this method.
     * 
     * @param shape The shape to texture.
     * @param image The image to tile across the shape
     * @param scaleX The scale to apply on the x axis for texturing
     * @param scaleY The scale to apply on the y axis for texturing
     */
    public static final void texture(Shape shape, Image image, final float scaleX, final float scaleY) {
    	if (!validFill(shape)) {
    		return;
    	}
    	
    	Texture t = Texture.getLastBind();
        image.getTexture().bind();
        
        fill(shape, new PointCallback() {
    		/**
    		 * @see org.newdawn.slick.geom.ShapeRenderer.PointCallback#preRenderPoint(float, float)
    		 */
			public float[] preRenderPoint(float x, float y) {
	            GL11.glTexCoord2f(x * scaleX, y * scaleY);
	            return null;
			}
    	});
    	
        float points[] = shape.getPoints();
        
        if (t == null) {
        	Texture.bindNone();
        } else {
        	t.bind();
        }
    }
    
    /**
     * Draw the the given shape filled in with a texture.  Only the vertices are set.  
     * The colour has to be set independently of this method. This method is required to 
     * fit the texture scaleX times across the shape and scaleY times down the shape.
     * 
     * @param shape The shape to texture.
     * @param image The image to tile across the shape
     * @param scaleX The scale to apply on the x axis for texturing
     * @param scaleY The scale to apply on the y axis for texturing
     */
    public static final void textureFit(Shape shape, Image image, final float scaleX, final float scaleY) {
    	if (!validFill(shape)) {
    		return;
    	}
    	
        float points[] = shape.getPoints();
        
        Texture t = Texture.getLastBind();
        image.getTexture().bind();
        
        final float minX = shape.getX();
        final float minY = shape.getY();
        final float maxX = shape.getMaxX() - minX;
        final float maxY = shape.getMaxY() - minY;

        fill(shape, new PointCallback() {
    		/**
    		 * @see org.newdawn.slick.geom.ShapeRenderer.PointCallback#preRenderPoint(float, float)
    		 */
			public float[] preRenderPoint(float x, float y) {
	            GL11.glTexCoord2f(((x - minX) / maxX) * scaleX, ((y - minY) / maxY) * scaleY);
	            GL11.glTexCoord2f(x * scaleX, y * scaleY);
	            return null;
			}
    	});
        
        if (t == null) {
        	Texture.bindNone();
        } else {
        	t.bind();
        }
    }

    /**
     * Draw the the given shape filled in.  Only the vertices are set.  
     * The colour has to be set independently of this method.
     * 
     * @param shape The shape to fill.
     * @param fill The fill to apply
     */
    public static final void fill(final Shape shape, final ShapeFill fill) {
        if (!validFill(shape)) {
    		return;
    	}
        
        Texture t = Texture.getLastBind();
        Texture.bindNone();

        final float center[] = shape.getCenter();
        fill(shape, new PointCallback() {
    		/**
    		 * @see org.newdawn.slick.geom.ShapeRenderer.PointCallback#preRenderPoint(float, float)
    		 */
			public float[] preRenderPoint(float x, float y) {
	            fill.colorAt(shape, x - center[0], y - center[1]).bind();
	            Vector2f offset = fill.getOffsetAt(shape, x, y);
	            
	            return new float[] {offset.x + x,offset.y + y};
			}
    	});
        
        if (t == null) {
        	Texture.bindNone();
        } else {
        	t.bind();
        }
    }
    

    /**
     * Draw the the given shape filled in with a texture.  Only the vertices are set.  
     * The colour has to be set independently of this method.
     * 
     * @param shape The shape to texture.
     * @param image The image to tile across the shape
     * @param scaleX The scale to apply on the x axis for texturing
     * @param scaleY The scale to apply on the y axis for texturing
     * @param fill The fill to apply
     */
    public static final void texture(final Shape shape, Image image, final float scaleX, final float scaleY, final ShapeFill fill) {
    	if (!validFill(shape)) {
    		return;
    	}
        
        Texture t = Texture.getLastBind();
        image.getTexture().bind();
        
        final float center[] = shape.getCenter();
        fill(shape, new PointCallback() {
    		/**
    		 * @see org.newdawn.slick.geom.ShapeRenderer.PointCallback#preRenderPoint(float, float)
    		 */
			public float[] preRenderPoint(float x, float y) {
	            fill.colorAt(shape, x - center[0], y - center[1]).bind();
	            Vector2f offset = fill.getOffsetAt(shape, x, y);
	            GL11.glTexCoord2f(x * scaleX, y * scaleY);

	            return new float[] {offset.x + x,offset.y + y};
			}
    	});
        
        if (t == null) {
        	Texture.bindNone();
        } else {
        	t.bind();
        }
    }
    
    /**
     * Description of some feature that will be applied to each point render
     *
     * @author kevin
     */
    private static interface PointCallback {
    	/** 
    	 * Apply feature before the call to glVertex
    	 * 
    	 * @param x The x poisiton the vertex will be at
    	 * @param y The y position the vertex will be at
    	 * @return The new coordinates of null
    	 */
    	float[] preRenderPoint(float x, float y);
    }
}
