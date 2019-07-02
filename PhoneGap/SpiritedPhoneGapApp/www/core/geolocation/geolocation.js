angular.module('geolocation', [])
    .factory('geolocation', ['$q', '$window', '$http', function ($q, $window, $http) {

        function getCurrentPosition() {
            var deferred = $q.defer();
            if (!$window.navigator.geolocation) {
                //deferred.reject('Geolocation not supported.');
                return tryIPinfoGeolocation();
            } else {
                $window.navigator.geolocation.getCurrentPosition(
                    function (position) {
                        deferred.resolve(position);
                    },
                    function (err) {
                        if (err.code === 1) {
                            return tryIPinfoGeolocation().then(function(geo){
                                deferred.resolve(geo);
                            });
                        } else {
                            deferred.reject(err.message || err);
                        }
                    });
            }
            return deferred.promise;
        }

        function tryGoogleApiGeolocation() {
            // $http.post("https://www.googleapis.com/geolocation/v1/geolocate?key=AIzaSyDCa1LUe1vOczX1hO_iGYgyo8p_jYuGOPU", function(success) {
            //     apiGeolocationSuccess({coords: {latitude: success.location.lat, longitude: success.location.lng}});
            // })
            //     .fail(function (err) {
            //         alert("API Geolocation error! \n\n" + err);
            //     });
        }

        function tryIPinfoGeolocation() {
            var deferred = $q.defer();
            $http.get('https://ipinfo.io/geo', {params: {token: '9134fd98c24203'}}).then(function (response) {
                if (response && response.status != 200) {
                    let message = 'Http error: (' + response.status + ') ' + response.statusText;
                    deferred.reject(message);
                }
                else {
                    var loc = response.data.loc.split(',');
                    var geo = {
                        coords: {
                            latitude: loc[0],
                            longitude: loc[1]
                        }
                    };
                    deferred.resolve(geo);
                }
            });
            return deferred.promise;
        }

        // geonames.org free API    //http://api.geonames.org/findNearbyPlaceNameJSON?formatted=true&lat=46.11&lng=-64.72&username=ma99us&style=short&radius=10&cities=cities1000
        function getNearbyCities(lat, lng, radius) {
            var deferred = $q.defer();
            let params = {
                lat: lat,
                lng: lng,
                formatted: true,
                username: 'ma99us',
                style: 'short',
                radius: radius || 10,
                cities: 'cities1000'
            };
            $http.get('https://secure.geonames.org/findNearbyPlaceNameJSON', {params: params}).then(function (response) {
                if (response && response.status != 200) {
                    let message = 'Http error: (' + response.status + ') ' + response.statusText;
                    deferred.reject(message);
                }
                else if (!response.data.geonames && response.data.status) {
                    let message = 'Server error: (' + response.data.status.value + ') ' + response.data.status.message;
                    deferred.reject(message);
                }
                else {
                    deferred.resolve(response.data.geonames);
                }
            });
            return deferred.promise;
        }

        return {
            getCurrentPosition: getCurrentPosition,
            getNearbyCities: getNearbyCities
        };
    }]);