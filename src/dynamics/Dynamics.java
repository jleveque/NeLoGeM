/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamics;

/**
 *
 * @author CFD
 */
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.Math;
import java.text.SimpleDateFormat;
import javax.imageio.ImageIO;
import java.io.FileReader;
import javax.swing.*;

public class Dynamics extends JFrame implements Runnable, MouseListener, WindowListener {

    int option = 0;

    JPanel canvas;
    BufferedImage offImage;
    Graphics offGraphics, ctlGraphics;

    Dimension canvasSize;

    private Thread testThread = null;

    boolean go = true;
    boolean clearScreen = true;
    int count;
    int centralBody = 4;
    double screenXoffset, screenYoffset, screenXYscale;
    Eye eye;
    double dt = 32.0;
    double G = 6.671984315419034E-11;       // gravitational constant
    boolean localGravity = false;
    SolarSystemApollo11 ss;
    Tetrahedron tetra;
    Process proc;
//  FoucaultPendulum fp1;
    FallingTower ft1;

    // copied for Eye()
    int view = 0;                  // 0 = x-y (down z axis), 1 = x-z (down y-axis), 2 = y-z (down x axis)
    boolean yearRotate = false;   // set true to rotate eye around z axis annually
    double startJDCT;
    boolean showNumbers = false;    // show body numbers
    boolean showNames = false;      // show body names
    double v_xoffset, v_yoffset, v_scale, zoom1x;

    double daySecs = 24.0 * 60.0 * 60.0;     // seconds per day

    // elapsed time since simulation start (18hours added to turn Greenwich towards sun on 21 dec 2012)
    // correction 0f 198.2 seconds to match Betelgeuse altitude and azimuth (Feb 2015)
    double elapsedTime = 64800.0 - 197.44;

    double baseJDCT = 2456283.000000000;     // base calendar Julian Date (days) = 21 Dec 2012 12:00
    double currentJDCT;                      // current Julian Date (days)
    double initialJDCT;
    double targetJDCT;
    double finalJDCT;
    double baseModifiedJDCT = 2400000.5;     // base of Modified Julian Date (add to produce Julian Date)
    double currentYear = 2012;
    double currentMonth = 12;
    double currentDay = 21;
    double currentSecond = 0.0;
    String currentDate = "21 Dec 2012 12.00";
    double siderealClock = 0.0;
    double SiderealDay = 86164.090530833;    // Earth sidereal day duration (seconds)
    int nPlanets;

