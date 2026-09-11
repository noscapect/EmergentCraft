$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..\brain')
if (-not (Test-Path node_modules)) { npm install }
npm run dev
