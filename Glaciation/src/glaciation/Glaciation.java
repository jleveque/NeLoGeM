/*
 * copied from FrozenRadioactiveAsteroid.java
 */
package glaciation;

/* jdk1.1.6 */
// repeat glaciations
// preliminary Earth model
// re-roganising graphics
// CZYN Cenozoic Year Numbering from Chicxulub crater 66.043 million years ago (working)
// ACS WORKING SINGLE-LAYER ATMOSPHERE MODEL
// 8.5 km atmosphere with fixed TOA display
// daily solar radiation calculations working (I hope), timestep solar gain timesteptable
// solar gain timestepTable in operation.
// variable albedo implemented
// AirMeltedIceSheet code debugged and working
// calculating pressure, internal planet gravitational acceleration

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.lang.Math;
import java.text.SimpleDateFormat;
import javax.swing.JTable;

public class Glaciation extends java.applet.Applet implements Runnable {
    private Thread testThread = null;
    EventHandler eventHandler;
    int count, xmax, ymax;
    Image offImage;
    Graphics offGraphics;
    Color colorpalette[] = new Color[15];
    int xpoints[] = new int[4];
    int ypoints[] = new int[4];
    int x1, x2, y1, y2, paletteindex;
    int p1, p2, p3, p4;
    boolean go = true;
    ItemGraphCanvas canvas1;
    Image fieldImage;
    Graphics fieldGraphics;
    LayerDisplay layerDisplay = new LayerDisplay();
    Image layerImage;
    Graphics layerGraphics;
    Image textImage;
    Graphics textGraphics;
    int layerMargin;
    boolean printManager = false;


    int mode = 1;
    int runmode = 1;
    boolean clearScreen = true;

    double timeOfDay = 0.0;                 // time of day 0 - 23
    double hoursPerDay = 10.0;
    double hourstimestep = 0.5;            // time step interval (hrs)
    int day = 0;
    int outcount = 0;
//  double conductionHeatGain = 0.0;
//  double subtemp[][] = new double[2][500]; // subsurface temperatures
    double tcount, tsum, tmean;
    int selector = 0;
    int opcontrol = 2;

    // asteroid additions
    Asteroid asteroid;
    double secondsPerYear = 31540000.0;
    double scale;
    double screenWidth = 400;
    Dimension appletSize;
    double year, century, millennium;
    double currentCZYN = 66043000;     // Cenozoic Tear Bumber 0 is 66 million years before 1 Jan 2000 (Chicxulub crater)
    double eemianCZYN = 66043000 - 100000;  // Emian interglacial
    double initialRadius = 6371000;
    double maxRadius = 6371000;
    double initialLayerHeight = 17;
    double fractionGranite = 1.0;
    double heatGranite = 1E-9;
    long tick = 0;
    long longPeriodIndex = 0;
    double previousGlaciationTotal = 0;
    double glaciationTotal = 0;         // counter
    double glaciationPeriod = 0;        // counter
    double glaciationMaxPeriod = 1000;  // period over which to measure glaciation
    double glaciationPercent = 0;       // % glaciation over glaciationMaxPeriod
    boolean rollingGraphUpdated = false;
    int rollingGraphWidth;
    int rollingGraphHeight;
    int rollingGraphStreams;
    SolarRadiation sr;
    double timeStepYears = 0.01;
    AirMeltedIceSheet amis;

    int videoWindow[][] = {
        {   360,  240, },
        {   480,  360, },
        {   640,  480, },
        {  1280,  720, },
        {  1920, 1080, }
    };

    public void init() {
        this.enableEvents( AWTEvent.MOUSE_EVENT_MASK );

//      amis = new AirMeltedIceSheet( 1000, 24, 273 + 0.1  );

//      setSize( (int)screenWidth, (int)screenWidth );
        screenWidth = videoWindow[1][0];
        setSize( videoWindow[1][0], videoWindow[1][1] );
        appletSize = this.getSize();
        xmax = appletSize.width;         // graphics width
        ymax = appletSize.height;        // graphics height

        setBackground(Color.white);
        count=0;
        year = 0;
        century = 0;
        millennium = 0;

        scale = videoWindow[1][0] / ( maxRadius * 2.5 );
        offImage = createImage( videoWindow[1][0], videoWindow[1][0] );
        offGraphics = offImage.getGraphics();

        // set up textImage for text display
        textImage = createImage( ( 3 * videoWindow[1][0] / 4 ), ( videoWindow[1][0] / 2 ) );
        textGraphics = textImage.getGraphics();

        eventHandler = new EventHandler( this );
        eventHandler.insertEvent( eemianCZYN );
        eventHandler.insertEvent( eemianCZYN + 15007 );
    //  eventHandler.insertEvent( eemianCZYN + 15018 );
        currentCZYN = eventHandler.eventQueue[0];
        eventHandler.printEventQueue();
        System.out.println( (long)(eemianCZYN + 15018) );

        // layer display setup
        layerMargin = videoWindow[1][0] / 4;
        layerImage = createImage( layerMargin, videoWindow[1][0] );
        layerGraphics = layerImage.getGraphics();
        layerDisplay.setDisplaySize( layerMargin, videoWindow[1][1] );

        // rolling graph display setup
        rollingGraphWidth = videoWindow[1][0] * 3 / 4;
        rollingGraphHeight = videoWindow[1][1] / 2;
        canvas1 = new ItemGraphCanvas( this, offGraphics, rollingGraphWidth, rollingGraphHeight, 5,  true );
        fieldImage = createImage( rollingGraphWidth, rollingGraphHeight );
        fieldGraphics = fieldImage.getGraphics();
        fieldGraphics.setColor(Color.white);
        fieldGraphics.fillRect(0, 0, rollingGraphWidth, rollingGraphHeight );
        canvas1.calendarLabel = "e5";

//      sr = new SolarRadiation( 1367.0, 65.0, 0.00273785078713210130047912388775, 365.25 );   // daily
        sr = new SolarRadiation( 1367.0, 30.0, timeStepYears, 365.25 );
//      sr = new SolarRadiation( 1367.0 );


        /* get displaymode */
        try {
          mode =  Integer.parseInt( getParameter("MODE" ) );
        } catch( Exception e ) {
          mode = 1;
        }

        colorpalette[0] = new Color(000, 000, 000);     // black
        colorpalette[1] = new Color( 64,  64,  64);     // light grey
        colorpalette[2] = new Color(128, 128, 128);     // mid gray
        colorpalette[3] = new Color(192, 192, 192);     // darke gray
        colorpalette[4] = new Color(255, 255, 255);     // white
        colorpalette[5] = Color.RED;
        colorpalette[6] = Color.ORANGE;
        colorpalette[7] = Color.YELLOW;
        colorpalette[8] = Color.CYAN;
        colorpalette[9] = new Color( 85,  52,  52);     // brown?
        colorpalette[10] = new Color(135,206, 250);     // sky blue
        colorpalette[11] = new Color(240,240, 240);     // steam
        colorpalette[12] = new Color( 0,  0,  255);     // blue
        colorpalette[13] = new Color( 0,  0,  255);     // blue
        colorpalette[14] = new Color( 0,  0,  255);     // blue


        asteroid = new Asteroid( this, 0 );
//      asteroid.setRadioactiveHeatGeneration( 2, mantle( 6371000, 3400000, 4650, 9990, 20E12 ) );


//      asteroid = new Asteroid( this,   50000, 30, 2, 5000, 5000 );
        asteroid.setRadioactiveHeatGeneration( 2, 2.1773010891400347E-8 );
//      asteroid.setRadioactiveHeatGeneration( 2, 0 );
//      asteroid.solarGain = 0;
//      asteroid.printAsteroid(x1);

//      System.out.println( asteroid.stefanBoltzmannHeatflow(1.0, 273 ) + " " + asteroid.stefanBoltzmannTemperature( 1.0, 314.96494193766694 ) );
        double r1 = 100.0;
        double r2 = 110.0;
        double v = asteroid.sphereVolume(r2) - asteroid.sphereVolume(r1);
        System.out.println( "r2 = " + asteroid.layerRadius( v, asteroid.sphereVolume(r1) ) );


//      asteroid = new Asteroid( initialRadius, initialLayerHeight, fractionGranite, heatGranite );
        System.out.println( "w/m^3= " + mantle( 6371000, 3400000, 4650, 9990, 20E12 ) );

        tsum = 0.0; tcount = 0.0;


//      go = false;

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
        offGraphics.setPaintMode();
        offGraphics.setColor( Color.white );
        offGraphics.fillRect( 0, 0, xmax, ymax );
        x2 = e.getX();
        y2 = e.getY();
        p1 = 1 + x2 % 5;
        p2 = 1 + y2 % 5;
        p3 = 1 + Math.abs( x2-y2 ) % 5;
        p4 = 1 + (x2 + y2) % 6;
        go =  true;
      }
      else super.processMouseEvent(e);
      System.out.println("The line number is " + new Exception().getStackTrace()[0].getLineNumber());
      Object o = this;
      Class c = o.getClass();
      System.out.println("class name is: " + c.getName());
      System.out.println("method name is: " + new Exception().getStackTrace()[0].getMethodName());
      System.out.println("calling method name is: " +  Thread.currentThread().getStackTrace()[2].getMethodName());
//    System.out.println("The class name is " + java.lang.Class.getSimpleName() );

    }


    public void run() {
        runmode = mode;
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        Thread myThread = Thread.currentThread();
        while (testThread == myThread) {
            if ( go ) {

                mathEngine();

                if ( opcontrol == 2 ) {
//                 clearScreen = true;
//                 repaint();
                }

                runmode++;                     // change display graph mode
                if ( runmode > 5) { runmode= 0; }
                count++;
           }
           try {
                Thread.sleep(1);
           } catch (InterruptedException e){ }
       }

    }

    void mathEngine() {
//      double y, ytimestep = 0.01;
        double y, ytimestep = this.timeStepYears;
        double printIinterval = 25000;
        double printDate = printIinterval;
        int nlayer;
        double meltingRadius, boilingRadius;
        int someTimePeriod = 100;

        // do eventchecking first
        eventHandler.checkEventQueue();
//      System.out.println( "CZYN = " + (long)currentCZYN );

        // with ytimestep=0.01, 100 * period = 1.0 year
        someTimePeriod = (int)( 1.0  /  ytimestep );
        for ( int period=0; period < someTimePeriod; period++ ) {

            if ( period%10 == 0 ) {
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
                String str = sdf.format(date);
            }


            year += ytimestep;        // time step monthly
            century = year / 100.0;
            millennium = year / 1000.0;


            // find new temperatures
            asteroid.getNewLayerTemperatures( ( this.secondsPerYear * ytimestep ), year, 600000, 0.5 );
            tick++;
            y = (int)( year * 10 );   // get a single decimal place


        }

        currentCZYN++;


    }

    void rollingGraphOutput( double interval) {
        String s;
        int maxn = 5;
        canvas1.day = (int)(interval);
//      canvas1.calendarStep = (int)(interval*10);   // years per pixel
        canvas1.calendarStep = 1000;                 // years per pixel
        double maxtemp = 500;

        canvas1.nexty[0] = asteroid.lastSurfaceTemperature;
        s =  "Ts";
        canvas1.setStreamLabel(0, s);
        canvas1.setStreamMaxMin( 0, maxtemp, 0 );
/*
        canvas1.nexty[1] = Math.abs( asteroid.surfaceRockHeatFlowRate * 100);
//      System.out.println( "heat flow " +asteroid.surfaceRockHeatFlowRate );
        s =  "hFl" ;
        canvas1.setStreamLabel(1, s);
        canvas1.setStreamMaxMin( 1, maxtemp, 0 );
*/
        canvas1.nexty[1] = Math.abs( asteroid.airLowestBandTemperature );
        s =  "Ta" ;
        canvas1.setStreamLabel(1, s);
        canvas1.setStreamMaxMin( 1, maxtemp, 0 );


        canvas1.nexty[2] = asteroid.surfaceRockTemperature;
        s =  "T" + Integer.toString(1);
        canvas1.setStreamLabel(2, s);
        canvas1.setStreamMaxMin( 2, maxtemp, 0 );


        canvas1.nexty[3] = asteroid.iceWaterDepth;
//      System.out.println( "asteroid.iceDepth " + asteroid.iceDepth );
//      canvas1.nexty[3] = glaciationPercent;
        s =  "ice";
        canvas1.setStreamLabel(3, s);
        canvas1.setStreamMaxMin( 3, maxtemp * 10, 0 );

        canvas1.nexty[4] = asteroid.atmosphereTemperature;
        s =  "Ta" ;
        canvas1.setStreamLabel(4, s);
        canvas1.setStreamMaxMin( 4, maxtemp, 0 );

    }

    // given 20 terawatt Earth radioactive heat gain (in mantle) find heat production per kg of mantle material
    double mantle( double rAsteroid, double rCore, double densityMantle, double densityCore, double totalRadioactiveHeat ) {
        double vAsteroid = 1.33333 * Math.PI * rAsteroid * rAsteroid * rAsteroid;
        double vCore = 1.33333 * Math.PI * rCore * rCore * rCore;
        double vMantle = vAsteroid - vCore;
        double massCore = densityCore * vCore;
        double massMantle = densityMantle * vMantle;  // target mass 4 E 24 Kg
        double heatPerCubicMetre = totalRadioactiveHeat / vMantle;
        double heatPerKilogram = totalRadioactiveHeat / massMantle;
        System.out.println( "massCore " + massCore + " massMantle " + massMantle + " heatPerCubicMetre " + heatPerCubicMetre );
        return( heatPerCubicMetre );
    }


    public void paint(Graphics g) {
        update(g);
//      paintAsteroid( g );
//                 canvas1.paint(g);
    }


    public void update( Graphics g ) {
        int n, bradius, nlayers;
        long lyear;
        int xcentre, ycentre, xtopleft, ytopleft, hr;
        double topRadiusMaterial[] = new double[10];


        if ( rollingGraphUpdated ) {
            canvas1.paint( fieldGraphics );
            this.rollingGraphUpdated = false;
        } else {
//          System.out.println( "roling graph not updated ");
        }

        // extract date from graphicLayer array of layer data
        n = 0;
        int material = -1;
        while ( asteroid.graphicLayer[n][0] >= 0 ) {
            if ( asteroid.graphicLayer[n][1] != material ) {
                material = (int)asteroid.graphicLayer[n][1];
                topRadiusMaterial[ material ] = asteroid.graphicLayer[n][2];
            }
            n++;
        }
        nlayers = n;


        offGraphics.setPaintMode();
        if ( clearScreen ) {
            offGraphics.setColor( Color.white );
            offGraphics.fillRect( 0, 0, xmax, ymax/2 );     // only clear top half of offImage
        }

        lyear = (long)year;

        // display screen text
        textGraphics.setPaintMode();
        textGraphics.setColor( Color.white );
        textGraphics.fillRect( 0, 0, xmax, ymax );
        textGraphics.setColor( Color.black );
        int px = 15;
        textGraphics.drawString( "CZYN " + (float)(currentCZYN - eemianCZYN), 2, px );
        px += 15;
        textGraphics.drawString( "Thin Surface " + asteroid.thinSurfaceBand + " T=" + (float)asteroid.thinSurfaceBandTemperature, 2, px );
        px += 15;
        textGraphics.drawString( "Rock " + (int)asteroid.surfaceRockBand + " hfr=" + (float)asteroid.surfaceRockHeatFlowRate, 2, px );
        px += 15;
        textGraphics.drawString( "IceWater Depth " + (float)asteroid.iceWaterDepth, 2, px );
        px += 15;
        double var;
        boolean showTemperatures =  true;
        for ( n=asteroid.graphicSurfaceRockBand; n<asteroid.graphicLayerCount; n++ ) {
            if ( asteroid.graphicLayer[n][0] >= 0 ) {
                if ( showTemperatures ) {
                    var = asteroid.graphicLayer[asteroid.graphicLayerCount-n-1][4];
                } else {
                    // show conductive heat flows to layer above
                    var = asteroid.graphicLayer[asteroid.graphicLayerCount-n-1][6];
                }
                textGraphics.drawString( "" + (int)asteroid.graphicLayer[asteroid.graphicLayerCount-n-1][1] + "band " + n + " T" + (int)asteroid.graphicLayer[asteroid.graphicLayerCount-n-1][0] + "= " + (float)var, 2, px  );
                px += 15;
            }
        }

//      paintTemperatureGraph( xtoa,  jscale );

        // paint the asteroid layers into layerImage
        layerDisplay.paint( layerGraphics, asteroid, topRadiusMaterial[ 2 ] );

        // draw the layerimage into top left corner of offImage
        offGraphics.drawImage( layerImage, 0, 0, this );

        // draw the textImage offset into offImage
        offGraphics.drawImage( textImage, layerMargin, 0, this );

        // draw the rolling graph fieldImage offset into offImage
        offGraphics.drawImage( fieldImage, layerMargin, ymax/2, this );

        g.drawImage( offImage, 0, 0, this );


    }

    Color setColour( int index ) {
        Color colour = Color.white;
        if ( index == 1 ) colour = Color.black;
        if ( index == 2 ) colour = Color.darkGray;
        if ( index == 3 ) colour = Color.lightGray;
        if ( index == 4 ) colour = Color.cyan;
        return( colour );
    }
/*
    Color setColour( int index , double temperature ) {
        Color colour = Color.white;
        Material mt = new Material( index );
        int cindex = 0;
        if ( temperature > mt.phaseTemperature1 ) cindex = 1;
        if ( temperature > mt.phaseTemperature2 ) cindex = 2;
        colour = colorpalette[ mt.colourIndex[ cindex ] ];
//      if ( index == 1 ) colour = Color.black;
//      if ( index == 2 ) colour = Color.darkGray;
        return( colour );
    }
*/
    Color setColour( int index, double phase ) {
        Color colour = Color.white;
        Material mt = new Material( index );
        int cindex = 0;
        if ( phase == 1 ) cindex = 1;
        if ( phase == 2 ) cindex = 2;
        colour = colorpalette[ mt.colourIndex[ cindex ] ];
        return( colour );
    }

    int scaleRadius( double r, double jscale ) {
        int pixelHeight = (int)( r * jscale );
        return( pixelHeight );
    }

    //



    public void stop() {
        testThread = null;
    }

}



class Asteroid {

    Glaciation ap;

    // planet Earth
    // material type T = 1, iron?, T = 2, rock, T= 3 ice, (T = 4 WAS water,) T = 5 air
    double[][] layers = {
        // 0        1         2      3       4     5     6  7  8    9
        // n,       h,        k,     s.h,    d,   d K,  MT,
        {   2,      610750,   2.12,  790, 12800,  7000,  6, 0, 0, 6371500 },      // 0 inner core
        {   8,      281250,   2.12,  790,  9900,  5000,  7, 0, 0, 6371500 },      // 0 outer core
        {   8,      224000,   2.12,  790,  4400,  4000,  8, 0, 0, 6371500 },      // 2 lower mantle
        {   4,      227000,   2.12,  790,  3400,  3000,  9, 0, 0, 6371500 },      // 2 upper mantle
        {   5,      25000,    2.12,  790,  2650,  1800,  1, 0, 0, 6371500 },      // 3 asthenosphere
        {   4,      6250,     2.12,  790,  2650,  1500,  2, 0, 0, 6371500 },      // 3 lithosphere
        {  15,      2000,     2.12,  790,  2650,  1200,  2, 0, 0, 6371500 },      // 3 lithosphere
        {  18,      1000,     2.12,  790,  2650,   600,  2, 0, 0, 6371500 },      // 3 lithosphere
        {   4,      500,      2.12,  790,  2650,   400,  2, 0, 0, 6371500 },      // 3 lithosphere
        {   1,      0,        2.12,  790,  2650,   288,  0, 0, 0, 6371500 },      // 5 thin surface layer
        {   1,      8500,   0.0243, 1000,  1.29,   243,  5, 0, 0, 6371500 },      // 5 atmospheric air
        {   1,      8500,   0.0243, 1000,  1.29,   243,  5, 0, 0, 6371500 },      // 6 dummy atmospheric air (troposphere)
        {  -1, }
    };

