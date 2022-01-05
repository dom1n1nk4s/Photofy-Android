package com.example.photofy_android;

public class Global {
        //    public static String IP_ADDRESS = "http://192.168.50.93:5001";
        public static String IP_ADDRESS = "http://***REMOVED***:5001";
        public static String CONNECTION_ID;
        public static String NICK;

        public static String getPrettyException(Exception e){
                return e.getMessage().split(": ")[1];
        }
}
/*replace the current connectionid system with a manually server generated one
*
*
*
* */