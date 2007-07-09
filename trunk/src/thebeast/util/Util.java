package thebeast.util;

import java.util.Collection;

/**
 * @author Sebastian Riedel
 */
public class Util {

  public static String toStringWithDelimiters(Collection objects, String delim) {
    StringBuffer buffer = new StringBuffer();
    int index = 0;
    for (Object obj : objects) {
      if (index++ > 0) buffer.append(delim);
      buffer.append(obj);
    }
    return buffer.toString();
  }

  public static String toMemoryString(double bytes) {
    double display = bytes;
    if (display < 1024)
      return display + "b";
    display /= 1024.0;
    if (display < 1024)
      return String.format("%-3.3f",display) + "kb";
    display /= 1024.0;
    if (display < 1024)
      return String.format("%-3.3f",display) + "mb";
    display /= 1024.0;
    return String.format("%-3.3f",display) + "gb";

  }


}
