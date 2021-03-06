'use strict';

var ioLeggoApp = angular.module('ioLeggoApp', ['ngRoute', 'ioLeggoControllers', 'ioLeggoServices', 'ngStorage']);

ioLeggoApp.config(['$routeProvider',
  function($routeProvider, $localStorage) {
    $routeProvider.
      when('/', {
        controller: 'ioLeggoCtrl',
        templateUrl: 'partials/index.html',
        reloadOnSearch: false
      }).
      when('/set', {
          redirectTo: function (routeParams, path, search) {
            var readingModes = search.readingModes;
            if (readingModes !== undefined && readingModes.length > 0) {
             readingModes = readingModes.replace(/\\"/,"\"");
             localStorage.setItem('ngStorage-ioLeggoReadingModes', readingModes);
            }
            return "/";
      }}).
      otherwise({
        redirectTo: '/'
      });
  }
]);

ioLeggoApp.filter('unsafe', ['$sce', function ($sce) {
    return function (val) {
        return $sce.trustAsHtml(val);
    };
}]);

ioLeggoApp.directive('readingArea', function($sce) {
  return {
    restrict: 'AE',
    replace: true,
    scope: {
      items: '=items',
      index: '=index',
      backgroundColor: '=backgroundColor',
      fontColor: '=fontColor',
      toReadColor: '=toReadColor',
      syllabation: '=syllabation',
      hideTextBefore: '=hideTextBefore',
      hideTextAfter: '=hideTextAfter',
      toUpper: '=toUpper',
      fontSize: '=fontSize',
      refresh: '=refresh'
    },
    template: '<div id="reading-area" style="background-color:{{backgroundColor}}; font-size:{{fontSize}}%;">',
    compile: function(tElem, attrs) {
      //do optional DOM transformation here
      $(tElem).addClass("reading-area");
      return function(scope, elem, attrs) {
        //linking function here
        scope.highlightWord = function(index) {
            if (index >= 0) {
                for (var i = 0; i < index; i++) {
                    $('#reading-area #item-'+ i).css('color', (scope.hideTextBefore ? scope.backgroundColor : scope.fontColor));
                }

                $('#reading-area #item-' + index).css('color', scope.toReadColor);

                for (var i = index+1; i < scope.items.length; i++) {
                    $('#reading-area #item-'+ i).css('color', (scope.hideTextAfter ? scope.backgroundColor : scope.fontColor));
                }
            } else {
                for (var i = 0; i < scope.items.length; i++) {
                    $('#reading-area #item-'+ i).css('color', scope.fontColor);
                }
            }
        }

        scope.$watch('items', function() {
            $('#reading-area').html('');
            scope.items.forEach(function(item, index) {
                $('#reading-area').append('<span id="item-'+index+'" style="color: '+scope.fontColor+';">'+(scope.toUpper ? item.toUpperCase() : item)+'</span>');
            });
        });

        scope.$watch('index', function() {
            scope.highlightWord(scope.index);
        });

        scope.$watch('refresh', function() {
            scope.highlightWord(scope.index);
        });

      };
    }
  };
});

ioLeggoApp.directive('colorPicker', function ($parse, $rootScope) {
   return {
      restrict: "E",
      replace: true,
      transclude: false,
      compile: function (element, attrs) {
         var modelAccessor = $parse(attrs.ngModel);

         var html = "<input type='text' id='" + attrs.id + "' >" + "</input>";

         var newElem = $(html);
         element.replaceWith(newElem);

         return function (scope, element, attrs, controller) {

            var processChange = function (container, color) {
               var color = color.tiny.toRgbString();
               if (! $rootScope.$root.$$phase) {
               scope.$apply(function (scope) {
                  // Change bound variable
                  modelAccessor.assign(scope, color);
               });
               }
            };

            element.ColorPickerSliders({
                size: 'lg',
                flat: true,
                swatches: false,
                order: {
                    rgb: 1
                },
                onchange: processChange
            });

            scope.$watch(modelAccessor, function (color) {
               element.trigger("colorpickersliders.updateColor", color);
            });

         };

      }
   };
});

