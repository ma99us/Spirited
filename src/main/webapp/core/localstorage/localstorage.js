angular.module('localstorage', [])
    .factory('localstorage', ['$window', function ($window) {
        return {
            set: function (key, value) {
                $window.localStorage[key] = value;
            },
            get: function (key, defaultValue) {
                return $window.localStorage[key] || defaultValue;
            },
            setObject: function (key, value) {
                $window.localStorage[key] = JSON.stringify(value);
            },
            getObject: function (key, defaultValue) {
                try {
                    return JSON.parse($window.localStorage[key]);
                }
                catch (err) {
                    return defaultValue;
                }
            },

            copyToClipboard: function (str) {
                const el = document.createElement('input');
                el.value = str;
                document.body.appendChild(el);
                el.select();
                document.execCommand('copy');
                document.body.removeChild(el);
            }
        }
    }]);