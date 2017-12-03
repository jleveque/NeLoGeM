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

    /* default constructor */
    public Body(){}

    /* body constructor - MKS units */
    public Body( Dynamics a, int bn, double jd, double x, double y, double z, double vx, double vy, double vz, double bm, double radius, boolean ifm ) {
        this.unique = uniqueNumber++;
        this.ap = a;
        this.currentState = new StateVector( x, y, z, vx, vy, vz );
        this.num = bn;
        this.m = bm;
        this.r = radius;
        this.inFreeMotion = ifm;

//      setBodyCharacteristics(  name, x, y, z, vx, vy, vz, m, ifm );

        lastState = new StateVector();

        this.status = 3;
        this.activated = true;
    }

    /* body constructor - MKS units */
    public Body( Dynamics a, int bn, StateVector v, double bm, double radius, boolean ifm ) {
        this.unique = uniqueNumber++;
        this.ap = a;
        this.currentState = new StateVector( 0, 0, 0, 0, 0, 0 );
        currentState.copyStateVectors( v );

        this.num = bn;
        this.m = bm;
        this.r = radius;
        this.inFreeMotion = ifm;

//      for ( int n=0; n<=2; n++ ) advanced[n] = new StateVector();
        lastState = new StateVector();

        this.status = 3;
        this.activated = true;
    }

    void setBodyCharacteristics(  String name, double x, double y, double z, double vx, double vy, double vz, double m, boolean ifm ) {
        this.name = name;
        this.m = m;
        this.r = 1.0;
        this.jd = jd;
        this.currentState.x = x;
        this.currentState.y = y;
        this.currentState.z = z;
        this.currentState.vx = vx;
        this.currentState.vy = vy;
        this.currentState.vz = vz;
        this.inFreeMotion = ifm;
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

    /**********start Euler Approximation***********************************************************************/

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

    /********** end Euler Approximation***********************************************************************/

}
