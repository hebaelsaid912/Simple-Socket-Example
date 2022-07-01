package com.hebaelsaid.android.driverapp.ui

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.hebaelsaid.android.driverapp.ui.notification.Notification
import com.hebaelsaid.android.driverapp.R
import com.hebaelsaid.android.driverapp.common.Constants
import com.hebaelsaid.android.driverapp.databinding.ActivityMainBinding
import com.hebaelsaid.android.driverapp.server.SocketHandler
import io.socket.client.Socket
import io.socket.emitter.Emitter

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    private lateinit var mSocket: Socket
    private lateinit var notify: Notification
    private lateinit var binding:ActivityMainBinding
    private lateinit var  builder: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        notify = Notification(this)
        SocketHandler.setSocket()
        mSocket = SocketHandler.getSocket()
        mSocket.on(Socket.EVENT_CONNECT, onConnect)
        SocketHandler.establishConnection()
        Log.d(TAG, "onCreate: isSocketConnected? ${mSocket.isActive}")
        mSocket.on(Constants.RECEIVE_ORDER_REQUEST_FROM_MERCHANT, onOrderReceivedFromMerchant)
        mSocket.on(Constants.WAIT_FOR_DRIVER_ACCEPTANCE, onWaitingForDriverAcceptance)
        mSocket.on(Constants.ACCEPT_ORDER_DELIVERY_REQUEST, onDriverAcceptRequest)
        mSocket.on(Constants.REJECT_ORDER_DELIVERY_REQUEST, onDriverRejectRequest)
        mSocket.on(Constants.CANCEL_DRIVER_REQUEST_AND_RESEND_ORDER, onCancelAndResendOrder)
    }

    private val onConnect = Emitter.Listener { args ->
        runOnUiThread(
            Runnable { Log.i(TAG, "Connect to socket.io") })
    }
    private val onOrderReceivedFromMerchant = Emitter.Listener { args ->
        mSocket.emit(Constants.WAIT_FOR_DRIVER_ACCEPTANCE,true)
        if (!args.isNullOrEmpty()) {
            val status = args[0] as Boolean
            val counter = args[1] as Int
            val item_1 = args[2] as String
            val item_2 = args[3] as String
            runOnUiThread(
                Runnable {
                    Log.i(TAG, "new order received")
                    Log.d(TAG, "onOrderReceivedFromMerchant: status: $status")
                    Log.d(TAG, "onOrderReceivedFromMerchant: counter: $counter")
                    Log.d(TAG, "onOrderReceivedFromMerchant: item_1: $item_1")
                    Log.d(TAG, "onOrderReceivedFromMerchant: item_2: $item_2")
                    notify.createAcceptedNotification("You have new order request")
                     builder = AlertDialog.Builder(this)
                        .setIcon(R.drawable.new_order)
                        .setTitle("New order request")
                        .setMessage("You have new order request ")
                        .setPositiveButton("Accept") { dialogInterface: DialogInterface, i: Int ->
                            mSocket.emit(
                                Constants.ACCEPT_ORDER_DELIVERY_REQUEST,
                                true
                            )
                        }
                        .setNegativeButton("Reject") { dialogInterface: DialogInterface, i: Int ->
                            mSocket.emit(Constants.REJECT_ORDER_DELIVERY_REQUEST,false)
                        }
                        .create()

                    builder.show()
                })

        }
    }
    private val onCancelAndResendOrder = Emitter.Listener {args->
        if (!args.isNullOrEmpty()) {
            val status = args[0] as Boolean
            val counter = args[1] as Int
            val item_1 = args[2] as String
            val item_2 = args[3] as String
            runOnUiThread(
                Runnable {
                    Log.d(TAG, "onOrderReceivedFromMerchant: status: $status")
                    Log.d(TAG, "onOrderReceivedFromMerchant: counter: $counter")
                    Log.d(TAG, "onOrderReceivedFromMerchant: item_1: $item_1")
                    Log.d(TAG, "onOrderReceivedFromMerchant: item_2: $item_2")
                    if(builder.isShowing){
                        builder.cancel()
                    }

                })
        }
    }
    private val onWaitingForDriverAcceptance = Emitter.Listener {args->
        if (!args.isNullOrEmpty()) {
            val isWaiting = args[0] as Boolean
            runOnUiThread(
                Runnable {
                    Log.i(TAG, "client and merchant waiting...")
                    Log.i(TAG, "isWaiting: $isWaiting")
                })
        }
    }
    private val onDriverAcceptRequest = Emitter.Listener { args ->
        if (!args.isNullOrEmpty()) {
            val orderStatus = args[0] as Boolean
            runOnUiThread(
                Runnable {
                    Log.i(TAG, "new order received")
                    Log.i(TAG, "order accepted from driver: $orderStatus")
                })
        }
    }
    private val onDriverRejectRequest = Emitter.Listener { args ->
        if (!args.isNullOrEmpty()) {
            val orderStatus = args[0] as Boolean
            runOnUiThread(
                Runnable {
                    Log.i(TAG, "order rejected from driver $orderStatus")
                })
        }
    }
}