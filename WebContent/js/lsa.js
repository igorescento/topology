var lsa = angular.module('lsa', []);

lsa.controller('lsaController', function($rootScope, $scope, $http, $location) {

  var headers = [
    { name: "id", value:"ID" },
    { name: "instance", value:"Instance" },
    { name: "area", value:"Area"},
    { name: "type", value:"Type"},
    { name: "originator", value:"Originator"},
    { name: "body", value:"Body"}
  ];

  $scope.singleSelect = headers;

  $scope.sort = {
            sortingOrder : 'id',
            reverse : false
        };

  /* load JSON with demo data */
  if($rootScope.isDemo){
      //$http.get('../demo/demo_lsa.json')
        $http.get('demo/demo_lsa.json')
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
    console.log("Live LSA table");
    /* config to fetch live data from DB */
    var config = {
        method: 'GET',
        url: 'http://localhost:8080/topology/api/mikrotik/lsa'
    };
    $http(config)
        .then(function (response) {
            $scope.items = response.data;
            $scope.totalRows = response.data.length;
        })
        .catch(function(error){
            console.log("ERROR RETRIEVING LSA DATA: " + error);
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
lsa.filter('searchFilter', function() {
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

lsa.directive("customSort", function() {
return {
    restrict: 'A',
    transclude: true,
    scope: {
      order: '=',
      sort: '='
    },
    template :
      ' <a ng-click="sort_by(order)" style="color: #555555;">'+
      '    <span ng-transclude></span>'+
      '    <i ng-class="selectedCls(order)"></i>'+
      '</a>',
    link: function(scope) {

    /* sort order change */
    scope.sort_by = function(newSortOr) {
        var sort = scope.sort;
        if(newSortOr === "id" || newSortOr === "originator"){

        }
        if (sort.sortingOrder == newSortOr){
            sort.reverse = !sort.reverse;
        }

        sort.sortingOrder = newSortOr;
    };

    /* trigger between icons when sort applied and switch order */
    scope.selectedCls = function(column) {
        if(column == scope.sort.sortingOrder){
            return ('icon_sort_' + ((scope.sort.reverse) ? 'desc' : 'asc'));
        }
        else{
            return'icon_sort'
        }
    };
  }
}
});
