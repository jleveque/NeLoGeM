/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamics;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;

/**
 *
 * @author CFD
 */
public class Mathut {

// General purpose static mathematical utilities (e.g. point transformations)
// Mostly copied from Orbit2014 Body class


    public static double D2R = Math.PI / 180.0;
    public static double degreesToRadians = D2R;
    public static double radiansToDegrees = 1 / D2R;
    public static double degreesToHours = 1/15.0;

    // (June 2013)
    public static double degreesToRadians( double degree ) {
        return( degree * degreesToRadians );
    }

    // (June 2013)
    public static double radiansToDegrees( double radians ) {
        return( radians / degreesToRadians );
    }

    // correct azimuths in range -90 to + 270
    public static double normaliseAzimuth( double degrees ) {
        if ( degrees < 0 ) degrees += 360.0;
        return( degrees);
    }

    // (June 2013)
    public static double normaliseLongitude( double degrees ) {
        if ( degrees < -180.0 ) {
            degrees = 360.0 + degrees;
        } else if ( degrees > 180.0 ) {
            degrees = degrees - 360.0;
        }
        return( degrees );
    }


    // https://www.mkyong.com/java/how-to-write-an-image-to-file-imageio/
    // http://www.java2s.com/Tutorial/Java/0261__2D-Graphics/CreatingaBufferedImagefromanImageobject.htm
    public static void saveImage( Image img, String name, Dynamics thisApplet ) {
        BufferedImage bufferedImage = new BufferedImage( img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.createGraphics();
        g.drawImage( img, 0, 0, thisApplet );
//      g.drawImage( img, null, null );
//      waitForImage(bufferedImage);

        try {
            // \ is an escape character, so \\ is equivalent to \
            ImageIO.write( bufferedImage, "jpg", new File("C:\\images\\" + name + ".jpg"));
//          ImageIO.write(image, "gif",new File("C:\\out.gif"));
//          ImageIO.write(image, "png",new File("C:\\out.png"));

        } catch (IOException e) {
        	e.printStackTrace();
        }
        System.out.println("Image saved to C:\\images\\" + name + ".jpg" );
    }

    public static void printDate() {
           System.out.println(  new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()));
    }

    // return index of smallest number of two
    public static int indexOfSmallest( double a, int ia, double b, int ib ) {
        int indexOfSmallest = ia;
        if ( Math.abs(b) < Math.abs(a) ) indexOfSmallest = ib;
        return( indexOfSmallest );
    }

    // return index ia or ib of largest number of two numbers a and b
    public static int indexOfLargest( double a, int ia, double b, int ib ) {
        int indexOfLargest = ia;
        if ( Math.abs(b) > Math.abs(a) ) indexOfLargest = ib;
        return( indexOfLargest );
    }


    // find distance between a body and a statevector
    public static double distanceBetween( Body b1, StateVector s ) {
        double lx = b1.currentState.x - s.x;
        double ly = b1.currentState.y - s.y;
        double lz = b1.currentState.z - s.z;

        double ld = Math.sqrt(lx * lx + ly * ly + lz * lz);
        return (ld);
    }

        // using barycentricXYZ, move StateVector s to be on surface of body b1
    public static StateVector makeRadiallyDistant( StateVector b1State, double b1Radius, StateVector s ) {
        double lx = b1State.x - s.x;
        double ly = b1State.y - s.y;
        double lz = b1State.z - s.z;
        double ld = Math.sqrt(lx * lx + ly * ly + lz * lz);

        System.out.println("Correction = " + b1Radius + " / " + ld );
        double correction = b1Radius / ld;
        s.x = correction * Math.abs(lx) + b1State.x;
        s.y = correction * Math.abs(ly) + b1State.y;
        s.z = correction * Math.abs(lz) + b1State.z;

        return( s );
    }


    // using barycentricXYZ, move StateVector s to be on surface of body b1 (NOT WORKING)
    public static StateVector makeRadiallyDistant( Body b1, StateVector s ) {
        double lx = ( b1.currentState.x - s.x );
        double ly = ( b1.currentState.y - s.y );
        double lz = ( b1.currentState.z - s.z );
        double ld = Math.sqrt(lx * lx + ly * ly + lz * lz);

        double correction = b1.r / ld;
        s.x = correction * lx + b1.currentState.x;
        s.y = correction * ly + b1.currentState.y;
        s.z = correction * lz + b1.currentState.z;

        return( s );
    }

