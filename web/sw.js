/* Service Worker for letterbox.eclabs.de (Vue SPA, Vite build)
 *
 * Minimal, dependency-free service worker that satisfies Chromium's
 * installability requirement (a registered SW with a functional fetch
 * handler) and adds basic offline support:
 *
 *   - page navigations:  network-first, then cached request, then
 *                        cached app shell (./), then offline.html
 *   - hashed Vite assets (/assets/*): cache-first, immutable
 *   - other same-origin assets: cache-first with background refresh
 *
 * Bump CACHE_VERSION when the shell changes; old caches are purged on
 * activate.
 */

const CACHE_VERSION = 'letterbox-v1';
const OFFLINE_URL = 'offline.html';
const APP_SHELL = './';

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_VERSION)
      .then((cache) => cache.addAll([APP_SHELL, OFFLINE_URL]))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(
        keys
          .filter((key) => key !== CACHE_VERSION)
          .map((key) => caches.delete(key))
      ))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  const { request } = event;

  // Only handle same-origin GET requests.
  if (request.method !== 'GET') return;
  const url = new URL(request.url);
  if (url.origin !== self.location.origin) return;

  // Navigations (page loads, incl. SPA deep links): network-first,
  // fall back to the cached app shell, then the offline page.
  if (request.mode === 'navigate') {
    event.respondWith(
      fetch(request)
        .then((response) => {
          const copy = response.clone();
          caches.open(CACHE_VERSION).then((cache) => cache.put(request, copy));
          return response;
        })
        .catch(async () => {
          const cached = await caches.match(request);
          if (cached) return cached;
          const shell = await caches.match(APP_SHELL);
          if (shell) return shell;
          return caches.match(OFFLINE_URL) || Response.error();
        })
    );
    return;
  }

  // Hashed Vite build output is immutable: cache-first, no revalidation.
  if (url.pathname.startsWith('/assets/')) {
    event.respondWith(
      caches.match(request).then((cached) => {
        if (cached) return cached;
        return fetch(request).then((response) => {
          if (response && response.status === 200) {
            const copy = response.clone();
            caches.open(CACHE_VERSION).then((cache) => cache.put(request, copy));
          }
          return response;
        });
      })
    );
    return;
  }

  // Other same-origin assets (manifest, icons, other files):
  // cache-first with background refresh (stale-while-revalidate).
  event.respondWith(
    caches.match(request).then((cached) => {
      const network = fetch(request)
        .then((response) => {
          if (response && response.status === 200) {
            const copy = response.clone();
            caches.open(CACHE_VERSION).then((cache) => cache.put(request, copy));
          }
          return response;
        })
        .catch(() => cached);
      return cached || network;
    })
  );
});
