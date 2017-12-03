/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamics;

import java.awt.*;

/**
 *
 * @author CFD
 */
public class SolarSystem extends Process {

//  Dynamics ap;

    double[][] ssbody = {
// (0) Barycentre, (1) Sun, (2) Mercury, (3) Venus, (4) Earth, (5) 5Mars, (6) Jupiter, (7) Saturn, (8) Uranus, (9) Neptune, (10) Luna  on date A.D. 2005-Feb-01 00:00:00.0000
// State vectors (kg/km/s): bnum, unused, mass, equ radius, polar radius, spin axis RA, spin axis Dec, rotation period, equinox adjust, JDCT (Julian date), x, y, z, vx, vy, vz, (NASA barycentric state vectors 25 April 2016)
        { 0, 0, 1.0, 1.0, 1.0, 0, 0, 0, 0, 2453402.500000000, 0, 0, 0, 0, 0, 0, },
        { 1, 0, 1.988544E30, 695500.032, 695500.032, 90, 0, 0, 0, 2453402.500000000, 6.449674202858886E+05, -1.127217154897759E+04, -1.579983536764750E+04,  4.682262903784831E-04,  1.246571925965659E-02, -1.185532757921838E-04, },
        { 2, 0, 3.301255063417226E23, 2440.0, 2440.0, 90, 0, 0, 0, 2453402.500000000, 1.486213953955887E+07, -6.635708505528798E+07, -6.740730134284206E+06,  3.786494218616802E+01,  1.271539606947296E+01, -2.437237098760083E+00, },
        { 3, 0, 4.867634243309893E24, 6051.8, 6051.8, 90, 0, 0, 0, 2453402.500000000, 1.641356471483682E+07, -1.076568267511855E+08, -2.398553057229348E+06,  3.441639485442118E+01,  4.964155860011824E+00, -1.918810867733495E+00, },
        { 4, 0, 5.972465000E24, 6371.01, 6371.01, 90, 0, 86164.090530833, 0, 2453402.500000000,  -9.829121543945661E+07,  1.092627561338944E+08, -1.701017000098526E+04, -2.257216806314086E+01, -2.008176105996227E+01,  1.204392043128166E-03, },
        { 5, 0, 6.417332641995618E23, 3389.9, 3389.9, 90, 0, 0, 0, 2453402.500000000,  -1.202238020070589E+08, -1.944316515253759E+08, -1.120180061675429E+06,  2.148983152751387E+01, -1.070305946423849E+01, -7.525648800168048E-01, },
        { 6, 0, 1.8986457800595672E27, 71492.0, 71492.0, 90, 0, 0, 0, 2453402.500000000,  -8.046376438857579E+08, -1.310800227631731E+08,  1.854828208607547E+07,  1.939471731899542E+00, -1.227959993064404E+01,  7.521753766719819E-03, },
        { 7, 0, 5.6849685143514635600949590702329E26, 58232.0, 58232.0, 90, 0, 0, 0, 2453402.500000000,  -5.609701355551313E+08,  1.233703176435795E+09,  8.586447359350324E+05, -9.309953235005809E+00, -4.021597846108083E+00,  4.408977577691868E-01, },
        { 8, 0, 8.6824683933763012819043797233395E25, 25362.0, 25362.0, 90, 0, 0, 0, 2453402.500000000,   2.750976959152953E+09, -1.199801396431557E+09, -4.010011731303924E+07,  2.672552487415579E+00,  5.924859647738733E+00, -1.278620255266505E-02, },
        { 9, 0, 1.0243763726603912365620370237477E26, 24624.0, 24624.0, 90, 0, 0, 0, 2453402.500000000,   3.180752853262133E+09, -3.180639539596162E+09, -7.804349508290529E+06,  3.806788498204133E+00,  3.874609434499094E+00, -1.674144641864925E-01, },
        { 10, 0, 7.347075E22, 1737.53, 1727.53, 90, 0, 0, 0, 2453402.500000000,   -9.863742604331432E+07,  1.090923285386415E+08, -1.738000310727954E+04, -2.207565004953833E+01, -2.096271183399073E+01, -8.798304982805849E-02, }
    };
    final int BNUM = 0;         // body number ( Sun=1, Mercury=2 ... Luna=10 )
    final int UNUSED = 1;
    final int MASS = 2;         // body mass (kg)
    final int ERADIUS = 3;      // equatorial radius (km)
    final int PRADIUS = 4;      // polar radius (km)
    final int SPINRA = 5;       // spin axis Right Ascension (degrees)
    final int SPINDEC = 6;      // spin axis Declination (degrees)
    final int SPINPERIOD = 7;   // spin period (seconds)
    final int SPINEQUADJ = 8;   // spin equinox adjustment (seconds?)
    final int JDCT = 9;         // Julian Date (days)
    final int X = 10;           // body X location (km)
    final int Y = 11;           //  ..  Y    ..
    final int Z = 12;           //  ..  Z    ..
    final int VX = 13;          //  ..  X velocity (km/sec)
    final int VY = 14;          //  ..  Y    ..
    final int VZ = 15;          //  ..  Z    ..

