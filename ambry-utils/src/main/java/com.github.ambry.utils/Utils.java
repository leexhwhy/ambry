package com.github.ambry.utils;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * A set of utility methods
 */
public class Utils {

  public static String readShortString(DataInputStream input) throws IOException {
    Short size = input.readShort();
    if(size <= 0)
      return null;
    byte[] bytes = new byte[size];
    int read = input.read(bytes);
    if (read != size) {
      throw new IllegalArgumentException("the size of the input does not match the actual data size");
    }
    return new String(bytes, "UTF-8");
  }

  public static String readIntString(DataInputStream input) throws IOException {
    int size = input.readInt();
    if(size <= 0)
      return null;
    byte[] bytes = new byte[size];
    int read = input.read(bytes);
    if (read != size) {
      throw new IllegalArgumentException("the size of the input does not match the actual data size");
    }
    return new String(bytes, "UTF-8");
  }

  public static ByteBuffer readIntBuffer(DataInputStream input) throws IOException {
    int size = input.readInt();
    if (size < 0)
      return null;
    ByteBuffer buffer = ByteBuffer.allocate(size);
    int read = input.read(buffer.array());
    if (read != size) {
      throw new IllegalArgumentException("the size of the input does not match the actual data size");
    }
    return buffer;
  }

  public static ByteBuffer readShortBuffer(DataInputStream input) throws IOException {
    short size = input.readShort();
    if (size < 0)
        return null;
    ByteBuffer buffer = ByteBuffer.allocate(size);
    int read = input.read(buffer.array());
    if (read != size) {
        throw new IllegalArgumentException("the size of the input does not match the actual data size");
    }
    return buffer;
  }

  /**
   * Create a new thread
   * @param runnable The work for the thread to do
   * @param daemon Should the thread block JVM shutdown?
   * @return The unstarted thread
   */
  public static Thread newThread(Runnable runnable, boolean daemon) {
    Thread thread = new Thread(runnable);
    thread.setDaemon(daemon);
    thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
              //error("Uncaught exception in thread '" + t.getName + "':", e)
          }
        });
    return thread;
  }

  /**
   * Create a new thread
   * @param name The name of the thread
   * @param runnable The work for the thread to do
   * @param daemon Should the thread block JVM shutdown?
   * @return The unstarted thread
   */
  public static Thread newThread(String name, Runnable runnable, boolean daemon) {
    Thread thread = new Thread(runnable, name);
    thread.setDaemon(daemon);
    thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
              //error("Uncaught exception in thread '" + t.getName + "':", e)
          }
        });
    return thread;
  }

  /**
   * Create a daemon thread
   * @param runnable The runnable to execute in the background
   * @return The unstarted thread
   */
  public static Thread daemonThread(Runnable runnable) {
    return newThread(runnable, true);
  }

  /**
   * Create a daemon thread
   * @param name The name of the thread
   * @param runnable The runnable to execute in the background
   * @return The unstarted thread
   */
  public static Thread daemonThread(String name, Runnable runnable) {
    return newThread(name, runnable, true);
  }

  /**
   * Open a channel for the given file
   */
  public static FileChannel openChannel(File file, boolean mutable) throws FileNotFoundException {
    if(mutable)
      return new RandomAccessFile(file, "rw").getChannel();
    else
      return new FileInputStream(file).getChannel();
  }

  /**
   * Instantiate a class instance from a given className.
   */
   public static <T> T getObj(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return (T)Class.forName(className)
                .newInstance();
   }

  /**
   * Compute the hash code for the given items
   */
   public static int hashcode(Object[] items) {
     if(items == null)
       return 0;
     int h = 1;
     int i = 0;
     while(i < items.length) {
       if(items[i] != null) {
         h = 31 * h + items[i].hashCode();
         i += 1;
       }
     }
     return h;
   }

  /**
   * Compute the CRC32 of the byte array
   * @param bytes The array to compute the checksum for
   * @return The CRC32
   */
  public static long crc32(byte[] bytes) {
    return crc32(bytes, 0, bytes.length);
  }

  /**
   * Compute the CRC32 of the segment of the byte array given by the specificed size and offset
   * @param bytes The bytes to checksum
   * @param offset the offset at which to begin checksumming
   * @param size the number of bytes to checksum
   * @return The CRC32
   */
  public static long crc32(byte[] bytes, int offset, int size) {
    Crc32 crc = new Crc32();
    crc.update(bytes, offset, size);
    return crc.getValue();
  }

  /**
   * Read a properties file from the given path
   * @param filename The path of the file to read
   */
  public static Properties loadProps(String filename) throws FileNotFoundException, IOException {
    InputStream propStream = new FileInputStream(filename);
    Properties props = new Properties();
    props.load(propStream);
    return props;
  }

  /**
   * Serializes a nullable string into byte buffer
   * @param outputBuffer The output buffer to serialize the value to
   * @param value The value to serialize
   */
  public static void serializeNullableString(ByteBuffer outputBuffer, String value) {
    if (value == null)
      outputBuffer.putInt(0);
    else {
      outputBuffer.putInt(value.length());
      outputBuffer.put(value.getBytes());
    }
  }

  /**
   * Returns the length of a nullable string
   * @param value The string whose length is needed
   * @return The length of the string. 0 if null.
   */
  public static int getNullableStringLength(String value) {
    return value == null ? 0 : value.length();
  }

  /**
   * Writes specified string to specified file path.
   *
   * @param string to write
   * @param path file path
   * @throws IOException
   */
  public static void writeStringToFile(String string, String path) throws IOException {
    FileWriter fileWriter = null;
    try {
      File clusterFile = new File(path);
      fileWriter = new FileWriter(clusterFile);
      fileWriter.write(string);
    } finally {
      if (fileWriter != null) {
        fileWriter.close();
      }
    }
  }

  /**
   * Pretty prints specified jsonObject to specified file path.
   *
   * @param jsonObject to pretty print
   * @param path file path
   * @throws IOException
   * @throws JSONException
   */
  public static void writeJsonToFile(JSONObject jsonObject, String path) throws IOException, JSONException {
    writeStringToFile(jsonObject.toString(2), path);
  }

  /**
   * Reads entire contents of specified file as a string.
   *
   * @param path file path to read
   * @return string read from specified file
   * @throws IOException
   */
  public static String readStringFromFile(String path) throws IOException {
    // Code cribbed from StackOverflow and is similar to IO Commons and Guava
    // http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
  }

  /**
   * Reads JSON object (in string format) from specified file.
   *
   * @param path file path to read
   * @return JSONObject read from specified file
   * @throws IOException
   * @throws JSONException
   */
  public static JSONObject readJsonFromFile(String path) throws IOException, JSONException {
    return new JSONObject(readStringFromFile(path));
  }
}