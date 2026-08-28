$loginBody = @{
    email = "paularijit368@gmail.com"
    password = "password123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"

$token = $loginResponse.token
Write-Host "Token: $token"

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

$correctionBody = @{
    targetType = "VEHICLE_REGISTRATION_APPLICATION"
    targetId = 2
    fieldName = "applicantName"
    currentValue = "Arijit"
    requestedValue = "Arijit"
    reason = "Please correct it was a mistake by the office"
    evidenceBase64 = "mock_base64_string"
} | ConvertTo-Json

try {
    $correctionResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/corrections" -Method Post -Body $correctionBody -Headers $headers
    Write-Host "Correction Response: "
    $correctionResponse | ConvertTo-Json
} catch {
    Write-Host "Error: $($_.Exception.Message)"
    if ($_.ErrorDetails) {
        Write-Host "Details: $($_.ErrorDetails.Message)"
    }
}
