'use strict';

// Declare app level module which depends on views, and core components
angular.module('myApp', [
    'ngRoute',
    'ngSanitize',
    'angular.filter',
    'myApp.view1',
    'api',
    'geolocation'
])
    .config(['$locationProvider', '$routeProvider', '$sceDelegateProvider', '$httpProvider', function ($locationProvider, $routeProvider, $sceDelegateProvider, $httpProvider) {
        //$locationProvider.hashPrefix('!');
        $locationProvider.hashPrefix('');
        // $locationProvider.html5Mode({
        //     enabled: true,
        //     requireBase: true
        // });

        $routeProvider.when('/legal', {
            templateUrl: 'privacy_policy.html'
        });
        $routeProvider.otherwise({redirectTo: '/view1'});

        $sceDelegateProvider.resourceUrlWhitelist([
            // Allow same origin resource loads.
            'self',
            // Allow loading from third party domains.
            'http://gerdov.com/spirited/**',
            'http://192.168.2.19/spirited/**',
            'https://3lspo5qztd.execute-api.us-west-2.amazonaws.com/prod/**',
            'http://spiritsearch.ca/**'
        ]);
    }])
    .controller('IndexCtrl', ['$scope', function ($scope) {
        // no-op
    }])
    .run(function (localstorage) {
        // console.log("Spirited started");
    });