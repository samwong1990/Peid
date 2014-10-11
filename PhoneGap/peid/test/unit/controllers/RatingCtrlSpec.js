describe('RatingCtrl', function () {
    'use strict';
    beforeEach(module('peid'));

    var scope, ratingCtrl;
    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();
        ratingCtrl = $controller('RatingCtrl', { $scope: scope });
    }));

    it('should be defined', inject(function ($rootScope, $controller) {
        expect(ratingCtrl).toBeDefined();
    }));

    describe('submitFeedback', function () {
        it('should call parse function to submit feedback', inject(function (ParseFunctionService) {
            spyOn(ParseFunctionService, 'submitFeedback');
            scope.submitFeedback();
            expect(ParseFunctionService.submitFeedback.calls.length).toEqual(1);
        }));

        it('should call parse function with params', inject(function (ParseFunctionService) {
            spyOn(ParseFunctionService, 'submitFeedback');
            var stars = 2, comments = "asdfkw\nfsa!@#$%^&*()_+=-\][`~[]\{|}:><?;,./\n\n\"";
            scope.rating = {};
            scope.rating.stars = stars;
            scope.rating.comments = comments;
            scope.submitFeedback();
            expect(ParseFunctionService.submitFeedback.mostRecentCall.args[0]).toEqual(stars);
            expect(ParseFunctionService.submitFeedback.mostRecentCall.args[1]).toEqual(comments);
        }));

    });

    describe('remote call lifecycle', function () {

        it('should update state on start', inject(function (ParseFunctionService, $controller) {
            var returnedSendBillsState = {
                state: "asdf",
                blah: "blah!"
            };
            spyOn(ParseFunctionService, 'getSendBillsState').andReturn(returnedSendBillsState);
            var newRatingCtrl = $controller('RatingCtrl', { $scope: scope });
            expect(scope.rpcState).toEqual(returnedSendBillsState);
        }));

        describe('updating view by RPC states', function () {
            _(["not-running", "sending-request", "succeeded", "failed"])
                .map(function (state) {
                    return {
                        state: state
                    };
                })
                .forEach(function (returnableState) {
                    it('should deal with ' + returnableState.state, function () {
                        scope.updateState(returnableState);

                        if (returnableState.state === "not-running") {
                            expect(scope.shouldShowProgress()).toBe(false);
                        } else {
                            expect(scope.shouldShowProgress()).toBe(true);
                            if (returnableState.state === "sending-request") {
                                expect(scope.getProgressType()).toBe("ongoing");
                            } else if (returnableState.state === "succeeded") {
                                expect(scope.getProgressType()).toBe("success");
                            } else {
                                expect(scope.getProgressType()).toBe("fail");
                            }
                        }
                    });
                });
        });

        it('should update status when sendBills emits event', inject(function ($rootScope, $httpBackend) {
            var emittedState = {
                state: "not-running",
                whatever: true
            };
            $httpBackend.whenGET( new RegExp(".*")).respond(200, {});
            $rootScope.$emit(
                "ParseFunctionService.sendBills.StateUpdated",
                emittedState
            );
            expect(scope.rpcState).toEqual(emittedState);
        }));

    });
});
