'use strict';

angular.module('myApp.view1', ['ngRoute', 'localstorage'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/view1', {
            templateUrl: 'view1/view1.html',
            controller: 'View1Ctrl'
        });
    }])
    .controller('View1Ctrl', ['$scope', '$q', 'localstorage', '$api', 'geolocation', function ($scope, $q, localstorage, $api, geolocation) {
        $scope.favStores = [];
        $scope.allStores = [];
        $scope.allWhiskies = [];
        $scope.dispQuantity = 10;

        $scope.scanBarcode = function () {
            //$scope.findWhisky('5000277003457');     // #DEBUG
            cordova.plugins.barcodeScanner.scan(
                function (result) {
                    // alert("We got a barcode\n" +
                    //     "Result: " + result.text + "\n" +
                    //     "Format: " + result.format + "\n" +
                    //     "Cancelled: " + result.cancelled);

                    if (result.cancelled || !result.text) {
                        alert("Scanning failed");
                        return;
                    }
                    $scope.findWhisky(result.text);
                },
                function (error) {
                    alert("Scanning failed: " + error);
                }
            );
        };

        $scope.onFavWhiskyNameChange = function (newVal) {
            if(newVal.length >=4){
                $scope.busyC = true;
                $scope.clearFavWhisky();
                $scope.allWhiskies = [];
                $scope.getAllWhiskies(newVal).finally(function(){
                    $scope.busyC = false;
                });
            }
        };

        $scope.captureUserLocation = function () {
            $scope.message = undefined;
            $scope.busyLoc = true;
            geolocation.getCurrentPosition().then(function (geo) {
                return geolocation.getNearbyCities(geo.coords.latitude, geo.coords.longitude);
            })
                .then(function (cities) {
                    let cNames = cities.map(function (c) {
                        return c.name;
                    });
                    //console.log(cNames);
                    $scope.selectAllCitiesStores(cNames);
                    // collapse stores selection panel
                    if ($scope.favStores.length) {
                        $('#favStores').collapse('hide');
                    }
                })
                .catch(function (err) {
                    $scope.message = err;
                })
                .finally(function () {
                    $scope.busyLoc = false;
                });
        };

        $scope.toggleStoreSelection = function (store) {
            let idx = $scope.favStores.indexOf(store);
            if (idx >= 0) {
                $scope.favStores.splice(idx, 1);    // deselect
            }
            else {
                $scope.favStores.push(store);   // select
            }

            $scope.onSelectedStoresChanged();    // update all
        };

        $scope.onSelectedStoresChanged = function () {
            // get uniques store cities
            $scope.favStoresCities = '<i><b>' + $scope.favStores.map(function (s) {
                return s.city;
            }).filter(function (value, index, self) {
                return self.indexOf(value) === index;
            }).join('</b></i>, <i><b>') + '</b></i>';
            // re-do the simlar whisky search
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

        $scope.selectAllCitiesStores = function (cities) {
            $scope.allStores.forEach(function (s) {
                if ($scope.favStores.indexOf(s) < 0 && cities.indexOf(s.city) >= 0) {
                    $scope.favStores.push(s);
                }
            });

            $scope.onSelectedStoresChanged();
        };

        $scope.clearFavWhisky = function () {
            $scope.favWhisky = undefined;
            $scope.fpData = undefined;
            $scope.similarWhiskies = undefined;
            $scope.selectedWhisky = undefined;
            $scope.selectedAvailableQty = undefined;
            $scope.selFpData = undefined;
            $scope.dispQuantity = 10;
        };

        $scope.selectFavWhisky = function (w) {
            $scope.clearFavWhisky();
            $scope.favWhisky = w;
            $scope.favWhiskyName = w.name;
            if ($scope.preferences) {
                $scope.preferences.favWhiskyName = $scope.favWhiskyName;
                $scope.savePrefs($scope.preferences);
            }
            $scope.busyW = true;
            $scope.fpData = undefined;
            return $scope.getFavWhisky(w.id).then(function () {
                $scope.favWhisky.available = $scope.filterAvailability($scope.favWhisky).length > 0;
                $scope.buildFPChart($scope.favWhisky);
                $scope.getSimilarWhiskies($scope.favWhisky);
                $scope.busyW = false;
            });
        };

        $scope.showWhisky = function (w) {
            $scope.busySW = true;
            $scope.selFpData = undefined;
            $scope.selectedAvailableQty = undefined;
            $scope.selectedWhisky = w;
            $('#selectedWhiskyModalCenter').modal('show');
            $('#selectedWhiskyModalCenter').on('shown.bs.modal', function () {
                $scope.getSelWhisky(w).then(function () {
                    $scope.selectedAvailableQty = $scope.filterAvailability($scope.selectedWhisky);
                    $scope.buildSimilarFPChart($scope.selectedWhisky, $scope.favWhisky);
                    $scope.busySW = false;
                });
            })
        };

        $scope.buildFPChart = function (whisky) {
            const xLabels = ['smoky', 'peaty', 'spicy', 'herbal', 'oily', 'full_bodied', 'rich', 'sweet', 'briny', 'salty', 'vanilla', 'tart', 'fruity', 'floral'];
            //$scope.fpOptions = {scaleShowGridLines: false,};
            $scope.fpColors = [];
            $scope.fpLabels = [];
            $scope.fpData = [];
            $scope.fpOptions = {};
            $scope.fpStyle = {};
            if (whisky && whisky.flavorProfile) {
                $scope.fpOptions = {
                    responsive: true,
                    maintainAspectRatio: false,
                    title: {
                        fontColor: "black",
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
                });
                let width = $('#barDiv')[0].clientWidth;
                $scope.fpStyle = {'width': width+'px', 'height': width/1.7+'px'};
            }
        };

        $scope.buildSimilarFPChart = function (whisky1, whisky2) {
            const xLabels = ['smoky', 'peaty', 'spicy', 'herbal', 'oily', 'full_bodied', 'rich', 'sweet', 'briny', 'salty', 'vanilla', 'tart', 'fruity', 'floral'];
            //$scope.fpOptions = {scaleShowGridLines: false,};
            $scope.selFpColors = [];
            $scope.selFpLabels = [];
            $scope.selFpData = [[], []];
            $scope.selFpSeries = [];
            $scope.selFpOptions = {};
            $scope.selFpStyle = {};
            if (whisky1 && whisky1.flavorProfile && whisky2 && whisky2.flavorProfile) {
                $scope.selFpOptions = {
                    responsive: true,
                    maintainAspectRatio: false,
                    legend: {
                        labels: {
                            fontColor: "black",
                        },
                        display: true
                    },
                    title: {
                        fontColor: "black",
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
                    $scope.selFpColors.push(color1);
                    // second dataset
                    let value2 = whisky2.flavorProfile[lbl];
                    $scope.selFpData[1].push(value2);
                    let color2 = '#AAAAAA';
                    $scope.selFpColors.push(color2);
                });
            }
            let width = $('#chartDiv')[0].clientWidth;
            $scope.selFpStyle = {'width': width+'px', 'height': width/1.4+'px'};
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

        $scope.showMoreCandidates = function () {
            $scope.dispQuantity += 10;
        };

        $scope.savePrefs = function (prefs) {
            localstorage.setObject("spirited", prefs || $scope.preferences);
        };

        $scope.loadPrefs = function () {
            let prefs = localstorage.getObject("spirited", {});
            if (prefs.favStoreNames && $scope.allStores) {
                $scope.allStores.forEach(function (s) {
                    if (prefs.favStoreNames.indexOf(s.name) >= 0) {
                        $scope.favStores.push(s);   // select
                        //$scope.toggleStoreSelection(s);
                    }
                });
                $scope.onSelectedStoresChanged();   // update all post-store selection
            }
            if (prefs.favWhiskyName) {
                $scope.getAllWhiskies(prefs.favWhiskyName).then(function(){
                    $scope.allWhiskies.forEach(function (w) {
                        if (prefs.favWhiskyName === w.name) {
                            $scope.selectFavWhisky(w);
                        }
                    });
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

        $scope.getCacheStatus = function () {
            return $api.getCacheStatus().then(function (data) {
                $scope.message = undefined;
                $scope.cacheStatus = data.data;
            }).catch(function (err) {
                $scope.message = err;
            });
        };

        $scope.getAllStores = function () {
            return $api.getAllStores().then(function (data) {
                $scope.message = undefined;
                $scope.allStores = data.data;
            }).catch(function (err) {
                $scope.message = err;
            });
        };

        $scope.getAllWhiskies = function (name) {
            return $api.findWhiskiesLike(name).then(function (data) {
                $scope.message = undefined;
                $scope.allWhiskies = data.data;
            }).catch(function (err) {
                $scope.message = err;
            });
        };

        $scope.getFavWhisky = function (id) {
            return $api.getWhisky(id).then(function (data) {
                $scope.message = undefined;
                if ($scope.favWhisky && $scope.favWhisky.id === data.data.id) {
                    $scope.favWhisky = data.data;
                }
            }).catch(function (err) {
                $scope.message = err;
            });
        };

        $scope.getSelWhisky = function (whisky) {
            if (whisky.flavorProfile && whisky.quantities && whisky.quantities.length) {
                // do not re-request whisky data from API again if already have it
                $scope.selectedWhisky = whisky;
                let deferred = $q.defer();
                deferred.resolve(whisky);
                return deferred.promise;
            }
            else {
                return $api.getWhisky(whisky.id).then(function (data) {
                    $scope.message = undefined;
                    if ($scope.selectedWhisky && $scope.selectedWhisky.id === data.data.id) {
                        $scope.selectedWhisky = data.data;
                    }
                }).catch(function (err) {
                    $scope.message = err;
                });
            }
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

        $scope.findWhisky = function (code) {
            $api.findWhisky(code).then(function (data) {
                if (!data.data) {
                    alert('no such product :-(');
                    throw 'no such product :-(';
                }
                $scope.message = undefined;
                return $scope.selectFavWhisky(data.data);
            }). then(function () {
                $scope.showWhisky($scope.favWhisky);
            })
            .catch(function (err) {
                $scope.message = err;
            });
        };

        //// initialization:

        $scope.busy = true;
        $scope.getCacheStatus()
            .then(function () {
                return $scope.getAllStores();
            })
            .then(function () {
                return $scope.loadPrefs();
            })
            .catch(function(err){
                $scope.message = err;
            })
            .finally(function () {
                $scope.busy = false;
            });
    }])
    .directive('addressLink', function () {
        return {
            restrict: 'E',
            scope: {
                addressTxt: '@address'
            },
            template: "<a href='http://maps.google.com/maps?q={{mapLink}}' target='_blank'><address>{{address}}</address></a>",
            link: function (scope, elem, attrs) {
                scope.mapLink = encodeURIComponent(scope.addressTxt);
                scope.address = scope.addressTxt;
            }
        };
    })
;
