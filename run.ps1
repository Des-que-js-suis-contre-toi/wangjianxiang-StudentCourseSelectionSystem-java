# Build and launch the course selection system.
# Data files courses.txt / students.txt are written to the project root (current directory).
# NOTE: keep this file ASCII-only. Windows PowerShell 5.1 misparses UTF-8 scripts without BOM.
$root = if ($PSScriptRoot) { $PSScriptRoot } else { (Get-Location).Path }
Push-Location $root
try {
    $outDir = Join-Path $root 'out'
    New-Item -ItemType Directory -Force -Path $outDir | Out-Null

    $sources = @(Get-ChildItem -Path (Join-Path $root 'src') -Recurse -Filter *.java |
        ForEach-Object { $_.FullName })
    if ($sources.Count -eq 0) {
        Write-Host 'no java sources found under src'
        exit 1
    }

    # Pass the file list directly: @argfile written by PowerShell 5.1 may carry a UTF-8 BOM
    javac -encoding UTF-8 -d $outDir $sources
    if ($LASTEXITCODE -ne 0) {
        Write-Host 'compile failed, see the errors above'
        exit 1
    }

    # No forced output encoding: JDK 18+ follows the console code page, which keeps CJK readable
    java -cp $outDir course.Main
}
finally {
    Pop-Location
}
