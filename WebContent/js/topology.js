"use strict";

var netModule = angular.module('topology', []);

netModule.controller('netgraph', function($rootScope, $scope, $http, $interval, $timeout) {

    $scope.buttonTitle = "Show Sync Tree";
    $scope.header = "Complete";
    $scope.responseReady = false;

    $scope.SwitchView = function () {
        $scope.buttonTitle = $scope.buttonTitle === "Show Sync Tree" ? "Show Topology" : "Show Sync Tree";
        $scope.header = $scope.header === "Complete" ? "Sync Tree" : "Complete";
    }

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
          if($scope.buttonTitle === "Show Sync Tree"){
                console.log("Displaying full network.");
                //poll data very often after table deletion
                $interval(function(){
                  if(!$scope.responseReady){
                    getFullNetwork();
                    console.log("Need show loading screen");
                    console.log($scope.responseReady);
                  }
                }, 500);

                // call get method every n seconds
                $scope.Timer = $interval(function() {
                    console.log(new Date());
                      $rootScope.connectionDetails.time = new Date().toLocaleString();
                      getFullNetwork();
                }, 6000);
            }

            else {
                console.log("Displaying sync tree, stopping the timer.");
                $interval.cancel($scope.Timer);
                getSyncTree();
            }

    }

    /* cancel timer when changing view */
    $scope.$on("$destroy",function(){
        if (angular.isDefined($scope.Timer)) {
            $interval.cancel($scope.Timer);
            console.log("Timer was cancelled.");
        }
    });

    /* Method to get the data from source DB */
    function getFullNetwork(){

        var config = {
            method: 'GET',
            url: 'http://localhost:8080/topology/api/topology/full'
        };

        $http(config)
            .then(function(response) {
                if(response.data.nodes.length !== 0 || response.data.edges.length !== 0){
                    processData(response.data, $rootScope.connectionDetails.routerid);
                    $scope.responseReady = true;
                    $scope.background = "#ffffff";
                }
            })
            .catch(function(error){
                console.log("Error retrieving full topology: " + error);
            });

    };

    /* Method to get the sync tree */
    function getSyncTree(){

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
                console.log("Error retrieving sync tree: " + error);
            });

    };
});

function processData(data, rId){
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

    /* remove the svg element and create new graph */
    if(d3.select("svg")){
        d3.select("svg").remove();
        d3.select(".njg-metadata").remove();
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
