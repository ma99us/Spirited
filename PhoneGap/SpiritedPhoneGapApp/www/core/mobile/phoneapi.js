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
                const diagnostic = getCordovaDiagnosticPlugin();
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
            },

            showToast: function (message, type) {
                let duration = "short";
                let backgroundColor = "#333333";
                let textColor = "#FFFFFF";
                if(type === 'error'){
                    duration = "long";
                    backgroundColor = "#DE3B3B";
                }
                else if(type === 'warning'){
                    duration = "short";
                    backgroundColor = "#FF9B21";
                }
                else if(type === 'success'){
                    duration = "short";
                    backgroundColor = "#6AA482";
                }
                try {
                    window.plugins.toast.showWithOptions({
                        message: message,
                        duration: duration, // "short" - 2000 ms
                        position: "center",
                        styling: {
                            opacity: 0.8, // 0.0 (transparent) to 1.0 (opaque). Default 0.8
                            backgroundColor: backgroundColor, // make sure you use #RRGGBB. Default #333333
                            textColor: '#FFFFFF', // Ditto. Default #FFFFFF
                            textSize: 14, // Default is approx. 13.
                            cornerRadius: 16, // minimum is 0 (square). iOS default 20, Android default 100
                            horizontalPadding: 20, // iOS default 16, Android default 50
                            verticalPadding: 16 // iOS default 12, Android default 30
                        }
                    }, function (result) {
                        alert("Toast success: " + result.event + "; " + result.message); // #TEST
                    }, function (err) {
                        //alert("Toast failed: " + err);
                        let t = type ? (type.toUpperCase() + ": ") : "";
                        alert(t + message);
                    });
                }
                catch (e) {
                    //alert("Toast error: " + e);
                    let t = type ? (type.toUpperCase() + ": ") : "";
                    alert(t + message);
                }
            }
        };
    }]);