    // 30 asteroid lsyer temperatures after 2.6 million years
    double[] temps = {
        300.13,  536.8,  771.2, 1006.0, 1240.2, 1473.5, 1705.7, 1935.4, 2163.6, 2388.8,
        2609.1, 2824.9, 3033.8, 3235.0, 3430.8, 3616.7, 3793.8, 3960.4, 4117.9, 4262.5,
        4396.5, 4436.0, 4552.5, 4656.3, 4746.4, 4822.5, 4883.6, 4928.7, 4955.5, 4955.5,
    };

    // 30 asteroid lsyer temperatures after 1.5 million years
    double[] temps1 = {
        420.13,  708.8,  970.2, 1229.0, 1488.2, 1746.5, 2001.7, 2254.4, 2501.6, 2741.8,
        2974.1, 3197.9, 3411.8, 3613.0, 3804.8, 3982.7, 4146.8, 4295.4, 4430.9, 4549.5,
        4653.5, 4741.0, 4815.5, 4874.3, 4919.4, 4953.5, 4975.6, 4989.7, 4996.5, 4996.5,
    };

    // 30 asteroid lsyer temperatures after 1.5 million years
    double[] temps2 = {
        276.88, 487.35, 690.75, 886.22, 1074.8, 1258.7, 1440.0, 1620.2, 1800.1, 2323.6,
        2159.0, 2337.6, 2514.8, 2690.2, 2863.4, 3034.2, 3202.2, 3366.9, 3527.8, 3684.5,
        3836.2, 3982.2, 4121.6, 4253.3, 4375.7, 4486.8, 4583.8, 4662.0, 4712.5, 4712.5,
    };

    // 30 asteroid lsyer temperatures after 1.8 million years
    double[] temps3 = {
        275.16, 485.28, 702.23, 924.87, 1151.9, 1382.0, 1613.6, 1845.2, 2082.0, 2323.6,
        2561.4, 2793.8, 3019.4, 3237.0, 3445.4, 3643.5, 3830.6, 4005.6, 4167.8, 4316.4,
        4450.7, 4570.3, 4674.7, 4763.6, 4837.2, 4895.7, 4939.8, 4970.4, 4988.9, 5000.0,
    };

    double radius;
    double nominalRadius;               // radius used in graphic display
    int nlayers;
    LinkedList<ConductiveLayer> llayer = new LinkedList<ConductiveLayer>();
    double initialTemperatureK;
    int nIterations = 0;
    double referenceRadius = 6371000.0;                 // earth radius
    int surfaceBand;                                    // surface band for solar irradiation
    int thinSurfaceBand;                                // thin radiative surface band number
    double thinSurfaceBandTemperature;                  // thin radiative surface band temperature
    int airLowestBand;                                  // bottom layer of air in atmospher
    double airLowestBandTemperature;                    // temperature of bottom layer of air
    double surfaceBandTemperature;
    double lastSurfaceTemperature;
    double surfaceRockHeatFlowRate;
    double surfaceRockTemperature = 0;
    double atmosphereTemperature;
    int surfaceRockBand;
    double surfaceHeatFlowRate = 0;
    double year;
    double graphicLayer[][] = new double[2000][10];     // date for graphics display
    int graphicLayerCount = 0;                          // data for grapjic display
    int graphicSurfaceRockBand = 0;                     // data for graphic display
    boolean resynchronise = true;
    double annualSolarInsolation = 1362 * 3.6E6;         // annual solar radiation kwh-> J UK
    double solarGain;                                   // solar gain Watts
    double albedo = 0.3;                                // mean Earth albedo
    double secondsPerYear = 3.154e+7;
    double iceDepth = 0;
    double iceWaterDepth = 0;
    boolean printFlag = true;
    double surfaceArea;                                 // surface area of asteroid
    double mass;                                        // mass of asteroid (earth 5.972 × 10^24 kg)
    double atmosphereMass;                              // (earth 5.1480×10^18 kg)
    double topOfAtmosphere;
    int topLayerOfAtmosphere;
    double surfaceGravity = 9.81;                       // surface gravitaional acceleration (should be calculated)

    Asteroid() { }

    Asteroid( Glaciation a ) {
        ap = a;
        int i, n = 0;
        double layerTemperature, layerTemperatureChange = 0;
        double wLayer, rLayer = 0;
        double mt, hLayer;
        double totalMass = 0;
        boolean atmosphereFound = false;
        ConductiveLayer layer;

        ACSalgebra();

//      solarGain = annualSolarInsolation / secondsPerYear;
        solarGain = 342.0;          // mean terrestrial solar radiation
        System.out.println( solarGain + "watts solar gain ");
        referenceRadius = layers[0][9];
        topOfAtmosphere = referenceRadius;
        this.surfaceArea = this.sphereSurfaceArea( referenceRadius );
        this.nlayers = 0;

        // build asteroid from centre outwards
        while ( layers[n][0] >= 0 ) {
            // initialise with linear temperature changes
            if ( n > 0 ) {
                layerTemperatureChange = ( layers[n][5] - layers[n-1][5] ) / layers[n][0];
                layerTemperature = layers[n-1][5] + layerTemperatureChange;
            } else {
                layerTemperature = layers[n][5];        // core temperature
            }
            System.out.println( (n) + " layerTemperatureChange " + layerTemperatureChange );
            System.out.println( layers[n][0] );
            hLayer = layers[n][1];
//          initialTemperatureK = layerTemperature;
            for ( i=0; i<layers[n][0]; i++ ) {
                rLayer = rLayer + hLayer;
                wLayer = rLayer / referenceRadius;          // layer width assumes top surface layer width = 1 metre
                mt = layers[n][6];
//              if ( mt == 3.0 && layerTemperature > 273 ) mt = 4.0;
//              if ( mt == 4.0 && layerTemperature < 273 ) mt = 3.0;  // no longer using 4 for water
                //                           radius. width,  height,  temp,               material index
                layer = new ConductiveLayer( rLayer, wLayer, hLayer, layerTemperature, mt, 0 );
                llayer.addFirst( layer );
                this.nlayers++;
                layerTemperature += layerTemperatureChange;


                if ( mt == 5  ) {
                    topOfAtmosphere += layer.height;
                    topLayerOfAtmosphere = layer.nBand;
                    // single layer atmosphere is first air mass found
                    if ( !atmosphereFound ) {
                    System.out.println( "atmosphere mass " + (layer.mass * this.surfaceArea) + " Kg asteroid mass " + (totalMass * this.surfaceArea) + " Kg" );
                    atmosphereFound = true;
                    }
                }
                totalMass += layer.mass;

            }
            n++;
        }
        this.nominalRadius = rLayer;

        reSynchronise();


        setGeothermalGradient( 289, 25.0, 50.0 );
        reSynchronise();

        setExponentialTemperatures( 900, 6000, 426 );

        this.setLayerPhases();

        printAsteroid( 0 );

        printAsteroidRC( 0 );


//      for ( n=1; n<=10; n++ ) {
//          System.out.println( n + ": " + exrponentialTemperature( 6000, (double)( n * 150 ), 20     ) );
//      }
    }


    // variant of above that uses table layer heights and densities to find layer masses
    Asteroid( Glaciation a, int j ) {
        ap = a;
        int i, n = 0;
        double layerTemperature, layerTemperatureChange = 0;
        double wLayer, rLayer = 0;
        double mt, hLayer;
        double totalMass = 0;
        boolean atmosphereFound = false;
        ConductiveLayer layer;

        ACSalgebra();

//      solarGain = annualSolarInsolation / secondsPerYear;
        solarGain = 342.0;          // mean terrestrial solar radiation
        System.out.println( solarGain + "watts solar gain ");
        referenceRadius = layers[0][9];
        topOfAtmosphere = referenceRadius;
        this.surfaceArea = this.sphereSurfaceArea( referenceRadius );
        this.nlayers = 0;

        // build asteroid from centre outwards
        while ( layers[n][0] >= 0 ) {
            // initialise with linear temperature changes over multiple layers with same temperature
            if ( n > 0 ) {
                layerTemperatureChange = ( layers[n][5] - layers[n-1][5] ) / layers[n][0];
                layerTemperature = layers[n-1][5] + layerTemperatureChange;
            } else {
                layerTemperature = layers[n][5];        // core temperature
            }
            System.out.println( (n) + " layerTemperatureChange " + layerTemperatureChange );
            System.out.println( layers[n][0] );
            hLayer = layers[n][1];
//          initialTemperatureK = layerTemperature;
            for ( i=0; i<layers[n][0]; i++ ) {
                rLayer = rLayer + hLayer;
                wLayer = rLayer / referenceRadius;          // layer width assumes top surface layer width = 1 metre
                mt = layers[n][6];
//              if ( mt == 3.0 && layerTemperature > 273 ) mt = 4.0;
//              if ( mt == 4.0 && layerTemperature < 273 ) mt = 3.0;  // no longer using 4 for water
                //                           radius. width,  height,  temp,               material index
                layer = new ConductiveLayer( rLayer, wLayer, hLayer, layerTemperature, mt, 0 );
                llayer.addFirst( layer );
                this.nlayers++;
                layerTemperature += layerTemperatureChange;


                if ( mt == 5  ) {
                    topOfAtmosphere += layer.height;
                    topLayerOfAtmosphere = layer.nBand;
                    // single layer atmosphere is first air mass found
                    if ( !atmosphereFound ) {
                    System.out.println( "atmosphere mass " + (layer.mass * this.surfaceArea) + " Kg asteroid mass " + (totalMass * this.surfaceArea) + " Kg" );
                    atmosphereFound = true;
                    }
                }
                totalMass += layer.mass;

            }
            n++;
        }
        this.nominalRadius = rLayer;

        reSynchronise();

        // set 25 degree/km temperature gradient to depth 50 km
        setGeothermalGradient( 289, 25.0, 50.0 );
        reSynchronise();

        setExponentialTemperatures( 900, 6000, 426 );

        this.setLayerPhases();

        printAsteroid( 0 );

        printAsteroidRC( 0 );

        System.out.println( "entire asteroid mass " + this.massOfEntireAsteroid() );

        printWholeAsteroid();

        redimensionLayers();

//      findNonTaperingLayerPressures( 1.0 );

//      for ( n=1; n<=10; n++ ) {
//          System.out.println( n + ": " + exrponentialTemperature( 6000, (double)( n * 150 ), 20     ) );
//      }
    }

    // construct a spherical asteroid of radius r with nlayers from material mt
    Asteroid( Glaciation a, double radius, int numLayers, double mt, double coreTemperatureK, double surfaceTemperatureK ) {
        int n;
        double rLayer, hLayer, wLayer, lastrLayer;
        ConductiveLayer layer;
        double layerTemperatureK = coreTemperatureK;
        double tstep = ( coreTemperatureK - surfaceTemperatureK ) / (double)(numLayers-1);
        double radiusLayer0 = equalLayerVolumeSphere( numLayers, radius );
        double volumeLayer0 = sphereVolume( radiusLayer0 );
        ap = a;

        rLayer = 0;
        lastrLayer = 0;
        for ( n=0; n<numLayers; n++ ) {
            rLayer = radiusConstantVolumeLayer( n, volumeLayer0 );
            hLayer = rLayer - lastrLayer;
            wLayer = rLayer / radius;          // layer width assumes top surface layer width = 1 metre
            layer = new ConductiveLayer( rLayer, wLayer, hLayer, layerTemperatureK, mt, 0 );
//          layer.volume = volumeLayer0;  // overwrite calculated volume
            llayer.addFirst( layer );
            layerTemperatureK -= tstep;
            lastrLayer = rLayer;
        }
        this.nominalRadius = rLayer;
        this.referenceRadius = this.nominalRadius;
        System.out.println( "referenceRadius " + referenceRadius + " nominalRadius " + nominalRadius);
        System.out.println( "asteroid slice mass " + this.massOfAllLayers() );
        System.out.println( "asteroid mass " + this.massOfEntireAsteroid() );

        this.setLayerTemperatures( temps1, 1.00 );

        // add ice or water
        rLayer += 20;
        hLayer = rLayer - lastrLayer;
        wLayer = rLayer / radius;          // layer width assumes top surface layer width = 1 metre
        layer = new ConductiveLayer( rLayer, wLayer, hLayer, 270, 3, 0 );
        llayer.addFirst( layer );


        // dummy top layer
        rLayer += 1000;                  //  air
        hLayer = rLayer - lastrLayer;
        wLayer = rLayer / radius;          // layer width assumes top surface layer width = 1 metre
        layer = new ConductiveLayer( rLayer, wLayer, hLayer, 276, 5, 0 );
        llayer.addFirst( layer );

        reSynchronise();

        System.out.println( "this.surfaceRockBand " + this.surfaceRockBand);
//      addLayer( this.surfaceRockBand+1, 273, 0, 0);
        addLayer( 30+1, 273, 0, 0);

        reSynchronise();
        System.out.println( "meanAsteroidTemperature " + meanAsteroidTemperature() );
        System.out.println( "constant layer volume asteroid complete " );
        printAsteroid( 0 );

        // divide 561.839 m top rock layer into 5 layers each 112.267 m high
//      divideLayerIntoMultipleLayers( 29, 5 );
        // divide 1129 m top rock layer into 7 layers each 16 m high
        // equal in resistance to 25 m ice
//      divideLayerIntoMultipleLayers( 33, 7 );


//      recalculateRadii( 0 );
//      recalculateRadii( 0 );
//      System.out.println( "radii recalculated " );

        this.setLayerPhases();

        System.out.println( "ice index 100K " + layer.material.indexIceTemperature( 100 ) );
        System.out.println( "ice index 150K " + layer.material.indexIceTemperature( 150 ) );
        System.out.println( "ice index 200K " + layer.material.indexIceTemperature( 200 ) );

        printAsteroidRC( 0 );

    }


    // find all condutctive heat flows
    // using link list iterator
    void getNewLayerTemperatures( double interval, double y, double snowStartYear, double snowDepthPerYear  ) {
        int removeLayer = -1;
        this.year = y;
        int n, bmax, bmin, i;
        boolean flag = false;
        double heatflowrate, heatgain, dtemp, tmax, tmin, hfr, effectiveTemperature;
        ConductiveLayer lyr[] = new ConductiveLayer[3];
        int counter = 0;
        ListIterator<ConductiveLayer> itr = null;

        // get the top 3 layers
        // layer 0 is a dummy floating layer
        heatflowrate = 0.0;
        itr = llayer.listIterator();
        if ( itr.hasNext() ) {
            lyr[0] = itr.next();
            counter++;
        }

        // layer 1 is a layer of air
        if ( itr.hasNext() ) {
            lyr[1] = itr.next();
            counter++;
        }

        // layer 2 is either a layer of water or ice or rock
        // lyr[] contains layer[n-1], layer[n], layer[n+1] for all layers
        while(itr.hasNext()){
            lyr[2] = itr.next();
            counter++;

            // here come the heat flow calculations
            heatflowrate = 0.0;
            lyr[0].layerResistance();
            lyr[1].layerResistance();
            lyr[2].layerResistance();

            if ( lyr[1].materialType == 0 ) {

                solarGain = ap.sr.getNextSolarGain() / interval;

                // if thin surface layer albedo < 0, use albedo of layer below
                this.albedo = Math.abs( lyr[1].material.albedo );
                if ( lyr[1].material.albedo < 0 ) {
                    this.albedo = Math.abs( lyr[2].material.albedo );
                }

                // surface layer coducts heat to layer below, and radiates to layer above
                // iterative solution for radiative surface temperature Ts
                double tparam;
                double newsense = 1.0;
                double oldsense = 1.0;
                double tstep = 10.0;
                int timeout = 100;
                double ts = lyr[1].currentTemperatureK;
                double difference = -surfaceHeatFlowRate( lyr[0], lyr[1], lyr[2], lyr[1].currentTemperatureK );
                if ( difference >= 0.0  ) { newsense = -1.0; } else { newsense = 1.0; }
                oldsense = newsense;

                while ( Math.abs( difference ) > 0.01 ) {
                    ts += tstep * oldsense;
                    tparam = ts;
                    difference = -surfaceHeatFlowRate( lyr[0], lyr[1], lyr[2], tparam );
//                  System.out.println( "difference " + difference + " oldsense " + (int)oldsense + " tstep " + tstep + " ts " + ts );
//                  if ( printFlag ) System.out.println( ts + " hfr " + difference + " step " + tstep );
                    if ( difference >= 0.0  ) { newsense = -1.0; } else { newsense = 1.0; }
                    if ( newsense != oldsense ) { oldsense = newsense; tstep = 0.1 * tstep;  }

                    if ( timeout-- < 0 ) {
                        difference = 0.0;
                        System.out.println( "surface iteration timeout " );
                        ts = lyr[1].currentTemperatureK;
                    }
                }
                this.lastSurfaceTemperature = ts;
                lyr[1].nextTemperatureK = ts;
                printFlag = false;

            } else if ( lyr[1].materialType == 5 ) {
               // ACS single layer atmosphere
                // radiative heat exchanges from atmospheric air
                heatflowrate = 0;
                // heat radiated from surface below
                heatflowrate += lyr[1].material.emissivity * wattsRadiated( lyr[2].currentTemperatureK, 1.0 );
                // heat radiated to surface below and to space
                heatflowrate -= 2.0 * wattsRadiated( lyr[1].currentTemperatureK, lyr[1].material.emissivity );
                // find heat gain over timestep
                heatgain = heatflowrate * interval;

                // find change in temperature due to heat gain.
                dtemp = heatgain / ( lyr[1].mass * lyr[1].layerSpecificHeat );
                lyr[1].nextTemperatureK = lyr[1].currentTemperatureK + dtemp;

            } else if ( lyr[1].materialType > 0 ) {
                // while layer phase change is in process, currentTemperatureK is fixed, and
                // phaseTemperature is used instead until phase change is complete
                if ( lyr[1].phaseChange ) {
                    effectiveTemperature = lyr[1].pseudoTemperatureK;
                } else {
                    effectiveTemperature = lyr[1].currentTemperatureK;
                }

                // conductive heat transfer
                // heat flow rate rate from higher layer 0 to layer 1
                heatflowrate += conductiveHeatFlow( lyr[1], lyr[0].layerResistance, lyr[1].layerResistance, lyr[0].currentTemperatureK, effectiveTemperature );
                lyr[1].upwardHeatFlowRate = heatflowrate;
                if ( lyr[1].nBand == this.surfaceRockBand ) this.surfaceRockHeatFlowRate = heatflowrate;

                // heat flow rate from lower layer layer 2 to layer 1
                heatflowrate += conductiveHeatFlow( lyr[1], lyr[2].layerResistance, lyr[1].layerResistance, lyr[2].currentTemperatureK, effectiveTemperature );
                heatflowrate += lyr[1].heatProduced;      // radioactive heat production in layer

                // find heat gain over timestep
                heatgain = heatflowrate * interval;

                // find change in temperature due to heat gain.
                dtemp = heatgain / ( lyr[1].mass * lyr[1].layerSpecificHeat );

                // set the next layer temperature that will replace current layer tmperature
                // during a phase change, hold next temperature constant, while varying phase temperature
                if ( lyr[1].phaseChange ) {
                    lyr[1].pseudoTemperatureK = lyr[1].pseudoTemperatureK + dtemp;
                    lyr[1].nextTemperatureK = lyr[1].phaseTemperatureK;
                } else {
                    lyr[1].nextTemperatureK = lyr[1].currentTemperatureK + dtemp;
                }

                // catch any phase change btween current temperature and next temperature
                lyr[1].phaseSum = phaseCheck( lyr[1].phaseSum, lyr[1], lyr[1].currentTemperatureK, lyr[1].nextTemperatureK  );

            }

            // roll layers up
            lyr[0] = lyr[1];
            lyr[1] = lyr[2];
        }


        // now copy next temperatures to current temperatures
        // and perform other housekeeping tasks
        double asteroidRadius = 0;
        counter = 0;
        itr=llayer.listIterator();
        while(itr.hasNext()){
            lyr[0] = itr.next();

            // set current temperature of layer
            lyr[0].previousTemperaureK = lyr[0].currentTemperatureK;
            lyr[0].currentTemperatureK = lyr[0].nextTemperatureK;
            if ( lyr[0].nBand  == 30 && ap.tick % 1000 == 0 ) {
                if ( lyr[0].currentTemperatureK > 270 && lyr[0].currentTemperatureK < 280 ) {
//                  System.out.println( "tick " + ap.longPeriodIndex + " " + lyr[0].phaseChange +  " - " + lyr[0].currentTemperatureK + ", " + lyr[0].layerSpecificHeat );
                }
            }

            if ( lyr[0].phaseHasChanged ) {
                double mtrl = lyr[0].materialType;
                mtrl = mtrl + ( lyr[0].phase / 10.0 );  // 3.0 is ice, 3.1 is water, 3.2 is steam
                lyr[0].material.setMaterialCharacteristics( mtrl );
//              System.out.println( "band " + lyr[0].nBand + " phase has become " + mtrl );
                lyr[0].phaseHasChanged = false;
            }

            // ice thermal characteristics vary with temperature (not during phase changes)
            if ( lyr[0].material.isIce( lyr[0].currentTemperatureK) && !lyr[0].phaseChange ) {
                i = lyr[0].material.indexIceTemperature( lyr[0].currentTemperatureK );
                lyr[0].layerDensity = lyr[0].material.iceCharacteristics[i][1];
                lyr[0].layerConductivity = lyr[0].material.iceCharacteristics[i][2];
                lyr[0].layerSpecificHeat = lyr[0].material.iceCharacteristics[i][3] * 1000.0;
                lyr[0].layerResistance = lyr[0].layerResistance();
            }

            // find temperature of lowest atmosphere layer
            if  ( lyr[0].material.isAir( lyr[0].currentTemperatureK ) ) {
                this.atmosphereTemperature = lyr[0].currentTemperatureK;
            }

            // set surface rock temperature.
            if ( lyr[0].nBand == this.surfaceRockBand ) {
                surfaceRockTemperature = lyr[0].currentTemperatureK;
            }

            // set radiative surface layer temperature
            if ( lyr[0].nBand == this.thinSurfaceBand ) {
                thinSurfaceBandTemperature = lyr[0].currentTemperatureK;
//              System.out.println( lyr[0].nBand + ": " + lyr[0].currentTemperatureK );
            }

            // set lowest atnospheric layer remperature
            if ( lyr[0].nBand == this.airLowestBand ) {
                this.airLowestBandTemperature = lyr[0].currentTemperatureK;
//              System.out.println( lyr[0].nBand + ": " + lyr[0].currentTemperatureK );
            }

            counter++;
        }
        nIterations = counter;

        // new asteroid radius (if new layers have been added to it )
        this.radius = asteroidRadius();


        // contiguous water layers are assumed to have same temperature (due to good convective mixing)
//      equaliseContiguousWaterLayerTemperatures();

        removeAllMaterialTypeLayersAboveT( 4, 273.0001 );
        removeAllMaterialTypeLayersAboveT( 3, 273.0001 );

        // remove any layer that's been flagged
//      removeLayer( removeLayer );

        if ( resynchronise ) reSynchronise();
    }

