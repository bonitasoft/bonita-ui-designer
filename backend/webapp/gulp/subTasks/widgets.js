const { src, dest, parallel, task } = require('gulp');
const { buildWidget } = require('widget-builder/src/index.js');
const config = require('../config');
const jsonSchema = require('widget-builder/src/index.js').jsonSchema;
//Check if we can replace/remove
const flatten = require('gulp-flatten');


/**
 * Task to inline add widgets to the webapp for production and inline templates in json file
 */
function widgetsJs(){
  return src(config.paths.widgetsJson).pipe(buildWidget()).pipe(dest(config.paths.dest.json));
}

/**
 * Task to move widgetWc in webapp for production
 */
function widgetsWc() {
  return src(config.paths.widgetsWcJson).pipe(dest(config.paths.dest.jsonWc))
}

/**
 * Extract json schema
 */
function extractJsonSchema() {
  return src(config.paths.widgetsJson)
    .pipe(jsonSchema())
    .pipe(flatten())
    .pipe(dest('target/widget-schema'));
}

exports.copy = parallel(widgetsWc,widgetsJs);
exports.extractJsonSchema = extractJsonSchema;
