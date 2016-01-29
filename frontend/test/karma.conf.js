module.exports = function(config) {

  'use strict';

  config.set({
    // base path, that will be used to resolve files and exclude
    basePath: '..',

    // frameworks to use
    frameworks: ['jasmine', 'commonjs'],

    // list of files / patterns to load in the browser
    files: [
      'node_modules/babel-polyfill/dist/polyfill.min.js',
      'bower_components/jquery/dist/jquery.js',
      'bower_components/ace-builds/src-noconflict/ace.js',
      'bower_components/ace-builds/src-noconflict/ext-language_tools.js',
      'bower_components/angular/angular.js',
      'bower_components/angular-ui-router/release/angular-ui-router.js',
      'bower_components/angular-sanitize/angular-sanitize.js',
      'bower_components/angular-messages/angular-messages.min.js',
      'bower_components/angular-recursion/angular-recursion.js',
      'bower_components/angular-bootstrap/ui-bootstrap-tpls.js',
      'bower_components/angular-ui-ace/ui-ace.js',
      'bower_components/angular-ui-validate/dist/validate.min.js',
      'bower_components/ngUpload/ng-upload.min.js',
      'bower_components/bonita-js-components/dist/bonita-lib-tpl.js',
      'bower_components/angular-mocks/angular-mocks.js',
      'bower_components/angular-gettext/dist/angular-gettext.min.js',
      'bower_components/moment/min/moment.min.js',
      'bower_components/angular-moment/angular-moment.min.js',
      'bower_components/keymaster/keymaster.js',
      'bower_components/angular-dynamic-locale/tmhDynamicLocale.min.js',
      'bower_components/identicon.js/pnglib.js',
      'bower_components/identicon.js/identicon.js',
      'bower_components/jsSHA/src/sha1.js',
      'bower_components/angular-sha/src/angular-sha.js',

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
    port: 9876,

    // enable / disable colors in the output (reporters and logs)
    colors: true,

    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,

    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera
    // - Safari (only Mac)
    // - PhantomJS
    // - IE (only Windows)
    browsers: ['PhantomJS'],

    // If browser does not capture in given timeout [ms], kill it
    captureTimeout: 60000,

    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: false
  });
};
