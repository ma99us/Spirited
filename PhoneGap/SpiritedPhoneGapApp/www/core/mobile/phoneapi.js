angular.module('phoneapi', [])
    .factory('phoneapi', ['$q', '$window', function ($q, $window) {

        function isDeviceReady() {
            return $window.app.deviceReady || false;
        }

        function getCordovaPlugins() {
            const cordova = isDeviceReady() ? $window.cordova : null;
            return cordova ? cordova.plugins : null;
        }

        function getCordovaDiagnosticPlugin() {
            const plugins = getCordovaPlugins();
            return plugins ? plugins.diagnostic : null;
        }

        return {
            isDeviceReady: function () {
                return isDeviceReady();
            },

            useCamera: function (successFunc, errorFunc) {
                const diagnostic = getCordovaDiagnosticPlugin()
                //alert('deviceReady=' + $window.app.deviceReady + '; diagnostic=' + diagnostic + '; $window.cordova.plugins.diagnostic=' + $window.cordova.plugins.diagnostic);   //#TEST
                if (!diagnostic) {
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
                const diagnostic = getCordovaDiagnosticPlugin()
                //alert('deviceReady=' + $window.app.deviceReady + '; diagnostic=' + diagnostic + '; $window.cordova.plugins.diagnostic=' + $window.cordova.plugins.diagnostic);   //#TEST
                if (!diagnostic) {
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