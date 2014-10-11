angular.module('peid.controllers.ratingCtrl', ['ionic.rating'])
    .controller('RatingCtrl', function ($rootScope, $scope, ParseFunctionService) {
        "use strict";
        // set the rate and max variables
        $scope.rating = {};
        $scope.rating.stars = 0;
        $scope.rating.max = 5;
        $scope.done = false;

        $scope.expandText = function () {
            var element = document.getElementById("rating-comment");
            element.style.height = element.scrollHeight + "px";
        };

        $scope.rpcState = ParseFunctionService.getSendBillsState();
        $scope.updateState = function (state) {
            console.log("updateState called with " + JSON.stringify(state));
            $scope.rpcState = state;
        };

        $scope.shouldShowProgress = function () {
            return $scope.rpcState.state !== "not-running";
        };

        $scope.getProgressType = function () {
            switch ($scope.rpcState.state) {
                case "not-running":
                    return "not-running";
                case "sending-request":
                    return "ongoing";
                case "succeeded":
                    return "success";
                case "failed":
                    return "fail";
            }
            throw "rpcState is in an unexpected State : " + $scope.rpcState.state;
        };

        $rootScope.$on('ParseFunctionService.sendBills.StateUpdated', function (event, newState) {
            console.log("received event in RatingCtrl:" + JSON.stringify(newState));
            $scope.$apply(function (scope) {
                scope.updateState(newState);
            });
            console.log("$scope.shouldShowProgress() returns " + $scope.shouldShowProgress());
            console.log("$scope.getProgressType() returns " + $scope.getProgressType());
        });

        //HACK! Put this feedback into its own directive
        var feedbackSent = false;
        $scope.submitFeedback = function () {
            ParseFunctionService.submitFeedback($scope.rating.stars, $scope.rating.comments);
            feedbackSent = true;
        };
        $scope.showFeedback = function(){
            return !feedbackSent;
        };

    });