       // make sure angle lies between 0 and 2.PI
    public static double normaliseRadianAngle( double a ) {
        double normalisedAngle = a;
        if ( a < 0 ) normalisedAngle = normalisedAngle + 2.0 * Math.PI;
        return( normalisedAngle );
    }

    // make sure angle lies between 0 and 360 degrees
    public static double normaliseDegreeAngle( double a ) {
        double normalisedAngle = a;
        if ( a < 0 ) normalisedAngle = normalisedAngle + 360.0;
        return( normalisedAngle );
    }

    // x transformation to screen coordinates
    public static int x_t( Dynamics ap, double x ) {
        return (int) ( ap.screenXYscale * x + ap.screenXoffset);
    }

    // y transformation to screen coordinates
    public static int y_t( Dynamics ap, double y ) {
        return (int) (ap.screenXYscale * -y + ap.screenYoffset);
    }

    // length transformation to screen pixels
    public static int l_t( Dynamics ap, double l ) {
        return (int) ( ap.screenXYscale * l);
    }

    // transform a point around the x-axis through xangle (june 2013)
    public static StateVector transformAroundXaxis( StateVector m,  double xangle ) {
        StateVector mt = new StateVector();
        mt.x = m.x;
        mt.y = m.y * Math.cos(xangle) - m.z * Math.sin(xangle);
        mt.z = m.z * Math.cos(xangle) + m.y * Math.sin(xangle);
        mt.vx = m.vx;
        mt.vy = m.vy * Math.cos(xangle) - m.vz * Math.sin(xangle);
        mt.vz = m.vz * Math.cos(xangle) + m.vy * Math.sin(xangle);
        return ( mt );
    }

    // transform a point around the y-axis through xangle (june 2013)
    public static StateVector transformAroundYaxis( StateVector m, double xangle ) {
        StateVector mt = new StateVector();
        mt.y = m.y;
        mt.x = m.x * Math.cos(xangle) - m.z * Math.sin(xangle);
        mt.z = m.z * Math.cos(xangle) + m.x * Math.sin(xangle);
        mt.vy = m.vy;
        mt.vx = m.vx * Math.cos(xangle) - m.vz * Math.sin(xangle);
        mt.vz = m.vz * Math.cos(xangle) + m.vx * Math.sin(xangle);
        return ( mt );
    }

    // transform a point around the z-axis through xangle (june 2013)
    public static StateVector transformAroundZaxis( StateVector m, double xangle ) {
        StateVector mt = new StateVector();
        mt.z = m.z;
        mt.y = m.y * Math.cos(xangle) + m.x * Math.sin(xangle);
        mt.x = m.x * Math.cos(xangle) - m.y * Math.sin(xangle);
        mt.vz = m.vz;
        mt.vy = m.vy * Math.cos(xangle) + m.vx * Math.sin(xangle);
        mt.vx = m.vx * Math.cos(xangle) - m.vy * Math.sin(xangle);
        return ( mt );
    }

    // 06 aug 2016
    public static StateVector tilt( StateVector v, Body b0, double signrotate )  {
//      double ob = b0.obliquity * Mathut.degreesToRadians * signrotate;
//      v = Mathut.transformAroundXaxis( v, ob );
        v = Mathut.transformAroundXaxis( v,  -b0.axial[5] * signrotate );
        v = Mathut.transformAroundYaxis( v,  -b0.axial[6] * signrotate );
        return( v );
    }


    // find the VX velocity (m/s) of a surface body at longitude = 0 Greenwich meridian, latitude = latitude
    public static double rotationXvelocity( Body b0, double rmul, double latitude ) {
        double r = b0.r * rmul * Math.cos( latitude );
        double v = r * 2.0 * Math.PI / b0.siderealDay;
        return( v );
    }

