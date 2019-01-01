'use strict';

angular.module('myApp.view1', ['ngRoute', 'localstorage'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/view1', {
            templateUrl: 'view1/view1.html',
            controller: 'View1Ctrl'
        });
    }])
    .controller('View1Ctrl', ['$scope', 'localstorage', '$api', function ($scope, localstorage, $api) {
        $scope.favStores = [];
        $scope.allStores = [];
        $scope.allWhiskies = [];

        $scope.onFavWhiskyNameChange = function (newVal) {
            $scope.clearFavWhisky();
        };

        $scope.toggleStoreSelection = function (store) {
            let idx = $scope.favStores.indexOf(store);
            if (idx >= 0) {
                $scope.favStores.splice(idx, 1);
            }
            else {
                $scope.favStores.push(store);
            }
            if ($scope.favWhisky) {
                $scope.getSimilarWhiskies($scope.favWhisky);        // recalculate availability
            }
            //console.log('Favorite stores: ' + $scope.favStores);
            if ($scope.preferences) {
                $scope.preferences.favStoreNames = $scope.favStores.map(function (s) {
                    return s.name;
                });
                $scope.savePrefs($scope.preferences);
            }
        };

        $scope.clearFavWhisky = function () {
            $scope.favWhisky = undefined;
            $scope.fpData = undefined;
            $scope.similarWhiskies = undefined;
            $scope.selectedWhisky = undefined;
            $scope.selectedAvailableQty = undefined;
            $scope.selFpData = undefined;
        };

        $scope.selectWhisky = function (w) {
            $scope.favWhisky = w;
            $scope.favWhiskyName = w.name;
            if ($scope.preferences) {
                $scope.preferences.favWhiskyName = $scope.favWhiskyName;
                $scope.savePrefs($scope.preferences);
            }
            $scope.busyW = true;
            $scope.fpData = undefined;
            $scope.getWhisky(w.id).then(function () {
                $scope.busyW = false;
                $scope.buildFPChart($scope.favWhisky);
                $scope.getSimilarWhiskies($scope.favWhisky);
            });
        };

        $scope.showWhisky = function (w) {
            $scope.selectedWhisky = w;
            $('#selectedWhiskyModalCenter').modal('show');
            $scope.selectedAvailableQty = $scope.filterAvailability($scope.selectedWhisky);
            $scope.buildSelectedFPChart($scope.selectedWhisky, $scope.favWhisky);
        };

        $scope.buildFPChart = function (whisky) {
            const xLabels = ['smoky', 'peaty', 'spicy', 'herbal', 'oily', 'full_bodied', 'rich', 'sweet', 'briny', 'salty', 'vanilla', 'tart', 'fruity', 'floral'];
            //$scope.fpOptions = {scaleShowGridLines: false,};
            $scope.fpColors = [];
            $scope.fpLabels = [];
            $scope.fpData = [];
            $scope.fpOptions = {};
            if (whisky && whisky.flavorProfile) {
                $scope.fpOptions = {
                    title: {
                        display: true,
                        text: whisky.flavorProfile.flavors
                    }
                };
                xLabels.forEach(function (lbl) {
                    let label = lbl === 'full_bodied' ? 'FULL' : lbl.toUpperCase();
                    $scope.fpLabels.push(label);
                    let value = whisky.flavorProfile[lbl];
                    $scope.fpData.push(value);
                    let color = '#13b7c6';
                    if (value >= 80) {
                        color = '#FF5766';
                    }
                    else if (value >= 50) {
                        color = '#FFa7b6';
                    }
                    $scope.fpColors.push(color);
                })
            }
        };

        $scope.buildSelectedFPChart = function (whisky1, whisky2) {
            const xLabels = ['smoky', 'peaty', 'spicy', 'herbal', 'oily', 'full_bodied', 'rich', 'sweet', 'briny', 'salty', 'vanilla', 'tart', 'fruity', 'floral'];
            //$scope.fpOptions = {scaleShowGridLines: false,};
            $scope.selFpColors = [];
            $scope.selFpLabels = [];
            $scope.selFpData = [[], []];
            $scope.selFpSeries = [];
            $scope.selFpOptions = {};
            if (whisky1 && whisky1.flavorProfile && whisky2 && whisky2.flavorProfile) {
                $scope.selFpOptions = {
                    legend: {
                        display: true
                    },
                    title: {
                        display: true,
                        text: whisky1.flavorProfile.flavors
                    }
                };
                $scope.selFpSeries.push(whisky1.name);
                $scope.selFpSeries.push(whisky2.name);
                xLabels.forEach(function (lbl) {
                    let label = lbl === 'full_bodied' ? 'FULL' : lbl.toUpperCase();
                    $scope.selFpLabels.push(label);
                    // first dataset
                    let value1 = whisky1.flavorProfile[lbl];
                    $scope.selFpData[0].push(value1);
                    let color1 = '#FF5766';
                    //$scope.selFpColors.push(color1);
                    // second dataset
                    let value2 = whisky2.flavorProfile[lbl];
                    $scope.selFpData[1].push(value2);
                    let color2 = '#cccccc';
                    //$scope.selFpColors.push(color2);
                })
            }
        };

        $scope.filterAvailability = function (whisky) {
            if (!whisky || !whisky.quantities || !whisky.quantities.length) {
                return [];
            }
            let favStoresNames = [];
            if ($scope.favStores) {
                favStoresNames = $scope.favStores.map(function (s) {
                    return s.name;
                });
            }
            if (!favStoresNames.length) {
                return whisky.quantities;
            }
            let qts = whisky.quantities.filter(function (q) {
                return favStoresNames.indexOf(q.name) >= 0;
            });
            return qts;
        };

        $scope.savePrefs = function (prefs) {
            localstorage.setObject("spirited", prefs || $scope.preferences);
        };

        $scope.loadPrefs = function () {
            let prefs = localstorage.getObject("spirited", {});
            if (prefs.favStoreNames && $scope.allStores) {
                $scope.allStores.forEach(function (s) {
                    if (prefs.favStoreNames.indexOf(s.name) >= 0) {
                        $scope.favStores.push(s);
                    }
                });
            }
            if (prefs.favWhiskyName && $scope.allWhiskies) {
                $scope.allWhiskies.forEach(function (w) {
                    if (prefs.favWhiskyName === w.name) {
                        $scope.selectWhisky(w);
                    }
                });
            }
            $scope.preferences = prefs;
        };

        $scope.clearPrefs = function () {
            $scope.savePrefs({});
            $scope.preferences = {};
            $scope.favStores = [];
            $scope.favWhiskyName = undefined;
            $scope.clearFavWhisky()
        };

        //// API ////

        $scope.getAllStores = function () {
            return $api.getAllStores().then(function (data) {
                $scope.message = undefined;
                $scope.allStores = data.data;
            }).catch(function (err) {
                $scope.message = err;
            });
        };

        $scope.getAllWhiskies = function () {
            return $api.getAllWhiskies(1000, 1, 'name').then(function (data) {
                $scope.message = undefined;
                $scope.allWhiskies = data.data;
                $scope.totaWhiskiesNum = data.metaData.totalResults;
            }).catch(function (err) {
                $scope.message = err;
            });
        };

        $scope.getWhisky = function (id) {
            return $api.getWhisky(id).then(function (data) {
                $scope.message = undefined;
                if ($scope.favWhisky && $scope.favWhisky.id === data.data.id) {
                    $scope.favWhisky = data.data;
                }
            }).catch(function (err) {
                $scope.message = err;
            });
        };

        $scope.getSimilarWhiskies = function (whisky) {
            $api.getSimilarWhiskies(whisky).then(function (data) {
                $scope.message = undefined;
                $scope.similarWhiskies = data.data.map(function (sw) {
                    sw.available = $scope.filterAvailability(sw.candidate).length > 0;
                    return sw;
                });
            }).catch(function (err) {
                $scope.message = err;
            });
        };

        ////

        $scope.getAllStores()
            .then(function () {
                return $scope.getAllWhiskies();
            })
            .then(function () {
                return $scope.loadPrefs();
            });
    }]);