//jshint strict: false
module.exports = function(config) {
  config.set({

    basePath: './webapp',

    files: [
      'lib/angular/angular.js',
      'lib/angular-route/angular-route.js',
      'lib/angular-resource/angular-resource.js',
      '../node_modules/angular-mocks/angular-mocks.js',
      'core/**/*.js',
      'view*/**/*.js'
    ],

    autoWatch: true,

    frameworks: ['jasmine'],

    browsers: ['Chrome'],

    plugins: [
      'karma-chrome-launcher',
      'karma-firefox-launcher',
      'karma-jasmine'
    ]

  });
};
