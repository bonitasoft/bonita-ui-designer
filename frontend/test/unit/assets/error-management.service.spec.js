describe('AsserErrorManagement service', () => {

  var assetErrorManagement, alerts, $scope;

  beforeEach(angular.mock.module('bonitasoft.designer.assets'));

  beforeEach(inject(function (_assetErrorManagement_, _alerts_, $rootScope) {
    assetErrorManagement = _assetErrorManagement_;
    alerts = _alerts_;
    $scope = $rootScope;
    spyOn(alerts, 'addError');
  }));

  it('should resolve promise when no error has occured', function () {
    var response = {'id': '3fdf575e-f589-4b8e-807c-295f18bee9cc', 'name': 'localization.json', 'type': 'json', 'order': 2, 'active': true, 'external': false};

    assetErrorManagement.manageErrorsFromResponse(response)

      .then(
      (promiseResponse) => expect(promiseResponse).toEqual(response),
      () => fail('Promise should not be rejected'));
    $scope.$apply();
  });

  it('should diplay error and reject promise when response contains error after form submit', function () {
    var response = {type: 'error', message: 'an error occured'};

    assetErrorManagement.manageErrorsFromResponse(response)

      .then(
      () => fail('Promise should not be resolved'),
      (promiseResponse) => {
        expect(alerts.addError).toHaveBeenCalledWith(response.message);
        expect(promiseResponse).toEqual(response);
      });
    $scope.$apply();
  });

  it('should display a specific error while uploading a malformed json file for json asset', function () {
    var response = {type: 'MalformedJsonException', message: 'an error occured', infos: {location: {column: 65, line: 12}}};

    assetErrorManagement.manageErrorsFromResponse(response)

      .then(
      () => fail('Promise should not be resolved'),
      (promiseResponse) => {
        expect(alerts.addError).toHaveBeenCalledWith({
          contentUrl: 'js/assets/malformed-json-error-message.html',
          context: response
        }, 12000);
        expect(promiseResponse).toEqual(response);
      });
    $scope.$apply();
  });
});
