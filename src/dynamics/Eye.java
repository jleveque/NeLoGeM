/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamics;

import java.awt.*;
import java.awt.Graphics;

/**
 *
 * @author CFD
 * Copied and adapted from Orbit3D-03nov2017.java
 * 1. Body numbers replaced with Bodies, because no Pool class.
 * 2. MapCoord() replaced by StateVector()
 * 3. Surface body locations disallowed
 * 4. No stars/constellations
 * 5. view, yearRotate, and startJDCT variables added to Dynamics.java.
 * 6. additional new files include:
 *      PaintStroke.java
 *      DispersedPolygon.java
 *      VecFont.java
 */
public class Eye {

    //                                                screen
    //     eye                                          |
    //   (subject)------>-------(object)--------------->|
    //                                                  |
    Dynamics app;
    Image eyeImage;
    Graphics eyeGraphics;
    int num;
    String name;
    int typeOfEye;               // type of eye: 0 pinhole, 1 fisheye
    int absRel;
    StateVector locus;      // eye subject position x,y,z, eye object gaze vx, vy, vz
    StateVector dimension;  // eye internal dimensions (x-y) is screen/retina, z distance from lens to screen/retina
    StateVector udjat;      // under-eye vector used for view z-rotation
//  double ZrotationAngle;  // angle to rotate around eye z axis
//  int bnumSubject;        // if < 0, use existing eye locus x,y,z, else body barycentric z,y,z
//  int bnumObject;         // if < 0, use existing eye locus vx,vy,vz else body barycentric x,y,z
    Body bSubject;
    Body bObject;
//  MapCoord subjectMapLocation;    // if bnumsubject = bnumobject, use subject and object map locations
//  MapCoord objectMapLocation;     // ...to set locus x,y,z and vx,vy,vz
    StateVector eyePlanetOffset;        // eye at origin planet map offset (NOT IN USE)
    StateVector eyeBarycentreOffset;    // eye at origin bodies offset     (NOT IN USE)
    final int istar = 1000000;
    double cursorClosestDistance;   // closest cursor distance to point
    int cursorClosestNum;           // closest body number to cursor
    int cursorx;
    int cursory;
    int npaintstrokes = 0;
    int relBody = 4;        // Earth            relative view settings
    int relView = 1;        // forward view
    int relAbs = 1;         // absolute or motion-relative (1)
    int relSign = 1;        // from Earth
    int relSubj;            // absolute eye subject to restore
    int relObj;             // absolute eye obbject to restore
    StateVector relLocus;   // absolute eye locus to restore
    PaintStroke nearPaintStroke;
    PaintStroke farPaintStroke;
    DispersedPolygon paintedPolygon;
    DispersedPolygon nearPolygon;
    DispersedPolygon farPolygon;
    int uniqueDispersedPolygonID = 0;
    boolean polygonPaintingInProcess;

    // Default constructor
    public Eye() {
        bSubject = null;    // no assigned subject body
        bObject = null;     // no assigned object body

        // create empty linked list of paintstrokes
        nearPaintStroke = new PaintStroke( -1, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 );
        farPaintStroke = new PaintStroke( (int)1E20, nearPaintStroke, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 );
        paintedPolygon =  new DispersedPolygon( 2, 64 );

        // create empty linked list of dispersed polygons
        nearPolygon = new DispersedPolygon( -1, null, nextUniqueDispersedPolygonID(), 0 );
        farPolygon = new DispersedPolygon( 1E20, nearPolygon, nextUniqueDispersedPolygonID(), 0 );
        polygonPaintingInProcess = false;
    }

    public Eye( Dynamics app, double x, double y, double z, double vx, double vy, double vz, double sd  ) {
        this(); // Call default constructor

        eyeImage = app.createImage( app.canvasSize.width, app.canvasSize.height );
        eyeGraphics = eyeImage.getGraphics();

        num = 233;
        this.app = app;
        locus = new StateVector( x, y, z, vx, vy, vz );
        udjat = new StateVector( 0, 0, -1E13, 0, 0, 0 );
//      System.out.println( "eye locus " + locus.x + " " + + locus.y + " " + + locus.z + " " +" v " + locus.vx + " " + + locus.vy + " " + + locus.vz + " "  );
        printEyeLocus();

//      screen = new StateVector();
//      screen.z = sd;
//        screen.z = 1E12;

        setEyeDimensions();
        typeOfEye = 0;

        eyePlanetOffset = new StateVector();
        eyeBarycentreOffset = new StateVector();

        absRel = 0;
        relLocus = new StateVector( locus.x, locus.y, locus.z, locus.vx, locus.vy, locus.vz );
    }

