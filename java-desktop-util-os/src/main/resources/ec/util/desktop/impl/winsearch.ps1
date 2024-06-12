# powershell -file winsearch.ps1

If ($args.Count -ne 1) {
    Write-Output "usage ..."
    Exit(0)
}

$query = $args[0]

$con = New-Object -ComObject ADODB.Connection
$con.Open("Provider=Search.CollatorDSO;Extended Properties='Application=Windows';")

$rs  = New-Object -ComObject ADODB.Recordset
$rs.Open("SELECT System.ItemUrl FROM SYSTEMINDEX WHERE SCOPE='file:' AND System.FileName like '%$query%'", $con)

While (-Not $rs.EOF) {
    $rs.Fields.Item(0).Value.Replace("file:", "")
    $rs.MoveNext()
}

$rs.Close()
$con.Close()
