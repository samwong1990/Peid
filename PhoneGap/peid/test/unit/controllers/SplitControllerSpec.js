/* jasmine specs for controllers go here */

describe('SplitCtrl', function () {
    'use strict';
    beforeEach(module('peid'));

    describe("Dependency exists", function(){
        it('BillSplitterService should be defined', inject(function(BillSplitterService){
            expect(BillSplitterService).toBeDefined();
        }));
    });

    describe("when Next is pressed", function(){
        var scope;
        var location;
        beforeEach(inject(function ($rootScope, $controller, $location) {
            location = $location;
            scope = $rootScope.$new();
            $controller("SplitCtrl", {
                $scope: scope
            });
            scope.$digest();
        }));

        it('should send bill details to bill splitter service', inject(function(BillSplitterService){
            spyOn(BillSplitterService, "updateBillDetails").andCallThrough();
            scope.billDetails.contacts = ["safd@asdf.ewasd", "ewfankj.sfewa@fdaskj.cdas"];
            scope.billDetails.billAmount = "12.34";

            scope.goToConfirmPage();
            expect(BillSplitterService.updateBillDetails).toHaveBeenCalledWith(
                {
                    "contacts": scope.billDetails.contacts,
                    "billAmount": scope.billDetails.billAmount
                }
            );
            expect(BillSplitterService.getBillDetails()).toEqual(
                {
                    "contacts": scope.billDetails.contacts,
                    "billAmount": scope.billDetails.billAmount
                }
            );
        }));

        it('should redirect to /tab/confirm', function () {
            spyOn(location, 'path');
            scope.goToConfirmPage();
            expect(location.path).toHaveBeenCalledWith("/tab/confirm");
        });
    });

    it('should be defined', inject(function ($rootScope, $controller) {
        //spec body
        var splitCtrl = $controller('SplitCtrl', { $scope: $rootScope.$new() });
        expect(splitCtrl).toBeDefined();
    }));

    it('should fetch latest bill details from service', inject(function ($rootScope, $controller, BillSplitterService) {
        spyOn(BillSplitterService, 'getBillDetails');
        var splitCtrl = $controller('SplitCtrl', { $scope: $rootScope.$new() });
        expect(BillSplitterService.getBillDetails).toHaveBeenCalled();
    }));



    describe("scope ops", function(){
        var scope;
        beforeEach(inject(function ($rootScope, $controller) {
            scope = $rootScope.$new();
            $controller("SplitCtrl", {
                $scope: scope
            });
            scope.$digest();
        }));

        it('should send billAmount as string to BillSplitterService', inject(function(BillSplitterService){
            spyOn(BillSplitterService, "updateBillDetails").andCallThrough();
            scope.billDetails.contacts = [
                {"$$hashKey": "00L", "email": "a@bc.de"},
                {"$$hashKey": "00N", "email": "a@bc.def"},
                {"$$hashKey": "00P", "email": "a@bc.defg"}
            ];
            scope.billDetails.billAmount = "12.34";
            scope.publishBillDetailsUpdate(scope.getBillDetails());
            expect(BillSplitterService.updateBillDetails.mostRecentCall.args[0].billAmount).toEqual(
                "12.34"
            );
            scope.billDetails.billAmount = 12.34;
            scope.publishBillDetailsUpdate(scope.getBillDetails());
            expect(BillSplitterService.updateBillDetails.mostRecentCall.args[0].billAmount).toEqual(
                "12.34"
            );
        }));

        it('should send clean contacts array to BillSplitterService', inject(function(BillSplitterService){
            spyOn(BillSplitterService, "updateBillDetails").andCallThrough();
            scope.billDetails.contacts = [
                {"$$hashKey": "00L", "email": "a@bc.de"},
                {"$$hashKey": "00N", "email": "a@bc.def"},
                {"$$hashKey": "00P"},
                null,
                {},
                undefined,
                123.45
            ];
            scope.billDetails.billAmount = "12.34";

            scope.publishBillDetailsUpdate(scope.getBillDetails());
            expect(BillSplitterService.updateBillDetails.mostRecentCall.args[0].contacts).toEqual(
                ["a@bc.de", "a@bc.def"]
            );
        }));

        it('should filter out empty contact in contacts array before sending to BillSplitterService', inject(function(BillSplitterService){
            spyOn(BillSplitterService, "updateBillDetails").andCallThrough();
            scope.billDetails.contacts = ["a@bc.de", "a@bc.def", "a@bc.defg", "", "a@bc.defgh"];
            scope.billDetails.billAmount = "12.34";

            scope.publishBillDetailsUpdate(scope.getBillDetails());
            expect(BillSplitterService.updateBillDetails.mostRecentCall.args[0].contacts).toEqual(
                ["a@bc.de", "a@bc.def", "a@bc.defg", "a@bc.defgh"]
            );
        }));

        describe("contacts", function () {
            var scope;
            beforeEach(inject(function ($rootScope, $controller) {
                scope = $rootScope.$new();
                $controller("SplitCtrl", {
                    $scope: scope
                });
                scope.$digest();
            }));

            it('should have var contacts defined', function () {
                expect(scope.billDetails.contacts).toBeDefined();
            });

            it('should not be empty', function () {
                expect(_.isEmpty(scope.billDetails.contacts)).toBe(false);
            });

            it('should be an array', function () {
                expect(_.isArray(scope.billDetails.contacts)).toBe(true);
            });

            it('should always have at least one empty item', function () {
                var i = 0;
                var fillInEmailIfEmpty = function (item) {
                    if (_.isEmpty(item)) {
                        item.email = "asdf.asdf@asdf.asfd";
                    }
                    return item;
                };

                do {
                    _.map(scope.billDetails.contacts, fillInEmailIfEmpty);
                    scope.$digest();
                    expect(_.size(_.filter(scope.billDetails.contacts, _.isEmpty))).not.toBeLessThan(1);
                } while (++i < 50);
            });

            it('should always have at least one empty item even when angular injects $$hashkey', function () {
                var i = 0;
                var fillInEmailIfEmpty = function (item) {
                    if (_.isEmpty(item)) {
                        item.email = "asdf.asdf@asdf.asfd";
                    }
                    return item;
                };
                var injectHashKey = function(item) {
                    item.$$hashKey = "blah";
                };

                var hasNoEmail = function (item) {
                    return _.not(_.curry2(_.flip(_.has))("email")(item));
                };

                do {
                    _.map(scope.billDetails.contacts, fillInEmailIfEmpty);
                    _.map(scope.billDetails.contacts, injectHashKey);
                    scope.$digest();
                    _.map(scope.billDetails.contacts, injectHashKey);
                    expect(_.size(_.filter(scope.billDetails.contacts, hasNoEmail))).not.toBeLessThan(1);
                } while (++i < 5);
            });

            it('should stop adding new ones if there are already one free slot', function () {
                var i = 0;
                var fillInEmailIfEmpty = function (item) {
                    if (_.isEmpty(item)) {
                        item.email = "asdf.asdf@asdf.asfd";
                    }
                    return item;
                };
                var injectHashKey = function(item) {
                    item.$$hashKey = "blah";
                };

                do {
                    _.map(scope.billDetails.contacts, fillInEmailIfEmpty);
                    _.map(scope.billDetails.contacts, injectHashKey);
                    scope.$digest();
                } while (++i < 5);

                // now remove the first one
                scope.billDetails.contacts[0] = {"email": ""};
                scope.$digest();
                scope.billDetails.contacts[5] = {"email": "asdf.asdf@asdf.afd"};
                scope.$digest();
                expect(_.size(scope.billDetails.contacts)).toBe(6);

            });
        });
    });

    it('should update billamount live without pressing next', function(){
        expect(true).toBe(false);
    })

    it('should update contacts live without pressing next', function(){
        expect(true).toBe(false);
    })



});
