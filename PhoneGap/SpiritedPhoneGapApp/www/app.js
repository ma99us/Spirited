'use strict';

// Declare app level module which depends on views, and core components
angular.module('myApp', [
    'ngRoute',
    'ngSanitize',
    'angular.filter',
    'myApp.view1',
    'myApp.view2',
    'api',
    'geolocation'
])
    .service('authInterceptor', function ($q) {
        var service = this;

        service.responseError = function (response) {
            if (response.status == 401) {
                window.location = "#/view1";
            }
            return $q.reject(response);
        };
    })
    .config(['$locationProvider', '$routeProvider', '$sceDelegateProvider', '$httpProvider', function ($locationProvider, $routeProvider, $sceDelegateProvider, $httpProvider) {
        //$locationProvider.hashPrefix('!');
        $locationProvider.hashPrefix('');
        // $locationProvider.html5Mode({
        //     enabled: true,
        //     requireBase: true
        // });

        $routeProvider.otherwise({redirectTo: '/view1'});

        $sceDelegateProvider.resourceUrlWhitelist([
            // Allow same origin resource loads.
            'self',
            // Allow loading from third party domains.
            'http://gerdov.com/spirited/**',
            'http://192.168.2.19/spirited/**',
            'https://3lspo5qztd.execute-api.us-west-2.amazonaws.com/prod/**'
        ]);

        $httpProvider.interceptors.push('authInterceptor');

    }])
    .controller('IndexCtrl', ['$scope', function ($scope) {

        $scope.$on('$routeChangeSuccess', function (scope, next, current) {
            let path = next.$$route.originalPath;
            $scope.menuView1Style = "/view1" === path ? {'background-color': 'CornFlowerBlue', color: 'black'} : {};
            $scope.menuView2Style = "/view2" === path ? {'background-color': 'CornFlowerBlue', color: 'black'} : {};
        });

    }])
    .run(function (localstorage) {
        // console.log("Spirited started");
    });