    // radiative heat transfers in thin surface layer
    // ACS single layer atmosphere
    double surfaceHeatFlowRate( ConductiveLayer layer0, ConductiveLayer layer1, ConductiveLayer layer2, double ts ) {
        double heatflowrate = 0;
        // minus for heat gained by surface, plus for heat lost by surface
        // plus conducted heat to layer below
        // plus radiated heat to layer above
//      heatflowrate += wattsRadiated( ts, layer1.material.emissivity );
        heatflowrate -= wattsRadiated( ts, 1.0 );
//      if ( printFlag ) System.out.println( "hfr2 " + heatflowrate );

        //  radiated heat from air in layer above
        heatflowrate += wattsRadiated( layer0.currentTemperatureK, layer0.material.emissivity );
//      if ( printFlag ) System.out.println( "hfr3 " + heatflowrate );

        // heat loss to space is negative
        layer1.upwardHeatFlowRate = heatflowrate;

        heatflowrate += wattsConducted( ts, layer2.currentTemperatureK, layer2.layerConductivity, layer2.height/2.0, layer2.area );
//      if ( printFlag ) System.out.println( "hfr1 " + heatflowrate );
        // minus solar irradiation
        heatflowrate += this.solarGain * ( 1.0 - this.albedo );       //
//      heatflowrate += this.solarGain * ( 1.0 - 0.05 );
//      if ( printFlag ) System.out.println( "hfr4 " + heatflowrate );

        return( heatflowrate );
    }

/*** Boltzmann *******************************************************************************/
    // Boltzmann code iteratively finds surface temperature where where
    //   a) there is a solar heat gain
    //   b) a radiative heat loss
    //   c) a conductive heat loss into material beneath surface

    // calculate surface heat losses by conduction and re-radiation
    double exFlux( double e, double tsurface, double td, double k, double d, double a ) {
        double conductedHeat, radiatedHeat;
        conductedHeat = wattsConducted( tsurface, td, k, d, a);   // heat conducted
        radiatedHeat = wattsRadiated( tsurface, e );              // stefan-Boltzmann law
    return( conductedHeat + radiatedHeat );
    }

    // Boltzmann
    double wattsConducted( double t1, double t2, double k, double d, double a ) {
        double watts =  ( t1 - t2 ) * k * a / d;
        return( watts );
    }

    // Boltzmann
    double wattsRadiated( double t, double e ) {
        double s = 5.6699E-8;                                     // Stefan's constant
        double watts = e * s * Math.pow( t, 4.0 );
        return( watts );
    }

    // StefanÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œBoltzmann radiation law
    double stefanBoltzmannHeatflow( double emissivity, double temperatureK ) {
        double watts = 0;
        double sigma = 5.670373E-8;
        watts = emissivity * sigma * Math.pow(temperatureK, 4.0);  // StefanÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œBoltzmann constant W m^-2 K^-4
        return( watts );
    }

    // Inverse StefanÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œBoltzmann radiation law
    double stefanBoltzmannTemperature( double emissivity, double watts ) {
        double temperature = 0;
        double sigma = 5.670373E-8;
        temperature = Math.pow( watts / (emissivity * sigma), 0.25 );  // StefanÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œBoltzmann constant W m^-2 K^-4
        return( temperature );
    }

    // Boltzmann
    // successive approximation for Tsurface used to match heat influx to within 0.01 of heat exflux
    double surfaceHeatFlow( double influx, double lastSurfaceTemp, double e, double td, double k, double d, double a ) {
        double difference = 100.0;
        double newsense = 1.0;
        double oldsense = 1.0;
        double tstep = 10.0;
        int timeout = 100;
        double conductedHeat;
        boolean flag = false;
        double ts = lastSurfaceTemp;
        double exflux = exFlux( e, ts, td, k, d, a );

        difference = exflux - influx;
        if ( difference >= 0.0  ) { newsense = -1.0; } else { newsense = 1.0; }
        oldsense = newsense;

//      if ( ap.year > 6013.2 && ap.year < 6013.25 ) {
//          flag = true;
//          System.out.println( ap.year + "****** " + ts + ", " + td );
//      }

        while ( Math.abs( difference ) > 0.01 ) {

            ts += tstep * oldsense;
            difference = exFlux( e, ts, td, k, d, a ) - influx;
            if ( difference >= 0.0  ) { newsense = -1.0; } else { newsense = 1.0; }
            if ( newsense != oldsense ) { oldsense = newsense; tstep = 0.1 * tstep;  }

            if ( timeout-- < 0 ) {
                difference = 0.0;
                System.out.println( "Timeout year " );
                ts = lastSurfaceTemp;
            }

        }

        this.lastSurfaceTemperature = ts;
        conductedHeat = wattsConducted( ts, td, k, d, a);   // heat conducted
//      System.out.println( ap.year + ": " + this.lastSurfaceTemperature + " " + conductedHeat );

        return( conductedHeat );

    }

/*** end Boltzmann *****************************************************************************/


    // calculate conduction heat flow rate (Watts)
    // k = conductivity, a = area, d = depth, t1-t2 temp difference
    double conductiveHeatFlow( ConductiveLayer l1, double k, double a, double d, double t1, double t2 ) {
        double heatflowrate = (t1 - t2) * k * a / d;
//      if ( l1.nBand == 30 && l1.materialType == 3 ) System.out.println( "k a d R t1 t2 hfr " + (float)k + ", " + (float)a + ", " + (float)d + ", " + (float)(d/(k*a)) + ", " + (float)t1 + ", " + (float)t2 + ", " + (float)heatflowrate );
        return ( heatflowrate );
    }

    // calculate conduction heat flow rate (Watts) between layer centres
    // thermal resistance between layers = r1/2 + r2/2
    double conductiveHeatFlow( ConductiveLayer l1, double r1, double r2, double t1, double t2 ) {
        double rm = ( r1 + r2 ) / 2.0;
//      System.out.println( "rm " + rm + " r1 " + r1 + " r2 " + r2 );
        double heatflowrate = (t1 - t2) / rm;
 //     if ( l1.nBand == 30 && l1.materialType == 3 ) System.out.println( "k r1 rm t1 t2 hfr " + (float)l1.layerConductivity + ", " + (float)r1 + ", " + (float)rm + ", " + (float)t1 + ", " + (float)t2 + ", " + (float)heatflowrate  );
//      System.out.println( "rm " + rm + " hfr " + heatflowrate  );
        return ( heatflowrate );
    }

    // from https://en.wikipedia.org/wiki/Planck%27s_law
    // freq = frequency, 1/s
    double planckBlackBodyFunction( double tK, double freq ) {
        double B = 0;                       // spectral radiance
        double h = 6.626070040E-34;         // Planck constant Js
        double c = 299792458;               // velocity of light in vacuum or air m/s
        double kB = 1.38064852E-23;         // Boltzmann constant J/K
        double f1 = ( 2.0 * h * freq * freq * freq ) / ( c * c );
        double f2 = ( h * freq ) / ( kB * tK );
        B = f1 / ( Math.pow( Math.E, f2 ) - 1.0 );
        return( B );
    }

    // source https://www.acs.org/content/acs/en/climatescience/atmosphericwarming/singlelayermodel.html
    // (incoming) (1 – a)Save = (1 – e)sTp4 + esTa4 (outgoing) . . . . . . . . .(1)
    // (absorbed) esTp4 = 2 esTa4 (emitted) . . . . . . . . . . . . . . . . . . (2)
    // Ta4 = (1/2) Tp4 . . . . . . . . . . . . . . . . . . . . . . . . . . . . .(3)
    // We can substitute for Ta4 in the planetary balance equation (1) and solve for Tp
    // Tp = {[2 (1 – a)Save]/ [s(2 – e)]}1/4
    // tested: results are same as in ACS
    void ACSalgebra() {
        double save = 342.0;                    // W/m2 mean solar radiation
        double albedo = 0.3;                    // earth mean albedo
        double sigma = 5.67E-8;                 // Stefan-Boltzmann constant, 5.67·10–8 W·m–2·K–4
        double emissivity, ta;
        System.out.println( "ACS single layer atmosphere" );
        for ( emissivity=0; emissivity<=1.0; emissivity += 0.1 ) {
            double tp = Math.pow ( ( 2.0 * (1.0 - albedo) * save ) / ( sigma * ( 2.0 - emissivity ) ), 0.25 );
            ta = tp * Math.pow( 0.5, 0.25 );
            System.out.println( "air emissivyty " + (float)emissivity + " surface temperature " + (float)tp + " air temperature " + (float)ta );
        }
    }

    void setExponentialTemperatures( double rc0, double tf, double ttarget ) {
        double ds = layerTemperatureDepth( ttarget );
        double d0 =  Math.log( tf - ttarget ) / tf;
        System.out.println( ds + " " + d0  );

        double tlayer, dl;
        boolean found = false;
        double depth = 0;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();

        // find thin surface layer, and measure depth from there
        while( itr.hasNext() ){
            layer = itr.next();
            if ( layer.materialType == 0 ) {
                depth = 0;
            } else {
                depth += layer.height / 2.0;
                if ( depth == ds ) {
                    found = true;
                    break;
                } else {
                    depth += layer.height / 2.0;
                }
            }
        }

        if ( found ) {
            while( itr.hasNext() ){
                layer = itr.next();
                depth += layer.height / 2.0;
                dl = depth - ds + d0;
                tlayer = exrponentialTemperature( tf, rc0, dl );
                System.out.println( "depth " + depth + " temperature " + tlayer );
                layer.currentTemperatureK = tlayer;
                layer.nextTemperatureK = tlayer;
                depth += layer.height / 2.0;
            }
        }


    }


    // function to generate a temperature approaching tmax at given depth (km)
    // suggested value for rc time constant = 1000. Smaller values raise temperatures, larger lower them.
    double exrponentialTemperature( double tmax, double rc, double depth  ) {
        return ( tmax - tmax * Math.pow ( Math.E, -depth/rc ) );
    }

    // find the depth below thin surface layer of layer at targetTemperature
    double layerTemperatureDepth( double targetTemperature ) {
        boolean found = false;
        double depth = 0;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();

        // find thin surface layer, and measure depth from there
        while( itr.hasNext() ){
            layer = itr.next();
            if ( layer.materialType == 0 ) {
                depth = 0;
            } else {
                depth += layer.height / 2.0;
                if ( layer.currentTemperatureK >= targetTemperature ) {
                    break;
                } else {
                    depth += layer.height / 2.0;
                }
            }
        }

        return( depth );
    }


    double volumeAtmosphere( double massAtmosphere, double densityAtmosphere ) {
        double volume = 0;
        volume = massAtmosphere / densityAtmosphere;
        return( volume );
    }

    double heightAtmosphere( double volumeAtmosphere ) {
        double h = 0;
        double volumeAsteroid = this.sphereVolume( this.referenceRadius );
        return( h );
    }

    double volumeOneSquareMetre( ) {
        double volume = 0;
        return( volume );
    }

    void setGeothermalGradient( double tThinSurface, double degreesPerKm, double maxDepth ) {
        boolean found = false;
        double depth;
        double thinSurfaceRadius = 0;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();

        // find thin surface layer
        while( itr.hasNext() ){
            layer = itr.next();
            thinSurfaceRadius = layer.radius;
            if ( layer.materialType == 0 ) {
                layer.currentTemperatureK = tThinSurface;
                layer.nextTemperatureK = tThinSurface;
                found = true;
                break;
            }
        }

        if ( found ) {
            while( itr.hasNext() ) {
                layer = itr.next();
                depth = thinSurfaceRadius - layer.radius;
                depth += layer.height / 2.0;                                        // depth of middle of layer'
                depth = depth / 1000.0;                                             // depth in km
                if ( depth <= maxDepth ) {
                    layer.currentTemperatureK = tThinSurface + depth * degreesPerKm;
                    layer.nextTemperatureK = layer.currentTemperatureK;
                } else {
                    break;
                }
            }
        } else {
            System.out.println( "thin surface layer not found " );
        }

    }

    // add up all the layer masses
    double massOfAllLayers() {
        double totalMass = 0;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();

        while( itr.hasNext() ){
            layer = itr.next();
            totalMass += layer.mass;
        }
        return( totalMass );
    }

    double massOfEntireAsteroid() {
        double totalMass = 0;
        double massSlice = this.massOfAllLayers();
        double asteroidSurfaceArea = this.sphereSurfaceArea( this.referenceRadius );
        totalMass = massSlice * asteroidSurfaceArea;
        return( totalMass );
    }

    // assumes only one set of contiguous layers of water
    double equaliseContiguousWaterLayerTemperatures() {
        int n = 0;
        double tmean;
        double v = 0;
        double vt = 0;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;

        itr=llayer.listIterator();
        while(itr.hasNext()){
            layer = itr.next();
            if ( layer.material.isWater( layer.currentTemperatureK) ) {
                v += layer.volume;
                vt += layer.currentTemperatureK * layer.volume;
                n++;
            }
        }
        tmean = vt / v;

        if ( n > 1 ) {
            itr=llayer.listIterator();
            while(itr.hasNext()){
                layer = itr.next();
                if ( layer.material.isWater( layer.currentTemperatureK) ) {
                    layer.currentTemperatureK = tmean;
                }
            }
        }

        return( tmean );
    }

    void setLayerPhases() {
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();

        while( itr.hasNext() ){
            layer = itr.next();
            layer.phase = layer.setLayerPhase();
            if ( layer.nBand == 30 ) System.out.println( "layer 30 phase " + layer.phase );
        }
    }



