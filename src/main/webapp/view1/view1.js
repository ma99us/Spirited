'use strict';

angular.module('myApp.view1', ['ngRoute', 'localstorage'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/view1', {
            templateUrl: 'view1/view1.html',
            controller: 'View1Ctrl'
        });
    }])
    .controller('View1Ctrl', ['$scope', 'localstorage', '$location', function ($scope, localstorage, $location) {

    }]);