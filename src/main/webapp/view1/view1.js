'use strict';

angular.module('myApp.view1', ['ngRoute', 'localstorage', 'chart.js'])

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
          beginAtZero: true,
        }
      }]
    });
  }])
  .controller('View1Ctrl', ['$scope', '$q', '$filter', 'localstorage', '$api', 'geolocation', '$routeParams', function ($scope, $q, $filter, localstorage, $api, geolocation, $routeParams) {
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
    $scope.latestWhiskies = {
      whiskies: [],
      resultsPerPage: 10,
      pageNumber: 1,
      sortBy: 'name',
      totalResults: null,
      filterAvailability: true,
      format: 'long',
      last: 'week'
    };
    $scope.discountedWhiskies = {
      whiskies: [],
      resultsPerPage: 10,
      pageNumber: 1,
      sortBy: 'name',
      totalResults: null,
      filterAvailability: true,
      format: 'long',
      onSale: true
    };
    $scope.dispQuantity = 10;
    $scope.recentWhiskies = [];
    $scope.paramProduct = $routeParams.prod;
    $scope.paramFooter = $routeParams.footer;
    $scope.activeTab = 'byname-tab';

    $scope.log = function (msg) {
      if (msg && typeof msg === 'object') {
        $scope.message = JSON.stringify(msg);
      } else {
        $scope.message = msg
      }
    };

    $scope.copyToClipboard = function (str) {
      localstorage.copyToClipboard(str);
      $('a[data-toggle="tooltip"]').tooltip({
        animated: 'fade',
        placement: 'bottom',
        trigger: 'click'
      });
    };

    $scope.scrollToDonate = function () {
      $('#infoDiv').collapse('show');
      //var height = $('#dono').offset().top;
      let height = $(document).height();
      $("html, body").animate({scrollTop: height}, "slow");
    };

    $scope.getSpiritedWhiskyName = function (whisky) {
      let res = whisky.name;
      res += whisky.unitVolumeMl ? (' - ' + whisky.unitVolumeMl + 'ml') : '';
      res += whisky.unitPrice ? (' - ' + $filter('currency')(whisky.unitPrice)) : '';
      return res;
    };

    $scope.getSpiritedUrl = function (whisky) {
      // $location.absUrl().split('?')[0]  // for site should work
      return 'http://spiritsearch.ca/spirited/#/view1?prod=' + encodeURI(whisky.name);
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
      //$('#browseView').collapse('show');
      $scope.getWhiskies(null, $scope.favWhiskyType, $scope.browseWhiskies).finally(function () {
        $scope.busyC = false;
      });
    };

    $scope.onLatestChange = function (newVal) {
      if (newVal) {
        $scope.busyC = true;
        $scope.clearLatestWhiskies();
        $('#latestView').collapse('show');
        $scope.latestWhiskies.last = newVal;
        $scope.getWhiskies(null, null, $scope.latestWhiskies).finally(function () {
          $scope.busyC = false;
        });
      }
    };

    $scope.onLatestSortBy = function (sortBy) {
      $scope.busyC = true;
      $scope.clearLatestWhiskies();
      $scope.latestWhiskies.sortBy = sortBy;
      //$('#browseView').collapse('show');
      $scope.getWhiskies(null, null, $scope.latestWhiskies).finally(function () {
        $scope.busyC = false;
      });
    };

    $scope.onDiscountedChange = function () {
      $scope.busyC = true;
      $scope.clearDiscountedWhiskies();
      $('#discountedView').collapse('show');
      $scope.discountedWhiskies.onSale = true;
      $scope.getWhiskies(null, null, $scope.discountedWhiskies).finally(function () {
        $scope.busyC = false;
      });
    };

    $scope.captureUserLocation = function () {
      $scope.log();
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
          alert("Location unle");
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

    $scope.clearAllWhiskies = function () {
      $scope.allWhiskies.whiskies = [];
      $scope.allWhiskies.resultsPerPage = 10;
      $scope.allWhiskies.pageNumber = 1;
      $scope.allWhiskies.totalResults = null;
    };

    $scope.clearBrowseWhiskies = function () {
      $scope.browseWhiskies.whiskies = [];
      $scope.browseWhiskies.resultsPerPage = 10;
      $scope.browseWhiskies.pageNumber = 1;
      $scope.browseWhiskies.totalResults = null;
    };

    $scope.clearLatestWhiskies = function () {
      $scope.latestWhiskies.whiskies = [];
      $scope.latestWhiskies.resultsPerPage = 10;
      $scope.latestWhiskies.pageNumber = 1;
      $scope.latestWhiskies.totalResults = null;
    };

    $scope.clearDiscountedWhiskies = function () {
      $scope.discountedWhiskies.whiskies = [];
      $scope.discountedWhiskies.resultsPerPage = 10;
      $scope.discountedWhiskies.pageNumber = 1;
      $scope.discountedWhiskies.totalResults = null;
    };

    $scope.clearFavWhisky = function () {
      $scope.favWhisky = null;
      $scope.fpData = null;
      $scope.similarWhiskies = null;
      $scope.selectedWhisky = null;
      $scope.selectedWhiskySpiritedUrl = null;
      $scope.selectedWhiskySpiritedName = null;
      $scope.selectedAvailableQty = null;
      $scope.selFpData = null;
      $scope.dispQuantity = 10;
    };

    $scope.storeRecentSearch = function (whisky) {
      if (!$scope.recentWhiskies.find(function (el) {
        return whisky.id === el.id;
      })) {
        $scope.recentWhiskies.unshift(whisky);
        $scope.recentWhiskies = $scope.recentWhiskies.slice(0, 10);
      }
      // store new preferences
      //$scope.favWhiskyName = whisky.name;
      if ($scope.preferences) {
        $scope.preferences.favWhiskyName = whisky.name;
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
      $scope.selectedWhiskySpiritedUrl = $scope.getSpiritedUrl(w);
      $scope.selectedWhiskySpiritedName = $scope.getSpiritedWhiskyName(w);
      $('#selectedWhiskyModalCenter').modal('show');
      $('#selectedWhiskyModalCenter').on('shown.bs.modal', function () {
        $(".sharethis-inline-share-buttons").show().children().show();
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

    $scope.latestMoreWhiskies = function () {
      $scope.latestWhiskies.pageNumber += 1;
      $scope.busyC = true;
      $scope.getWhiskies(null, null, $scope.latestWhiskies).finally(function () {
        $scope.busyC = false;
      });
    };

    $scope.discountedMoreWhiskies = function () {
      $scope.discountedWhiskies.pageNumber += 1;
      $scope.busyC = true;
      $scope.getWhiskies(null, null, $scope.discountedWhiskies).finally(function () {
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
      if ($scope.preferences.recentWhiskyNames) {
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
          if (favProd && !$scope.paramFooter) {   // do not show any whisky if we are in 'footer' mode
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
      let stores = null;
      if (meta.filterAvailability && $scope.favStores && $scope.favStores.length > 0) {
        stores = $scope.favStores.map(function (s) {
          return s.id;
        });
      }
      return $api.getWhiskies(name, type, meta.last, stores, meta.onSale, meta.resultsPerPage, meta.pageNumber, meta.sortBy, meta.format)
        .then(function (data) {
          $scope.log();
          // if(meta.filterAvailability && data.data){
          //   data.data = data.data.filter(function (sw) {
          //     return $scope.filterAvailability(sw).length > 0;
          //   });
          // }
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
      if ($scope.activeTab === 'byname-tab') {
        $("#spiritNameCtrl").focus();
      }
      else if ($scope.activeTab === 'bytype-tab') {
        $("#spiritTypeCtrl").focus();
      }
      else if ($scope.activeTab === 'latest-tab') {
        $scope.onLatestChange('week');  //TODO: should not be hardcoded
      }
      else if ($scope.activeTab === 'discounted-tab') {
        $scope.onDiscountedChange();
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

        if ($scope.paramFooter) {
          $scope.scrollToDonate();
        }
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
        toShow: '@',
        large: '='
      },
      templateUrl: 'view1/whisky-row.html',
      controller: ['$scope', function ($scope) {
        $scope.$watch('selWhisky', function (newValue, oldValue) {
          $scope.isSelected = $scope.selWhisky && ($scope.whisky.id === $scope.selWhisky.id);
          if ($scope.isSelected) {
            //$scope.rowStyle = {'color': 'white'};
          }
          else {
            $scope.rowStyle = {}
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
          if (val === 'name') {
            if ($scope.sortBy === 'name') {
              $scope.sortBy = '-name';
            }
            else {
              $scope.sortBy = 'name';
            }
          }
          else if (val === 'price') {
            if ($scope.sortBy === 'price') {
              $scope.sortBy = '-price';
            }
            else {
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