(function(){
  'use strict';

  angular.module('pb.directives').directive('componentHighlighter', function(){
    return {
      restrict:'A',
      link: function($scope, elem, attrs) {
        var cssClassName=attrs.componentHighlighter;
        var node;

        function onMouseOver(event) {
          var currentNode = event.target;
          while(currentNode.parentNode) {
            if (/\w-element/.test(currentNode.className)){
              if (node === currentNode) {
                return;
              }
              if (node) {
                node.className = node.className.replace(cssClassName,'').trim();
              }
              currentNode.className += ' ' + cssClassName;
              node = currentNode;
              return;
            }
            currentNode = currentNode.parentNode;
          }
        }

        function onMouseLeave() {
          if (node) {
            node.className = node.className.replace(cssClassName,'');
            node = null;
          }
        }

        var wrapper = angular.element( elem[0].querySelector('.widget-wrapper'));

        elem.on('mouseover', onMouseOver);
        wrapper.on('mouseleave', onMouseLeave);
      }
    };
  });
})();
