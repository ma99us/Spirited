angular.module('phoneapi', [])
    .factory('phoneapi', ['$q', '$window', function ($q, $window) {
        return {
            useCamera: function (successFunc, errorFunc) {
                if(!cordova){
                    errorFunc('No Cordova');
                } else {
                    $window.cordova.plugins.diagnostic.isCameraAuthorized(
                        function (authorized) {
                            if (!authorized) {
                                $window.cordova.plugins.diagnostic.requestCameraAuthorization(
                                    function (status) {
                                        if (status == $window.cordova.plugins.diagnostic.permissionStatus.GRANTED) {
                                            successFunc();
                                        } else {
                                            errorFunc('Bad status: ' + status);
                                        }
                                    }, function (error) {
                                        // fail
                                        errorFunc('Auth failed: ' + error);
                                    }, false
                                );
                            } else {
                                successFunc();
                            }
                        }, function (error) {
                            errorFunc('Error occurred: ' + error);
                        }, false
                    );
                }
            },

            useLocation: function (successFunc, errorFunc) {
                if(!cordova){
                    errorFunc('No Cordova');
                } else {
                    $window.cordova.plugins.diagnostic.isLocationAuthorized(function (authorized) {
                        if (!authorized) {
                            $window.cordova.plugins.diagnostic.requestLocationAuthorization(function (status) {
                                if (status === $window.cordova.plugins.diagnostic.permissionStatus.GRANTED
                                    || status === $window.cordova.plugins.diagnostic.permissionStatus.GRANTED_WHEN_IN_USE) {
                                    successFunc();
                                }
                                else {
                                    errorFunc('Bad status: ' + status);
                                }
                            }, function (error) {
                                errorFunc('Auth failed: ' + error);
                            }/*, cordova.plugins.diagnostic.locationAuthorizationMode.ALWAYS*/);
                        } else {
                            successFunc();
                        }
                    }, function (error) {
                        errorFunc('Error occurred: ' + error);
                    });
                }
            }
        };
    }]);