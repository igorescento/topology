var connectModule = angular.module('connect', []);

connectModule.factory('AuthenticationService',
    ['$http', '$rootScope', '$timeout',
    function ($http, $rootScope, $timeout, routerData) {
        var service = {};

        service.Login = function (ipaddress, username, password, callback) {

            /* Dummy authentication for demo mode, $timeout is used to simulate api call */
            if(ipaddress.toLowerCase() === "demo" && username.toLowerCase() === "demo" && password.toLowerCase() === "demo"){
                console.log("DEMO MODE");
                $rootScope.isDemo = true;

                $timeout(function(){
                    var response = {};
                    response.status = 200;
                    console.log("SUCCESSFUL AUTHENTICATION");
                    callback(response);
                }, 1000);
          }
          /* normal mode with real connection */
          else {
            $rootScope.isDemo = false;

            var config = {
              method: 'POST',
              url: 'http://localhost:8080/topology/api/mikrotik',
              data: {
                  'ipaddress': ipaddress, 'username': username, 'password': password === undefined ? "" : password
              },

           };
            $http(config)
                .then(function (response) {
                    console.log(response);
                    callback(response);
                })
                .catch(function(error){
                  console.log(error);
                  callback(error);
                });

          }
        };
        return service;
    }])
  
connectModule.controller('connectionController',
    ['$scope', '$rootScope', '$location', 'AuthenticationService',
    function ($scope, $rootScope, $location, AuthenticationService) {
        $scope.login = function () {
            $scope.dataLoading = true;
            AuthenticationService.Login($scope.ipaddress, $scope.username, $scope.password, function(response) {
  console.log("AAA");
    console.log(response);
                if(parseInt(response.status) === 200) {
                    $location.path('/topology');

                    /*setting root scope */
                    $rootScope.isDataLoaded = true;

                    /* enable buttons */
                    console.log("Enabling buttons");
                    for(var key in mappings){
                      Array.from(document.getElementsByClassName(key)).forEach(button => {
                        button.disabled = false;
                      });
                    }
                } else {
                    $scope.error = "Error " + response.status + ": " + response.data;
                    $scope.dataLoading = false;
                }
            });
        };
}]);
