'use strict';

angular.module('myApp.view2', ['ngRoute'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/view2', {
            templateUrl: 'view2/view2.html',
            controller: 'View2Ctrl'
        });
    }])
    .controller('View2Ctrl', ['$scope', '$http', '$window', '$q', function ($scope, $http, $window, $q) {
        $scope.whisky = [];

        $scope.selectWhisky = function (whisky) {
            $scope.selWhisky = whisky;
            if($scope.selWhisky){
                $scope.getWhisky($scope.selWhisky.id).then(function(data){
                    $scope.selWhisky = data;
                });
            }
        };

        $scope.onPageChange = function (page) {
            $scope.getAllWhisky($scope.resultsPerPage, page, $scope.sortBy);
        };

        $scope.toggleResultsPerPage = function () {
            if ($scope.resultsPerPage === 10) {
                $scope.resultsPerPage = 30;
            }
            else if ($scope.resultsPerPage === 30) {
                $scope.resultsPerPage = 100;
            }
            else {
                $scope.resultsPerPage = 10;
            }
            $scope.getAllWhisky();
        };

        $scope.toggleSortBy = function () {
            if ($scope.sortBy === 'name') {
                $scope.sortBy = '-name';
            }
            else if ($scope.sortBy === '-name') {
                $scope.sortBy = 'unitPrice';
            }
            else {
                $scope.sortBy = 'name';
            }
            $scope.getAllWhisky();
        };

        ////// API calls /////

        $scope.rebuildAll = function(){
            $scope.busy = true;
            var ts0 = new Date().getTime();
            $http.get('api/whisky/rebuild', {params: {}}).then(function (response) {
                if ($scope.validateResponse(response)) {
                    //console.log(response.data.data);
                    $scope.getAllWhisky();
                }
                $scope.busy = false;
                $scope.lastRebuildTs = ((new Date().getTime() - ts0)/1000).toFixed(1);
            });
        };

        $scope.getAllWhisky = function (resultsPerPage, pageNumber, sortBy) {
            let params = {
                resultsPerPage: resultsPerPage || $scope.resultsPerPage,
                pageNumber: pageNumber || $scope.pageNumber,
                sortBy: sortBy || $scope.sortBy
            };
            var ts0 = new Date().getTime();
            $http.get('api/whisky', {params: params}).then(function (response) {
                if ($scope.validateResponse(response)) {
                    $scope.whisky = response.data.data;
                    $scope.resultsPerPage = response.data.metaData.resultsPerPage;
                    $scope.pageNumber = response.data.metaData.pageNumber;
                    $scope.sortBy = response.data.metaData.sortBy;
                    $scope.totalResults = response.data.metaData.totalResults;
                    $scope.lastQueryTs = new Date().getTime() - ts0;
                }
            });
        };

        $scope.getWhisky = function (id) {
            $scope.busyW = true;
            var deferred = $q.defer();
            $http.get('api/whisky/' + id, {params: {}}).then(function (response) {
                if ($scope.validateResponse(response)) {
                    deferred.resolve(response.data.data);
                }
                $scope.busyW = false;
            });
            return deferred.promise;
        };

        $scope.addTitle = function (str) {
            var w = {
                name: str,
            };
            $http.put('api/whisky', w).then(function (response) {
                if ($scope.validateResponse(response)) {
                    $scope.getAllWhisky();
                    $window.document.getElementById('name').focus();
                }
            });
        };

        $scope.addWhisky = function (w) {
            $http.put('api/whisky', w).then(function (response) {
                if ($scope.validateResponse(response)) {
                    $scope.getAllWhisky();
                }
            });
        };

        $scope.updateWhisky = function (w) {
            $http.post('api/whisky/' + w.id, w).then(function (response) {
                if ($scope.validateResponse(response)) {
                    $scope.getAllWhisky();
                }
            });
        };

        $scope.delWhisky = function (w) {
            $http.delete('api/whisky/' + w.id, {params: {}}).then(function (response) {
                if ($scope.validateResponse(response)) {
                    $scope.getAllWhisky();
                }
            });
        };

        $scope.clearAllWhisky = function () {
            $scope.whisky.forEach(function (w) {
                $http.delete('api/whisky/' + w.id, {params: {}}).then(function (response) {
                    if ($scope.validateResponse(response)) {
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

        $scope.getAllWhisky(10);
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
    .directive('whiskyRow', function () {
        return {
            restrict: 'E',
            scope: {
                whisky: '<',
                onClick: '&',
                selWhisky: '<'
            },
            templateUrl: 'view2/whisky-row.html',
            controller: ['$scope', function ($scope) {
                $scope.$watch('selWhisky', function (newValue, oldValue) {
                    $scope.isSelected = $scope.selWhisky && ($scope.whisky.id === $scope.selWhisky.id);
                    if($scope.isSelected){
                        $scope.rowStyle={'background-color':'CornFlowerBlue'};
                    }
                    else{
                        $scope.rowStyle={}
                    }
                }, true);
            }],
            link: function (scope, elem, attrs) {

            }
        };
    })
    .directive('whiskyDetails', function () {
        return {
            restrict: 'E',
            scope: {
                selWhisky: '<',
                editable: '<',
            },
            templateUrl: 'view2/whisky-details.html',
            controller: ['$scope', function ($scope) {
                $scope.$watch('selWhisky', function (newValue, oldValue) {
                    $scope.init();
                }, true);
                $scope.$watch('editable', function (newValue, oldValue) {
                    $scope.init();
                }, true);
                $scope.clear = function () {
                    $scope.whisky = {};
                    $scope.updatable = false;
                };

                $scope.init = function () {
                    $scope.whisky = $scope.fixWarehouseQuantities(angular.copy($scope.selWhisky));   //FIXME: got to be a better way to disable two-way binding, but I am too tired
                    $scope.updatable = $scope.editable && $scope.whisky && $scope.whisky.id;
                };

                $scope.onEnter = function(whisky){
                    if ($scope.updatable){
                        $scope.$parent.updateWhisky(whisky);
                    }
                }

                $scope.submit = function (whisky) {
                    if ($scope.updatable) {
                        $scope.$parent.updateWhisky(whisky);
                    }
                    else {
                        $scope.$parent.addWhisky(whisky);
                    }
                };

                $scope.fixWarehouseQuantities = function (whisky) {
                    if(!whisky || !whisky.quantities){
                        return whisky;
                    }
                    var qtyArr = Object.entries(whisky.quantities).map(function (entry) {
                        let wh = angular.fromJson(entry[0]);
                        return {
                            store: wh.store,
                            address: wh.address,
                            city: wh.city,
                            qty: entry[1]
                        }
                    });
                    whisky.quantities = qtyArr;

                    return whisky;
                }

            }],
            link: function (scope, elem, attrs) {

            }
        };
    })
    .directive('pagination', function () {
        return {
            restrict: 'E',
            scope: {
                resultsPerPage: '=',
                pageNumber: '=',
                totalResults: '=',
                callback: '&',
            },
            templateUrl: 'view2/pagination.html',
            controller: ['$scope', function ($scope) {
                $scope.$watch('resultsPerPage', function (newValue, oldValue) {
                    $scope.init();
                }, true);
                $scope.$watch('pageNumber', function (newValue, oldValue) {
                    $scope.init();
                }, true);
                $scope.$watch('totalResults', function (newValue, oldValue) {
                    $scope.init();
                }, true);

                $scope.onPrevPage = function (first) {
                    let page = first ? 1 : $scope.pageNumber - 1;
                    $scope.callback({page: page});
                };
                $scope.onNextPage = function (last) {
                    let page = last ? $scope.lastPage : $scope.pageNumber + 1;
                    $scope.callback({page: page});
                };

                $scope.init = function () {
                    $scope.lastPage = Math.floor($scope.totalResults / $scope.resultsPerPage) + 1;
                };
            }],
            link: function (scope, elem, attrs) {

            }
        };
    });