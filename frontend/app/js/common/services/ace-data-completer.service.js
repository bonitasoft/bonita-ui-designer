angular.module('pb.common.services').service('aceDataCompleter', function(){
  return function(data){
     return {
      getCompletions: function(editor, session, pos, prefix, callback) {

        function getPrefixedKeys(key) {
          return '$data.'+key;
        }

        function filterKeys (match, key) {
          return key.indexOf(match) === 0;
        }

        var completions = Object.keys(data)
          .map(getPrefixedKeys)
          .filter( filterKeys.bind(null, prefix) )
          .map(function(data) {
            return {
              name: data,
              value: data,
              score: 2, // increase score to show suggestion on top of the list
              meta: 'data' // the suggestion's category
            };
          });
        callback(null, completions);
      }
    };
  };
});