    Dynamics() {
        go = true;

        // ss = new SolarSystem( this );
        ss = new SolarSystemApollo11( this );
//      ss = new SolarSystemTestbed( this );
        tetra = new Tetrahedron( this );

        ss.setAxialRotationParams();
//      ss.createMaps();
        ss.setAllSiderealClocks( elapsedTime );

//      System.out.println( 7.023694035922034E+07 - 7.023698945180266E7  );
//      ss.setupTest();
        this.addWindowListener( this );   // Register this class as a window event listener
        this.addMouseListener( this );    // Register this class as a mouse event listener

        canvasSize = new Dimension( 400, 400 );

        offImage = new BufferedImage( canvasSize.width,
                canvasSize.height,
                BufferedImage.TYPE_INT_RGB );
        offGraphics = offImage.getGraphics();
        offGraphics.setColor( Color.white );
        offGraphics.fillRect( 0, 0, canvasSize.width, canvasSize.height );

        // Create a JPanel object for drawing on
        canvas = new JPanel() {
            @Override
            protected void paintComponent( Graphics g ) {
                super.paintComponent( g );

                offGraphics.setPaintMode();
                if ( clearScreen ) {
                    offGraphics.setColor( Color.white );
                    offGraphics.fillRect( 0, 0, canvasSize.width, canvasSize.height );
                }

                offGraphics.setColor( Color.black );

                // 2D paint paints selected option centralBody
                if ( option == 10 || option == 4 || option == 0 ) {
                    if ( count > 6 ) {
                        ft1.paint( g );
                    }
                }
                ss.paint( g );
                tetra.paint( g );

                if ( option == 0 ) {
                    // 3D paint
                    // reset paintstroke count;
                    eye.npaintstrokes = 0;
                    // paint all paintstrokes
                    eye.paint( g, offGraphics, offGraphics );

//                  showEyeSelection(false);
                    eye.drawEyeFrame( offGraphics );
                }

                offGraphics.setColor( Color.black );
                offGraphics.drawString( currentDate, 5, canvasSize.height - 10 );

                g.drawImage( offImage, 0, 0, null );
            }
        };

        canvas.setPreferredSize( canvasSize );

        // Configure the window (JFrame)
        this.setTitle( "NeLoGeM Orbital Simulation Model" );
        this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        this.setContentPane( canvas );
        this.pack();  // Size the JFrame so that all its contents are at or above their preferred sizes
        this.setLocationRelativeTo( null );  // Create window centered on screen
        this.setVisible( true );

        screenXYscale = 4500.0 * 10.0 / 1E11;
//      screenXYscale = 1.0 * 10.0/1E11;
        screenXoffset = canvasSize.width / 2.0;
        screenYoffset = canvasSize.height / 2.0;

        // Eye zoom
        v_xoffset = screenXoffset;
        v_yoffset = screenYoffset;
        zoom1x = v_xoffset / 4.55E+12;
//      zoom1x = v_xoffset / 2.55E+10;
        v_scale = 512.0 * 1024.0 * zoom1x;

        // Eye instantiation
        eye = new Eye( this, 0, 0, 1E13, 0, 0, 0, 1E12 );  // standard view down onto solar system
        eye.setEyeSubjectBody( ss.b[10] );      // Moon
        eye.setEyeObjectBody( ss.b[4] );        // Earth
        eye.typeOfEye = 1;
        System.out.println( "Eye Subject " + eye.bSubject.num + " looks at eye object " + eye.bObject.num );
        eye.printEyeLocus();
        ss.b[4].currentState.printStateVectorKm();
        ss.b[10].currentState.printStateVectorKm();

        selectOption();
    }

    public void start() {
        if ( testThread == null ) {
            testThread = new Thread( this, "Test1" );
            testThread.start();
        }
    }

    public void run() {
        Thread.currentThread().setPriority( Thread.MIN_PRIORITY );
        Thread myThread = Thread.currentThread();
        while ( testThread == myThread ) {
            if ( go ) {

//              eye.printEyeLocus();
/*
                // i, j are dt and orbit radius indices used in TestBed
                for ( int j=0; j<3; j++ ) {
                    for ( int i=0; i<3; i++ ) {
                        ss.test( i, j );
                    }
                }
                 */
//              go = false;
                ss.moveEuler( dt );
                tetra.moveEuler( dt );
                if ( count > 5 ) {
                    ft1.moveEuler( dt );
                }

                if ( count == 5 ) {
                    ft1 = new FallingTower( this, 4, 32.543618, 44.42408, 1.0, 100000.0 );   // Babylon
                }

                if ( count > 5 ) {
                    ft1.moveReferenceFrame();

                    if ( count % 100 == 0 ) {
                        if ( ( option == 4 || option == 10 ) && ft1.index > 0 ) {
                            ft1.index--;
                            ft1.flagUnfixed( ft1.currentState[ft1.index] );
                        }
                    }
                }

                eye.setEye();

                if ( option == 1 || option == 12 ) {
                    System.out.println( currentDate + " " + Mathut.distanceBetween( ss.b[11], ss.b[4].currentState ) / 1000.0 );
                    ss.b[11].currentState.printStateVectorKm();
                }

                currentDate = ( advanceCalendar( dt ) );
                ss.setAllSiderealClocks( elapsedTime );

//              System.out.println( Mathut.distanceBetween( ss.b[4], ss.b[10].currentState ));
                repaint();
                count++;
            }

            try {
                Thread.sleep( 50 );
            } catch ( InterruptedException e ) {
            }
        }
    }

    public void stop() {
        testThread = null;
    }

