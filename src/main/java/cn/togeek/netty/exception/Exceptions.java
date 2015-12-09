package cn.togeek.netty.exception;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.nio.file.NoSuchFileException;

import cn.togeek.netty.util.ByteBufs;

import io.netty.buffer.ByteBuf;

public class Exceptions {
   public static void writeThrowable(Throwable throwable, ByteBuf out) {
      if(throwable == null) {
         out.writeBoolean(false);
      }
      else {
         out.writeBoolean(true);
         boolean writeCause = true;
         boolean writeMessage = true;

         if(throwable instanceof NullPointerException) {
            out.writeInt(1);
            writeCause = false;
         }
         else if(throwable instanceof NumberFormatException) {
            out.writeInt(2);
            writeCause = false;
         }
         else if(throwable instanceof IllegalArgumentException) {
            out.writeInt(3);
         }
         else if(throwable instanceof EOFException) {
            out.writeInt(4);
            writeCause = false;
         }
         else if(throwable instanceof SecurityException) {
            out.writeInt(5);
         }
         else if(throwable instanceof StringIndexOutOfBoundsException) {
            out.writeInt(6);
            writeCause = false;
         }
         else if(throwable instanceof ArrayIndexOutOfBoundsException) {
            out.writeInt(7);
            writeCause = false;
         }
         else if(throwable instanceof AssertionError) {
            out.writeInt(8);
         }
         else if(throwable instanceof FileNotFoundException) {
            out.writeInt(9);
            writeCause = false;
         }
         else if(throwable instanceof NoSuchFileException) {
            out.writeInt(10);
            NoSuchFileException exception = (NoSuchFileException) throwable;
            ByteBufs.writeString(exception.getFile(), out);
            ByteBufs.writeString(exception.getOtherFile(), out);
            ByteBufs.writeString(exception.getReason(), out);
            writeCause = false;
         }
         else if(throwable instanceof OutOfMemoryError) {
            out.writeInt(11);
            writeCause = false;
         }
         else if(throwable instanceof IllegalStateException) {
            out.writeInt(12);
         }
         else if(throwable instanceof InterruptedException) {
            out.writeInt(13);
            writeCause = false;
         }
         else if(throwable instanceof ArithmeticException) {
            out.writeInt(14);
            writeCause = false;
         }
         else {
            out.writeInt(0);
            BaseException exception;

            if(throwable instanceof BaseException) {
               exception = (BaseException) throwable;
            }
            else {
               exception = new NotSerializableExceptionWrapper(throwable);
            }

            exception.writeTo(out);
            return;
         }

         if(writeMessage) {
            ByteBufs.writeString(throwable.getMessage(), out);
         }

         if(writeCause) {
            writeThrowable(throwable.getCause(), out);
         }

         writeStackTraces(throwable, out);
      }
   }

   /**
    * Serializes the given exceptions stacktrace elements as well as it's
    * suppressed exceptions to the given output stream.
    */
   static <T extends Throwable> void writeStackTraces(T throwable,
                                                      ByteBuf out)
   {
      StackTraceElement[] stackTrace = throwable.getStackTrace();
      out.writeInt(stackTrace.length);

      for(StackTraceElement element : stackTrace) {
         ByteBufs.writeString(element.getClassName(), out);
         ByteBufs.writeString(element.getFileName(), out);
         ByteBufs.writeString(element.getMethodName(), out);
         out.writeInt(element.getLineNumber());
      }

      Throwable[] suppressed = throwable.getSuppressed();
      out.writeInt(suppressed.length);

      for(Throwable t : suppressed) {
         writeThrowable(t, out);
      }
   }

   @SuppressWarnings("unchecked")
   public static <T extends Throwable> T readThrowable(ByteBuf in) {
      if(in.readBoolean()) {
         int key = in.readInt();

         switch(key) {
         case 0 :
            final String declaringClass = ByteBufs.readString(in);
            BaseException throwable = null;

            try {
               Class<BaseException> clazz =
                  (Class<BaseException>) Class.forName(declaringClass);
               Constructor<BaseException> cons =
                  clazz.getConstructor(String.class, Throwable.class);
               throwable = cons.newInstance(
                  ByteBufs.readString(in), readThrowable(in));
               throwable = readStackTrace(throwable, in);
               throwable.readFrom(in);
            }
            catch(Exception e) {
               throw new RuntimeException(
                  "no such exception [" + declaringClass + "]");
            }

            return (T) throwable;
         case 1 :
            return (T) readStackTrace(
               new NullPointerException(ByteBufs.readString(in)), in);
         case 2 :
            return (T) readStackTrace(
               new NumberFormatException(ByteBufs.readString(in)), in);
         case 3 :
            return (T) readStackTrace(new IllegalArgumentException(
               ByteBufs.readString(in), readThrowable(in)), in);
         case 4 :
            return (T) readStackTrace(
               new EOFException(ByteBufs.readString(in)), in);
         case 5 :
            return (T) readStackTrace(
               new SecurityException(ByteBufs.readString(in),
                  readThrowable(in)),
               in);
         case 6 :
            return (T) readStackTrace(
               new StringIndexOutOfBoundsException(ByteBufs.readString(in)),
               in);
         case 7 :
            return (T) readStackTrace(
               new ArrayIndexOutOfBoundsException(ByteBufs.readString(in)), in);
         case 8 :
            return (T) readStackTrace(
               new AssertionError(ByteBufs.readString(in), readThrowable(in)),
               in);
         case 9 :
            return (T) readStackTrace(
               new FileNotFoundException(ByteBufs.readString(in)), in);
         case 10 :
            final String file = ByteBufs.readString(in);
            final String other = ByteBufs.readString(in);
            final String reason = ByteBufs.readString(in);
            ByteBufs.readString(in); // skip the msg - it's composed from file,
                                     // other and reason
            return (T) readStackTrace(
               new NoSuchFileException(file, other, reason), in);
         case 11 :
            return (T) readStackTrace(
               new OutOfMemoryError(ByteBufs.readString(in)), in);
         case 12 :
            return (T) readStackTrace(
               new IllegalStateException(ByteBufs.readString(in),
                  readThrowable(in)),
               in);
         case 13 :
            return (T) readStackTrace(
               new InterruptedException(ByteBufs.readString(in)), in);
         case 14 :
            return (T) readStackTrace(
               new ArithmeticException(ByteBufs.readString(in)), in);
         default :
            assert false : "no such exception for id: " + key;
         }
      }

      return null;
   }

   /**
    * Deserializes stacktrace elements as well as suppressed exceptions from the
    * given output stream and
    * adds it to the given exception.
    */
   static <T extends Throwable> T readStackTrace(T throwable, ByteBuf in) {
      final int stackTraceElements = in.readInt();
      StackTraceElement[] stackTrace =
         new StackTraceElement[stackTraceElements];

      for(int i = 0; i < stackTraceElements; i++) {
         final String declaringClass = ByteBufs.readString(in);
         final String fileName = ByteBufs.readString(in);
         final String methodName = ByteBufs.readString(in);
         final int lineNumber = in.readInt();
         stackTrace[i] = new StackTraceElement(declaringClass, methodName,
            fileName, lineNumber);
      }

      throwable.setStackTrace(stackTrace);
      int numSuppressed = in.readInt();

      for(int i = 0; i < numSuppressed; i++) {
         throwable.addSuppressed(readThrowable(in));
      }

      return throwable;
   }
}