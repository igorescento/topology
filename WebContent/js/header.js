var headerModule = angular.module('header',['ngRoute']);

/* button to link mappings */
var mappings = {
  "button-connect": "connect",
  "button-topology": "network",
  "button-lsa": "lsa",
  "button-router": "routers",
  "button-network": "nets",
  "button-export": "export"
};

/* live time controller */
headerModule.controller('TimeCtrl', function($scope, $interval) {
    var tick = function() {
      $scope.clock = Date.now();
    };

    tick();
    $interval(tick, 1000);
});

/* button click controller to display html */
headerModule.controller('ButtonClick', function ($scope, $rootScope, $location, $http, $window) {
  $scope.ButtonClick = function (value) {

    if(value.currentTarget.className === "button-export"){
        $scope.svg = d3.select("svg").node() === null ? null : d3.select("svg").node();
        //var svg = d3.select("svg").node();

        /* config to fetch live data from DB */
        var config = {
            method: 'POST',
            url: 'http://localhost:8080/topology/api/generate/report',
            headers: {
                'Content-Type': 'text/plain'
            },
            data: $scope.svg === null ? null : getSVGString($scope.svg)
        };
        $http(config)
            .then(function (response) {
                download(response.data);
            })
            .catch(function(error){
                $window.alert("Error occured while generating report. Please login and try again.");
            });
    }

    else {
        for(var key in mappings){
            if(value.currentTarget.className === key){
                $location.url(mappings[key]);
            }
        }
    }

    /*for(var key in mappings){
      if(value.currentTarget.className === key){
        found = true;
        if(mappings[key] === "export"){
            var svg = d3.select("svg").node();
            var svgString = getSVGString(svg);*/

            /* config to fetch live data from DB */
            /*var config = {
                method: 'POST',
                url: 'http://localhost:8080/topology/api/generate/report',
                headers: {
                    'Content-Type': 'text/plain'
                },
                data: getSVGString(svg)
            };
            $http(config)
                .then(function (response) {
                    download(response.data);
                })
                .catch(function(error){
                });
        }
        else {
            $location.url(mappings[key]);
        }
      }
    }*/
  };

  /* return only Excel file in case not in topology screen */
  function download(id) {
      if($scope.svg !== null){
          $window.open('http://localhost:8080/topology/api/generate/download/' + id);
          $window.open('http://localhost:8080/topology/api/generate/downloadsvg/');
      }
      else {
          $window.open('http://localhost:8080/topology/api/generate/download/' + id);
      }
  }

  /** a method to extract the SVG string from D3 SVG file */
  function getSVGString(svgNode) {
  	svgNode.setAttribute('xlink', 'http://www.w3.org/1999/xlink');
  	var cssStyleText = getCSSStyles(svgNode);
  	appendCSS(cssStyleText, svgNode);

  	var serializer = new XMLSerializer();
  	var svgString = serializer.serializeToString(svgNode);
  	svgString = svgString.replace(/(\w+)?:?xlink=/g, 'xmlns:xlink='); // Fix root xlink without namespace
  	svgString = svgString.replace(/NS\d+:href/g, 'xlink:href'); // Safari NS namespace fix

  	return svgString;

  	function getCSSStyles(parentElement) {
  		var selectorTextArr = [];

  		// Add Parent element Id and Classes to the list
  		selectorTextArr.push('#' + parentElement.id);
  		for (var c = 0; c < parentElement.classList.length; c++)
  				if (!contains('.' + parentElement.classList[c], selectorTextArr))
  					   selectorTextArr.push( '.' + parentElement.classList[c]);

  		// Add Children element Ids and Classes to the list
  		var nodes = parentElement.getElementsByTagName("*");
  		for (var i = 0; i < nodes.length; i++) {
    			var id = nodes[i].id;
    			if (!contains('#'+id, selectorTextArr))
    				selectorTextArr.push( '#' + id);

    			var classes = nodes[i].classList;
    			for (c = 0; c < classes.length; c++)
      				if (!contains('.' + classes[c], selectorTextArr))
      					   selectorTextArr.push( '.' + classes[c]);
  		}

  		// Extract CSS Rules
  		var extractedCSSText = "";
  		for (i = 0; i < document.styleSheets.length; i++) {
  			var s = document.styleSheets[i];

  			try {
  			    if(!s.cssRules) continue;
  			} catch( e ) {
  		    		if(e.name !== 'SecurityError') throw e; // for Firefox
  		    		continue;
  		    	}

  			var cssRules = s.cssRules;
  			for (var r = 0; r < cssRules.length; r++) {
    				if (contains( cssRules[r].selectorText, selectorTextArr ))
    					   extractedCSSText += cssRules[r].cssText;
  			}
  		}
  		return extractedCSSText;

  		function contains(str, arr) {
  			   return arr.indexOf(str) === -1 ? false : true;
  		}

  	}

  	function appendCSS(cssText, element) {
    		var styleElement = document.createElement("style");
    		styleElement.setAttribute("type", "text/css");
    		styleElement.innerHTML = cssText;
    		var refNode = element.hasChildNodes() ? element.children[0] : null;
    		element.insertBefore(styleElement, refNode);
  	}
  }
});
