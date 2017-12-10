/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dynamics;

import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author CFD
 */
public class Tie {


// elastic ties connect bodies
    static int uniqueNumber = 0;
    int unique;
    int objectType = 2;
    Dynamics ap;
    static int nties;                   // number of ties
    static double net_pe;               // net potential energy
    boolean exists;
    boolean snapped;
    int num;                        // tied body number
    Body b1;                      // end body 1
    Body b2;                      // end body 2
    double tie_length;           // tie current length
    double tie_free_length;      // tie free length
    double tie_max_length;       // tie maximum length
    double tie_K;                // tie spring constant
    double tension;              // tie tension
    double extension;            // tie extension
    boolean paint_it;
    Color tie_colour;            // tie colour

    // explicit default constructor
    public Tie() {  }


    public Tie( Dynamics a, int n, Body n1, Body n2, double fmax, double k ){
      this.unique = uniqueNumber++;
      this.exists = true;
      this.snapped = false;
      ap = a;
      this.paint_it = true;
      this.b1 = n1;
      this.b2 = n2;

      this.set_tie_length();
      this.tie_free_length = this.tie_length;
      this.tie_max_length = fmax * this.tie_free_length;

      this.tie_colour = new Color(  80,  80,  80 );
      tie_K = k;
    }

    // break tie
    void break_tie() {
        if ( !this.snapped ) System.out.println( "tie " + this.num + " snapped!!!" );
        this.tie_K = 0;
        this.snapped = true;
        this.exists = false;
        this.paint_it = false;
    }

    void set_tie_length() {
        double lx, ly, lz;
        lx = this.b1.currentState.x - this.b2.currentState.x;
        ly = this.b1.currentState.y - this.b2.currentState.y;
        lz = this.b1.currentState.z - this.b2.currentState.z;
        this.tie_length = Math.sqrt( lx * lx + ly * ly + lz * lz );
    }

    void setFreeLengthToActualLength() {
        set_tie_length();
        this.tie_free_length = this.tie_length;
    }

    // change body at one end of tie
    void changeTieNode( Body oldb, Body newb ) {
        if ( this.b1 == oldb ) this.b1 = newb;
        if ( this.b2 == oldb ) this.b2 = newb;
    }

    // Calculate accelerations due to tie tension/compression.
    public void add_elastic_accelerations( Body b[] ) {
        double lx, ly, lz, r, dl, f;

        lx = this.b1.currentState.x - this.b2.currentState.x;
        ly = this.b1.currentState.y - this.b2.currentState.y;
        lz = this.b1.currentState.z - this.b2.currentState.z;
        r = Math.sqrt(lx * lx + ly * ly + lz * lz );             // tie length
        if ( r < 0.0001 * this.tie_free_length ) r = 0.0001 * this.tie_free_length;
        this.tie_length = r;                                     // new tie length
        this.extension = this.tie_free_length - r;               // tie extension

//      if ( this.extension > 0 ) { this.extension = 0; }        // tension only (for now)
        f = this.tie_K * this.extension;                         // force = K.dl
        this.tension = f;                                        // f is + in compression, - in tension

        // acceleration = force / mass
        this.b1.currentState.ax = this.b1.currentState.ax + (f * lx)/(r * this.b1.m);    // x component of accel
        this.b1.currentState.ay = this.b1.currentState.ay + (f * ly)/(r * this.b1.m);    // y component of accel
        this.b1.currentState.az = this.b1.currentState.az + (f * lz)/(r * this.b1.m);    // z component of accel

        this.b2.currentState.ax = this.b2.currentState.ax - (f * lx)/(r * this.b2.m);    // x component of accel
        this.b2.currentState.ay = this.b2.currentState.ay - (f * ly)/(r * this.b2.m);    // y component of accel
        this.b2.currentState.az = this.b2.currentState.az - (f * lz)/(r * this.b2.m);    // z component of accel

    }

    // paint method draws tie as a line.
    // blue in tension, red in compression
    public void paint( Graphics g ) {
        int x, y, z, x1, y1, x2, y2, ci;

        ap.offGraphics.setPaintMode();
        if ( this.tension < 0 ) {
            ap.offGraphics.setColor( Color.blue );
        } else {
            ap.offGraphics.setColor( Color.red );
        }

        ap.offGraphics.drawLine( (int)x_t( b1.currentState.x ), (int)y_t( b1.currentState.y ), (int)x_t( b2.currentState.x ), (int)y_t( b2.currentState.y ) );

    }

    // x transformation to screen coordinates
    int x_t(double x ) {
        return (int) ( ap.screenXYscale * x + ap.screenXoffset);
    }

    // y transformation to screen coordinates
    int y_t(double y ) {
        return (int) (ap.screenXYscale * -y + ap.screenYoffset);
    }

    // length transformation to screen pixels
    int l_t(double l ) {
        return (int) ( ap.screenXYscale * l);
    }

}