    public Eye( Dynamics app, Body sbody, Body obody, double sd ) {
        this(); // Call default constructor

        this.app = app;
        bSubject = sbody;
        bObject = obody;

        locus = new StateVector( sbody.currentState.x, sbody.currentState.y, sbody.currentState.z, sbody.currentState.vx, sbody.currentState.vy, sbody.currentState.vz );
        udjat = new StateVector( 0, 0, -1E13, 0, 0, 0 );

        setEyeDimensions();
        typeOfEye = 0;

        eyePlanetOffset = new StateVector();
        eyeBarycentreOffset = new StateVector();

        absRel = 0;  // absolute view = 0, relative view = 1
        relLocus = new StateVector( locus.x, locus.y, locus.z, locus.vx, locus.vy, locus.vz );
    }

/*
    // eye is a map point on surface of a body, looking in some altitude and azimuth direction.
    public Eye( Dynamics app, int parent, double latitude, double longitude, double altitude, double azimuth ) {
        double x, y, z, l, r, al, az, lat, lon;
        int width, height;
        StateVector v = new StateVector();

        width = app.canvasSize.width;
        height = app.canvasSize.height;
        eyeImage = app.createImage( width, height);
        eyeGraphics = eyeImage.getGraphics();
        this.num = 233;
        this.app = app;
        bnumSubject = -parent;                       // no assigned subject body
        bnumObject = -parent;                        // no assigned object body
        locus = new StateVector( 0, 0, 0, 0, 0, 0 );
        subjectMapLocation = new MapCoord();
        objectMapLocation = new MapCoord();

        subjectMapLocation.latitude = latitude;
        subjectMapLocation.longitude = longitude;
        subjectMapLocation.radius = app.pool.b[parent].r;
        subjectMapLocation.parent = parent;

        udjat = new StateVector( 0, 0, -1E13, 0, 0, 0 );

        if ( app.pool.b[parent].map.mapExists ) {
            objectMapLocation = altAz_to_LatLongR( subjectMapLocation, app.pool.b[parent].r, altitude, azimuth );

            // get subject barycentricXYZ
            locus = degreesLatLongR_to_barycentricXYZ( app.pool.b[parent], subjectMapLocation );

            // get object barycentricXYZ in locus vXYZ
            v = degreesLatLongR_to_barycentricXYZ( app.pool.b[parent], objectMapLocation );
            locus.vx = v.x;
            locus.vy = v.y;
            locus.vz = v.z;
//          ZrotationAngle = 0;

            System.out.println( "Eye Subject barycentric XYZ " + locus.x + " " + locus.y + " " + locus.z );
            System.out.println( "Eye Object barycentric XYZ " + locus.vx + " " + locus.vy + " " + locus.vz );

//
            setEyeDimensions();

        } else {
            bnumObject = parent+1;
        }

        eyePlanetOffset = new StateVector();
        eyeBarycentreOffset = new StateVector();

        absRel = 0;  // absolute view = 0, relative view = 1
        relLocus = new StateVector( locus.x, locus.y, locus.z, locus.vx, locus.vy, locus.vz );

        // create empty linked list
        nearPaintStroke = new PaintStroke( -1, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 );
        farPaintStroke = new PaintStroke( (int)1E20, nearPaintStroke, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 );
        paintedPolygon =  new DispersedPolygon( 2, 64 );
        // create empty linked list of dispersed polygons
        nearPolygon = new DispersedPolygon( -1, null, nextUniqueDispersedPolygonID(), 0 );
        farPolygon = new DispersedPolygon( 1E20, nearPolygon, nextUniqueDispersedPolygonID(), 0 );
        polygonPaintingInProcess = false;
    }
*/

    int nextUniqueDispersedPolygonID() {
        int id = uniqueDispersedPolygonID;
        // don't allow max int value to be exceeded
        if ( uniqueDispersedPolygonID < Integer.MAX_VALUE ) {
            uniqueDispersedPolygonID++;
        } else {
            uniqueDispersedPolygonID = 2; // 0 = nearPolygon uniqueID, 1 = farPolygon unniqueID, so start at 2
        }
        return( id );
    }


    void printEyeLocus() {
        System.out.println( "eye locus " + locus.x + " " + + locus.y + " " + + locus.z + " " +" v " + locus.vx + " " + + locus.vy + " " + + locus.vz + " udjat " + udjat.x + " " + udjat.y + " " + udjat.z + " zangle " + locus.zangle * Mathut.radiansToDegrees );
    }

