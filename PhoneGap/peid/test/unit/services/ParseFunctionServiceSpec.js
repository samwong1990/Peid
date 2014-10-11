describe('ParseFunctionService', function () {
    'use strict';

    beforeEach(module('peid'));

    describe('parse cloud functions', function () {
        var parseFunctionService;
        var parse;
        beforeEach(inject(function (ParseFunctionService) {
            console.log(JSON.stringify(ParseFunctionService));
            parseFunctionService = ParseFunctionService;
            parse = Parse;
        }));

        describe('submitFeedback', function () {
            beforeEach(function () {
                spyOn(Parse.Cloud, 'run');
            });
            var stars = 3;
            var comments = "asdf aewkj\n\n\njsanfkjewan\n!@#%#$^^%*)_+{}:<>?\]\[p\./,/./]";

            it('should call Parse Cloud Function submitFeedback', function () {
                parseFunctionService.submitFeedback(stars, comments);
                expect(Parse.Cloud.run.mostRecentCall.args[0]).toEqual('submitFeedback');
            });

            it('should call Parse\'s submitFeedback with args', function () {
                parseFunctionService.submitFeedback(stars, comments);
                expect(Parse.Cloud.run.mostRecentCall.args[1]).toEqual({
                    stars: stars,
                    comments: comments
                });
            });

        });

        describe('sendBills', function () {
            var expected = {
                originatorEmail: "asdf@cdas.cdsa",
                nameOfOriginator: "asdf",
                emailsToSendBillTo: ["ab@cd.e", "arb@cd.e", "ab@cdq.e", "ab@csd.e", "ab@vcd.e"],
                amountToPayPerPerson: "2.71"
            };

            beforeEach(inject(function (AuthService, BillSplitterService, $rootScope) {
                spyOn($rootScope, '$emit');

                spyOn(AuthService, 'getCurrentUser').andReturn({
                    email: expected.originatorEmail,
                    name: expected.nameOfOriginator
                });
                spyOn(BillSplitterService, 'getBreakdown').andReturn({
                    billPerPerson: expected.amountToPayPerPerson
                });
                spyOn(BillSplitterService, 'getBillDetails').andReturn({
                    contacts: expected.emailsToSendBillTo
                });

            }));

            describe("state updates", function () {
                var rootScope;
                beforeEach(inject(function ($rootScope) {
                    rootScope = $rootScope;
                }));

                describe('before parse function returns', function(){
                    beforeEach(function ($rootScope) {
                        // Stub out parse.cloud else you'd be running it live!
                        spyOn(Parse.Cloud, 'run').andReturn(new Parse.Promise());
                    });

                    it('should have state equal not-running at the beginning', function () {
                        var billState = parseFunctionService.getSendBillsState();
                        expect(billState.state).toEqual("not-running");
                    });

                    it('should emit state update when sendBills is called', function () {
                        parseFunctionService.sendBills();
                        expect(rootScope.$emit.mostRecentCall.args[0]).toEqual('ParseFunctionService.sendBills.StateUpdated');
                        var billState = rootScope.$emit.mostRecentCall.args[1];
                        expect(billState.state).toEqual("sending-request");
                    });
                });

                describe("on different parse function outcomes", function () {
                    var promise;
                    beforeEach(function () {
                        promise = new Parse.Promise();
                        spyOn(Parse.Cloud, 'run').andReturn(promise);
                        parseFunctionService.sendBills();
                    });

                    it('should emit state update when sendBills succeeded', function () {
                        promise.resolve("success");
                        expect(rootScope.$emit.mostRecentCall.args[0]).toEqual('ParseFunctionService.sendBills.StateUpdated');
                        var billState = rootScope.$emit.mostRecentCall.args[1];
                        expect(billState.state).toEqual("succeeded");
                        expect(billState.result).toEqual("success");
                    });

                    it('should emit state update when sendBills failed', function () {
                        promise.reject("failure");
                        expect(rootScope.$emit.mostRecentCall.args[0]).toEqual('ParseFunctionService.sendBills.StateUpdated');
                        var billState = rootScope.$emit.mostRecentCall.args[1];
                        expect(billState.state).toEqual("failed");
                        expect(billState.error).toEqual("failure");
                    });
                });

            });

            describe("sendBills orchestration", function () {
                beforeEach(function () {
                    spyOn(Parse.Cloud, 'run').andReturn(Parse.Promise.as(undefined));
                });

                it('should call Parse Cloud Function sendBills', function () {
                    parseFunctionService.sendBills();
                    expect(Parse.Cloud.run.mostRecentCall.args[0]).toEqual('sendBills');
                });

                it('should call Parse.Cloud.sendBills only once', function () {
                    parseFunctionService.sendBills();
                    expect(Parse.Cloud.run.calls.length).toEqual(1);
                });

                it('should call AuthService to get user info', inject(function (AuthService) {
                    parseFunctionService.sendBills();
                    expect(AuthService.getCurrentUser.calls.length).toEqual(1);
                }));

                it('should call BillSplitterService to get bill details', inject(function (BillSplitterService) {
                    parseFunctionService.sendBills();
                    expect(BillSplitterService.getBreakdown.calls.length).toEqual(1);
                }));

                it('should aggregate data and call Parse Cloud function with proper info', function () {
                    parseFunctionService.sendBills();
                    expect(Parse.Cloud.run.mostRecentCall.args[1]).toEqual(expected);
                });
            });

        });
    });
});
