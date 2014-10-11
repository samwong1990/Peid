/* jasmine specs for controllers go here */

describe('SignOnCtrl', function () {
    'use strict';
    beforeEach(module('peid'));

    it('should be defined', inject(function ($rootScope, $controller) {
        //spec body
        var signOnCtrl = $controller('SignOnCtrl', { $scope: $rootScope.$new() });
        expect(signOnCtrl).toBeDefined();
    }));

    describe("abortUpdateProfile", function () {
        var scope, authService, rootScope;
        beforeEach(inject(function ($rootScope, AuthService, $controller) {
            rootScope = $rootScope;
            scope = $rootScope.$new();
            authService = AuthService;
            $controller("SignOnCtrl", {
                $scope: scope
            });
            scope.state.formToShow = "updateProfile";
        }));

        it('should go to logged in if profile is complete', function(){
            scope.credentials = {name: "a", email: "asd@asd.dsa"};
            scope.abortUpdateProfile();
            expect(scope.state.formToShow).toEqual('loggedIn');
        });
        it('should go to signIn if profile is not complete', function(){
            scope.credentials = {name: "a"};
            scope.abortUpdateProfile();
            expect(scope.state.formToShow).toEqual('signIn');
        });
    });

    describe("when login", function () {
        var scope, authService, rootScope;
        beforeEach(inject(function ($rootScope, AuthService, $controller) {
            rootScope = $rootScope;
            scope = $rootScope.$new();
            authService = AuthService;
            $controller("SignOnCtrl", {
                $scope: scope
            });
        }));

        describe("is successful", function () {
            beforeEach(inject(function ($q) {
                var q = $q.defer();
                spyOn(authService, 'login').andReturn(q.promise);
                scope.login({name: "a", email: "bc@cd.com"}); // no credential check
                q.resolve({name: "abcd", email: "bcde@cd.com"});
                scope.$digest();
            }));

            it('should show loggedIn', function () {
                expect(scope.state.formToShow).toBe('loggedIn');
            });
            it('should show update credentials', function () {
                expect(scope.credentials).toEqual({name: "abcd", email: "bcde@cd.com"});
            });
        });

        describe("is failed", function () {
            beforeEach(inject(function ($q) {
                var q = $q.defer();
                spyOn(authService, 'login').andReturn(q.promise);
                scope.login({name: "a", email: "bc@cd.com"}); // no credential check
                q.reject("error!!!");
                scope.$digest();
            }));

            it('should set error message', function () {
                expect(scope.errorMessage).toBe("error!!!");
            });
            it('should continue to show signIn page', function () {
                expect(scope.state.formToShow).toBe('signIn');
            });
        });
    });

    describe("when logout", function () {
        var scope, authService, rootScope;
        beforeEach(inject(function ($rootScope, AuthService, $controller) {
            rootScope = $rootScope;
            scope = $rootScope.$new();
            authService = AuthService;
            $controller("SignOnCtrl", {
                $scope: scope
            });
        }));

        describe("is successful", function () {
            beforeEach(inject(function ($q) {
                spyOn(authService, 'logout');
                scope.state.formToShow = "loggedIn";
                scope.credentials = {this:"is not empty"};
                scope.logout();
            }));

            it('should wipe credentials', function () {
                expect(_.isEmpty(scope.credentials)).toBe(true);
            });

            it('should show login', function () {
                expect(scope.state.formToShow).toBe('signIn');
            });
        });
    });

    describe("when updateProfile", function () {
        var scope, authService, rootScope;
        beforeEach(inject(function ($rootScope, AuthService, $controller) {
            rootScope = $rootScope;
            scope = $rootScope.$new();
            authService = AuthService;
            $controller("SignOnCtrl", {
                $scope: scope
            });
        }));

        describe("if", function(){
            describe("update is successful", function () {
                var user = {name: "a", email: "bc@cd.com"};
                beforeEach(inject(function ($q) {
                    scope.state.formToShow = "updateProfile";


                    var q = $q.defer();
                    spyOn(authService, 'updateProfile').andReturn(q.promise);
                    scope.updateProfile(user);
                    q.resolve(user);
                    scope.$digest();
                }));

                it('should update credentials', function () {
                    expect(scope.credentials).toBe(user);
                });

                it('should show loggedIn', function () {
                    expect(scope.state.formToShow).toBe('loggedIn');
                });
            });
            describe("update is not successful", function () {
                var user = {name: "a", email: "bc@cd.com"};
                beforeEach(inject(function ($q) {
                    scope.state.formToShow = "updateProfile";
                    scope.credentials = {name:"asdf", email:"asdf@asdf.fdas"};
                    var q = $q.defer();
                    spyOn(authService, 'updateProfile').andReturn(q.promise);
                    scope.updateProfile(user);
                    q.reject("error!!!");
                    scope.$digest();
                }));

                it('should not update credentials', function () {
                    expect(scope.credentials).not.toBe(user);
                });

                it('should show updateProfile', function () {
                    expect(scope.state.formToShow).toBe('updateProfile');
                });

                it('should update error message', function(){
                    expect(scope.errorMessage).toEqual('error!!!');
                });
            });
        });
    });

    describe("sign up", function () {
        var scope, authService, rootScope;
        beforeEach(inject(function ($rootScope, AuthService, $controller) {
            rootScope = $rootScope;
            scope = $rootScope.$new();
            authService = AuthService;
            $controller("SignOnCtrl", {
                $scope: scope
            });
        }));

        it('should reject unmatched password', function(){
            scope.signUp({
                email: "asdf@asdf.asdf",
                password: "asdf",
                confirmPassword: "fdsa"
            });
            expect(scope.errorMessage).toEqual("Passwords don't match");
        })

        describe("is successful", function () {
            var credentials = {email: "bc@cd.com", password: "a", confirmPassword: "a"};
            var user = {name: "asdf", email: "fdas@fdsa.fdsa"};
            beforeEach(inject(function ($q) {
                scope.state.formToShow = "updateProfile";
                scope.credentials = {name: "asdf", email: "rewq@rewqr.eqw"};

                var q = $q.defer();
                spyOn(authService, 'signUp').andReturn(q.promise);
                scope.signUp(credentials);
                q.resolve(user);
                scope.$digest();
            }));

            it('should update credentials', function () {
                expect(scope.credentials).toBe(user);
            });

            it('should show loggedIn', function () {
                expect(scope.state.formToShow).toBe('loggedIn');
            });
        });

        describe("is not successful", function () {
            var credentials = {email: "bc@cd.com", password: "a", confirmPassword: "a"};
            var user = {name: "asdf", email: "fdas@fdsa.fdsa"};
            beforeEach(inject(function ($q) {
                scope.state.formToShow = "updateProfile";
                scope.credentials = {name: "asdf", email: "rewq@rewqr.eqw"};

                var q = $q.defer();
                spyOn(authService, 'signUp').andReturn(q.promise);
                scope.signUp(credentials);
                q.reject("error!!!");
                scope.$digest();
            }));

            it('should not update credentials', function () {
                expect(scope.credentials).not.toBe(user);
            });

            it('should show signIn', function () {
                expect(scope.state.formToShow).toBe('signIn');
            });

            it('should set error Message', function () {
                expect(scope.errorMessage).toBe('error!!!');
            });
        });
    });

    describe("findLandingPage", function () {
        var scope;
        beforeEach(inject(function ($rootScope) {
            scope = $rootScope.$new();
        }));

        it("should put user in log in screen if not logged in", inject(function (AuthService, $controller) {
            spyOn(AuthService, 'getCurrentUser').andReturn({});
            $controller("SignOnCtrl", {
                $scope: scope
            });
            expect(scope.state.formToShow).toEqual('signIn');
        }));

        it("should put user in update profile if logged in but profile is incomplete (missing name)", inject(function (AuthService, $controller) {
            spyOn(AuthService, 'getCurrentUser').andReturn({email: "ab@cd.com"});
            $controller("SignOnCtrl", {
                $scope: scope
            });
            expect(scope.state.formToShow).toEqual('updateProfile');
        }));

        it("should put user in update profile if logged in but profile is incomplete (missing email)", inject(function (AuthService, $controller) {
            spyOn(AuthService, 'getCurrentUser').andReturn({name: "ab cd com"});
            $controller("SignOnCtrl", {
                $scope: scope
            });
            expect(scope.state.formToShow).toEqual('updateProfile');
        }));

        it("should put user in loggedIn if logged in and profile is complete", inject(function (AuthService, $controller) {
            spyOn(AuthService, 'getCurrentUser').andReturn({name: "ab cd ef", email: "ab@cd.com"});
            $controller("SignOnCtrl", {
                $scope: scope
            });
            expect(scope.state.formToShow).toEqual('loggedIn');
        }));
    });

    describe("trap", function () {
        var AuthServiceRef, scope, state;
        beforeEach(inject(function ($rootScope, $controller, AuthService, $state) {
            state = $state;
            AuthServiceRef = AuthService;
            scope = $rootScope.$new();
            $controller("SignOnCtrl", {
                $scope: scope,
                AuthService: AuthService
            });
        }));

        describe("if user is signed out", function () {

            it("should be fine to show signin form", function () {
                scope.credentials = {};
                scope.state.formToShow = 'signIn';
                scope.$digest();
                expect(scope.state.formToShow).toEqual('signIn');
            });

            it("should revert to signin if for any reason it tries to show logged in form", function () {
                scope.credentials = {};
                scope.state.formToShow = 'loggedIn';
                scope.$digest();
                expect(scope.state.formToShow).toEqual('signIn');
            });

            it("should revert to signin if for any reason it tries to show updateProfile form", function () {
                scope.credentials = {};
                scope.state.formToShow = 'updateProfile';
                scope.$digest();
                expect(scope.state.formToShow).toEqual('signIn');
            });

            it("should revert to signin if the state is unknown", function () {
                scope.credentials = {};
                scope.state.formToShow = 'sadfwejfask';
                scope.$digest();
                expect(scope.state.formToShow).toEqual('signIn');
            });
        });

        describe("if user is signed in but profile is incomplete", function () {

            it("should be kicked to updateProfile when it access loggedIn", function () {
                scope.credentials = {email: "ab@cd.com"};
                scope.state.formToShow = 'loggedIn';
                scope.$digest();
                expect(scope.state.formToShow).toEqual('updateProfile');
            });

            it("should be fine to stay at updateProfile when it access updateProfile", function () {
                scope.credentials = {email: "ab@cd.com"};
                scope.state.formToShow = 'updateProfile';
                scope.$digest();
                expect(scope.state.formToShow).toEqual('updateProfile');
            });

            it("should be kicked to updateProfile when it access signin", function () {
                scope.credentials = {email: "ab@cd.com"};
                scope.state.formToShow = 'signIn';
                scope.$digest();
                expect(scope.state.formToShow).toEqual('updateProfile');
            });

            it("should be kicked to updateProfile when it access limbo state", function () {
                scope.credentials = {email: "ab@cd.com"};
                scope.state.formToShow = 'fkoasmienvsa';
                scope.$digest();
                expect(scope.state.formToShow).toEqual('updateProfile');
            });
        });

        describe("if user is signed in and profile is complete", function () {

            it("should be fine to stay at loggedIn when it access loggedIn", function () {
                scope.credentials = {email: "ab@cd.com", name: "b"};
                scope.state.formToShow = 'loggedIn';
                scope.$digest();
                expect(scope.state.formToShow).toEqual('loggedIn');
            });

            it("should be fine to stay at updateProfile when it access updateProfile", function () {
                scope.credentials = {email: "ab@cd.com", name: "b"};
                scope.state.formToShow = 'updateProfile';
                scope.$digest();
                expect(scope.state.formToShow).toEqual('updateProfile');
            });

            it("should be kicked to loggedIn when it access signon", function () {
                scope.credentials = {email: "ab@cd.com", name: "b"};
                scope.state.formToShow = 'signon';
                scope.$digest();
                expect(scope.state.formToShow).toEqual('loggedIn');
            });
        });
    });

    describe("goToSplitTab should redirect to /split", function () {
        var scope;
        var location;
        beforeEach(inject(function ($rootScope, $controller, $location) {
            location = $location;
            scope = $rootScope.$new();
            $controller("SignOnCtrl", {
                $scope: scope
            });
        }));

        it('should redirect to /tab/split', function () {
            spyOn(location, 'path');
            scope.goToSplitTab();
            expect(location.path).toHaveBeenCalledWith("/tab/split");
        });
    });
});
