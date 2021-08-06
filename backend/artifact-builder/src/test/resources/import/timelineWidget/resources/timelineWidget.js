(function () {
  try {
    return angular.module('bonitasoft.ui.widgets');
  } catch(e) {
    return angular.module('bonitasoft.ui.widgets', []);
  }
})().directive('timelineWidget', function() {
    return {
      controllerAs: 'ctrl',
      controller: /**
 * The controller is a JavaScript function that augments the AngularJS scope and exposes functions that can be used in the custom widget template
 *
 * Custom widget properties defined on the right can be used as variables in a controller with $scope.properties
 * To use AngularJS standard services, you must declare them in the main function arguments.
 *
 * You can leave the controller empty if you do not need it.
 */
function ($scope) {
    
  /**
   * new Date in IE11 must be receive string with ISO8601 format. This formatting was use for IE.
   * (link: https://stackoverflow.com/a/44314573)
   */
  $scope.formatDate = function(mydate){
    return new Date(mydate.replace(/^(.*-[0-9][0-9])(\ )([0-9][0-9]\:.*$)/, '$1T$3')).getTime();
  }


  $scope.buildEventTitle = function(event) {
    return $scope.$eval($scope.properties.eventsTitleExpression, {"event": event});
  };

  $scope.buildEventTime = function(event) {
    return $scope.$eval($scope.properties.eventsTimeExpression, {"event": event});
  };

  $scope.buildEventAuthor = function(event) {
    return $scope.$eval($scope.properties.eventsAuthorExpression, {"event": event});
  };

  $scope.buildEventContent = function(event) {
    return $scope.$eval($scope.properties.eventsContentExpression, {"event": event});
  };
  
  $scope.buildEventIcon = function(event) {
    return $scope.$eval($scope.properties.eventsIconExpression, {"event": event});
  };
  
   $scope.buildEventStyle = function(event) {
    return $scope.$eval($scope.properties.eventsIconStyleExpression, {"event": event});
  };


  // ------------
  // -- Origin --
  // ------------

  $scope.buildOriginTitle = function() {
      return $scope.$eval($scope.properties.originTitleExpression, {"origin": $scope.properties.origin});
  };

  $scope.buildOriginTime = function() {
    return $scope.$eval($scope.properties.originTimeExpression, {"origin": $scope.properties.origin});
  };

  $scope.buildOriginAuthor = function() {
    return $scope.$eval($scope.properties.originAuthorExpression, {"origin": $scope.properties.origin});
  };

  $scope.buildOriginContent = function() {
    return $scope.$eval($scope.properties.originContentExpression, {"origin": $scope.properties.origin});
  };

}
,
      template: '<!-- The custom widget template is defined here\n   - You can use standard HTML tags and AngularJS built-in directives, scope and interpolation system\n   - Custom widget properties defined on the right can be used as variables in a templates with properties.newProperty\n   - Functions exposed in the controller can be used with ctrl.newFunction()\n - You can use the environment property injected in the scope when inside the whiteboard editor. It allows to create a mockup display for the whiteboard as the real use data will not be available.\n-->\n\n<span ng-if="environment"><identicon name="{{environment.component.id}}" size="30" background-color="[255,255,255, 0]" foreground-color="[51,51,51]"></identicon> {{environment.component.name}}</span>\n\n<timeline>\n    <timeline-node  ng-repeat="event in properties.events" side="{{$even ?\'left\':\'right\'}}">\n        <timeline-badge class="{{buildEventStyle(event)}}"> <i class="glyphicon glyphicon-{{buildEventIcon(event)}}"></i>\n        </timeline-badge>\n        <timeline-panel>\n            <timeline-heading>\n                <timeline-title ng-bind-html="buildEventTitle(event)">\n               </timeline-title>\n                <p>\n                    <small class="text-muted"> <i class="glyphicon glyphicon-time"></i>\n                          {{formatDate(buildEventTime(event)) | date:properties.eventsTimeAttributeFormat}}\n                    </small>\n                    <br/>\n                    <small class="text-muted" ng-if="buildEventAuthor(event)"> <i class="glyphicon glyphicon-user"></i>\n                        {{buildEventAuthor(event)}}\n                    </small>\n                </p>\n            </timeline-heading>\n            <timeline-content>\n                <p>{{buildEventContent(event)}}</p>\n            </timeline-content>\n        </timeline-panel>\n    </timeline-node>\n    <timeline-node ng-if="properties.events[0]===undefined">\n        <timeline-badge class="warning"> <i class="glyphicon glyphicon-asterisk"></i>\n        </timeline-badge>\n        <timeline-panel>\n            <timeline-heading>\n                <timeline-title>\n                    <span translate>{{properties.noEventsTitle}}</span>\n                </timeline-title>\n            </timeline-heading>\n            <timeline-content>\n                <p translate>{{properties.noEventsContent}}</p>\n            </timeline-content>\n        </timeline-panel>\n    </timeline-node>\n</timeline>\n<timeline-footer>\n    <timeline-panel>\n        <timeline-heading>\n            <timeline-title>\n                <span translate>{{buildOriginTitle()}}</span>\n            </timeline-title>\n            <p>\n                <small class="text-muted"> <i class="glyphicon glyphicon-time"></i>\n                    {{formatDate(buildOriginTime()) | date:properties.eventsTimeAttributeFormat}}\n                </small>\n                 <br/>\n                <small class="text-muted"> <i class="glyphicon glyphicon-user"></i>\n                    {{buildOriginAuthor()}}\n                </small>\n            </p>\n        </timeline-heading>\n        <timeline-content>\n            <p>{{buildOriginContent()}}</p>\n        </timeline-content>\n    </timeline-panel>\n</timeline-footer>\n'
    };
  });