    void setEyeDimensions() {
        dimension = new StateVector();
        // eye dimensions.
        // screen retina x,y dimensions from centre.
        // z dimension is distance from pinhole/lens.
//      dimension.x = (double)(app.xmax / 3);
//      dimension.y = (double)(app.ymax / 4);
//      dimension.z = (double)(-app.xmax / 2.0 );
//      dimension.ax = (double)(app.xmax / 2);    // x offset of centre from image top left
//      dimension.ay = (double)(app.ymax / 2);    // y offset
        dimension.x = (double)(app.canvasSize.width / 2.0);
        dimension.y = (double)(app.canvasSize.height / 2.0 );
        dimension.z = (double)(-app.canvasSize.width *   16.0 );
        dimension.ax = (double)(app.canvasSize.width / 2.0);    // x offset of centre from image top left
        dimension.ay = (double)(app.canvasSize.height / 2.0);    // y offset
        System.out.println( "Eye XYZ dimensions" + dimension.x + " " + dimension.y + " " + dimension.z );
    }

/*
    // find one of 6 views away from (+) body b, or towards (-) body b
    StateVector setRelativeEye( Body b, int view, int relAbs, int sign ) {
        StateVector v = new StateVector();
        StateVector s = new StateVector();
        StateVector loc = new StateVector();
        double offset1 = 1E13;
        double vl = Math.sqrt( b.xx*b.xx + b.yy*b.yy + b.zz*b.zz );
        double offset2 = 1E7 / vl;
        double relXYZ[][] = {
            { 0, 0, 0 },
            { +b.xx, +b.yy, +b.zz },    // 1 forward
            { -b.xx, -b.yy, -b.zz },    // 2 backward
            { -b.yy, +b.xx, +b.zz },    // 3 left
            { +b.yy, -b.xx, -b.zz },    // 4 right
            { +b.zz, -b.yy, +b.xx },    // 5 up
            { -b.zz, +b.yy, -b.xx },    // 6 down
            { 0, 0, 0 }
        };
        double absXYZ[][] = {
            { 0, 0, 0 },
            { 0, 0, +1 },    // +Z
            { 0, 0, -1 },    // -Z
            { 0, +1, 0 },    // +Y
            { 0, -1, 0 },    // -Y
            { +1, 0, 0 },    // +X
            { -1, 0, 0 },    // -X
            { 0, 0, 0 }
        };



        if ( relAbs > 0 ) {
            // relative view
            if ( sign > 0 ) {
              v.x = b.x;                        // view from body
              v.y = b.y;
              v.z = b.z;
              v.vx = b.x + relXYZ[view][0] * offset2;
              v.vy = b.y + relXYZ[view][1] * offset2;
              v.vz = b.z + relXYZ[view][2] * offset2;
//            System.out.println( "rel " +relXYZ[view][0] + " " + relXYZ[view][1] + " " + relXYZ[view][2] );
//            System.out.println( "    " + v.vx + " " + v.vy + " " + v.vz );
//            v.printStateVector();
            } else {
              v.vx = b.x;                       // view towards body
              v.vy = b.y;
              v.vz = b.z;
              v.x = b.x + relXYZ[view][0] * offset2;
              v.y = b.y + relXYZ[view][1] * offset2;
              v.z = b.z + relXYZ[view][2] * offset2;
            }
        } else {
            // absolute view down XYZ axes
            if ( sign > 0 ) {
              v.x = b.x;                        // view from body
              v.y = b.y;
              v.z = b.z;
              v.vx = b.x + absXYZ[view][0] * offset1;
              v.vy = b.y + absXYZ[view][1] * offset1;
              v.vz = b.z + absXYZ[view][2] * offset1;
//            System.out.println( "abs " +absXYZ[view][0] + " " + absXYZ[view][1] + " " + absXYZ[view][2] );
//            System.out.println( "    " + v.vx + " " + v.vy + " " + v.vz );
            } else {
              v.vx = b.x;                       // view towards body
              v.vy = b.y;
              v.vz = b.z;
              v.x = b.x + absXYZ[view][0] * offset1;
              v.y = b.y + absXYZ[view][1] * offset1;
              v.z = b.z + absXYZ[view][2] * offset1;
            }
        }
        this.bnumSubject = 0;
        this.bnumObject = 0;
        locus.setStateVector( v.x, v.y, v.z, v.vx, v.vy, v.vz );


        // eye z-rotation to place barycentric -z axis at bottom of view
        udjat.setStateVector( app.pool.b[bnumSubject].x, app.pool.b[bnumSubject].y, app.pool.b[bnumSubject].z - 1E6 );
        loc.setStateVector( locus.x, locus.y, locus.z, locus.vx, locus.vy, locus.vz );
        loc.zangle = 0;
        s = Mathut.transformAroundEye( this.udjat, loc );
        locus.zangle =  Math.PI - Math.atan2( s.x, s.y );
//      System.out.print( " relative body zangle " + locus.zangle );

        return( v );
    }


    StateVector updateEyeAtMapLocation() {
        int parent = subjectMapLocation.parent;
        StateVector v = new StateVector();

        // get subject barycentricXYZ
        locus = degreesLatLongR_to_barycentricXYZ( app.pool.b[parent], subjectMapLocation );

        // get object barycentricXYZ in locus vXYZ
        v = degreesLatLongR_to_barycentricXYZ( app.pool.b[parent], objectMapLocation );

//      System.out.println( "Eye Subject barycentric XYZ " + locus.x + " " + locus.y + " " + locus.z );
//      System.out.println( "Eye Object barycentric XYZ " + locus.vx + " " + locus.vy + " " + locus.vz );
        return( v );
    }

    // convert altitude-azimuth at a location to lat-long of 500 km offset point
    MapCoord altAz_to_LatLongR( MapCoord location, double radius, double altitude, double azimuth ) {
        double x, y, z, l, r, al, az, lat, lon;
        MapCoord obj = new MapCoord();

        // vector at N pole pointing to altitude and azimuth
        r = 500000000.0;                             // 500 km view vector
        al = Mathut.degreesToRadians * altitude;
        az = Mathut.degreesToRadians * azimuth;
        obj.z = r * Math.sin(al);
        l = r * Math.cos(al);
        obj.x = l * Math.sin(az);
        obj.y = l * Math.cos(az);
        // offset from body centre
        obj.z += radius;

        // transform to right latitude and longitude
        lat = Mathut.degreesToRadians * subjectMapLocation.latitude;
        lon = Mathut.degreesToRadians * subjectMapLocation.longitude;
        obj = Mathut.transformAroundXaxis(obj, 0.5* Math.PI - lat);
        obj = Mathut.transformAroundZaxis(obj, lon);
        System.out.println( "v     " + obj.x + " " + obj.y + " " + obj.z );

        // find lat. long, and radius
        l = Math.sqrt(obj.x * obj.x + obj.y * obj.y);
        obj.latitude = Math.atan2(obj.z, l) / Mathut.degreesToRadians;
        obj.longitude = Math.atan2(obj.x,-obj.y) / Mathut.degreesToRadians;
        obj.radius = Math.sqrt( obj.x * obj.x + obj.y * obj.y + obj.z * obj.z );
        System.out.println( "lat/long/radius " + obj.latitude + " " + obj.longitude + " " + obj.radius );

        return( obj );
    }


    // terrestrial latitude and longitude to obliquity-transformed terrestrial x,y,z (Jan 2015)
    public StateVector degreesLatLongR_to_barycentricXYZ( Body b0, MapCoord m  ) {
        double lx, ly, lz, ll, lr, x1, y1, z1, xangle;
        double latitude, longitude, fd, vx, vy, vz;
        StateVector v = new StateVector();

        double lat = m.latitude;
        double lon = m.longitude;

        // rotate longitude part way around spinning planet, and convert to radians.
        // Longitude sign is inverted because the convention used in Earth.map has
        // been that points east are -ve, and points west +ve. It would appear that
        // the opposite convention would have been more appropriate.
        fd = b0.siderealClock / b0.siderealDay;
        longitude = ( +lon + fd * 360.0 ) * Mathut.degreesToRadians;   // fixed for correct longitude sign
        latitude = lat * Mathut.degreesToRadians;

        // find relative lx, ly, lz location from centre of this body
        // m.radius included Mar 2015
        lr = Math.abs( m.radius * Math.cos(latitude) );

        // rotate point through latitude and longitude
        lz = lr * Math.tan(latitude);
        ly = lr * Math.sin(longitude);
        lx = lr * Math.cos(longitude);

        // rotate point around x-axis by angle of obliquity of host.
        xangle = +b0.obliquity * Mathut.degreesToRadians;
        x1 = lx;
        y1 = ly * Math.cos(xangle) - lz * Math.sin(xangle);
        z1 = lz * Math.cos(xangle) + ly * Math.sin(xangle);

        // add body location to give barycentric XYZ
        x1 += b0.x;
        y1 += b0.y;
        z1 += b0.z;

        // vector contains terrestrial x,y,z
        v.setStateVector( x1, y1, z1 );

        return ( v );
    }
*/

    void setEyeSubjectObjectBody( Body bn1, Body bn2 ) {
        this.bSubject = bn1;
        this.bObject = bn2;
        setEyeSubjectBody( bn1 );
        setEyeObjectBody( bn2 );

    }

