# Type Erasure Demonstration

This project demonstrates type erasure of generics in Java. Generics were
introduced in Java 5 as a very useful way to ensure correctness of types
when using classes that are generic compatible.

However, this type checking is useful only during compilation and allow
the compiler or the IDE to warn the developer when type safety is being
violated. In fact, the compilation will not proceed until the typing issue
is fixed in the source code.

However at runtime, those generic types are _erased_ from memory. In other
words the runtime doesn't care what objects are used with a specific object
even when it violates what was defined by the developer at the time of
writing the code.

To be very clear about the subject, the reflection API still can retrieve
the type information about the class. It's not that the class metadata
doesn't contain the type information. The only point regarding type
erasure is that the runtime system (the JVM) does nothing to ensure
type safety using generics.

This project is a very simplistic Java program - no Maven or Gradle
necessary for dependency management. And this program compiles and
executes in any Java version after Java 5, but it was developed with
OpenJDK 11+28.
