package com.example.photofy_android;

public class Global {
        private static String IP_ADDRESS;
        public static String CONNECTION_ID;
        public static String NICK;
        public static String getPrettyException(Exception e){
                return e.getMessage().split(": ")[1];
        }
        public static void setIpAddress(String ip){
                IP_ADDRESS = ip;
        }

        public static String getIpAddress(){
                return IP_ADDRESS;
        }
}
/*
TODO if there is poor stability, replace the current connectionid system with a manually server generated one
TODO add build variants for debug and release

 */