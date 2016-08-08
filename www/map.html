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
    <script>
	  var resolution = 5;
      var map;
      function initMap() {
        map = new google.maps.Map(document.getElementById('map'), {
          center: {lat: 46.02387067793921, lng: 8.917525438530165},
          zoom: 11
        });
		var flightPlanCoordinates =new Array();	
		$.ajax({
			url:'test.txt',
			success: function (data){
				var re=/\r\n|\n\r|\n|\r/g;
				var arrayofLines=data.replace(re,"\n").split("\n");
				
				for (i = 0 ; i < arrayofLines.length ; i++)
				{
					var splitdata = arrayofLines[i].split(',');
					if (splitdata.length !== 2)
						continue;
					console.log(splitdata[0],splitdata[1]);
					flightPlanCoordinates.push(new google.maps.LatLng(splitdata[0],splitdata[1]));
					//console.log(
					if (i%resolution === 0)
					{
						//console.log(i);
						var coords = new google.maps.LatLng(splitdata[0],splitdata[1]);
						var contentString = '<div id="content">'+
						'<div id="siteNotice">'+
						'</div>'+
						'<h1 id="firstHeading" class="firstHeading">E-Mission details</h1>'+
						'<div id="bodyContent">'+
						'<p><b>Speed: </b>'+ 0 +'km/h</p>'+
						'<p><b>Travelled Distance: </b>'+ 0 +' km</p>'+
						'<p><b>Consumed Fuel: </b>'+ 0 +' L</p>'+
						'<p><b>Avg fuel rate: </b>'+ 0 +' L/100km</p>'+
						'<p><b>Instant fuel rate: </b>'+ 0 +'</p>'+
						'<p><b>RPM: </b>'+ 0 +'</p>'+
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
