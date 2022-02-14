GMP-Java is a general-purpose JNI wrapper to the GMP library. It's
heavily based on the wrapper written in GNU Classpath for Big
Integers (GNU Classpath uses GMP for BigInteger objects). This wrapper
does nothing more than make it easier to use GMP big integers from Java. 

The original copyright notices have been left intact. It should be
noted that this particular work is copyright (C) 2009 didier
deshommes <dfdeshom@gmail.com> and released under the GPL 2 or later. 


Building 
--------
Make sure `JAVA_HOME` is set in your environment.

You'll need a recent JVM, the GMP library and its sources to build
this module. To build it, simply type `make`. When the sources finish
building, you should have a jar file called `GMP.jar` that you
can use in your Java application now. 

Using
-----
You'll need to add `GMP.jar` to you classpath. You will also need to add
 the generated `lib` directory to your `java.library.path`. Example:

     $ javac -cp target/*.jar Program.java
     $ java  -Djava.library.path=gmp-java/ -cp gmp-java/ Program

The main exposed class is `org.dfdeshom.math.GMP`. This library follows the same conventions as GMP for
usage for efficiency reasons. In particular, all methods return
`void` and `GMP` objects are modified IN-PLACE. In general, inputs go
first in methods. Example:

    abs(GMP input, GMP result)

An example of using this library in java can be found in `example.java` which can be run using `make example`
