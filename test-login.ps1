$loginBody = @{
    email = "paularijit368@gmail.com"
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
