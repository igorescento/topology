var router = angular.module('router', []);

router.controller('routersController', function($rootScope, $scope, $http, $location) {

  var headers = [
    { name: "id", value:"ID" },
    { name: "linktype", value:"Link Type"},
    { name: "bodyid", value:"Body ID"},
    { name: "metric", value:"Metric"}
  ];

  $scope.singleSelect = headers;

  $scope.sort = {
            sortingOrder : 'id',
            reverse : false
        };

  /* load JSON with demo data */
  if($rootScope.isDemo){
      $http.get('../demo/demo_router.json')
          .then(function(res){
              $scope.items = res.data;
              $scope.totalRows = res.data.length;
          })
          .catch(function(error){
            console.log(error);
            $location.path('/connect');
        })

  }
  else {
    console.log("Live Router table");
    /* config to fetch live data from DB */
    var config = {
        method: 'GET',
        url: 'http://localhost:8080/topology/api/type/router'
    };
    $http(config)
        .then(function (response) {
            $scope.items = response.data;
            $scope.totalRows = response.data.length;
        })
        .catch(function(error){
            console.log("ERROR RETRIEVING ROUTER DATA: " + error);
            $location.path('/connect');

        });
  };

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
  else {
    return (v1.index < v2.index) ? -1 : 1;
  }
};

})

/* custom filter implementation */
router.filter('searchFilter', function() {

    return function(input, option) {
        if (!option.type || !option.term) {
            return input;
        }
        var result = [];

        angular.forEach(input, function(value, key) {
            if(value[option.type].indexOf(option.term) > -1) {
                result.push(value);
              }
        })
        return result;
    }
});
