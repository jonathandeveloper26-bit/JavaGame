<#
.SYNOPSIS
    Exports the Ripple Pond design documents (docs/RIPPLE_POND_GDD.md and
    docs/RIPPLE_POND_UML.md) to PDF.

.DESCRIPTION
    Each run writes a NEW revision next to the source: RIPPLE_POND_<DOC>_REV_<n>.pdf,
    where n is one higher than the highest revision already in docs/. A PDF without a
    revision suffix (for example RIPPLE_POND_UML.pdf) counts as revision 1. Existing
    PDFs are never overwritten.

    The Markdown is rendered in a headless browser (Edge or Chrome) so that the Mermaid
    diagrams appear as drawings. Needs an internet connection, because the Markdown and
    Mermaid renderers are loaded from a CDN.

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File tools\make-doc-pdfs.ps1 -Doc GDD
    powershell -ExecutionPolicy Bypass -File tools\make-doc-pdfs.ps1 -Doc All
#>
param(
    [ValidateSet('GDD', 'UML', 'All')]
    [string]$Doc = 'All'
)

$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
$docsDir  = Join-Path $repoRoot 'docs'

$docs = [ordered]@{
    GDD = @{ Source = 'RIPPLE_POND_GDD.md'; Name = 'RIPPLE_POND_GDD'; Title = 'Ripple Pond - Game Design Document'; Orientation = 'portrait' }
    UML = @{ Source = 'RIPPLE_POND_UML.md'; Name = 'RIPPLE_POND_UML'; Title = 'Ripple Pond - UML class diagram';    Orientation = 'landscape' }
}

$browser = @(
    'C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe',
    'C:\Program Files\Microsoft\Edge\Application\msedge.exe',
    'C:\Program Files\Google\Chrome\Application\chrome.exe',
    'C:\Program Files (x86)\Google\Chrome\Application\chrome.exe'
) | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $browser) { throw 'Neither Edge nor Chrome was found.' }

