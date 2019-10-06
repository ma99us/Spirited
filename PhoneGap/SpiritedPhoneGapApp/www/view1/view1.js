'use strict';

angular.module('myApp.view1', ['ngRoute', 'localstorage', 'chart.js', 'phoneapi'])

    .config(['$routeProvider', 'ChartJsProvider', function ($routeProvider, ChartJsProvider) {
        $routeProvider.when('/view1', {
            templateUrl: 'view1/view1.html',
            controller: 'View1Ctrl'
        });

        ChartJsProvider.setOptions({
            chartColors: ['#13b7c6', '#0c818e', '#1be0ea'],
            showTooltips: true,
            responsive: false
        });
        ChartJsProvider.setOptions('scales', {
            xAxes: [{
                ticks: {
                    display: true,
                    autoSkip: false
                },
                gridLines: {
                    display: false,
                }
            }],
            yAxes: [{
                display: false,
                ticks: {
                    max: 100,
                    beginAtZero:true,
                }
            }]
        });
    }])
    .controller('View1Ctrl', ['$scope', '$q', 'localstorage', '$api', 'geolocation', '$routeParams', 'phoneapi', function ($scope, $q, localstorage, $api, geolocation, $routeParams, phoneapi) {
        $scope.favStores = [];
        $scope.allStores = [];
        $scope.allWhiskies = {
            whiskies: [],
            resultsPerPage: 10,
            pageNumber: 1,
            sortBy: 'name',
            totalResults: null
        };
        $scope.browseWhiskies = {
            whiskies: [],
            resultsPerPage: 10,
            pageNumber: 1,
            sortBy: 'name',
            totalResults: null
        };
        $scope.dispQuantity = 10;
        $scope.recentWhiskies = [];
        $scope.paramProduct = $routeParams.prod;
        $scope.activeTab = 'byname-tab';

        $scope.log = function (msg) {
            if (msg && typeof msg === 'object') {
                $scope.message = JSON.stringify(msg);
            } else {
                $scope.message = msg
            }
        };

        $scope.isDeviceReady = phoneapi.isDeviceReady;  // function
        $scope.$watch(function () {
            return phoneapi.isDeviceReady();
        }, function (newValue) {
            if (newValue && cordova) {
                cordova.getAppVersion.getVersionNumber(function (version) {
                    $scope.version = version;
                    log('App version: ' + $scope.version);
                });
            }
        });

        $scope.scanBarcode = function () {
            phoneapi.useCamera(function () {
                    cordova.plugins.barcodeScanner.scan(
                        function (result) {
                            if (result.cancelled) {
                                //alert("Scanning cancelled");
                            } else if (!result.text) {
                                alert("Scanning failed: no data");
                            } else {
                                $scope.findWhiskyByCode(result.text);
                            }
                        },
                        function (error) {
                            alert("Scanning failed: " + error);
                        },
                        {
                            // preferFrontCamera : true, // iOS and Android
                            // showFlipCameraButton : true, // iOS and Android
                            showTorchButton : true, // iOS and Android
                            torchOn: false, // Android, launch with the torch switched on (if available)
                            // saveHistory: true, // Android, save scan history (default false)
                            prompt : "Place a barcode inside the scan area", // Android
                            resultDisplayDuration: 0, // Android, display scanned text for X ms. 0 suppresses it entirely, default 1500
                            formats : "UPC_A,UPC_E,EAN_8,EAN_13,ITF", // default: all but PDF_417 and RSS_EXPANDED
                            orientation : "portrait", // Android only (portrait|landscape), default unset so it rotates with the device
                            disableAnimations : true, // iOS
                            disableSuccessBeep: false // iOS and Android
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

        $scope.onFavWhiskyNameChange = function (newVal, force) {
            if (newVal && (newVal.length >= 4 || force)) {
                $scope.busyC = true;
                $scope.clearFavWhisky();
                $scope.clearAllWhiskies();
                $('#searchView').collapse('show');
                $scope.getWhiskies(newVal, null, $scope.allWhiskies).finally(function () {
                    $scope.busyC = false;
                });
            }
        };

        $scope.onFavTypeChange = function (newVal) {
            if (newVal) {
                $scope.busyC = true;
                $scope.clearBrowseWhiskies();
                $('#browseView').collapse('show');
                $scope.getWhiskies(null, newVal, $scope.browseWhiskies).finally(function () {
                    $scope.busyC = false;
                });
            }
        };

        $scope.onSortBy = function (sortBy) {
            $scope.busyC = true;
            $scope.clearBrowseWhiskies();
            $scope.browseWhiskies.sortBy = sortBy;
            $('#browseView').collapse('show');
            $scope.getWhiskies(null, $scope.favWhiskyType, $scope.browseWhiskies).finally(function () {
                $scope.busyC = false;
            });
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

        $scope.clearAllWhiskies = function () {
            $scope.allWhiskies = {
                whiskies: [],
                resultsPerPage: 10,
                pageNumber: 1,
                sortBy: 'name',
                totalResults: null
            };
        };

        $scope.clearBrowseWhiskies = function () {
            $scope.browseWhiskies = {
                whiskies: [],
                resultsPerPage: 10,
                pageNumber: 1,
                sortBy: 'name',
                totalResults: null
            };
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
            $('#browseView').collapse('hide');
            $('#searchView').collapse('hide');
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

        $scope.showMoreWhiskies = function () {
            $scope.allWhiskies.pageNumber += 1;
            $scope.busyC = true;
            $scope.getWhiskies($scope.favWhiskyName, null, $scope.allWhiskies).finally(function () {
                $scope.busyC = false;
            });
        };

        $scope.browseMoreWhiskies = function () {
            $scope.browseWhiskies.pageNumber += 1;
            $scope.busyC = true;
            $scope.getWhiskies(null, $scope.favWhiskyType, $scope.browseWhiskies).finally(function () {
                $scope.busyC = false;
            });
        };

        $scope.savePrefs = function (prefs) {
            localstorage.setObject("spirited", prefs || $scope.preferences);
        };

        $scope.loadPrefs = function () {
            let deferred = $q.defer();
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
                    const favProd = $scope.paramProduct || $scope.preferences.favWhiskyName;
                    if (favProd) {
                        $api.findWhiskyByName(favProd).then(function (data) {
                            if (data.data) {
                                $scope.selectFavWhisky(data.data);
                            }
                        })
                            .then(function () {
                                if ($scope.paramProduct && $scope.favWhisky) {
                                    $scope.showWhisky($scope.favWhisky);
                                }
                            })
                            .finally(function () {
                                deferred.resolve();
                            });
                    }
                    else {
                        deferred.resolve();
                    }
                });
            return deferred.promise;
        };

        $scope.clearPrefs = function () {
            $scope.clearFavWhisky();
            $scope.recentWhiskies = [];
            $scope.favWhiskyName = null;
            $scope.favWhiskyType = null;
            $scope.favStores = [];
            $scope.preferences = {};
            $scope.savePrefs($scope.preferences);
        };

        //// API ////

        $scope.getCacheStatus = function () {
            return $api.getCacheStatus().then(function (data) {
                $scope.log();
                $scope.cacheStatus = data.data;
            }).catch(function (err) {
                $scope.log(err);
                throw err;
            });
        };

        $scope.getAllStores = function () {
            return $api.getAllStores().then(function (data) {
                $scope.log();
                $scope.allStores = data.data;
            }).catch(function (err) {
                $scope.log(err);
                throw err;
            });
        };

        $scope.getWhiskies = function (name, type, meta) {
            return $api.getWhiskies(name, type, meta.resultsPerPage, meta.pageNumber, meta.sortBy)
                .then(function (data) {
                    $scope.log();
                    meta.whiskies.push.apply(meta.whiskies, data.data);
                    if (data.metaData) {
                        meta.resultsPerPage = data.metaData.resultsPerPage;
                        meta.pageNumber = data.metaData.pageNumber;
                        meta.sortBy = data.metaData.sortBy;
                        meta.totalResults = data.metaData.totalResults;
                    }
            }).catch(function (err) {
                $scope.log(err);
                throw err;
            });
        };

        $scope.getFavWhisky = function (id) {
            return $api.getWhisky(id).then(function (data) {
                $scope.log();
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
                    $scope.log();
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
                $scope.log();
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
                $scope.log();
                return $scope.selectFavWhisky(data.data);
            }).then(function () {
                $scope.showWhisky($scope.favWhisky);
            })
                .catch(function (err) {
                    $scope.log(err);
                });
        };

        // active tab change listener
        $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
            $scope.activeTab = e.target.id; // newly activated tab
            if($scope.activeTab === 'byname-tab') {
                $("#spiritNameCtrl").focus();
            }
            else if($scope.activeTab === 'bytype-tab') {
                $("#spiritTypeCtrl").focus();
            }
        });

        //// initialization:
        $scope.busy = true;
        $scope.getCacheStatus()
            .then(function () {
                return $scope.getAllStores();
            })
            .then(function () {
                return $scope.loadPrefs();
            })
            .catch(function (err) {
                $scope.log(err);
            })
            .finally(function () {
                $scope.busy = false;
            });
    }])
    .directive('whiskyRow', function () {
        return {
            restrict: 'E',
            scope: {
                whisky: '<',
                onClick: '&?',
                selWhisky: '<?',
                available: '<',
                toShow: '@'
            },
            templateUrl: 'view1/whisky-row.html',
            controller: ['$scope', function ($scope) {
                $scope.$watch('selWhisky', function (newValue, oldValue) {
                    $scope.isSelected = $scope.selWhisky && ($scope.whisky.id === $scope.selWhisky.id);
                    if($scope.isSelected){
                        $scope.rowStyle={'background-color':'CornFlowerBlue', 'color':'black'};
                    }
                    else{
                        $scope.rowStyle={}
                    }
                }, true);
                $scope.toShow = $scope.toShow ? $scope.toShow.split(',') : ['volume', 'price', 'available'];
            }],
            link: function (scope, elem, attrs) {

            }
        };
    })
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
    .directive('sortBy', function () {
        return {
            restrict: 'E',
            scope: {
                sortBy: '=',
                onSort: '&?',
            },
            templateUrl: 'view1/sortby.html',
            controller: ['$scope', function ($scope) {
                $scope.$watch('sortBy', function (newValue, oldValue) {
                    $scope.init();
                }, true);

                $scope.onSortBy = function (val) {
                    if(val === 'name'){
                        if($scope.sortBy === 'name') {
                            $scope.sortBy = '-name';
                        }
                        else{
                            $scope.sortBy = 'name';
                        }
                    }
                    else if(val === 'price'){
                        if($scope.sortBy === 'price') {
                            $scope.sortBy = '-price';
                        }
                        else{
                            $scope.sortBy = 'price';
                        }
                    }
                    $scope.onSort({sortBy: $scope.sortBy});
                };

                $scope.init = function () {

                };
            }],
            link: function (scope, elem, attrs) {

            }
        };
    })
;
