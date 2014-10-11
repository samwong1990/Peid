angular.module('peid.services.authService', [])
    .factory('AuthService', function ($q, $rootScope) {
        var authService = {};

        var convertToSimpleUser = function (parseUser) {
            return _.isEmpty(parseUser) ?
            {} :
            {
                email: parseUser.getEmail(),
                name: parseUser.get("name")
            };
        };

        authService.updateProfile = function (credentials) {
            var deferred = $q.defer(), parseUser = Parse.User.current();
            parseUser.save({
                email: credentials.email,
                name: credentials.name
            }, {
                success: function (parseUser) {
                    deferred.resolve(convertToSimpleUser(parseUser));
                },
                error: function (gameTurnAgain, error) {
                    deferred.reject(error);
                }
            });
            return deferred.promise;
        };

        authService.requestPasswordReset = function (email, options) {
            var deferred = $q.defer();

            Parse.User.requestPasswordReset(email, {
                success: function () {
                    deferred.resolve();
                },
                error: function (error) {
                    deferred.reject(error);
                }
            });
            return deferred.promise;
        };

        authService.getCurrentUser = function () {
            var parseUser = Parse.User.current();
            return convertToSimpleUser(parseUser);
        };

        authService.logout = function () {
            Parse.User.logOut();
        };

        authService.login = function (credentials) {
            var deferred = $q.defer();

            Parse.User.logIn(credentials.email, credentials.password, {
                success: function (parseUser) {
                    deferred.resolve(convertToSimpleUser(parseUser));
                },
                error: function (user, error) {
                    deferred.reject(error);
                }
            });
            return deferred.promise;
        };

        authService.signUp = function (credentials) {
            var deferred = $q.defer(), user = new Parse.User();
            user.set("username", credentials.email);
            user.set("password", credentials.password);
            user.set("email", credentials.email);

            // other fields can be set just like with Parse.Object
            user.set("name", credentials.name);

            user.signUp(null, {
                success: function (parseUser) {
                    deferred.resolve(convertToSimpleUser(parseUser));
                },
                error: function (user, error) {
                    deferred.reject(error);
                }
            });
            return deferred.promise;
        };

        return authService;
    });

