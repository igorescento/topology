"use strict";

var netModule = angular.module('topology', []);

netModule.controller('netgraph', function($rootScope, $scope, $http, $interval) {

    /* load JSON with demo data */
    if($rootScope.isDemo){
        $http.get('../demo/demo_topology.json')
            .then(function(res){
                $scope.topology = res.data;

                d3.netJsonGraph($scope.topology , {
                    linkDistance: 50,
                    charge: -200,
                    circleRadius: 12,
                    defaultStyle: false,
                    labelDy: "-1.8em"
                });
            });
    }

    /* live data */
    else {
        getData();
        /* call get method every n seconds */
        $scope.Timer = $interval(function() {
            console.log(new Date());
              $rootScope.connectionDetails.time = new Date().toLocaleString();
              getData();
        }, 60000);

        /* cancel timer when changing view */
        $scope.$on("$destroy",function(){
            if (angular.isDefined($scope.Timer)) {
                $interval.cancel($scope.Timer);
                console.log("Timer was cancelled.");
            }
        });
    };

    /* Method to get the data from source DB */
    function getData(){

        var config = {
            method: 'GET',
            url: 'http://localhost:8080/topology/api/topology'
        };

        $http(config)
            .then(function(response) {
                if(response.data.edges.length !== 0 && response.data.edges.length !== 0){
                    processData(response.data, $rootScope.connectionDetails.routerid);
                }
                else{
                  console.log(response.data);
                }
            })
            .catch(function(error){
                console.log("Error retrieving data: " + error);
            });

    };
});

/* module to switch between sync tree view and topology */
netModule.controller('SwitchView', function ($scope, $http, $rootScope) {
    $scope.title = "Sync Tree";
    $scope.header = "Complete";

    $scope.SwitchView = function () {
        $scope.title = $scope.title === "Sync Tree" ? "Full Topology" : "Sync Tree";
        $scope.header = $scope.header === "Complete" ? "Sync Tree" : "Complete";

        if($scope.title === "Full Topology"){

            var config = {
                method: 'GET',
                url: 'http://localhost:8080/topology/api/topology/synctree/'+$rootScope.connectionDetails.routerid
            };
            $http(config)
                .then(function (response) {
                  console.log(response);
                  processData(response.data, $rootScope.connectionDetails.routerid);
                })
                .catch(function(error){
                    console.log("ERROR RETRIEVING DATA: " + error);
                });
        }

      else {
        console.log("WHERE IS FULL NETWORK GONE?");
      }

    }
});

function processData(data, rId){
    console.log("Loading live material.");
    /* initializing variables */
    var nodes = new Set(),
        newNodes = [],
        links = [],
        otherLinks = [],
        newLinks,
        topology = {
            "type": "",
            "label":"",
            "protocol":"",
            "version": "",
            "revision": "",
            "metric": "",
            "router_id":"",
            "nodes": "",
            "links": ""
        };

    data.nodes.forEach(function(node){
        node.color = "default";
        console.log(node.id + " " + rId);
        if(node.id === rId){
            node.color = "green";
        }
    })

    newNodes = data.nodes;
    data.edges.forEach(function(row){
        links.push({ "source": row.source.id, "target": row.destination.id, "cost": row.metric, "deleted": false});
    })

    /* check for same nodes / targets - need to be removed*/
    for(var i = 0; i < links.length; i++){
        for(var j = i + 1; j < links.length; j++){
            if(links[i].source === links[j].target){
                if(links[i].target === links[j].source){
                    links[j].deleted = true;
                }
            }
        }
    }

    topology.nodes = newNodes;
    /* remove duplicate routes, connections */
    topology.links = links.filter(function(del){
        return (del.deleted === false);
    });

    if(d3.select("svg")){
      console.log("REMOVING SVG");
      d3.select("svg").remove();
    }

    d3.netJsonGraph( topology , {
        linkDistance: 50,
        charge: -200,
        circleRadius: 12,
        defaultStyle: false,
        linkClassProperty: "type",
        nodeClassProperty: "gateway",
        labelDy: "-1.8em"
    });
}
