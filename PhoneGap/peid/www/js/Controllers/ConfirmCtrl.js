angular.module('peid.controllers.confirmCtrl', [])
    .controller('ConfirmCtrl', function ($scope, $rootScope, BillSplitterService, ParseFunctionService, $location) {
        "use strict";

        var confirmCtrl = this;

        this.getFormattedBreakdown = function (breakdown) {
            var formattedBreakdown = {
                billAmount: {
                    'row': 0,
                    'icon': 'ion-pricetag',
                    'name': 'Bill Amount',
                    'color': 'energized',
                    'content': '£' + breakdown.billAmount
                },
                partySize: {
                    'row': 1,
                    'icon': 'ion-person-stalker',
                    'name': 'Party Size',
                    'color': '',
                    'content': '÷ ' + breakdown.partySize
                },
                costPerPerson: {
                    'row': 2,
                    'icon': 'ion-person',
                    'name': 'Cost per person',
                    'color': '',
                    'content': '= £' + breakdown.costPerPerson
                },
                costOfMagic: {
                    'row': 3,
                    'icon': 'ion-wand',
                    'name': 'Cost of magic (2%)',
                    'color': '',
                    'content': '+ £' + breakdown.costOfMagic
                },
                billPerPerson: {
                    'row': 4,
                    'icon': 'ion-card',
                    'name': 'Bill per person',
                    'color': 'balanced',
                    'content': '= £' + breakdown.billPerPerson
                }
            };
            return formattedBreakdown;
        };

        $rootScope.$on('billSplitterService.breakdownUpdated', function (event) {
            $scope.billDetails = BillSplitterService.getBillDetails();
            $scope.formattedBreakdown = confirmCtrl.getFormattedBreakdown(BillSplitterService.getBreakdown());
        });

        $scope.onConfirmClicked = function () {
            ParseFunctionService.sendBills();
            $location.path("/tab/rating");
        };

        // initialize fields
        $scope.billDetails = {contacts:[]};
        $scope.formattedBreakdown = {
            PendingBillDetails: {
                'row': 0,
                'icon': 'ion-wand',
                'name': 'Go to split tab to split a bill!',
                'color': '',
                'content': ''
            }
        };

        // Get latest details
        $scope.billDetails = BillSplitterService.getBillDetails();
        var breakdown = BillSplitterService.getBreakdown();
        if (!_.isEmpty(breakdown)) {
            $scope.formattedBreakdown = confirmCtrl.getFormattedBreakdown(BillSplitterService.getBreakdown());
        }
        console.log("latest details = " + JSON.stringify(breakdown));
        console.log($scope.formattedBreakdown);
    });