    // checks if a phase change temperature is being crossed
    // if so, changes materialType in layer, and thermal characteristics
    // phase change takes place over a 1 degree K range
    double phaseCheck( double lastsum, ConductiveLayer layer, double t1, double t2 ) {
        int lowPhase = 0;    // solid
        int highPhase = 1;   // liquid
        double t = t2;
        double dtemp;

        // find relevant phase temperauure
        // Every phase change is from one low phase to a higher phase or back
        layer.phaseTemperatureK = layer.material.phaseTemperature1;
        if ( Math.abs( t1 - layer.material.phaseTemperature1 ) > Math.abs( t1 - layer.material.phaseTemperature2 )  ) {
            layer.phaseTemperatureK = layer.material.phaseTemperature2;
            lowPhase = 1;   // liquid
            highPhase = 2;  // vapour
        }

        double lowerPhaseTemp = layer.phaseTemperatureK;
        double upperPhaseTemp = layer.phaseTemperatureK;  // was pt1 + 1.0
        double sl1 = Math.signum( t1 - lowerPhaseTemp );
        double su1 = Math.signum( t1 - upperPhaseTemp );
        double sl2 = Math.signum( t2 - lowerPhaseTemp );
        double su2 = Math.signum( t2 - upperPhaseTemp );
        double sum = sl1 + su1 + sl2 + su2;

        if ( sum == 0 ) {
            // phase change
            if ( !layer.phaseChange ) {
                // start phase change
                layer.pseudoTemperatureK = layer.phaseTemperatureK;
                layer.nextTemperatureK = layer.phaseTemperatureK;
                layer.layerSpecificHeat = layer.material.latentHeatOfFusion;
                layer.phaseChange = true;
            } else {
                // phase change in process
                layer.layerSpecificHeat = layer.material.latentHeatOfFusion;
                layer.nextTemperatureK = layer.phaseTemperatureK;
                layer.phaseChange = true;
                layer.phaseHasChanged = false;

                // temperature of layer is locked to phase temperature during phase cahnge
                // pseudoTemperature needs to rise or fall 1 degree K to complete phase change
                // because specific heat has been replaced by latent heat
                if ( layer.pseudoTemperatureK > layer.phaseTemperatureK + 1.0   ) {
                    layer.phaseChange = false;  // phase has changed, so no more phase change
                    layer.phase = highPhase;    // now water or vapour
                    layer.phaseHasChanged = true;
                    if ( layer.phase == 2 ) System.out.println( "layer " + layer.nBand + " vapour phase");
                } else if ( layer.pseudoTemperatureK < layer.phaseTemperatureK - 1.0 ) {
                    layer.phaseChange = false;  // phase has changed, so no more phase change
                    layer.phase = lowPhase;     // now ice or water
                    layer.phaseHasChanged = true;
                }
            }
        }
        return( sum );
    }


    // Measures ice presence above rock layers
    // Called with control = 0, each period during which ice (mt 3 ) is present is added to total
    // Called with ccontrol = 1, total/period is printed out, and total reset
    // assumes constnnt period
    double measureDegreeOfGlaciation( double gtotal, double period, double timestep, int control ) {
        double degree = 0;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();

        this.iceDepth = 0;
        while( itr.hasNext() ){
            layer = itr.next();
            // record glaciations
            if ( layer.material.isIce( layer.currentTemperatureK) ) {
                degree = timestep;
                iceDepth += layer.height;
            }
            // quit when rock or iron reached
            if ( layer.materialType == 2 || layer.materialType == 1 ) {
                break;
            }
        }
//      System.out.println( degree + " " + gtotal );
        gtotal += degree;
        if ( control == 1 ) {
//          System.out.println( "% glaciation " + ( 100.0 * gtotal / period ) + " over period "+ period );
//          gtotal = 0;
        }
        return( gtotal );
    }

/*
    double phaseChange( ConductiveLayer layer, double t1, double t2 ) {
        double t = t2;
        return( t );
    }
*/
    // set radioactive heat produced in all layers of material type mt
    void setRadioactiveHeatGeneration( double mt, double wattsPerCubicMetre ) {
        double totalHeatProduced = 0;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();

        while( itr.hasNext() ){
            layer = itr.next();
            if ( layer.materialType == mt  ) {
                layer.heatProduced = layer.volume * wattsPerCubicMetre;
                totalHeatProduced += layer.heatProduced;
            }
        }
        System.out.println( "total radioactive heat produced " + totalHeatProduced  + " W/m^3");
    }

    // using an array of temperatures, set layer temperatures from top down
    void setLayerTemperatures( double temps[], double multiplier ) {
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();

        int i = 0;
        while( itr.hasNext() ){
            layer = itr.next();
            layer.currentTemperatureK = temps[i++] * multiplier;
        }
    }

/*
    // when ice melts, the franite particles in it are freed, and fall to the contre
    // of the asteroid to form a sphere of solid granite
    double meltedVolume() {
        double volume = 1.3333333 * Math.PI * Math.pow( meltRadius, 3.0 );
        return( volume );
    }
*/

/*
    // water sphere encompasses granite sphere
    double waterSphereRadius( double vWater, double vGranite ) {
        // sphere volume = 4/3 .pi. r^3
        double fn = ( (vGranite + vWater) * 3.0 ) / ( 4.0 * Math.PI );
        double r = Math.pow( fn, 0.33333333 );
        return( r );
    }

    double heatStoredinBands( int b1, int b2 ) {
        double joules = 0;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();

        // seems that list iterator can only go back to revious elemnts if it's been forrward to next elemnts.
        while( itr.hasNext() ){
            layer = itr.next();
            if ( layer.nBand >= b1 && layer.nBand <= b2 )
            joules = ( Math.abs( layer.storedTemperature[0] - layer.storedTemperature[1]) * layer.mass * layer.specificHeatGranite );
        }
        return( joules );
    }
*/
    // recalculate all the radii of layers above startband,
    // using layer volume to find new heights and radii
    void recalculateRadii( int startBand ) {
        int count = 0;
        boolean started = false;
        double subsphereRadius, subsphereVolume, subsphereArea, subsphereSliceWidth, sliceFraction, layerVolume;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        subsphereRadius = this.referenceRadius;
        subsphereArea = this.sphereSurfaceArea( subsphereRadius );
        subsphereVolume = this.sphereVolume( subsphereRadius );
        sliceFraction = 1.0 / subsphereArea;

        // first go down the layers to the start layer
        itr = llayer.listIterator();
        while(itr.hasNext()){
            layer = itr.next();
            if ( layer.nBand == startBand ) {
                started = true;
                break;
            }
        }

        if ( itr.hasPrevious() ){
            layer = itr.previous();
            subsphereRadius = layer.radius;
            subsphereVolume = this.sphereVolume( subsphereRadius );
            subsphereArea = this.sphereSurfaceArea( subsphereRadius );
            subsphereSliceWidth = layer.width;
            sliceFraction = subsphereSliceWidth * subsphereSliceWidth / subsphereArea;
            System.out.println( layer.nBand + "- " + layer.radius + " " + layer.width );
        }
        // go up the layers from the centre outwards to find the start band
        while( itr.hasPrevious() ){
            layer = itr.previous();
            if ( started ) {
                System.out.println( layer.nBand + ": " + layer.radius + " " + layer.width );
                layerVolume = layer.volume / sliceFraction;  //
                layer.radius = this.layerRadius( layerVolume, subsphereVolume );
                layer.height = layer.radius - subsphereRadius;
                layer.width = layer.radius / this.referenceRadius;
                layer.length = layer.radius / this.referenceRadius;
                System.out.println( layer.nBand + ": r " + layer.radius + " w " + layer.width + " v " + layer.volume);
            }
            subsphereRadius = layer.radius;
            subsphereVolume = this.sphereVolume( subsphereRadius );
            subsphereArea = this.sphereSurfaceArea( subsphereRadius );
            subsphereSliceWidth = layer.width;
            sliceFraction = subsphereSliceWidth * subsphereSliceWidth / subsphereArea;
        }
    }

    // divide a layer into multiple equal thickness layers
    void divideLayerIntoMultipleLayers( int layerBandNum, int numLayers ) {
        int n;
        boolean layerFound = false;
        double r, newLayerHeight;
        ConductiveLayer lyr = null;
        ConductiveLayer layer = null;
        ListIterator<ConductiveLayer> itr = null;

        System.out.println( "dividing layer " + layerBandNum + " into " + numLayers + " new layers.");

        // find required layer
        itr=llayer.listIterator();
        while(itr.hasNext()){
            layer = itr.next();
            if ( layer.nBand == layerBandNum ) {
                layerFound = true;
                break;
            }
        }

        if ( layerFound ) {
            // find new layer height of multiple bands
            newLayerHeight = layer.height / (double)numLayers;

            // reduce original layer height
            layer.height = newLayerHeight;
            r = layer.radius;
            layer.volume = layer.layerVolume(r, r-newLayerHeight, layer.width );
            layer.mass = layer.layerMass();
            layer.layerThermalCapacity = layer.layerThermalCapacity();
            layer.layerResistance = layer.layerResistance();
            layer.layerTimeConstant = layer.layerResistance * layer.layerThermalCapacity;
            System.out.println( "0 new layer voluume " + layer.volume );

            // add num:ayers-1 of same material underneath layer layerBandNum,
            // and reduce layer layerBandNum height to newLayerHeight.
            for ( n=1; n<numLayers; n++ ) {
                r = layer.radius - newLayerHeight;
                lyr =  new ConductiveLayer( r, layer.width, newLayerHeight, layer.currentTemperatureK, layer.materialType, 0 );
                itr.add( lyr );
                lyr.copyLayer( layer );
                System.out.println( n + " new layer voluume " + lyr.volume );
            }

            System.out.println( "new layers created");

            this.reSynchronise();

//          this.recalculateRadii( 0 );

//          this.printAsteroid( 0 );

        } else {
            System.out.println( "layer not found");
        }
    }


    // remover a single contiguous set of layers of material type1 (e.g. all ice layers in ice sheet )
    // for single layer removal, set both types to be the same type.
    int removeAllMaterialTypeLayersAboveT( int type1, double t ) {
        boolean layerRemoved =  false;
        int count = 0;
        int nremoved = 0;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;

        double heightRemoved = 0;
        itr=llayer.listIterator();
        while(itr.hasNext()){
            layer = itr.next();
            layerRemoved = false;
            if ( layer.materialType == type1 && layer.currentTemperatureK > t ) {
                itr.remove();
                heightRemoved += layer.height;
                nlayers--;
                nremoved++;
                layerRemoved = true;
//              System.out.println( "removed layer band " + (int)layer.nBand  + " at " + layer.currentTemperatureK + " K " );
            }   //
            count++;
//          if ( !layerRemoved && nremoved > 0  ) break;
        }

        // now go back up to surface, lowering radii of layers above
        if ( nremoved > 0 ) {
            layer = itr.previous();
            while( itr.hasPrevious() ){
                layer = itr.previous();
//              System.out.println( "lowered layer type " + (int)layer.materialType + " by " + heightRemoved );
                layer.radius -= heightRemoved;
                count--;
            }
        }

        if ( nremoved > 0 ) {
//          System.out.println( type1 + " number of bands " + setBandNumbers() + " nremoved " + nremoved );
        }
        reSynchronise();
        return( nremoved );
    }

    // add new layer at nBand nb, of material mt, temperature t.
    // adds UNDER layer nb
    void addLayer( int nb, double t, double h, double mt ) {
        double r, w;
        ConductiveLayer layer, lyr;
        ListIterator<ConductiveLayer> itr = null;
//      System.out.println( "add layer" + nb );

        itr=llayer.listIterator();
        while(itr.hasNext()){
            layer = itr.next();
            r = layer.radius;
            w = layer.width;
            if ( layer.nBand == nb ) {
                lyr =  new ConductiveLayer( r+h, w, h, t, mt, 0 );
                itr.add( lyr );
                System.out.println( ap.year + " added layer of material " + mt + " at band" + nb );
            }
        }

        reSynchronise();
    }

    // add new layer at nBand nb, of material mt, temperature t.
    // adds top layer nb
    void addTopLayer( double t, double h, double mt ) {
        double r, w;
        ConductiveLayer layer, lyr;
        ListIterator<ConductiveLayer> itr = null;
        itr = llayer.listIterator();
        if (itr.hasNext() ) {
            layer = itr.next();
            r = layer.radius;
            w = layer.width;
            layer =  new ConductiveLayer( r+h, w, h, t, mt, 0 );
            llayer.addFirst( layer );
        }

        reSynchronise();
    }

    // Add volume v at temperature t into nband nb
    // All higher layers need their height recalculated (but havent yet)
    void addIntopLayer( double t, double v, int nb ) {
        double r, w, vnew, tnew;
        ConductiveLayer layer, lyr;
        ListIterator<ConductiveLayer> itr = null;
        itr = llayer.listIterator();
        if (itr.hasNext() ) {
            layer = itr.next();
            if ( layer.nBand == nb ) {
                vnew = layer.volume + v;
                tnew = meanTemperature( layer.volume, layer.currentTemperatureK, v, t );
                layer.volume = vnew;
                layer.currentTemperatureK = tnew;
                layer.height = layer.volume / ( layer.width * layer.width );  // this needs to be correctly calculated
            }
        }

        reSynchronise();
    }

    // remove nBand layer nb;
    void removeLayer( int nb ) {
        int nremoved = 0;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();
        while(itr.hasNext()){
            layer = itr.next();
            if ( layer.nBand == nb ) {
                itr.remove();
                nlayers--;
                nremoved++;
            }
        }
        reSynchronise();
    }

    // find band number of layer of higher densiy than material mt
    int findHigherDensityNBand( double mt ) {
        int nb = -1;
        Material mtrl = new Material( mt );
        ConductiveLayer layer, lyr;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();
        while(itr.hasNext()){
            layer = itr.next();
            System.out.println( layer.nBand + ": " + layer.layerDensity + " > " + mtrl.density + " " + nb );
            if ( layer.layerDensity >= mtrl.density ) {
                nb = layer.nBand;
                break;
            }
        }
        return( nb );
    }

    double meanTemperature( double v1, double t1, double v2, double t2 ) {
        double tmean = ( v1 * t1 + v2 * t2 ) / ( v1 + v2 );
        return( tmean );
    }

    // find mean temperature of whole asteroid
    // this is the temperature that surface rocks would reach beneath a perfect insulator
    double meanAsteroidTemperature() {
        double vtot = 0;
        double vttot = 0;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();
        while(itr.hasNext()){
            layer = itr.next();
            vttot += layer.volume * layer.currentTemperatureK;
            vtot += layer.volume;
        }
        vttot = vttot / vtot;
        return( vttot );
    }

    double radiusConstantVolumeLayer( int layer, double layerVolume ) {
        double lRadius, vol;
        double f = 4.0 * Math.PI / 3;
        double cubeRoot = 1.0 / 3.0;
        vol = (layer+1) * layerVolume;
        lRadius = Math.pow( ( vol / f ), cubeRoot );
        return( lRadius);
    }

    // return the radius of the innermost layer of a sphere of radius r with n layers in it.
    double equalLayerVolumeSphere( int numLayers, double sphereRadius ) {
        double r1;
        double r[] = new double[ numLayers ];
        // In a sphere with centre layer radius R1,
        // the radius of the nth layer, Rn, is given by Rn^3 = n * R1^3
        r1 = Math.pow( ( sphereRadius * sphereRadius * sphereRadius / numLayers ), 0.333333 );
        return( r1 );
    }


    double sphereVolume( double r ) {
        double volume = 1.3333333333 * Math.PI * r * r * r;
        return( volume );
    }

    double sphereSurfaceArea( double r ) {
        double a = 4.0 * Math.PI * r * r;
        return( a );
    }


    double sphereRadius( double vol ) {
        // sphere volume = 4/3 .pi. r^3
        double fn = ( vol * 3.0 ) / ( 4.0 * Math.PI );
        double r = Math.pow( fn, 0.33333333 );
        return( r );
    }

    // find outer radius of layer sitting above an inner subsphere
    double layerRadius( double layerVolume, double volumeSubsphere ) {
        double radius = 0;
        double fn = 3.0 * ( layerVolume + volumeSubsphere ) / ( 4.0 * Math.PI );
        radius = Math.pow( fn, 1.0/3.00 );
        return( radius );
    }

    // find highest band number of nmaterial layers
    int countMaterialBands( int nmaterial ) {
        int count = 0;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();
        while(itr.hasNext()){
            layer = itr.next();
            if ( layer.materialType == nmaterial ) {
                count++;
            }
        }
        return( count );
    }

    double findIcePlusWaterDepth() {
        double depth = 0;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();
        while(itr.hasNext()){
            layer = itr.next();
            if ( layer.material.isIce( layer.currentTemperatureK) || layer.material.isWater( layer.currentTemperatureK) ) {
//              System.out.println( layer.nBand + " " + layer.materialType );
                depth += layer.height;
            }
        }
//      System.out.println( "ice depth " + depth );
        return( depth );

    }

    // find highest band number of nmaterial layers
    int materialBandNumber( int nmaterial ) {
        int count = -1;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();
        while(itr.hasNext()){
            layer = itr.next();
            if ( layer.materialType == nmaterial ) {
                count = layer.nBand;
                break;
            }
        }
        return( count );
    }

    // layers are counted from top surface (layer 0) of asteroid downwards.
    // bands are counted from asteroid centre (band 0) upwards.
    // This needs to be called whenever layers are added to or subtracted from the asteroid
    // This should also be called fairly regularly to update graphic display temperatures and materials in layers
    int setBandNumbers() {
        boolean flag1 = false;
        boolean flag2 = false;
        boolean flag3 = false;
        int n = 0;
        int count = nlayers - 1;
        double totalMass = 0;

        iceWaterDepth = 0;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();

        // seems that list iterator can only go back to revious elemnts if it's been forrward to next elemnts.
        while( itr.hasNext() ){
            layer = itr.next();

            // store data for graphics display
            graphicLayer[n][0] = layer.nBand;
            graphicLayer[n][1] = layer.materialType;
            graphicLayer[n][2] = layer.radius;
            graphicLayer[n][3] = layer.height;
            graphicLayer[n][4] = layer.currentTemperatureK;
            graphicLayer[n][5] = layer.phase;
            graphicLayer[n][6] = layer.upwardHeatFlowRate;
            layer.nBand = count;
            totalMass += layer.mass;

            // find thin surface band for solar irradiation
            if ( layer.height == 0 && !flag1 ) {
                this.thinSurfaceBand = layer.nBand;
                this.surfaceBand = layer.nBand - 1;  // old surface band is band underneath thin surface band
//              System.out.println( "thin layer found " + layer.nBand );
                flag1 = true;
            }
            // find top rock layer
            if ( layer.materialType == 2 && !flag2 ) {
                this.surfaceRockBand = layer.nBand;
//              System.out.println( "surf layer found " + layer.nBand );
                flag2 = true;
            }

            // record temperature of surface band under thin surface band
            if ( layer.nBand == surfaceBand ) {
                this.surfaceBandTemperature = layer.currentTemperatureK;
            }

            // find ice + water depth
            if ( layer.materialType == 3 ) {
                iceWaterDepth += layer.height;
            }

            if ( layer.materialType == 5 && !flag3 ) {
                this.airLowestBand = layer.nBand;
 //             System.out.println( "sair bottom band " + layer.nBand );
//              flag3 = true;
            }
            count--;
            n++;
        }
        // set topmost layer radius to pre-defined top of atmospher
 //     graphicLayer[n-1][2] = this.topOfAtmosphere;

        graphicLayerCount = n;
        graphicLayer[n][0] = -1;        // terminator
        layer = itr.previous();
        graphicSurfaceRockBand = this.surfaceRockBand;

//      System.out.println( "band thinSurf " + this.thinSurfaceBand + " surf " + this.surfaceBand + " rock " + this.surfaceRockBand );
//      System.out.println( "setBandNumbers totalMass " + totalMass );
        return( n );
    }


    // add up layer heights to get asteroid radius
    // and set nlayers variable
    double asteroidRadius() {
        int count = 0;
        double r = 0;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();
        while(itr.hasNext()){
            layer = itr.next();
            r += layer.height;
            count++;
        }
        this.nlayers = count;
        return( r );
    }

