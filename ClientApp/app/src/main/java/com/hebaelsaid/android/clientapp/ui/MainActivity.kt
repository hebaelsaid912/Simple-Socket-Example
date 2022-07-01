package com.hebaelsaid.android.clientapp.ui

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.hebaelsaid.android.clientapp.R
import com.hebaelsaid.android.clientapp.common.Constants
import com.hebaelsaid.android.clientapp.databinding.ActivityMainBinding
import com.hebaelsaid.android.clientapp.server.SocketHandler
import com.hebaelsaid.android.clientapp.ui.notification.Notification
import io.socket.client.Socket
import io.socket.emitter.Emitter


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var mSocket: Socket
    private lateinit var notify: Notification
    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        notify = Notification(this)
        //socket connection
        SocketHandler.setSocket()
        mSocket = SocketHandler.getSocket()
        mSocket.on(Socket.EVENT_CONNECT, onConnect)
        SocketHandler.establishConnection()
        Log.d(TAG, "onCreate: isSocketConnected? ${mSocket.isActive}")
        mSocket.on(Constants.CREATE_NEW_ORDER, onOrderCreated)
        mSocket.on(Constants.SEND_ORDER_TO_MERCHANT, onReceiveOrder)
        mSocket.on(Constants.WAIT_FOR_MERCHANT_RESPONSE, onWaitingForResponse)
        mSocket.on(Constants.ACCEPT_ORDER_REQUEST, onOrderStatusChanges)
        mSocket.on(Constants.REJECT_ORDER_REQUEST, onOrderStatusChanges)

        binding.createNewOrderBtn.setOnClickListener {
            Log.d(TAG, "onCreate: create new order button is clicked!")
            mSocket.emit(Constants.CREATE_NEW_ORDER)
        }
    }

    private val onOrderCreated = Emitter.Listener { args ->
        if (!args.isNullOrEmpty()) {
            val counter = args[0] as Int
            val item_1 = args[1] as String
            val item_2 = args[2] as String
            runOnUiThread(Runnable {
                Log.d(TAG, "onOrderCreated: counter: $counter")
                Log.d(TAG, "onOrderCreated: item_1: $item_1")
                Log.d(TAG, "onOrderCreated: item_2: $item_2")
                val builder = AlertDialog.Builder(this)
                    .setIcon(R.drawable.check)
                    .setTitle("created successfully :)")
                    .setMessage("Order send to merchant successfully\nwaiting to notification with acceptance..")
                    .setPositiveButton("Okay") { dialogInterface: DialogInterface, i: Int ->

                    }
                    .setCancelable(false)
                    .create()
                builder.show()
                mSocket.emit(Constants.SEND_ORDER_TO_MERCHANT, counter, item_1, item_2)
                Log.d(TAG, "onOrderCreated: created successfully :) ")
            })
        }
    }
    private val onConnect = Emitter.Listener { args ->
        runOnUiThread(
            Runnable { Log.i(TAG, "Connect to socket.io") })
    }
    private val onReceiveOrder = Emitter.Listener { args ->
        if (!args.isNullOrEmpty()) {
            val counter = args[0] as Int
            val item_1 = args[1] as String
            val item_2 = args[2] as String
            runOnUiThread(Runnable {
                Log.d(TAG, "onOrderCreated: counter: $counter")
                Log.d(TAG, "onOrderCreated: item_1: $item_1")
                Log.d(TAG, "onOrderCreated: item_2: $item_2")
                    Log.i(TAG, "order sent")
                    mSocket.emit(Constants.WAIT_FOR_MERCHANT_RESPONSE,true)
                })
        }
    }
    private val onWaitingForResponse = Emitter.Listener { args ->
        if (!args.isNullOrEmpty()) {
            runOnUiThread(
                Runnable {
                    Log.i(TAG, "Waiting.....")
                })
        }
    }
    private val onOrderStatusChanges = Emitter.Listener { args ->
        if (!args.isNullOrEmpty()) {
            val orderStatus = args[0] as Boolean
            runOnUiThread(
                Runnable {
                    Log.i(TAG, "order status : $orderStatus")
                    if (orderStatus) {
                        notify.createAcceptedNotification(Constants.NOTIFY_MESS_ACCEPT_ORDER)
                    } else {
                        notify.createAcceptedNotification(Constants.NOTIFY_MESS_REJECT_ORDER)
                    }

                })
        }
    }
}