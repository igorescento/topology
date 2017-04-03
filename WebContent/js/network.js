var network = angular.module('network', []);

network.controller('networksController', function($rootScope, $scope, $http, $location, $window) {

    var headers = [
      { name: "id", value:"ID" },
      { name: "netmask", value:"Mask" },
      { name: "routersid", value:"Routers" },
      { name: "numrouters", value:"# of Routers" },
      { name: "originator", value:"Originator" },
      { name: "firstaddr", value:"First IP" },
      { name: "lastaddr", value:"Last IP" },
      { name: "networkaddr", value:"Network IP" },
      { name: "broadcastaddr", value:"Broadcast IP" },
      { name: "ipavailable", value:"Available IPs" }
    ];

    $scope.singleSelect = headers;

    $scope.sort = {
              sortingOrder : 'id',
              reverse : false
          };

    /* load JSON with demo data */
    if($rootScope.isDemo){
        $http.get('demo/demo_network.json')
            .then(function(res){
                $scope.items = res.data;
                $scope.totalRows = res.data.length;
            })
            .catch(function(error){
              $window.alert("Error retrieving network data. Please try again.");
              $location.path('/connect');
          });

    }
    else {
      /* config to fetch live data from DB */
      var config = {
          method: 'GET',
          url: 'http://localhost:8080/topology/api/type/network'
      };
      $http(config)
          .then(function (response) {
              if(response.data.length > 0){
                  $rootScope.tempNetItems = $scope.items = response.data;
                  $rootScope.tempNetTotalRows = $scope.totalRows = response.data.length;

                  //$scope.items = response.data;
                  //$scope.totalRows = response.data.length;
              }
              else {
                  $scope.items = $rootScope.tempNetItems;
                  $scope.totalRows = $rootScope.tempNetTotalRows;
              }
          })
          .catch(function(error){
              w("Error retrieving network data. Please try again.");
              $location.path('/connect');

          });
    }

    /* own comparator to compare each octet of ip address in order to sort them correctly in the table */
    $scope.ipComparator = function(v1, v2) {
    // compare each octet as a number
    if (v1.type === 'string' && v2.type === 'string') {
      if(v1.value.split(".").length === 4 && v2.value.split(".").length === 4){
        if(parseInt(v1.value.split(".")[0]) < parseInt(v2.value.split(".")[0])) {
          return -1;
        }
        else if(parseInt(v1.value.split(".")[0]) > parseInt(v2.value.split(".")[0])) {
          return 1;
        }
        else {
          if(parseInt(v1.value.split(".")[1]) < parseInt(v2.value.split(".")[1])) {
            return -1;
          }
          else if(parseInt(v1.value.split(".")[1]) > parseInt(v2.value.split(".")[1])) {
            return 1;
          }
          else {
            if(parseInt(v1.value.split(".")[2]) < parseInt(v2.value.split(".")[2])) {
              return -1;
            }
            else if(parseInt(v1.value.split(".")[2]) > parseInt(v2.value.split(".")[2])) {
              return 1;
            }
            else {
              if(parseInt(v1.value.split(".")[3]) < parseInt(v2.value.split(".")[3])) {
                return -1;
              }
              else {
                return 1;
              }
            }
          }
        }
      }
      else {
        return v1.value.localeCompare(v2.value);
      }
    }
    else if(v1.type === 'number' && v2.type === 'number') {
        return (v1.value < v2.value) ? -1 : 1;
    }
    else {
        return (v1.index < v2.index) ? -1 : 1;
    }
  };

  });

/* custom filter implementation */
network.filter('searchFilter', function() {

    return function(input, option) {
        if (!option.type || !option.term) {
            return input;
        }
        var result = [];

        angular.forEach(input, function(value, key) {
            if(value[option.type].indexOf(option.term) > -1) {
                result.push(value);
              }
        });
        return result;
    };
});
