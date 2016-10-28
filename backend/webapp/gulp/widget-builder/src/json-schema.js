(() => {

  const through2 = require('through2');

  function buildJsonSchema() {

    // map a widget property type to jsonschema type
    var jsonSchemaTypes = {
      html: 'string',
      text: 'string',
      boolean: 'boolean',
      integer: 'number',
      collection: 'string'  // TODO collection has string for now since we need reference to collection
    };

    return through2.obj(function (file, enc, callback) {

      if (file.path.indexOf('.json') > 0) {
        var widget = JSON.parse(file.contents);
        var schema = {
          type: 'object',
          id: widget.id,
          properties: {
            cssClasses: {
              type: 'string'
            },
            hidden: {
              type: 'boolean',
              default: false
            }
          }
        };

        schema.properties = (widget.properties || []).reduce((acc, prop) => {
          acc[prop.name] = {
            default: prop.defaultValue
          };

          if (prop.type === 'choice') {
            acc[prop.name].enum = getEnums(prop.choiceValues);
          } else {
            acc[prop.name].type = jsonSchemaTypes[prop.type];
          }
          return acc;
        }, schema.properties);


        file.contents = new Buffer(String(JSON.stringify(schema, null, 2)));
      }

      this.push(file);
      callback();
    });

    function getEnums(choiceValues) {
      return typeof choiceValues[0] === 'object' ? choiceValues.map((choice) => choice.value) : choiceValues;
    }

  };

  module.exports = buildJsonSchema;

})();
