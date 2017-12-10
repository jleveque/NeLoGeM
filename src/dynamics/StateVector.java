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
public class StateVector {

/*
* StateVector class carries body position, speed, acceleration
*/

    double x;              // location
    double y;
    double z;
    double vx;             // velocity/view
    double vy;
    double vz;
    double ax;             // acceleration/offset
    double ay;
    double az;
    double latitude;
    double longitude;
    double distance;
    int num;                // number
    int vtype;
    int parent;
    int flag1;              // general purpose flags
    int flag2;
    int flag3;
    int flag4;
    int colour;
    double xangle;          // angle of vector to x axis
    double yangle;          // angle of vector to y axis
    double zangle;          // angle of vector to z axis
    double length;          // length of vector (from origin)

    public StateVector(){
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.vx = 0;
        this.vy = 0;
        this.vz = 0;
        this.ax = 0;
        this.ay = 0;
        this.az = 0;
    }

    public StateVector( double x1, double y1, double z1, double xx, double yy, double zz ) {
        this.x = x1;
        this.y = y1;
        this.z = z1;
        this.vx = xx;
        this.vy = yy;
        this.vz = zz;
        this.ax = 0;
        this.ay = 0;
        this.az = 0;
    }

    public StateVector( double x1, double y1, double z1, int f1, int f2, int f3 ) {
        this.x = x1;
        this.y = y1;
        this.z = z1;
        flag1 = f1;
        flag2 = f2;
        flag3 = f3;
    }


    public StateVector( StateVector v ) {
        this.setStateVector( v );
    }

    void setStateVector( double x1, double y1, double z1, double xx, double yy, double zz ) {
        this.x = x1;
        this.y = y1;
        this.z = z1;
        this.vx = xx;
        this.vy = yy;
        this.vz = zz;
    }

    void setStateVector( double x1, double y1, double z1 ) {
        this.x = x1;
        this.y = y1;
        this.z = z1;
    }

    void setStateVector( StateVector v ) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.vx = v.vx;
        this.vy = v.vy;
        this.vz = v.vz;
        this.ax = v.ax;
        this.ay = v.ay;
        this.az = v.az;
        this.xangle = v.xangle;
        this.yangle = v.yangle;
        this.zangle = v.zangle;
    }

    void setvStateVector( double x1, double y1, double z1 ) {
        this.vx = x1;
        this.vy = y1;
        this.vz = z1;
    }

    void clearStateVector() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.vx = 0;
        this.vy = 0;
        this.vz = 0;
        this.ax = 0;
        this.ay = 0;
        this.az = 0;
    }

    void copyStateVectors( StateVector v ) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.vx = v.vx;
        this.vy = v.vy;
        this.vz = v.vz;
        this.ax = v.ax;
        this.ay = v.ay;
        this.az = v.az;
        this.flag1 = v.flag1;
        this.flag2 = v.flag2;
    }

    boolean sameAs( StateVector v ) {
        boolean same = false;
        if ( ( this.x - v.x ) + ( this.y - v.y ) + ( this.z - v.z ) == 0 ) same = true;
        return( same );
    }

    StateVector offsetStateVector( StateVector v ) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        return( this );
    }

    StateVector addStateVector( StateVector v ) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        this.vx += v.vx;
        this.vy += v.vy;
        this.vz += v.vz;
        return( this );
    }

    StateVector subtractStateVector( StateVector v ) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
        this.vx -= v.vx;
        this.vy -= v.vy;
        this.vz -= v.vz;
        return( this );
    }

    double findSpeed( ) {
        double v = Math.sqrt( this.vx*this.vx + this.vy*this.vy + this.vz*this.vz );
        return( v );
    }

    double findLength() {
        this.length = Math.sqrt( this.x*this.x + this.y*this.y + this.z*this.z );
        return( this.length );
    }

    double findXangle() {
        this.xangle = Math.atan2( this.z, this.x );
        return( this.xangle );
    }

    StateVector cloneStateVector() {
        StateVector v = new StateVector();
        v.x = this.x;
        v.y = this.y;
        v.z = this.z;
        v.vx = this.vx;
        v.vy = this.vy;
        v.vz = this.vz;
        return( v );
    }

    StateVector[] cloneStateVector( StateVector[] toClone ) {
        int nrows = toClone.length;
//      int ncols = toClone[0].length;
        StateVector v[] = new StateVector[ nrows ];
        for ( int n=0; n<nrows; n++ ) {
            v[n].x = toClone[n].x;
            v[n].y = toClone[n].y;
            v[n].z = toClone[n].z;
            v[n].vx = toClone[n].vx;
            v[n].vy = toClone[n].vy;
            v[n].vz = toClone[n].vz;
            v[n].flag1 = toClone[n].flag1;
            v[n].flag2 = toClone[n].flag2;
        }
        return( v );
    }


    void printStateVector() {
        System.out.println( "state vector " + x + " " + y + " " + z + " ;"  + vx + " " + vy + " " + vz );
    }

    void printStateVectorKm() {
        System.out.println( "state vector " + x/1000.0 + " " + y/1000.0 + " " + z/1000.0 + " ;"  + vx/1000.0 + " " + vy/1000.0 + " " + vz/1000.0 + " km s");
    }


}