    // the subject does the looking
    void setEyeSubjectBody( Body bn ) {
        StateVector v = new StateVector();

        this.bSubject = bn;
//      if ( bnumSubject > 0 || bnumSubject < 0 ) {
//          v = lookupPlanetOrStar( bnumSubject );
//          if ( bnumSubject < 0 && v.flag4 > 0 && Math.abs( bnumSubject ) < app.pool.nbodies ) {
//              StateVector s = new StateVector();
//              double fd = app.pool.b[-bnumSubject].siderealClock / app.pool.b[-bnumSubject].SiderealDay;
//              double rotationAngle  = ( fd * 360.0 ) * Mathut.degreesToRadians;   // map longitude rotation angle (radians)
//              double ob = app.pool.b[-bnumSubject].obliquity * Mathut.degreesToRadians;
//              s.setStateVector( app.pool.b[-bnumSubject].map.map[0].x, app.pool.b[-bnumSubject].map.map[0].y, app.pool.b[-bnumSubject].map.map[0].z );
//              s = Mathut.staticXYZ_to_relativeXYZ( rotationAngle, app.pool.b[-bnumSubject], s  );
//              locus.x = app.pool.b[-bnumSubject].x + s.x;
//              locus.y = app.pool.b[-bnumSubject].y + s.y;
//              locus.z = app.pool.b[-bnumSubject].z + s.z;
//              System.out.println("locus   " + locus.x + " " + locus.y + " " + locus.z );
//          } else {
                locus.x = bn.currentState.x;
                locus.y = bn.currentState.y;
                locus.z = bn.currentState.z;
//          }

//      }
    }

    // the object is looked at
    void setEyeObjectBody( Body bn ) {
        StateVector v = new StateVector();

        this.bObject = bn;
//      if ( bnumObject > 0 || bnumObject < 0 ) {
//          v = lookupPlanetOrStar( bnumObject );
//          if ( bnumObject < 0 && v.flag4 > 0 && Math.abs( bnumObject ) < app.pool.nbodies ) {
//              StateVector s = new StateVector();
//              double fd = app.pool.b[-bnumObject].siderealClock / app.pool.b[-bnumObject].SiderealDay;
//              double rotationAngle  = ( fd * 360.0 ) * Mathut.degreesToRadians;   // map longitude rotation angle (radians)
//              double ob = app.pool.b[-bnumObject].obliquity * Mathut.degreesToRadians;
//              s.setStateVector( app.pool.b[-bnumObject].map.map[1].x, app.pool.b[-bnumObject].map.map[1].y, app.pool.b[-bnumObject].map.map[1].z );
//              s = Mathut.staticXYZ_to_relativeXYZ( rotationAngle, app.pool.b[-bnumObject], s  );
//              locus.vx = app.pool.b[-bnumObject].x + s.x;
//              locus.vy = app.pool.b[-bnumObject].y + s.y;
//              locus.vz = app.pool.b[-bnumObject].z + s.z;
//              System.out.println("locus v " + locus.vx + " " + locus.vy + " " + locus.vz );
//          } else {
                locus.vx = bn.currentState.x;
                locus.vy = bn.currentState.y;
                locus.vz = bn.currentState.z;
//          }

//      }
    }

    Body getEyeObjectBody() {
        return( bObject );
    }


    Body getEyeSubjectBody() {
        return( bSubject );
    }

    /*
    void setEye( double x, double y, double z, double vx, double vy, double vz ) {
        this.locus.setStateVector( x, y, z, vx, vy, vz );
        this.bnumObject = 0;
        this.bnumObject = 0;

    }
*/

    // called from run() method to
    void setEye() {
        if ( absRel == 0) {
            setEyeSubjectBody( bSubject );
            setEyeObjectBody( bObject );
            setEyeTransform();
        } else {
//          setRelativeEye( app.pool.b[relBody], relView, relAbs, relSign );
        }

//      System.out.println( "closest = " + cursorClosestNum );
        cursorClosestDistance = 1E20;   // closest cursor distance to point
        cursorClosestNum = -1;           // closest body number to cursor

//  System.out.println("setEye " + bnumSubject + " " + bnumObject );    }
    }

    void setEye( double x, double y, double z, double vx, double vy, double vz ) {
        this.bObject = null;
        this.bSubject = null;
        locus.x = x;    // subject location
        locus.y = y;
        locus.z = z;
        locus.vx = vx;  // object location
        locus.vy = vy;
        locus.vz = vz;
    }

    /*
    // index = 0 no idnex
    // +index for position of body or star (star index starts at istar)
    // -index for position at lat-long-radius on indexed body.
    StateVector lookupPlanetOrStar( int index ) {
        StateVector v = new StateVector();
        int negindex = -index;
        v.flag4 = 1;
        if (index > 0 && index < app.pool.nbodies ) {
            v.x = app.pool.b[index].x;                  // body
            v.y = app.pool.b[index].y;
            v.z = app.pool.b[index].z;
        } else if ( index >= istar  && (index-istar < app.pool.nstars ) ) {
            v.x = app.pool.star[index-istar].x;         // star
            v.y = app.pool.star[index-istar].y;
            v.z = app.pool.star[index-istar].z;
        } else if ( negindex > 0  && negindex < app.pool.nbodies ) {
            if ( app.pool.b[negindex].map.mapExists ) {
                v.x = app.pool.b[negindex].map.vectorMap[0].x;      // map location on body
                v.y = app.pool.b[negindex].map.vectorMap[0].y;
                v.z = app.pool.b[negindex].map.vectorMap[0].z;
                v.vx = app.pool.b[negindex].map.vectorMap[0].vx;
                v.vy = app.pool.b[negindex].map.vectorMap[0].vy;
                v.vz = app.pool.b[negindex].map.vectorMap[0].vz;
            } else {
                v.x = app.pool.b[negindex].x;                       // body location
                v.y = app.pool.b[negindex].y;
                v.z = app.pool.b[negindex].z;
            }
        }  else if ( negindex >= istar  && (negindex-istar < app.pool.nstars ) ) {
            v.x = app.pool.star[negindex-istar].x;
            v.y = app.pool.star[negindex-istar].y;
            v.z = app.pool.star[negindex-istar].z;
        } else {
            System.out.println("planet or star not found");
            v.flag4 = -1;
        }
        return( v );
    }
*/

