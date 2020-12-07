const {widgets} = require('./subTasks/widgets');
const {runtime, runtimeCss, runtimeJs} = require('./subTasks/runtime');
const {task, series, src, dest, watch} = require('gulp');
const shell = require('gulp-shell');
const {config} = require('./config');
const fs = require('fs');
const { buildWidget } = require('widget-builder/src/index.js');

function dev(){
  return series(widgets,runtime,devHtml, devWatch, shell.task('mvn jetty:run -Djetty.reload=manual ' + config.javaArgs));
}
/**
 * dev task
 * Watch js and html files and launch jetty, without automatic reloading
 */
task('dev',dev());

exports.dev = dev;

/**
 * Watch task.
 */
function devWatch(done) {
  watch(config.paths.css, runtimeCss);
  watch(config.paths.runtime, runtimeJs);
  watch(config.paths.widgets, devWidgets);
  watch(config.paths.widgetsWc, devWidgetsWc);
  watch(config.paths.templates, devHtml);
  done();
}

/**
 * html task, just updating the templates used by jetty
 */
function devHtml() {
  return src(config.paths.templates).pipe(dest('target/classes/templates/'));
}

/**
 * Task to inline json and build a widgets repository for development.
 */
function devWidgets(done) {
  // only copy widgets if the repository exist to let
  // the application create and build them the first time.
  if (fs.existsSync(config.paths.dev.widgets)) {
    src(config.paths.widgetsJson)
      .pipe(buildWidget())
      .pipe(dest(config.paths.dev.widgets));
  }
  done();
}

/**
 * Task to inline json and build a widgets repository for development.
 */
function devWidgetsWc(done) {
  // only copy widgets if the repository exist to let
  // the application create and build them the first time.
  if (fs.existsSync(config.paths.dev.widgetsWc)) {
    src(config.paths.widgetsWcJson)
      .pipe(dest(config.paths.dev.widgetsWc));
  }
  done();
}

