Content-Security-Policy:
  default-src 'self';
  object-src 'none';
  base-uri 'self';
  frame-ancestors 'none';
  upgrade-insecure-requests;


Directive
Ce qu'elle apporte
default-src 'self'
Bloque tout ce qui ne vient pas de ton domaine
object-src 'none'
Bloque Flash et plugins → vecteur d'attaque classique
base-uri 'self'
Bloque l'injection via balise <base>
frame-ancestors 'none'
Bloque le Clickjacking
upgrade-insecure-requests
Force HTTPS sur toutes les ressources
