package com.ikeirnez.ikeirlibs.core;

/**
 * A collection of Utilities to ease the debugging process
 */
public class DebugUtils {

    private DebugUtils(){}

    /**
     * Used for debugging when the caller of a method needs to be found and such like occasions
     */
    public static void printStackTrace(){
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()){
            System.out.println(stackTraceElement.toString());
        }
    }

}
