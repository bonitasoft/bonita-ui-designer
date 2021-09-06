const gulp = require('gulp');
const config = require('./config.js');
const del = require('del');
const rename = require("gulp-rename");
const path = require("path");

let paths = config.paths;

function locales() {
  return gulp.src(paths.locales)
    .pipe(gulp.dest(paths.dist + '/locales'));
}

function font() {
  return gulp.src(paths.assets.fonts)
    .pipe(gulp.dest(paths.dist + '/fonts'));
}

function ace() {
  return gulp.src(paths.assets.ace)
    .pipe(gulp.dest(paths.dist + '/js'));
}

function images() {
  return gulp.src(paths.assets.images)
    .pipe(gulp.dest(paths.dist + '/img'));
}

function licences() {
  return gulp.src(paths.assets.licences)
    .pipe(gulp.dest(paths.dist));
}

function favicon() {
  return gulp.src(paths.assets.favicon)
    .pipe(gulp.dest(paths.dist));
}

function toCamelCase(str) {
  // e.g. uid-input -> uidInput
  return str.replace(/-([a-z])/g, (g) => {
    return g[1].toUpperCase()
  });
}

function stdWidgetsUid() {
  // Copy Standard widgets (from npm install) to backend resources, with required directories,
  // so that they are included in the jar file (and then copied in the UID workspace at start time).
  // Target dir structure should be copied like:
  //  (From npm)                         (Target)
  //  uid-input                         // uidInput
  //    assets                          //   assets
  //      uidInput.json                 //     js
  //      uidInput.tpl.html             //       uid-input.es5.min.js
  //      uidInput.tpl.runtime.html     //       uidInput.tpl.runtime.html
  //    lib                             //   uidInput.json
  //      uid-input.es5.min.js          //   uidInput.tpl.html
  gulp.src(paths.assets.stdWidgetsUidJson)
    .pipe(rename(function (file) {
      // this removes the last parent directory of the relative file path
      file.dirname = toCamelCase(path.dirname(file.dirname));
    }))
    .pipe(gulp.dest(paths.assets.resourceWidgets));
  gulp.src(paths.assets.stdWidgetsUidTmpl)
    .pipe(rename(function (file) {
      file.dirname = toCamelCase(path.dirname(file.dirname));
    }))
    .pipe(gulp.dest(paths.assets.resourceWidgets));
  gulp.src(paths.assets.stdWidgetsUidRuntimeTmpl)
    .pipe(rename(function (file) {
      file.dirname = toCamelCase(file.dirname +'/js');
    }))
    .pipe(gulp.dest(paths.assets.resourceWidgets));

  return gulp.src(paths.assets.stdWidgetsUidLib)
    .pipe(rename(function (file) {
      file.dirname = toCamelCase(path.dirname(file.dirname) +'/assets/js');
    }))
    .pipe(gulp.dest(paths.assets.resourceWidgets));
}

exports.copy = gulp.series(font, ace, images, licences, favicon, locales, stdWidgetsUid);