    // eye.locus.XYZ becomes origin, with view down -z axis
    // Find offsets to add to planets and map coordinates
    // CURRENTLY NOT IN USE (really ??? )
    public void setEyeTransform() {
        double lx, ly, lz, fd, rotationAngle, obliquity;
        StateVector s = new StateVector();
        StateVector loc = new StateVector();

        // find eyeBarycentreOffset to add to all planets
        lx = -this.locus.x;
        ly = -this.locus.y;
        lz = -this.locus.z;
        eyeBarycentreOffset.setStateVector( lx, ly, lz );

        // find eyePlanetOffset to add to all planets and body map points
        // if eye is located on the surface of some body, use body map[0].XYZ as eye location
        eyePlanetOffset.setStateVector( 0, 0, 0 );
//      if ( bnumSubject < 0 && bnumSubject < istar ) {
//          fd = app.pool.b[-bnumSubject].siderealClock / app.pool.b[-bnumSubject].SiderealDay;
//          rotationAngle = ( fd * 360.0 ) * Mathut.degreesToRadians;                           // map longitude rotation angle (radians)
//          obliquity = app.pool.b[-bnumSubject].obliquity * Mathut.degreesToRadians;           // map obliquity rotation angle (radians)
//          s.x = app.pool.b[-bnumSubject].map.map[0].x;
//          s.y = app.pool.b[-bnumSubject].map.map[0].y;
//          s.z = app.pool.b[-bnumSubject].map.map[0].z;
//          s = Mathut.staticXYZ_to_relativeXYZ( rotationAngle, app.pool.b[-bnumSubject], s  );
//          eyePlanetOffset.setStateVector( -s.x, -s.y, -s.z );
//
//          // find eye z-rotation angle needed to keep body centre on -y axis
//          s.setStateVector( app.pool.b[-bnumSubject].x, app.pool.b[-bnumSubject].y, app.pool.b[-bnumSubject].z );
//          locus.zangle = 0;
//          s = Mathut.transformAroundEye( s, locus );
//          locus.zangle =  Math.PI - Math.atan2( s.x, s.y );
//      } else {

            if ( bSubject == null ) {
                if ( app.view == 0  ) {
                    udjat.setStateVector( locus.x, locus.y - 1E6, locus.z );
                } else {
                    udjat.setStateVector( locus.x, locus.y, locus.z - 1E6 );
                }
            }
            if ( bSubject != null ) udjat.setStateVector( bSubject.currentState.x, bSubject.currentState.y, bSubject.currentState.z - 1E6 );
            loc.setStateVector( locus.x, locus.y, locus.z, locus.vx, locus.vy, locus.vz );
            loc.zangle = 0;
            s = Mathut.transformAroundEye( this.udjat, loc );
            locus.zangle =  Math.PI - Math.atan2( s.x, s.y );
//      }


//      System.out.println( "Eye offets: bary " + eyeBarycentreOffset.x + " " + eyeBarycentreOffset.y + " " + eyeBarycentreOffset.z + " planet " + eyePlanetOffset.x + " " + eyePlanetOffset.y + " " + eyePlanetOffset.z );
    }

    // add offset from eye
    StateVector addEyeOffset( StateVector v, StateVector offset ) {
        v.x = v.x + offset.x;
        v.y = v.y + offset.y;
        v.z = v.z + offset.z;
        return( v );
    }

    void drawEyeFrame( Graphics g ) {
        StateVector v = new StateVector();
        v.setStateVector( dimension.x, dimension.y, -1, dimension.x, -dimension.y, -1 );
        clipAndDrawLine( g, v, 5 );
        v.setStateVector( dimension.x, -dimension.y, -1, -dimension.x, -dimension.y, -1 );
        clipAndDrawLine( g, v, 5 );
        v.setStateVector( -dimension.x, -dimension.y, -1, -dimension.x, dimension.y, -1 );
        clipAndDrawLine( g, v, 5 );
        v.setStateVector( -dimension.x, dimension.y, -1, dimension.x, dimension.y, -1 );
        clipAndDrawLine( g, v, 5 );
    }


    // paint all paintstrokes in linked list of paintstrokes starting with farPaintStoke
    void paint( Graphics g1, Graphics g2, Graphics g3 ) {
       // print linked list z depths
       int n = 0;
       PaintStroke p = this.farPaintStroke;
       while ( p.depth >= 0 ) {
//         System.out.println( "paintstroke " + n + " at depth " + p.depth );
           p.paint( g1, g2, g3, this );
           p = p.nextPaintStroke;
           n++;
       }
//     System.out.println( n + " paintstrokes " );
       farPaintStroke = new PaintStroke( (int)1E20, nearPaintStroke, 0, 0, 0, 0, 0, 0, 0, 0, 0 , 0, 0 );


    }

    void paintString( Graphics g, String str, int stroffset, StateVector s ) {
        double zdepth = s.z;
        StateVector v = new StateVector( s.x, s.y, s.z, s.vx, s.vy, s.vz );
        if ( this.typeOfEye == 1 ) v = unPinHole( v );
        v = screenScale( v );
//      if ( str.equals( "275") ) System.out.println( zdepth );
        VecFont.drawString( this, app.ctlGraphics, str, stroffset, v, zdepth );
//      System.out.println( "paintstring showNumbers " + app.showNumbers);
   }


    // this paint method expects points to have negative z distances down z axis
    // after having been transformed around the eye.
    StateVector paintPoint( Graphics g, StateVector s, int num, int ci )  {
        double angle, d;
        int x1, y1, x2, y2;
        StateVector v = new StateVector( s.x, s.y, s.z, s.vx, s.vy, s.vz );
        double zdepth = s.z;

        if ( v.z < 0 ) {
            if ( this.typeOfEye == 1 ) v = unPinHole( v );
            if ( ( Math.abs(dimension.z *v.x/v.z) < dimension.x && Math.abs(dimension.z *v.y/v.z) < dimension.y ) ) {
                if ( app.yearRotate ) {
                    v = Mathut.transformAroundZaxis( v, -yearRotationAngle() );  // this to rotate view annually
                }
//              if ( app.pool.b[num].z < 0 ) ci = 9;
                v = screenScale( v );
                v = screenFinalTransform( v );
                x1 = (int)v.x;
                y1 = (int)v.y;
                x2 = (int)v.x;
                y2 = (int)v.y;

//              if ( num == 4 ) System.out.println( "star eye v " + num + ": " + s.x + " " + s.y + " " + s.z );
//

                // if one point is inside screen, draw the line
//              g.drawLine( x1, y1, x2, y2);
                farPaintStroke.addPaintStroke( zdepth, farPaintStroke, this, 1, 2, 0, ci, x1, y1, x2, y2, 0, 0, 0 );

/*
                // distance from cursor
                d = Math.sqrt( (cursorx-x1)*(cursorx-x1) + (cursory-y1)*(cursory-y1) );
                if ( d < cursorClosestDistance ) {
                    cursorClosestDistance = d;
                    cursorClosestNum = num;
                }
*/
            }
        }
        return( s );
    }

