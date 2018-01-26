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
public class Body {

/*
 * Body class refers to individual bodies
 */

    static int uniqueNumber = 0;
    int unique;
    int objectType = 1;
    Dynamics ap;

    int num;                                                                    // body number
    String name;                                                                // body name
    int status;
    boolean activated;
    boolean inFreeMotion;
    int referenceFrame;
    double m;                                                                   // body mass (kg)
    double r;                                                                   // body radius (m)
    StateVector advanced[] = new StateVector[3];                                // RK4 positions
    StateVector currentState;                                                   // current xyz, vx, vy, vz
    StateVector lastState;                                                      // last    xyz, vx, vy, vz
    double jd;                                                                  // Julian date
    double efx, efy, efz;                                                       // elastic accelerations
    double smallg = 9.81;                                                       // elastic forces
    double damping = 0.00;                                                      // a value of 1.85 gives 53 m/s terminal velocity in sea level air
    double axial[] = new double[12];                                            // planet spin axis data
    double siderealDay;
    double siderealClock;                                                       // duplicate of ap siderealClock
    double rotationPeriod;
    double skipRadius;
    Map bmap;
    boolean mapExists = false;
    boolean paint_it = true;

    /* default constructor */
    public Body(){
        ap = null;
        currentState = new StateVector( 0, 0, 0, 0, 0, 0 );
        unique = uniqueNumber;

        // Increment uniqueNumber, but don't allow max int value to be exceeded
        if ( uniqueNumber < Integer.MAX_VALUE ) {
            uniqueNumber++;
        } else {
            uniqueNumber = 0;
        }
    }

        /* body constructor - MKS units */
    public Body( Dynamics a, int bn, StateVector v, double bm, double radius, boolean ifm, boolean mapped ) {
        this(); // Call default constructor

        ap = a;
        currentState.copyStateVectors( v );

        num = bn;
        m = bm;
        r = radius;
        inFreeMotion = ifm;

//      setBodyCharacteristics(  name, v.x, v.y, v.z, v.vx, v.vy, v.vz, m, ifm );

        for ( int n=0; n<=2; n++ ) advanced[n] = new StateVector();

        lastState = new StateVector();

        status = 3;
        activated = true;

        if ( mapped ) {
            bmap = new Map( ap, bn, radius );
        }
    }

    /* body constructor - MKS units */
    public Body( Dynamics a, int bn, double jd, double x, double y, double z, double vx, double vy, double vz, double bm, double radius, boolean ifm, boolean mapped ) {
        this( a, bn, new StateVector(x, y, z, vx, vy, vz), bm, radius, ifm, mapped );

        this.jd = jd;
    }

    void setBodyCharacteristics( String name, double x, double y, double z, double vx, double vy, double vz, double m, boolean ifm ) {
        this.name = name;
        this.m = m;
        r = 1.0;
        currentState.x = x;
        currentState.y = y;
        currentState.z = z;
        currentState.vx = vx;
        currentState.vy = vy;
        currentState.vz = vz;
        inFreeMotion = ifm;
    }


