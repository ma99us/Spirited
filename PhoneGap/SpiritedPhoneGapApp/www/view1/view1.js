'use strict';

angular.module('myApp.view1', ['ngRoute', 'localstorage', 'phoneapi'])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/view1', {
            templateUrl: 'view1/view1.html',
            controller: 'View1Ctrl'
        });
    }])
    .controller('View1Ctrl', ['$scope', '$q', 'localstorage', '$api', 'geolocation', 'phoneapi', function ($scope, $q, localstorage, $api, geolocation, phoneapi) {
        $scope.favStores = [];
        $scope.allStores = [];
        $scope.allWhiskies = [];
        $scope.dispQuantity = 10;
        $scope.recentWhiskies = [];

        $scope.log = function (msg) {
            if (msg && typeof msg === 'object') {
                $scope.message = JSON.stringify(msg);
            } else {
                $scope.message = msg
            }
        };

        $scope.isDeviceReady = phoneapi.isDeviceReady;  // function

        $scope.scanBarcode = function () {
            phoneapi.useCamera(function () {
                    cordova.plugins.barcodeScanner.scan(
                        function (result) {
                            if (result.cancelled || !result.text) {
                                alert("Scanning failed");
                            } else {
                                $scope.findWhiskyByCode(result.text);
                            }
                        },
                        function (error) {
                            alert("Scanning failed: " + error);
                        }
                    );
                },
                function (err) {
                    if (err === 'No Cordova') {
                        $scope.findWhiskyByCode('5000277003457');     // #TEST
                    } else {
                        alert("Camera unavailable");

                    }
                });
        };

        $scope.onFavWhiskyNameChange = function (newVal) {
            if (newVal.length >= 4) {
                $scope.busyC = true;
                $scope.clearFavWhisky();
                $scope.allWhiskies = [];
                $scope.getAllWhiskies(newVal).finally(function () {
                    $scope.busyC = false;
                });
            }
        };

        $scope.captureUserLocation = function () {
            phoneapi.useLocation(function () {
                $scope.log(undefined);
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
                        $scope.log(err);
                    })
                    .finally(function () {
                        $scope.busyLoc = false;
                    });
            }, function (err) {
                if (err === 'No Cordova') {
                    $scope.selectAllCitiesStores(['Fredericton']);      // #TEST
                    // collapse stores selection panel
                    if ($scope.favStores.length) {
                        $('#favStores').collapse('hide');
                    }
                } else {
                    alert("Location unavailable");
                }
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

        $scope.storeRecentSearch = function(whisky) {
            if (!$scope.recentWhiskies.find(function (el) {
                return whisky.id === el.id;
            })) {
                $scope.recentWhiskies.unshift(whisky);
                $scope.recentWhiskies = $scope.recentWhiskies.slice(0, 10);
            }
            // store new preferences
            $scope.favWhiskyName = whisky.name;
            if ($scope.preferences) {
                $scope.preferences.favWhiskyName = $scope.favWhiskyName;
                $scope.preferences.recentWhiskyNames = $scope.recentWhiskies.map(function (w) {
                   return w.name;
                });
                $scope.savePrefs($scope.preferences);
            }
        };

        $scope.selectFavWhisky = function (w) {
            $scope.clearFavWhisky();
            $scope.favWhisky = w;
            // add to recent searches
            $('#recentViews').collapse('hide');   // always hide recent searches panel
            $scope.storeRecentSearch(w);
            // update product data
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
                $scope.getSelWhisky($scope.selectedWhisky).then(function () {
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
                $scope.fpStyle = {'width': width + 'px', 'height': width / 1.7 + 'px'};
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
            $scope.selFpStyle = {'width': width + 'px', 'height': width / 1.4 + 'px'};
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
            $scope.preferences = localstorage.getObject("spirited", {});
            if ($scope.preferences.favStoreNames && $scope.allStores) {
                $scope.allStores.forEach(function (s) {
                    if ($scope.preferences.favStoreNames.indexOf(s.name) >= 0) {
                        $scope.favStores.push(s);   // select
                        //$scope.toggleStoreSelection(s);
                    }
                });
                $scope.onSelectedStoresChanged();   // update all post-store selection
            }

            let promises = [];
            if($scope.preferences.recentWhiskyNames) {
                $scope.preferences.recentWhiskyNames.forEach(function (name) {
                    promises.push($api.findWhiskyByName(name).then(function (data) {
                        if (data.data) {
                            $scope.recentWhiskies.push(data.data);
                        }
                    }));
                });
            }
            $q.all(promises)
                .then(function () {
                    if ($scope.preferences.favWhiskyName) {
                        $api.findWhiskyByName($scope.preferences.favWhiskyName).then(function (data) {
                            if (data.data) {
                                $scope.selectFavWhisky(data.data);
                                //$scope.favWhisky = data.data;
                            }
                        });
                    }
                    else{
                        let deferred = $q.defer();
                        deferred.resolve();
                        return deferred.promise;
                    }
                });
        };

        $scope.clearPrefs = function () {
            $scope.clearFavWhisky();
            $scope.recentWhiskies = [];
            $scope.favWhiskyName = undefined;
            $scope.favStores = [];
            $scope.preferences = {};
            $scope.savePrefs($scope.preferences);
        };

        //// API ////

        $scope.getCacheStatus = function () {
            return $api.getCacheStatus().then(function (data) {
                $scope.log(undefined);
                $scope.cacheStatus = data.data;
            }).catch(function (err) {
                $scope.log(err);
            });
        };

        $scope.getAllStores = function () {
            return $api.getAllStores().then(function (data) {
                $scope.log(undefined);
                $scope.allStores = data.data;
            }).catch(function (err) {
                $scope.log(err);
            });
        };

        $scope.getAllWhiskies = function (name) {
            return $api.findWhiskiesLike(name).then(function (data) {
                $scope.log(undefined);
                $scope.allWhiskies = data.data;
            }).catch(function (err) {
                $scope.log(err);
            });
        };

        $scope.getFavWhisky = function (id) {
            return $api.getWhisky(id).then(function (data) {
                $scope.log(undefined);
                if ($scope.favWhisky && $scope.favWhisky.id === data.data.id) {
                    $scope.favWhisky = data.data;
                }
            }).catch(function (err) {
                $scope.log(err);
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
                    $scope.log(undefined);
                    if ($scope.selectedWhisky && $scope.selectedWhisky.id === data.data.id) {
                        $scope.selectedWhisky = data.data;
                    }
                }).catch(function (err) {
                    $scope.log(err);
                });
            }
        };

        $scope.getSimilarWhiskies = function (whisky) {
            $api.findSimilarWhiskies(whisky).then(function (data) {
                $scope.log(undefined);
                $scope.similarWhiskies = data.data.map(function (sw) {
                    sw.available = $scope.filterAvailability(sw.candidate).length > 0;
                    return sw;
                });
            }).catch(function (err) {
                $scope.log(err);
            });
        };

        $scope.findWhiskyByCode = function (code) {
            $api.findWhiskyByCode(code).then(function (data) {
                if (!data.data) {
                    alert('no such product "' + code + '" :-(');
                    throw 'no such product "' + code + '" :-(';
                }
                $scope.log(undefined);
                return $scope.selectFavWhisky(data.data);
            }).then(function () {
                $scope.showWhisky($scope.favWhisky);
            })
                .catch(function (err) {
                    $scope.log(err);
                });
        };

        //// initialization:
        try {
            $scope.busy = true;
            $scope.getCacheStatus()
                .then(function () {
                    return $scope.getAllStores();
                })
                .then(function () {
                    return $scope.loadPrefs();
                })
                .catch(function(err){
                    $scope.log(err);
                })
                .finally(function () {
                    $scope.busy = false;
                });
        }
        catch (err) {
            $scope.log(err);
            $scope.busy = false;
        }
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
