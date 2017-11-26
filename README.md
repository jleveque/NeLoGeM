Netbeans usage:

    Import the Dynamics.zip file.

    Open Dynamics.java by double-clicking on file in Projects list

    Select Run -> run file.

    Applet window will open and run.

    Stop run by via X close on applet window

    Edit java files as desired. No need to save edits, as Netbeans automatically saves and compiles


Display options:

    The first line in Dynamics.java is

    int option = 1;

    Change the number to change the selected option. There are 9 or 10 possible options.

    1. Shows Apollo 11 trajectory. It takes about 10 minutes to watch this unfold.
    2. Shows view of Solar System with sun at centre and planet orbits very slowly appearing.
    3. Shows view down onto Venus, which hardly rotates at all.
    4. Shows view down on Earth. A high tower is erected on the site of Babylon. 
       The tower then slowly falls down, dropping bodies down onto (and through) the Earth below.
       There is a lat-long grid map, and also an outline map of the world which includes a
       satellite at geostationary orbit.
    5. View down onto Mars with rotating lat-long grid map.
    6. View down onto Jupiter with rotating lat-long grid map.
    7. View down onto Saturn with rotating lat-long grid map.
    8. View down onto Uranus with rotating lat-long grid map.
    9. View down onto Neptune with rotating lat-long grid map. 
    10. Same as 4 except that the view is of the surface reference frame in which the tower is positioned.

Program outline operation:

    Dynamics.java calculates the gravitational (and elastic) forces acting between a number of bodies in a 
    solar system, and their consequent accelerations, velocities, and positions at DT time intervals.

    Bodies have positions, velocities, mass, radius, rotation axis, rotation period, etc. 
    They may also have surface maps. Bodies may also be tied to other bodies by elastic ties.

    Collectives of bodies are called Processes. A pendulum, for example, might consist one body in free
    motion attached to a body fixed at some point above the surface of a planet. A geodesic dome,
    made up of a number of point masses held together by ties is other process. A piston engine would
    be another process made up of bodies and ties. A building is a process made up of a number of bodies
    held together by ties.

    Another sort of process might be a river flowing in a river valley, or a sea subject to tidal forces.

    An individual person or animal might be a set of body masses held together by ties.

Files:

    AxisMap.java is some code to draw coloured XYZ axes (not used at the moment)
    Body.java is is where new bodies with barycentric XYZ locations and velocities are constructed, 
        moved under gravitational forces, and painted to display.
    Dynamics.java is the applet with some additional calendar code.
    EarthMap.java is a vector map of the surface of the Earth.
    FallingTower.java constructs a tower of 48 bodies, and moves the tower round the Earth
    Map.java is code to create maps on surface of planets.
    Mathut.java is a bunch of mathematical utilities.
    Process.java. Everything that happens is part of some Process or other. 
        The solar system is a Process. The Falling Tower is a Process. 
    RK4.java is RK4 code for body motions currently not in use.
    ReferenceFrame.java defines reference frames on the surface of planets
    SolarSystem.java is a snapshot of the solar system in 2005 (not in use)
    SolarSystemApollo11.java is a snapshot of the solar system in July 1969 
    StateVector.java. State Vectors are XYZ and vx, vy, vz objects used in multiple ways
    Tetrahedron.java is a bunch of 4 bodies in free motion round the Sun. They were originally held 
        together by elastic ties in a tetrahedron, but this requires the model to be run using small 
        values of DT.
    Tie.java allows the construction of elastic ties between bodies
    VectorMap.java handles vector maps of surfaces of bodies 