'use strict';

// Declare app level module which depends on views, and core components
angular.module('myApp', [
    'ngRoute',
    'ngSanitize',
    'myApp.view1',
    'myApp.view2'
]).config(['$locationProvider', '$routeProvider', '$sceDelegateProvider', function ($locationProvider, $routeProvider, $sceDelegateProvider) {
    $locationProvider.hashPrefix('!');

    $routeProvider.otherwise({redirectTo: '/view2'});

    $sceDelegateProvider.resourceUrlWhitelist([
        // Allow same origin resource loads.
        'self'
        // Allow loading from our assets domain.  Notice the difference between * and **.
    ]);
}])
    .controller('IndexCtrl', ['$scope', function ($scope) {

    }])
    .run(function (localstorage) {
        // console.log("FavTor started");
    });