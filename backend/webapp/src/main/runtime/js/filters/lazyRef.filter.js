(function() {
  'use strict';

  angular.module('bonitasoft.ui.filters')

    .filter('lazyRef',function($log) {
    	return function toLazyRef(bo,rel) {
		    if(bo){
		    	if(bo.links){
			    	var result = bo.links.filter((ref) => ref.rel === rel);
			    	if(result[0] && result[0].href){
			    		return '..' + result[0].href;
			    	}
			    	$log.warn('No lazy relation ',rel,' found');
		    	}
		    }
	        return undefined;
	    };
    });
})();
