$loginBody = @{
    email = "johndoe@example.com"
    password = "password123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "Token: $token"
    
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }

    Write-Host "Submitting correction..."
    $correctionBody = @{
        targetType = "VEHICLE"
        targetId = 1
        fieldName = "nickname"
        currentValue = "My Nexon"
        requestedValue = "John's Nexon"
        reason = "I want to change the nickname"
    } | ConvertTo-Json

    $createResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/corrections" -Method Post -Body $correctionBody -Headers $headers
    $createResponse | ConvertTo-Json

    Write-Host "Fetching my corrections..."
    $response = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/corrections/mine" -Method Get -Headers $headers
    Write-Host "Success:"
    $response | ConvertTo-Json
} catch {
    Write-Host "Error occurred."
    if ($_.ErrorDetails) {
        Write-Host "Response Body:"
        Write-Host $_.ErrorDetails.Message
    } else {
        Write-Host $_.Exception.Message
    }
}
