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
public class VectorMap extends Body {
    Dynamics ap;
    int bnum;

    Body body;
    boolean mapPresent = false;
    int mapSize = 2000;
    StateVector vector[] = new StateVector[mapSize];        // vector map with +z north, -y meridian
    StateVector flyVector[] = new StateVector[mapSize];     // spun, tilted, offset, and otherwise transformed vector map
    int vpoints;                                            // number of vectors in vector map
    Color colorpalette[] = new Color[10];
    double obliquity;                                       // tilt/obliquity of mapped body
    double siderealClock;                                   // rotation/spin of mapped body
    double siderealDay;
    StateVector sunXYZ;                                     // sun barycentric XYZ
    StateVector eyeXYZ;                                     // eye barycentric XYZ
    StateVector earthXYZ;                                   // eye barycentric XYZ
    double horizonDistance;


    // default constructor
    VectorMap() {
    }

    VectorMap( Dynamics a ) {
        ap = a;
    }

    // read in planet vectorMap
    VectorMap( Dynamics a, int bnum ) {
        this( a );

        EarthMap simpleEarthMap = new EarthMap();
        int nrows = simpleEarthMap.findNrows();

        for ( int n = 0; n < nrows; n++ ) {
            vector[n] = simpleEarthMap.readNextRow();
        }

        vpoints = nrows;

        mapPresent = true;
        createFlyVectorMap();
        setColorPalette();
    }

    // create XYZ axes vectorMap at lat-long-rmul on planet b0
    VectorMap( Dynamics a, int b0, double lat, double lon, double rmul, double scale ) {
        this( a );

        this.bnum = b0;
        AxisMap ax = new AxisMap( this.ap, bnum, lat, lon, rmul, scale );
        int nVectors = ax.findNvectors();

        for ( int n = 0; n < nVectors; n++ ) {
            vector[n] = ax.readNextVector();
        }

        vpoints = nVectors;

        mapPresent = true;
        createFlyVectorMap();
        setColorPalette();
    }

    // create a latitude-longitude vector map, maybe including tropics and degree labels
    VectorMap( Dynamics a, double mapRadius, double degreeStep, boolean tropics, boolean textOn ) {
        this( a );

        int i, j, m, n, nsteps, nhalfsteps;
        double x, y, z, vectorStepAngle, vectorHalfStepAngle, latitude, angle;

        sunXYZ = new StateVector();
        earthXYZ = new StateVector();
        eyeXYZ = new StateVector();

        // first vector is from equator in positive latitude, same longitude
        nsteps = (int)( 360.0 / degreeStep );
        vectorStepAngle = degreeStep * Mathut.degreesToRadians;
        nhalfsteps = nsteps * 2;
        vectorHalfStepAngle = vectorStepAngle * 0.5;
        y = mapRadius * Math.cos( vectorHalfStepAngle );
        z = mapRadius * Math.sin( vectorHalfStepAngle );

        i = 0;
        vector[i] = new StateVector( 0, 0, 0, 0, 0, 0 );     // vector[0] is centre of mapped body
        i++;
        vector[i] = new StateVector( 0, -mapRadius, 0, 0, -y, z );
        i++;

        for ( n=1; n<nhalfsteps; n++ ) {
            vector[i] = new StateVector( );
            vector[i] = Mathut.transformAroundXaxis( vector[i-1], vectorHalfStepAngle );
            vector[i].colour = 1;
            i++;
        }

        for( n=1; n<nsteps; n++ ) {
            angle = (double)n * vectorStepAngle;
            for ( j=0; j<nhalfsteps; j++ ) {
                vector[i] = Mathut.transformAroundZaxis( vector[j], angle );
                vector[i].colour = 1;
                i++;
            }
        }

        StateVector v = new StateVector();
        for ( m=0; m<nsteps; m++ ) {
            if ( m > 0 ) {
                latitude = 0.5 * Math.PI - (double)m * vectorStepAngle;
                x = mapRadius * Math.cos( latitude ) * Math.sin( vectorStepAngle );
                y = mapRadius * Math.cos( latitude ) * Math.cos( vectorStepAngle );
                z = mapRadius * Math.sin( latitude );
                vector[i] = new StateVector( x, y, z, 0, 0, 0 );
                v = Mathut.transformAroundZaxis( vector[i], vectorHalfStepAngle );
                vector[i].vx = v.x;
                vector[i].vy = v.y;
                vector[i].vz = v.z;
                vector[i].colour = 1;
                i++;
                for ( n=1; n<nhalfsteps; n++ ) {
                    vector[i] = Mathut.transformAroundZaxis( vector[i-1], vectorHalfStepAngle );
                    vector[i].colour = 1;
                    i++;
                }
            }
        }
        vpoints = i;

        createFlyVectorMap( );
        setColorPalette();
    }

    void setColorPalette() {
        colorpalette[0] = new Color(255, 255, 255);
        colorpalette[1] = new Color(200, 200, 200);
        colorpalette[2] = new Color(150, 150, 150);
        colorpalette[3] = new Color(100, 100, 100);
        colorpalette[4] = new Color(050, 050, 050);
        colorpalette[5] = new Color(000, 000, 000);
        colorpalette[6] = Color.red;
        colorpalette[7] = Color.GREEN;
        colorpalette[8] = Color.blue;
        colorpalette[9] = Color.cyan;
    }

    // create the set of vectors which will hold the updated map coordinates
    // produced on the fly at runtime.
    void createFlyVectorMap( ) {
        for ( int n=0; n<mapSize; n++ ) {
            flyVector[n] = new StateVector();
            flyVector[n].vtype = 1;
        }
    }

    // spin and tilt the vector map, and store in flyVector map..
    void spinAndTiltVectorMap( Body b0 ) {
        double rotate, fd;
        int j;

        fd = b0.siderealClock / b0.siderealDay;
        rotate = ( fd * 360.0 ) * Mathut.degreesToRadians;   // map longitude rotation angle (radians)
//      ob = b0.obliquity * Mathut.degreesToRadians;         // map obliquity rotation angle (radians)

        for (j=0; j<vpoints; j++) {                                                 // was j<vpoints-1 aug 2016
            flyVector[j] = Mathut.staticXYZ_to_relativeXYZ( rotate, b0, vector[j] );
            flyVector[j].colour = vector[j].colour;
            flyVector[j].vtype = vector[j].vtype;
        }

    }

    void paint( Graphics g, Color color ) {

        ap.offGraphics.setPaintMode();
        ap.offGraphics.setColor( color );
        for (int j=0; j<vpoints; j++) {
            if ( flyVector[j].z > 0 ) {
                ap.offGraphics.drawLine( (int)Mathut.x_t( ap, flyVector[j].x ), (int)Mathut.y_t( ap, flyVector[j].y ), (int)Mathut.x_t( ap, flyVector[j].vx ), (int)Mathut.y_t( ap, flyVector[j].vy ) );
            }
        }
    }

    void paint( Graphics g ) {

        ap.offGraphics.setPaintMode();
        ap.offGraphics.setColor( Color.lightGray );
        for (int j=0; j<vpoints; j++) {
            if ( flyVector[j].z > 0 ) {
                if ( flyVector[j].colour > 0 ) ap.offGraphics.setColor( colorpalette[flyVector[j].colour] );
                ap.offGraphics.drawLine( (int)Mathut.x_t( ap, flyVector[j].x ), (int)Mathut.y_t( ap, flyVector[j].y ), (int)Mathut.x_t( ap, flyVector[j].vx ), (int)Mathut.y_t( ap, flyVector[j].vy ) );
            }
        }
    }

}
