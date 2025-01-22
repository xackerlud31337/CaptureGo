# PowerShell Script to Connect 100 Clients to localhost:4444

# Number of clients to create
$num_clients = 1000

# Host and port
$targetHost = "localhost"
$port = 4444

# Loop to create clients
for ($i = 1; $i -le $num_clients; $i++) {
    # Generate unique username
    $username = "User$i"

    # Start a new ncat client in a separate process and keep the connection alive
    Start-Process -NoNewWindow -FilePath "cmd.exe" -ArgumentList "/c (echo HELLO && echo LOGIN~$username) | ncat $targetHost $port"
}

Write-Host "All clients connected and logged in."