    // find layer pressures, and use these and layer temperatures to find layer strain,
    // and then use layer strain to find layer density and volume
    // and use layer volume to find layer width, height, and radius from centre
    void redimensionLayers() {
        double glocal;              // local gravitational acceleration
        double layerCentreRadius;
        double totalMass = 0;
        double totalForce = 0;
        double midLayerForce, deltaT, deltaP, depth;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();
        while ( itr.hasNext() ) {
            layer = itr.next();
            layerCentreRadius = layer.radius - layer.height / 2.0;
            depth = this.nominalRadius - layer.radius;
            glocal = gravitationalAccelerationInsideAsteroid( layerCentreRadius, this.nominalRadius, this.surfaceGravity );
            midLayerForce = totalForce + glocal * layer.mass / 2.0;
//          System.out.println( layer.nBand + " midLayerForce " + midLayerForce + " totalForce " + totalForce + " glocal * layer.mass / 2.0 " + glocal * layer.mass / 2.0);
            layer.previousLayerPressure = layer.currentLayerPressure;
            layer.currentLayerPressure = midLayerForce / layer.area;
            deltaP = layer.currentLayerPressure - layer.previousLayerPressure;
            deltaT = layer.currentTemperatureK - layer.previousTemperaureK;
            totalForce += layer.mass * glocal;
            totalMass  += layer.mass;
            System.out.println( layer.nBand + " area " + layer.area + " km mass "+ (int)layer.mass + " kg pressure " + (float)(layer.currentLayerPressure/1000000000.0) + " gPascals" + " glocal " + (float)glocal );
        }
        System.out.println( "totalMass " + (float)totalMass * this.surfaceArea + " kg" );
    }

    void findNonTaperingLayerPressures( double a ) {
        double glocal;              // local gravitational acceleration
        double layerCentreRadius;
        double totalMass = 0;
        double totalForce = 0;
        double midLayerForce, deltaT, deltaP, depth, mass;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();
        while ( itr.hasNext() ) {
            layer = itr.next();
            layerCentreRadius = layer.radius - layer.height / 2.0;
            depth = this.nominalRadius - layer.radius;
            glocal = gravitationalAccelerationInsideAsteroid( layerCentreRadius, this.nominalRadius, this.surfaceGravity );
            mass = a * layer.height * layer.layerDensity;
            midLayerForce = totalForce + glocal * mass / 2.0;
//          System.out.println( layer.nBand + " midLayerForce " + midLayerForce + " totalForce " + totalForce + " glocal * layer.mass / 2.0 " + glocal * mass / 2.0);
            layer.previousLayerPressure = layer.currentLayerPressure;
            layer.currentLayerPressure = midLayerForce / a ;
            deltaP = layer.currentLayerPressure - layer.previousLayerPressure;
            deltaT = layer.currentTemperatureK - layer.previousTemperaureK;
            totalForce += mass * glocal;
            totalMass  += mass;
            System.out.println( layer.nBand + " km mass "+ mass + " kg pressure " + (layer.currentLayerPressure/1000000000.0) + " gPascals" + " glocal " + (float)glocal );
        }
        System.out.println( "totalMass " + (float)totalMass * this.surfaceArea + " kg" );
    }

    // layer strain (change of height) due to both pressure and temperature
    double layerStrain() {
        double deltaH = 0;
        return( deltaH );
    }

    // source http://physicsteacher.in/2017/10/18/acceleration-due-to-gravity-height-depth/
    // aradius = asteroid radius, r = required radius
    double gravitationalAccelerationInsideAsteroid( double r, double aradius, double surfaceacceleration  ) {
        double h = aradius - r;
        return( surfaceacceleration * ( 1.0 - h / aradius ) );
    }

    // when numbers of layers change, or height/thickness of layers changes,
    // all the layer radii and band numbers must be recalcu;ated
    void reSynchronise() {
        this.radius = asteroidRadius();  // get complete radius of asteroid using all layers

        // radius of each layer is radius of top of layer
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();
        double heightOfLayersAbove = 0;
        while(itr.hasNext()){
            layer = itr.next();
            layer.radius = this.radius - heightOfLayersAbove;
            heightOfLayersAbove += layer.height;
        }

        setBandNumbers();

//      if ( this.materialBandNumber( 4 ) > this.surfaceBand ) this.surfaceBand = this.materialBandNumber( 4 );
//      if ( this.materialBandNumber( 3 ) > this.surfaceBand ) this.surfaceBand = this.materialBandNumber( 3 );

        resynchronise = false;      // unflag reSynchroise() call needed
    }

    void printWholeAsteroid() {
        double mass[] = new double[10];
        String name[] = new String[10];
        int index;
        double wLayer, r = 0;
        double totalMass = 0;
        double hfl = 0;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();

        System.out.println( "Year " + ap.year);
        // seems that list iterator can only go back to revious elemnts if it's been forrward to next elemnts.
        while( itr.hasNext() ){
            layer = itr.next();
            index = (int)layer.materialType;
            name[ index ] = layer.material.name;
            mass[ index ] += layer.mass;
        }

        for( index=0; index<=9; index++ ) {
            System.out.println( index + " " + name[index] + " mass " + mass[index] * this.surfaceArea );
        }

    }

    void printAsteroid( int ref ) {
        double wLayer, r = 0;
        double totalMass = 0;
        double hfl = 0;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();

        System.out.println( "Year " + ap.year);
        // seems that list iterator can only go back to revious elemnts if it's been forrward to next elemnts.
        while( itr.hasNext() ){
            layer = itr.next();
            totalMass += layer.mass;
            wLayer = layer.radius / referenceRadius;
            if ( ref == 1 || ref == 2 ) layer.storedTemperature[ ref-1 ] = layer.currentTemperatureK;
            System.out.println( "band " + layer.nBand + " r " + (float)layer.radius + " ht " + (float)layer.height + " lvol " + (float)layer.volume + " mtl " + (float)layer.materialType + " mass " + (int)layer.mass + " lres " + (float)layer.layerResistance + " uhfr " + (float)layer.upwardHeatFlowRate + " degrees K " + (float)layer.currentTemperatureK  ) ;
//          System.out.println( "band " + layer.nBand + " radius " + (float)layer.radius + " height " + (float)layer.height + " lvolume " + (float)layer.layerVolume( layer.radius, layer.radius - layer.height, wLayer ) + " material " + (float)layer.materialType + " heat " + (float)layer.heatProduced  + " degrees K " + (float)layer.currentTemperatureK  ) ;
            r += layer.height;
            hfl += layer.upwardHeatFlowRate;
        }
        System.out.println( "asteroid radius " + r + " net upward heat flow rate " + hfl );
        System.out.println( "asteroid column mass  " + totalMass + " asteroid mass " + totalMass * this.surfaceArea );
        System.out.println( "True Earth mass = 5.972 × 10^24 kg");
    }

    // print asteroid R, C, and RC time constants
    void printAsteroidRC( int ref ) {
        double wLayer, r = 0;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr=llayer.listIterator();

        System.out.println( "Year " + ap.year);
        // seems that list iterator can only go back to revious elemnts if it's been forrward to next elemnts.
        while( itr.hasNext() ){
            layer = itr.next();
            wLayer = layer.radius / referenceRadius;
            if ( ref == 1 || ref == 2 ) layer.storedTemperature[ ref-1 ] = layer.currentTemperatureK;
            System.out.println( "band " + layer.nBand + " v " + (float)layer.volume + " mtl " + (float)layer.materialType + " l_k " + (float)layer.layerConductivity + " lres " + (float)layer.layerResistance + " cap " + (float)layer.layerThermalCapacity + " time const (years) " + (float)(layer.layerTimeConstant / ap.secondsPerYear) ) ;
//          System.out.println( "band " + layer.nBand + " radius " + (float)layer.radius + " height " + (float)layer.height + " lvolume " + (float)layer.layerVolume( layer.radius, layer.radius - layer.height, wLayer ) + " material " + (float)layer.materialType + " heat " + (float)layer.heatProduced  + " degrees K " + (float)layer.currentTemperatureK  ) ;
            r += layer.height;
        }
        System.out.println( "asteroid radius " + r );
    }

    void printGraphicLayers() {
        System.out.println( "Year " + ap.year + " graphic layers");
        for ( int n=0; n<graphicLayerCount; n++ ) {
            System.out.println( "band " + (int)graphicLayer[n][0] + " type " + (int)graphicLayer[n][1] + " radius " + graphicLayer[n][2] );
        }
    }

}



class ConductiveLayer {
    double materialType;                // 1=iron, 2= rock, 3=ice. 4=water, 5=air, 6.nnn=rock-ice where .nnn = fgarnite
    Material material;
    double radius;                      // radial distance of top of layer from asteroid centre
    double length;                      // longitudinal m
    double width;                       // latitudinal m
    double height;                      // radial thickness m
    double area;                        // length x widthe
    double volume;                      // length x width * height
    double mass;                        // layer mass kg
    int    phase;                       // 0 solid, 1 liqid. 2 vapour
    double volumetricFractionGranite;
    double heatProduced;                // watts heat generated by radioactive granite in layer
    double upwardHeatFlowRate;          // conductive heat flow rate to layer above
    double netHeatFlowRate;             // layer heat flow from all sources
    double previousTemperaureK;         // temperature at previous iteration
    double currentTemperatureK;
    double nextTemperatureK;
    double pseudoTemperatureK;          // temperature used during phase change
    double phaseTemperatureK;           // temperature of current phase change
    boolean phaseChange;                // flagged true if currently changing phase
    boolean phaseHasChanged = false;    // flag set when material phase changes
    double layerDensity;
    double layerConductivity;
    double layerSpecificHeat;
    double layerThermalCapacity;
    double layerResistance;
    double layerTimeConstant;
    double currentLayerPressure;        // kg / square metre
    double previousLayerPressure;       // kg / square metre
    int nBand;                          // band numbers have reverse sense of layers, Band 0 is at centre.
    double storedTemperature[] = new double[2];
    double phaseSum = 0;

    // default constructor
    ConductiveLayer() { }

    // indexed material table driven constructor
    ConductiveLayer( double rlyr, double w, double h, double t, double mt, double wattsPerCubicMetre ) {
        this.materialType = mt;
        this.length = w;
        this.width = w;
        this.height = h;
        this.area = w * w;
        this.volume = this.layerVolume( rlyr, rlyr - this.height, this.width );
        this.radius = rlyr;         // radial distance of top of layer from asteroid centre

        this.currentTemperatureK = t;
        this.nextTemperatureK = t;

        this.material = new Material( mt );

//      this.heatProductionGranite = 0;    // watts / kg  (multiple of 0.0000000001 )
        this.layerDensity = material.density;
        this.layerConductivity = material.conductivity;
        this.layerSpecificHeat = material.specificHeat;
        this.mass = this.volume * this.layerDensity;
        this.layerResistance = this.layerResistance();
        this.layerThermalCapacity = this.layerThermalCapacity();
        this.layerTimeConstant = this.layerResistance * this.layerThermalCapacity;
        this.heatProduced = volume * wattsPerCubicMetre;
    }

    // copy the characteristics of another layer
    void copyLayer( ConductiveLayer layer ) {
        // this.radius = layer.radius;         not copied because it's a different layer
        // nBand not copied because it's a different layer
        // nBand is set by asteroid.reSynchronise();
        this.materialType = layer.materialType;
        this.material = layer.material;
        this.length = layer.length;
        this.width = layer.width;
        this.height = layer.height;
        this.area = layer.area;
        this.volume = layer.volume;
        this.mass = layer.mass;
        this.currentTemperatureK = layer.currentTemperatureK;
        this.nextTemperatureK = layer.nextTemperatureK;
        this.layerDensity = layer.layerDensity;
        this.layerConductivity = layer.layerConductivity;
        this.layerSpecificHeat = layer.layerSpecificHeat;
        this.layerThermalCapacity = layer.layerThermalCapacity;
        this.heatProduced = layer.heatProduced;
    }

    // Layer are thin slices of a sphere, with this.width * this width cross-sectional area at top.
    double layerVolume( double radiusTopOfLayer, double radiusBottomOfLayer, double sliceWidth ) {
        double v = sphereVolume( radiusTopOfLayer ) - sphereVolume( radiusBottomOfLayer );
        double fraction = sliceWidth * sliceWidth / sphereSurfaceArea( radiusTopOfLayer );
        return( fraction * v );
    }

    double layerMass() {
        return( this.volume * this.material.density );
    }

    double layerThermalCapacity() {
        return( this.volume * this.material.density * this.material.specificHeat );
    }

    double heatGenerated() {
        return( this.heatProduced );
    }

    int setLayerPhase() {
        if ( material.isSolid( this.currentTemperatureK ) ) {
            this.phase = 0;
        } else if ( material.isLiquid( this.currentTemperatureK ) ) {
            this.phase = 1;
        } else {
            this.phase = 2;
        }
        return( this.phase );
    }
/*
    // return meanDensity of layer
    double density() {
        double volumeGranite = this.volume * this.volumetricFractionGranite;
        double massGranite = volumeGranite * this.densityGranite;
        double volumeIce = this.volume - volumeGranite;
        double massIce = volumeIce * this.densityIce;
        double meanDensity = ( massIce + massGranite ) / this.volume;
        return( meanDensity );
    }

    // mean specific heat is heat required to raise layer temperature 1 degree K
    double specificHeat() {
        double massGranite = this.volume * this.volumetricFractionGranite * this.densityGranite;
        double heatToRaiseGraniteTemp = massGranite * specificHeatGranite;
        double massIce = this.volume * ( 1.0 - this.volumetricFractionGranite ) * this.densityIce;
        double heatToRaiseIceTemp = massIce * specificHeatIce;
        double meanSpecificHeat = ( heatToRaiseGraniteTemp + heatToRaiseIceTemp ) / ( massGranite + massIce );
        return( meanSpecificHeat );
    }
*/
    double layerResistance() {
        this.layerResistance = this.height / ( this.layerConductivity * this.area );
        return( this.layerResistance );
    }
/*
    // layer resistance = resistance of granite part + resistance of ice part
    double conductivity() {
        double resistanceOfLayerIce = ( this.height * ( 1.0 - this.volumetricFractionGranite ) ) / this.conductivityIce;
        double resistanceOfLayerGranite = ( this.height * this.volumetricFractionGranite ) / this.conductivityGranite;
        double layerResistance = resistanceOfLayerIce + resistanceOfLayerGranite;
//      System.out.println( "resiatance " +resistanceOfLayerIce + " + " + resistanceOfLayerGranite + " = " + layerResistance);
        double layerConductance = 1.0 / layerResistance;
        double meanConductivity = layerConductance * this.height;
        return( meanConductivity );
    }
*/
    double sphereVolume( double r ) {
        double volume = 1.3333333333 * Math.PI * r * r * r;
        return( volume );
    }

    double sphereSurfaceArea( double r ) {
        double a = 4.0 * Math.PI * r * r;
        return( a );
    }

}


class Material{
    String name;
    double index;
    double albedo;                          // negative albedo is transparent, use albedo of layer below
    double density;
    double conductivity;
    double specificHeat;
    double emissivity;
    double absorptivity;
    double latentHeatOfFusion;
    double latentHeatOdEvaporation;
    double volumetricFractionGranite = 0;
    double radioactiveHeatGenerated;
    double phaseTemperature1;               // melting point
    double phaseTemperature2;               // boiling point
    double expansivity;                     // coefficient of linear thermal expansion
    double modulusElasticity;
    int colourIndex[] = new int[3];         // 0 solid, 1 liquid, 2 vapaor

double[][] iceCharacteristics = {
    // copied from https://www.engineeringtoolbox.com/ice-thermal-properties-d_576.html
    // degC    density    k      sp ht (kJ/kg)
    { 0,	916.2,	2.22,	2.050, },
    { -5,	917.5,	2.25,	2.027, },
    { -10,	918.9,	2.30,	2.000, },
    { -15,	919.4,	2.34,	1.972, },
    { -20,	919.4,	2.39,	1.943, },
    { -25,	919.6,	2.45,	1.913, },
    { -30,	920.0,	2.50,	1.882, },
    { -35,	920.4,	2.57,	1.851, },
    { -40,	920.8,	2.63,	1.818, },
    { -50,	921.6,	2.76,	1.751, },
    { -60,	922.4,	2.90,	1.681, },
    { -70,	923.3,	3.05,	1.609, },
    { -80,	924.1,	3.19,	1.536, },
    { -90,	924.9,	3.34,	1.463, },
    {-100,	925.7,	3.48,	1.389, },
    {-150,	926.7,	3.52,	1.360, },   // extrapolated value (v approx)
    {-200,	927.7,	3.56,	1.340, },   // extrapolated value (v approx)
    {-250,	928.7,	3.60,	1.320, },   // extrapolated value (v approx)

};

    Material() { }

    Material( double i, String name, double d, double c, double s, double lhf, double lhv, double fgranite, double hgranite ) {
            this.name = name;
            this.index = i;
            this.density = d;
            this.conductivity = c;
            this.specificHeat = s;
            this.latentHeatOfFusion = lhf;
            this.latentHeatOdEvaporation = lhv;
            this.volumetricFractionGranite = fgranite;
            this.radioactiveHeatGenerated = hgranite;
    }

    // select from existing materials
    Material( double i ) {
        setMaterialCharacteristics(i);
    }

