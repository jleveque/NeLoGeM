/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamics;

/**
 *
 * @author CFD
 */
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.Math;
import java.text.SimpleDateFormat;
import javax.imageio.ImageIO;
import java.io.FileReader;


public class Dynamics extends java.applet.Applet implements Runnable {
    int option =  11;
    Dimension appletSize;
    private Thread testThread = null;
    int xmax, ymax, count;
    Image offImage;
    Graphics offGraphics;
    boolean go = true;
    boolean clearScreen = false;
    int centralBody =   4;
    double screenXoffset, screenYoffset, screenXYscale;
    double dt =  16.0;
    double G = 6.671984315419034E-11;                                           // gravitational constant
    boolean localGravity = false;
//  SolarSystem ss;
    SolarSystemApollo11 ss;
    Tetrahedron tetra;
    Process proc;
//  FoucaultPendulum fp1;
    FallingTower ft1;

    double daySecs = 24.0 * 60.0 * 60.0;     // seconds per day
    double elapsedTime = 64800.0 - 197.44;   // elapsed time since simulation start (18hours added to turn Greenwich towards sun on 21 dec 2012)
                                             // correction 0f 198.2 seconds to match Betelgeuse altitude and azimuth (Feb 2015)
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


    public void init() {
        go = true;
        this.enableEvents( AWTEvent.MOUSE_EVENT_MASK );

        setSize(400, 400);
        setBackground(Color.white);

        appletSize = this.getSize();
        xmax = appletSize.width;         // graphics width
        ymax = appletSize.height;        // graphics height

        screenXYscale =    4500.0 * 10.0/1E11;
//      screenXYscale = 1.0 * 10.0/1E11;
        screenXoffset = xmax / 2.0;
        screenYoffset = ymax / 2.0;


        offImage = createImage( xmax, ymax );
        offGraphics = offImage.getGraphics();

        selectOption();

//      ss = new SolarSystem( this );
        ss = new SolarSystemApollo11( this );
        tetra = new Tetrahedron( this );

        ss.setAxialRotationParams();
        ss.createMaps();
        ss.setAllSiderealClocks( elapsedTime );

    }

    public void start() {
        if (testThread == null) {
            testThread = new Thread(this, "Test1");
            testThread.start();
        }
    }

