/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamics;

/**
 *
 * @author CFD
 * 
 * Testbed creates a solar system with just sun and earth in circular orbit around Sun
 * and compares expected orbit period with measured orbit period using 3 different 
 * orbit radii and 3 different dt timesteps, printing out results.
 * 
 * There is no graphics display for this.
 */
public class SolarSystemTestbed extends Process{

    Dynamics ap;
    Body bd;
    double maxRadius = 0;
    double minRadius = 0;
    double expectedPeriod, actualPeriod, lastElapsedTime;
    double radiusGkm[] = { 108, 149.5837, 228 };
//  double radiusGkm[] = { 10, 20, 30 };
    double dtRange[] = { 4096, 2048, 1024 };
    int radiusIndex = 0;
    int dtIndex = 0;
    double mSun, mEarth, rEarth, vTangential, orbitCircumference, orbitPeriod, orbitError;
    public StateVector state[] = new StateVector[1000];                    // body states for RK4
    
    public SolarSystemTestbed( ) {        
    }
    
    public SolarSystemTestbed( Dynamics a ) {
        super();
        ap = a; 

        for ( int n=0; n<1000; n++ ) state[n] = new StateVector();
        
   }

    void setupTest() {
        // set up a small mass (earth) in circular orbit around a large mass (sun)
        mSun = 1.988544E30;                                                     // mass kg
        mEarth = 5.972465000E24;                                                // mass kg
        dtIndex = 0;
        radiusIndex = 0;
        
        getOrbitCharacteristics();

        // create solar system
        //          OrbitMin a, Pool pl, double jd, int number, String name, double x, double y, double z, double vx, double vy, double vz, double m, boolean ifm 
        b[nbodies] = new Body( ap, nbodies,  ap.currentJDCT,  0, 0, 0, 0, 0, 0, 0, 0, false );
        nbodies++;
        b[nbodies] = new Body( ap, nbodies,  ap.currentJDCT,  0, 0, 0, 0, 0, 0, mSun, 1.0, true );
        nbodies++;
        b[nbodies] = new Body( ap, nbodies,  ap.currentJDCT,  rEarth, 0, 0, 0, vTangential, 0, mEarth, 1.0, true );
        nbodies++;

//      pool.b[pool.nbodies] = new Body( ap, pool, 2458033.5, pool.nbodies, "BARYCENTRE", 0, 0, 0, 0, 0, 0, 0, false );
//      pool.b[pool.nbodies] = new Body( ap, pool, 2458033.5, pool.nbodies, "Sun", 0, 0, 0, 0, 0, 0, mSun, true );
//      pool.b[pool.nbodies] = new Body( ap, pool, 2458033.5, pool.nbodies, "Earth", rEarth, 0, 0, 0, vTangential, 0, mEarth, true );

    } 
    
    void test( int i, int j ) {
        dtIndex = i;
        radiusIndex = j;
                        
        System.out.println();
        System.out.println( i + "/" + j + " " + dtRange[dtIndex] + " r: " + radiusGkm[radiusIndex] + " 10^6 km" );

        // setup next orbit
        getOrbitCharacteristics();

        // reset sun and earth positions
        b[1].setBodyCharacteristics( "Sun", 0, 0, 0, 0, 0, 0, mSun, true );
        b[2].setBodyCharacteristics( "Earth", rEarth, 0, 0, 0, vTangential, 0, mEarth, true );

        // run a complete  single orbit
        int iterations = 0;
        boolean complete = false;
        while ( !complete ) {
            moveEuler( ap.dt );
            complete = checkTestComplete();
            checkRadius();
            iterations++;
            ap.clearScreen = true;
            ap.repaint();
//          b[2].currentState.printStateVectorKm();
        }    
//      System.out.println( "***RK4 iterations " + iterations);
        
    }   

    boolean checkTestComplete() {
        
        // check if planet has returned to x axis
        boolean complete = false;
//      System.out.println( "RK4 test " + ap.elapsedTime + " " + ap.dt + " " + b[2].currentState.y + " " + b[2].lastState.y );
        if ( ap.elapsedTime > ap.dt && b[2].currentState.y > 0 && b[2].lastState.y <= 0){
            // output orbital period
            System.out.println( "Test complete dt " + ap.dt + " radius " + rEarth + " " + ap.currentDate + " elapsed time " + ap.elapsedTime );
            actualPeriod = trueElapsedTime( ap.elapsedTime, b[2].currentState.y, b[2].lastState.y, ap.dt  )/( 60.0 * 60.0 * 24.0 );
            orbitError = ( actualPeriod - expectedPeriod ) / expectedPeriod;
            System.out.println( "Actual elapsed time " + actualPeriod + " days, expected period " + expectedPeriod + " error " + orbitError );
            System.out.println( "Orbit radius max error +" + maxRadius + ", actual radius " + rEarth + ", radius min error -" + minRadius + " metres" );
            resetRadii();
            complete = true;        
        }
      
        return( complete );
    } 
    
    void getOrbitCharacteristics() {
        ap.dt = dtRange[ dtIndex ];
        rEarth = radiusGkm[ radiusIndex ] * 1.0E09;                             // radial distance metres for 365.256 day orbit
        vTangential = Math.sqrt(ap.G * mSun / rEarth);                             // tangential velocity m/s
        orbitCircumference = Math.PI * 2.0 * rEarth;
        orbitPeriod = orbitCircumference / (vTangential * 60 * 60 * 24);        // orbit period in days
        expectedPeriod = orbitPeriod;
        ap.elapsedTime = 0;
        lastElapsedTime = 0;        
    }

   // starting out on the x axis, a body returns to the x axis when its last y position is -ve, and current y position is +ve
    double trueElapsedTime( double elapsedTime, double currentY, double lastY, double timestep ) {
        double trueElapsedTime = elapsedTime - ( currentY * timestep ) / ( currentY - lastY ); 
        return( trueElapsedTime );
    }
    
    void resetRadii() {
        maxRadius = 0;
        minRadius = 0;
    }
    
    void checkRadius() {
        double currentRadius = Mathut.distanceBetween( b[2], b[1].currentState );
        double rdifference;
        if ( currentRadius > rEarth ) {
            rdifference = currentRadius - rEarth;
            if ( rdifference > maxRadius ) {
                maxRadius = rdifference;
            }
        } else {
            rdifference = rEarth - currentRadius;
            if ( rdifference > minRadius ) {
                minRadius = rdifference;
            }
        }
    }

    void moveEuler( double dt ) {
        
        RK4.moveRK4( ap, b, nbodies, state, dt );
        
        ap.elapsedTime += ap.dt;
        
    }   
    
}