    void setMaterialCharacteristics( double i ) {
        if ( i == 0 ) {
            // thin surface layer
            this.name = "surface";
            this.index = i;
            this.albedo = -1;
            this.density = 7870;  // iron
            this.conductivity = 79.5;  // iron
            this.specificHeat = 460.5;  // iron
            this.phaseTemperature1 = 100000000;     // no phase
            this.phaseTemperature2 = 100000000;     // no phase change
            this.emissivity = 0.96;                 // ???
            this.absorptivity = 0.96;
            this.expansivity = 0;
            this.modulusElasticity = -0;
            this.colourIndex[0] = 0;                // black
            this.colourIndex[1] = 0;                //
            this.colourIndex[2] = 0;                //
        } else if ( i == 1 ) {
            // basalt rock
            this.name = "basalt asthenosphere";
            this.index = i;
            this.albedo = 0.3;
            this.density = 3000.0;                  // kg / m^3 basalt
            this.conductivity = 2.20;               // W/ m deg K basalt
            this.specificHeat = 840;                // J/ kg degK basalt
            this.phaseTemperature1 = 1250;
            this.latentHeatOfFusion = 840;          // set same as specific heat
            this.phaseTemperature2 = 10000;
            this.latentHeatOdEvaporation = 840;     // set same as specific heat
            this.volumetricFractionGranite = 0;
            this.radioactiveHeatGenerated = 1E-9;    // modern terrestrial granite
            this.emissivity = 0.95;                  // https://www.optotherm.com/emiss-table.htm
            this.absorptivity = 0.95;
            this.expansivity = 8.0e-6;               // 10^-6 same as granite
            this.modulusElasticity = 50E9;       // http://community.dur.ac.uk/~des0www4/cal/dams/geol/mod.htm
            this.colourIndex[0] = 0;                 // brown
            this.colourIndex[1] = 5;                 // red
            this.colourIndex[2] = 7;                 // yellow
        } else if ( i == 2 ) {
            // granite rock
            this.name = "granite crust";
            this.index = i;
            this.albedo = 0.3;
            this.density = 2650.0;                  // kg / m^3
            this.conductivity = 2.12;               // W/ m deg K
            this.specificHeat = 790;                // J/ kg degK engineering toolbox
            this.phaseTemperature1 = 1250;
            this.latentHeatOfFusion = 418000;       // conversion of 100 calories/gm
            this.phaseTemperature2 = 10000;
            this.latentHeatOdEvaporation = 460;     // set same as specific heat
            this.volumetricFractionGranite = 1;
            this.radioactiveHeatGenerated = 1E-9;       // modern terrestrial granite
            this.emissivity = 0.95;                 // https://www.optotherm.com/emiss-table.htm
            this.absorptivity = 0.95;
            this.expansivity = 8.0e-6;              // 10^-6
            this.modulusElasticity = 40E9;      //http://community.dur.ac.uk/~des0www4/cal/dams/geol/mod.htm
            this.colourIndex[0] = 9;                // brown
            this.colourIndex[1] = 5;                // red
            this.colourIndex[2] = 7;                // yellow
        } else if ( i == 3 ) {
            // water ice
            this.name = "ice";
            this.index = 3.0;
            this.albedo = 0.60;
            this.density = 917;                     // density of ice kg/ m^3
            this.conductivity = 3.48;               // conductivity of Ice  W / m degK
            this.specificHeat = 1389;               // specific heat of ice, J / kg deg K
            this.phaseTemperature1 = 273;
            this.latentHeatOfFusion = 334000;       // J/kg
            this.phaseTemperature2 = 373;
            this.latentHeatOdEvaporation = 2264705; // J/kg
            this.volumetricFractionGranite = i - (int)i;
            this.radioactiveHeatGenerated = 0;
            this.emissivity = 0.85;                 // snow 0.8 - 0.9
            this.absorptivity = 0.85;
            this.expansivity = 51.0;
            this.modulusElasticity = 0;         // unknown
            this.colourIndex[0] = 3;                // mid gray (ice)
            this.colourIndex[1] = 12;               // cyan (water)
            this.colourIndex[2] = 11;               // very light gray (steam)
        } else if ( i == 3.1 ) {
            // water
            this.name = "water";
            this.index = 3.1;
            this.albedo = -0.3;                     // negative means transparent
            this.density = 999.8;                   // water density at 273 K
            this.conductivity = 0.59;               // water conductivity at 273 K
            this.specificHeat = 4217;    // water specific heat at 273 K
            this.phaseTemperature1 = 273;
            this.latentHeatOfFusion = 334000;              // J/kg
            this.phaseTemperature2 = 373;
            this.latentHeatOdEvaporation = 2264705;        // J/kg
            this.volumetricFractionGranite = i - (int)i;
            this.radioactiveHeatGenerated = 0;
            this.emissivity = 0.96;                  // water, snow 0.8 - 0.9
            this.absorptivity = 0.96;
            this.expansivity = 0;                    // water dees not expand much
            this.modulusElasticity = 2.2E9;      // 2.2 GN/m2,
            this.colourIndex[0] = 2;                 // mid gray
            this.colourIndex[1] = 8;                 // cyan
            this.colourIndex[2] = 1;                 // light gray
        } else if ( i == 3.2 ) {
            // steam
            // using characteristics f water for now
            this.name = "steam";
            this.index = 3.2;
            this.albedo = -0.3;                     // transparent
            this.density = 999.8;                   // water density at 273 K
            this.conductivity = 0.59;               // water conductivity at 273 K
            this.specificHeat = 4217;               // water specific heat at 273 K
            this.phaseTemperature1 = 273;
            this.latentHeatOfFusion = 334000;       // J/kg
            this.phaseTemperature2 = 373;
            this.latentHeatOdEvaporation = 2264705; // J/kg
            this.volumetricFractionGranite = i - (int)i;
            this.radioactiveHeatGenerated = 0;
            this.emissivity = 0.96;                 // ???
            this.absorptivity = 0.96;
            this.expansivity = 0;                   // no idea!
            this.modulusElasticity = -0;        // unknown
            this.colourIndex[0] = 2;                // mid gray
            this.colourIndex[1] = 8;                // cyan
            this.colourIndex[2] = 1;                // light gray
        } else if ( i == 5 ) {
            // air
            this.name = "air";
            this.index = i;
            this.albedo = -0.3;
            this.density = 1.293;                   // density of air kg/ m^3
            this.conductivity = 0.0243;             // conductivity of air  W / m degK
            this.specificHeat = 1000;               // specific heat of air, J / kg deg K
            this.phaseTemperature1 = 58;
            this.latentHeatOfFusion = 1000;         // set same as specific heat
            this.phaseTemperature2 = 76.3;
            this.latentHeatOdEvaporation = 1000;    // set same as specific heat
            this.volumetricFractionGranite = i - (int)i;
            this.radioactiveHeatGenerated = 0;
            this.emissivity = 0.80;                 // ???
            this.absorptivity = 0.80;
            this.expansivity = 0.00369;             // 1/K coefficient at 0°C and 1 bar https://www.engineeringtoolbox.com/air-properties-d_156.html
            this.modulusElasticity = 1.01325E5; // https://www.engineeringtoolbox.com/air-properties-d_156.html
            this.colourIndex[0] = 10;               // mid gray
            this.colourIndex[1] = 10;               // cyan
            this.colourIndex[2] = 10;               // light gray
        } else if ( i == 6 ) {
            // planet inner core (iron?)
            this.name = "inner iron core";
            this.index = i;
            this.albedo = 0.3;
            this.density = 13000;                   // http://hyperphysics.phy-astr.gsu.edu/hbase/Geophys/earthstruct.html
            this.conductivity = 79.5;               // iron
            this.specificHeat = 800.0;              // file:///C:/Users/Me/Downloads/m00024-dat-914-core-properties.pdf
            this.phaseTemperature1 = 1811;          // iron
            this.latentHeatOfFusion = 460.5;        // set same as specific heat
            this.phaseTemperature2 = 3134;          // iron
            this.latentHeatOdEvaporation = 460.5;   // set same as specific heat
            this.volumetricFractionGranite = i - (int)i;
            this.radioactiveHeatGenerated = 0;
            this.emissivity = 0.87;
            this.absorptivity = 0.87;
            this.expansivity = 10.5E-6;
            this.modulusElasticity = -0;
            this.colourIndex[0] = 0;                 // black
            this.colourIndex[1] = 6;                 // orange
            this.colourIndex[2] = 7;                 // yellow
        } else if ( i == 7 ) {
            // planet outer core (iron?)
            this.name = "outer iron core";
            this.index = i;
            this.albedo = 0.3;
            this.density = 12200;                   // http://hyperphysics.phy-astr.gsu.edu/hbase/Geophys/earthstruct.html
            this.conductivity = 79.5;               // iron
            this.specificHeat = 800.0;              // file:///C:/Users/Me/Downloads/m00024-dat-914-core-properties.pdf
            this.phaseTemperature1 = 1811;          // iron
            this.latentHeatOfFusion = 460.5;        // set same as specific heat
            this.phaseTemperature2 = 3134;          // iron
            this.latentHeatOdEvaporation = 460.5;   // set same as specific heat
            this.volumetricFractionGranite = i - (int)i;
            this.radioactiveHeatGenerated = 0;
            this.emissivity = 0.87;
            this.absorptivity = 0.87;
            this.expansivity = 10.5E-6;             // same as inner core
            this.modulusElasticity = -0;        // unknown
            this.colourIndex[0] = 0;                // black
            this.colourIndex[1] = 6;                // orange
            this.colourIndex[2] = 7;                // yellow
        } else if ( i == 8 ) {
            // planet lower mantle)
            this.name = "lower mantle";
            this.index = i;
            this.albedo = 0.3;
            this.density =  4600;                   // http://hyperphysics.phy-astr.gsu.edu/hbase/Geophys/earthstruct.html
            this.conductivity =  6.0;               // https://escholarship.org/uc/item/6ns1k73c
            this.specificHeat = 1000.0;             // https://www.colorado.edu/geolsci/courses/GEOL5700-PCE/convection.pdf
            this.phaseTemperature1 = 1811;          // iron
            this.latentHeatOfFusion = 460.5;        // set same as specific heat
            this.phaseTemperature2 = 3134;          // iron
            this.latentHeatOdEvaporation = 460.5;   // set same as specific heat
            this.volumetricFractionGranite = i - (int)i;
            this.radioactiveHeatGenerated = 0;
            this.emissivity = 0.87;
            this.absorptivity = 0.87;
            this.expansivity = 15.0E-6;             // MgO
            this.modulusElasticity = -0;        // unknown
            this.colourIndex[0] = 0;                // black
            this.colourIndex[1] = 6;                // orange
            this.colourIndex[2] = 7;                // yellow
        } else if ( i == 9 ) {
            // planet upper mantle)
            this.name = "upper mantle";
            this.index = i;
            this.albedo = 0.3;
            this.density =  3600;                   // http://hyperphysics.phy-astr.gsu.edu/hbase/Geophys/earthstruct.html
            this.conductivity =  4.5;               // guessed
            this.specificHeat = 1000.0;             // https://www.colorado.edu/geolsci/courses/GEOL5700-PCE/convection.pdf
            this.phaseTemperature1 = 1811;          // iron
            this.latentHeatOfFusion = 460.5;        // set same as specific heat
            this.phaseTemperature2 = 3134;          // iron
            this.latentHeatOdEvaporation = 460.5;   // set same as specific heat
            this.volumetricFractionGranite = i - (int)i;
            this.radioactiveHeatGenerated = 0;
            this.emissivity = 0.87;
            this.absorptivity = 0.87;
            this.expansivity = 30.0E-6;             // MgO
            this.modulusElasticity = -0;        // unknown
            this.colourIndex[0] = 0;                // black
            this.colourIndex[1] = 6;                // orange
            this.colourIndex[2] = 7;                // yellow
        } else {
            System.out.println( "Can't find material " + i );
        }
    }

    // for use when ice (index 3) is combined with water (index 4)
    boolean isWater( double t ) {
        boolean isWater = false;
        if ( this.index == 3 ) {
            // water phase change over 1 degree K
            if ( t > this.phaseTemperature1+0.01  && t < this.phaseTemperature2 ) {
                isWater = true;
            }
        }
        return ( isWater );
    }

    // for use when ice (material index 3) is combined with water (material index 4)
    boolean isIce( double t ) {
        boolean isIce = false;
        if ( this.index == 3 ) {
            // water phase change over 1 degree K
            if ( t < this.phaseTemperature1+0.01  && t < this.phaseTemperature2 ) {
                isIce = true;
            }
        }
        return ( isIce );
    }

    boolean isLiquid( double t ) {
        boolean isLiquid = false;
        if ( t > this.phaseTemperature1+0.01  && t < this.phaseTemperature2 ) {
            isLiquid = true;
        }
        return ( isLiquid );
    }

    boolean isSolid( double t ) {
        boolean isSolid = false;
        if ( t < this.phaseTemperature1+0.01  && t < this.phaseTemperature2 ) {
            isSolid = true;
        }
        return ( isSolid );
    }

    boolean isAir( double t ) {
        boolean isAir = false;
        if ( this.index == 5 ) {
            isAir = true;
        }
        return ( isAir );
    }

    int indexIceTemperature( double degK ) {
        int index = 0;
        for ( int n=0; n<18; n++ ) {
            if ( celsiusToKelvin( iceCharacteristics[n][0] ) < degK ) {
                index = n;
                break;
            }
        }
        return( index);
    }

    double celsiusToKelvin( double degC ) {
        return( degC + 273 );
    }

}



/*********** moving graph canvas *****************************************/
class ItemGraphCanvas extends Canvas {
    Glaciation ap;
    Dimension fielddimension;
//  Image fieldImage;
//  Graphics field2Graphics;
    int day;
    int maxstreams = 6;
    int nstreams;
    double lasty[] = new double[maxstreams];
    double nexty[] = new double[maxstreams];
    String streamLabel[] = new String[maxstreams];
    double streamMaxValue[] = new double[maxstreams];
    double streamMinValue[] = new double[maxstreams];
    boolean overlappingStreams = true;
//  int graph_height = 100;
    int graphymax = 100;
    boolean started;
    Font font;
    FontMetrics fm;
    int redLetterDay;
    String ylabel = new String();
    Dimension d;
    int sideband = 25;
    int lowband = 15;
    long calendar = 0;                  // count keeping elapsed time
    double calendarStep;                // actual duration of each pixel calendar step (e.g. years)
    long calendarDisplayInterval;
    String calendarLabel;               // label to attach to calanddar dates
    boolean autoCalendar = true;        // if true, created dates automatically
    Color colorpalette[] = new Color[15];
    Image historicalRecord;             // rolling image of historical graphs
    Graphics historicalGraphics;
    Dimension hr;                       // width and height of historical record
    Image currentWindow;                // current window onto historical record
    Graphics currentGraphics;
    Dimension cw;                       // width and height of historical record



    // Constructor allows access to Field objects by this class
    public ItemGraphCanvas( Glaciation app, Graphics g, int rollingGraphWidth, int rollingGraphHeight, int ns, boolean overlap ) {
       super();
        ap = app;
        d  = new Dimension( rollingGraphWidth, rollingGraphHeight );
//    graph_height = d.height;
        nstreams = ns;
        overlappingStreams = overlap;
/*
        // create rolling historical record image
        hr = new Dimension( 2 * rollingGraphWidth, rollingGraphHeight - this.lowband );
        this.historicalRecord = createImage( hr.width, hr.height );
        historicalGraphics = historicalRecord.getGraphics();
        historicalGraphics.setColor(Color.white);
        historicalGraphics.fillRect(0, 0, hr.width, hr.height );

        // create current window image
        cw = new Dimension( rollingGraphWidth - this.sideband, rollingGraphHeight - this.lowband );
        this.currentWindow = createImage( cw.width, cw.height );
        currentGraphics = currentWindow.getGraphics();
        currentGraphics.setColor(Color.white);
        currentGraphics.fillRect(0, 0, cw.width, cw.height );
*/


        for ( int n = 0; n < nstreams; n++ ) {
          lasty[n] = -100.0; nexty[n] = -100.0; streamLabel[n] = "    ";
        }
        ylabel = "500";
        day = 0;
        started = false;
        redLetterDay = -1;
        font = new Font( "Helvetica", Font.PLAIN, 12 );

        calendarDisplayInterval = 100000;
        calendarLabel = "e5";

        colorpalette[0] = new Color(000, 000, 000);     // black
        colorpalette[1] = new Color( 64,  64,  64);     // light grey
        colorpalette[2] = new Color(128, 128, 128);     // mid gray
        colorpalette[3] = new Color(192, 192, 192);     // darke gray
        colorpalette[4] = new Color(255, 255, 255);     // white
        colorpalette[5] = Color.RED;
        colorpalette[6] = Color.ORANGE;
        colorpalette[7] = Color.YELLOW;
        colorpalette[8] = Color.CYAN;
        colorpalette[9] = new Color( 85,  52,  52);     // brown?
        colorpalette[10] = new Color(135,206, 250);     // sky blue
        colorpalette[11] = new Color(240,240, 240);     // steam
        colorpalette[12] = new Color( 0,  0,  255);     // blue
        colorpalette[13] = new Color( 0,  0,  255);     // blue
        colorpalette[14] = new Color( 0,  0,  255);     // blue


    }

    void setNumberOfStreams( int n ) {
        nstreams = n;
    }

    public void addStream() {
      nstreams++;
    }

    public void removeStream() {
      nstreams--;
    }

    public void setStreamLabel( int stream, String s ) {
      streamLabel[stream] = s;
    }

    // set max and min values that are to be visible
    public void setStreamMaxMin( int stream, double vmax, double vmin ) {
      streamMaxValue[ stream ] =  vmax;
      streamMinValue[ stream ] =  vmin;
    }

    public void setYscale( int ymax ) {
      graphymax = ymax;
      ylabel = Integer.toString( ymax );
    }

    public void stop() {

//      field2Graphics = null;
//      fieldImage = null;
    }

    public void redraw() { repaint(); }

    // update calls paint(), otherwise clears the screen.
    public void update(Graphics g) {
      paint(g);
    }

    public void paint(Graphics fieldGraphics ) {
      int n, h;
      String s;

      calendar += calendarStep;
//    System.out.println( "year " + (float)ap.year + " calendar " + (float)calendar + " calendarDisplayInterval " + (float)calendarDisplayInterval);

      // Create the offscreen graphics context, if no good one exists.
      if ( (fieldGraphics == null) ) {
//      fieldImage = createImage(d.width, d.height + lowband);
//      fieldGraphics = fieldImage.getGraphics();
        fieldGraphics.setColor(Color.WHITE);
        fieldGraphics.fillRect(0, 0, d.width, d.height + lowband);
        fieldGraphics.setFont( font );
//      g.drawImage(fieldImage, 0, 0, this);
      }

      fm = fieldGraphics.getFontMetrics( font );

      if ( started ) {

        // move image 1 pixel leftward
        fieldGraphics.setPaintMode();
        fieldGraphics.copyArea(1,0, d.width-sideband, d.height+lowband-1, -1, 0 );

        // rub out unmoved vertical line
        fieldGraphics.setColor(Color.WHITE);
        fieldGraphics.fillRect(d.width-sideband-1, 0, d.width-1, d.height+lowband);

        // add righthand text
        fieldGraphics.setColor(Color.darkGray);
//      fieldGraphics.drawString( ylabel, d.width-sideband+1, 10 );
//      fieldGraphics.drawString( "0", d.width-sideband/3-1, d.height-lowband );


        // draw horizontal gridlines
        if ( this.overlappingStreams ) {
            h = 0;
            for ( n=0; n<=nstreams; n++ ) {
              h = h + (d.height - lowband)/5;
              fieldGraphics.setColor(Color.darkGray);
              fieldGraphics.drawLine( d.width-sideband-2, h, d.width-sideband-1, h );
              s = Integer.toString( (int)( streamMaxValue[0] ) );
              fieldGraphics.drawString( s, d.width-sideband, rescale( 0, streamMaxValue[0] ) +9 );
              s = Integer.toString( (int)( streamMinValue[0] ) );
              fieldGraphics.drawString( s, d.width-sideband, rescale( 0, streamMinValue[0] ) -2 );
            }
        } else {
            for ( n=0; n<nstreams; n++ ) {
              fieldGraphics.setColor(Color.darkGray);
              fieldGraphics.drawLine( d.width-sideband-2, rescale(n, 0), d.width-sideband-1, rescale(n, 0) );
              s = Integer.toString( (int)( streamMaxValue[n] ) );
              fieldGraphics.drawString( s, d.width-sideband, rescale( n, streamMaxValue[n] ) +9 );
              s = Integer.toString( (int)( streamMinValue[n] ) );
              fieldGraphics.drawString( s, d.width-sideband, rescale( n, streamMinValue[n] ) -2 );
            }
        }
        // draw vertical gridlines and day numbers
        if ( autoCalendar ) {
          if ( calendar % calendarDisplayInterval == 0) {
              System.out.println( calendar + " calendar " + calendarDisplayInterval );
              fieldGraphics.setColor(Color.darkGray);
              fieldGraphics.drawLine( d.width-sideband-1, 0, d.width-sideband-1, d.height-lowband );
              s = Integer.toString( (int)( calendar / calendarDisplayInterval ) ) + calendarLabel;
              fieldGraphics.drawString( s, d.width-sideband-fm.stringWidth(s)-1, d.height-2 );
          }
        } else if ( (day % calendarDisplayInterval ) == 0 ) {
          fieldGraphics.setColor(Color.darkGray);
          fieldGraphics.drawLine( d.width-sideband-1, 0, d.width-sideband-1, d.height );
          s = Integer.toString(day);
          fieldGraphics.setColor(Color.darkGray);
          fieldGraphics.drawString( s, d.width-sideband-fm.stringWidth(s)-1, d.height+lowband-1 );
        }

        // draw red vertical line to show event
        if ( redLetterDay >= 0 ) {
          fieldGraphics.setColor(Color.red);
          fieldGraphics.drawLine( d.width-sideband-1, 0, d.width-sideband-1, d.height );
          s = Integer.toString(redLetterDay);
          fieldGraphics.drawString( s, d.width-sideband-fm.stringWidth(s)-3, fm.getAscent() + 1 );
        }

//        System.out.println( " overlap " + nstreams );

        // draw graph lines and labels at right edge of graph
        for ( n = nstreams-1; n >= 0; n-- ) {
          fieldGraphics.setColor(Color.orange);
          if ( n==0 ) fieldGraphics.setColor(Color.BLACK);
          if ( n==1 ) fieldGraphics.setColor(Color.red);
          if ( n==2 ) fieldGraphics.setColor(Color.green);
          if ( n==3 ) fieldGraphics.setColor(Color.cyan);
          if ( n==4 ) fieldGraphics.setColor(Color.yellow);
          if ( n==5 ) fieldGraphics.setColor(Color.darkGray);
          fieldGraphics.drawLine( d.width-sideband-2, rescale( n, lasty[n] ), d.width-sideband-1, rescale( n, nexty[n] ) );
          fieldGraphics.drawString( streamLabel[n], d.width-sideband+2, rescale( n, nexty[n] ) );
        }

//      fieldGraphics.setColor(Color.red);
//      fieldGraphics.fillRect(  20, 20, 20, 20);


        //Paint the image onto the screen.
//      g.drawImage(fieldImage, 0, 0, this);

      } else {
        started = true;
        fieldGraphics.setColor(Color.WHITE);
        fieldGraphics.fillRect(0, 0, d.width, d.height);
      }

      for ( n = 0; n < nstreams; n++ ) lasty[n] = nexty[n];
    }


