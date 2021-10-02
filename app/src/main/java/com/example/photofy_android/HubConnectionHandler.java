package com.example.photofy_android;

import com.microsoft.signalr.HubConnection;

public class HubConnectionHandler {
    private static HubConnection hubConnection;
    public static synchronized  HubConnection getHubConnection(){
        return hubConnection;
    }
    public static synchronized  void setHubConnection(HubConnection hubConnectionNew){
        hubConnection = hubConnectionNew;
    }

}
