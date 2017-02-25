'use strict';

var connectModule = angular.module('connect', []);

connectModule.factory('AuthenticationService',
    ['$http', '$rootScope', '$timeout', '$location',
    function ($http, $rootScope, $timeout, $location) {
        var service = {};

        service.Login = function (ipaddress, username, password, callback) {

            /* Dummy authentication for demo mode, $timeout is used to simulate api call */
            if(ipaddress.toLowerCase() === "demo" && username.toLowerCase() === "demo" && password.toLowerCase() === "demo"){
                $rootScope.isDemo = true;

                $timeout(function(){
                    var response = {};
                    response.status = 200;
                    callback(response);
                }, 2000);
          }
          /* normal mode with real connection */
          else {
              $rootScope.isDemo = false;

              var config = {
                  method: 'POST',
                  url: 'http://localhost:8080/topology/api/mikrotik',
                  data: {
                      'ipaddress': ipaddress, 'username': username, 'password': password === undefined ? "" : password
                  }
              };
              $http(config)
                  .then(function (response) {
                      callback(response);
                      var configDetails = {
                          method: 'POST',
                          url: 'http://localhost:8080/topology/api/mikrotik/details',
                          headers: {
                              'Content-Type': 'application/json'
                          },
                          data: {
                              'ipaddress': ipaddress, 'username': username, 'password': password === undefined ? "" : password
                          }
                      };
                      $http(configDetails)
                          .then(function (res) {
                            $rootScope.connectionDetails.routerid = res.data;
                            /* enable buttons */
                            for(var key in mappings){
                              Array.from(document.getElementsByClassName(key)).forEach(button => {
                                button.disabled = false;
                              });
                            }
                            
                            $location.path('/topology');
                          })
                          .catch(function(error){
                              console.log("ERROR RETRIEVING DATA: " + error);
                          });

                  })
                  .catch(function(error){
                    callback(error);
                  });
          }
        };
        return service;
    }])

/* connection controller */ 
connectModule.controller('connectionController',
    ['$scope', '$rootScope', '$location', 'AuthenticationService',
    function ($scope, $rootScope, $location, AuthenticationService) {
        $scope.login = function () {
            $scope.dataLoading = true;
            AuthenticationService.Login($scope.ipaddress, $scope.username, $scope.password, function(response) {
                if(parseInt(response.status) === 200) {
                    //$location.path('/topology');

                    /*setting root scope */
                    $rootScope.isDataLoaded = true;
                    $rootScope.connectionDetails = { ipaddress: $scope.ipaddress, time: (new Date()).toLocaleString(), routerid: null };
                } else {
                    $scope.error = "Error " + response.status + ": " + (response.data === null ? "Connection Error." : response.data);
                    $scope.dataLoading = false;
                    $rootScope.connectedRouter = null;
                }
            });
        };

        $scope.demo = {
            check: 'false'
        };

        $scope.check = function(){
            if($scope.demo.check){
                $scope.ipaddress = 'demo';
                $scope.username = 'demo';
                $scope.password = 'demo';
            }
            else {
              $scope.ipaddress = '';
              $scope.username = '';
              $scope.password = '';
            }
        };
}]);
