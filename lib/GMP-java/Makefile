Includes := -I /usr/include -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -I include
LIBS := -L /usr/lib -lgmp -L. -ljcl
CFLAGS := -Wall -shared -fPIC -Wl,-rpath,`pwd`
JAVA_FLAGS := -Xlint

all: jar

java: jni org/dfdeshom/math/*.java libgmpjava jni
	javac $(JAVA_FLAGS) org/dfdeshom/math/GMP.java org/dfdeshom/math/Pointer32.java org/dfdeshom/math/Pointer64.java	

jni:
	javac $(JAVA_FLAGS) org/dfdeshom/math/GMP.java
	javah -jni -d include org.dfdeshom.math.GMP 

libgmpjava:GMP.c libjcl
	gcc $(Includes)  GMP.c -o libnativegmp.so $(LIBS) $(CFLAGS)


libjcl:jcl.c
	gcc  $(Includes)  -L /usr/lib/ jcl.c -o libjcl.so $(CFLAGS)

jar: java
	mkdir -p target 
	jar cvf target/GMP.jar org/dfdeshom/math/GMP.class org/dfdeshom/math/Pointer*.class 

clean: 
	rm -rf org/dfdeshom/math/*.class *.so *~ target/*.jar include
	rm -rf org/dfdeshom/math/*.class

example: jar
	javac -cp target/*.jar example.java
	java  -Djava.library.path=.   example