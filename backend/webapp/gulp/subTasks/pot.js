const { dest, src, parallel } = require('gulp');
const config = require('../config');
const gettextWidget = require('./gettext-widget.js');
const gettext = require('gulp-angular-gettext');
const concat = require('gulp-concat');
const replace = require('gulp-replace');

/**
 * Extract translation keys from Json widget files
 */
function potJson(){
  return src(config.paths.widgetsJson)
    .pipe(gettextWidget.prepare())
    .pipe(concat('widgets.json', {newLine: ','}))
    .pipe(gettextWidget.extract())
    .pipe(dest('target/po'));
}

/**
 * Extract translation keys from HTML files
 * remove empty msgid key header as it fails in crowndin/upload.sh when using msguniq
 */
function potHtml() {
  return src(config.paths.widgetsHtml)
    .pipe(gettext.extract('widgets.html.pot', {}))
    .pipe(replace(/^[^#]*/, ''))
    .pipe(dest('target/po'));
}

exports.copy = parallel(potJson,potHtml);