    // Body paint method draws body as single pixel, circle, or mapped sphere....
    // copied and adapted from Orbit3D.java
    void paint(Graphics g, Eye eye ) {
        int colorIndex;
        double apparentRadius, d;
        StateVector loc = new StateVector();
        loc.setStateVector( eye.locus.x, eye.locus.y, eye.locus.z, eye.locus.vx, eye.locus.vy, eye.locus.vz );
        loc.zangle = eye.locus.zangle;
        StateVector v = new StateVector( this.currentState.x, this.currentState.y, this.currentState.z, 0, 0, 0 );

//      if ( this ==  eye.bObject ) System.out.println( "eye locus v " + this.num + ": " + loc.vx + " " + loc.vy + " " + loc.vz );

        if ( this != eye.bSubject && this.paint_it ) {                       // don't paint eye subject body

            // v contains barycentric x,y,z and vx,vy,vz
//          if ( this ==  eye.bObject ) System.out.println( "eye object " + this.num + ": " + v.x + " " + v.y + " " + v.z );
            v = Mathut.transformAroundEye( v, loc );
//          if ( this ==  eye.bObject ) System.out.println( "transformedAroundEye object " + this.num + ": " + v.x + " " + v.y + " " + v.z );
            d = Math.sqrt( v.x*v.x + v.y*v.y + v.z*v.z );
            apparentRadius = Math.abs( this.r * eye.dimension.z / d );
//          if ( this.num == 4 ) System.out.println( this.num + " apparent radius " + apparentRadius );

//          System.out.println( "body " + this.mapExists );

            if ( this.mapExists && apparentRadius > 575 ) {
                // here to draw circle with map inside
                this.bmap.screenXYscale = ap.v_scale;
                this.bmap.screenXoffset = ap.v_xoffset;
                this.bmap.screenYoffset = ap.v_yoffset;
//              System.out.println( "paint body map " + this.num);
                this.bmap.paint( g, eye, this );

            } else if ( apparentRadius > 1.5 ) {
                // here to draw circle
                g.setPaintMode();
                g.setColor( Color.black );

//              if ( this.num == 1 ) {
//                  System.out.println( "app r " + apparentRadius );
//                  System.out.println( this.r + " " + eye.dimension.z + " " + v.z );
//              }
                eye.paintCircle( g, v, apparentRadius, 5 );
                ap.offGraphics.setPaintMode();
                ap.offGraphics.setColor(Color.black);
                if ( ap.showNumbers ) {
                    eye.paintString( ap.offGraphics, "" + this.num, 2, v );
                } else if ( ap.showNames ) {
                    eye.paintString( ap.offGraphics, "" + this.name, 2, v );
                }

            } else {
                // here to draw single pixel
                // v contains body position transformed around eye onto -z axis
                g.setPaintMode();
                g.setColor( Color.black );

                if ( this.num <= 10 ) {
                    colorIndex = 2;    // if planet, colour black
                }  else {
                    colorIndex = 6;
                }

                eye.paintPoint( g, v, this.num, colorIndex );
                ap.offGraphics.setPaintMode();
                ap.offGraphics.setColor(Color.black);
                if ( ap.showNumbers ) {
                    eye.paintString( ap.offGraphics, "" + this.num, 2, v );
                } else if ( ap.showNames ) {
                    eye.paintString( ap.offGraphics, "" + this.name, 2, v );
                }

            }
        }
    }


    // centralBody is current planet selected to be viewed
    void paint( Graphics g ) {
        double lx, ly, lz, rpixels;

        ap.offGraphics.setPaintMode();
        ap.offGraphics.setColor( Color.blue  );
        lx = this.currentState.x - ap.ss.b[ ap.centralBody ].currentState.x;    // paint relative to central body
        ly = this.currentState.y - ap.ss.b[ ap.centralBody ].currentState.y;
        lz = this.currentState.z - ap.ss.b[ ap.centralBody ].currentState.z;

        rpixels = l_t ( this.r );
        if ( rpixels < 2 ) {
            if ( this.num == 11 ) {
                ap.offGraphics.setColor( Color.red  );
     //         System.out.println( "b11 rel XYZ km " + lx/1000.0 + ", " + ly/1000.0 + ", " + lz/1000.0 + " XYpix " + (int)x_t( lx ) + ", " +  (int)y_t( ly ) );
            }
            // draw point
            ap.offGraphics.drawLine( (int)x_t( lx ), (int)y_t( ly ), (int)x_t( lx ), (int)y_t( ly ) );
//          ap.offGraphics.drawLine( (int)x_t( lx )-1, (int)y_t( ly ), (int)x_t( lx )+1, (int)y_t( ly ) );
//          ap.offGraphics.drawLine( (int)x_t( lx ), (int)y_t( ly )-1, (int)x_t( lx ), (int)y_t( ly )+1 );
        } else {
            // draw circle around centralBody
            int circlePoints = 24;
            double stepAngle = Math.PI * 2.0 / circlePoints;
            double x0 = this.r;
            double y0 = 0;
            StateVector circlePoint = new StateVector( x0, y0, 0, x0, y0, 0 );
            circlePoint = Mathut.transformAroundZaxis( circlePoint, stepAngle );
            circlePoint.x = x0;
            circlePoint.y = y0;

            for (int n=0; n<circlePoints; n++ ) {
                ap.offGraphics.drawLine( (int)x_t( circlePoint.x + lx), (int)y_t( circlePoint.y + ly ), (int)x_t( circlePoint.vx + lx ), (int)y_t( circlePoint.vy + ly ) );
                circlePoint = Mathut.transformAroundZaxis( circlePoint, stepAngle );
            }
        }


    }

