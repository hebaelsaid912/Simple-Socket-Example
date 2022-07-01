const express = require('express'); //requires express module
const socket = require('socket.io'); //requires socket.io module
const fs = require('fs');
const app = express();
var PORT = process.env.PORT || 3000;
const server = app.listen(PORT); //tells to host server on localhost:3000


//Playing variables:
app.use(express.static('public')); //show static files in 'public' directory
console.log('Server is running');
const io = socket(server);

var count = 0;
var itemnum1 = "Item 1"
var itemnum2 = "Item 2"


//Socket.io Connection------------------
io.on('connection', (socket) => {

    console.log("New socket connection: " + socket.id)


    socket.on('create_order', () => {
        count++;
        console.log(count)
        console.log(itemnum1)
        console.log(itemnum2)
        io.emit('create_order', count,itemnum1,itemnum2);
    })

    socket.on('receive_order', (count,itemnum1,itemnum2) => {
        console.log("reseive_order is hit")
        console.log(count)
        console.log(itemnum1)
        console.log(itemnum2)
        io.emit('receive_order', count,itemnum1,itemnum2);
    })

    socket.on('wait_for_merchant_acceptance', (wait) => {
        console.log("client waiting..")
        io.emit('wait_for_merchant_acceptance',wait);
    })

    socket.on('accept_order_request', (status,count,itemnum1,itemnum2) => {
        console.log("accept_order_request is hit")
        console.log(status)
        io.emit('accept_order_request',status,count,itemnum1,itemnum2);
    })

    socket.on('reject_order_request', (status) => {
        console.log("reject_order_request is hit")
        console.log(status)
        io.emit('reject_order_request',status);
    })

    socket.on('send_order_request_to_driver', (status,count,itemnum1,itemnum2) => {
        console.log("send_order_request_to_driver is hit")
        console.log(status)
        console.log(count)
        console.log(itemnum1)
        console.log(itemnum2)
        var counter = 5;
        var WinnerCountdown = setInterval(function(){
            io.sockets.emit('counterTimer', counter,"waiting..");
            counter--
            if (counter === 0) {
            io.sockets.emit('counterTimer',0, "NO Response From Driver");
           // io.emit('cancel_driver_request_and_resend_order',status,count,itemnum1,itemnum2);
            clearInterval(WinnerCountdown);
            }
        }, 1000);
        io.emit('send_order_request_to_driver',status,count,itemnum1,itemnum2);
    })
    socket.on('cancel_driver_request_and_resend_order', (status,count,itemnum1,itemnum2) => {
        console.log("cancel_driver_request_and_resend_order is hit")
      //  io.emit('send_order_request_to_driver',status,count,itemnum1,itemnum2);
        io.emit('cancel_driver_request_and_resend_order',status,count,itemnum1,itemnum2);
    })
    socket.on('wait_for_driver_acceptance', (wait) => {
        console.log("client & merchant waiting..")
        io.emit('wait_for_driver_acceptance',wait);
    })

    socket.on('accept_order_delivery_request', (status) => {
        console.log("accept_order_delivery_request is hit")
        console.log(status)
        io.emit('accept_order_delivery_request',status);
    })

    socket.on('reject_order_delivery_request', (status) => {
        console.log("reject_order_delivery_request is hit")
        console.log(status)
        io.emit('reject_order_delivery_request',status);
    })
})
