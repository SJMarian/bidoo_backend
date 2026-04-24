$token2 = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaXB0b0BnbWFpbC5jb20iLCJpYXQiOjE3NzcwNDg5ODcsImV4cCI6MTc3NzEzNTM4N30.VHvvI9TDKz5354Uhr4B7eAkINUUxf5KeqCe9hSDZPsg"
$token1 = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsZWJ1QGdtYWlsLmNvbSIsImlhdCI6MTc3NzA0OTE5MywiZXhwIjoxNzc3MTM1NTkzfQ.li0mSxnStGCu9jE69AjiNUKYHG1r26dOu2Eti-gZBhY"

$url = "http://localhost:8080/api/v1/bids"

$body1 = @{
    auctionItemId = 2
    bidAmount = 2000
} | ConvertTo-Json

$body2 = @{
    auctionItemId = 2
    bidAmount = 2000
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