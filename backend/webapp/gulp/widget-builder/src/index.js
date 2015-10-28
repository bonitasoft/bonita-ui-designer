(function () {
  'use strict';

  var through2 = require('through2');
  var File = require('vinyl');
  var fs = require('fs');
  var path = require('path');
  var jsesc = require('jsesc');
  var Handlebars = require('handlebars');
  var vfs = require('vinyl-fs');

  /**
   * Inline file contents by replacing @<path>postfix with the contents
   * of the file found at <path>postfix relative to original file path.
   */
  function inline(file, postfix) {
    if (file.path.indexOf('.json') > 0) {
      var originalFilePath = getParentDir(file);
      var matchingPattern = new RegExp('@(.*)' + postfix);
      var contents = String(file.contents);
      var match = matchingPattern.exec(contents);

      if (match) {
        var contentsFilePath = path.join(originalFilePath, match[1] + postfix);
        if (fs.existsSync(contentsFilePath)) {
          file.contents = new Buffer(contents.replace(
            matchingPattern,
            jsesc(fs.readFileSync(contentsFilePath, 'utf8'), {'quotes': 'double'})));
        }
      }
    }
    return file;
  }

  function getDirectiveTemplate(context) {
    return Handlebars.compile(String(fs.readFileSync(__dirname + '/widgetDirectiveTemplate.hbs.js')))(context);
  }

  function buildDirective(file) {
    var context = JSON.parse(file.contents);
    context.escapedTemplate = jsesc(context.template);
    return new File({
      cwd: file.cwd,
      base: file.base,
      path: file.path.replace('.json', '.js'),
      contents: new Buffer(getDirectiveTemplate(context))
    });
  }

  function pushTo(stream) {
    return through2.obj(function (file, enc, callback) {
      stream.push(file);
      callback();
    });
  }

  function getParentDir(file) {
    return file.path.slice(0, file.path.lastIndexOf(path.sep) - file.path.length);
  }

  function buildWidget() {

    return through2.obj(function (file, enc, callback) {
      var base = {base: file.base};
      var parentDir = getParentDir(file);
      inline(file, '.tpl.html');
      inline(file, '.ctrl.js');
      this.push(file);
      this.push(buildDirective(file));
      fs.exists(path.join(parentDir, 'assets'), function (exists) {
        if (!exists) {
          callback();
          return;
        }
        vfs.src(path.join(parentDir, '/assets/**/*.*'), base)
          .pipe(pushTo(this))
          .on('finish', function () {
            callback();
          });
      }.bind(this));
    });
  }

  module.exports = buildWidget;
})();
