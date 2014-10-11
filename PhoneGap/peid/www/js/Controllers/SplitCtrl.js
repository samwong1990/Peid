angular.module('peid.controllers.splitCtrl', ['peid.constants'])
    .controller('SplitCtrl', function ($scope, BillSplitterService, $location, utils) {
        "use strict";
        var latestDetails = BillSplitterService.getBillDetails();
        console.log("latestDetails=" + JSON.stringify(latestDetails));

        if (!_.isEmpty(latestDetails)) {
            // convert to hash
            $scope.billDetails = {
                billAmount: Number(latestDetails.billAmount),
                contacts: _.map(latestDetails.contacts, function (email) {
                    return {email: email};
                })
            };
            console.log("converted billDetails = " + JSON.stringify($scope.billDetails));
        } else {
            $scope.billDetails = {
                contacts: [],
                billAmount: 3.14
            };
        }


        var hasEmail = _.curry2(_.flip(_.has))("email");
        // decide when to add a new row
        $scope.$watch('billDetails.contacts', function (newValue, oldValue) {
            var emptySlots = _.size(_.filter(newValue, function (item) {
                return !hasEmail(item) || _.isEmpty(item.email);
            }));

            if (emptySlots < 1) {
                newValue.push({});
            }
        }, /* objectEquality = */ true); // if set to false, it won't trigger if you simply change the content of an element


        $scope.getBillDetails = function () {
            var contacts = $scope.billDetails.contacts;
            if (!utils.isStringArray(contacts)) {
                contacts = _.map(
                    contacts,
                    function (obj) {
                        return _.has(obj, 'email') ?
                            obj.email : undefined;
                    }
                );
            }
            contacts = _.compact(contacts);
            return {
                'contacts': contacts,
                'billAmount': '' + $scope.billDetails.billAmount
            };
        };

        $scope.publishBillDetailsUpdate = function (billDetails) {
            BillSplitterService.updateBillDetails(billDetails);
        };

        $scope.goToConfirmPage = function () {
            $scope.publishBillDetailsUpdate($scope.getBillDetails());
            $location.path("/tab/confirm");
        };
    });