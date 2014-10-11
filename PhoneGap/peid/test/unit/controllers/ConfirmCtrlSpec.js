/* jasmine specs for controllers go here */

describe('ConfirmCtrl', function () {
    'use strict';
    beforeEach(module('peid'));

    describe("scope work", function () {
        var scope, location, confirmCtrl;
        beforeEach(inject(function ($rootScope, $controller, $location) {
            location = $location;
            scope = $rootScope.$new();
            confirmCtrl = $controller("ConfirmCtrl", {
                $scope: scope
            });
        }));

        it('should be defined', inject(function () {
            expect(confirmCtrl).toBeDefined();
        }));

        it('BillSplitterService should be defined', inject(function (BillSplitterService) {
            expect(BillSplitterService).toBeDefined();
        }));

        describe("when Confirm is pressed (onConfirmClicked)", function () {
            var parseFunctionService;
            beforeEach(inject(function (ParseFunctionService) {
                parseFunctionService = ParseFunctionService;
                spyOn(parseFunctionService, 'sendBills');
            }));

            it('onConfirmClicked should be defined', function () {
                expect(scope.onConfirmClicked).toBeDefined();
            });

            it('should call parse function to send split bill emails', function () {
                scope.onConfirmClicked();
                expect(parseFunctionService.sendBills.calls.length).toEqual(1);
            });

            it('should redirect to rating page on click', inject(function ($location) {
                spyOn($location, 'path');
                scope.onConfirmClicked();
                expect($location.path).toHaveBeenCalledWith("/tab/rating");
            }));
        });

        it('should update breakdown as BillSplitterService publish changes', inject(function (BillSplitterService) {
            var modelAns = [578.87, 4, 144.72, 2.9, 147.62];
            var keys = [
                'billAmount',
                'partySize',
                'costPerPerson',
                'costOfMagic',
                'billPerPerson'
            ];

            var modelBreakdown = _.zipObject(keys, modelAns);
            // note that 3 email + user = party of 4.
            var newContacts = ["a@cd.com", "ab@cd.com", "abc@cd.com"];

            BillSplitterService.updateBillDetails({
                'contacts': newContacts,
                'billAmount': '' + modelBreakdown.billAmount
            });

            for (var j = 0; j < keys.length; j++) {
                var modelAnswerAsString = modelBreakdown[keys[j]];
                expect(_.contains(scope.formattedBreakdown[keys[j]].content, modelAnswerAsString)).toBe(true);
            }
        }));

        it('should update contacts as BillSplitterService publish changes', inject(function (BillSplitterService) {
            var modelAns = [578.87, 4, 144.72, 2.9, 147.62];
            var keys = [
                'billAmount',
                'partySize',
                'costPerPerson',
                'costOfMagic',
                'billPerPerson'
            ];

            var modelBreakdown = _.zipObject(keys, modelAns);
            // note that 3 email + user = party of 4.
            var newContacts = ["a@cd.com", "ab@cd.com", "abc@cd.com"];

            BillSplitterService.updateBillDetails({
                'contacts': newContacts,
                'billAmount': '' + modelBreakdown.billAmount
            });

            expect(scope.billDetails.contacts).toEqual(newContacts);
        }));

        describe("breakdown formatter", function () {
            it('should have a set structure for all of its items', function () {
                var givenBreakdown = {
                    billAmount: "999.99",
                    partySize: "12",
                    costPerPerson: '12.34',
                    costOfMagic: '0.45',
                    billPerPerson: '45.67'
                };
                var expectedFormattedBreakdown = {
                    billAmount: {
                        'row': 0,
                        'icon': 'ion-pricetag',
                        'name': 'Bill Amount',
                        'color': 'energized',
                        'content': '£999.99'
                    },
                    partySize: {
                        'row': 1,
                        'icon': 'ion-person-stalker',
                        'name': 'Party Size',
                        'color': '',
                        'content': '÷ 12'
                    },
                    costPerPerson: {
                        'row': 2,
                        'icon': 'ion-person',
                        'name': 'Cost per person',
                        'color': '',
                        'content': '= £12.34'
                    },
                    costOfMagic: {
                        'row': 3,
                        'icon': 'ion-wand',
                        'name': 'Cost of magic (2%)',
                        'color': '',
                        'content': '+ £0.45'
                    },
                    billPerPerson: {
                        'row': 4,
                        'icon': 'ion-card',
                        'name': 'Bill per person',
                        'color': 'balanced',
                        'content': '= £45.67'
                    }
                };
                expect(confirmCtrl.getFormattedBreakdown(givenBreakdown)).toEqual(expectedFormattedBreakdown);
            });
        });

    });
});
