<?php
 
$response = array();
 
$jsonInput = file_get_contents('php://input');

$json = json_decode( $jsonInput, true );

if ($json) {
    if ($json['config'] == "UnCryptDB")
    {
        require_once __DIR__ . '/UnCryptDB_CONFIG.php';
    }
    elseif ($json['config'] == "SSDB")
    {
        require_once __DIR__ . '/SSDB_CONFIG.php';
    }
    else
    {
        $response["success"] = 0;
        $response["message"] = "Config property not defined correctly.";
        echo json_encode($response);
        return;
    }

    $con = mysqli_connect(DB_SERVER,DB_USER,DB_PASSWORD,DB_DATABASE,DB_PORT);

    if (mysqli_connect_errno()) {
        $response["success"] = 0;
        $response["message"] = "Failed to connect to MySQL: " . mysqli_connect_error();
        echo json_encode($response);
        return;
    }

    foreach($json['dataset'] as $arrayVal)
    {
        // if value is not found, empty values
        // are inserted into the field
        $id = "AAAA22";
        $date = "??/??/??";
        $time = $arrayVal['time'];
        $value = $arrayVal['value'];
        $level = $json['datasetInterval'];
    
        $query = "INSERT INTO heart_rate(clientId, date, time, value, level) VALUES ('$id', '$date', '$time', '$value', '$level');";
        $result = mysqli_query($con, $query);
    }
 
    // check if row inserted or not
    if ($result) {
        // successfully inserted into database
        $response["success"] = 1;
        $response["message"] = "Product successfully created.";
 
        // echoing JSON response
        echo json_encode($response);
    } else {
        // failed to insert row
        $response["success"] = 0;
        $response["message"] = "Oops! An error occurred.";
 
        // echoing JSON response
        echo json_encode($response);
    }
} else {
    // required field is missing
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";

    // echoing JSON response
    echo json_encode($response);
}

?>
