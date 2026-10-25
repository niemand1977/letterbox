/* Registers the service worker for letterbox.eclabs.de.
 *
 * Include in the site <head> (or before </body>):
 *   <script src="/pwa.js" defer></script>
 *
 * The service worker file must be served from the site root (sw.js)
 * so that its scope covers the whole origin.
 */
(function () {
  if (!('serviceWorker' in navigator)) return;

  window.addEventListener('load', function () {
    // SW must be same-origin and served over HTTPS (or localhost).
    try {
      var swUrl = new URL('sw.js', window.location.href).toString();
      navigator.serviceWorker.register(swUrl, { scope: '/' })
        .then(function (reg) {
          console.info('[letterbox-pwa] service worker registered:', reg.scope);
        })
        .catch(function (err) {
          console.warn('[letterbox-pwa] service worker registration failed:', err);
        });
    } catch (err) {
      console.warn('[letterbox-pwa] service worker setup failed:', err);
    }
  });
})();
