$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot 'backend')
$env:DB_USERNAME = Read-Host 'Ten tai khoan MySQL (vi du student_v2)'
$dbSecret = Read-Host 'Mat khau MySQL' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $dbSecret).Password
$env:DB_URL = 'jdbc:mysql://localhost:3306/student_manager_v2?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh'
$seedDemo = Read-Host 'Nhap YES de tao du lieu mau khi database moi hoan toan'
$env:APP_SEED = if ($seedDemo -ceq 'YES') { 'true' } else { 'false' }
try {
    Write-Host 'Sau khi thay Started Application, mo http://localhost:8081'
    & java -jar '..\release\student-manager-2.0.0.jar' --spring.profiles.active=mysql --server.port=8081
} finally {
    Remove-Item Env:DB_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item Env:APP_SEED -ErrorAction SilentlyContinue
}