    void setCalendar( double jd ) {
        double advanceTime;

        // only update calendar once
        // by adding the difference between current JDCT (from csv file) and base Julian Days.
        if ( currentJDCT == 0.0 ) {
            initialJDCT = jd;
            currentJDCT = baseJDCT;
            advanceTime = jd - baseJDCT;
            advanceCalendar( daySecs * advanceTime );

            setDateByJulianDay( currentJDCT );
        }
    }

    // advance calendar using Julian Day
    String advanceCalendar( double step ) {
        String sdate;
        double secondsInDay = 24 * 60 * 60;

        // advance elapsed time and current Julian Date
        elapsedTime += step;
        currentJDCT += step / secondsInDay;

        // update sidereal clock
        siderealClock = elapsedTime % SiderealDay;

        // get new date and time
        sdate = setDateByJulianDay( currentJDCT );
        sdate += " dt=" + (float)step + " s";

        return ( sdate );
    }

    String setDateByJulianDay( double julianDay ) {
        double z, w, x, a, b, c, d, e, f;
        double nyear, nmonth, nday, nhour, nminute, nsecond;
        double daysInMonth[] = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        String monthsOfYear[] = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        double monthsInYear = 12;
        double secondsInDay = 24 * 60 * 60;
        double secondsInHour = 60 * 60;
        double secondsInMinute = 60;
        String sdate, dayPrefix, hourPrefix;

        int year, month, dayOfMonth;
// from   http://quasar.as.utexas.edu/BillInfo/JulianDatesG.html
//        To convert a Julian Day Number to a Gregorian date, assume that it is for 0 hours,
//        Greenwich time (so that it ends in 0.5). Do the following calculations, again dropping
//        the fractional part of all multiplicatons and divisions.
//        Note: This method will not give dates accurately on the Gregorian Proleptic Calendar,
//        i.e., the calendar you get by extending the Gregorian calendar backwards to years earlier
//        than 1582. using the Gregorian leap year rules. In particular, the method fails if Y<400.
//        check 2299160.5 = 1582 October 15        (checked)

        z = julianDay + 0.5;
        w = (int)( ( z - 1867216.25 ) / 36524.25 );
        x = (int)( w / 4 );
        a = (int)z + 1 + w - x;
        b = (int)a + 1524;
        c = (int)( ( b - 122.1 ) / 365.25 );
        d = (int)( 365.25 * c );
        e = (int)( ( b - d ) / 30.6001 );
        f = (int)( 30.6001 * e );
        currentDay = (int)( b - d - f );
        currentMonth = (int)( e - 1 );
        if ( currentMonth > 12 ) {
            currentMonth = (int)( e - 13 );   //(must get number less than or equal to 12)
        }
        if ( currentMonth <= 2 ) {
            currentYear = (int)( c - 4715 );  // (if Month is January or February) or C-4716 (otherwise)
        } else {
            currentYear = (int)( c - 4716 );
        }

        // Julian days seem to start at noon.
        currentSecond = ( julianDay - (int)julianDay ) * secondsInDay;
        currentSecond -= secondsInDay * 0.5;
        if ( currentSecond < 0.0 ) {
            currentSecond = 0.5 * secondsInDay + ( 0.5 * secondsInDay + currentSecond );
        }
        nsecond = currentSecond;
        nhour = (int)( nsecond / secondsInHour );
        nsecond = nsecond % secondsInHour;
        nminute = (int)( nsecond / secondsInMinute );
        nsecond = nsecond % secondsInMinute;

        // put into string format
        sdate = "" + prefix( currentDay ) + " "
                + monthsOfYear[(int)currentMonth] + " "
                + (int)currentYear + " "
                + prefix( nhour ) + ":"
                + prefix( nminute ) + ":"
                + prefix( nsecond );

        return ( sdate );
    }

    // prefix with '0' if necessary
    String prefix( double number ) {
        String s;
        if ( number < 10.0 ) {
            s = "0" + (int)number;
        } else {
            s = "" + (int)number;
        }
        return ( s );
    }

