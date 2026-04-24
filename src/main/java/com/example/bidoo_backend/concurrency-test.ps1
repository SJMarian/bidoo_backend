$token1 = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMkBnbWFpbC5jb20iLCJpYXQiOjE3NzcwMzQ5ODQsImV4cCI6MTc3NzEyMTM4NH0.ylI135lvT7DLnhq74w9jwtMOKg1UWF6pXZazklHmv_E"
$token2 = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMUBnbWFpbC5jb20iLCJpYXQiOjE3NzcwMzQ5NTQsImV4cCI6MTc3NzEyMTM1NH0.GAAHpDTbv8zF_rSvgS5LAxxrkCOjN9RV1nN727czvOY"

$url = "http://localhost:8080/api/v1/bids"

$body1 = @{
    auctionItemId = 1
    bidAmount = 1500
} | ConvertTo-Json

$body2 = @{
    auctionItemId = 1
    bidAmount = 1500
} | ConvertTo-Json

$job1 = Start-Job -ScriptBlock {
    param($url, $token, $body)

    Invoke-RestMethod `
        -Uri $url `
        -Method Post `
        -Headers @{ Authorization = "Bearer $token" } `
        -ContentType "application/json" `
        -Body $body
} -ArgumentList $url, $token1, $body1

$job2 = Start-Job -ScriptBlock {
    param($url, $token, $body)

    Invoke-RestMethod `
        -Uri $url `
        -Method Post `
        -Headers @{ Authorization = "Bearer $token" } `
        -ContentType "application/json" `
        -Body $body
} -ArgumentList $url, $token2, $body2

Receive-Job -Job $job1,$job2 -Wait -AutoRemoveJob