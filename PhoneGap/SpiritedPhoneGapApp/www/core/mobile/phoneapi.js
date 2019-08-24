angular.module('phoneapi', [])
    .factory('phoneapi', ['$q', 'cordova', function ($q, $window, cordova) {
        return {
            useCamera: function (successFunc, errorFunc) {
                if(!cordova){
                    errorFunc('No Cordova');
                } else {
                    cordova.plugins.diagnostic.isCameraAuthorized(
                        function (authorized) {
                            if (!authorized) {
                                cordova.plugins.diagnostic.requestCameraAuthorization(
                                    function (status) {
                                        if (status == cordova.plugins.diagnostic.permissionStatus.GRANTED) {
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
                    cordova.plugins.diagnostic.isLocationAuthorized(function (authorized) {
                        if (!authorized) {
                            cordova.plugins.diagnostic.requestLocationAuthorization(function (status) {
                                if (status === cordova.plugins.diagnostic.permissionStatus.GRANTED
                                    || status === cordova.plugins.diagnostic.permissionStatus.GRANTED_WHEN_IN_USE) {
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