    // find angle p1-p2-p3 in degrees (fixed aug 2016)
    public static double angleBetween3Points( StateVector vp1, StateVector vp2, StateVector vp3 ) {
        double d, angle = 0;
        StateVector p1 = new StateVector( vp1 );
        StateVector p2 = new StateVector( vp2 );
        StateVector p3 = new StateVector( vp3 );

        // set p2 as origin
        p1.subtractStateVector( p2 );
        p3.subtractStateVector( p2 );

        // transform line p2-p1 onto x axis (and transform p3 at the same time
        angle = Math.atan2( p1.z, p1.x );
        p1 = Mathut.transformAroundYaxis( p1, -angle );
        p3 = Mathut.transformAroundYaxis( p3, -angle );
        d = Math.sqrt( p1.x*p1.x + p1.z*p1.z );
        angle = Math.atan2( p1.y, d );
        p1 = Mathut.transformAroundZaxis( p1, -angle );
        p3 = Mathut.transformAroundZaxis( p3, -angle );

        // transform line p2-p3 onto XY plane
        angle = Math.atan2( p3.z, p3.y );
        p3 = Mathut.transformAroundXaxis( p3, -angle );

        // find the angle p1-p2-p3
        angle = Mathut.radiansToDegrees( Math.atan2( p3.y, p3.x ) );

        return( angle );
    }

    // 31 July 2016
    // returns RA (hrs) in m.longitude, Dec (degrees) in m.latitude
    public static StateVector barycentricXYZ_to_RADec( Body b0, StateVector m ) {
        m = Mathut.barycentricXYZ_to_relativeXYZ( b0, m );
        m = Mathut.tilt( m, b0, -1.0 );
        m = Mathut.relativeXYZ_to_latLong( m );                                 // convert x,y,z to barycentric/ecliptic lat and long
        m.latitude = m.latitude * Mathut.radiansToDegrees;
        m.longitude = m.longitude * Mathut.radiansToDegrees;
        if ( m.longitude < 0 ) m.longitude = 180.0 + ( 180 + m.longitude );     // longitude has negative values
        m.longitude = m.longitude / 15.0;                                       // convert degrees to hours
        return( m );
    }



    // right ascension and declination to ecliptic x,y,z (Sep 2013)
    public static StateVector raDec_to_relativeXYZ( double rasc, double decl, double distance ) {
        double lx, ly, lz, ll, lr, x1, y1, z1, xangle;
        double latitude, longitude, fd, vx, vy, vz, v;

        // convert ra and dec to longitude and latitude
        latitude = decl;
        longitude = rasc * 15.0;
        if (longitude > 180.0) longitude = longitude - 360.0;
        latitude = latitude * Mathut.degreesToRadians;
        longitude = longitude * Mathut.degreesToRadians;

        // find relative lx, ly, lz location from ecliptic 0,0,0
        // using a very long radius
        lr = Math.abs( distance * Math.cos(latitude) );

        // rotate point through latitude and longitude
        lz = lr * Math.tan(latitude);
        ly = lr * Math.sin(longitude);
        lx = lr * Math.cos(longitude);

        // vector contains terrestrial x,y,z
        StateVector m = new StateVector( lx, ly, lz, 0, 0, 0);

        return ( m );
    }

    // (jan 2015)
    // modified dec 2014 to change 'this.x' to 'v.x'
    public static StateVector relativeXYZ_to_barycentricXYZ( Body b0, StateVector s ) {
        s.x += b0.currentState.x;
        s.y += b0.currentState.y;
        s.z += b0.currentState.z;
        s.vx += b0.currentState.vx;
        s.vy += b0.currentState.vy;
        s.vz += b0.currentState.vz;
        return( s );
    }

    public static StateVector degreesLatLong_to_barycentricXYZ( Body b0, StateVector m, double fd  ) {
        StateVector v = new StateVector();
        v = degreesLatLong_to_relativeXYZ( b0, m, fd );
        v = relativeXYZ_to_barycentricXYZ( b0, v );
        return( v );
    }


    // converts degrees alt-long-radius on body b0 to relativeXYZ
    public static StateVector degreesLatLong_to_relativeXYZ( Body b0, StateVector m, double fd  ) {

        double longitude = ( +m.longitude + fd * 360.0 ) * Mathut.degreesToRadians;
        StateVector v = new StateVector();

        // find x and z locations at y=0 greenwich meridian
        v.x  = b0.r * m.length * Math.cos( m.latitude * Mathut.degreesToRadians );
        v.y  = 0;
        v.z  = b0.r * m.length * Math.sin( m.latitude * Mathut.degreesToRadians );
        v.vx = 0;
        v.vy = rotationXvelocity( b0, m.length, m.latitude * Mathut.degreesToRadians );
        v.vz = 0;

        // spin round z axis to find x location
        v = Mathut.transformAroundZaxis( v, longitude );

        double signrotate = +1;
        v = Mathut.tilt( v, b0, signrotate );

        return( v );
    }