    // this paint method expects points to have negative z distances down z axis
    // after having been transformed around the eye.
    StateVector paintVector( Graphics g, StateVector s, boolean scaleIt, int ci, int uniqueID, int vtype, int vpoints, int vpn )  {
        int x1, y1, x2, y2;
        String str;
        StateVector v = new StateVector( s.x, s.y, s.z, s.vx, s.vy, s.vz );
        v.parent = s.parent;
        v.flag1 = s.flag1;
        double zdepth = s.z;

        if ( v.z < 0 && v.vz < 0 ) {

                    // correct pinhole camera effect, and scale image as seen from eye
                    if ( scaleIt ) {
                        if ( this.typeOfEye == 1 ) {
                            // correct the pinhole camera effect
                            v = unPinHole( v );
                        }
//                      if ( vtype == 7 ) System.out.println( "enter screenScale (v.x - v.vx) = " + (v.x - v.vx) + "v.flag1" + v.flag1 );
                        v = screenScale( v );
//                      if ( vtype == 7 ) System.out.println( "exit screenScale (v.x - v.vx) = " + (v.x - v.vx) );
                    } else {
                        // vecFont doesn't need scaling
                        v.setStateVector( v.x, v.y, v.z, v.vx, v.vy, v.vz );
                    }

                    // clip to view window
                    if ( vtype == 7 ) {
                        // here if DispersedPolygon vectors are being processed
                        v = polyClip( v, -dimension.x, -dimension.y, dimension.x, dimension.y );
                    } else {
                        // clip returns clipped vector v.flag1 = 2 if vector needs painting
                        v = clip( v, -dimension.x, -dimension.y, dimension.x, dimension.y );
                    }

                    if ( v.flag1 == 2 ) {

                        // final transform is to have x,y locations conform to top left screen writing
                        // This shifts the numbers, but doesn't scale them.
                        v = screenFinalTransform( v );
                        x1 = (int)v.x;
                        y1 = (int)v.y;
                        x2 = (int)v.vx;
                        y2 = (int)v.vy;

                        if ( v.parent > 0 ) {
                            System.out.println( "paint parent number");
                            paintString( g, ""+v.parent, 0, v );
                        }

                        // if one point is inside screen, draw the line
                        // vtype = 1 for standard vectors
                        // vtype = 7 for filled polygon vectors
                        farPaintStroke.addPaintStroke( zdepth, farPaintStroke, this, vtype, 2, 0, ci, x1, y1, x2, y2, uniqueID, vpoints, vpn );

                    }
                }
//          }
//      }
        return( v );
    }

    void paintCircle( Graphics g, StateVector v, double r, int ci )  {
        int x1, y1, x2, y2, npoints;
        double a;
        double rc[] = new double[4];
        double rmax = 0;
        double rmin = 1E10;
        double stepAngle = 0;
        double zdepth = v.z;

        if ( v.z < 0 ) {
            if ( this.typeOfEye == 1 ) v = unPinHole( v );
            v = screenScale( v );
//          System.out.println( "star eye v " + num + ": " + v.x + " " + v.y + " " + v.z );

            rc[0] = Math.sqrt( (dimension.x - v.x)*(dimension.x - v.x) + (dimension.y - v.y)*(dimension.y - v.y)  );
            rc[1] = Math.sqrt( (-dimension.x - v.x)*(-dimension.x - v.x) + (-dimension.y - v.y)*(-dimension.y - v.y)  );
            rc[2] = Math.sqrt( (dimension.x - v.x)*(dimension.x - v.x) + (-dimension.y - v.y)*(-dimension.y - v.y)  );
            rc[3] = Math.sqrt( (-dimension.x - v.x)*(-dimension.x - v.x) + (dimension.y - v.y)*(dimension.y - v.y)  );
            if ( rc[3] > rc[2] ) {  a = rc[2]; rc[2]=rc[3]; rc[3] = a; }
            if ( rc[2] > rc[1] ) {  a = rc[1]; rc[1]=rc[2]; rc[2] = a; }
            if ( rc[1] > rc[0] ) {  a = rc[0]; rc[0]=rc[1]; rc[1] = a; }
            if ( rc[2] > rc[1] ) {  a = rc[1]; rc[1]=rc[2]; rc[2] = a; }
            if ( rc[3] > rc[2] ) {  a = rc[2]; rc[2]=rc[3]; rc[3] = a; }
            rmax =  rc[0];
            rmin =  rc[3];

//          v = screenFinalTransform( v );
//          g.drawOval( (int)(v.x-r), (int)(v.y-r), (int)(r*2.0), (int)(r*2.0) );
//          r = this.dimension.z * r / v.z;

// updated code 4 april 2015 not working yet

            if ( (v.x > -dimension.x ) && (v.x < dimension.x  ) && (v.y > -dimension.y  ) && (v.y < dimension.y  )  ) {
                if ( (v.x > -dimension.x + r ) && (v.x < dimension.x - r ) && (v.y > -dimension.y + r ) && (v.y < dimension.y - r )  ) {
                    // if circle within window, use generic method
                    v = screenFinalTransform( v );
//                  g.drawOval( (int)(v.x-r), (int)(v.y-r), (int)(r*2.0), (int)(r*2.0) );
                    farPaintStroke.addPaintStroke( zdepth, farPaintStroke, this, 5, 2, 0, ci, (int)(v.x-r), (int)(v.y-r), (int)(r*2.0), (int)(r*2.0), 0, 64, 0 );
                } else if ( r < rmax ) {
                    // circle centre inside window, radius < window corner rmax
                    paintVectorCircle( g, v, r );
                }
            } else  {
                if ( rmax > r && r > rmin ) {
                    // circle centre outside window
                    paintVectorCircle( g, v, r );
                }
                if ( (v.x > -dimension.x - r ) && (v.x < dimension.x + r ) && (v.y > -dimension.y - r ) && (v.y < dimension.y + r )  ) {
                    // circle centre just outside edge of window
                    paintVectorCircle( g, v, r );
                }
            }

        }
    }

