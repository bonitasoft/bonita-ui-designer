var through = require('through');
var fs = require('fs');
var path = require('path');
var jsesc = require('jsesc');

/**
 * Inline file contents by replacing @<path>postfix with the contents
 * of the file found at <path>postfix relative to original file path.
 */
function inline(postfix) {
  return through(function (file) {
    var originalFilePath = file.path.slice(0, file.path.lastIndexOf(path.sep) - file.path.length);
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
    this.queue(file);
  });
}

module.exports = function inlineJSON() {
  var stream = inline('.tpl.html');
  stream.pipe(inline('.ctrl.js'));
  return stream;
};
