<!DOCTYPE html>
<html>
  <head>
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
    <title>Simple Map</title>
    <meta name="viewport" content="initial-scale=1.0">
    <meta charset="utf-8">
    <style>
      html, body {
        height: 100%;
        margin: 0;
        padding: 0;
      }
      #map {
        height: 100%;
      }
    </style>
  </head>
  <body>
    <div id="map"></div>
	<p>test</p>
    <script>
	  var resolution = 2;
      var map;
      function initMap() {
        map = new google.maps.Map(document.getElementById('map'), {
          center: {lat: 46.02387067793921, lng: 8.917525438530165},
          zoom: 11
        });
		var flightPlanCoordinates =new Array();	
		$.ajax({
			url:'<?php 
			echo "uploads/" . htmlspecialchars($_GET["file"]); 
			?>',
			success: function (data){
				var re=/\r\n|\n\r|\n|\r/g;
				var arrayofLines=data.replace(re,"\n").split("\n");
				var vehicleInfo = JSON.parse(arrayofLines[0]);
				console.log(vehicleInfo['trip-info'].VIN);
				
				//start after the vehicle info (index 0)
				for (i = 1 ; i < arrayofLines.length ; i++)
				{
					if (!arrayofLines[i] ||arrayofLines[i] ==="")
						continue;
					console.log(arrayofLines[i]);
					var tripData = JSON.parse(arrayofLines[i]);
					console.log(tripData);
					var coords = new google.maps.LatLng(tripData['dev-coords'][0],tripData['dev-coords'][1]);
					flightPlanCoordinates.push(coords);
					//console.log(
					if (i%resolution === 0)
					{
						//console.log(i);
						//var coords = new google.maps.LatLng(splitdata[0],splitdata[1]);
						var contentString = '<div id="content">'+
						'<div id="siteNotice">'+
						'</div>'+
						'<h1 id="firstHeading" class="firstHeading">E-Mission details</h1>'+
						'<div id="bodyContent">'+
						'<p><b>Speed: </b>'+ tripData['dev-speed'] +'km/h</p>'+
						'<p><b>Travelled Distance: </b>'+ tripData.OD +' km</p>'+
						'<p><b>Consumed Fuel: </b>'+ tripData.FC +'</p>'+
						'<p><b>Avg fuel rate: </b>'+ tripData.FEC +'</p>'+
						'<p><b>Instant fuel rate: </b>'+ tripData.FUEL +'</p>'+
						'<p><b>RPM: </b>'+ tripData.RPM +'</p>'+
						'</div>'+
						'</div>';
						
						var infowindow = new google.maps.InfoWindow({
							content: contentString
						});
						
						var marker = new google.maps.Marker({
							position: coords,
							map: map,
							title: '#'+i
						});
						google.maps.event.addListener(marker,'click', (function(marker,contentString,infowindow){ 
							return function() {
								infowindow.setContent(contentString);
								infowindow.open(map,marker);
							};
						})(marker,contentString,infowindow));  
						
						/*marker.addListener('click', function() {
							infowindow.open(map, marker);
						});*/
					}
				}
				
				var flightPath = new google.maps.Polyline({
					path: flightPlanCoordinates,
					geodesic: true,
					strokeColor: '#FF0000',
					strokeOpacity: 1.0,
					strokeWeight: 2
				});
				flightPath.setMap(map);
			}
		});

		
		


      }
	
    </script>
    <script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyB51TWcSKOWWHYeuaE1NiUQ56LkiVJxXoU&callback=initMap"
    async defer></script>
  </body>
</html>
