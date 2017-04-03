(function(app) {
  /* application with modules */
  angular.module('topologyApp', [
    'topology',
    'routes',
    'ngRoute',
    'header',
    'connect',
    'lsa',
    'router',
    'network'
  ])

  /* set global variables in root scope*/
  .run(function ($rootScope){
      $rootScope.isDataLoaded = false;
      $rootScope.isDemo = false;
  });
})(window.app || (window.app = {}));
