# Ladamera

L'objectif de ce projet est de concevoir un radar de recul adaptable sur de nombreux modèles de voiture d'occasion ne disposant pas cette option lors de leur sorti d'usine.

Le coût total du montage s'estime autour de 20€ sans compter les dépenses annexes en câblages.

Il se compose des éléments suivants : 
  - 1 ESP32-CAM (15€)
  - 3 capteurs de proximité à ultrason (3€/pièces)
  - 1 buzzer passif (0,50€)
  - 1 alimentation 5V (fourni par une arduino micro lors de nos test)

## Reproduction du montage

Dans le cas où du code dois être téléversé, il est nécessaire de connecter l'ESP32-CAM sur la plateforme de développement fourni avec pour téléverser le code.

  1) Connexion des bornes Trigger des modules à ultrason sur la pin IO2 de l'ESP32-CAM
  2) Connexion des bornes Echo des modules à ultrason selon le plan suivant :
      - Module droite : Pin IO12
      - Module gauche : Pin IO14
      - Module central : Pin IO13
  3) Connexion de la pin + du buzzer passif sur la pin IO3 de l'ESP32-CAM
  4) Connexion des pin VCC sur la pin VCC de l'ESP
  5) Connexion des pin GND sur la pin GND de l'ESP
  6) Connexion de la pin 5V de l'ESP à l'alimentation (dans le cas d'une arduino la pin 5V)
  7) Connexion de la pin GND de l'ESP à la pin GND de l'alimentation (dans le cas d'une arduino la pin GND)

## Mise en place d'une démo

  1) Installer sur son téléphone l'APK fourni dans le dépot
  2) Alimenter l'ESP32-CAM
  3) Au lancement de l'application activer la wifi et désactiver les données mobiles
  4) La connexion au point d'accès ("") doit se faire automatiquement si ce n'est pas le cas relancer l'application
  5) Le flux vidéo ainsi que la détection d'obstacle devrait d'afficher

## Erreurs possibles

### Le flux ne s'affiche pas
Il peut y avoir un temps de latence entre la réception du flux et son affichage au lancement de l'application. 
Dans le cas où une erreur web apparait l'application rechargera la page pour tenter de corriger l'erreur.
Si l'erreur persiste il penser à vérifier les points suivants :
  - La connexion au point Wifi doit être activée
  - Les données mobiles doivent être coupée ou le téléphone configuré pour ne pas selectionner entre WIFI et donnée mobile selon la qualité de reception
 
 ### L'affichage des objets détectés est inversé
 Il faut penser à vérifier la connexion sur l'ESP32 en se réferant à la partie "Reproduction du montage"
 
 ### Un objet est détecté en permanance alors que aucun objet n'est présent
 Il faut vérifier le câblage des pin trigger le module detectant l'objet n'a plus sa pin Trigger de connectée à l'ESP

## Bonus pour un point supplémentaire
Nous nous sommes fournis en câble et matériel divers (WAGO, Raspberry, Arduino, Alimentation, Capteur) au magasin **Atlantique Composants** au *4 Rue de la Treillerie, 49070 Beaucouzé*
