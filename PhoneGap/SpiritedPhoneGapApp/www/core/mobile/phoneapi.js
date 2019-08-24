angular.module('phoneapi', [])
    .factory('phoneapi', ['$q', '$window', function ($q, $window) {
        const cordova = $window.cordova;
        const plugins = cordova ? cordova.plugins : null;
        const diagnostic = plugins ? plugins.diagnostic : null;

        return {
            useCamera: function (successFunc, errorFunc) {
                if(!diagnostic){
                    errorFunc('No Cordova');
                } else {
                    diagnostic.isCameraAuthorized(
                        function (authorized) {
                            if (!authorized) {
                                diagnostic.requestCameraAuthorization(
                                    function (status) {
                                        if (status === diagnostic.permissionStatus.GRANTED) {
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
                if(!diagnostic){
                    errorFunc('No Cordova');
                } else {
                    diagnostic.isLocationAuthorized(function (authorized) {
                        if (!authorized) {
                            diagnostic.requestLocationAuthorization(function (status) {
                                if (status === diagnostic.permissionStatus.GRANTED || status === diagnostic.permissionStatus.GRANTED_WHEN_IN_USE) {
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