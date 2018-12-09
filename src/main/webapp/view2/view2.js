'use strict';

angular.module('myApp.view2', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/view2', {
            templateUrl: 'view2/view2.html',
            controller: 'View2Ctrl'
        });
    }])
    .controller('View2Ctrl', ['$scope', '$http', '$window', function ($scope, $http, $window) {
        $scope.whisky = [];

        $scope.selectWhisky = function(whisky){
            $scope.selWhisky = whisky;
        };

        $scope.getAllWhisky = function () {
            $http.get('api/whisky/list', {params: {}}).then(function (response) {
                if ($scope.validateResponse(response)) {
                    $scope.whisky = response.data.data;
                }
            });
        };

        $scope.addTitle = function (str) {
            var w = {
                name: str,
            };
            $http.put('api/whisky/create', w).then(function (response) {
                if($scope.validateResponse(response)){
                    $scope.getAllWhisky();
                    $window.document.getElementById('name').focus();
                }
            });
        };

        $scope.addWhisky = function (w) {
            $http.put('api/whisky/create', w).then(function (response) {
                if($scope.validateResponse(response)){
                    $scope.getAllWhisky();
                }
            });
        };

        $scope.updateWhisky = function (w) {
            $http.post('api/whisky/update', w).then(function (response) {
                if($scope.validateResponse(response)){
                    $scope.getAllWhisky();
                }
            });
        };

        $scope.delWhisky = function (w) {
            $http.delete('api/whisky/delete/' + w.id, {params: {}}).then(function (response) {
                if($scope.validateResponse(response)){
                    $scope.getAllWhisky();
                }
            });
        };

        $scope.clearAllWhisky = function () {
            $scope.whisky.forEach(function (w) {
                $http.delete('api/whisky/delete/' + w.id, {params: {}}).then(function (response) {
                    if($scope.validateResponse(response)){
                        $scope.getAllWhisky();
                    }
                });
            });
        };

        $scope.validateResponse = function (response) {
            if (response && response.data && response.data.status != 200) {
                $scope.message = 'Server error: (' + response.data.status + ') ' + response.data.type + ': ' + response.data.message;
                return false;
            }
            else if (response && response.status != 200) {
                $scope.message = 'Http error: (' + response.status + ') ' + response.statusText;
                return false;
            }
            else {
                $scope.newWhisky = '';
                $scope.message = '';
                return response;
            }
        };

        $scope.getAllWhisky();
    }])
    .directive('dlEnterKey', function () {
        return {
            restrict: 'A',
            link: function ($scope, $element, $attrs) {
                $element.bind("keypress", function (event) {
                    var keyCode = event.which || event.keyCode;

                    if (keyCode === 13) {
                        $scope.$apply(function () {
                            $scope.$eval($attrs.dlEnterKey, {$event: event});
                        });

                    }
                });
            }
        };
    })
    .directive('whisky', function () {
        return {
            restrict: 'E',
            scope: {
                selWhisky: '<',
                editable: '<',
            },
            templateUrl: 'view2/whisky.html',
            controller: ['$scope', function($scope) {
                $scope.$watch('selWhisky', function (newValue, oldValue) {
                    $scope.init();
                }, true);
                $scope.$watch('editable', function (newValue, oldValue) {
                    $scope.init();
                }, true);
                $scope.clear = function(){
                    $scope.whisky = {};
                    $scope.updatable = false;
                };

                $scope.init = function(){
                    $scope.whisky = angular.copy($scope.selWhisky);   //FIXME: got to be a better way to disable two-way binding, but I am too tired
                    $scope.updatable = $scope.editable && $scope.whisky && $scope.whisky.id;
                };

                $scope.submit = function(whisky){
                    if($scope.updatable){
                        $scope.$parent.updateWhisky(whisky);
                    }
                    else{
                        $scope.$parent.addWhisky(whisky);
                    }
                };

            }],
            link: function (scope, elem, attrs) {

            }
        };
    });