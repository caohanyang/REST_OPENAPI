package gate.resources.img.svg;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * This class has been automatically generated using <a
 * href="http://englishjavadrinker.blogspot.com/search/label/SVGRoundTrip">SVGRoundTrip</a>.
 */
@SuppressWarnings("unused")
public class DanishIcon implements
		javax.swing.Icon {
	/**
	 * Paints the transcoded SVG image on the specified graphics context. You
	 * can install a custom transformation on the graphics context to scale the
	 * image.
	 * 
	 * @param g
	 *            Graphics context.
	 */
	public static void paint(Graphics2D g) {
        Shape shape = null;
        Paint paint = null;
        Stroke stroke = null;
        Area clip = null;
         
        float origAlpha = 1.0f;
        Composite origComposite = g.getComposite();
        if (origComposite instanceof AlphaComposite) {
            AlphaComposite origAlphaComposite = 
                (AlphaComposite)origComposite;
            if (origAlphaComposite.getRule() == AlphaComposite.SRC_OVER) {
                origAlpha = origAlphaComposite.getAlpha();
            }
        }
        
	    Shape clip_ = g.getClip();
AffineTransform defaultTransform_ = g.getTransform();
//  is CompositeGraphicsNode
float alpha__0 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0 = g.getClip();
AffineTransform defaultTransform__0 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
clip = new Area(g.getClip());
clip.intersect(new Area(new Rectangle2D.Double(0.0,0.0,48.0,48.0)));
g.setClip(clip);
// _0 is CompositeGraphicsNode
float alpha__0_0 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_0 = g.getClip();
AffineTransform defaultTransform__0_0 = g.getTransform();
g.transform(new AffineTransform(0.019714679569005966f, 0.0f, 0.0f, 0.033432260155677795f, -22.450380325317383f, 36.48228073120117f));
// _0_0 is CompositeGraphicsNode
float alpha__0_0_0 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_0_0 = g.getClip();
AffineTransform defaultTransform__0_0_0 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 1.0313969865888631E-13f, -59.82244110107422f));
// _0_0_0 is CompositeGraphicsNode
float alpha__0_0_0_0 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_0_0_0 = g.getClip();
AffineTransform defaultTransform__0_0_0_0 = g.getTransform();
g.transform(new AffineTransform(1.0434789657592773f, 0.0f, 0.0f, 1.0f, 3126.22802734375f, -59.82246017456055f));
// _0_0_0_0 is CompositeGraphicsNode
float alpha__0_0_0_0_0 = origAlpha;
origAlpha = origAlpha * 0.40206185f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_0_0_0_0 = g.getClip();
AffineTransform defaultTransform__0_0_0_0_0 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_0_0_0_0 is ShapeNode
paint = new LinearGradientPaint(new Point2D.Double(302.8571472167969, 366.64788818359375), new Point2D.Double(302.8571472167969, 609.5050659179688), new float[] {0.0f,0.5f,1.0f}, new Color[] {new Color(0, 0, 0, 0),new Color(0, 0, 0, 255),new Color(0, 0, 0, 0)}, MultipleGradientPaint.CycleMethod.NO_CYCLE, MultipleGradientPaint.ColorSpaceType.SRGB, new AffineTransform(3.7817609310150146f, 0.0f, 0.0f, 1.9697060585021973f, -2104.845947265625f, -856.03662109375f));
shape = new Rectangle2D.Double(-1651.03466796875, -133.84803771972656, 1826.0504150390625, 478.357177734375);
g.setPaint(paint);
g.fill(shape);
origAlpha = alpha__0_0_0_0_0;
g.setTransform(defaultTransform__0_0_0_0_0);
g.setClip(clip__0_0_0_0_0);
float alpha__0_0_0_0_1 = origAlpha;
origAlpha = origAlpha * 0.40206185f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_0_0_0_1 = g.getClip();
AffineTransform defaultTransform__0_0_0_0_1 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_0_0_0_1 is ShapeNode
paint = new RadialGradientPaint(new Point2D.Double(605.7142944335938, 486.64788818359375), 117.14286f, new Point2D.Double(605.7142944335938, 486.64788818359375), new float[] {0.0f,1.0f}, new Color[] {new Color(0, 0, 0, 255),new Color(0, 0, 0, 0)}, MultipleGradientPaint.CycleMethod.NO_CYCLE, MultipleGradientPaint.ColorSpaceType.SRGB, new AffineTransform(2.0371599197387695f, 0.0f, 0.0f, 1.9697060585021973f, -1052.7010498046875f, -856.2755737304688f));
shape = new GeneralPath();
((GeneralPath)shape).moveTo(175.01578, -134.07053);
((GeneralPath)shape).curveTo(175.01578, -134.07053, 175.01578, 344.26028, 175.01578, 344.26028);
((GeneralPath)shape).curveTo(279.92447, 345.1607, 428.63394, 237.0906, 428.63388, 105.06409);
((GeneralPath)shape).curveTo(428.63388, -26.962448, 311.5637, -134.0705, 175.01578, -134.07053);
((GeneralPath)shape).closePath();
g.setPaint(paint);
g.fill(shape);
origAlpha = alpha__0_0_0_0_1;
g.setTransform(defaultTransform__0_0_0_0_1);
g.setClip(clip__0_0_0_0_1);
float alpha__0_0_0_0_2 = origAlpha;
origAlpha = origAlpha * 0.40206185f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_0_0_0_2 = g.getClip();
AffineTransform defaultTransform__0_0_0_0_2 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_0_0_0_2 is ShapeNode
paint = new RadialGradientPaint(new Point2D.Double(605.7142944335938, 486.64788818359375), 117.14286f, new Point2D.Double(605.7142944335938, 486.64788818359375), new float[] {0.0f,1.0f}, new Color[] {new Color(0, 0, 0, 255),new Color(0, 0, 0, 0)}, MultipleGradientPaint.CycleMethod.NO_CYCLE, MultipleGradientPaint.ColorSpaceType.SRGB, new AffineTransform(-2.0371599197387695f, -1.4506279901930043E-15f, -2.235549917249278E-15f, 2.0180580615997314f, -423.3185119628906f, -879.5623779296875f));
shape = new GeneralPath();
((GeneralPath)shape).moveTo(-1651.0345, -133.82722);
((GeneralPath)shape).curveTo(-1651.0345, -133.82722, -1651.0345, 344.50357, -1651.0345, 344.50357);
((GeneralPath)shape).curveTo(-1755.9434, 345.40402, -1904.6528, 237.3339, -1904.6528, 105.30739);
((GeneralPath)shape).curveTo(-1904.6528, -26.719152, -1787.5825, -133.82721, -1651.0345, -133.82722);
((GeneralPath)shape).closePath();
g.setPaint(paint);
g.fill(shape);
origAlpha = alpha__0_0_0_0_2;
g.setTransform(defaultTransform__0_0_0_0_2);
g.setClip(clip__0_0_0_0_2);
origAlpha = alpha__0_0_0_0;
g.setTransform(defaultTransform__0_0_0_0);
g.setClip(clip__0_0_0_0);
origAlpha = alpha__0_0_0;
g.setTransform(defaultTransform__0_0_0);
g.setClip(clip__0_0_0);
origAlpha = alpha__0_0;
g.setTransform(defaultTransform__0_0);
g.setClip(clip__0_0);
float alpha__0_1 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_1 = g.getClip();
AffineTransform defaultTransform__0_1 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_1 is ShapeNode
paint = new LinearGradientPaint(new Point2D.Double(11.002116203308105, 29.756582260131836), new Point2D.Double(46.99897384643555, 29.756582260131836), new float[] {0.0f,0.17355372f,0.6363636f,1.0f}, new Color[] {new Color(204, 0, 0, 255),new Color(237, 57, 57, 255),new Color(176, 33, 33, 255),new Color(217, 27, 27, 255)}, MultipleGradientPaint.CycleMethod.NO_CYCLE, MultipleGradientPaint.ColorSpaceType.SRGB, new AffineTransform(1.2857420444488525f, 0.0f, 0.0f, 1.2857420444488525f, -13.28717041015625f, -14.937620162963867f));
shape = new GeneralPath();
((GeneralPath)shape).moveTo(1.5015852, 8.410663);
((GeneralPath)shape).curveTo(13.307709, 9.6089265E-4, 34.469635, 10.910843, 46.49851, 8.410663);
((GeneralPath)shape).lineTo(46.49851, 40.685734);
((GeneralPath)shape).curveTo(34.915146, 44.54965, 14.867007, 32.50332, 1.5015852, 40.685734);
((GeneralPath)shape).lineTo(1.5015852, 8.410663);
((GeneralPath)shape).closePath();
g.setPaint(paint);
g.fill(shape);
paint = new Color(94, 25, 25, 255);
stroke = new BasicStroke(1.0000002f,0,0,4.0f,null,0.0f);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(1.5015852, 8.410663);
((GeneralPath)shape).curveTo(13.307709, 9.6089265E-4, 34.469635, 10.910843, 46.49851, 8.410663);
((GeneralPath)shape).lineTo(46.49851, 40.685734);
((GeneralPath)shape).curveTo(34.915146, 44.54965, 14.867007, 32.50332, 1.5015852, 40.685734);
((GeneralPath)shape).lineTo(1.5015852, 8.410663);
((GeneralPath)shape).closePath();
g.setPaint(paint);
g.setStroke(stroke);
g.draw(shape);
origAlpha = alpha__0_1;
g.setTransform(defaultTransform__0_1);
g.setClip(clip__0_1);
float alpha__0_2 = origAlpha;
origAlpha = origAlpha * 0.62921345f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_2 = g.getClip();
AffineTransform defaultTransform__0_2 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_2 is ShapeNode
paint = new LinearGradientPaint(new Point2D.Double(36.88053894042969, 20.81171417236328), new Point2D.Double(23.073863983154297, 43.5311279296875), new float[] {0.0f,1.0f}, new Color[] {new Color(255, 255, 255, 255),new Color(255, 255, 255, 136)}, MultipleGradientPaint.CycleMethod.NO_CYCLE, MultipleGradientPaint.ColorSpaceType.SRGB, new AffineTransform(1.2857420444488525f, 0.0f, 0.0f, 1.2857420444488525f, -13.28717041015625f, -14.937620162963867f));
stroke = new BasicStroke(1.0000007f,0,0,4.0f,null,0.0f);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(2.4200048, 8.983105);
((GeneralPath)shape).curveTo(12.676559, 1.6180695, 33.709705, 11.483286, 45.541058, 9.625976);
((GeneralPath)shape).lineTo(45.535015, 39.959194);
((GeneralPath)shape).curveTo(36.64789, 43.028446, 15.874652, 32.449287, 2.5003638, 39.08726);
((GeneralPath)shape).lineTo(2.4200048, 8.983105);
((GeneralPath)shape).closePath();
g.setPaint(paint);
g.setStroke(stroke);
g.draw(shape);
origAlpha = alpha__0_2;
g.setTransform(defaultTransform__0_2);
g.setClip(clip__0_2);
float alpha__0_3 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_3 = g.getClip();
AffineTransform defaultTransform__0_3 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_3 is ShapeNode
paint = new Color(255, 255, 255, 255);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(13.0, 5.6852837);
((GeneralPath)shape).lineTo(17.0, 5.748227);
((GeneralPath)shape).lineTo(17.0, 37.35272);
((GeneralPath)shape).lineTo(13.0, 37.262585);
((GeneralPath)shape).lineTo(13.0, 5.6852837);
((GeneralPath)shape).closePath();
g.setPaint(paint);
g.fill(shape);
origAlpha = alpha__0_3;
g.setTransform(defaultTransform__0_3);
g.setClip(clip__0_3);
float alpha__0_4 = origAlpha;
origAlpha = origAlpha * 1.0f;
g.setComposite(AlphaComposite.getInstance(3, origAlpha));
Shape clip__0_4 = g.getClip();
AffineTransform defaultTransform__0_4 = g.getTransform();
g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
// _0_4 is ShapeNode
paint = new Color(255, 255, 255, 255);
shape = new GeneralPath();
((GeneralPath)shape).moveTo(1.9925584, 22.437485);
((GeneralPath)shape).curveTo(13.798682, 14.027783, 33.971104, 24.937666, 45.999977, 22.437485);
((GeneralPath)shape).lineTo(45.999977, 25.54732);
((GeneralPath)shape).curveTo(34.41661, 29.411238, 15.35798, 17.364908, 1.9925584, 25.54732);
((GeneralPath)shape).lineTo(1.9925584, 22.437485);
((GeneralPath)shape).closePath();
g.setPaint(paint);
g.fill(shape);
origAlpha = alpha__0_4;
g.setTransform(defaultTransform__0_4);
g.setClip(clip__0_4);
origAlpha = alpha__0;
g.setTransform(defaultTransform__0);
g.setClip(clip__0);
g.setTransform(defaultTransform_);
g.setClip(clip_);

	}
	
	public Image getImage() {
		BufferedImage image =
            new BufferedImage(getIconWidth(), getIconHeight(),
                    BufferedImage.TYPE_INT_ARGB);
    	Graphics2D g = image.createGraphics();
    	paintIcon(null, g, 0, 0);
    	g.dispose();
    	return image;
	}

    /**
     * Returns the X of the bounding box of the original SVG image.
     * 
     * @return The X of the bounding box of the original SVG image.
     */
    public static int getOrigX() {
        return 0;
    }

    /**
     * Returns the Y of the bounding box of the original SVG image.
     * 
     * @return The Y of the bounding box of the original SVG image.
     */
    public static int getOrigY() {
        return 1;
    }

	/**
	 * Returns the width of the bounding box of the original SVG image.
	 * 
	 * @return The width of the bounding box of the original SVG image.
	 */
	public static int getOrigWidth() {
		return 48;
	}

	/**
	 * Returns the height of the bounding box of the original SVG image.
	 * 
	 * @return The height of the bounding box of the original SVG image.
	 */
	public static int getOrigHeight() {
		return 48;
	}

	/**
	 * The current width of this resizable icon.
	 */
	int width;

	/**
	 * The current height of this resizable icon.
	 */
	int height;

	/**
	 * Creates a new transcoded SVG image.
	 */
	public DanishIcon() {
        this.width = getOrigWidth();
        this.height = getOrigHeight();
	}
	
	/**
	 * Creates a new transcoded SVG image with the given dimensions.
	 *
	 * @param size the dimensions of the icon
	 */
	public DanishIcon(Dimension size) {
	this.width = size.width;
	this.height = size.width;
	}

	public DanishIcon(int width, int height) {
	this.width = width;
	this.height = height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#getIconHeight()
	 */
    @Override
	public int getIconHeight() {
		return height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#getIconWidth()
	 */
    @Override
	public int getIconWidth() {
		return width;
	}

	public void setDimension(Dimension newDimension) {
		this.width = newDimension.width;
		this.height = newDimension.height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics,
	 * int, int)
	 */
    @Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.translate(x, y);
						
		Area clip = new Area(new Rectangle(0, 0, this.width, this.height));		
		if (g2d.getClip() != null) clip.intersect(new Area(g2d.getClip()));		
		g2d.setClip(clip);

		double coef1 = (double) this.width / (double) getOrigWidth();
		double coef2 = (double) this.height / (double) getOrigHeight();
		double coef = Math.min(coef1, coef2);
		g2d.scale(coef, coef);
		paint(g2d);
		g2d.dispose();
	}
}

