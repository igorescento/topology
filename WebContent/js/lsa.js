var lsa = angular.module('lsa', []);

lsa.controller('LsaCtrl', function($rootScope, $scope, $http) {

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
      $http.get('../demo/demo_lsa.json')
          .then(function(res){
              $scope.items = res.data;
              $scope.totalRows = res.data.length;
          });
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
        })
        .catch(function(error){
            console.log("ERROR RETRIEVING LSA DATA: " + error);
        });
  };
})

/* custom filter implementation */
lsa.filter('searchFilter', function() {

    return function(input, option) {
        if (!option.type || !option.term) {
          console.log(input);
          console.log(option.type);
          console.log(option.term);
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

        if (sort.sortingOrder == newSortOr){
            sort.reverse = !sort.reverse;
        }

        sort.sortingOrder = newSortOr;
    };

    /* trigger between icons when sort applied */
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
