'use strict';

/* jasmine specs for filters go here */

describe('filter', function () {
    beforeEach(module('peid'));//.filters'));


    describe('someFilter', function () {
        beforeEach(module(function ($provide) {
            // some prep
        }));


        it('should pass', function (interpolateFilter) {
            expect(true).toBe(true);
        });
    });
});
