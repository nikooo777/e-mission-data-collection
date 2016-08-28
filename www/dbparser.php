<?php
	if (empty($_GET['token'])){
		echo "you should not be here<br>";
		return;
	}
	$hostname = "127.0.0.1";
	$database = "emission";
	$dbusername = "emission";
	$pass = "Em1ssion!123"; //localhost only
	
	$conn = new mysqli($hostname, $dbusername, $pass, $database);
	// Check connection
	if ($conn->connect_error) {
		die("Connection failed: " . $conn->connect_error);
	} 
	$token = mysqli_real_escape_string($conn,$_GET["token"]);

	$sql = "select VIN, date, filename from storage WHERE token = '$token';";
	$rows=$conn->query($sql);
	$data = array();
	if ($rows->num_rows > 0) 
	{
		// output data of each row
		while($row = $rows->fetch_assoc()) 
		{
			$data['trip'][] = $row;
		}
	} 
	$json = json_encode($data, JSON_PRETTY_PRINT);
	print $json;
	$conn->close();
?>