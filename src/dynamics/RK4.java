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




/*
 * RK4 class carries out RK4 motion calculations on all Bodies in Pool using static methods

class RK4{

    public static Pool pool;
    public static Body body;            // pool body under consideration

    public static void moveRK4( Pool p, double dt ) {
        int m, n;

        pool = p;

        // don't perform calculations using dt = 0. 19 aug 2016
        if ( dt != 0 ) {

            // find straight-line-advanced body locations
            for (n = 0; n < pool.nbodies; n++) {
//              if ( pool.b[n].status == 3 ) {
                    body = pool.b[n];
                    advanceBody(  dt );
//              }
            }

            // perform RK4 calculations on current state
            for (n = 1; n < pool.nbodies; n++) {
                body = pool.b[n];
                // here for RK4 acceleration
                if ( pool.b[n].status == 3 && pool.b[n].activated ) {
                    pool.state[n].copyStateVectors( pool.b[n].currentState );
                    pool.state[n] = integrate( pool.state[n], dt );
                }
            }

            // update positions and speeds
            for (n = 0; n < pool.nbodies; n++) {
                body = pool.b[n];
                if ( pool.b[n].status >= 2 && pool.b[n].activated && pool.b[n].inFreeMotion ) {
                    setVectors( pool.b[n], pool.state[n] );    // RK4
                }
            }

        }
    }


// RK4 code *************************************************************************************************************

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
    // Finds acceleration on this body due to all other bodies (> than some minimum mass)
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

        for( int n=0; n<pool.nbodies; n++ ) {
            if ( body.activated && body.inFreeMotion && (n != body.num) && (pool.b[n].status == 3) ) {

                x = state.x - pool.b[n].advanced[ai].x;                 // x distance from m
                y = state.y - pool.b[n].advanced[ai].y;                 // y distance ..   ..
                z = state.z - pool.b[n].advanced[ai].z;                 // z distance ..   ..
                r = Math.sqrt(x * x + y * y + z * z);                       // distance   ..   ..

                // only find interactions with massive bodies > 1 kg mass
                if ( pool.b[n].m > 1.0 )  {

                    // gravitational accelerations act on bodies if > 5 metres distance (to avoid large accelarations)
                    if ( pool.b[n].status == 3 && pool.b[n].activated  && ( r > 5.0 ) ) {
                        a = -(G * pool.b[n].m) / (r * r);                       // acceleration towards that
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

// end RK4 code ***********************************************************************************************************

}
*/



}
