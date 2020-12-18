(function() {
  'use strict';

  angular
    .module('bonitasoft.ui.services')
    .run(createExpressionResolver);

  function createExpressionResolver(Resolver, ResolverService, $log, $rootScope, $filter) {

    class ExpressionResolver extends Resolver {
      constructor(model, name, content) {
        super(model, name, content);
        this.dependencies = ExpressionResolver.extractDependencies(this.content);
      }
      resolve() {
        // use strict. Avoid pollution of the global object.
        var expression = new Function(
          '$data',//inject all data
          'uiTranslate',//inject translate function
          '"use strict";' + this.content);
        try {
          this.model[this.name] = expression(
            this.model, // all data
            (text) => $filter('translate')(text) // translate function
          );
        } catch (e) {
          $log.warn('Error evaluating <', this.name, '> data: ', e.message);
        }
      }
      watchDependencies() {
        this.dependencies.forEach(
          (dependency) => $rootScope.$watch(() => this.model[dependency],
            () => this.resolve(), true)
        );
      }
      hasDependencies() {
        return this.dependencies.length;
      }
      static extractDependencies(impl) {
        //looking for dependencies in the form of '$data.XXX'
        return ((impl + '').match(/\$data\.([\w\$_]+)/g) || [])
          .map((dependency) => dependency.replace(/\$data\./, ''))
          //filter unique dependencies
          .filter((item, position, self) => self.indexOf(item) === position);
      }
    }

    ResolverService.addResolverType('expression', (model, name, content) => new ExpressionResolver(model, name, content));
  }
})();
