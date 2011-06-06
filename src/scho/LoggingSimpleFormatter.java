package scho;

import java.util.*;
import java.util.logging.*;
import java.text.MessageFormat;


public class LoggingSimpleFormatter extends SimpleFormatter {

  /**
   *  the string to use for new lines defaults to \n if not specified
   */
  private static String newLine = "\n";

  static {
    try {
      newLine = (String) java.security.AccessController.doPrivileged(
          new sun.security.action.GetPropertyAction("line.separator"));
    } catch (Throwable t) {
      // print an error
      // LogStdStreams initialized in the main application will redirect this to a file.
      System.err.println("Error getting system line separator for Logging will use \\n\n" + t.toString());
    }
  }

  /**
   *  the date object for the time/date output
   */
  Date date = new Date();
  /**
   *  the date time format
   */
  private final static String format = "{0,date} {0,time}";

  /**
   *  the formatter for the date and time
   */
  private MessageFormat formatter = null;

  /**
   *  the arguments for the date time formatter
   */
  private Object args[] = new Object[1];


  //~ Methods ..................................................................

  /**
   *  Returns the newLine setting for this class use system newLine
   *
   *@return    the string to use for new lines.
   */
  protected String newLineString() {
    return newLine;
  }


  /**
   *  Format the given LogRecord. Don't allow any errors to be thrown here
   *
   *@param  record  the log record to be formatted.
   *@return         a formatted log record
   */
  public synchronized String format(LogRecord record) {
    try {
      StringBuffer sb = new StringBuffer();
      StringBuffer text = new StringBuffer();
      if (formatter == null) {
        formatter = new MessageFormat(format);
      }
//      try {
//        date.setTime(record.getMillis());
//        args[0] = date;
//        formatter.format(args, text, null);
//      } catch (Throwable t1) {
//        text.append("Error formatting record date and time" + newLineString() + t1.toString());
//      }
//      sb.append(text);
//      sb.append(" ");
//      try {
//        if (record.getSourceClassName() != null) {
//          sb.append(record.getSourceClassName());
//        } else {
//          sb.append(record.getLoggerName());
//        }
//      } catch (Throwable t2) {
//        sb.append("Error getting class name" + newLineString() + t2.toString());
//      }
//      try {
//        if (record.getSourceMethodName() != null) {
//          sb.append(" ");
//          sb.append(record.getSourceMethodName());
//        }
//      } catch (Throwable t3) {
//        sb.append("Error getting method name" + newLineString() + t3.toString());
//      }
//
//      sb.append(newLineString());
//      try {
//        sb.append(record.getLevel().getLocalizedName());
//      } catch (Throwable t4) {
//        sb.append("Error getting localized level name" + newLineString() + t4.toString());
//      }
//      sb.append(": ");
      try {
        sb.append(formatMessage(record));
      } catch (Throwable t5) {
        sb.append("Error formatting record message" + newLineString() + t5.toString());
      }
      sb.append(newLineString());
//      try {
//        Throwable t = record.getThrown();
//        if (t != null) {
//          sb.append(t.toString());
//        }
//      } catch (Throwable t6) {
//        sb.append("Error getting record exception thrown" + newLineString() + t6.toString());
//      }
//      sb.append(newLineString());
      return sb.toString();
    } catch (Throwable t) {
      return ("Unexpected error caught while trying to log a record" + newLineString() + t.toString());
    }
  }

}

