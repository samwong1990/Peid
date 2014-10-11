// Ionic peid App

// angular.module is a global place for creating, registering and retrieving Angular modules
// 'peid' is the name of this angular module example (also set in a <body> attribute in index.html)
// the 2nd parameter is an array of 'requires'
// 'peid.services' is found in services.js
// 'peid.controllers' is found in controllers.js
angular.module('peid', ['ionic', 'peid.controllers', 'peid.services', 'peid.constants'])

    .run(function ($ionicPlatform) {
        $ionicPlatform.ready(function () {
            // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
            // for form inputs)
            if (window.cordova && window.cordova.plugins.Keyboard) {
                cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
            }
            if (window.StatusBar) {
                // org.apache.cordova.statusbar required
                StatusBar.styleDefault();
                StatusBar.hide();
            }
        });
        // Parse initialization
        var app_id = "parse_app_id",
            js_key = "parse_js_id";
        Parse.initialize(app_id, js_key);
    })

    .config(function ($stateProvider, $urlRouterProvider) {

        // Ionic uses AngularUI Router which uses the concept of states
        // Learn more here: https://github.com/angular-ui/ui-router
        // Set up the various states which the app can be in.
        // Each state's controller can be found in controllers.js
        $stateProvider

            // setup an abstract state for the tabs directive
            .state('tab', {
                url: "/tab",
                abstract: true,
                templateUrl: "templates/tabs.html"
            })

            // Each tab has its own nav history stack:
            .state('tab.signon', {
                url: '/signon',
                // Note that controller must be defined inside 'views'
                // Else it won't get inherited in the children
                views: {
                    'tab-signon': {
                        templateUrl: 'templates/tab-signon.html',
                        controller: "SignOnCtrl"
                    }
                }
            })

            .state('tab.split', {
                url: '/split',
                views: {
                    'tab-split': {
                        templateUrl: 'templates/tab-split.html',
                        controller: 'SplitCtrl'
                    }
                }
            })

            .state('tab.confirm', {
                url: '/confirm',
                views: {
                    'tab-confirm': {
                        templateUrl: 'templates/tab-confirm.html',
                        controller: 'ConfirmCtrl'
                    }
                }
            })

            .state('tab.rating', {
                url: '/rating',
                views: {
                    'tab-rating': {
                        templateUrl: 'templates/tab-rating.html',
                        controller: 'RatingCtrl'
                    }
                }
            });

        // if none of the above states are matched, use this as the fallback
        $urlRouterProvider.otherwise('/tab/signon');

    })

    .filter('orderObjectBy', function () {
        return function (items, field, reverse) {
            var filtered = [];
            angular.forEach(items, function (item) {
                filtered.push(item);
            });
            filtered.sort(function (a, b) {
                return (a[field] > b[field] ? 1 : -1);
            });
            if (reverse) filtered.reverse();
            return filtered;
        };
    });
;

