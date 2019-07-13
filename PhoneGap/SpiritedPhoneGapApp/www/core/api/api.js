angular.module('api', [])
    .factory('$api', ['$http', '$q', function ($http, $q) {
        //const hostAdr = 'http://192.168.2.19/spirited/';
        const hostAdr = 'https://3lspo5qztd.execute-api.us-west-2.amazonaws.com/prod/';
        let busy = false;
        let validateResponse = function (deferred, response) {
            if (response && response.data && response.data.status != 200) {
                let message = 'Server error: (' + response.data.status + ') ' + response.data.type + ': ' + response.data.message;
                deferred.reject(message);
            }
            else if (response && response.status != 200) {
                let message = 'Http error: (' + response.status + ') ' + response.statusText;
                deferred.reject(message);
            }
            else {
                deferred.resolve(response.data);
            }
        };
        return {

            auth: function () {
                var deferred = $q.defer();
                $http.get(hostAdr + 'api/cache/auth', {params: {}}).then(function (response) {
                    validateResponse(deferred, response);
                }, function(err) {
                    validateResponse(deferred, err);
                });
                return deferred.promise;
            },

            getAllStores: function () {
                var deferred = $q.defer();
                $http.get(hostAdr + 'api/store', {params: {}}).then(function (response) {
                    validateResponse(deferred, response);
                }, function(err) {
                    validateResponse(deferred, err);
                });
                return deferred.promise;
            },

            getCacheStatus: function () {
                var deferred = $q.defer();
                $http.get(hostAdr + 'api/cache/status', {params: {}}).then(function (response) {
                    validateResponse(deferred, response);
                }, function(err) {
                    validateResponse(deferred, err);
                });
                return deferred.promise;
            },

            rebuildAllCache: function () {
                //$http.defaults.headers.common['Authorization'] = 'Basic <username:pw>'; where username:password has to be Base64 encoded.
                var deferred = $q.defer();
                busy = true;
                $http.get(hostAdr + 'api/cache/rebuild', {params: {full: true}}).then(function (response) {
                    busy = false;
                    validateResponse(deferred, response);
                }, function(err) {
                    validateResponse(deferred, err);
                });
                return deferred.promise;
            },

            getSimilarWhiskies: function (whisky) {
                var deferred = $q.defer();
                busy = true;
                $http.get(hostAdr + 'api/whisky/similar/' + whisky.id, {params: {}}).then(function (response) {
                    busy = false;
                    validateResponse(deferred, response);
                }, function(err) {
                    validateResponse(deferred, err);
                });
                return deferred.promise;
            },

            findWhiskiesLike: function (name) {
                var deferred = $q.defer();
                busy = true;
                $http.get(hostAdr + 'api/whisky/like/' + name, {params: {}}).then(function (response) {
                    busy = false;
                    validateResponse(deferred, response);
                }, function(err) {
                    validateResponse(deferred, err);
                });
                return deferred.promise;
            },

            getAllWhiskies: function (resultsPerPage, pageNumber, sortBy) {
                var deferred = $q.defer();
                busy = true;
                let params = {
                    resultsPerPage: resultsPerPage,
                    pageNumber: pageNumber,
                    sortBy: sortBy
                };
                $http.get(hostAdr + 'api/whisky', {params: params}).then(function (response) {
                    busy = false;
                    validateResponse(deferred, response);
                }, function(err) {
                    validateResponse(deferred, err);
                });
                return deferred.promise;
            },

            getWhisky: function (id) {
                var deferred = $q.defer();
                busy = true;
                $http.get(hostAdr + 'api/whisky/' + id, {params: {}}).then(function (response) {
                    busy = false;
                    validateResponse(deferred, response);
                }, function(err) {
                    validateResponse(deferred, err);
                });
                return deferred.promise;
            },

            findWhisky: function (code) {
                var deferred = $q.defer();
                busy = true;
                $http.get(hostAdr + 'api/whisky/find/' + code, {params: {}}).then(function (response) {
                    busy = false;
                    validateResponse(deferred, response);
                }, function(err) {
                    validateResponse(deferred, err);
                });
                return deferred.promise;
            },

            addWhisky: function (w) {
                var deferred = $q.defer();
                $http.put(hostAdr + 'api/whisky', w).then(function (response) {
                    validateResponse(deferred, response);
                }, function(err) {
                    validateResponse(deferred, err);
                });
                return deferred.promise;
            },

            updateWhisky: function (w) {
                var deferred = $q.defer();
                $http.post(hostAdr + 'api/whisky/' + w.id, w).then(function (response) {
                    validateResponse(deferred, response);
                }, function(err) {
                    validateResponse(deferred, err);
                });
                return deferred.promise;
            },

            deleteWhisky: function (w) {
                var deferred = $q.defer();
                $http.delete(hostAdr + 'api/whisky/' + w.id, {params: {}}).then(function (response) {
                    validateResponse(deferred, response);
                }, function(err) {
                    validateResponse(deferred, err);
                });
                return deferred.promise;
            },
        }
    }]);