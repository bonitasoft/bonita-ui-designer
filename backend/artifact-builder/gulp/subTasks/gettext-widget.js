const through = require('through2');
const os = require('os');
const path = require('path');
const replaceExt = require('replace-ext');


function prepare() {
  return through.obj(function (file, enc, cb) {
    let normalizedPath = path.relative(path.resolve(__dirname, '../../..'), file.path);

    try {
      JSON.parse(file.contents.toString());
    } catch (err) {
      cb(err, file);
      return;
    }
    let newfile = [
      '{',
      '  "__file" : "' + escape(normalizedPath) + '",',
      '  "__data" : ' + file.contents.toString(),
      '}'
    ];

    file.contents = Buffer.from(newfile.join(os.EOL));
    cb(undefined, file);
  });

}

function extract() {
  return through.obj(function (file, enc, cb) {

    let i18n = {};
    let widgets;
    let lines = file.contents.toString().split(os.EOL);
    let lineNumber = 0;

    /**
     * parse line by line to find the matched pattern
     * @param  {Array} lines    chunck of lines to parse
     * @param  {String} pattern string to match
     * @return {int}            lineNumber
     */
    function getLine(lines, pattern) {
      let pos = 0;
      lines.some(function (line, index) {
        if (new RegExp(pattern).test(line)) {
          pos = index;
          return true;
        }
        return false;
      });
      return pos;
    }

    /**
     * Return comment information for a given key
     * @param  {String} key  the pattern to find
     * @param  {String} path the current filename
     * @return {String}      a gettext comment about the key to translate
     */
    function getInfo(key, path) {
      let start = getLine(lines.slice(lineNumber), key);
      lineNumber = start + 1;
      return '#: ' + unescape(path) + ':' + lineNumber;
    }

    /**
     * Transform an object containing gettext info into a pot output
     * @param  {Object} data list of key to translate
     * @return String}       a pot string
     */
    function transform(data) {
      return Object.keys(data).map(function (key) {
        if (key.length > 0) {
          return data[key]
            .concat('msgid  "' + escapeQuote(key) + '"')
            .concat('msgstr ""')
            .concat('')
            .join(os.EOL);
        }
      })
        .join(os.EOL);
    }

    function escapeQuote(string) {
      return string.replace(/"/g, '\\"');
    }


    try {
      widgets = JSON.parse('[' + file.contents.toString() + ']');
    } catch (err) {
      cb(err, file);
      return;
    }

    widgets.forEach(function (widgetFile) {
      let fileName = widgetFile.__file;
      let widget = widgetFile.__data;

      i18n = widget.properties.reduce(function (acc, property) {
        let value;
        if (property.hasOwnProperty('label')) {
          value = property.label;
          acc[value] = (acc[value] || []).concat(getInfo('label', fileName));
        }
        if (property.hasOwnProperty('help')) {
          value = property.help;
          acc[value] = (acc[value] || []).concat(getInfo('help', fileName));
        }
        if (property.hasOwnProperty('caption')) {
          value = property.caption;
          acc[value] = (acc[value] || []).concat(getInfo('caption', fileName));
        }
        if (property.hasOwnProperty('defaultValue') && typeof property.defaultValue === 'string') {
          value = property.defaultValue;
          acc[value] = (acc[value] || []).concat(getInfo('defaultValue', fileName));
        }
        if (property.hasOwnProperty('choiceValues')) {
          value = property.choiceValues;
          acc = value.reduce(function (dict, choice) {
            // grouped choice values
            if (typeof choice === 'object') {
              dict[choice.label] = (acc[choice.label] || []).concat('#: ' + unescape(fileName) + ':' + lineNumber);
              if (choice.group) {
                dict[choice.group] = (acc[choice.group] || []).concat('#: ' + unescape(fileName) + ':' + lineNumber);
              }
            } else {
              dict[choice] = (acc[choice] || []).concat('#: ' + unescape(fileName) + ':' + lineNumber);
            }
            return dict;
          }, acc);
        }

        return acc;
      }, i18n);

      if (widget.hasOwnProperty('description')) {
        i18n[widget.description] = (i18n[widget.description] || []).concat(getInfo('description', fileName));
      }
    });

    file.path = replaceExt(file.path, '.pot');
    file.contents = Buffer.from(transform(i18n));

    cb(undefined, file);

  });
}

exports.extract = extract;
exports.prepare = prepare;
