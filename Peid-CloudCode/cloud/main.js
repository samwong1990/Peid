// to enable the stuff under peid.parseapp.com
require('cloud/app.js');

// gocardless stuff
var _ = require('underscore');
var gcConfig = {
     appId: 'appid',
     appSecret: 'appsecret',
     token: 'token',
     merchantId: 'merchantid'
   };

var gocardless = require('cloud/gocardless-node/lib/gocardless.js')(gcConfig);

// send email
var Mandrill = require('mandrill');
Mandrill.initialize('mandrill_Id');

Parse.Cloud.define("getMerchantDetails", function (request, response) {
    gocardless.merchant.getSelf().then(function (httpRequest) {
        response.success(httpRequest.text);
    });
});

Parse.Cloud.define("submitFeedback", function(req, res) {
    console.log("submitFeedback called with:" + JSON.stringify(req));
    var Feedback = Parse.Object.extend("Feedback");
    var feedback = new Feedback();
    feedback.save({
        stars: req.params.stars,
        comments: req.params.comments
    }).then(
        function (success) {
            res.success();
        }, function (error) {
            console.error(error);
            res.error(error);
        }
    );
});

Parse.Cloud.define("sendBills", function (request, response) {
    console.log(JSON.stringify(request.params));
    var nameOfOriginator = request.params.nameOfOriginator;
    var amountToPayPerPerson = request.params.amountToPayPerPerson;
    var emailsToSendBillTo = request.params.emailsToSendBillTo;
    var originatorEmail = request.params.originatorEmail;
    console.log("nameOfOriginator:" + nameOfOriginator +
        ",amountToPayPerPerson:" + amountToPayPerPerson +
        ",emailsToSendBillTo:" + emailsToSendBillTo +
        ",originatorEmail:" + originatorEmail);
    if(    _.isEmpty(nameOfOriginator)
        || _.isEmpty(amountToPayPerPerson)
        || _.isEmpty(emailsToSendBillTo)
        || _.isEmpty(originatorEmail)){
        console.error("Missing parameter, abort");
        response.error("Missing parameter, abort");
        return;
    }


    var email_gocardlessUrl_pairs = getDemandTuples(nameOfOriginator, emailsToSendBillTo, amountToPayPerPerson);

    var taskPromises = [
        sendGoCardlessBills(nameOfOriginator, email_gocardlessUrl_pairs),
        sendSummaryToOriginator(originatorEmail, email_gocardlessUrl_pairs)
    ];

    Parse.Promise.when(taskPromises)
        .then(
        function () {
            response.success("Emails sent!");
        },
        function (error) {
            response.error("Oops! " + JSON.stringify(error));
        }
    );
});

function getDemandUrl(nameOfOriginator, email, amountToCollect) {
    console.log("getDemandUrl email=" + email);
    var now = new Date();
    return {
        'email': email,
        'gocardlessUrl': gocardless.bill.newUrl({
            amount: amountToCollect,
            name: 'Peid: split bill at ' + now,
            description: nameOfOriginator + " has split a bill with you at " + now,
            user: {
                "email": email,
                "billing_town": "London",
                "country_code": "GB"
            }
        })
    };
}

function getDemandTuples(nameOfOriginator, emails, amountToCollect) {
    return _.map(emails, function (email) {
        console.log("getDemandTuples email=" + email);
        return getDemandUrl(nameOfOriginator, email, amountToCollect);
    });
}

function sendGoCardlessBill(nameOfOriginator, emailToSendBillTo, goCardlessUrl) {
    var subject = nameOfOriginator + " has split a bill with you using Peid";
    var html = nameOfOriginator + " has split a bill with you using Peid.\nUse <a href='" + goCardlessUrl + "'>GoCardless</a> to pay your bill online in 1 minute.";
    return sendEmail(emailToSendBillTo, subject, html);
}

function sendGoCardlessBills(nameOfOriginator, email_gocardlessUrl_pairs) {
    console.log("sendGoCardlessBills:" + JSON.stringify(email_gocardlessUrl_pairs));
    return Parse.Promise.as(email_gocardlessUrl_pairs)
        .then(function (pairs) {
            // Create a trivial resolved promise as a base case.
            var promises = [];
            _.each(pairs, function (pair) {
                // For each pair, send email
                console.log("sendGoCardlessBills inner: obj=" + JSON.stringify(pair) + ", pair.email=" + pair.email + " pair.gocardlessUrl=" + pair.gocardlessUrl);
                promises.push(sendGoCardlessBill(nameOfOriginator, pair.email, pair.gocardlessUrl));
            });
            return Parse.Promise.when(promises);
        });
}

function sendSummaryToOriginator(originatorEmail, email_gocardlessUrl_pair) {
    var summary = _.reduce(email_gocardlessUrl_pair, function (memo, pair) {
        return memo +
            "<p>" + pair.email + " : <a href='" + pair.gocardlessUrl + "'>" + pair.gocardlessUrl + "</a></p>";
    }, "");
    var html = "Thank you for using Peid. This is the list of bill urls for your record.<br>" + summary;
    var subject = "You have split a bill with your friends using Peid!";
    return sendEmail(originatorEmail, subject, html);
}


function sendEmail(email, subject, html) {
    var promise = new Parse.Promise();
    Mandrill.sendEmail({
        message: {
            from_email: "sam.wong.1990@gmail.com",
            from_name: "Peid",
            to: [
                {
                    email: email
                }
            ],
            subject: subject,
            html: html

        },
        async: true
    }, {
        success: function (httpResponse) {
            console.log("sendEmail successful\n" + JSON.stringify(httpResponse));
            promise.resolve(httpResponse);
        },
        error: function (httpResponse) {
            console.error("sendEmail failed\n" + JSON.stringify(httpResponse));
            promise.reject(httpResponse);
        }
    });
    return promise;
}
