var routesModule = angular.module('routes',['ngRoute']);
  //routes configuration

    routesModule.config(function($routeProvider, $locationProvider) {

      $routeProvider

          // route for the router login screen
          .when('/connect', {
              templateUrl : 'html/index.htm',
              controller  : 'connectionController'
          })
          // route for topology
          .when('/topology', {
              templateUrl: 'html/topology.htm',
              controller: 'topologyController',
              resolve: {
                factory : function ($rootScope, $location) {
                    if(!$rootScope.isDataLoaded){
                      $location.path('/connect');
                    };
                }
              }
          })

          // route for the lsa info
          .when('/lsa', {
              templateUrl : 'html/lsa.htm',
              controller  : 'lsaController',
              resolve: {
                factory : function ($rootScope, $location) {
                    if(!$rootScope.isDataLoaded){
                      $location.path('/connect');
                    };
                }
              }
          })

          // route for the routers page
          .when('/routers', {
              templateUrl : 'html/routers.htm',
              controller  : 'routersController',
              resolve: {
                factory : function ($rootScope, $location) {
                    if(!$rootScope.isDataLoaded){
                      $location.path('/connect');
                    };
                }
              }
          })

          // route for the networks page
          .when('/networks', {
              templateUrl : 'html/networks.htm',
              controller  : 'networksController',
              resolve: {
                factory : function ($rootScope, $location) {
                    if(!$rootScope.isDataLoaded){
                      $location.path('/connect');
                    };
                }
              }
          })

          //route for anything else than mentioned above
          .otherwise({
              redirectTo: '/connect'
          });

      $locationProvider.html5Mode(true).hashPrefix('!');
  });

  routesModule.controller('lsaController', function($scope) {
      $scope.message = 'LSA Table';
  });

  routesModule.controller('routersController', function($scope) {
      $scope.message = 'Routers table.';
  });

  routesModule.controller('topologyController', function($scope) {
      $scope.message = 'Network Map';
  });
