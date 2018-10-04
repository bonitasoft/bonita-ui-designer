describe('ace-data-completer', function() {
  var service;

  beforeEach(angular.mock.module('bonitasoft.designer.common.services'));
  beforeEach(inject(function($injector) {
    service = $injector.get('aceDataCompleter');
  }));

  it('should return an object with getCompletions ', function() {
    var completer = service();
    expect(completer.getCompletions).toBeDefined();
  });

  describe('getCompletions', function() {
    it('should return each data in completions', function () {
      var data = {
        'users': {value: []}
      };
      var completer = service(data);
      completer.getCompletions(null, null, 0, '', function (err, results) {
        expect(results).toContain({name: '$data.users', value: '$data.users', score: 10, meta: 'data'});
      });
    });
    it('should return translate function in completions', function () {
      var data = {
        'users': { value: [] }
      };
      var completer = service(data);
      completer.getCompletions(null, null, 0, '' ,function(err, results) {
        expect(results.find((e) => {
          return e.caption && e.caption.includes('uiTranslate');
        })).toBeDefined();
      });
    });
  });
});
