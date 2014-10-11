angular.module('peid.services.billSplitterService', ['peid.constants'])
    .factory('BillSplitterService', function ($rootScope, utils) {
        "use strict";
        var billSplitterService = {
            // fields: billAmount, contacts
            // this is used to calculate breakdown
            billDetails: {},
            // fields: billAmount, partySize, costPerPerson, costOfMagic, billPerPerson
            // this is calculated from billDetails
            breakdown: undefined
        };

        billSplitterService.getPartySize = function (contacts) {
            return _.size(contacts) + 1;
        };

        billSplitterService.roundUpDivision = function (numerator, divisor, decimalPlaces) {
            var scale = Math.pow(10, decimalPlaces);
            return Math.ceil((numerator / divisor) * scale) / scale;
        };

        billSplitterService.roundUpMultiplication = function (multiplicand, multiplier, decimalPlaces) {

            var scale = Math.pow(10, decimalPlaces);
            // toFixed(2) was added to fix the failing case
            // 28*0.02 = 0.56, 28*0.02*100 = 56.00000000000001
            // ceil then bump it to 57 which is very wrong.
            // This will not fix all edge cases, if this proves to be a problem, we'll need
            // a bignum library.
            var result = Math.ceil(((multiplicand * multiplier) * scale).toFixed(2)) / scale;
            return result.toFixed(decimalPlaces);
        };

        // returns string to ensure there is no floating point creep
        billSplitterService.calculateBreakdown = function (billAmount, partySize) {
            var breakdown = {};
            breakdown.billAmount = billAmount.toString();
            breakdown.partySize = partySize.toString();
            breakdown.costPerPerson = this.roundUpDivision(billAmount, partySize, 2).toString();
            breakdown.costOfMagic = this.roundUpMultiplication(breakdown.costPerPerson, 0.02, 2).toString();
            breakdown.billPerPerson = (Number(breakdown.costPerPerson) + Number(breakdown.costOfMagic)).toFixed(2).toString();
            return breakdown;
        };

        billSplitterService.updateBillDetails = function (newBillDetails) {
            // sanity check
            if (_.isEmpty(newBillDetails)) {
                console.error("newBillDetails should not satisfy _.isEmpty");
                console.error("Received " + JSON.stringify(newBillDetails));
                return;
            }
            if (!_.isString(newBillDetails.billAmount)) {
                console.error("newBillDetails.billAmount should be a string");
                console.error("Received " + JSON.stringify(newBillDetails));
                return;
            }
            if (!utils.isStringArray(newBillDetails.contacts)) {
                console.error("newBillDetails.contacts should be a string array");
                console.error("Received " + JSON.stringify(newBillDetails));
                return;
            }

            console.log("emit:" + JSON.stringify(this.billDetails));
            this.billDetails = _.clone(newBillDetails, true);
            this.breakdown = this.calculateBreakdown(this.billDetails.billAmount, this.getPartySize(this.billDetails.contacts));

            $rootScope.$emit('billSplitterService.breakdownUpdated');
        };

        billSplitterService.getBillDetails = function () {
            return _.clone(this.billDetails, true);
        };

        billSplitterService.getBreakdown = function () {
            return _.clone(this.breakdown, true);
        };

        return billSplitterService;
    });

