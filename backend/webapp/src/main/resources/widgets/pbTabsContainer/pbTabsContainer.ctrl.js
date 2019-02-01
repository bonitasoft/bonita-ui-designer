function PbTabsContainerCtrl($scope, $log) {
    var ctrl = this;

    this.defaultOptions = {
        allowHtml: false,
        disabled: false,
        activated: false,
        hidden: false,
        cssClasses: ""
    };

    $scope.getOptions = function(tabTitle) {
      return angular.extend({},
        ctrl.defaultOptions,
        $scope.options && $scope.options[tabTitle]);
    };

    $scope.$watch('properties.advancedOptions ', function(value) {
        if (angular.isString(value)) {
            try {
                $scope.options = angular.fromJson(value);
            } catch (e) {
                $log.error('[TabsContainer widget] Advanced options should be a valid json object');
            }
        } else {
            $scope.options = value;
        }
    });
}
