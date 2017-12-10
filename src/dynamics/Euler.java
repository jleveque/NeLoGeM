/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamics;

import static dynamics.RK4.body;

/**
 *
 * @author CFD
 */


public class Euler{

    public static Dynamics ap;
//  public static Body body;            // pool body under consideration



    public static void moveEuler( Dynamics a, Body[] b, int nbodies, double dt ) {
        int n, m;
        ap = a;

        for ( n=0; n<nbodies; n++ ) {
            zero_acceleration( b[n] );
        }

        for ( n=0; n<nbodies; n++ ) {
            add_gravitational_acceleration( b[n] );
            add_damping_acceleration( b[n] );
        }

//        for ( n=0; n<nties; n++ ) {
//            t[n].add_elastic_accelerations(b);
//        }

        for ( n=0; n<nbodies; n++ ) {
            if ( b[n].inFreeMotion ) {
                new_speed( b[n], dt );
            } else {
                b[n].currentState.vx = 0;
                b[n].currentState.vy = 0;
                b[n].currentState.vz = 0;
            }
        }

        for ( n=0; n<nbodies; n++ ) {
            new_position( b[n], dt );
        }

    }


    /**********start Euler Approximation***********************************************************************/

    // Reset acceleration to zero
    public static void zero_acceleration( Body body ) {
      body.currentState.ax = 0;
      body.currentState.ay = 0;
      body.currentState.az = 0;
    }

    // Calculate accelerations due to gravitational attraction.
    public static void add_gravitational_acceleration( Body body ) {
        double x, y, z, r, a;
        double G = 6.671984315419034E-11;   // G = NASA mu / b[0].m        Orbit3D sep 2017 value
        Body that;

        if ( ap.localGravity ) {
//          body.currentState.ay = body.currentState.ay - smallg;
        } else {
            // go through SolarSystem ss and find gravitational accelerations due to each planet in it.
            for ( int n=0; n<ap.nPlanets; n++ ) {
                that = ap.ss.b[n];
                if ( body.inFreeMotion && body.num != that.num ) {
                    x = body.currentState.x - that.currentState.x;               // x distance from m
                    y = body.currentState.y - that.currentState.y;               // y distance ..   ..
                    z = body.currentState.z - that.currentState.z;               // z distance ..   ..
                    r = Math.sqrt(x * x + y * y + z * z);                        // distance   ..   ..
                    a = -(G * that.m) / (r * r);                                 // acceleration towards that
                    body.currentState.ax = body.currentState.ax + a * x / r;     // x component of accel
                    body.currentState.ay = body.currentState.ay + a * y / r;     // y component of accel
                    body.currentState.az = body.currentState.az + a * z / r;     // z component of accel
                }
            }

        }
    }

    // Calculate accelerations due to damping forces proportional to velocity.
    public static void add_damping_acceleration( Body body ) {
      body.currentState.ax = body.currentState.ax - body.currentState.vx * body.damping / body.m;
      body.currentState.ay = body.currentState.ay - body.currentState.vy * body.damping / body.m;
      body.currentState.az = body.currentState.az - body.currentState.vz * body.damping / body.m;
    }

    // Calculate new body speed after time t
    public static void new_speed( Body body, double t ){
        body.currentState.vx = body.currentState.vx + body.currentState.ax * t;       // new x velocity
        body.currentState.vy = body.currentState.vy + body.currentState.ay * t;       // new y velocity
        body.currentState.vz = body.currentState.vz + body.currentState.az * t;       // new z velocity
    }

    // Calculate new body position after time t
    public static void new_position( Body body, double t ){
        body.lastState.copyStateVectors( body.currentState );
        body.currentState.x = body.currentState.x + body.currentState.vx * t;         // new x
        body.currentState.y = body.currentState.y + body.currentState.vy * t;         // new y
        body.currentState.z = body.currentState.z + body.currentState.vz * t;         // new z
    }

    /********** end Euler Approximation***********************************************************************/

}
