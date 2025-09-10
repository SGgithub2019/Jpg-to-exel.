# OCR to Excel Android App

Ez a projekt egy egyszerű Android app, amely képből (JPG/PNG) OCR-rel szöveget nyer ki,
majd azt Excel fájlba (.xlsx) menti.

## Használat GitHub Actions-szel (APK fordítás)
1. Hozz létre egy új repository-t a GitHub-on.
2. Töltsd fel ennek a ZIP-nek a tartalmát a repo-ba (main branch).
3. Menj a repo **Actions** fülére → futni fog az "Android CI".
4. Ha lefutott, az **Artifacts** résznél letöltheted az APK-t (`OcrToExcel-APK`).

> Tipp: Az első build akár 5-10 perc is lehet, de utána gyorsabb.