    // rescale y value
    int rescale( double yvalue ) {
      int rsv;
      double gph = (double)d.height;
      double yv = (double)yvalue;
      double gymax = (double)graphymax;
      double yl = gph - yv * ( gph / gymax );
      rsv = (int)yl;
      return( rsv );
    }

    // new 2018 rescale y value
    // new code can handle +/- yvalue range
    int rescale( int s, double yvalue ) {
        int rsv;
        double height = d.height - lowband;
        if ( this.overlappingStreams ) {
            double vscale = height / streamMaxValue[s];
            double xaxis = vscale * streamMaxValue[s];
            rsv = (int)( xaxis - yvalue * vscale );
        } else {
            // each stream height = ( d.height / (double)nstreams )
            double vscale = height / ( streamMaxValue[s] * (double)nstreams );
            double xaxis = ( (double)s * height / (double)nstreams ) + ( vscale * streamMaxValue[s] );
            rsv = (int)( xaxis - yvalue * vscale );
        }
        return( rsv );
    }
}



class LayerDisplay {

    int xcentre;
    int ycentre;
    int xtopleft;
    int ytopleft;
    int hr;
    double jscale;
    int xmax;
    int ymax;
    Color colorpalette[] = new Color[15];
    boolean showLayerBoundaries =  true;

    int NBAND = 0;
    int MATERIALTYPE = 1;
    int RADIUS = 2;
    int HEIGHT = 3;
    int TEMPERATURE = 4;
    int PHASE = 5;
    int UHFLOWRATE = 6;

    public LayerDisplay() {

        colorpalette[0] = new Color(000, 000, 000);     // black
        colorpalette[1] = new Color( 64,  64,  64);     // light grey
        colorpalette[2] = new Color(128, 128, 128);     // mid gray
        colorpalette[3] = new Color(192, 192, 192);     // darke gray
        colorpalette[4] = new Color(255, 255, 255);     // white
        colorpalette[5] = Color.RED;
        colorpalette[6] = Color.ORANGE;
        colorpalette[7] = Color.YELLOW;
        colorpalette[8] = Color.CYAN;
        colorpalette[9] = new Color( 85,  52,  52);     // brown?
        colorpalette[10] = new Color(135,206, 250);     // sky blue
        colorpalette[11] = new Color(240,240, 240);     // steam
        colorpalette[12] = new Color( 0,  0,  255);     // blue
        colorpalette[13] = new Color( 0,  0,  255);     // blue
        colorpalette[14] = new Color( 0,  0,  255);     // blue
    }

    void setDisplaySize( int x, int y ) {
        xmax = x;
        ymax = y;
//      System.out.println( "layer xmax " + xmax + " ymax " + ymax );
    }

    void paint( Graphics g, Asteroid asteroid, double topRockRadius ) {
        double r;

        // show 200 km of top layers of body
        double topLayerDepth =  15000.0;
        double jscale = xmax / topLayerDepth;        // screen to show top 40 km
        double tscale = xmax / 5200.0;

        // clear window
        g.setColor( Color.white );
        g.fillRect( 0, 0, xmax, ymax );

        // circle centre (pixels)
        xcentre = xmax / 2;
        ycentre = (int)( topRockRadius * jscale ) + ( ymax / 2 );

        // paint atmosphere filled circle
        // surface rock radius
        int sr = (int)( asteroid.graphicLayer[0][RADIUS] * jscale );

        xtopleft = xcentre - sr;
        ytopleft = ycentre - sr;
        hr = 2 * sr;
//      g.setColor( Color.darkGray );
//      g.drawOval( ytopleft, xtopleft, hr, hr );
        int xtoa = xtopleft;
        int ytoa = ytopleft;

        Color colour = Color.white;
        Color lastColour = Color.white;
        for ( int n=0; n<asteroid.graphicLayerCount; n++ ) {
            colour = setColour( (int)asteroid.graphicLayer[n][MATERIALTYPE], asteroid.graphicLayer[n][PHASE] );
            if ( colour != lastColour ) {
                r = asteroid.graphicLayer[n][RADIUS];
                if ( r > asteroid.topOfAtmosphere ) r = asteroid.topOfAtmosphere;
                sr = (int)( r * jscale );
                xtopleft = xcentre - sr;
                ytopleft = ycentre - sr;
                hr = 2 * sr;
                g.setColor( colour );
                g.fillOval( xtopleft, ytopleft, hr, hr );
                lastColour = colour;
            }
        }

        if ( showLayerBoundaries ) {
           for ( int n=0; n<asteroid.graphicLayerCount; n++ ) {
                sr = (int)( asteroid.graphicLayer[n][RADIUS] * jscale );
                xtopleft = xcentre - sr;
                ytopleft = ycentre - sr;
                hr = 2 * sr;
                g.setColor( Color.white );
                g.drawOval( xtopleft, ytopleft, hr, hr );
            }
        }


//      g.setColor( Color.darkGray );
//      g.fillRect( 100, 120, 30, 30 );

        paintTemperatureGraph( g, asteroid, ytoa, jscale );

    }

    void paintTemperatureGraph( Graphics g, Asteroid asteroid, int xtoa, double jscale ) {
        int count = 0;
        int x1, x2, y1, y2, m;
        double t, r, d;
        ConductiveLayer layer;
        ListIterator<ConductiveLayer> itr = null;
        itr = asteroid.llayer.listIterator();

        x1 = 0;
        y1 = 0;
        t = 0;
        g.setColor( Color.black );
//      g.drawLine( centre-bradius, centre, centre, centre );
        g.setColor( Color.yellow );
        for ( int n=0; n<asteroid.nlayers; n++ ) {
            t = asteroid.graphicLayer[n][4];
            y1 = xtoa + (int)(( asteroid.graphicLayer[0][2] - asteroid.graphicLayer[n][2] ) * jscale );
            x1 = (int)( asteroid.graphicLayer[n][4] / 50.0 );
            m = n+1;
            if ( m == asteroid.nlayers ) m = n;
            y2 = xtoa + (int)(( asteroid.graphicLayer[0][2] - asteroid.graphicLayer[m][2] ) * jscale );
            x2 = (int)( asteroid.graphicLayer[m][4] / 50.0 );
            g.drawLine( x1, y1, x2, y2 );
        }
 //     g.drawLine( x1, y1, centre, y1 );
        g.drawString( "  " + t + " degrees K", x1, y1 );
        g.drawString( "  0 degrees K", x1, (ymax/2) );

    }



    Color setColour( int index, double phase ) {
        Color colour = Color.white;
        Material mt = new Material( index );
        int cindex = 0;
        if ( phase == 1 ) cindex = 1;
        if ( phase == 2 ) cindex = 2;
        colour = colorpalette[ mt.colourIndex[ cindex ] ];
        return( colour );
    }


}

// handle annual scheduled events (adapted from Orbit3D.java )
class EventHandler {

    Glaciation ap;
    double eventQueue[] = new double[500];
    int nextEvent = 0;
    int eventCount = 0;

    int repeatEventType = 0;
    double repeatEventCZYN;
    double repeatEventTotal[] = new double[10];
    double repeatEventCount;
    double repeatEventStartCZYN;
    double repeatEventStopCZYN;
    int nrepeatEvent = 0;
    double repeatEventMeasure[][] = new double[4][1000];
    int repeatEventBody = 11;
    double repeatEventInterval = 0;  // quarter hour intervals of day

    double timestepRemainder;
    double secsperday;

    double printAsteroidCZYN;
    double setBandNumCZYN;
    double rollingGraphCZYN;
    double printEventQueueCZYN;
    double layerDisplayCZYN;

    // default constructor
    public EventHandler() {}

    public EventHandler( Glaciation app ) {
        ap = app;

        printAsteroidCZYN = ap.eemianCZYN;
        this.insertEvent( printAsteroidCZYN );
        System.out.println( "printAsteroidCZYN " + (long)printAsteroidCZYN);

        setBandNumCZYN = ap.eemianCZYN + 9;
        this.insertEvent( setBandNumCZYN );
        System.out.println( "setBandNumCZYN " + (long)setBandNumCZYN);

        layerDisplayCZYN = ap.eemianCZYN + 10;
        this.insertEvent( layerDisplayCZYN );

        rollingGraphCZYN = ap.eemianCZYN + 1000;
        this.insertEvent( rollingGraphCZYN );

    }

    void setupRepeatEvent( int type, double startCZYN, int bn, double duration, double interval ) {
        repeatEventType = type;
        repeatEventBody = bn;
        if ( type == 1 ) {
            repeatEventCZYN = ap.currentCZYN;
            repeatEventTotal[0] = 0;
            repeatEventTotal[1] = 0;
            repeatEventTotal[2] = 0;
            repeatEventCount = 0;
            repeatEventStopCZYN = repeatEventCZYN + duration;         // record for some period of time
            repeatEventInterval = interval;
            repeatEventCZYN = ap.currentCZYN + ( interval );    // add quarter hour
            insertEvent( repeatEventCZYN );
            System.out.println( "currentCZYN " + ap.currentCZYN + " repeatEventCZYN scheduled for " + repeatEventCZYN );
            printEventQueue();
        } else if ( type == 2 || type == 3 || type == 4 || type == 5 ) {
            repeatEventStartCZYN = startCZYN;
            repeatEventStopCZYN = startCZYN + duration;
            repeatEventCount = 0;
            repeatEventInterval = interval;
            repeatEventCZYN = startCZYN;
            insertEvent( startCZYN );
            System.out.println(  "repeat event scheduled for " + startCZYN );
        }

    }


    // A variety of dates need to be accurately hit. These include:
    // 1. Historical dates on which known planetary body positions are corrected.
    // 2. Stop dates when state vectors are printed.
    // 3. Body state changes defined by b[n].initialCZYN and finalCZYN
    // 4. Rockcloud halo launch dates
    // The timestep manager keeps track of the nearest upcoming date from a variety of sources,
    // and, if necessary, alters dt in order to hit these dates.
    double timestepManager( double currentTimestep ) {
        double nextjd, timestepToEvent;
        double newTimestep = currentTimestep;

//      if ( pool.currentCZYN > 2454877.2 && pool.currentCZYN < 2454877.4) System.out.println( "***MANAGER " + pool.currentCZYN + " nextCZYN : " + eventQueue[nextEvent] + " dt " + currentTimestep );
//      System.out.println( "***MANAGER " + pool.currentCZYN + " nextCZYN : " + eventQueue[nextEvent] + " dt " + currentTimestep );

        // if there is a remaining timestep to complete, use it as the next timestep
        // so as to not break the timestep sequence.
        if ( timestepRemainder != 0.0 ) newTimestep = timestepRemainder;

        nextjd = eventQueue[nextEvent];
//      if ( nextjd <= pool.currentCZYN ) nextEvent++;  // if next event is before current date, try next event along.

        // if timestep length < current dt, return shortest.
        timestepToEvent = ( nextjd -   ap.currentCZYN ) * secsperday;
        if ( Math.abs(timestepToEvent) < Math.abs(newTimestep) ) {
            timestepRemainder = newTimestep - timestepToEvent;
            newTimestep = timestepToEvent;
//          if ( printManager )  {
//              if ( newTimestep != dt ) System.out.println( "MANAGER " + pool.currentCZYN + " new timestep : " + newTimestep + " current timestep : " + currentTimestep + " rem " + timestepRemainder );
//          }
        } else {
            timestepRemainder = 0;
        }
//      if ( pool.currentCZYN > 2454877.2 && pool.currentCZYN < 2454877.4) System.out.println( "MANAGER " + pool.currentCZYN + " nextjd " + nextjd + " timestep : " + newTimestep + " rem " + timestepRemainder );

        return( newTimestep );
    }


    double getNearestDate( double currentDate, double currentNearestDate, double offeredDate, double timestep ) {
        double newDate = currentNearestDate;
        if ( timestep > 0 ) {
            if ( offeredDate > currentDate ) {
                if ( offeredDate < currentNearestDate ) newDate = offeredDate;
            }
        } else {
            if ( offeredDate < currentDate ) {
               if ( offeredDate > currentNearestDate ) newDate = offeredDate;
            }
        }
        return( newDate );
    }


    // add event to event queue if it doesn't already exist
    void addEvent( double jd ) {
        int n;
        boolean found = false;
        for ( n=0; n<eventCount; n++ ) {
            if ( eventQueue[n] == jd ) {
                found = true;
                break;
            }
        }
        if ( !found ) {
            eventQueue[ eventCount ] = jd;
            eventCount++;
        }
    }

    // Insert Julian date jd into date-ordered event queue
    void insertEvent( double jd ) {
        int n, i;
        boolean found = false;
        if ( eventCount == 0 ) {
            eventQueue[0] = jd;                     // if no events, add new event
            eventCount++;
            eventQueue[1] = jd * 2.0;               // dummy terminal event
            eventCount++;
        } else {
            for ( n=0; n<eventCount; n++ ) {
                if ( eventQueue[n] == jd ) {
                    found = true;                           // event already exists
                    break;
                } else if ( eventQueue[n] > jd ) {
                    eventCount++;
                    for ( i=eventCount; i>n; i-- ) {
                        eventQueue[i] = eventQueue[i-1];    // move subsequent events down queue
                    }
                    eventQueue[n] = jd;                     // add new event
                    break;
                }
            }
        }
    }

    // Insert Julian date jd into date-ordered event queue before NextEvent pointer
    void insertNowEvent( double jd ) {
        for ( int i=eventCount; i>nextEvent; i-- ) {
            eventQueue[i] = eventQueue[i-1];    // move subsequent events down queue
        }
        eventQueue[nextEvent] = jd;                     // add new event
        eventCount++;
        nextEvent++;                                    // point to next event after it
    }

    // Insert Julian date jd into date-ordered event queue
    void insertIntermediateEvent( double jd ) {
        int n, i;
        boolean found = false;
        if ( jd > eventQueue[0] && jd < eventQueue[eventCount-1]) {
            for ( n=0; n<eventCount; n++ ) {
                if ( eventQueue[n] == jd ) {
                    found = true;                           // event already exists
                    break;
                } else if ( eventQueue[n] > jd ) {
                    eventCount++;
                    for ( i=eventCount; i>n; i-- ) {
                        eventQueue[i] = eventQueue[i-1];    // move subsequent events down queue
                    }
                    eventQueue[n] = jd;                     // add new event
                    break;
                }
            }
        }
    }


    // remove Julian date jd event from event queue
    void removeEvent( double jd ) {
        int n, i;
        boolean found = false;
        for ( n=0; n<eventCount; n++ ) {
            if ( eventQueue[n] == jd ) {
                for ( i=n; i<eventCount; i++ ) {
                    eventQueue[i] = eventQueue[i+1];    // move subsequent events up queue
                    eventQueue[i+1] = 0;
                }
                eventCount--;
                break;
            }
        }
    }

    void printEventQueue() {
        System.out.println( "Event Queue count = " + eventCount  );
        for ( int n=0; n<eventCount; n++ ) {
            System.out.println( n + " " + eventQueue[n] );
        }
        System.out.println( "nextEvent " + nextEvent );
    }

    // a variety of events can all happen at the same time,
    // so when an event occurs, a whole bunch of things need to be checked.
    void checkEventQueue() {
        double dt = 1.0;

        if ( nextEvent < 500 ) {
            if (  ap.currentCZYN == eventQueue[nextEvent] ) {
//              if ( ap.printManager ) System.out.println( ap.currentCZYN + " EVENT at " + ap.currentCZYN  );
                if ( ap.printManager ) System.out.println( ap.currentCZYN + " EVENT at " + ap.currentCZYN  );

                if ( ap.currentCZYN == printAsteroidCZYN ) {
                    ap.asteroid.printAsteroid( 0 );
                    printAsteroidCZYN += 12000;
                    this.insertEvent( printAsteroidCZYN );
                    if ( ap.printManager ) System.out.println( "print asteroid " );
                    this.printEventQueue();
                }

                if ( ap.currentCZYN == setBandNumCZYN ) {
                    ap.asteroid.setBandNumbers();
                    setBandNumCZYN += 1000;
                    this.insertEvent( setBandNumCZYN );
                    if ( ap.printManager ) System.out.println( "set band numbers " );
                }

                if ( ( ap.currentCZYN == (ap.eemianCZYN + 15007) ) || ( ap.currentCZYN == (ap.eemianCZYN + 15018) ) ) {
//                  ap.asteroid.addLayer( ap.asteroid.thinSurfaceBand, ap.asteroid.surfaceBandTemperature,  1000, 3 );
                    ap.asteroid.addLayer( ap.asteroid.thinSurfaceBand, 200,  1000, 3 );
                    System.out.println( "add glaciation " );
                }

                if ( ap.currentCZYN == layerDisplayCZYN ) {
                    ap.repaint();
                    layerDisplayCZYN += 100;
                    this.insertEvent( layerDisplayCZYN );
                    if ( ap.printManager ) System.out.println( "layerDisplay " );
                }

                if ( ap.currentCZYN == rollingGraphCZYN ) {
                    ap.rollingGraphOutput( ap.century );
                    ap.rollingGraphUpdated = true;
                    ap.repaint();
//                  ap.rollingGraphUpdated = false;
                    rollingGraphCZYN += ap.canvas1.calendarStep;
                    this.insertEvent( rollingGraphCZYN );
                    if ( ap.printManager ) System.out.println( (float)(ap.currentCZYN - ap.eemianCZYN) + " rollingGraph numbers " );
                }


                // initialise any bodies that need initialising on this date
//              pool.initialise();
                            // if timestep dt is positive, point to next following event, else previous event
/*
                if ( dt > 0 ) {
                    nextEvent++;
                    if ( nextEvent >= eventCount ) nextEvent = 0;
                    if ( ap.printManager ) System.out.println(" next event is at " + eventQueue[nextEvent] );
                } else {
                    nextEvent--;
                    if ( nextEvent < eventCount ) nextEvent = eventCount-1;
                    if ( ap.printManager ) System.out.println(" next event is at " + eventQueue[nextEvent] );
                }
*/
                removeEvent( ap.currentCZYN );
            }
        } else {
            nextEvent = 0; // prevent overflow
        }
    }


}