$template = @'
<!doctype html>
<html lang="en"><head><meta charset="utf-8"><title>__TITLE__</title>
<script src="https://cdn.jsdelivr.net/npm/marked@12.0.2/marked.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/mermaid@10.9.1/dist/mermaid.min.js"></script>
<style>
  @page { size: A4 __ORIENTATION__; margin: 16mm 15mm; }
  html { font-family: "Segoe UI", Arial, sans-serif; font-size: 10.5pt; line-height: 1.45; color: #1b1f24; }
  body { margin: 0; }
  .revision { font-size: 9pt; color: #57606a; border-bottom: 1px solid #d0d7de; padding-bottom: 4px; margin-bottom: 14px; }
  h1 { font-size: 22pt; margin: 0 0 10px; }
  h2 { font-size: 15pt; margin: 22px 0 8px; padding-bottom: 3px; border-bottom: 1px solid #d0d7de; }
  h3 { font-size: 12pt; margin: 16px 0 6px; }
  h1, h2, h3, h4 { break-after: avoid; }
  p, ul, ol { margin: 6px 0; }
  li { margin: 2px 0; }
  hr { border: 0; border-top: 1px solid #d0d7de; margin: 16px 0; }
  a { color: #0969da; text-decoration: none; }
  code { font-family: Consolas, "Courier New", monospace; font-size: 9.5pt; background: #eff1f3; padding: 1px 4px; border-radius: 3px; }
  pre { background: #f6f8fa; padding: 8px 10px; border-radius: 4px; overflow-wrap: anywhere; white-space: pre-wrap; }
  pre code { background: none; padding: 0; }
  table { border-collapse: collapse; margin: 8px 0 12px; width: auto; max-width: 100%; }
  th, td { border: 1px solid #d0d7de; padding: 4px 8px; text-align: left; vertical-align: top; }
  th { background: #f3f5f7; }
  tr { break-inside: avoid; }
  .diagram { break-inside: avoid; text-align: center; margin: 10px 0 14px; }
  .diagram svg { max-width: 100% !important; height: auto; max-height: __MAXDIAGRAM__; }
  .render-error { color: #b00020; font-weight: bold; border: 2px solid #b00020; padding: 8px; }
  /* Diagram documents (landscape): each section starts a page so a heading, its
     introduction and its diagram stay together instead of leaving half-empty pages. */
  body.landscape hr { display: none; }
  body.landscape h2 { break-before: page; }
</style></head>
<body class="__ORIENTATION__">
<div class="revision">__TITLE__ &nbsp;|&nbsp; __REV__ &nbsp;|&nbsp; exported __DATE__</div>
<div id="content"></div>
<script>
const source = __SOURCE__;
(async () => {
  const content = document.getElementById('content');
  try {
    content.innerHTML = marked.parse(source);
    // Same anchors GitHub generates, so in-document links keep working.
    content.querySelectorAll('h1,h2,h3,h4').forEach(h => {
      h.id = h.textContent.toLowerCase().trim().replace(/[^\w\s-]/g, '').replace(/\s/g, '-');
    });
    // Links to other repository files do not exist next to a PDF: keep the text only.
    content.querySelectorAll('a[href$=".md"], a[href$=".pdf"], a[href$=".png"]').forEach(a => {
      const span = document.createElement('span'); span.textContent = a.textContent; a.replaceWith(span);
    });
    mermaid.initialize({ startOnLoad: false, theme: 'default' });
    let n = 0;
    for (const code of [...content.querySelectorAll('code.language-mermaid')]) {
      const holder = document.createElement('div');
      try {
        const out = await mermaid.render('diagram' + (n++), code.textContent);
        holder.className = 'diagram'; holder.innerHTML = out.svg;
      } catch (e) {
        holder.className = 'render-error'; holder.textContent = 'DIAGRAM FAILED TO RENDER: ' + (e.message || e);
      }
      code.parentElement.replaceWith(holder);
    }
  } catch (e) {
    const err = document.createElement('div');
    err.className = 'render-error'; err.textContent = 'DOCUMENT FAILED TO RENDER: ' + (e.message || e);
    content.prepend(err);
  }
})();
</script>
</body></html>
'@

$work = Join-Path $env:TEMP ('ripple-pond-pdf-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $work | Out-Null
$utf8 = New-Object System.Text.UTF8Encoding($false)

$keys = if ($Doc -eq 'All') { $docs.Keys } else { @($Doc) }
try {
    foreach ($key in $keys) {
        $d = $docs[$key]
        $sourcePath = Join-Path $docsDir $d.Source
        if (-not (Test-Path $sourcePath)) { throw "Missing source: $sourcePath" }

        # Next revision number: highest existing + 1. An unsuffixed PDF counts as revision 1.
        $highest = 0
        if (Test-Path (Join-Path $docsDir ($d.Name + '.pdf'))) { $highest = 1 }
        Get-ChildItem -Path $docsDir -Filter ($d.Name + '_REV_*.pdf') -ErrorAction SilentlyContinue | ForEach-Object {
            if ($_.BaseName -match '_REV_(\d+)$') { $highest = [Math]::Max($highest, [int]$Matches[1]) }
        }
        $rev = 'REV_' + ($highest + 1)
        $pdfPath = Join-Path $docsDir ($d.Name + '_' + $rev + '.pdf')
        if (Test-Path $pdfPath) { throw "Refusing to overwrite $pdfPath" }

        $markdown = [System.IO.File]::ReadAllText($sourcePath, [System.Text.Encoding]::UTF8)
        $maxDiagram = if ($d.Orientation -eq 'landscape') { '122mm' } else { '240mm' }
        $html = $template.Replace('__TITLE__', [System.Net.WebUtility]::HtmlEncode($d.Title)).
                          Replace('__ORIENTATION__', $d.Orientation).
                          Replace('__MAXDIAGRAM__', $maxDiagram).
                          Replace('__REV__', $rev).
                          Replace('__DATE__', (Get-Date -Format 'd MMMM yyyy')).
                          Replace('__SOURCE__', (ConvertTo-Json $markdown -Compress))
        $htmlPath = Join-Path $work ($key + '.html')
        [System.IO.File]::WriteAllText($htmlPath, $html, $utf8)

        $tmpPdf = Join-Path $work ($key + '.pdf')
        $browserArgs = @(
            '--headless=new', '--disable-gpu', '--no-first-run', '--no-pdf-header-footer',
            ('--user-data-dir="' + (Join-Path $work 'profile') + '"'),
            '--virtual-time-budget=30000',
            ('--print-to-pdf="' + $tmpPdf + '"'),
            ('"' + ([System.Uri]$htmlPath).AbsoluteUri + '"')
        )
        Start-Process -FilePath $browser -ArgumentList $browserArgs -Wait -WindowStyle Hidden | Out-Null

        if (-not (Test-Path $tmpPdf) -or (Get-Item $tmpPdf).Length -lt 1000) { throw "The browser did not produce a PDF for $key." }
        Copy-Item -Path $tmpPdf -Destination $pdfPath
        Write-Output ("{0}: {1} ({2:N0} bytes)" -f $key, $pdfPath, (Get-Item $pdfPath).Length)
    }
}
finally {
    Remove-Item -Recurse -Force $work -ErrorAction SilentlyContinue
}
