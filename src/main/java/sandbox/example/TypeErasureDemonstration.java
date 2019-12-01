package sandbox.example;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * This class highlights how type erasure happens on collections in the JVM runtime and what happens when
 * an object of the wrong type is inserted into the collection. Because of how generics work in the Java
 * compilation process, note that this example of inserting a wrong type of value into the collection is
 * nearly impossible without using reflection.
 *
 * @author Steve Afrin
 * @version 1.0.0
 */
public class TypeErasureDemonstration {

  final List<String> strings;

  private TypeErasureDemonstration()
  {
    this.strings = new ArrayList<>();
  }

  /**
   * The worst possible use of reflection. Reflection is a powerful and useful tool, but should never be
   * used for the purposes of what's being demonstrated here.
   */
  private void neverDoThis()
  {
    try {
      /*
       * Everything about these two lines is a horrible code smell, but this code demonstrates the power
       * and the horrible abuse that can come with improper use of reflection. This code executes only at
       * runtime, so the JVM runtime has no idea that an Integer type should not be added to the defined
       * strings collection. This is what type erasure in the JVM runtime means.
       *
       * This code is in a try block only because the possible exceptions that can be thrown are caught
       * exceptions and must be declared by this method to be thrown or must be handled here, but in this
       * example application, those exceptions will never be thrown. This code will execute flawlessly.
       */
      final Field localStrings = TypeErasureDemonstration.class.getDeclaredField("strings");
      ((List) localStrings.get(this)).add(5);
    } catch (NoSuchFieldException | IllegalAccessException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * <p>This method shows what happens when invoking a method on the wrong type of object. This essentially
   * is a &quot;method unknown&quot; type of error for the runtime system.</p>
   * <p>Normally this error cannot happen without using the reflection API because the compiler will catch
   * and flag all instances of trying to invoke an invalid method name on a specific type of object at
   * compilation time.</p>
   */
  private void dontDoThisEither()
  {
    // This is a totally valid method to invoke on a String type object.
    final String methodName = "length";

    /*
     * Note that we can easily access the actual Integer object here in the code by referencing its position in
     * the strings collection, but we have to make it an Object type of reference. If we try to define it as a
     * String type of object here, a ClassCastException will be thrown. We don't want to do that here because
     * that particular type of casting exception will actually be shown later on at line 172 in the
     * getReverseStringsValue method further below in the code. This particular reference here helps to show
     * what happens when an assumption is made about the object type that is returned, and as a consequence,
     * an invalid method is invoked on that object.
     */
    final Object fourthElementOfStringsList = this.strings.get(3);

    try {
      final Method lengthMethod = String.class.getMethod(methodName);

      /*
       * This is the precise line where an IllegalArgumentException is thrown because we try to invoke a
       * String#length() method on an Integer type object. This is another example where the compiler time
       * type checking helps us to avoid these types of runtime problems.
       */
      lengthMethod.invoke(fourthElementOfStringsList);
    } catch (IllegalArgumentException ex) {
      final String thrownExceptionName = ex.getClass().getSimpleName();

      /*
       * This line shows that the runtime system (the JVM) maintains type information of Java objects on the
       * heap, but it does not reinforce rules of which methods are callable on the specific object type until
       * execution time.
       */
      final String nameOfWrongClass = fourthElementOfStringsList.getClass().getCanonicalName();
      System.out.println(
        String.format("%s: Tried to invoke method '%s' on object '%s', but '%s' is not a valid method for an "
            + "object of type '%s', so that's what causes this %s exception", thrownExceptionName, methodName,
          fourthElementOfStringsList, methodName, nameOfWrongClass, thrownExceptionName)
      );
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Adds values to the strings collection for this instance.
   */
  private void initializeStrings()
  {
    this.strings.add("string value 1");
    this.strings.add("string value 2");
    this.strings.add("string value 3");

    /*
     * This line would normally be rejected by the Java compiler because the Integer type is non-compatible
     * with the String type that has been declared for the strings collection. Go ahead and uncomment this
     * line just to see what your IDE or the Java compiler tells you about this line. Then re-comment this
     * line so the program can compile and execute.
     *
     * This is the reason that generics were introduced to the Java language in Java 5. Generics provide
     * a compile-time opportunity to detect these types of object incompatibilities when trying to insert
     * values into a collection.
     */
    // this.strings.add(5);

    /*
     * This method call uses reflection to get a reference to the local strings collection instance and
     * adds the Integer type value to the collection even though the collection is clearly defined in this
     * class to allow only String type values to be added to it.
     */
    this.neverDoThis();

    this.strings.add("string value 4");
    this.strings.add("string value 5");
  }

  /**
   * Generates and returns a string that contains a list of all the values in the strings
   * collection for this object.
   * @return a list of all values in the strings collection for this object
   */
  private String getStringsValue()
  {
    final StringBuilder stringBuilder = new StringBuilder("The Strings collection value is: [");

    /*
     * This method works fine and throws no exceptions, but only because we treat each element of
     * the strings collection as an Object type instead of a String type. This works, but it does
     * not conform with the List<String> definition of the strings class attribute.
     */
    for (final Object value : this.strings) {
      stringBuilder.append("'").append(value).append("', ");
    }

    final int lastCommaPosition = stringBuilder.lastIndexOf(",");
    // Removes the final ", " sequence from the StringBuilder instance.
    stringBuilder.delete(lastCommaPosition, lastCommaPosition + 2);
    return stringBuilder.append(']').toString();
  }

  /**
   * Generates and returns a string that contains a list of all reversed String type values in the
   * strings collection for this object.
   * @return a list of all reversed String type values in the strings collection for this object
   */
  private String getReverseStringsValue()
  {
    final StringBuilder stringBuilder
      = new StringBuilder("The Strings collection with values in reverse order is: ['");

    /*
     * This is the point where we expect a ClassCastException (a specific type of runtime exception)
     * to be thrown when the element of the strings collection that is an Integer type is encountered.
     */
    for (final String value : this.strings) {
      stringBuilder
        .append("'")
        .append(new StringBuilder(value).reverse().toString())
        .append("', ");
    }

    final int lastCommaPosition = stringBuilder.lastIndexOf(",");
    // Removes the final ", " sequence from the StringBuilder instance.
    stringBuilder.delete(lastCommaPosition, lastCommaPosition + 2);
    return stringBuilder.append(']').toString();
  }

  /**
   * A simple entry point for this program to begin execution.
   * @param args an array of String type values from the command line; not used for this application
   */
  public static void main(final String[] args)
  {
    final TypeErasureDemonstration example = new TypeErasureDemonstration();
    example.initializeStrings();

    /*
     * This method invocation helps show that String and Integer types coexist in the strings
     * collection instance.
     */
    System.out.println(example.getStringsValue());

    /*
     * This method invocation helps show that when non-homogeneous type objects coexist in a
     * collection, assumptions about what methods can be invoked on those objects also cause
     * runtime exceptions. In the situation where an object in the runtime system tries to be
     * cast to a wrong/incompatible type, a ClassCastException is thrown by the JVM, but in
     * the scenario where an invalid method tries to be invoked on a wrong type of object, an
     * IllegalArgumentException is thrown by the JVM.
     */
    example.dontDoThisEither();

    /*
     * This method invocation shows how the Integer type value that was inserted into the strings
     * collection breaks downstream processing based on a reasonable expectation of the type of values
     * that should be in the collection.
     */
    System.out.println(example.getReverseStringsValue());
  }
}
