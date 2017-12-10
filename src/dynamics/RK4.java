/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamics;

/**
 *
 * @author CFD
 */
public class RK4 {




// RK4 class carries out RK4 motion calculations on all Bodies in Pool using static methods

    public static Dynamics ap;
//  public static Pool pool;
    public static Body body;            // pool body under consideration

    // adapted from OrbitNin.java
    // b[] array contains nbodies from Solar System ss or elsewhere
    // state[] is a temporary array
    // gravitational accelerations are only calculated using Solar System bodies
    public static void moveRK4( Dynamics a, Body[] b, int nbodies, StateVector[] state, double dt ) {
        int m, n;

        ap = a;

        // don't perform calculations using dt = 0. 19 aug 2016
        if ( dt != 0 ) {

            // find straight-line-advanced body locations
            for (n = 1; n < nbodies; n++) {
                if ( b[n].status == 3 ) {
//                  System.out.println( nbodies + "b num " + b[n].num );
                    body = b[n];
//                  System.out.println( "body num " + body.num );
//                  System.out.println( body.currentState.x );
                    advanceBody(  dt );
                }
            }

            // perform RK4 calculations on current state
            for (n = 1; n < nbodies; n++) {
                body = b[n];
                // here for RK4 acceleration
                if ( b[n].status == 3 && b[n].activated ) {
                    state[n].copyStateVectors( b[n].currentState );
                    state[n] = integrate( state[n], dt );
                }
            }

            // update positions and speeds
            for (n = 0; n < nbodies; n++) {
                body = b[n];
                if ( b[n].status >= 2 && b[n].activated && b[n].inFreeMotion ) {
                    setVectors( b[n], state[n] );    // RK4
                }
            }

        }
    }


/* RK4 code *************************************************************************************************************/

    // copied from http://gafferongames.com/game-physics/integration-basics/
    // derivatives dx, dv are held as vx, ax in a StateVector.
    // y and z derivatives are also calculated, of course.
    // It is passed the current body state vectors in StateVector variable 'state',
    // and it returns a StateVector state after interval dt.
    public static StateVector integrate( StateVector state, double dt) {
         StateVector dummy = new StateVector();
         StateVector derivative_a, derivative_b, derivative_c, derivative_d;


         derivative_a = evaluate(state, 0.0, dummy, 0  );              // derivative a returns initial velocity and acceleration
         derivative_b = evaluate(state, dt*0.5, derivative_a, 1 );
         derivative_c = evaluate(state, dt*0.5, derivative_b, 1 );
         derivative_d = evaluate(state, dt, derivative_c, 2 );

         double dxdt = 1.0/6.0 * (derivative_a.vx + 2.0f*(derivative_b.vx + derivative_c.vx) + derivative_d.vx);
         double dydt = 1.0/6.0 * (derivative_a.vy + 2.0f*(derivative_b.vy + derivative_c.vy) + derivative_d.vy);
         double dzdt = 1.0/6.0 * (derivative_a.vz + 2.0f*(derivative_b.vz + derivative_c.vz) + derivative_d.vz);
         double dvxdt = 1.0/6.0 * (derivative_a.ax + 2.0f*(derivative_b.ax + derivative_c.ax) + derivative_d.ax);
         double dvydt = 1.0/6.0 * (derivative_a.ay + 2.0f*(derivative_b.ay + derivative_c.ay) + derivative_d.ay);
         double dvzdt = 1.0/6.0 * (derivative_a.az + 2.0f*(derivative_b.az + derivative_c.az) + derivative_d.az);

         state.x = state.x + dxdt * dt;
         state.y = state.y + dydt * dt;
         state.z = state.z + dzdt * dt;
         state.vx = state.vx + dvxdt * dt;
         state.vy = state.vy + dvydt * dt;
         state.vz = state.vz + dvzdt * dt;

         return( state );
    }


    // http://gafferongames.com/game-physics/integration-basics/
    // returns derivative dx, dv as vx, ax in StateVector
    public static StateVector evaluate( StateVector initial, double t, StateVector d, int aindex ){
        StateVector state = new StateVector();
        state.x = initial.x + d.vx * t;
        state.y = initial.y + d.vy * t;
        state.z = initial.z + d.vz * t;
        state.vx = initial.vx + d.ax * t;
        state.vy = initial.vy + d.ay * t;
        state.vz = initial.vz + d.az * t;

        return( acceleration( state, aindex ) );
    }


    // RK4 acceleration
    // Finds acceleration on this body due to all other bodies
    // Returns this body's current position, speed, and acceleration in a StateVector
    public static StateVector acceleration( StateVector state, int ai ){
        double x, y, z, r, a;
        double G = 6.671984315419034E-11;   // G = NASA mu / b[0].m        Orbit3D sep 2017 value
        a = 0;
        StateVector acc = new StateVector();
        StateVector s = new StateVector();

        acc.copyStateVectors( state );                        // set accelerations to zero
        acc.ax = 0;
        acc.ay = 0;
        acc.az = 0;

        for( int n=0; n<ap.ss.nbodies; n++ ) {
            if ( body.activated && body.inFreeMotion && (n != body.num) && (ap.ss.b[n].status == 3) ) {

                x = state.x - ap.ss.b[n].advanced[ai].x;                 // x distance from m
                y = state.y - ap.ss.b[n].advanced[ai].y;                 // y distance ..   ..
                z = state.z - ap.ss.b[n].advanced[ai].z;                 // z distance ..   ..
                r = Math.sqrt(x * x + y * y + z * z);                       // distance   ..   ..

                // only find interactions with massive bodies > 1 kg mass
                if ( ap.ss.b[n].m > 1.0 )  {

                    // gravitational accelerations act on bodies if > 5 metres distance (to avoid large accelarations)
                    if ( ap.ss.b[n].status == 3 && ap.ss.b[n].activated  && ( r > 5.0 ) ) {
                        a = -(G * ap.ss.b[n].m) / (r * r);                       // acceleration towards that
                        acc.ax = acc.ax + a * x / r;                                // x component of accel
                        acc.ay = acc.ay + a * y / r;                                // y component of accel
                        acc.az = acc.az + a * z / r;                                // z component of accel
                    }
                }
            }
        }

        return( acc );
    }

    // set new positions and speeds using RK4 calculations
    public static void setVectors( Body body, StateVector state ) {

        body.lastState.copyStateVectors( body.currentState );
        body.currentState.x = state.x;
        body.currentState.y = state.y;
        body.currentState.z = state.z;
        body.currentState.vx = state.vx;
        body.currentState.vy = state.vy;
        body.currentState.vz = state.vz;

    }

    // produce advanced positions 0 s ahead, dt/2 ahead, and dt ahead i
    public static void advanceBody( double t ) {

        for ( int i=0; i<=2; i++ ) {
            body.advanced[i].x = body.currentState.x + body.currentState.vx * (double)i * t / 2.0;
            body.advanced[i].y = body.currentState.y + body.currentState.vy * (double)i * t / 2.0;
            body.advanced[i].z = body.currentState.z + body.currentState.vz * (double)i * t / 2.0;
        }

    }

/* end RK4 code ***********************************************************************************************************/



}