    void selectOption() {
        if ( option == 0 ) {
            // 3D view of Earth from Monn
            centralBody = 4;                        // was 4
            clearScreen = true;                     // was true
            dt = 128.0;                            // was 512.0
            screenXYscale = 45000.0 * 10.0 / 1E11;   // was 200000.0 * 10.0/1E11;
        } else if ( option == 1 ) {
            // Apollo 11 trajectory
            centralBody = 4;
            clearScreen = false;
            dt = 60.0;
            screenXYscale = 4500.0 * 10.0 / 1E11;
        } else if ( option == 2 ) {
            // Sun and planet orbits
            centralBody = 1;
            clearScreen = false;
            dt = 2048.0;
            screenXYscale = 1.0 * 10.0 / 1E11;
        } else if ( option == 3 ) {
            // Venus
            centralBody = 3;
            clearScreen = true;
            dt = 32.0;
            screenXYscale = 200000.0 * 10.0 / 1E11;
        } else if ( option == 4 ) {
            // Earth Falling Tower
            centralBody = 4;
            clearScreen = true;
            dt = 16;
            screenXYscale = 200000.0 * 10.0 / 1E11;
        } else if ( option == 5 ) {
            // Mars
            centralBody = 5;
            clearScreen = true;
            dt = 32.0;
            screenXYscale = 400000.0 * 10.0 / 1E11;
        } else if ( option == 6 ) {
            // Jupiter
            centralBody = 6;
            clearScreen = true;
            dt = 32.0;
            screenXYscale = 20000.0 * 10.0 / 1E11;
        } else if ( option == 7 ) {
            // Saturn
            centralBody = 7;
            clearScreen = true;
            dt = 32.0;
            screenXYscale = 20000.0 * 10.0 / 1E11;
        } else if ( option == 8 ) {
            // Uranus
            centralBody = 8;
            clearScreen = true;
            dt = 32.0;
            screenXYscale = 30000.0 * 10.0 / 1E11;
        } else if ( option == 9 ) {
            // Neptune
            centralBody = 9;
            clearScreen = true;
            dt = 32.0;
            screenXYscale = 30000.0 * 10.0 / 1E11;
        } else if ( option == 10 ) {
            // Earth Falling Tower surface reference frame view
            // body radii not scaled properly
            centralBody = 4;
            clearScreen = true;
            dt = 32.0;
            screenXYscale = 200000.0 * 10.0 / 1E11;
        } else if ( option == 11 ) {
            // sun
            centralBody = 1;
            clearScreen = true;
            dt = 512.0;
            screenXYscale = 1000.0 * 10.0 / 1E11;
        } else if ( option == 12 ) {
            // Moon (in order to view Apollo11 S-IVB close approach)
            centralBody = 10;
            clearScreen = false;
            dt = 128.0;
            screenXYscale = 200000.0 * 10.0 / 1E11;
        }
    }

    // MouseListener implementation
    @Override
    public void mouseClicked( MouseEvent e ) {
    }

    @Override
    public void mousePressed( MouseEvent e ) {
    }

    @Override
    public void mouseReleased( MouseEvent e ) {
        go = true;
    }

    @Override
    public void mouseEntered( MouseEvent e ) {
        go = false;
    }

    @Override
    public void mouseExited( MouseEvent e ) {
        go = true;
    }

    // WindowListener implementation
    @Override
    public void windowClosing( WindowEvent e ) {
        dispose();
        System.exit( 0 ); // Normal exit of program
    }

    @Override
    public void windowOpened( WindowEvent e ) {
    }

    @Override
    public void windowClosed( WindowEvent e ) {
    }

    @Override
    public void windowIconified( WindowEvent e ) {
    }

    @Override
    public void windowDeiconified( WindowEvent e ) {
    }

    @Override
    public void windowActivated( WindowEvent e ) {
    }

    @Override
    public void windowDeactivated( WindowEvent e ) {
    }

    public static void main( String[] args ) {
        Dynamics d = new Dynamics();

        d.start();
    }
}
