/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamics;

/**
 *
 * @author CFD
 */
public class AxisMap extends VectorMap {

    Dynamics ap;
    double latitude;
    double longitude;
    double radius;
    int bref;
    StateVector axes[] = new StateVector[3];
    int currentVector;

    AxisMap() {}

    AxisMap( Dynamics a, int refBody, double lat, double lon, double rmul, double scale) {
        this.ap = a;
        bref = refBody;
        latitude = lat;
        longitude = lon;
        setupAxes();

        // move axes from centre of refBody out along x axis to surface.
        StateVector o = new StateVector();
        o = new StateVector( rmul * ap.ss.b[bref].r, 0, 0, 0, 0, 0 );
        extendAxes( scale );         // make axes 500 km long
        moveAxes( o );

        // transform axes to place origin at required latitude and longitude on refBody
        transformAroundYaxis( latitude );
        transformAroundZaxis( longitude );

        // axes[0] is now z axis pointing skyward on surface (true)
        // axes[1] is now x axis pointing east (true)
        // axes[2] is now y axis pointing north (true)

        swapAxes();
        swapAxes();
        //  axes[0] is now x axis pointing east
        //  axes[1] is now y axis pointing north
        //  axes[2] is now z axis pointing skyward on surface



//      System.out.println( "required    " + lat + ", " + lon + ", " + rmul );
//      System.out.println( "axes origin " + axes[0].x + ", " + axes[0].y + ", " + axes[0].z );
//      System.out.println( "axes Z " + axes[0].vx + ", " + axes[0].vy + ", " + axes[0].vz );
//      System.out.println( "axes Y " + axes[1].vx + ", " + axes[1].vy + ", " + axes[1].vz );
//      System.out.println( "axes X " + axes[2].vx + ", " + axes[2].vy + ", " + axes[2].vz );

        currentVector = 0;

        axes[0].colour = 6;      // red
        axes[1].colour = 7;      // green
        axes[2].colour = 8;      // blue

    }

    void setupAxes() {
        // set up XYZ axes
        // origin ---> x axis
        axes[0] = new StateVector( 0, 0, 0, 1.0, 0, 0 );
        // y axis
        axes[1] = new StateVector( 0, 0, 0, 0, 1.0, 0 );
        // z axis
        axes[2] = new StateVector( 0, 0, 0, 0, 0, 1.0 );
    }

    // extend axes longer
    void extendAxes( double mul ) {
        for (int n=0; n<3; n++ ) {
            axes[n].vx = axes[n].vx * mul;
            axes[n].vy = axes[n].vy * mul;
            axes[n].vz = axes[n].vz * mul;
        }
    }

    void moveAxes( StateVector s ) {
        for (int n=0; n<3; n++ ) {
            axes[n].x += s.x;
            axes[n].y += s.y;
            axes[n].z += s.z;
            axes[n].vx += s.x;
            axes[n].vy += s.y;
            axes[n].vz += s.z;
        }
    }

    // axis0 --> axis1, axis1 --> axis2, axis2 --> axis0,
    void swapAxes() {
        StateVector v = new StateVector();
        v = axes[0];
        axes[0] = axes[2];
        axes[2] = axes[1];
        axes[1] = v;

    }

    void transformAroundXaxis( double angle ) {
        axes[0] = Mathut.transformAroundXaxis( axes[0], angle * Mathut.degreesToRadians );
        axes[1] = Mathut.transformAroundXaxis( axes[1], angle * Mathut.degreesToRadians );
        axes[2] = Mathut.transformAroundXaxis( axes[2], angle * Mathut.degreesToRadians );
    }

    void transformAroundYaxis( double angle ) {
        axes[0] = Mathut.transformAroundYaxis( axes[0], angle * Mathut.degreesToRadians );
        axes[1] = Mathut.transformAroundYaxis( axes[1], angle * Mathut.degreesToRadians );
        axes[2] = Mathut.transformAroundYaxis( axes[2], angle * Mathut.degreesToRadians );
    }

    void transformAroundZaxis( double angle ) {
        axes[0] = Mathut.transformAroundZaxis( axes[0], angle * Mathut.degreesToRadians );
        axes[1] = Mathut.transformAroundZaxis( axes[1], angle * Mathut.degreesToRadians );
        axes[2] = Mathut.transformAroundZaxis( axes[2], angle * Mathut.degreesToRadians );
    }

     int findNvectors() {
         return( 3 );
     }

     StateVector readNextVector() {
         return( axes[currentVector++] );
     }

}
