describe('pbUpload', function() {

  let $compile, scope, dom;

  beforeEach(module('bonitasoft.ui.widgets'));
  beforeEach(module('bonitasoft.ui.services'));
  beforeEach(inject(function ($injector){
    $compile = $injector.get('$compile');
    scope = $injector.get('$rootScope').$new();
    scope.properties = {
      isBound: function() {
        return false;
      },
      labelHidden: false,
      required: false,
      label: 'upload',
      labelPosition: 'left',
      labelWidth: '4',
      placeholder: 'coucou',
      alignment: 'left',
      url: 'test',
      value: ''
    };
  }));

  describe('label', function() {
    it('should set the label value', function () {
      let element = $compile('<pb-upload></pb-upload>')(scope);
      scope.$apply();

      let label = element.find('label');
      expect(label.text().trim()).toBe('upload');
    });

    it('should be on left by default', function () {
      let element = $compile('<pb-upload></pb-upload>')(scope);
      scope.$apply();

      let label = element.find('label');
      expect(element.find('.form-horizontal').length).toBe(1);
    });

    it('should be on the top of the input', function () {
      scope.properties.labelPosition = 'top';

      let element = $compile('<pb-upload></pb-upload>')(scope);
      scope.$apply();

      expect(element.find('.form-horizontal').length).toBe(0);
    });

    it('should not be there when labelHidden is true', function () {
      scope.properties.labelHidden = true;

      let element = $compile('<pb-upload></pb-upload>')(scope);
      scope.$apply();

      expect(element.find('label').length).toBe(0);
    });

    it('should allows html markup to be interpreted', function() {
      scope.properties = angular.extend(scope.properties, {
        label: '<span>allow html!</span>',
        allowHTML: true
      });

      var element = $compile('<pb-upload></pb-upload>')(scope);
      scope.$apply();
      var label = element.find('label');
      expect(label.text().trim()).toBe('allow html!');
    });

    it('should prevent html markup to be interpreted', function() {
      scope.properties = angular.extend(scope.properties, {
        label: '<span>allow html!</span>',
        allowHTML: false
      });

      var element = $compile('<pb-upload></pb-upload>')(scope);
      scope.$apply();
      var label = element.find('label');
      expect(label.text().trim()).toBe('<span>allow html!</span>');
    });
  });

  describe('input', function() {
    it('should adapt its width to label size when on the left', function () {
      scope.properties.labelWidth = 3;

      let element = $compile('<pb-upload></pb-upload>')(scope);
      scope.$apply();

      let form = element.find('form');
      expect(form.parent().hasClass('col-xs-9')).toBeTruthy();
    });

    it('should be wrapped in full width div when label is hidden', function () {
      scope.properties.labelHidden = true;

      let element = $compile('<pb-upload></pb-upload>')(scope);
      scope.$apply();

      let form = element.find('form');
      expect(form.parent().hasClass('col-xs-12')).toBeTruthy();
    });

    it('should not be required by default', function () {
      let element = $compile('<pb-upload></pb-upload>')(scope);
      scope.$apply();

      let input = element.find('input');
      expect(input.attr('required')).toBeFalsy();
    });

    it('should be required when requested', function () {
      scope.properties.required = true;
      let element = $compile('<pb-upload></pb-upload>')(scope);
      scope.$apply();

      let input = element.find('input[type=file]');
      expect(input.attr('required')).toBeTruthy();
    });

    it('should validate required', function () {
      scope.properties.required = true;

      let element = $compile('<pb-upload></pb-upload>')(scope);
      let input = element.find('input[type="file"]');
      scope.$apply();
      expect(input.attr('class')).toMatch('ng-invalid-required');
      let controller = element.controller('pbUpload');
      controller.uploadComplete({filename: 'toto.jpg'});
      scope.$apply();

      expect(input.attr('class')).not.toMatch('ng-invalid-required');
    });
  });

  describe('PBController', function(){
    it('should set the input when uploadComplete', function() {
      let file = {
        filename:'foobar',
        tempPath: 'quux'
      };
      let element = $compile('<pb-upload></pb-upload>')(scope);
      let controller = element.controller('pbUpload');
      controller.uploadComplete(file);
      scope.$apply();
      expect(element.find('input').val()).toBe('foobar');
      expect(element.find('.file-upload-clear')).toBeDefined();
    });

    it('should clear the input', function() {
      let file = {
        filename:'foobar',
        tempPath: 'quux'
      };
      let element = $compile('<pb-upload></pb-upload>')(scope);
      let controller = element.controller('pbUpload');
      controller.uploadComplete(file);
      scope.$apply();
      expect(element.find('input').val()).toBe('foobar');
      expect(element.find('.file-upload-clear')).toBeDefined();

      controller.clear();
      scope.$apply();
      expect(element.find('input').val()).toBe('');
      expect(element.find('.file-upload-clear').length).toBe(0);
      expect(scope.properties.value).toEqual({});
    });

    it('should set a message when upload start', function() {
      let element = $compile('<pb-upload></pb-upload>')(scope);
      let controller = element.controller('pbUpload');

      controller.startUploading();
      scope.$apply();

      expect(element.find('input').val()).toMatch(/uploading/i);
    });

    it('should set a message when upload fails', function() {
      let element = $compile('<pb-upload></pb-upload>')(scope);
      let controller = element.controller('pbUpload');
      let errorBody = 'upload failed because of FileTooBigException';

      controller.uploadComplete(errorBody);
      scope.$apply();

      expect(element.find('input').val()).toMatch(/Upload failed/i);
      expect(scope.properties.errorContent).toEqual(errorBody);
    });

    it('should set a message when upload fails with a Json object', function() {
      let element = $compile('<pb-upload></pb-upload>')(scope);
      let controller = element.controller('pbUpload');
      let error = {
        type: 'FileTooBigException',
        message: 'The given file is too big to be stored or processed'
      };
      controller.uploadComplete(error);
      scope.$apply();
      expect(element.find('input').val()).toMatch(/Upload failed/i);
      expect(scope.properties.errorContent).toEqual(error.message);
    });

    it('should update the filename if the value changes (on deletion for instance)', function () {
      let element = $compile('<pb-upload></pb-upload>')(scope);
      let controller = element.controller('pbUpload');

      scope.$apply();
      expect(controller.filename).toBe('');

      let filename = 'myFile.txt';
      scope.properties.value = {filename: filename};
      scope.$apply();
      expect(controller.filename).toBe(filename);

      scope.properties.value = undefined;
      scope.$apply();
      expect(controller.filename).toBeUndefined();
    });

    it('should sanitize the filename', function () {
      let element = $compile('<pb-upload></pb-upload>')(scope);
      let controller = element.controller('pbUpload');

      scope.$apply();
      expect(controller.filename).toBe('');

      scope.properties.value = {filename: 'file"with>forbidden.txt'};
      scope.$apply();
      expect(controller.filename).toBe('file&#34;with&gt;forbidden.txt');

      scope.properties.value = undefined;
      scope.$apply();
      expect(controller.filename).toBeUndefined();
    });

    it('should update the filename if the value changes to null', function () {
      let element = $compile('<pb-upload></pb-upload>')(scope);
      let controller = element.controller('pbUpload');

      scope.$apply();
      expect(controller.filename).toBe('');

      scope.properties.value = null;
      scope.$apply();
      expect(controller.filename).toBeUndefined();
    });

    it('should set the event value to null on submit', function() {
      let element = $compile('<pb-upload></pb-upload>')(scope);
      let controller = element.controller('pbUpload');
      scope.$apply();
      spyOn(controller, 'submitForm');
      let myEvent = {target: {value: 'value'}};
      controller.forceSubmit(myEvent);
      expect(myEvent.target.value).toBe(null);
    });

    it('should reset errorContent properties when upload is successful', function () {
      let file = {
        filename:'foobar',
        tempPath: 'quux'
      };

      let element = $compile('<pb-upload></pb-upload>')(scope);
      let controller = element.controller('pbUpload');

      scope.properties.errorContent = 'Error during upload';
      scope.$apply();

      controller.uploadComplete(file);

      expect(scope.properties.errorContent).toBe(undefined);
    });
  });
});
