/*
 * AngularJS merge method polyfill (mandatory for AngularJS < 1.4)
 * retrieved from https://gist.github.com/luckylooke/8433aa366bb680014cd0
 */
(() => {
  if (!angular.merge) {
    angular.merge = (function mergePollyfill() {
      function setHashKey(obj, h) {
        if (h) {
          obj.$$hashKey = h;
        } else {
          delete obj.$$hashKey;
        }
      }

      function baseExtend(dst, objs, deep) {
        var h = dst.$$hashKey;

        for (var i = 0, ii = objs.length; i < ii; ++i) {
          var obj = objs[i];
          if (!angular.isObject(obj) && !angular.isFunction(obj)) { continue; }
          var keys = Object.keys(obj);
          for (var j = 0, jj = keys.length; j < jj; j++) {
            var key = keys[j];
            var src = obj[key];

            if (deep && angular.isObject(src)) {
              if (angular.isDate(src)) {
                dst[key] = new Date(src.valueOf());
              } else {
                if (!angular.isObject(dst[key])) { dst[key] = angular.isArray(src) ? [] : {}; }
                baseExtend(dst[key], [src], true);
              }
            } else {
              dst[key] = src;
            }
          }
        }

        setHashKey(dst, h);
        return dst;
      }

      return function merge(dst) {
        return baseExtend(dst, [].slice.call(arguments, 1), true);
      };
    })();
  }
})();
