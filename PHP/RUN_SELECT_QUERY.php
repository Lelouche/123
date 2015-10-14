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

    if (isset($json['query']))
    {
        $query = $json['query'];
    }
    else
    {
        $response["success"] = 0;
        $response["message"] = "Query is missing";
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

    $result = mysqli_query($con, $query);
    $result_array = array();
    while($r = mysqli_fetch_assoc($result)) {
        $result_array[] = $r;
    }

    $response["success"] = 1;
    $response["result"] = $result_array;
    echo json_encode($response);

} else {
    // required field is missing
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";

    echo json_encode($response);
}
?>