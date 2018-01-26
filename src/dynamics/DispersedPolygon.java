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
public class DispersedPolygon {

// Dispersed polygon uses Java generic fillPolygon method to paint filled polygon.
// The paintStrokes picked up by DispersedPolygon are ready-to-paint, and have already been transformed, scaled and clipped
    int uniqueID;
    int vertexCount;
    int npoints;
    int xpoints[] = new int[100];
    int ypoints[] = new int[100];
    Color lineColor, fillColor;
    double depth;                       // eye z depth for use in linked list (if necessary)
    DispersedPolygon nextPolygon;       // next polygon in linked list

    DispersedPolygon() {
        uniqueID = 0;
        vertexCount = 0;
        npoints = 0;
    }

    DispersedPolygon( int id, int vcount ) {
        this(); // Call default constructor

        uniqueID = id;
        vertexCount = vcount;
    }

    DispersedPolygon( double zdepth, DispersedPolygon nextp, int id, int vcount ) {
        this( id, vcount );

        depth = zdepth;
        nextPolygon = nextp;
    }

    void setColors( Color a, Color b ) {
        lineColor = a;
        fillColor = b;
    }

    boolean addPoint( int x, int y ) {
        boolean completed = false;
        xpoints[npoints] =  x;
        ypoints[npoints] =  y;
        npoints++;
        if ( npoints >= this.vertexCount ) completed = true;
        return( completed );
    }

    boolean addPoint( int x, int y, int vn ) {
        boolean completed = false;
        xpoints[vn] =  x;
        ypoints[vn] =  y;
        npoints++;
        if ( npoints >= this.vertexCount ) completed = true;
        return( completed );
    }

    // Find a polygon with a particular uniqueID.
    // create new Polygon and add to a linked list of Polygons (adapted from PaintStroke linked list).
    DispersedPolygon findPolygon( double zdepth, DispersedPolygon farPolygon, Eye eye, int polygonID, int polygonVcount  ) {
        boolean found = false;
        DispersedPolygon lastp = farPolygon;
        DispersedPolygon p = farPolygon;

        // make the negative z values positive
        zdepth = Math.abs(zdepth);

        // find Polygon with required ID in linked list
        while( p.depth >= 0) {
            if ( p.nextPolygon.uniqueID != polygonID ) {
                lastp = p;                              // go to next polygon
                p = p.nextPolygon;
            } else {
                p = p.nextPolygon;
                found = true;
                break;
            }
        }

        // if not found, create new polygon with required ID and add to linked list.
        if ( !found ) {
                p = new DispersedPolygon( zdepth, lastp.nextPolygon, polygonID, polygonVcount );       // create new paintstroke
                lastp.nextPolygon = p;
//              System.out.println( "  new " + zdepth + " Polygon at depth " + p.depth + " between " + lastp.depth + " and " + p.nextPolygon.depth );
        }

        return( p );
    }


    boolean removePolygon( DispersedPolygon farPolygon, Eye eye, int polygonID ) {
        boolean found = false;
        DispersedPolygon lastp = farPolygon;
        DispersedPolygon p = farPolygon;

        // find Polygon with required ID in linked list
        while( p.depth >= 0) {
            if ( p.nextPolygon.uniqueID != polygonID ) {
                lastp = p;                              // go to next polygon
                p = p.nextPolygon;
            } else {
                lastp = p;                              // go to next polygon
                p = p.nextPolygon;
                lastp.nextPolygon = p.nextPolygon;
                found = true;
                break;
            }
        }

        return( found );
    }

    void paint( Graphics g ) {
        fillColor = new Color( 251, 251, 251 );
        g.setColor(fillColor);
        g.fillPolygon( xpoints, ypoints, npoints );
        g.setColor(lineColor);
        g.drawPolygon( xpoints, ypoints, npoints );
//      System.out.println( npoints + " points of " + vertexCount + " point filled polygon painted " + lineColor );
        npoints = 0;
    }

}
