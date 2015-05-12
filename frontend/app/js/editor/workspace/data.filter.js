/**
 * Filter a data value to print data:{{data.value}} when data type is data
 * Just a reminder to the user in the editor that he has linked a field to a data
 */
angular.module('pb.filters').filter('data', function () {

  'use strict';

  return function (param, paramType) {
    if (!param || !param.value) {
      return '';
    } else if (param.type === 'data') {
      var value = 'data:' + param.value;

      // In case of collection property type, we force the property to Array
      // so it plays well with editor render
      if (paramType === 'collection') {
        return [value];
      }

      return value;
    } else {
      return param.value;
    }
  };
});
