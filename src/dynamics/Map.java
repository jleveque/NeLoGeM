/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamics;

import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author CFD
 */
public class Map extends Body {

    Dynamics ap;
    int bnum;
    VectorMap vmap[] = new VectorMap[10];
    double screenXoffset, screenYoffset, screenXYscale;
    double[][] bodyMap = {
        { 1.00, 90.0, 0, 2, },                                                  // N pole
        { 1.00, -90.0, 0, 1, },                                                 // S pole
        { 1.0000001569609842, 52.0339, -2.4235, 2, },
        { 1.0010001569609842, 52.0339, -1.4235, 1, }
    };

    final int RMUL = 0;
    final int LAT = 1;
    final int LON = 2;
    final int PEN = 3;

    // default constructor
    Map() { }

    Map( Dynamics a, int num, double r ) {
        this.ap = a;
        this.bnum = num;
//      System.out.println( "bnum " + this.bnum );

        vmap[1] = new VectorMap( this.ap, r, 30.0, false, false );              // lat-long grid map

        if ( this.bnum == 4 ) {
            vmap[0] = new VectorMap( this.ap, this.bnum );                      // simple Earth map
            this.mapExists = true;
//          vmap[2] = new VectorMap( this.ap, this.bnum,  60.0, 60.0, 1.0, 3000000.0 );       // XYZ axes map
        } else {
            vmap[0] = new VectorMap();
//          vmap[2] = new VectorMap();
        }

    }

    void paint( Graphics g ) {
        int n;
        double fractionOfDay;
        StateVector v = new StateVector();
        ap.offGraphics.setPaintMode();
        ap.offGraphics.setColor( Color.blue );

        // paint planet map
        if ( vmap[0].mapPresent ) {
            vmap[0].spinAndTiltVectorMap( ap.ss.b[bnum] );
            vmap[0].paint( g, Color.BLACK );
        }
/*
        // paint axes  map
        if ( vmap[2].mapPresent ) {
            vmap[2].spinAndTiltVectorMap( ap.ss.b[bnum] );
            vmap[2].paint( g );
        }
*/
        // paint latitude and longitude grid map
        vmap[1].spinAndTiltVectorMap( ap.ss.b[bnum] );
        vmap[1].paint( g );

        // paint simple map
        for ( n=0; n<4; n++ ) {
            v.latitude = bodyMap[n][LAT];
            v.longitude = bodyMap[n][LON];
            v.length = 1.0;
            fractionOfDay = ap.siderealClock / ( ap.SiderealDay ) ;
            v = Mathut.degreesLatLong_to_relativeXYZ( ap.ss.b[bnum], v, fractionOfDay  );
            ap.offGraphics.drawLine( (int)Mathut.x_t( ap, v.x ), (int)Mathut.y_t( ap, v.y ), (int)Mathut.x_t( ap, v.x ), (int)Mathut.y_t( ap, v.y ) );
        }


    }

    public void paint(Graphics g, Eye eye, Body b0 ) {
        int n, j, x1, x2, y1, y2, l1, z1, nmaps, nvectors;
        double r, a, d, l, angle, hr;
        boolean scaleIt = true;
        StateVector loc = new StateVector();
        loc.setStateVector( eye.locus.x, eye.locus.y, eye.locus.z, eye.locus.vx, eye.locus.vy, eye.locus.vz );
        loc.zangle = eye.locus.zangle;
/*
        StateVector s[] = new StateVector[ hcirclePoints ];
        StateVector q = new StateVector();
        StateVector b = new StateVector();

        // repaint vectormap white, skipping eye locus map[0] and map[1]
//      g.setPaintMode();
//      g.setColor(Color.white);
//      for (j = 2; j < vpoints; j++) {
//          eye.paintVector( g, vectorMap[j], scaleIt );
//      }

        nmaps = 4;
        for ( n=0; n<=nmaps; n++ ) {
//          System.out.println(n);
            loc.setStateVector( eye.locus.x, eye.locus.y, eye.locus.z, eye.locus.vx, eye.locus.vy, eye.locus.vz );
            vmap[n].siderealClock = b0.siderealClock;
            vmap[n].siderealDay = b0.SiderealDay;
            vmap[n].obliquity = b0.obliquity;
            vmap[n].setRefBodies();

            // get a circle of points on b0 where horizon appears
            if ( n==3  ) {
                // vtype = 7 for filled polygon
                vmap[n].getHorizonVectorMap2( vmap[n].eyeXYZ, b0, 64, true, 7 );
            }

            // get terminator circle
            if ( n==4  ) {
                // vtype = 1 for standard vectors
                vmap[n].getHorizonVectorMap2( vmap[n].sunXYZ, b0, 64, false, 1 );
            }

            vmap[n].spinAndTiltVectorMap( b0 );

            vmap[n].relocateVectorMap( b0.x, b0.y, b0.z );

            // eye position is eye.locus.x, y, z
            // eye sees in direction eye.locus.vx, vy, vz
            // transform eye position to barycentric origin, with eye looking down z axis (onto an x-y plane)
            // and transform everything else the same way.
            b = vmap[n].transformVectorMapAroundEye( b0, loc );
            if ( n == 0) q.setStateVector( b );

        }

        // paint transformed maps
        nvectors = vpoints;
        for ( n=0; n<=nmaps; n++ ) {
            // find distance down z axis where points aren't visible
            a = Math.sqrt( b.z*b.z - b0.r*b0.r );
            angle = Math.atan2( b0.r, a );
            d = -a * Math.cos(angle);
            d = -vmap[3].horizonDistance;
//          if ( n==3 ) System.out.println( "horizonDistance " + d );
            vmap[n].paintVectorMap( g, eye, d, false, n );
        }
*/

//      printCSVflyMap( 3 );

    }


}
