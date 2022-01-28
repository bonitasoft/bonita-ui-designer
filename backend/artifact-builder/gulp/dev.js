const widgets = require('./subTasks/widgets');
const runtime = require('./subTasks/runtime');
const {series, src, dest, watch} = require('gulp');
const shell = require('gulp-shell');
const config = require('./config');
const fs = require('fs');
const { buildWidget } = require('widget-builder/src/index.js');

/**
 * Watch task.
 */
function devWatch(done) {
  watch(config.paths.css, runtime.runtimeCss);
  watch(config.paths.runtime, runtime.runtimeJs);
  watch(config.paths.widgets, devWidgets);
  watch(config.paths.templates, devHtml);
  done();
}

/**
 * html task, just updating the templates used by server
 */
function devHtml() {
  return src(config.paths.templates).pipe(dest('target/classes/templates/'));
}

/**
 * Task to inline json and build a widgets repository for development.
 */
function devWidgets(done) {
  // only copy widgets if the repository exist
  if (fs.existsSync(config.paths.dev.widgets)) {
    // The application create and build the pb widgets the first time.
    src(config.paths.widgetsPbJson)
      .pipe(buildWidget())
      .pipe(dest(config.paths.dev.widgets));
  }
  done();
}

/**
 * dev task
 * Watch js and html files and launch spring boot, without automatic reloading
 */
exports.runServer = series(widgets.copy, runtime.copy, devHtml, devWatch, shell.task('mvn spring-boot:run ' + config.javaArgs));



