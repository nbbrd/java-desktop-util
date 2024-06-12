# powershell -file winsearch.ps1

If ($args.Count -ne 1) {
    Write-Output "usage ..."
    Exit(0)
}

$query = $args[0]

$con = New-Object -ComObject ADODB.Connection
try {
    $con.Open("Provider=Search.CollatorDSO;Extended Properties='Application=Windows';")

    $rs  = New-Object -ComObject ADODB.Recordset
    try {
        $rs.Open("SELECT System.ItemUrl FROM SYSTEMINDEX WHERE SCOPE='file:' AND System.FileName like '%$query%'", $con)

        While (-Not $rs.EOF) {
            $rs.Fields.Item(0).Value.Replace("file:", "")
            $rs.MoveNext()
        }
    } catch {
        Write-Output "Query failed: [$($_.Exception.GetType().FullName)] $_.Message"
        Exit(2)
    } finally {
        if ($rs.State -eq [System.Data.ConnectionState]::Open) { $rs.Close() }
    }
} catch {
    Write-Output "Connection failed: [$($_.Exception.GetType().FullName)] $_.Message"
    Exit(1)
} finally {
    if ($con.State -eq [System.Data.ConnectionState]::Open) { $con.Close() }
}

