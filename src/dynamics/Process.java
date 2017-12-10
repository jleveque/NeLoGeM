/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamics;

import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 *
 * @author Me
 */
public class Process {

/*
 * A process is any dynamic assemblage of bodies and ties.
 */
    Dynamics ap;
    Body b[] = new Body[1000];
    int nbodies = 0;
    Tie t[] = new Tie[1000];
    int nties = 0;
    double siderealDay;
    double siderealClock;                                                       // duplicate of ap siderealClock

    final static int TYPE = 0;         // body number ( Sun=1, Mercury=2 ... Luna=10 )
    final static int BNUM = 1;         // body number ( Sun=1, Mercury=2 ... Luna=10 )
    final static int MASS = 2;         // body mass (kg)
    final static int RADIUS = 3;       // polar radius (km)
    final static int JDCT = 4;         // polar radius (km)
    final static int X =  5;           // body X location
    final static int Y =  6;           //  ..  Y    ..
    final static int Z =  7;           //  ..  Z    ..
    final static int VX =  8;          //  ..  X velocity
    final static int VY =  9;          //  ..  Y    ..
    final static int VZ = 10;          //  ..  Z    ..
    final static int IFM = 11;         //  ..  Z    ..

    String line = "body, 1, 1.988544E30, 0.0, 0.01, 6.449674202858886E8, -1.1272171548977591E7, -1.57998353676475E7, 0.4682262903784831, 12.46571925965659, -0.11855327579218379, true";

    public Process() { }

    public Process( Dynamics a ) {
        this.ap = a;
    }

    public Process( Dynamics a, String filename ) {
        double jd;

        this.ap = a;
        jd = gpCSVreader( filename );
        ap.setCalendar( jd );

    }

    // Orbit3D csvReader
    // read .csv (comma separated variables) file containing body Keplerian orbital elements
    // (need to set working directory and applet.policy )
    double gpCSVreader( String file ) {
        double m, r, jd, startJulianDate;
        int rowcount, fieldcount;
        String dataRow;
        BufferedReader CSVFile;
        String rs[] = new String[400];
        rowcount = 0;
        StateVector sv = new StateVector();
        startJulianDate = 1E20;

        System.out.println( "gpCSVreader" );

        try {
            CSVFile = new BufferedReader(new FileReader(file));
            rowcount = 0;
            try {
                dataRow = CSVFile.readLine(); // Read first line.
            } catch (IOException e) {
                dataRow = new String("");
            }

            while (dataRow != null) {

                StringTokenizer st = new StringTokenizer(dataRow, ",");
                fieldcount = 0;
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    rs[ fieldcount] = s;
                    fieldcount++;
                }

                if (rowcount > 0) {
                        m = Double.valueOf(rs[1]).doubleValue();
                        r = Double.valueOf(rs[2]).doubleValue() * 1000.0;
                        jd = Double.valueOf(rs[3]).doubleValue();
                        sv.x = Double.valueOf(rs[5]).doubleValue() * 1000.0;
                        sv.y = Double.valueOf(rs[6]).doubleValue() * 1000.0;
                        sv.z = Double.valueOf(rs[7]).doubleValue() * 1000.0;
                        sv.vx = Double.valueOf(rs[8]).doubleValue() * 1000.0;
                        sv.vy = Double.valueOf(rs[9]).doubleValue() * 1000.0;
                        sv.vz = Double.valueOf(rs[10]).doubleValue() * 1000.0;
                        if ( jd < startJulianDate ) startJulianDate = jd;
                        b[this.nbodies] = new Body( ap, this.nbodies, sv, m, r, true );
                        nbodies++;
                }
                rowcount++;

                try {
                    dataRow = CSVFile.readLine(); // Read next line.
                } catch (IOException e) {
                    dataRow = new String("");
                }
            }

            // Close the file once all data has been read.
            try {
                CSVFile.close();
            } catch (IOException e) {
            }

        } catch (IOException e) {
            System.out.println("File not found");
        }

        System.out.println( rowcount + " lines read from " + file );

        return( startJulianDate );
    }



    static void printProcess( Dynamics ap, Body b[], Tie t[] ) {
        int nrows = b.length;

        for ( int n=0; n<nrows; n++ ) {
            System.out.print( "body, ");
            System.out.print(  b[n].num + ", " + b[n].m + ", " + b[n].r + ", " + b[n].jd   );
            System.out.println(  b[n].num + ", " + b[n].currentState.x + ", " + b[n].currentState.y + ", " + b[n].currentState.z + ", " + b[n].currentState.vx + ", " + b[n].currentState.vy + ", " + b[n].currentState.vz + ", " + b[n].inFreeMotion );
        }

        nrows = t.length;
        for ( int n=0; n<nrows; n++ ) {
            System.out.print( "tie, ");
            System.out.println(  t[n].num + ", " + t[n].b1.num + ", " + t[n].b2.num + ", " + t[n].tie_free_length + ", " + t[n].tie_max_length + ", " + t[n].tie_K );
        }
    }

    static void readProcess( Dynamics ap, String lines[], Body b[], Tie t[]  ) {
        int n, fieldcount;
        BufferedReader CSVFile;
        String rs[] = new String[400];

        for ( n=0; n<lines.length; n++ ) {

            while (lines[n]!= null) {

                StringTokenizer st = new StringTokenizer( lines[n], ",");
                fieldcount = 0;
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    rs[ fieldcount] = s;
                    fieldcount++;
                }

                if ( rs[0].equals("body") ) {
                    b[n].num = (int)Double.valueOf(rs[BNUM]).doubleValue();
                    b[n].m = Double.valueOf(rs[MASS]).doubleValue();
                    b[n].r = Double.valueOf(rs[RADIUS]).doubleValue();
                    b[n].jd = Double.valueOf(rs[JDCT]).doubleValue();
                    b[n].currentState.x = Double.valueOf(rs[X]).doubleValue();
                    b[n].currentState.y = Double.valueOf(rs[Y]).doubleValue();
                    b[n].currentState.z = Double.valueOf(rs[Z]).doubleValue();
                    b[n].currentState.vx = Double.valueOf(rs[VX]).doubleValue();
                    b[n].currentState.vy = Double.valueOf(rs[VY]).doubleValue();
                    b[n].currentState.vy = Double.valueOf(rs[VZ]).doubleValue();
                    b[n].inFreeMotion = true;
                    if ( rs[IFM].equals("false")  ) { b[n].inFreeMotion = false; }
                } else if ( rs[0].equals("tie") ) {

                }

            }
        }
    }


    void moveEuler( double dt ) {
        int n, m;

//      System.out.println("euler " + nbodies);
        Euler.moveEuler(ap, b, nbodies, dt);

    }

    void paint( Graphics g ) {
        int n;

//      System.out.println("euler paint " + nbodies);

        // paint bodies
        for ( n=0; n<nbodies; n++ ) {
            b[n].paint( g );
        }

        // paint ties
        for ( n=0; n<nties; n++ ) {
            t[n].paint( g );
        }
    }

}



