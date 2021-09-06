const homedir = require('os').homedir();
const path = require('path');

let paths = {
  templates: [
    'src/main/runtime/templates/*.html'
  ],
  runtime: ['src/main/runtime/**/*.js'],
  runtimeFolder: 'src/main/runtime',
  vendor: [
    'node_modules/angular/angular.min.js',
    'node_modules/angular-sanitize/angular-sanitize.min.js',
    'node_modules/angular-messages/angular-messages.min.js',
    'node_modules/angular-gettext/dist/angular-gettext.min.js',
    'node_modules/angular-cookies/angular-cookies.min.js'
  ],
  fonts: [
    'node_modules/bootstrap/dist/fonts/*.*'
  ],
  css: [
    'node_modules/bootstrap/dist/css/bootstrap.min.css',
    'src/main/runtime/css/**.css'
  ],
  widgets: ['src/main/resources/widgets/**/*.*'],
  widgetsPbJson: ['src/main/resources/widgets/**/pb*.json'],
  widgetsJson: ['src/main/resources/widgets/**/*.json'],
  widgetsHtml: ['src/main/resources/widgets/**/*.html'],
  karma: { configFile: __dirname + '../../src/test/javascript/karma.conf.js' },
  tests: ['src/test/**/*.spec.js'],

  dest: {
    vendors: 'target/classes/META-INF/resources/runtime/js',
    css: 'target/classes/META-INF/resources/runtime/css',
    fonts: 'target/classes/META-INF/resources/runtime/fonts',
    js: 'target/classes/META-INF/resources/runtime/js',
    json: 'target/classes/widgets',
  },
  dev:{
    widgets: homedir + path.sep + '.bonita/widgets',
  }
};

exports.paths = paths;
exports.javaArgs = '';

