/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamics;

/**
 *
 * @author CFD
 */
public class Tetrahedron extends Process {
//  Dynamics ap;

    Tetrahedron() { }

    Tetrahedron( Dynamics a ) {
        this.ap = a;
        double mass = 1.0;
        StateVector svref = new StateVector(  -1.80E+11, -2.7E+11, -1.12E+09,  2.148983152751387E+04, -1.070305946423849E+04, -7.525648800168048E-04 );
        StateVector sv = new StateVector( 10, 0, 0, 0, 0, 0 );

        // negative body numbers is a fix to prevent Body add_gravitational_acceleration() finding two body numbers the same
        b[this.nbodies] = new Body( ap, -this.nbodies, svref, mass, 1.0, true );
        b[this.nbodies].referenceFrame = 0;
        nbodies++;

        b[this.nbodies] = new Body( ap, -this.nbodies, sv.addStateVector(svref), mass, 1.0, true );
        b[this.nbodies].referenceFrame = 0;
        nbodies++;

        sv.setStateVector( 0, 7, 0, 0, 0, 0 );
        b[this.nbodies] = new Body( ap, -this.nbodies, sv.addStateVector(svref), mass, 1.0, true );
        b[this.nbodies].referenceFrame = 0;
        nbodies++;

        sv.setStateVector( 0, 2, 6, 0, 0, 0 );
        b[this.nbodies] = new Body( ap, -this.nbodies, sv.addStateVector(svref), mass, 1.0, true );
        b[this.nbodies].referenceFrame = 0;
        nbodies++;
/*                              leave ties out to prevent breakup of tetrahedron
        int m;
        for ( int n=0; n<nbodies; n++ ) {
            m = n+1;
            if ( m > 3 ) m = 0;
            this.t[nties] = new Tie( ap, 0, b[n], b[m], 1.5, 2000.0 );
            nties++;
        }
*/

    }

}