    String bnames[] = { "Sol, Mercury, Venus, Earth, Mars, Jupiter, Saturn, Uranus, Neptune, Luna" };

    double axialRotation[][] = {
    //   RA (degrees),  dec,  x,   y,   z,   period, obliquity  source http://nssdc.gsfc.nasa.gov/planetary/planetfact.html
    //  Obliquity figures from https://en.wikipedia.org/wiki/Axial_tilt.
    //  "obliquity is the angle between the axis of rotation and the direction perpendicular to the orbital plane"
    //  My calculations of axial tilt only agree with those of Sun and Earth shown below. Others are a few degrees different.
    //  I suspect that exial tilt of other planets may be measured with respect to their own orbital planes rather than ecliptic plane.
    //  { 37.9461468,  89.26413805,  0,  0,  0,  100.0, 0 },             // RA dec of Polaris (for test purposes)
        { 0,  0,  0,  0,  0,  0,  0 },                // 0 barycentre
        { 286.130, 63.870, 0, 0, 0, 609.12, 7.25 },   // 1 sun
        { 281.010, 61.414, 0, 0, 0, 1407.6, 0.03 },   // 2 mercury
        {  92.76, -67.16, 0, 0, 0, 5832.6, 2.64 },    // 3 venus +ve pole
        {    0.0,  90.0,  0, 0, 0, 23.9345, 23.44 },  // 4 earth
        { 317.681, 52.887, 0, 0, 0, 24.6229, 25.19 }, // 5 mars
        { 268.057, 64.495, 0, 0, 0, 9.9250, 3.13 },   // 6 jupiter
        { 40.589, 83.537, 0, 0, 0, 10.656, 26.73 },   // 7 saturn
        {  77.311, 15.175, 0, 0, 0, 17.24, 82.23 },   // 8 uranus +ve pole
        { 299.36, 43.46, 0, 0, 0, 16.11, 28.32 },     // 9 neptune
        {    0.0, 90.0, 0, 0, 0, 660.0,  0 },             // 10 Luna
    };

    Map bmap[] = new Map[12];                   // body maps

    SolarSystem() { }

    // this constructor used to create solar system from ssbody[] array
    SolarSystem( Dynamics a ) {

        double jd = 0;
        this.ap = a;
        int nrows = ssbody.length;
        int ncols = ssbody[0].length;
        StateVector s;

        for ( int n=0; n<nrows; n++ ) {
            jd = ssbody[n][JDCT];
            s = new StateVector( km_m(ssbody[n][X]), km_m(ssbody[n][Y]), km_m(ssbody[n][Z]), km_m(ssbody[n][VX]), km_m(ssbody[n][VY]), km_m(ssbody[n][VZ]) );
            b[n] = new Body( ap, (int)ssbody[n][BNUM], s, ssbody[n][MASS], ssbody[n][ERADIUS]*1000.0, true );
            b[n].siderealClock = 0.0;
            b[n].siderealDay = axialRotation[n][5] * 60.0 * 60.0;               // body rotation period
            b[n].skipRadius = 1.0 * b[n].r;                                     // arriving bodies bounce off st skipRadius
            b[n].referenceFrame = 0;                                            // barycentric/heliocentric reference frame
            this.nbodies++;
        }
        ap.nPlanets = this.nbodies;
        b[0].inFreeMotion = false;      // fix barycentre

        ap.setCalendar( jd );
    }

    // this used to read Orbit3D csv file of planet state vectors
    SolarSystem( Dynamics a, String fn ) {
        super( a, fn );
        this.ap = a;
    }


    void createMaps() {
        // create maps
        for ( int n=0; n<ap.nPlanets; n++ ) {
            bmap[n] = new Map( ap, n, ssbody[n][ERADIUS]*1000.0 );
        }
    }


    void moveEuler( double dt ) {
        int n;

        for ( n=0; n<nbodies; n++ ) {
            b[n].zero_acceleration();
        }

        for ( n=0; n<nbodies; n++ ) {
            b[n].add_gravitational_acceleration();
            b[n].add_damping_acceleration();
        }

        for ( n=0; n<nties; n++ ) {
            t[n].add_elastic_accelerations(b);
        }

        for ( n=0; n<nbodies; n++ ) {
            if ( b[n].inFreeMotion ) {
                b[n].new_speed( dt );
            } else {
                b[n].currentState.vx = 0;
                b[n].currentState.vy = 0;
                b[n].currentState.vz = 0;
            }
        }

        for ( n=0; n<nbodies; n++ ) {
            b[n].new_position( dt );
        }
    }

