<?php
	$file_path = "/home/emission/uploads/";
	if (empty($_FILES['uploaded_file']) || empty($_GET['token'])) {
		echo "you should not be here<br>";
		return;
	}
	$vin           = "";
	$unixTimestamp = time();
	
	
	if ($_FILES['uploaded_file']['error'] == UPLOAD_ERR_OK && is_uploaded_file($_FILES['uploaded_file']['tmp_name'])) { //checks that file is uploaded
		$line          = fgets(fopen($_FILES['uploaded_file']['tmp_name'], 'r'));
		$info          = json_decode($line);
		$vin           = $info->{'trip-info'}->{'VIN'};
		$unixTimestamp = $info->{'trip-info'}->{'timestamp'};
	}
	
	$date       = date("Y-m-d H:i:s", $unixTimestamp/1000); //division by 1k because java uses ms rather than s
	$filename   = md5(basename($_FILES['uploaded_file']['name']));
	$file_path  = $file_path . $filename;
	$hostname   = "127.0.0.1";
	$database   = "emission";
	$dbusername = "emission";
	$pass       = "Em1ssion!123"; //localhost only
	
	$conn = new mysqli($hostname, $dbusername, $pass, $database);
	// Check connection
	if ($conn->connect_error) {
		die("Connection failed: " . $conn->connect_error);
	}
	$token = mysqli_real_escape_string($conn, $_GET["token"]);
	if (move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $file_path)) {
		$sql = "INSERT INTO storage (token, VIN, filename, date) VALUES ('$token', '$vin', '$filename', '$date')";
		if ($conn->query($sql) === TRUE) {
			echo "New record created successfully\n";
		} else {
			echo "Error: " . $sql . "<br>" . $conn->error;
		}
		echo "success";
	} else {
		echo "failure";
	}
?>
