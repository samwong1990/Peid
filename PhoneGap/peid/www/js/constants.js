angular.module('peid.constants', [])
    .constant('AUTH_EVENTS', {
        loginSuccess: 'auth-login-success',
        loginFailed: 'auth-login-failed',
        logoutSuccess: 'auth-logout-success',
        sessionTimeout: 'auth-session-timeout',
        notAuthenticated: 'auth-not-authenticated',
        notAuthorized: 'auth-not-authorized'
    })
    .constant('utils', {
        isStringArray: function (arr) {
            return _.isArray(arr) &&
                _.foldr(arr, function (acc, item) {
                    return acc && _.isString(item);
                }, true);

        }
    });
