describe('pbUpload', function() {

  var $compile, scope, dom;

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
      var element = $compile('<pb-upload></pb-upload>')(scope);
      scope.$apply();

      var label = element.find('label');
      expect(label.text().trim()).toBe('upload');
    });

    it('should be on left by default', function () {
      var element = $compile('<pb-upload></pb-upload>')(scope);
      scope.$apply();

      var label = element.find('label');
      expect(element.find('.form-horizontal').length).toBe(1);
    });

    it('should be on the top of the input', function () {
      scope.properties.labelPosition = 'top';

      var element = $compile('<pb-upload></pb-upload>')(scope);
      scope.$apply();

      expect(element.find('.form-horizontal').length).toBe(0);
    });

    it('should not be there when labelHidden is true', function () {
      scope.properties.labelHidden = true;

      var element = $compile('<pb-upload></pb-upload>')(scope);
      scope.$apply();

      expect(element.find('label').length).toBe(0);
    });
  });

  describe('input', function() {
    it('should adapt its width to label size when on the left', function () {
      scope.properties.labelWidth = 3;

      var element = $compile('<pb-upload></pb-upload>')(scope);
      scope.$apply();

      var form = element.find('form');
      expect(form.parent().hasClass('col-xs-9')).toBeTruthy();
    });

    it('should be wrapped in full width div when label is hidden', function () {
      scope.properties.labelHidden = true;

      var element = $compile('<pb-upload></pb-upload>')(scope);
      scope.$apply();

      var form = element.find('form');
      expect(form.parent().hasClass('col-xs-12')).toBeTruthy();
    });

    it('should not be required by default', function () {
      var element = $compile('<pb-upload></pb-upload>')(scope);
      scope.$apply();

      var input = element.find('input');
      expect(input.attr('required')).toBeFalsy();
    });

    it('should be required when requested', function () {
      scope.properties.required = true;
      var element = $compile('<pb-upload></pb-upload>')(scope);
      scope.$apply();

      var input = element.find('input[type=file]');
      expect(input.attr('required')).toBeTruthy();
    });

    it('should validate required', function () {
      scope.properties.required = true;

      var element = $compile('<pb-upload></pb-upload>')(scope);
      var input = element.find('input[type="file"]');
      scope.$apply();
      expect(input.attr('class')).toMatch('ng-invalid-required');
      var controller = element.controller('pbUpload');
      controller.uploadComplete({filename: 'toto.jpg'});
      scope.$apply();

      expect(input.attr('class')).not.toMatch('ng-invalid-required');
    });
  });

  describe('PBController', function(){
    it('should set the input when uploadComplete', function() {
      var file = {
        filename:'foobar',
        tempPath: 'quux'
      };
      var element = $compile('<pb-upload></pb-upload>')(scope);
      var controller = element.controller('pbUpload');
      controller.uploadComplete(file);
      scope.$apply();
      expect(element.find('input').val()).toBe('foobar');
      expect(element.find('.file-upload-clear')).toBeDefined();
    });

    it('should clear the input', function() {
      var file = {
        filename:'foobar',
        tempPath: 'quux'
      };
      var element = $compile('<pb-upload></pb-upload>')(scope);
      var controller = element.controller('pbUpload');
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
      var element = $compile('<pb-upload></pb-upload>')(scope);
      var controller = element.controller('pbUpload');

      controller.startUploading();
      scope.$apply();
      
      expect(element.find('input').val()).toMatch(/uploading/i);
    });

    it('should set a message when upload fails', function() {
      var element = $compile('<pb-upload></pb-upload>')(scope);
      var controller = element.controller('pbUpload');
      var errorBody = 'upload failed because of FileTooBigException';
      
      controller.uploadComplete(errorBody);
      scope.$apply();
      
      expect(element.find('input').val()).toMatch(/Upload failed/i);
      expect(scope.properties.errorContent).toEqual(errorBody);
    });

    it('should set a message when upload fails with a Json object', function() {
      var element = $compile('<pb-upload></pb-upload>')(scope);
      var controller = element.controller('pbUpload');
      var error = {
        type: 'FileTooBigException',
        message: 'The given file is too big to be stored or processed'
      };
      controller.uploadComplete(error);
      scope.$apply();
      expect(element.find('input').val()).toMatch(/Upload failed/i);
      expect(scope.properties.errorContent).toEqual(error.message);
    });
    it('should update the filename if the value changes (on deletion for intance)', function () {
      var element = $compile('<pb-upload></pb-upload>')(scope);
      var controller = element.controller('pbUpload');
       
      scope.$apply();
      expect(controller.filename).toBe('');

      var filename = 'myFile.txt';
      scope.properties.value = {filename: filename};
      scope.$apply();
      expect(controller.filename).toBe(filename);

      scope.properties.value = undefined;
      scope.$apply();
      expect(controller.filename).toBeUndefined();
    });
  });
});
