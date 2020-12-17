/* globals process */
process.env.CHROME_BIN = require('puppeteer').executablePath();
module.exports = function(config) {

  'use strict';

  config.set({
    plugin:[
      require('karma-chrome-launcher'),
    ],
    // base path, that will be used to resolve files and exclude
    basePath: '..',

    // frameworks to use
    frameworks: ['jasmine', 'commonjs'],

    // list of files / patterns to load in the browser
    files: [
      'node_modules/babel-polyfill/dist/polyfill.min.js',
      'node_modules/jquery/dist/jquery.js',
      'node_modules/ace-builds/src-noconflict/ace.js',
      'node_modules/ace-builds/src-noconflict/ext-language_tools.js',
      'node_modules/angular/angular.js',
      'node_modules/angular-ui-router/release/angular-ui-router.js',
      'node_modules/angular-sanitize/angular-sanitize.js',
      'node_modules/angular-recursion/angular-recursion.js',
      'node_modules/angular-bootstrap/ui-bootstrap-tpls.js',
      'node_modules/angular-ui-ace/ui-ace.js',
      'node_modules/angular-ui-validate/dist/validate.min.js',
      'node_modules/ngUpload/ng-upload.min.js',
      'node_modules/angular-mocks/angular-mocks.js',
      'node_modules/angular-gettext/dist/angular-gettext.min.js',
      'node_modules/moment/min/moment.min.js',
      'node_modules/angular-moment/angular-moment.min.js',
      'node_modules/bonita-js-components/dist/bonita-lib-tpl.js',
      'node_modules/mousetrap/mousetrap.min.js',
      'node_modules/mousetrap/plugins/global-bind/mousetrap-global-bind.min.js',
      'node_modules/angular-dynamic-locale/tmhDynamicLocale.min.js',
      'node_modules/identicon.js/pnglib.js',
      'node_modules/identicon.js/identicon.js',
      'node_modules/jsSHA/src/sha1.js',
      'node_modules/angular-sha/src/angular-sha.js',
      'node_modules/angular-cookies/angular-cookies.min.js',
      'node_modules/angular-switcher/dist/angular-switcher.min.js',
      'node_modules/ngstorage/ngStorage.min.js',
      'node_modules/angular-resizable/angular-resizable.min.js',
      'node_modules/angular-animate/angular-animate.min.js',
      'node_modules/angular-filter/dist/angular-filter.min.js',

      'app/js/**/*.module.js',
      'app/js/**/*.js',
      'app/js/**/*.html',

      'test/unit/utils/*.js',
      'test/unit/**/*.js'
    ],

    // list of files to exclude
    exclude: [
    ],

    babelPreprocessor: {
      options: {
        presets: ['bonita'],
        retainLines: true
      }
    },

    // test results reporter to use
    // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
    reporters: ['progress', 'junit', 'coverage'],

    preprocessors: {
      // source files, that you wanna generate coverage for
      // do not include tests or libraries
      // (these files will be instrumented by Istanbul)
      'app/js/**/*.js': ['babel', 'coverage'],
      'test/unit/**/*.js': ['babel', 'commonjs'],
      'app/js/**/*.html': ['ng-html2js']
    },

    ngHtml2JsPreprocessor: {
      stripPrefix: 'app/',
      moduleName: 'bonitasoft.designer.templates'
    },

    coverageReporter: {
      type: 'lcov',
      dir: 'build/reports/coverage/'
    },

    junitReporter: {
      outputFile: 'build/reports/unit-tests/test-results.xml',
      suite: 'JavaScript unit tests'
    },

    // web server port
    port: process.env.KARMA_PORT || 9876,

    // enable / disable colors in the output (reporters and logs)
    colors: true,

    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,

    browsers: ['ChromeWithoutSecurity'],
    // you can define custom flags
    customLaunchers: {
      ChromeWithoutSecurity: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox']
      }
    },

    // If browser does not capture in given timeout [ms], kill it
    captureTimeout: 60000,
    browserDisconnectTimeout: 5000,
    browserDisconnectTolerance: 5,
    browserNoActivityTimeout: 40000,

    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: false
  });
};
