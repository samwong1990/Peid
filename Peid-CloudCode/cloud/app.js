// These two lines are required to initialize Express in Cloud Code.
var express = require('express');
var app = express();

var gcConfig = {
     appId: 'appid',
     appSecret: 'appsecret',
     token: 'token',
     merchantId: 'merchantid'
   };

var gocardless = require('cloud/gocardless-node/lib/gocardless.js')(gcConfig);


// Global app configuration section
app.set('views', 'cloud/views');  // Specify the folder to find templates
app.set('view engine', 'ejs');    // Set the template engine
app.use(express.bodyParser());    // Middleware for reading request body

// gocardless webhook
app.post('/gocardless_webhook', function (req, res) {
    console.log("req.body" + JSON.stringify(req.body))
    console.log("req.body.message" + req.body.message)
    console.log("req.body.text" + req.body.text);
    if (!gocardless.webhookValid(req.body)) return res.send(403);
    // Do anything which you need to do, e.g. update your database
    // Better to make this async as gocardless needs a 200 response
    // within 5 seconds
    var Webhook = Parse.Object.extend("Webhook");
    var webhook = new Webhook();
    webhook.save({
        body: JSON.stringify(req.body),
        reqBodyMessage: req.body.message,
        reqBodyText: req.body.text
    }).then(
        function (success) {
            res.send(200);
        }, function (error) {
            console.error(error);
            res.status(500);
            res.send('Error');
        }
    );

});

// app.get('/subscriptions', function(req, res) {
//   var url = gocardless.bill.newUrl({
//     amount: '12.34',
//     name: 'Coffee',
//     description: 'You ate at blah on 2014 03 01 32:34:12 with A,B,C and D.',
//     user: {
//       "first_name": "Alasdair",
//       "last_name": "Monk",
//       "company_name": "GoCardless Ltd",
//       "email": "alasdair@gocardless.com",
//       "billing_address1": "22-25 Finsbury Square",
//       "billing_address2": "Royal London House",
//       "billing_town": "London",
//       "billing_postcode": "E84DQ",
//       "country_code": "GB"
//     }
//   });
//   res.redirect(url);

// });

// // Custom webhook
// // https://www.parse.com/docs/cloud_code_guide#webhooks
// app.post('/notify_message',
//          express.basicAuth('user', 'pw'),
//          function(req, res) {
//   // Use Parse JavaScript SDK to create a new message and save it.
//   var Message = Parse.Object.extend("Message");
//   var message = new Message();
//   message.save({ text: req.body.text }).then(function(message) {
//     res.send('Success');
//   }, function(error) {
//     res.status(500);
//     res.send('Error');
//   });
// });

// Attach the Express app to Cloud Code.
app.listen();



