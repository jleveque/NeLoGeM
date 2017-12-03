/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamics;

import static dynamics.Mathut.degreesToRadians;

/**
 *
 * @author CFD
 */
public class ReferenceFrame {

    Dynamics ap;
    StateVector referenceFrame[] = new StateVector[3];
    final int Xaxis = 0;
    final int Yaxis = 1;
    final int Zaxis = 2;
    StateVector axes[] = new StateVector[3];

    int bref;                   // reference body (planet)
    double latitude;            // latitude (degrees)
    double longitude;           // longitude (degrees)
    double rmul;                // radius multiple (1.0 for surface)

    // default constructor
    ReferenceFrame() { }

    // reference frame origin and axes held in statevector XYZ
    ReferenceFrame( Dynamics a, StateVector origin, StateVector xAxis, StateVector yAxis, StateVector zAxis ) {
        this.ap = a;
        // XYZ axes are stored as state vectors x,y,z  --> vx,vy,vz
        referenceFrame[Xaxis] = new StateVector( origin.x, origin.y, origin.z, xAxis.x, xAxis.y, xAxis.z );
        referenceFrame[Yaxis] = new StateVector( origin.x, origin.y, origin.z, yAxis.x, yAxis.y, yAxis.z );
        referenceFrame[Zaxis] = new StateVector( origin.x, origin.y, origin.z, zAxis.x, zAxis.y, zAxis.z );
    }

    // reference frame on surface of planet b0
    ReferenceFrame( Dynamics a, int bnum, double lat, double lon, double rmul, double extend ) {
        this.ap = a;
        this.bref = bnum;
        this.latitude = lat;
        this.longitude = lon;
        this.rmul = rmul;
    }

    // 17 nov 2017
    // v[] contains a set of XYZ body locations within the current reference frame
    // at lat-long-rmul on the surface of a planet, and with the same velocity as
    // a point on the surface.
    // v[n] is transformed to barycentric XYZ, and Vx, Vy, Vz
    StateVector[] transformToBarycentricXYZ( StateVector[] v, int ns ) {
        int n;
        double radialDistance, axialRadius;
        double rLat = latitude * Mathut.degreesToRadians;
        double rLong = longitude * Mathut.degreesToRadians;
        double fractionOfDay = ap.ss.b[bref].siderealClock / ap.ss.b[bref].siderealDay;
        double addLongitude = ( fractionOfDay * 360.0 ) * Mathut.degreesToRadians;   // fixed for correct longitude sign
        double signrotate = +1;
        double angularVelocity = Math.PI * 2.0 / ap.ss.b[bref].siderealDay;
//      double axialRadius = rmul * ap.ss.b[this.bref].r * Math.cos( rLat );
        StateVector o = new StateVector( rmul * ap.ss.b[bref].r, 0, 0, 0, 0, 0 );
//      System.out.println( "rmul " + rmul + " vy "  + (axialRadius * angularVelocity) );

        for ( n=0; n<ns; n++ ) {
            // move vector from planet 0,0,0 origin to surface on X axis
            v[n] = addPositionStateVector( v[n], o );
            // rotate vector around Y axis to required latitude
            v[n] = Mathut.transformAroundYaxis( v[n], rLat );
            // if body is fixed, find its vx,vy,vz in rotating reference frame
            // otherwise leave vx, vy, vz unchanged
            if ( v[n].flag1 == 1 ) {
                radialDistance = Math.sqrt( v[n].x*v[n].x + v[n].y*v[n].y + v[n].z*v[n].z );
                axialRadius = radialDistance * Math.cos( rLat );
                // tangential velocity at rmul radius
                v[n].vx = 0;
                v[n].vy = axialRadius * angularVelocity;
                v[n].vz = 0;
            }
            // rotate vector around Z axis to required latitude
            v[n] = Mathut.transformAroundZaxis( v[n], rLong );
            // rotate vector around Z axis to right time of day
            v[n] = Mathut.transformAroundZaxis( v[n], addLongitude );
            // tilt axis of planet
            v[n] = Mathut.tilt( v[n], ap.ss.b[this.bref], signrotate );
            // offset vector by planet barycentric XYZ location
            v[n] = addPositionStateVector( v[n], ap.ss.b[bref].currentState );
            // v[n] is now a barycentric state vector carrying XYZ. vx, vy, vz
        }

        return( v );
    }

    // transformFromBarycentricXYZ() is opposite of transformToBarycentricXYZ()
    StateVector[] transformFromBarycentricXYZ( StateVector[] v, int ns ) {
        int n;
        double rLat = latitude * Mathut.degreesToRadians;
        double rLong = longitude * Mathut.degreesToRadians;
        double fractionOfDay = ap.ss.b[bref].siderealClock / ap.ss.b[bref].siderealDay;
        double addLongitude = ( fractionOfDay * 360.0 ) * Mathut.degreesToRadians;   // fixed for correct longitude sign
        double signrotate = +1;
        StateVector o = new StateVector( rmul * ap.ss.b[bref].r, 0, 0, 0, 0, 0 );

        for ( n=0; n<ns; n++ ) {
            // offset vector by planet barycentric XYZ location
            v[n] = subtractPositionStateVector( v[n], ap.ss.b[bref].currentState );
            // tilt back axis of planet
            v[n] = Mathut.tilt( v[n], ap.ss.b[this.bref], -signrotate );
            // rotate vector around Z axis to right time of day
            v[n] = Mathut.transformAroundZaxis( v[n], -addLongitude );
            // rotate vector around Z axis from current time of day
            v[n] = Mathut.transformAroundZaxis( v[n], -rLong );
            // rotate vector around Z axis from required longitude
            v[n] = Mathut.transformAroundYaxis( v[n], -rLat );
            // move vector from surface on X axis to planet 0,0,0 origin
            v[n] = subtractPositionStateVector( v[n], o );
            // v[n] now holds XYZ and vx, vy, vz in rotating reference frame
        }

        return( v );
    }

    // move vector v
    StateVector moveVector( StateVector v, StateVector m ) {
        v.x += m.x;
        v.y += m.y;
        v.z += m.z;
        v.vx += m.x;
        v.vy += m.y;
        v.vz += m.z;
        return( v );
    }

    // add state vector v  xyz, vx, vy, vz
    StateVector addPositionStateVector( StateVector v, StateVector m ) {
        v.x += m.x;
        v.y += m.y;
        v.z += m.z;
        v.vx += m.vx;
        v.vy += m.vy;
        v.vz += m.vz;
        return( v );
    }

    // subtract state vector v xyz, vx, vy, vz
    StateVector subtractPositionStateVector( StateVector v, StateVector m ) {
        v.x -= m.x;
        v.y -= m.y;
        v.z -= m.z;
        v.vx -= m.vx;
        v.vy -= m.vy;
        v.vz -= m.vz;
        return( v );
    }

    //
    StateVector swapAxes( StateVector v ) {
        double temp = v.x;
        v.x = v.z;
        v.z = v.y;
        v.y = temp;
        return( v );
    }


}
