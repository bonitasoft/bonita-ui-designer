describe('Service: bindingContextFactory', function () {

  beforeEach(module('bonitasoft.ui.services'));

  var bindingContextFactory, modelCtrl;

  beforeEach(inject(function (_bindingContextFactory_) {
    bindingContextFactory = _bindingContextFactory_;
    modelCtrl = {
      createGateway: function () {
        return {};
      }
    };
  }));

  it('should expose $index when defined', function () {
    var scope = {$index: 1};

    var context = bindingContextFactory.create(modelCtrl, scope);

    expect(context.$index).toBe(1);
  });

  it('should expose $item when defined', function () {
    var scope = {$item: {"foo": "bar"}};

    var context = bindingContextFactory.create(modelCtrl, scope);

    expect(context.$item).toEqual({"foo": "bar"});
  });

  it('should expose $collection when defined', function () {
    var scope = {$collection: [{ item: 1 }, { item: 2 }]};

    var context = bindingContextFactory.create(modelCtrl, scope);

    expect(context.$collection).toEqual([ { item: 1 }, { item: 2 }]);
  });

  it('should update $collection when setting $item value', function () {
    var scope = {$collection: [{"foo": "bar"}], $index: 0};

    var context = bindingContextFactory.create(modelCtrl, scope);
    context.$item = {"foo": "bar"};

    expect(scope.$collection).toEqual([{"foo": "bar"}]);
  });

  it('should expose $form when defined', function () {
    var scope = {$form: "form"};

    var context = bindingContextFactory.create(modelCtrl, scope);

    expect(context.$form).toEqual("form");
  });
});
