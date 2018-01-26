/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamics;

import java.awt.*;

/**
 *
 * @author CFD
 */
public class PaintStroke {

    int graphicType;              // line, rectangle, circle, polygon, etc
    Graphics graphics;            // graphics environment
    int layer;                    // sub-image
    int xor;                      // XOR on = 1, off = 0
    int colourIndex;              // colour index
    int param[] = new int[4];     // params (e.g. points)
    static Color XORcolor = Color.white;
    static Color colorpalette[] = new Color[10];
    boolean PolygonInProcess;
    int polygonUniqueID;          // each polygon has an unique ID
    int polygonVertexCount;       // number of vertices in uniqueID polygon
    int polygonVertexNum;         // current vertex number

    double depth;                    // z depth in eye field of view
    PaintStroke nextPaintStroke;  // next paintstroke in depth-ordered linked list

    // default constructor
    PaintStroke() {
        graphics = null;
        layer = 0;
        PolygonInProcess = false;
        setColorPalette();
    }

    PaintStroke( int gtype, Graphics g, int gxor, int gcolor, int p0, int p1, int p2, int p3 ) {
        this(); // Call default constructor

        setPaintStroke( gtype, 0, gxor, gcolor, p0, p1, p2, p3 );
        graphics = g;
    }

    PaintStroke( int gtype, int glayer, int gxor, int gcolor, int p0, int p1, int p2, int p3 ) {
        this(); // Call default constructor

        setPaintStroke( gtype, glayer, gxor, gcolor, p0, p1, p2, p3 );
    }

    PaintStroke( double zdepth, PaintStroke nextp, int gtype, int glayer, int gxor, int gcolor, int p0, int p1, int p2, int p3, int polygonID, int polygonVcount, int vn ) {
        this( gtype, glayer, gxor, gcolor, p0, p1, p2, p3 );

        // linked list parameters
        depth = zdepth;
        nextPaintStroke = nextp;

        polygonUniqueID = polygonID;          // (if present) each polygon has an unique ID
        polygonVertexCount = polygonVcount;   // number of vertices in polygon
        polygonVertexNum = vn;                // vertex number (0-63)
    }

    void setPaintStroke( int gtype, int glayer, int gxor, int gcolor, int p0, int p1, int p2, int p3) {
        graphicType = gtype;
        layer = glayer;
        xor = gxor;
        colourIndex = gcolor;
        param[0] = p0;
        param[1] = p1;
        param[2] = p2;
        param[3] = p3;
    }

    // create new PaintStroke and add to a linked list of PaintStrokes at depth zdepth.
    void addPaintStroke( double zdepth, PaintStroke farPaintStroke, Eye eye, int gtype, int glayer, int gxor, int gcolor, int p0, int p1, int p2, int p3, int polygonID, int polygonVcount, int vn  ) {
        PaintStroke lastp = farPaintStroke;
        PaintStroke p = farPaintStroke;

        // make the negative z values positive
        zdepth = Math.abs(zdepth);

        // find place in linked list between farPaintStroke and nearPaintStroke to add new PaintStroke
        while( p.depth >= 0) {
            if ( p.nextPaintStroke.depth > zdepth ) {
//              System.out.println( "  next paintstroke has depth " + p.nextPaintStroke.depth );
                lastp = p;                              // go to next paintstroke
                p = p.nextPaintStroke;
            } else {
                lastp = p;
                p = new PaintStroke( zdepth, p.nextPaintStroke, gtype, glayer, gxor, gcolor, p0, p1, p2, p3, polygonID, polygonVcount, vn );       // create new paintstroke
                lastp.nextPaintStroke = p;
//              System.out.println( "  new " + zdepth + " paintstroke at depth " + p.depth + " between " + lastp.depth + " and " + p.nextPaintStroke.depth );
                break;
            }
        }

    }

    void setColorPalette() {
        colorpalette[0] = new Color(255, 255, 255);
        colorpalette[1] = new Color(200, 200, 200);
        colorpalette[2] = new Color(150, 150, 150);
        colorpalette[3] = new Color(100, 100, 100);
        colorpalette[4] = new Color(050, 050, 050);
        colorpalette[5] = new Color(000, 000, 000);
        colorpalette[6] = Color.red;
        colorpalette[7] = Color.cyan;
        colorpalette[8] = Color.blue;
        colorpalette[9] = Color.orange;
    }

    void paint( Graphics g, Graphics ctlGraphics, Graphics offGraphics, Eye eye ) {
        Graphics pg;
        DispersedPolygon p;

        // if layer = 0, use Graphics g environment
        pg = g;
        if ( layer == 1 ) {
            pg = ctlGraphics;
        } else if ( layer == 2 ) {
            pg = offGraphics;
        }

        if ( xor == 0 ) {
            pg.setPaintMode();
            pg.setColor( colorpalette[ colourIndex ] );
        } else {
            pg.setXORMode( XORcolor );
            pg.setColor( colorpalette[ colourIndex ] );
        }

        switch ( graphicType ) {
            case 0:
                break;
            case 1:
                pg.drawLine( param[0], param[1], param[2], param[3] );
                break;
            case 2:
                pg.drawRect( param[0], param[1], param[2], param[3] );
                break;
            case 3:
                pg.fillRect( param[0], param[1], param[2], param[3] );
                break;
            case 4:
                pg.drawOval(  param[0], param[1], param[2], param[3] );
                break;
            case 5:
                pg.setPaintMode();
                pg.setColor( colorpalette[1] );
                pg.fillOval(  param[0], param[1], param[2], param[3] );
                pg.setColor( colorpalette[ colourIndex ] );
                pg.drawOval(  param[0], param[1], param[2], param[3] );
                break;
            case 6:
                // drawpolygon uses DispersedPolygon methods
                break;
            case 7:
                // fillpolygon uses DispersedPolygon methods
                // find polygon with UniqueID in linked list of polygons
                // polygon depth doesn't matter as long as between nearPolygon and farPolygon.
                p = eye.farPolygon.findPolygon( 1000, eye.farPolygon, eye, this.polygonUniqueID, this.polygonVertexCount );

                if ( p.npoints == 0 ) {
                    // here if new polygon. If new ID, create new dispersedPolygon and add to link list of polygons
                    p.uniqueID = this.polygonUniqueID;
                    p.vertexCount =  this.polygonVertexCount;
                    p.npoints = 0;
                    p.setColors( colorpalette[ colourIndex ], colorpalette[ 1 ] );
                    p.addPoint( param[0], param[1], this.polygonVertexNum );
//                  System.out.println( "new polygon ID = " + this.polygonUniqueID );
                } else {
                    // here if existing polygon
                    if ( p.addPoint( param[0], param[1], this.polygonVertexNum ) ) {
                        // if complete polygon has been acquired from the paintStroke linked list, paint it.
                        p.paint( pg );
                        // remove polygon from linked list.
                        eye.farPolygon.removePolygon( eye.farPolygon, eye, this.polygonUniqueID );
//                      System.out.println( "remove polygon ID = " + this.polygonUniqueID );
                    }

                }
                break;
            case 8:
                break;
            default:
                break;
        }
/*
 // A simple triangle.
 x[0]=100; x[1]=150; x[2]=50;
 y[0]=100; y[1]=150; y[2]=150;
 n = 3;

 Polygon p = new Polygon(x, y, n);  // This polygon represents a triangle with the above
                                    //   vertices.

 g.fillPolygon(p);     // Fills the triangle above.
*/

    }



}
