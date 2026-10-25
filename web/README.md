# PWA: "App installieren" für letterbox.eclabs.de

Chrome (Android 16/17, moderne Chromium-Versionen) blendet den Eintrag
**„App installieren"** im Browser-Menü aus, wenn nicht alle
Installability-Kriterien erfüllt sind. Die aktuelle Live-Seite
(https://letterbox.eclabs.de) hat **kein Web-App-Manifest und keinen
registrierten Service Worker** — daher fehlt der Menüpunkt komplett.

Diese Dateien beheben das. Sie sind deployment-ready und werden unter
dem Webroot von `https://letterbox.eclabs.de` abgelegt.

## Was ist kaputt (Audit 2026-10-24)

| Kriterium (Chromium)                        | Status live            | Fix |
|---------------------------------------------|------------------------|-----|
| Manifest über `<link rel="manifest">`       | 404, kein Link         | `manifest.webmanifest` + head-Snippet |
| `name` / `short_name`                       | fehlt                  | im Manifest |
| `start_url` (erreichbar)                    | fehlt                  | `./` (= `https://letterbox.eclabs.de/`) |
| `display: standalone`                       | fehlt                  | im Manifest |
| Icons 192x192 + 512x512 (PNG, absolut)      | fehlen (nur 0 Assets)  | `icons/` aus dem echten App-Icon, `any` + `maskable` |
| Registrierter Service Worker + `fetch`-Handler | **fehlend** (404)  | `sw.js` + `pwa.js` |
| HTTPS / kein Mixed Content                  | OK (0× `http://`)      | nichts zu tun |

Die Seite ist eine Vite/Vue-SPA (`/assets/index-*.js|css` gehasht).
Der Service Worker behandelt gehashte Vite-Assets als immutable
(cache-first) und den App-Shell-Load network-first mit Offline-Fallback.

## Dateien

```
web/
├── manifest.webmanifest        → /manifest.webmanifest
├── sw.js                       → /sw.js        (WICHTIG: site root, Scope "/")
├── pwa.js                      → /pwa.js
├── offline.html                → /offline.html
├── icons/icon-192.png          → /icons/icon-192.png
├── icons/icon-512.png          → /icons/icon-512.png
├── icons/icon-maskable-512.png → /icons/icon-maskable-512.png
└── icons/apple-touch-icon.png  → /icons/apple-touch-icon.png
```

Die Icons sind aus dem echten App-Icon generiert
(`fastlane/metadata/android/en-US/images/icon.png`, 512×512):
`any`-Icons unverändert, `maskable`-Version mit App-Primärfarbe
`#FF8F00` als Vollblut-Hintergrund und Motiv in der 78%-Safe-Zone,
`apple-touch-icon` 180×180 auf `#FFF8E1` (Letter-Button-Cream).

## 1. Dateien deployen

Alle Dateien aus `web/` 1:1 unter den Pfaden aus der Tabelle in den
Webroot legen, der `letterbox.eclabs.de` bedient (z. B. Nginx Root
`/var/www/letterbox`):

```
/var/www/letterbox/manifest.webmanifest
/var/www/letterbox/sw.js
/var/www/letterbox/pwa.js
/var/www/letterbox/offline.html
/var/www/letterbox/icons/icon-192.png
/var/www/letterbox/icons/icon-512.png
/var/www/letterbox/icons/icon-maskable-512.png
/var/www/letterbox/icons/apple-touch-icon.png
```

MIME-Typen (Nginx `mime.types`), falls noch nicht vorhanden:

```nginx
types {
    application/manifest+json webmanifest;
    application/javascript    js;
}
```

## 2. `<head>` der Seite ergänzen

In den `<head>` der erzeugten SPA (Vite: `index.html` in der
Web-Quelle, d. h. die Vorlage, aus der der Build erzeugt wird)
einbauen:

```html
<link rel="manifest" href="/manifest.webmanifest">
<meta name="theme-color" content="#FF8F00">
<link rel="apple-touch-icon" href="/icons/apple-touch-icon.png">
```

`theme-color` ist bereits mit `#FF8F00` gesetzt — prüfen, dass es
beim nächsten Build erhalten bleibt.

Vor `</body>` (oder `defer` im `<head>`):

```html
<script src="/pwa.js" defer></script>
```

Da es sich um einen Vite-Build handelt, alternativ (sauberer):
`pwa.js` als normales Modul/Script in die Vue-App einbinden, oder
`/pwa.js` in `index.html` hinterlegen und per
`vite build` ausliefern. Wichtig ist nur, dass das Skript nach der
Load-Sequenz der SPA läuft.

`sw.js` muss unter **`/sw.js`** liegen: Der Scope eines Service
Workers ist durch seinen Pfad begrenzt; von der Site-Root aus deckt
er den ganzen Subdomain-Origin ab.

## 3. Mixed Content

Keine `http://`-Ressourcen in der Live-Seite gefunden (0 Treffer).
Nichts zu tun — nur bei künftigen Änderungen beachten.

## 4. Verifizieren

1. `https://letterbox.eclabs.de/manifest.webmanifest` → HTTP 200,
   `Content-Type: application/manifest+json`
2. `https://letterbox.eclabs.de/sw.js` → HTTP 200,
   `Content-Type: application/javascript`
3. DevTools (mobile Emulation): **Application → Manifest** — keine
   roten Fehler; **Service Worker** — `sw.js` aktiv, keine Errors
   in der Console.
4. **Lighthouse → Progressive Web App** (mobile Viewport):
   - Installable: ✓
   - Loads an application shell: ✓
   - Uses the HTTPS protocol: ✓
   - Has a manifest: ✓
   - Has a splash screen / standalone display: ✓
   - Has a `fetch`-handler: ✓
5. Echter Test: Chrome auf Android 16/17 → `https://letterbox.eclabs.de`
   öffnen → Menü (⋮) → **„App installieren"** muss sichtbar und aktiv
   sein.

   Hinweis: Der Eintrag erscheint erst, nachdem die SW-Registration
   erfolgreich war (ggf. Seite nach dem ersten Load neu laden).

## Hinweis zu `start_url`

`start_url: "./"` wird relativ zur Manifest-URL aufgelöst, also
`https://letterbox.eclabs.de/`. Falls die PWA immer auf eine
bestimmte SPA-Route starten soll, `start_url` und `id` anpassen
(Vue-Router-Deep-Links funktionieren dank des App-Shell-Fallbacks
im Service Worker).
