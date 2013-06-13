# What is this?

JaamSim is a discrete-event simulation environment developed by Ausenco 
Sandwell as the foundation of all our simulation applications. It 
represents about 10 man-years of effort since 2002 and in under 
continuous development to support our simulation projects. JaamSim is 
used daily by our team of 16 simulation analysts and programmers. 

Examples of our simulation models can be seen at: 
www.youtube.com/user/javasimulation. 

JaamSim is written in the Java programming language and includes a 
drag-and-drop user interface, interactive 3D graphics, input and output 
processing, and model development tools and editors. 

The key feature that makes JaamSim different from commercial off-the-shelf 
simulation software is that it allows a user to develop new pallets of 
high-level objects for a given application. These objects will automatically 
have 3D graphics, be available in the drag-and-drop interface, and have their 
inputs editable through the Input Editor. Users can focus on the logic for their 
objects without having to program a user interface and input/output processing. 

All the coding for new objects is done in standard Java using standard 
development tools such as Eclipse. There is no need for the specialised 
simulation languages, process flow diagrams, or scripting languages used by 
commercial off-the-shelf simulation software. Model logic can be coded directly 
in either a event- or process-oriented style using a few simple classes and 
methods provided by JaamSim. 

This first release includes only a few graphical objects that can be 
dragged-and-dropped. Pallets of basic objects such as queues, servers, etc. will 
be added in the next few months. 

# Dependencies and Installation

A copy of all dependencies is shipped in the jar/ folder and are as follows:

jogl2 - from the jogamp.org people

Simply run the build.sh script from the top level to produce a functional jar 'JaamSim.jar',
you'll need to have the prerequisites from the jar/ folder in you classpath.

To build it in another environment, link the jar/ folder as an external 
classpath folder, or download your own copy of JOGL2.


# License

JaamSim is GPLv3

# Contact

For licensing and general inquiries:
Dr. Harry King <harry.king@ausenco.com>

If you have any problems building the source or technical inquiries:
Harvey Harrison <harvey.harrison@ausenco.com>

# Todo

- Further tightening of the code now that JaamSim is a standalone application
- Pallets of basic objects such as queue, server, source, sink, etc.
- Inputs files for example models
- User manual
- Programming guide
