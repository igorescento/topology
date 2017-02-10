var netModule = angular.module('topology', []);

netModule.controller('netgraph', function($rootScope, $scope, $http) {

    /* load JSON with demo data */
    if($rootScope.isDemo){
        $http.get('../demo/demo.json')
       .then(function(res){
          $scope.topology = res.data;

          d3.netJsonGraph( $scope.topology , {
             linkDistance: 200,
             charge: -200,
             circleRadius: 12,
             defaultStyle: false,
             linkClassProperty: "type",
             nodeClassProperty: "gateway",
             labelDy: "-1.8em"

             });
        });
    }

  /*  $http.get("http://localhost:8080/topology/api/topology")
   .then(function (response, data, headers, status, config) {
     $scope.names = response.data;
     var topology = {
      "type": "NetworkGraph",
      "label": "Summary",
      "protocol": "OSPF",
      "version": "0.6.6.2",
      "revision": "abcdef",
      "metric": "1111111",
      "router_id": "10.255.255.1",
      "nodes": [
  {"id":"10.255.255.1","label":"10.255.255.1"},{"id":"10.255.255.2","label":"10.255.255.2"},{"id":"10.255.255.3","label":"10.255.255.3"},{"id":"10.255.255.4","label":"10.255.255.4"},{"id":"10.255.255.5","label":"10.255.255.5"},{"id":"10.255.255.7","label":"10.255.255.7"},{"id":"10.255.255.8","label":"10.255.255.8"},{"id":"10.255.255.6","label":"10.255.255.6"}
      ],
      "links": [
          {"source":"10.255.255.1","target":"10.255.255.2","cost":10},{"source":"10.255.255.1","target":"10.255.255.3","cost":10},{"source":"10.255.255.3","target":"10.255.255.4","cost":10},{"source":"10.255.255.4","target":"10.255.255.5","cost":10},{"source":"10.255.255.5","target":"10.255.255.1","cost":10},{"source":"10.255.255.5","target":"10.255.255.7","cost":10},{"source":"10.255.255.4","target":"10.255.255.8","cost":10},{"source":"10.255.255.6","target":"10.255.255.2","cost":10}
      ]
  };
});*/
});
