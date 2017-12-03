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

    Map() { }

    Map( Dynamics a, int num, double r ) {
        this.ap = a;
        this.bnum = num;
//      System.out.println( "bnum " + this.bnum );

        vmap[1] = new VectorMap( this.ap, r, 30.0, false, false );              // lat-long grid map

        if ( this.bnum == 4 ) {
            vmap[0] = new VectorMap( this.ap, this.bnum );                      // simple Earth map
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
}
