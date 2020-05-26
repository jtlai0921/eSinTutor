package com.example.esintutor;

import android.app.Application;

public class gVariable extends Application {

    public final static String gWebURL = "http://123.194.136.229/webapi/esign/";

    private static String gTutorId;
    private static String gTutorName;
    private static String gClassId;
    private static String gClassName;

    public static void setgTutorId(String gTutorId)     {gVariable.gTutorId = gTutorId;}
    public static void setgTutorName(String gTutorName) {gVariable.gTutorName = gTutorName;}
    public static void setgClassId(String gClassId)     {gVariable.gClassId = gClassId;}
    public static void setgClassName(String gClassName) {gVariable.gClassName = gClassName;}

    public static String getgTutorId()   {return gTutorId;}
    public static String getgTutorName() {return gTutorName;}
    public static String getgClassId()   {return gClassId;}
    public static String getgClassName() {return gClassName;}

}
