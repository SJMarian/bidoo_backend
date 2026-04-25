$token2 = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsZWJ1QGdtYWlsLmNvbSIsImlhdCI6MTc3NzA5NjQ0NywiZXhwIjoxNzc3MTgyODQ3fQ.YicEcmGWg5s3Mh1B88Jo8fB36TbUmeRiyoCbEhTftLw"
$token1 = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYWJyaW5hQGdtYWlsLmNvbSIsImlhdCI6MTc3NzA5NzE4NSwiZXhwIjoxNzc3MTgzNTg1fQ.TcUbDOWKsl1CGLsouOMs7KWEtbvGtI7mDSh2m6U95x0"
$url = "http://localhost:8080/api/v1/bids"

$body1 = @{
    auctionItemId = 23
    bidAmount = 10501
} | ConvertTo-Json

$body2 = @{
    auctionItemId = 23
    bidAmount = 10501
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