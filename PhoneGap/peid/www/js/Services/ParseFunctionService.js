angular.module('peid.services.parseFunctionService', [])
    .factory('ParseFunctionService', function ($rootScope, BillSplitterService, AuthService) {
        "use strict";

        var parseFunctionService = {};

        parseFunctionService.sendBillsState = {
            state: "not-running"
        };

        parseFunctionService.getSendBillsState = function () {
            return _.cloneDeep(parseFunctionService.sendBillsState);
        };

        /**
         * @returns {Parse.Promise} A promise that will be resolved with the result of the function.
         */
        parseFunctionService.sendBills = function () {
            console.log("sendBills begin");
            parseFunctionService.sendBillsState = {
                state: "sending-request"
            };
            $rootScope.$emit(
                "ParseFunctionService.sendBills.StateUpdated",
                parseFunctionService.getSendBillsState()
            );
            console.log("sendBills emits for sending-request");
            var currentUser = AuthService.getCurrentUser();
            var billDetails = BillSplitterService.getBillDetails();
            var billBreakdown = BillSplitterService.getBreakdown();

            console.log("splittedBillDetails:" + JSON.stringify(billBreakdown));
            Parse.Cloud.run('sendBills',
                {
                    originatorEmail: currentUser.email,
                    nameOfOriginator: currentUser.name,
                    emailsToSendBillTo: billDetails.contacts,
                    amountToPayPerPerson: billBreakdown.billPerPerson
                }).then(
                function (result) {
                    console.log("sendBills succeeded");
                    parseFunctionService.sendBillsState = {
                        state: "succeeded",
                        result: result
                    };
                    $rootScope.$emit(
                        "ParseFunctionService.sendBills.StateUpdated",
                        parseFunctionService.getSendBillsState()
                    );
                },
                function (error) {
                    console.log("sendBills failed");
                    parseFunctionService.sendBillsState = {
                        state: "failed",
                        error: error
                    };
                    $rootScope.$emit(
                        "ParseFunctionService.sendBills.StateUpdated",
                        parseFunctionService.getSendBillsState()
                    );
                }
            );
        };

        /**
         * @returns {Parse.Promise} A promise that will be resolved with the result of the function.
         */
        parseFunctionService.submitFeedback = function (stars, comments) {
            return Parse.Cloud.run('submitFeedback',
                {
                    stars: stars,
                    comments: comments
                });
        };

        return parseFunctionService;
    });