    void setAllSiderealClocks( double elapsed_time  ) {
        for ( int n=1; n<nbodies; n++ ) {
            b[n].siderealClock = elapsed_time % b[n].siderealDay;
        }
    }



    void paint( Graphics g ) {
        int n;

        // paint bodies
        for ( n=0; n<nbodies; n++ ) {
            b[n].paint( g );
        }

        // paint ties
        for ( n=0; n<nties; n++ ) {
            t[n].paint( g );
        }

        this.bmap[ ap.centralBody ].paint( g );

    }

    double km_m( double v ) {
        return( 1000.0 * v );
    }

    void setAxialRotationParams() {
        int n;
        int nmax = nbodies;
        if ( nmax > 10 ) nmax = 10;

        // set up Earth axes first
        setAxialRotationParams( 4 );

        // then do all other planet axes
        for ( n=0; n<nmax; n++ ) {
            setAxialRotationParams( n );
        }
    }

    void setAxialRotationParams( int n ) {
        double ra, dec;
        StateVector v = new StateVector();
        StateVector vo = new StateVector( 0, 0, 0, 0, 0, 0 );
        StateVector vz = new StateVector( 0, 0, 1000, 0, 0, 0);
        StateVector vx = new StateVector( 1000, 0, 0, 0, 0, 0);
        StateVector s = new StateVector();


            b[n].axial[0] = axialRotation[n][0];
            b[n].axial[1] = axialRotation[n][1];
            ra = axialRotation[n][0] / 15.0;        // convert degrees to hours
            dec = axialRotation[n][1];
            v = Mathut.raDec_to_relativeXYZ( ra, dec, 1.0E+20 );
            v = Mathut.transformAroundXaxis( v, -23.44*Mathut.degreesToRadians);   // correct for earth's obliqity
            axialRotation[n][2] = v.x;              // barycentric XYZ of planet axial N pole
            axialRotation[n][3] = v.y;
            axialRotation[n][4] = v.z;
            b[n].axial[2] = v.x;
            b[n].axial[3] = v.y;
            b[n].axial[4] = v.z;

            b[n].rotationPeriod = axialRotation[n][5] * 60.0 * 60.0;            // seconds

            s = Mathut.barycentricXYZ_to_RADec( b[4], v );

            // use north or positive pole xyz to find rotation angles from +z axis
            double alpha = Math.atan2( v.y, v.z );                  // north pole y / z
            double h = Math.sqrt( v.y*v.y + v.z*v.z );              // h = sqrt( y*y + z*z )
            double beta = Math.atan2( v.x, h );                     // north pole x / h
            b[n].axial[5] = alpha;                                  // angle Zaxis - Origin - hypotenuse
            b[n].axial[6] = beta;                                   // angle hypotenuse - Origin - NorthPole
            b[n].axial[7] = Math.sin(alpha);
            b[n].axial[8] = Math.cos(alpha);
            b[n].axial[9] = Math.sin(beta);
            b[n].axial[10] = Math.cos(beta);

    }

    void setAxialRotationParams( int n, double ra, double dec ) {
        StateVector m = new StateVector();

            b[n].axial[0] = ra;
            b[n].axial[1] = dec;
            ra = ra / 15.0;        // convert degrees to hours
            m = Mathut.raDec_to_relativeXYZ( ra, dec, 1.0E+20 );
            m = Mathut.transformAroundXaxis( m, -23.44*Mathut.degreesToRadians);   // correct for earth's obliqity
            b[n].axial[2] = m.x;                    // barycentric XYZ of planet axial N pole
            b[n].axial[3] = m.y;
            b[n].axial[4] = m.z;

            // use north pole xyz to find rotation angles from +z axis
            double alpha = Math.atan2( m.y, m.z );                  // north pole y / z
            double h = Math.sqrt( m.y*m.y + m.z*m.z );              // h = sqrt( y*y + z*z )
            double beta = Math.atan2( m.x, h );                     // north pole x / h
            b[n].axial[5] = alpha;                                  // angle Zaxis - Origin - hypotenuse
            b[n].axial[6] = beta;                                   // angle hypotenuse - Origin - NorthPole
            b[n].axial[7] = Math.sin(alpha);
            b[n].axial[8] = Math.cos(alpha);
            b[n].axial[9] = Math.sin(beta);
            b[n].axial[10] = Math.cos(beta);

    }

}
