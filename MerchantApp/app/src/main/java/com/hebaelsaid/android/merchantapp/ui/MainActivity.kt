package com.hebaelsaid.android.merchantapp.ui

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hebaelsaid.android.merchantapp.R
import com.hebaelsaid.android.merchantapp.common.Constants
import com.hebaelsaid.android.merchantapp.data.OrderDataModel
import com.hebaelsaid.android.merchantapp.databinding.ActivityMainBinding
import com.hebaelsaid.android.merchantapp.server.SocketHandler
import com.hebaelsaid.android.merchantapp.ui.notification.Notification
import io.socket.client.Socket
import io.socket.emitter.Emitter

private const val TAG = "MainActivity"


class MainActivity : AppCompatActivity() {
    private lateinit var mSocket: Socket
    private lateinit var notify: Notification
    private lateinit var binding:ActivityMainBinding
    private var countSocket:Int=0
    private lateinit var item_1_Socket:String
    private lateinit var item_2_Socket:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this ,R.layout.activity_main)
        notify = Notification(this)
        //socket connection
        SocketHandler.setSocket()
        mSocket = SocketHandler.getSocket()
        mSocket.on(Socket.EVENT_CONNECT, onConnect)
        SocketHandler.establishConnection()
        mSocket.on(Constants.RECEIVE_ORDER_FROM_CLIENT, onOrderReceived)
        mSocket.on(Constants.ACCEPT_ORDER_REQUEST, onOrderAccepted)
        mSocket.on(Constants.REJECT_ORDER_REQUEST, onOrderRejected)
        mSocket.on(Constants.WAIT_FOR_MERCHANT_ACCEPTANCE, onWaiting)
        mSocket.on(Constants.SEND_ORDER_TO_DRIVER, onOrderSendToDriver)
        mSocket.on(Constants.WAIT_FOR_DRIVER_ACCEPTANCE, onWaitingForDriverAcceptance)
        mSocket.on(Constants.TIMER_WAIT_FOR_DRIVER, onTimerCount)
        mSocket.on(Constants.ACCEPT_ORDER_DELIVERY_REQUEST, onDriverAcceptRequest)
        mSocket.on(Constants.REJECT_ORDER_DELIVERY_REQUEST, onDriverRejectRequest)
        mSocket.on(Constants.CANCEL_DRIVER_REQUEST_AND_RESEND_ORDER, onCancelAndResendOrder)

        binding.findOtherDriver.setOnClickListener {
            mSocket.emit(Constants.CANCEL_DRIVER_REQUEST_AND_RESEND_ORDER,true,countSocket,item_1_Socket,item_2_Socket)
        }
    }

    private val onConnect = Emitter.Listener {
        runOnUiThread(
            Runnable { Log.i(TAG, "Connect to socket.io") })
    }
    private val onOrderReceived = Emitter.Listener { args ->
        mSocket.emit(Constants.WAIT_FOR_MERCHANT_ACCEPTANCE,true)
        if (!args.isNullOrEmpty()) {
            val counter = args[0] as Int
            val item_1 = args[1] as String
            val item_2 = args[2] as String
            runOnUiThread(
                Runnable {
                    Log.i(TAG, "new order received")
                    Log.d(TAG, "onOrderCreated: counter: $counter")
                    Log.d(TAG, "onOrderCreated: item_1: $item_1")
                    Log.d(TAG, "onOrderCreated: item_2: $item_2")
                    countSocket = counter
                    item_1_Socket = item_1
                    item_2_Socket = item_2
                    notify.createAcceptedNotification("You have new order request")
                    val builder = AlertDialog.Builder(this)
                        .setIcon(R.drawable.new_order)
                        .setTitle("New order request")
                        .setMessage("You have new order request ")
                        .setPositiveButton("Accept") { dialogInterface: DialogInterface, i: Int ->
                            mSocket.emit(Constants.SEND_ORDER_TO_DRIVER,true,counter,item_1,item_2)
                        }
                        .setNegativeButton("Reject") { dialogInterface: DialogInterface, i: Int ->
                            mSocket.emit(Constants.REJECT_ORDER_REQUEST,false)
                        }
                        .setCancelable(false)
                        .create()
                    builder.show()

                })

        }
    }
    private val onWaiting = Emitter.Listener {args->
        if (!args.isNullOrEmpty()) {
            val isWaiting = args[0] as Boolean
            runOnUiThread(
                Runnable {
                    Log.i(TAG, "waiting...")
                    Log.i(TAG, "isWaiting: $isWaiting")
                })
        }
    }


    private val onOrderAccepted = Emitter.Listener { args ->
        if (!args.isNullOrEmpty()) {
            val orderStatus = args[0] as Boolean
            runOnUiThread(
                Runnable {
                    Log.i(TAG, "new order received")
                    Log.i(TAG, "order accepted: $orderStatus")
                })
        }
    }
    private val onOrderRejected = Emitter.Listener { args ->
        if (!args.isNullOrEmpty()) {
            val orderStatus = args[0] as Boolean
            runOnUiThread(
                Runnable {
                    Log.i(TAG, "order rejected $orderStatus")
                })
        }
    }


    private val onOrderSendToDriver = Emitter.Listener { args ->
        mSocket.emit(Constants.WAIT_FOR_DRIVER_ACCEPTANCE,true)
        mSocket.emit(Constants.TIMER_WAIT_FOR_DRIVER)
        if (!args.isNullOrEmpty()) {
            val status = args[0] as Boolean
            val counter = args[1] as Int
            val item_1 = args[2] as String
            val item_2 = args[3] as String
            runOnUiThread(
                Runnable {
                    Log.i(TAG, "new order send to driver")
                    Log.d(TAG, "onOrderCreated: status: $status")
                    Log.d(TAG, "onOrderCreated: counter: $counter")
                    Log.d(TAG, "onOrderCreated: item_1: $item_1")
                    Log.d(TAG, "onOrderCreated: item_2: $item_2")
            })

        }
    }
    private val onWaitingForDriverAcceptance = Emitter.Listener {args->
        if (!args.isNullOrEmpty()) {
            val isWaiting = args[0] as Boolean
            runOnUiThread(
                Runnable {
                    Log.i(TAG, "waiting for driver...")
                    Log.i(TAG, "isWaiting: $isWaiting")
                })
        }
    }
    private val onTimerCount = Emitter.Listener {args->
        if (!args.isNullOrEmpty()) {
            val counter = args[0] as Int
            val message = args[1] as String
            runOnUiThread(
                Runnable {
                    Log.i(TAG, "message: $message")
                    Log.i(TAG, "counter: $counter")
                    binding.counterTv.text = if(counter!= 0) counter.toString() else message
                    binding.counterTv.visibility = View.VISIBLE
                    if(counter==0){
                        binding.findOtherDriver.visibility= View.VISIBLE
                    }
                })
        }
    }
    private val onDriverAcceptRequest = Emitter.Listener { args ->
        if (!args.isNullOrEmpty()) {
            val orderStatus = args[0] as Boolean
            runOnUiThread(
                Runnable {
                    notify.createAcceptedNotification(Constants.NOTIFY_MESS_ACCEPT_ORDER)
                    Log.i(TAG, "new order received")
                    Log.i(TAG, "order accepted: $orderStatus")
                    binding.counterTv.visibility = View.GONE
                    binding.findOtherDriver.visibility = View.GONE
                    val builder = AlertDialog.Builder(this)
                        .setIcon(R.drawable.check)
                        .setTitle("created successfully :)")
                        .setMessage(Constants.NOTIFY_MESS_ACCEPT_ORDER)
                        .setPositiveButton("Okay") { dialogInterface: DialogInterface, i: Int ->

                        }
                        .setCancelable(false)
                        .create()
                    builder.show()
                    binding.counterTv.visibility = View.GONE
                    binding.findOtherDriver.visibility = View.GONE
                    mSocket.emit(
                        Constants.ACCEPT_ORDER_REQUEST,
                        true
                    )
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
                    mSocket.emit(Constants.SEND_ORDER_TO_DRIVER,status,counter,item_1,item_2)

                })
        }
    }
    private val onDriverRejectRequest = Emitter.Listener { args ->
        if (!args.isNullOrEmpty()) {
            val orderStatus = args[0] as Boolean
            runOnUiThread(
                Runnable {
                    notify.createAcceptedNotification(Constants.NOTIFY_MESS_REJECT_ORDER)
                    binding.counterTv.text = Constants.NOTIFY_MESS_REJECT_ORDER
                    binding.counterTv.visibility = View.VISIBLE
                    binding.findOtherDriver.visibility = View.VISIBLE
                    Log.i(TAG, "order rejected $orderStatus")
                })
        }
    }
}