package com.hebaelsaid.android.merchantapp.server

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

private const val TAG = "SocketHandler"
object SocketHandler {
    private lateinit var  mSocket: Socket

    @Synchronized
    fun setSocket(){
        try {
            Log.d(TAG, "setSocket: 1")
           // mSocket = IO.socket("http://172.20.96.1:3000")
          //  mSocket = IO.socket("http://192.168.1.107:3000")
       //     mSocket = IO.socket("http://192.168.1.1:3000")
           // mSocket = IO.socket("http://localhost:3000")
            mSocket = IO.socket("http://10.0.2.2:3000")
            Log.d(TAG, "setSocket: 2")
        } catch (e: URISyntaxException) {
            Log.d(TAG, "setSocket: ${e.message}")
        }
    }

    @Synchronized
    fun getSocket(): Socket{
        return mSocket
    }
    @Synchronized
    fun establishConnection() {
        mSocket.connect()
    }

    @Synchronized
    fun closeConnection() {
        mSocket.disconnect()
    }
}