    void paintVectorCircle(  Graphics g, StateVector v, double r ) {
        int x1, y1, x2, y2, npoints;
        double stepAngle;

        if ( this.typeOfEye == 1 ) v = unPinHole( v );
        npoints = (int)(0.2 * r) + 7;
        stepAngle = 2.0 * Math.PI / (double)npoints;
        StateVector s1 = new StateVector();
        StateVector s2 = new StateVector( r, 0, 0, r * Math.cos(stepAngle), r * Math.sin(stepAngle), 0 );
        s1.x = s2.x + v.x;
        s1.y = s2.y + v.y;
        s1.vx = s2.vx + v.x;
        s1.vy = s2.vy + v.y;
//      System.out.println( "circle " + s1.x + " " + s1.y + " " + s1.vx + " " + s1.vy );
        clipAndDrawLine( g, s1, 5 );
        for ( int n=0; n<npoints; n++ ) {
            s2 = Mathut.transformAroundZaxis( s2, stepAngle );
            s1.x = s2.x + v.x;
            s1.y = s2.y + v.y;
            s1.vx = s2.vx + v.x;
            s1.vy = s2.vy + v.y;
//          System.out.println( "   " + s1.x + " " + s1.y + " " + s1.vx + " " + s1.vy );
            clipAndDrawLine( g, s1, 5 );
        }

    }

    // unpinhole vector
    StateVector unPinHole( StateVector view ) {
        StateVector v1 = new StateVector();
        StateVector v2 = new StateVector();
        v1 = unPinHole( view.x, view.y, view.z );
        v2 = unPinHole( view.vx, view.vy, view.vz );
        v1.vx = v2.x;
        v1.vy = v2.y;
        v1.vz = v2.z;
        v1.flag1 = view.flag1;
        return( v1 );
    }

    // unpinhole point
    // ensures thar constant radial angles are represented by constant radial distance on screen.
    // This doesn't result in constant circumferential distances as well, but is an improvement on pinhole view.
    StateVector unPinHole( double x, double y, double z ) {
        double d, f, r, theta, alpha;
        StateVector v = new StateVector();

//      System.out.println( "unpinhole a " + view.x + " " + view.y );
        // diary 2 may 2015
        d = Math.sqrt( x*x + y*y );
        theta = Math.atan2( d, -z );
        f = -z * Math.tan( theta );
        r = -z * theta ;
//      r = (11.0 * r + f ) /12.0;
        alpha = Math.atan2( y, x );
        v.x = r * Math.cos(alpha);
        v.y = r * Math.sin(alpha);
        v.z = z;
//      System.out.println( "unpinhole b " + v.x + " " + v.y );

        return( v );
    }


    void clipAndDrawLine( Graphics g, StateVector v, int ci ) {
        StateVector s = new StateVector( v.x, v.y, -1, v.vx, v.vy, -1 );
        s  = clip( s, -dimension.x, -dimension.y, dimension.x, dimension.y );
        if ( s.flag1 == 2 ) {
            s = screenFinalTransform( s );
//          g.drawLine( (int)s.x, (int)s.y, (int)s.vx, (int)s.vy );
            farPaintStroke.addPaintStroke( s.z, farPaintStroke, this, 1, 2, 0, ci, (int)s.x, (int)s.y, (int)s.vx, (int)s.vy, 0, 0, 0 );
       }
    }

    double yearRotationAngle() {
        double a = this.app.currentJDCT - this.app.startJDCT;
        a = a * 2.0 * Math.PI / 365.25;
        return( a );
    }

    void testClip() {
        double wx1 = -100.0;
        double wx2 = +100.0;
        double wy1 = -100.0;
        double wy2 = +100.0;
        StateVector v = new StateVector();

        System.out.println();
        v.setStateVector( -80,-20, 0, -100, -100, 0 );
        v.flag1 = -1;
        System.out.println( v.flag1 + " " + v.x + "," + v.y  + " " + v.vx + "," + v.vy );
        v = clip( v, wx1, wy1, wx2, wy2 );
        System.out.println( v.flag1 + " " + v.x + "," + v.y  + " " + v.vx + "," + v.vy );

        System.out.println();
        v.setStateVector(  40, 60, 0, 60,  80, 0 );
        v.flag1 = -1;
        System.out.println( v.flag1 + " " + v.x + "," + v.y  + " " + v.vx + "," + v.vy );
        v = clip( v, wx1, wy1, wx2, wy2 );
        System.out.println( v.flag1 + " " + v.x + "," + v.y  + " " + v.vx + "," + v.vy );

        System.out.println();
        v.setStateVector( -140, 20, 0, 140, 20, 0 );
        v.flag1 = -1;
        System.out.println( v.flag1 + " " + v.x + "," + v.y  + " " + v.vx + "," + v.vy );
        v = clip( v, wx1, wy1, wx2, wy2 );
        System.out.println( v.flag1 + " " + v.x + "," + v.y  + " " + v.vx + "," + v.vy );

        System.out.println();
        v.setStateVector(   150, 0, 0, 0, 0, 0 );
        v.flag1 = -1;
        System.out.println( v.flag1 + " " + v.x + "," + v.y  + " " + v.vx + "," + v.vy );
        v = clip( v, wx1, wy1, wx2, wy2 );
        System.out.println( v.flag1 + " " + v.x + "," + v.y  + " " + v.vx + "," + v.vy );

    }

