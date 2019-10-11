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

            copyToClipboard: function (text_to_share) {
                // create temp element
                var copyElement = document.createElement("span");
                copyElement.appendChild(document.createTextNode(text_to_share));
                copyElement.id = 'tempCopyToClipboard';
                angular.element(document.body.append(copyElement));

                // select the text
                var range = document.createRange();
                range.selectNode(copyElement);
                window.getSelection().removeAllRanges();
                window.getSelection().addRange(range);

                // copy & cleanup
                document.execCommand('copy');
                window.getSelection().removeAllRanges();
                copyElement.remove();
            }
        }
    }]);