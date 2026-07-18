# TLS + `www` redirect setup

## Problem

`https://worldwidewalter.ch` works, but `https://www.worldwidewalter.ch` fails the TLS
handshake (`tlsv1 alert internal error`).

- **DNS is fine** — both `worldwidewalter.ch` and `www.worldwidewalter.ch` resolve to the
  same server IP (`178.105.93.239`), so no DNS change is needed.
- **The certificate only covers the apex.** Its Subject Alternative Names list *only*
  `worldwidewalter.ch`, so the server has no valid certificate for the `www` name.

Fix = reissue the cert for **both** names and add a `www` → apex redirect on the
reverse proxy that terminates TLS in front of the Spring Boot container (port 8080).

> The backend already trusts `X-Forwarded-Proto` (`server.forward-headers-strategy=framework`)
> and has an app-level HTTP→HTTPS redirect, so the proxy just needs to set that header.
> Because the app now uses WebSockets (`/ws` STOMP endpoint), the proxy must also forward
> the `Upgrade`/`Connection` headers.

---

## Option A — Caddy (recommended: auto-obtains + renews certs, handles everything)

```caddy
worldwidewalter.ch {
    reverse_proxy 127.0.0.1:8080
}

www.worldwidewalter.ch {
    redir https://worldwidewalter.ch{uri} permanent
}
```

Caddy automatically gets a Let's Encrypt cert for **both** names, redirects HTTP→HTTPS,
sets `X-Forwarded-*`, and proxies WebSockets — no extra config.

---

## Option B — nginx

1. Issue a cert covering both names:

   ```bash
   sudo certbot --nginx -d worldwidewalter.ch -d www.worldwidewalter.ch
   ```

2. Config:

   ```nginx
   # HTTP -> HTTPS for both hosts
   server {
       listen 80;
       listen [::]:80;
       server_name worldwidewalter.ch www.worldwidewalter.ch;
       return 301 https://worldwidewalter.ch$request_uri;
   }

   # www (HTTPS) -> apex
   server {
       listen 443 ssl;
       listen [::]:443 ssl;
       server_name www.worldwidewalter.ch;

       ssl_certificate     /etc/letsencrypt/live/worldwidewalter.ch/fullchain.pem;
       ssl_certificate_key /etc/letsencrypt/live/worldwidewalter.ch/privkey.pem;

       return 301 https://worldwidewalter.ch$request_uri;
   }

   # apex -> Spring Boot on 8080
   server {
       listen 443 ssl;
       listen [::]:443 ssl;
       server_name worldwidewalter.ch;

       ssl_certificate     /etc/letsencrypt/live/worldwidewalter.ch/fullchain.pem;
       ssl_certificate_key /etc/letsencrypt/live/worldwidewalter.ch/privkey.pem;

       location / {
           proxy_pass http://127.0.0.1:8080;
           proxy_set_header Host              $host;
           proxy_set_header X-Real-IP         $remote_addr;
           proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;

           # WebSocket upgrade for the /ws STOMP endpoint
           proxy_http_version 1.1;
           proxy_set_header Upgrade    $http_upgrade;
           proxy_set_header Connection "upgrade";
       }
   }
   ```

Reload: `sudo nginx -t && sudo systemctl reload nginx`.
