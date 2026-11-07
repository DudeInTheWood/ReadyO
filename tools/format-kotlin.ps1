param([switch]$Check)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$formatterVersion = '0.64'
# Keep the formatter outside the project: a "!" in a JAR path breaks compiler resource lookup.
$formatterPath = Join-Path ([System.IO.Path]::GetTempPath()) "readyo-tools/ktfmt-$formatterVersion.jar"

if (-not (Test-Path -LiteralPath $formatterPath)) {
    New-Item -ItemType Directory -Path (Split-Path $formatterPath) -Force | Out-Null
    Invoke-WebRequest -Uri "https://repo.maven.apache.org/maven2/com/facebook/ktfmt/$formatterVersion/ktfmt-$formatterVersion-with-dependencies.jar" -OutFile $formatterPath
}

$javaExecutable = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/java.exe' } else { 'java' }
$sourceFiles = Get-ChildItem -LiteralPath (Join-Path $projectRoot 'shared/src'), (Join-Path $projectRoot 'androidApp/src') -Filter '*.kt' -Recurse | Select-Object -ExpandProperty FullName
$buildFiles = @('build.gradle.kts', 'settings.gradle.kts', 'shared/build.gradle.kts', 'androidApp/build.gradle.kts') | ForEach-Object { Join-Path $projectRoot $_ }
$formatterOptions = @('--kotlinlang-style', '--quiet')
if ($Check) { $formatterOptions += @('--dry-run', '--set-exit-if-changed') }

& $javaExecutable -jar $formatterPath @formatterOptions @sourceFiles @buildFiles
exit $LASTEXITCODE