    // x transformation to screen coordinates
    int x_t(double x ) {
        return (int) ( ap.screenXYscale * x + ap.screenXoffset);
    }

    // y transformation to screen coordinates
    int y_t(double y ) {
        return (int) (ap.screenXYscale * -y + ap.screenYoffset);
    }

    // length transformation to screen pixels
    int l_t(double l ) {
        return (int) ( ap.screenXYscale * l);
    }

//  /**********start Euler Approximation***********************************************************************/
/*
    // Reset acceleration to zero
    public void zero_acceleration() {
      this.currentState.ax = 0;
      this.currentState.ay = 0;
      this.currentState.az = 0;
    }

    // Calculate accelerations due to gravitational attraction.
    public void add_gravitational_acceleration() {
        double x, y, z, r, a;
        double G = 6.671984315419034E-11;   // G = NASA mu / b[0].m        Orbit3D sep 2017 value
        Body that;

        if ( ap.localGravity ) {
            this.currentState.ay = this.currentState.ay - smallg;
        } else {
            // go through SolarSystem ss and find gravitational accelerations due to each planet in it.
            for ( int n=0; n<ap.nPlanets; n++ ) {
                that = ap.ss.b[n];
                if ( this.inFreeMotion && this.num != that.num ) {
                    x = this.currentState.x - that.currentState.x;               // x distance from m
                    y = this.currentState.y - that.currentState.y;               // y distance ..   ..
                    z = this.currentState.z - that.currentState.z;               // z distance ..   ..
                    r = Math.sqrt(x * x + y * y + z * z);                        // distance   ..   ..
                    a = -(G * that.m) / (r * r);                                 // acceleration towards that
                    this.currentState.ax = this.currentState.ax + a * x / r;     // x component of accel
                    this.currentState.ay = this.currentState.ay + a * y / r;     // y component of accel
                    this.currentState.az = this.currentState.az + a * z / r;     // z component of accel
                }
            }

        }
    }

    // Calculate accelerations due to damping forces proportional to velocity.
    public void add_damping_acceleration() {
      this.currentState.ax = this.currentState.ax - this.currentState.vx * damping / this.m;
      this.currentState.ay = this.currentState.ay - this.currentState.vy * damping / this.m;
      this.currentState.az = this.currentState.az - this.currentState.vz * damping / this.m;
    }

    // Calculate new body speed after time t
    public void new_speed( double t ){
        this.currentState.vx = this.currentState.vx + this.currentState.ax * t;       // new x velocity
        this.currentState.vy = this.currentState.vy + this.currentState.ay * t;       // new y velocity
        this.currentState.vz = this.currentState.vz + this.currentState.az * t;       // new z velocity
    }

    // Calculate new body position after time t
    public void new_position( double t ){
        this.lastState.copyStateVectors( this.currentState );
        this.currentState.x = this.currentState.x + this.currentState.vx * t;         // new x
        this.currentState.y = this.currentState.y + this.currentState.vy * t;         // new y
        this.currentState.z = this.currentState.z + this.currentState.vz * t;         // new z
    }
*/
//  /********** end Euler Approximation***********************************************************************/


}



