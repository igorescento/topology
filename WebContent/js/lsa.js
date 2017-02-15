var lsa = angular.module('lsa', []);

lsa.controller('LsaCtrl', function($scope) {

  $scope.orderByField = 'firstName';
  $scope.reverseSort = false;

  $scope.data = {
    employees: [{
      firstName: 'John',
      lastName: 'Doe',
      age: 30
    },{
      firstName: 'Frank',
      lastName: 'Burns',
      age: 54
    },{
      firstName: 'Sue',
      lastName: 'Banter',
      age: 21
    }]
  };

  /* load JSON with demo data */
  if($rootScope.isDemo){
      $http.get('../demo/demo_lsa.json')
     .then(function(res){
        $scope.topology = res.data;

        d3.netJsonGraph( $scope.topology , {
           linkDistance: 200,
           charge: -200,
           circleRadius: 12,
           defaultStyle: false,
           labelDy: "-1.8em"
           });
      });
  }
  else {
    
  }
});
