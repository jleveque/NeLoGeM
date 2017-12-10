## Setting up a development environment

1. Install NetBeans IDE (currently working with v8.2)
2. In NetBeans menu bar, click File -> Open Project and select the NeLoGeM directory

## Compiling/debugging/running

- In NetBeans toolbar, either select "Debug Project" or "Run Project"

- Application will open in a window and run

- Application can be closed by clicking the standard Windows close ("X") button

- Edit java files as desired. No need to save edits, as NetBeans automatically saves and compiles upon running or debugging

## Display options

- The first line in Dynamics.java is:

    `int option = 1;`

- Change the number to change the selected option. Options are as follows:

1. Shows Apollo 11 trajectory. It takes about 10 minutes to watch this unfold.
2. Shows view of Solar System with sun at centre and planet orbits very slowly appearing.
3. Shows view down onto Venus, which hardly rotates at all.
4. Shows view down on Earth. A high tower is erected on the site of Babylon. The tower then slowly falls down, dropping bodies down onto (and through) the Earth below. There is a lat-long grid map, and also an outline map of the world which includes a satellite at geostationary orbit.
5. View down onto Mars with rotating lat-long grid map.
6. View down onto Jupiter with rotating lat-long grid map.
7. View down onto Saturn with rotating lat-long grid map.
8. View down onto Uranus with rotating lat-long grid map.
9. View down onto Neptune with rotating lat-long grid map.
10. Same as 4 except that the view is of the surface reference frame in which the tower is positioned.

## Program operation outline

- Dynamics.java calculates the gravitational (and elastic) forces acting between a number of bodies in a solar system, and their consequent accelerations, velocities, and positions at DT time intervals.

- Bodies have positions, velocities, mass, radius, rotation axis, rotation period, etc. They may also have surface maps. Bodies may also be tied to other bodies by elastic ties.

- Collectives of bodies are called Processes. A pendulum, for example, might consist one body in free motion attached to a body fixed at some point above the surface of a planet. A geodesic dome, made up of a number of point masses held together by ties is other process. A piston engine would be another process made up of bodies and ties. A building is a process made up of a number of bodies held together by ties.

- Another sort of process might be a river flowing in a river valley, or a sea subject to tidal forces.

- An individual person or animal might be a set of body masses held together by ties.

## Files

- AxisMap.java
  - Some code to draw coloured XYZ axes (not used at the moment)
- Body.java
  - Where new bodies with barycentric XYZ locations and velocities are constructed, moved under gravitational forces, and painted to display
- Dynamics.java
  - The applcation class (contains `main()` method) with some additional calendar code
- EarthMap.java
  - A vector map of the surface of the Earth
- FallingTower.java
  - Constructs a tower of 48 bodies, and moves the tower round the Earth
- Map.java
  - Code to create maps on surface of planets
- Mathut.java
  - A bunch of mathematical utilities
- Process.java
  - Everything that happens is part of some Process or other. The solar system is a Process. The Falling Tower is a Process.
- RK4.java
  - RK4 code for body motions currently not in use.
- ReferenceFrame.java
  - Defines reference frames on the surface of planets
- SolarSystem.java
  - A snapshot of the solar system in 2005 (not in use)
- SolarSystemApollo11.java
  - A snapshot of the solar system in July 1969
- StateVector.java
  - State Vectors are XYZ and vx, vy, vz objects used in multiple ways
- Tetrahedron.java
  - A bunch of 4 bodies in free motion round the Sun. They were originally held together by elastic ties in a tetrahedron, but this requires the model to be run using small values of DT.
- Tie.java
  - Allows the construction of elastic ties between bodies
- VectorMap.java
  - Handles vector maps of surfaces of bodies
