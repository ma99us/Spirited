'use strict';

// Declare app level module which depends on views, and core components
angular.module('myApp', [
    'ngRoute',
    'ngSanitize',
    'angular.filter',
    'myApp.view1',
    'myApp.view2',
    'api'
]).config(['$locationProvider', '$routeProvider', '$sceDelegateProvider', function ($locationProvider, $routeProvider, $sceDelegateProvider) {
    $locationProvider.hashPrefix('!');

    $routeProvider.otherwise({redirectTo: '/view1'});

    $sceDelegateProvider.resourceUrlWhitelist([
        // Allow same origin resource loads.
        'self'
        // Allow loading from our assets domain.  Notice the difference between * and **.
    ]);
}])
    .controller('IndexCtrl', ['$scope', function ($scope) {

        $scope.$on('$routeChangeSuccess', function (scope, next, current) {
            let path = next.$$route.originalPath;
            $scope.menuView1Style = "/view1" === path ? {'background-color':'CornFlowerBlue'} : {};
            $scope.menuView2Style = "/view2" === path ? {'background-color':'CornFlowerBlue'} : {};
        });

    }])
    .run(function (localstorage) {
        // console.log("FavTor started");
    });