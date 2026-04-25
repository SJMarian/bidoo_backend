$token2 = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsZWJ1QGdtYWlsLmNvbSIsImlhdCI6MTc3NzA1MzI4MywiZXhwIjoxNzc3MTM5NjgzfQ.AIvmLmeXq0MfX0d_oEsIYFvW6I1Dwa9TyGue9UZc4Iw"
$token1 = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYWJyaW5hQGdtYWlsLmNvbSIsImlhdCI6MTc3NzA1MzIwOSwiZXhwIjoxNzc3MTM5NjA5fQ.y9cmu4uKb8jcimBJ6cthw_OIw9czWfUwRDLY7WfEtxk"

$url = "http://localhost:8080/api/v1/bids"

$body1 = @{
    auctionItemId = 2
    bidAmount = 200
} | ConvertTo-Json

$body2 = @{
    auctionItemId = 2
    bidAmount = 200
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