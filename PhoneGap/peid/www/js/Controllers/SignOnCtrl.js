angular.module('peid.controllers.signOnCtrl', [])
    .controller('SignOnCtrl', function ($scope, $rootScope, $location, AuthService) {
        "use strict";
        var completeProfileHint = "Almost there! Just need to confirm your name and email.";

        $scope.hasCompleteProfile = function (user) {
            return !_.isEmpty(user.name) && !_.isEmpty(user.email);
        };

        var user = AuthService.getCurrentUser();
        $scope.credentials = user || {};

        $scope.state = {};
        if (_.isEmpty(user)) {
            $scope.state.formToShow = "signIn";
        } else if ($scope.hasCompleteProfile(user)) {
            $scope.state.formToShow = "loggedIn";
        } else {
            $scope.state.formToShow = "updateProfile";
        }

        $scope.updateCredentials = function (user) {
                $scope.credentials = user;
        };

        $scope.clearMessages = function () {
            $scope.errorMessage = "";
            $scope.hint = "";
        };

        $scope.goToSplitTab = function () {
            $location.path("/tab/split");
        };

        $scope.goToUpdateProfile = function () {
            $scope.state.formToShow = "updateProfile";
        }

        $scope.login = function (credentials) {
            $scope.clearMessages();
            console.log('calling auhgtservice');
            AuthService.login(credentials).then(function (user) {
                console.log('inside then');
                console.log("credentials used to be " + JSON.stringify($scope.credentials));
                $scope.updateCredentials(user);
                console.log("credentials is now" + JSON.stringify($scope.credentials));
                console.log('logged in and got' + JSON.stringify(user));
                $scope.state.formToShow = 'loggedIn';
                console.log('formToShow is ' + $scope.state.formToShow);
            }, function (error) {
                console.log('inside error');
                $scope.errorMessage = error;
            });
        };

        $scope.logout = function () {
            $scope.clearMessages();
            AuthService.logout();

            // Wipe all session related data
            $scope.updateCredentials({});
            $scope.state.formToShow = 'signIn';
        };

        $scope.updateProfile = function (credentials) {
            $scope.clearMessages();
            if (!credentials.name || !credentials.email) {
                $scope.errorMessage = "Both fields are mandatory";
                return;
            }

            AuthService.updateProfile(credentials).
                then(function (user) {
                    console.log("returned user is " + JSON.stringify(user));
                    $scope.updateCredentials(user);
                    $scope.state.formToShow = 'loggedIn';
                }, function (error) {
                    $scope.errorMessage = error;
                });
        };

        $scope.showConfirmPassword = false;
        $scope.signUp = function (credentials) {
            $scope.showConfirmPassword = true;
            $scope.clearMessages();
            if (_.isEmpty(credentials.confirmPassword)) {
                $scope.errorMessage = "Just need to confirm your password";
                return;
            }
            if (credentials.password !== credentials.confirmPassword) {
                $scope.errorMessage = "Passwords don't match";
                return;
            }

            AuthService.signUp(credentials).
                then(
                function (user) {
                    $scope.updateCredentials(user);
                    $scope.state.formToShow = 'loggedIn';
                },
                function (errorMessage) {
                    if (JSON.stringify(errorMessage) === '{}') {
                        $scope.errorMessage = "Encountered an error, please try again.";
                    } else {
                        $scope.errorMessage = errorMessage;
                    }
                    $scope.state.formToShow = 'signIn';
                }
            );
        };

        $scope.abortUpdateProfile = function () {
            if ($scope.hasCompleteProfile($scope.credentials)) {
                $scope.state.formToShow = 'loggedIn';
            } else {
                $scope.logout();
            }
        };

        // Traps
        $scope.$watch(
            'state.formToShow',
            function (newValue, oldValue) {
                console.log('state.formToShow updated');
                var user = $scope.credentials;
                console.log('credentials is ' + JSON.stringify(user));
                console.log('formToShow before is ' + JSON.stringify($scope.state.formToShow));

                if (_.isEmpty(user)) {
                    console.log('empty branch');
                    $scope.state.formToShow = "signIn";
                    return;
                }
                // if logged in but profile incomplete
                if (!$scope.hasCompleteProfile(user)) {
                    console.log('incomplete branch with formToShow=' + $scope.state.formToShow );
                    if (newValue === 'signIn') {
                        $scope.hint = completeProfileHint;
                        $scope.state.formToShow = 'updateProfile';
                        return;
                    } else if (newValue === 'loggedIn') {
                        $scope.hint = completeProfileHint;
                        $scope.state.formToShow = 'updateProfile';
                        return;
                    } else if (newValue === 'updateProfile') {
                        return;
                    } else {
                        $scope.hint = completeProfileHint;
                        $scope.state.formToShow = 'updateProfile';
                        return;
                    }
                }
                // user must have logged in with full profile
                if (newValue === 'signon') {
                    console.log('complete branch');
                    $scope.state.formToShow = 'loggedIn';
                    return;
                }
            }
        );
    })

    .directive('signIn', function () {
        "use strict";
        return {
            restrict: 'AE',
            replace: true,
            templateUrl: 'templates/signon/signIn.html'
        };
    })
    .directive('loggedIn', function () {
        "use strict";
        return {
            restrict: 'AE',
            replace: true,
            templateUrl: 'templates/signon/loggedIn.html'
        };
    })
    .directive('updateProfile', function () {
        "use strict";
        return {
            restrict: 'AE',
            replace: true,
            templateUrl: 'templates/signon/updateProfile.html'
        };
    });