class SolarRadiation {
    // source: Solar Radiation Calculation Dr. Mohamad Kharseh
    // source: https://www.researchgate.net/file.PostFileLoader.html?id=553e4871d685ccd10e8b4618&assetKey=AS%3A273765705945088%401442282238044
    // mean terrestrial solar radiation of 342 W/m^2 = 2.95E7 W / m^2 day
    double timeStepTable[] = new double[1000];          // J
    int nTimeSteps;
    double timeStep;                                    // days
    double latitude;                                    // +/-degrees
    double solarConstant;                               // Watts / square metre
    int currentTimeStep;

    public SolarRadiation() { }

    // test code
    public SolarRadiation( double solarConstant ) {
        timeStepTerrestrialRadiation( solarConstant, 183.75, 185.25, 65 );
    }

    // create table of solar heat gains during each time step at given latiude
    // try to set integer number of timesteps per year (e.g. 1000 per year)
    public SolarRadiation( double solarConstant, double latitude, double timestepYears, double daysPerYear ) {
        double ho;
        double timestepDays = daysPerYear * timestepYears;
        int nTimestepsPerYear = (int)( daysPerYear / timestepDays );
        this.solarConstant = solarConstant;
        this.latitude = latitude;
        this.timeStep = timestepDays;
        this.nTimeSteps = nTimestepsPerYear;
        this.currentTimeStep = 0;
        double daynum = 0;
        double totho = 0;
        int step = 0;
        System.out.println( "timestepDays " + timestepDays + " steps per year " + nTimestepsPerYear );
        for ( int s=1; s<=nTimestepsPerYear; s++ ) {
            ho = timeStepTerrestrialRadiation( solarConstant, daynum, daynum+timestepDays, latitude );
//          System.out.println( (float)daynum + ": " + (int)ho );
            System.out.printf( "% 6.2f", daynum  );
            System.out.printf("% 9d", (int)ho );
            System.out.println();
            totho += ho;
            daynum += timestepDays;
            timeStepTable[ step ] = ho;
            step++;
        }
        System.out.println( "total solar gain " + (int)totho + " mean power (watts) " + totho/31536000.0  );
        timeStepTable[ step ] = -1;  // terminator character if needed
//      printTimeStepTable();
    }

    void printTimeStepTable() {
        int n = 0;
        System.out.println( "timeStepTable:");
        while ( this.timeStepTable[n] >= 0 ) {
            System.out.printf("% 9d", n );
            System.out.printf("% 9d", (int)this.timeStepTable[n] );
            System.out.println();
            n++;
        }
    }

    // return the solar gain for the current time step, and update the current time step number
    double getNextSolarGain() {
        double solargain = this.timeStepTable[ this.currentTimeStep ];
        this.currentTimeStep++;
        if ( this.timeStepTable[ this.currentTimeStep ] < 0 ) this.currentTimeStep = 0;
        return( solargain );
    }


/*
    void annualTerrestrialRadiation( double solarConstant ) {
        int nlat = 0;
        double nday, ho, totho, maxho;
        double step2 = 1.0;  // multiples of a day
        double step1 = 2.5;   // degrrees latitude
        double maxday = 0;
        System.out.println( "Annual solar gain at each latitude "  );
        for ( double latitude=0; latitude<=90; latitude=latitude + step1 ) {
            nday = 0;
            maxho = 0;
            totho = 0;
            for ( double n=0; n<=365; n = n + step2 ) {
                ho = dailyExtraTerrestrialRadiation( solarConstant, n, latitude );
                totho += ho;
//              data[ nlat][ nday ] = (int)ho;
                if ( ho > maxho ) {
                    maxday = nday;
                    maxho = ho;
                }
                nday = nday + step2;
//              System.out.print( (int)ho + " " );
            }
            System.out.println( latitude + ": " + (int)totho );
            nlat++;
        }

    }
*/
    // tested, working for non-integer timesteps of n days.
    // use to create a timestepTable of nsteps of terrestrial solar heat gains at different latitudes
    double timeStepTerrestrialRadiation(  double solarConstant, double dn1, double dn2, double latitude ) {
        double toth = 0;
        double h = 0;

        // parse start and stop day numbers
        if ( dn2 > dn1 ) {

            // start day
            boolean s1i = false;
            int sd1 = (int)dn1;
            double f11 = dn1 - (int)dn1;                            // remaining fraction of first day
            if ( f11 == 0 ) s1i = true;                             // integer start day
            f11 = fractionAngle( f11 );
            double f12 = 180;
            // stop day
            boolean s2i = false;
            int sd2 = (int)dn2;
            double f22 = dn2 - (int)dn2;
            if ( f22 == 0 ) s2i = true;                             // integer stop day
            f22 = fractionAngle( f22 );                                 // initial fraction of last day
            double f21 = -180;

//          System.out.println( dn1 + " to " + dn2 + ": " + sd1 + " " + s1i + " + " + sd2  + " " + s2i  );
            if ( sd1 == sd2 ) {
                // period is fraction of one day, sd1
                f12 = f22;
//              System.out.println( sd1 + ": " + f11 + " to " + f12 + " same day" );
                h = hourlyExtraTerrestrialRadiation( solarConstant, sd1, f11, f12, latitude  );
//              System.out.println( "h = " + (int)h );
                toth += h;
            } else {
                int wd1 = sd1 + 1;
                int wd2 = sd2 - 1;

                if ( !s1i ) {
                    // start day fraction
//                  System.out.println( sd1 + ": " + f11 + " to " + f12 + " start" );
                    h = hourlyExtraTerrestrialRadiation( solarConstant, sd1, f11, f12, latitude  );
//                  System.out.println( "h = " + (int)h );
                    toth += h;
                } else {
                    wd1 = sd1;
                }
                // whole days
                for ( int n=wd1; n<=wd2; n++ ) {
//                  System.out.println( n + " whole" );
                    h = dailyExtraTerrestrialRadiation( solarConstant, wd1, latitude );
//                  System.out.println( "h = " + (int)h );
                    toth += h;
                }
                // stop day fraction
                if ( !s2i ) {
//                 System.out.println( sd2 + ": " + f21 + " to " + f22 + " stop" );
                    h = hourlyExtraTerrestrialRadiation( solarConstant, sd2, f21, f22, latitude  );
//                  System.out.println( "h = " + (int)h );
                    toth += h;
                } else {
                    wd1 = sd1;
                }

            }
//          System.out.println( "toth = " + (int)toth );

        } else {
            System.out.println( "invalid day numbers");
        }

        return( toth );
    }

    // when fraction is 0.5, this corresponds to noon, hour angle 0
    // when fraction is 0 or 1, hour angle = 180
    double fractionAngle( double fraction ) {
        double angle = ( fraction - 0.5 ) * 360.0;
//      System.out.println( fraction + " a " + angle );
        return( angle );
    }

    // degrees are coverted to radians
    // tested working (produces slighly smaller values over a day than dailETradiation))
    double hourlyExtraTerrestrialRadiation( double solarConstant, double dayNumber, double ha1, double ha2, double latitude  ) {
        double hourlyRadiation = 0;
        double hourAngle1 = degreesToRadians( ha1 );
        double hourAngle2 = degreesToRadians( ha2 );
        double lat = degreesToRadians( latitude );                              // radians
        double declination = declination( dayNumber );                          // radians
        double sunsetHourAngle = hourAngle( lat, declination );                 // radians
        double hoursSunlight = 2.0 * radiansToDegrees( sunsetHourAngle ) / 15.0;
        double f1 = 12.0 * 3600.0 * solarConstant / Math.PI;
        double f2 = 1.0 + 0.033 * Math.cos ( degreesToRadians( ( 360.0 * dayNumber ) / 365.0 ) );
        double f3 = Math.cos(lat) * Math.cos(declination)* ( Math.sin(hourAngle2) - Math.sin(hourAngle1 ) );
        double f4 = ( hourAngle2 - hourAngle1 ) * Math.sin(lat) * Math.sin(declination);
        hourlyRadiation = f1 * f2 * ( f3 + f4 );
        if ( hourlyRadiation < 0 ) hourlyRadiation = 0;
        return( hourlyRadiation );
    }

    // degrees are coverted to radians
    // tested working
    double dailyExtraTerrestrialRadiation( double solarConstant, double dayNumber, double latitude  ) {
        double dailyRadiation;
        double lat = degreesToRadians( latitude );                              // radians
        double declination = declination( dayNumber );                          // radians
        double sunsetHourAngle = hourAngle( lat, declination );                 // radians
        double f1 = 24.0 * 3600.0 * solarConstant / Math.PI;
        double f2 = 1.0 + 0.033 * Math.cos ( degreesToRadians( ( 360.0 * dayNumber ) / 365.0 ) );
        double f3 = Math.cos(lat) * Math.cos(declination)* Math.sin(sunsetHourAngle );
        double f4 = sunsetHourAngle * Math.sin(lat) * Math.sin(declination);
        // was f4 = Math.PI * sunsetHourAngle(degrees) * Math.sin(lat) * Math.sin(declination ) / 180.0;
        dailyRadiation = f1 * f2 * ( f3 + f4 );
//      if ( sunsetHourAngle == 0 ) {
//          System.out.println( latitude + " sunsetHourAngle " + sunsetHourAngle + " declination " + declination);
//      }
        return( dailyRadiation );
    }

    double instantaneousExtraTerrestrialRadiation( double solarConstant, double dayNumber, double hourAngle, double latitude ) {
        double instantaneousRadiation;
        double hourAngleR = degreesToRadians( hourAngle );
        double lat = degreesToRadians( latitude );                              // radians
        double declination = declination( dayNumber );                          // radians
        double f1 = solarConstant;
        double f2 = 1.0 + 0.033 * Math.cos ( degreesToRadians( ( 360.0 * dayNumber ) / 365.0 ) );
        double f3 = Math.cos(lat) * Math.cos(declination)* Math.cos(hourAngleR);
        double f4 = Math.sin(lat) * Math.sin(declination);
        instantaneousRadiation = f1 * f2 * ( f3 + f4 );
        if ( instantaneousRadiation < 0 ) instantaneousRadiation = 0;
        return( instantaneousRadiation );
    }

    // Declination is the angle made between the plane of the equator
    // and the line joining the two centres of the earth and the sun
    // tested working
    double declination( double dn ) {
        double d = 23.45 * Math.sin( degreesToRadians( 360 * ( 284.0 + dn ) / 365.0 ) );
        return( degreesToRadians( d ) );
    }

    // The hour angle is the sun’s angular deviation from south
    // tested working
    double hourAngle( double lat, double dec ) {
        double ha;
        double rightAngle = 0.5 * Math.PI;
        if ( lat >= 0 ) {
            // northern hemisphere (tested)
            if ( (lat-dec) > rightAngle ) {
                // sun never rises
                ha = 0;
            } else if ( (lat+dec) > rightAngle ) {
                // sun never sets
                ha =  Math.PI;
            } else {
                // sun rises and sets
                ha = Math.acos( -Math.tan(lat) * Math.tan(dec) );
            }

        } else {
            // southern hemisphere (untested)
            if ( (-lat-dec) > rightAngle ) {
                // sun never sets
                ha =  Math.PI;
            } else if ( (-lat+dec) > rightAngle ) {
                // sun never rises
                ha = 0;
            } else {
                // sun rises and sets
                ha = Math.acos( -Math.tan(lat) * Math.tan(dec) );
            }
        }

//      if ( lat == rightAngle ) System.out.println( "ha " + ha );
        return( ha );
    }

    double degreesToRadians( double d ) {
        double r = d * Math.PI / 180.0;
        return( r );
    }

    double radiansToDegrees( double r ) {
        double d = r * 180.0 / Math.PI;
        return( d );
    }

    // untested
    double arcos(double x) {
        double a = Math.atan(  ( Math.sqrt(1-x*x)  )  / x );
        return( a );
    }

    private void printDebugData(JTable table, double step1 ) {
        int numRows = table.getRowCount();
        int numCols = table.getColumnCount();
        javax.swing.table.TableModel model = table.getModel();

/*
        source: https://www.cs.colostate.edu/~cs160/.Summer16/resources/Java_printf_method_quick_reference.pdf
        Java printf( ) Method Quick Reference
        System.out.printf( “format-string” [, arg1, arg2, … ] );

        Format String:
        Composed of literals and format specifiers. Arguments are required only if there are format specifiers in the
        format string. Format specifiers include: flags, width, precision, and conversion characters in the following
        sequence:
        % [flags] [width] [.precision] conversion-character ( square brackets denote optional parameters )

        Flags:
        - : left-justify ( default is to right-justify )
        + : output a plus ( + ) or minus ( - ) sign for a numerical value
        0 : forces numerical values to be zero-padded ( default is blank padding )
        , : comma grouping separator (for numbers > 1000)
        : space will display a minus sign if the number is negative or a space if it is positive

        Width:
        Specifies the field width for outputting the argument and represents the minimum number of characters to
        be written to the output. Include space for expected commas and a decimal point in the determination of
        the width for numerical values.

        Precision:
        Used to restrict the output depending on the conversion. It specifies the number of digits of precision when
        outputting floating-point values or the length of a substring to extract from a String. Numbers are rounded
        to the specified precision.

        Conversion-Characters:
        d : decimal integer [byte, short, int, long]
        f : floating-point number [float, double]
        c : character Capital C will uppercase the letter
        s : String Capital S will uppercase all the letters in the string
        h : hashcode A hashcode is like an address. This is useful for printing a reference
        n : newline Platform specific newline character- use %n instead of \n for greater compatibility

        Examples:
        System.out.printf("Total is: $%,.2f%n", dblTotal);
        System.out.printf("Total: %-10.2f: ", dblTotal);
        System.out.printf("% 4d", intValue);
        System.out.printf("%20.10s\n", stringVal);
        String s = "Hello World";
        System.out.printf("The String object %s is at hash code %h%n", s, s);

        String class format( ) method:
        You can build a formatted String and assign it to a variable using the static format method in the String class.
        The use of a format string and argument list is identical to its use in the printf method. The format method
        returns a reference to a String. Example:
        String grandTotal = String.format("Grand Total: %,.2f", dblTotal);
*/


        System.out.println("Value of data: ");
        for (int i=0; i < numRows; i++) {
            System.out.printf( "% 6.1f", 2.5 + (double)(i*step1) );
            for (int j=0; j < numCols; j++) {
                System.out.printf("% 9d", model.getValueAt(i, j) );
//             System.out.print("  " + model.getValueAt(i, j));
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }
}


class AirMeltedIceSheet {
    double h;                   // ice sheet vertical depth
    double w;                   // ice sheet horizontal width
    double l;                   // ice sheet horizontal length
    double fi;                  // fragmentation index (0 no fragmentation, 1 isolation, 2 qurtering, etc))
    double t_ice = 273.0;       // ice temperature, degrees Kelvin
    double t_air;               // air temperature
    double d;                   // ice effective conductive depth
    double a;                   // ice conductive surface area
    double vol_ice;             // volume of ice m^3
    double m_ice;               // mass of ice  Kg
    double k_ice = 2.12;        // ice thermal conductivity J / m deg K
    double sh_ice = 2108;       // specific heat of ice J / Kg deg K
    double density_ice = 917;   // ice density kg / m^3
    double hf_ice = 3.33E5;     // heat of fusion of ice, J / Kg
    double hmelt;               // heat required to melt all ice (Joules)
    double secondsPerYear = 31557600;

    public AirMeltedIceSheet() { }

    // 1 km x 1 km sheet
    public AirMeltedIceSheet( double depth, double ifrag, double airTemp ) {
        this.w = 1000;
        this.l = 1000;
        this.h = depth;
        this.vol_ice = this.w * this.l * this.h;
        this.m_ice = this.vol_ice * this.density_ice;
        System.out.println( "this.vol_ice " + this.vol_ice + " this.m_ice " + this.m_ice );
        this.a = fragmentedSurfaceArea( ifrag );
        this.d = fragmentedMeanDepth( ifrag );
        System.out.println( "this.a " + this.a + " this.d " + this.d );
        this.t_air = airTemp;
        hmelt = ( (273 - t_ice) * m_ice + hf_ice ) * m_ice;
        System.out.println( "hmelt " + hmelt  );
        System.out.println( "Air temp " + this.t_air + " K " + ifrag + " fragmented " + this.h + " m deep ice sheet melting time (kyrs) " + (float)meltingTimeKyrs() );
    }

    double heatFlowRate() {
        return(  (t_air - t_ice) * 2.0 * k_ice * a / d );
    }


    double meltingTimeSeconds() {
        double t = hmelt / heatFlowRate();
        System.out.println( "melt time secs " + t  );
        return ( t );
    }

    double meltingTimeYears() {
        return ( meltingTimeSeconds() / secondsPerYear );
    }

    double meltingTimeCenturies() {
        return ( meltingTimeYears() / 100.0 );
    }

    double meltingTimeKyrs() {
        return ( meltingTimeYears() / 1000.0 );
    }

    double meltingTimeMyrs() {
        return ( meltingTimeYears() / 1000000.0 );
    }

    // fragmentation index really ought to be integer 0,
    // find surface area of fragmented ice sheet
    // no heat flow from underlying surface rock
    double fragmentedSurfaceArea( double ifrag ) {
        double nsurfaces = fragmentationSurfaces( ifrag );
        System.out.println( "nsurfaces " + nsurfaces  );
        // top surface + two sets of transverse vertical cracks/crevaasses
        double area = (w * l) + ( nsurfaces * l * h / 2.0) + (nsurfaces * w * h / 2.0);
        return( area );
    }

    // find mean thickness of ice in fragmented ice sheet
    // no heat flow from underlying surface rock
    double fragmentedMeanDepth( double ifrag ) {
        double meanThickness;
        double nsurfaces = fragmentationSurfaces( ifrag );
        double d1 = h;
        double a1 = l * w;
        meanThickness = h;
        if ( ifrag > 0 ) {
            double d2 = l / ifrag;
            double a2 = (nsurfaces * w * h / 2.0);
            double d3 = w / ifrag;
            double a3 = (nsurfaces * l * h / 2.0);
            // area-weighted mean thickness of ice
            meanThickness = ( ( d1 * a1) + ( d2 * a2) + ( d3 * a3) ) / (a1 + a2 + a3);
        }
        return( meanThickness );
    }


    double fragmentationSurfaces( double ifrag ) {
        // if ice sheet has fragnetation index of 0, there sare no cracks in it
        // if ice sheet has fragnetation index of 1, there is a crack all around it, so 4 cracks each with 1 surface, or 2 cracks wach with 2 surfaces
        // if ice sheet has fragnetation index of 2, there is a crack all around it, and 2 cracks across the middle of it each with 2 surfaces
        double numCracks = ifrag * 2.0;
        double numSurfaces = 2.0 * numCracks;
        return( numSurfaces );
    }

}
