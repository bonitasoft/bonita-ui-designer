describe('App config', function() {

  beforeEach(angular.mock.module('uidesigner'));

  describe('UI_DESIGNER_SCREEN_SIZE', function() {
    var resolutions;

    beforeEach(inject(function(_resolutions_) {
      resolutions = _resolutions_;
    }));

    it('should not register extra resolutions', function() {
      expect(resolutions.all()).toEqual([
        {
          key: 'xs',
          label: 'Phone',
          icon: 'mobile',
          width: 320,
          tooltip: 'Extra Small devices (width \u003C 768px)'
        },
        {
          key: 'sm',
          label: 'Tablet',
          icon: 'tablet',
          width: 768,
          tooltip: 'Small devices (width \u2265 768px)'
        },
        {
          key: 'md',
          label: 'Desktop',
          icon: 'laptop',
          width: 992,
          tooltip: 'Medium devices (width \u2265 992px)'
        },
        {
          key: 'lg',
          label: 'Large desktop',
          icon: 'desktop',
          width: 1200,
          tooltip: 'Large devices (width \u2265 1200px)'
        }
      ]);
    });

    it('should change default dimension', function() {
      expect(resolutions.getDefaultDimension()).toEqual({ xs: 12, sm: 12, md: 12, lg: 12 });
    });

    it('should change default resolution', function() {
      expect(resolutions.getDefaultResolution()).toEqual({
        key: 'md',
        label: 'Desktop',
        icon: 'laptop',
        width: 992,
        tooltip: 'Medium devices (width \u2265 992px)'
      });
    });
  });
});