    // converts XYZ axes at latitude 0, longitude 0 to lat-long-radius on body b0 to relativeXYZ
    // axes[0] is origin, axes[1] = x axis pointing Eeast, axes[2] = y axis pointing North, axes[3] = z axis pointing up
    public static StateVector[] axesLatLong_to_relativeXYZ( Body b0, double lat, double lon, StateVector axes[], double fd  ) {
        int n = 0;
        double signrotate;
        double longitude = ( +lon + fd * 360.0 ) * degreesToRadians;   // fixed for correct longitude sign
        StateVector v = new StateVector();

        for ( n=0; n<4; n++ ) {
            // transform XYZ axes to lat and lon
            axes[n].x += b0.r;
            axes[n]  = Mathut.transformAroundYaxis( axes[n], lat * degreesToRadians );
            axes[n]  = Mathut.transformAroundZaxis( axes[n], lon * degreesToRadians );

            // add appropriate body obliquity
            signrotate = +1;
            axes[n] = Mathut.tilt( axes[n], b0, signrotate );
        }

        return( axes );
    }

    public static StateVector barycentricXYZ_to_degreesLatLongRadius( Body b0, StateVector m ) {
        StateVector mcdegrees = new StateVector();
        m = barycentricXYZ_to_latLong( b0, m );
        mcdegrees.latitude = radiansToDegrees( m.latitude );
        mcdegrees.longitude = radiansToDegrees( m.longitude );
        mcdegrees.longitude = normaliseLongitude( mcdegrees.longitude );
        mcdegrees.length = m.length / b0.r;
        return( mcdegrees );
    }

    // Find the  latitude and longitude (measured in radians) of barycentric object m on surface of body b0
    public static StateVector barycentricXYZ_to_latLong( Body b0, StateVector m ) {
        m = barycentricXYZ_to_relativeXYZ( b0, m );
        m = Mathut.tilt( m, b0, -1.0 );

        m = relativeXYZ_to_latLong( m );
        double fd = b0.siderealClock / b0.siderealDay;
        m.longitude = ( m.longitude - fd * 2.0 * Math.PI );
        return( m );
    }

    // (jan 2015)
    // modified dec 2014 to change 'this.x' to 'v.x'
    public static StateVector barycentricXYZ_to_relativeXYZ( Body b0, StateVector m  ) {
        m.x -= b0.currentState.x;
        m.y -= b0.currentState.y;
        m.z -= b0.currentState.z;
        m.vx -= b0.currentState.vx;
        m.vy -= b0.currentState.vy;
        m.vz -= b0.currentState.vz;
        return( m );
    }

   // (jan 2015)
    // returns values in radians
    public static StateVector relativeXYZ_to_latLong( StateVector m ) {
        // find host (usually terrestrial) latitude and longitude
        m.length = Math.sqrt(m.x * m.x + m.y * m.y + m.z * m.z);
        double ll = Math.sqrt(m.x * m.x + m.y * m.y);
        m.latitude = Math.atan2(m.z, ll);
        m.longitude = Math.atan2(m.y, m.x);
        return( m );
    }

    // rotation around z axis for time of day, and
    // rotation around x and y axis using planet fixed north poles (so no need to find sines and cosines each time)
    public static StateVector staticXYZ_to_relativeXYZ( double rotationAngle, Body b0, StateVector m  ) {
        StateVector rotm = new StateVector();
        double signrotate = 1.0;

        rotm = Mathut.transformAroundZaxis( m, rotationAngle );
        rotm = Mathut.tilt( rotm, b0, signrotate );                           // signrotate was -1.0

        return ( rotm );
    }


    public static double phaseAngle( StateVector s ) {
        double phaseAngle = Math.atan2( s.y, s.x );
        return( phaseAngle );
    }

    public static double oblateRadius( double equatorialRadius, double polarRadius, double latitude ) {
        double a, b, theta, fn;
        double rb, lx, ly;

        // m.latitude is actually the "eccentric anomaly"
        // and I'm hoping that since the Earth is nearly spherical, values won't
        // be far off real values.
        a = equatorialRadius;
        b = polarRadius;
        theta = latitude;
        fn = Math.sqrt( b*b + a*a * Math.tan(theta)*Math.tan(theta) );
        lx = a * b / fn;
        ly = a * b * Math.tan(theta)/ fn;
        rb = Math.sqrt( lx*lx + ly*ly );

        return( rb );
    }
}