    // Clip line to window.
    // wx1, wy1 is bottom left of window, wx2, wy2 is top right of window.
    // v holds the line vector, which has a rectangular box containing it, vx1, vy1 - vx2,vy2
    StateVector clip( StateVector v, double wx1, double wy1, double wx2, double wy2 ) {
        int i = 0;
        double vx1, vx2, vy1, vy2, vlx, vly, lx, ly, x1, y1, x2, y2;
        double x[] = new double[2];
        double y[] = new double[2];

        vlx = 0;
        vly = 0;
        v.flag1 = 0;
        if ( v.x < wx1 && v.vx < wx1 ) { }
        else if ( v.x > wx2 && v.vx > wx2 ) { }     // vector is outside right
        else if ( v.y < wy1 && v.vy < wy1 ) { }     // vector is outside top
        else if ( v.y > wy2 && v.vy > wy2 ) { }     // vector is outside bottom
        else {
            // vector may intersect window
            // vector box has vx1,vy1 bottom left, vx2,vy2 top right.
            if ( v.x > v.vx ) {
                vx2 = v.x;
                vx1 = v.vx;
            } else {
                vx2 = v.vx;
                vx1 = v.x;
            }
            if ( v.y > v.vy ) {
                vy2 = v.y;
                vy1 = v.vy;
            } else {
                vy2 = v.vy;
                vy1 = v.y;
            }

            vlx = v.vx - v.x;
            vly = v.vy - v.y;

            // find 2 window intercept points inside both boxes
            if ( isInside( v.x, v.y, wx1, wy1, wx2, wy2 ) ) {
                x[i] = v.x;
                y[i] = v.y;
                i++;
            }
            if ( isInside( v.vx, v.vy, wx1, wy1, wx2, wy2 ) ) {
                x[i] = v.vx;
                y[i] = v.vy;
                i++;
            }
            if ( i < 2 ) {
                {
                    x[i] = v.x + (wy1 - v.y) * vlx / vly;                       // intersect with wy1 (allow the infinities when vly=0)
                    y[i] = wy1;
                }
                if ( isInside( x[i], y[i], wx1, wy1, wx2, wy2 ) ) {
                    if ( isInside( x[i], y[i], vx1, vy1, vx2, vy2 ) ) {
                        i++;
                    }
                }

                if ( i < 2 ) {
                    {
                        x[i] = v.x + (wy2 - v.y) * vlx / vly;                   // intersect with wy2
                        y[i] = wy2;
                    }
                    if ( isInside( x[i], y[i], wx1, wy1, wx2, wy2 ) ) {
                        if ( isInside( x[i], y[i], vx1, vy1, vx2, vy2 ) ) {
                            i++;
                        }
                    }


                    if ( i < 2 ) {
                        {
                            y[i] = v.y + (wx1 - v.x) * vly / vlx;               // intersect with wx1
                            x[i] = wx1;
                        }
                        if ( isInside( x[i], y[i], wx1, wy1, wx2, wy2 ) ) {
                            if ( isInside( x[i], y[i], vx1, vy1, vx2, vy2 ) ) {
                                i++;
                            }
                        }

                        if ( i < 2 ) {
                            {
                                y[i] = v.y + (wx2 - v.x) * vly / vlx;           // intersect with wx2
                                x[i] = wx2;
                            }
                            if ( isInside( x[i], y[i], wx1, wy1, wx2, wy2 ) ) {
                                if ( isInside( x[i], y[i], vx1, vy1, vx2, vy2 ) ) {
                                    i++;
                                }
                            }
                        }
                    }
                }
            }
        }
        v.flag1 = i;
        v.x = x[0];
        v.y = y[0];
        v.vx = x[1];
        v.vy = y[1];

        return( v );
    }

    // special clipping method for DispersedPolygon vectors to pull them to clip window
    // wx1, wy1 is bottom left of window, wx2, wy2 is top right of window.
    // v holds the line vector, which has a rectangular box containing it, vx1, vy1 - vx2,vy2
    StateVector polyClip( StateVector v, double wx1, double wy1, double wx2, double wy2 ) {

        if ( v.x > wx2 ) v.x = wx2;
        if ( v.x < wx1 ) v.x = wx1;
        if ( v.y > wy2 ) v.y = wy2;
        if ( v.y < wy1 ) v.y = wy1;

        if ( v.vx > wx2 ) v.vx = wx2;
        if ( v.vx < wx1 ) v.vx = wx1;
        if ( v.vy > wy2 ) v.vy = wy2;
        if ( v.vy < wy1 ) v.vy = wy1;

        // normal well-tested clip(} returns vflag1 = 2 if vector needs painting
        // In the current relatively-untested polyClip method, points outside the clip window are pulled back into it.
        // and so should all be painted
        v.flag1 = 2;

        return( v );
    }


    // find whether point x,y is inside window
    boolean isInside( double x, double y, double wx1, double wy1, double wx2, double wy2 ) {
        boolean inside = false;
        if ( x <= wx2 && x >= wx1 ) {
            if ( y <= wy2 && y >= wy1 ) {
                inside = true;
            }
        }
        return( inside );
    }

    StateVector screenScale( StateVector s ) {
        StateVector v = new StateVector();
        v.x = this.dimension.z * s.x / s.z;
        v.y = this.dimension.z * s.y / s.z;
        v.z = s.z;
        v.vx = this.dimension.z * s.vx / s.vz;
        v.vy = this.dimension.z * s.vy / s.vz;
        v.vz = s.vz;
        return( v );
    }

    StateVector screenFinalTransform( StateVector s ) {
        StateVector v = new StateVector();
        v.x = ( this.dimension.ax + s.x );
        v.y = ( this.dimension.ay - s.y );
        v.vx = ( this.dimension.ax + s.vx );
        v.vy = ( this.dimension.ay - s.vy );
//      v.z = s.z;
//      v.vz = s.vz;
        return( v );
    }
/*
    // untransform a transform
    StateVector unScreenFinalTransform( StateVector s ) {
        StateVector v = new StateVector();
        v.x = ( -dimension.ax + s.x );
        v.y = ( -dimension.ay + s.y );
        v.vx = ( -dimension.ax + s.vx );
        v.vy = ( -dimension.ay + s.vy );
        return( v );
    }
*/
/*
    StateVector screenTransform( StateVector s ) {
        StateVector v = new StateVector();
        if ( this.typeOfEye == 0 ) {
            // pinhole camera
            // using internal dimensions of eye, find screen
            v.x = ( this.dimension.ax + this.dimension.z * s.x / s.z );
            v.y = ( this.dimension.ay - this.dimension.z * s.y / s.z );
            v.vx = ( this.dimension.ax + this.dimension.z * s.vx / s.vz );
            v.vy = ( this.dimension.ay - this.dimension.z * s.vy / s.vz );
        } else if ( this.typeOfEye == 1 ) {
            // fisheye camera NOT WORKING ( v should become s )
            double l, altitude, azimuth;
            l = Math.sqrt( v.x*v.x + v.y*v.y );
            azimuth = Math.atan2(v.y, v.x);
            altitude = Math.atan2( v.z, l);
            v.x = this.dimension.ax + altitude * dimension.z * Math.cos( azimuth );
            v.y = this.dimension.ay - altitude * dimension.z * Math.sin( azimuth );
            l = Math.sqrt( v.vx*v.vx + v.vy*v.vy );
            azimuth = Math.atan2(v.vy, v.vx);
            altitude = Math.atan2( v.vz, l);
            v.vx = this.dimension.ax + altitude * dimension.z * Math.cos( azimuth );
            v.vy = this.dimension.ay - altitude * dimension.z * Math.sin( azimuth );

        }
        return( v );
    }
*/

    double distanceFromOrigin( StateVector v ) {
        return( Math.sqrt( v.x*v.x + v.y*v.y + v.z*v.z ) );
    }


}
