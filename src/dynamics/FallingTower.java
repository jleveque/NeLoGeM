/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamics;

import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author CFD
 */
public class FallingTower {

    Dynamics ap;
    ReferenceFrame rf;
    int bref;                   // reference body on which pendulum is sited
    int bnum;                   // body number of pendulum

    StateVector currentState[] = new StateVector[50];
    StateVector newState[] = new StateVector[50];
    StateVector barycentricState[] = new StateVector[50];
    StateVector clone[] = new StateVector[50];
    int firstBnum, lastBnum, index;
    int nVectors;
    double screenXYscale;
    double screenXoffset;
    double screenYoffset;

    // default constructor
    FallingTower() { }

    // The original Foucault pendulum was in the Pantheon, Paris, latitude 48.846252, longitude 2.346157
    // He suspended a 28-kg brass-coated lead bob with a 67-m-long wire from the dome of the Panthéon, Paris.
    // The pendulum's swing rotated clockwise approximately 11.3° per hour, making a full circle in approximately 31.8 hours
    FallingTower( Dynamics a, int ref, double lat, double lon, double rmul, double extend ) {
        this.ap = a;
        this.bref = ref;

        screenXYscale =  200000.0 * 10.0/1E11;
        screenXoffset = ap.xmax / 2.0;
        screenYoffset = ap.ymax / 2.0;

        createStructure( 500000, 500000, 500000 );

        rf = new ReferenceFrame( ap, bref, lat, lon, rmul, extend );

        makeClone();
        barycentricState = rf.transformToBarycentricXYZ( clone, nVectors );

        firstBnum = ap.ss.nbodies;
        for ( int n=0; n<nVectors; n++ ) {
            ap.ss.b[ ap.ss.nbodies ] = new Body( ap, ap.ss.nbodies, barycentricState[n], 28.0, 100000.0, true );
            ap.ss.nbodies++;
        }
        lastBnum = ap.ss.nbodies;
        index = nVectors;

    }

    // restore body positions
    void moveReferenceFrame() {
        int i;
        StateVector[] sv = new StateVector[50];

        // clone barycentric state vectors
        i = firstBnum;
        for ( int n=0; n<nVectors; n++  ) {
            sv[n] = new StateVector();
            sv[n].copyStateVectors( ap.ss.b[i].currentState );
            i++;
        }
        // transform barycentric state vectors back to reference frame state vectors
        newState = rf.transformFromBarycentricXYZ( sv, nVectors );

        // copy newState -> currentState for bodies >= bnum
        for ( i=0; i<nVectors; i++ ) {
            if ( currentState[i].flag1 == 1 ) {
                // currentStae unchanged
            } else {
                if ( newState[i].x < 0 ) {
//                  System.out.println( "body[" + i + "].x = " + newState[i].x );
//                  newState[i].vx = -newState[i].vx;
                }
                currentState[i].copyStateVectors( newState[i] );
                currentState[i].flag1 = 0;
            }
        }

        makeClone();                        // this so as to prevent currentState being changed during transformation
        barycentricState = rf.transformToBarycentricXYZ( clone, nVectors );
        int n = firstBnum;
        for ( int m=0; m<nVectors; m++  ) {
            if ( isFixed( currentState[m] ) ) {
                ap.ss.b[n].currentState.copyStateVectors( barycentricState[m] );
            }
            n++;
        }

    }

    // create some sort of structure somewhere on the surface of the earth
    void createStructure( double dx, double dy, double dz ) {
        int n = 0;
        StateVector s = new StateVector();
        for ( int i=0; i<2; i++ ) {
            for ( int j=0; j<2; j++ ) {
                for ( int k=0; k<12; k++ ) {
                    currentState[n] = new StateVector( k * dx, i * dy, j * dz, 0, 0, 0 );
                    currentState[n] = flagFixed( currentState[n] );       // flag fixed
//                  System.out.println( "structure " + n + ": " + currentState[n].x + ", "  + currentState[n].y + ", "  + currentState[n].z  + " flag1 " + currentState[n].flag1 );
                    n++;
                }
            }
        }
        nVectors = n;
    }

    StateVector flagFixed( StateVector v ) {
        v.flag1 = 1;
        return( v );
    }

    StateVector flagUnfixed( StateVector v ) {
        v.flag1 = 0;
        return( v );
    }

    boolean isFixed( StateVector v ) {
        boolean fixed = false;
        if ( v.flag1 == 1 ) fixed = true;
        return( fixed );
    }

    // make clone of current reference frame state vectors
    void makeClone() {
        for ( int n=0; n<nVectors; n++ ) {
            clone[n] = this.currentState[n].cloneStateVector();
        }
    }

    void paint( Graphics g ) {
        int x, y, r;
        ap.offGraphics.setPaintMode();
        ap.offGraphics.setColor( Color.blue   );

        for ( int n=0; n<nVectors; n++ ) {
            x = x_t( currentState[n].x );
            y = y_t( currentState[n].y );
            r = 10;
            ap.offGraphics.drawOval( x, y, r, r );
        }
    }

    // x transformation to screen coordinates
    int x_t( double x ) {
        return (int) ( screenXYscale * x + screenXoffset);
    }

    // y transformation to screen coordinates
    int y_t( double y ) {
        return (int) ( screenXYscale * -y + screenYoffset);
    }

    // length transformation to screen pixels
    int l_t( double l ) {
        return (int) ( screenXYscale * l);
    }


}
