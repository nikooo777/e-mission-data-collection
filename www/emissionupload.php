<?php
    $file_path = "/home/emission/uploads/";
	if (empty($_FILES['uploaded_file']) || empty($_GET['token']){
		echo "you should not be here<br>";
		return;
	}
	$filename = md5(basename( $_FILES['uploaded_file']['name']));
    $file_path = $file_path . $filename;
	$hostname = "127.0.0.1";
	$database = "emission";
	$dbusername = "emission";
	$pass = "Em1ssion!123"; //localhost only
	
	
	$vin = "VIN123";
	$unixTimestamp = time();
	$date = date("Y-m-d H:i:s", $unixTimestamp);
	//$date = date("d.m.Y H:i:s")
	
	
	$conn = new mysqli($hostname, $dbusername, $pass, $database);
	// Check connection
	if ($conn->connect_error) {
		die("Connection failed: " . $conn->connect_error);
	} 
	$token = mysqli_real_escape_string($conn,$_GET["token"]);
    if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $file_path) ){
		$sql = "INSERT INTO storage (token, VIN, filename, date) VALUES ('$token', '$vin', '$filename', '$date')";
		if ($conn->query($sql) === TRUE) {
			echo "New record created successfully";
		} else {
			
			echo "Error: " . $sql . "<br>" . $conn->error;
		}
        echo "success";
    } else{
        echo "failure";
    }
?>