    public void processMouseEvent( MouseEvent e) {

        if ( e.getID() == MouseEvent.MOUSE_ENTERED ) { go =  false; }
        else if ( e.getID() == MouseEvent.MOUSE_EXITED ) { go =  true; }
        else if ( e.getID() == MouseEvent.MOUSE_RELEASED ) {
            go =  true;
        }
        else super.processMouseEvent(e);
    }


    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        Thread myThread = Thread.currentThread();
        while (testThread == myThread) {
            if ( go ) {

                if ( count == 5 ) {
                    ft1 = new FallingTower( this, 4, 32.543618, 44.42408, 1.0, 100000.0 );   // Babylon
                }
                if ( count > 5 ) {
                    ft1.moveReferenceFrame();
                    if ( count%100 == 0 ) {
                        if ( option == 4 || option == 10 && ft1.index > 0 ) {
                            ft1.index--;
                            ft1.flagUnfixed(ft1.currentState[ ft1.index ]);
                        }
                    }
                }

                ss.moveEuler( dt );
                tetra.moveEuler( dt );

                currentDate = ( advanceCalendar( dt ) );
                ss.setAllSiderealClocks( elapsedTime );

                repaint();
                count++;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e){ }
        }
    }

    public void paint(Graphics g) {
        update(g);
    }

    public void update(Graphics g) {

        offGraphics.setPaintMode();
        if ( clearScreen ) {
            offGraphics.setColor( Color.white );
            offGraphics.fillRect( 0, 0, xmax, ymax );
        }

        offGraphics.setColor( Color.black );

        if ( option != 10 ) {
            ss.paint(g);
            tetra.paint(g);
        } else {
            if ( count > 6 ) {
                ft1.paint(g);
            }
        }

        offGraphics.setColor( Color.black );
        offGraphics.drawString( currentDate, 5, ymax-10);

        g.drawImage( offImage, 0, 0, this );
    }

    public void stop() {
        testThread = null;
    }

    void setCalendar( double jd ) {
        double advanceTime;

         // only update calendar once
         // by adding the difference between current JDCT (from csv file) and base Julian Days.
         if (currentJDCT == 0.0) {
             initialJDCT = jd;
             currentJDCT = baseJDCT;
             advanceTime = jd - baseJDCT;
             advanceCalendar(daySecs * advanceTime);

             setDateByJulianDay( currentJDCT );
         }
    }

    // advance calendar using Julian Day
    String advanceCalendar(double dt) {
        String sdate;
        double secondsInDay = 24 * 60 * 60;

        // advance elapsed time and current Julian Date
        elapsedTime += dt;
        currentJDCT += dt / secondsInDay;

        // update sidereal clock
        siderealClock = elapsedTime % SiderealDay;

        // get new date and time
        sdate = setDateByJulianDay( currentJDCT );
        sdate += " dt=" + (float) dt + " s";

        return( sdate );
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
          w = (int)( (z - 1867216.25)/36524.25 );
          x = (int)( w/4 );
          a = (int)z+1+w-x;
          b = (int)a+1524;
          c = (int)( (b-122.1)/365.25 );
          d = (int)( 365.25*c );
          e = (int)( (b-d)/30.6001 );
          f = (int)( 30.6001*e );
          currentDay = (int)(b-d-f);
          currentMonth = (int)(e-1);
          if ( currentMonth > 12) currentMonth = (int)( e-13 );   //(must get number less than or equal to 12)
          if ( currentMonth <= 2) {
              currentYear = (int)(c-4715 );  // (if Month is January or February) or C-4716 (otherwise)
          } else {
              currentYear = (int)(c-4716);
          }

          // Julian days seem to start at noon.
          currentSecond = ( julianDay - (int)julianDay ) * secondsInDay;
          currentSecond -= secondsInDay * 0.5;
          if (currentSecond < 0.0 ) currentSecond = 0.5 * secondsInDay + ( 0.5 * secondsInDay + currentSecond );
          nsecond = currentSecond;
          nhour = (int) (nsecond / secondsInHour);
          nsecond = nsecond % secondsInHour;
          nminute = (int) (nsecond / secondsInMinute);
          nsecond = nsecond % secondsInMinute;

          // put into string format
          sdate = "" + prefix(currentDay) + " "
                     + monthsOfYear[ (int) currentMonth] + " "
                     + (int) currentYear + " "
                     + prefix(nhour) + ":"
                     + prefix(nminute) + ":"
                     + prefix(nsecond);

          return( sdate );
    }

    // prefix with '0' if necessary
    String prefix(double number) {
        String s;
        if (number < 10.0) {
            s = "0" + (int) number;
        } else {
            s = "" + (int) number;
        }
        return (s);
    }

    void selectOption() {
        if ( option == 1 ) {
            // Apollo 11 trajectory
            centralBody =   4;
            clearScreen = false;
            dt =  16.0;
            screenXYscale =    4500.0 * 10.0/1E11;
        } else if ( option == 2 ) {
            // Sun and planet orbits
            centralBody =   1;
            clearScreen = false;
            dt = 2048.0;
            screenXYscale =    1.0 * 10.0/1E11;
        } else if ( option == 3 ) {
            // Venus
            centralBody =   3;
            clearScreen = true;
            dt = 32.0;
            screenXYscale =    200000.0 * 10.0/1E11;
        } else if ( option == 4 ) {
            // Earth Falling Tower
            centralBody =   4;
            clearScreen = true;
            dt = 32.0;
            screenXYscale =    200000.0 * 10.0/1E11;
        } else if ( option == 5 ) {
            // Mars
            centralBody =   5;
            clearScreen = true;
            dt = 32.0;
            screenXYscale =    400000.0 * 10.0/1E11;
        } else if ( option == 6 ) {
            // Jupiter
            centralBody =   6;
            clearScreen = true;
            dt = 32.0;
            screenXYscale =    20000.0 * 10.0/1E11;
        } else if ( option == 7 ) {
            // Saturn
            centralBody =   7;
            clearScreen = true;
            dt = 32.0;
            screenXYscale =    20000.0 * 10.0/1E11;
        } else if ( option == 8 ) {
            // Uranus
            centralBody =   8;
            clearScreen = true;
            dt = 32.0;
            screenXYscale =    30000.0 * 10.0/1E11;
        } else if ( option == 9 ) {
            // Neptune
            centralBody =   9;
            clearScreen = true;
            dt = 32.0;
            screenXYscale =    30000.0 * 10.0/1E11;
        } else if ( option == 10 ) {
            // Earth Falling Tower surface reference frame view
            // body radii not scaled properly
            centralBody =   4;
            clearScreen = true;
            dt = 32.0;
            screenXYscale =    200000.0 * 10.0/1E11;
        } else if ( option == 11 ) {
            // sun
            centralBody =   1;
            clearScreen = true;
            dt = 512.0;
            screenXYscale =    1000.0 * 10.0/1E11;
        }
    }
}
