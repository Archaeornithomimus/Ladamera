#include "esp_camera.h"
#include <WiFi.h>
#include "esp_http_server.h"
#include <HCSR04.h>

HCSR04 hc(2, new int[3]{12,13,14},3); //initialisation class HCSR04 (trig pin , echo pin)

/*float zone5 = 5;// 1m m découpé en 5 zone 20cm par zone
float zone4 = 4;// 2m à 1m découpé en 5 zone
float zone3 = 3;// 1m à 40cm découpé en 5 zone
float zone2 = 2;// 40cm à 20cm découpé en 5 zone
float zone1 = 1;// 20cm à 0cm découpé en 5 zone

// D2 gauche = sensor 0
// D3 droite = sensor 1
// D4 centre = sensor 2
*/


float proximityL = 0;
float proximityR = 0;
float proximityC = 0;

const char* ssid = "ESP32-Access-Point";
const char* password = "123456789";

String readSensor() {
  proximityR = hc.dist(0);
  delay(60);
  proximityC = hc.dist(1);
  delay(60);
  proximityL = hc.dist(2);
  delay(60);
  return String(String(proximityR)+","+String(proximityC)+","+String(proximityL));
}

#define PART_BOUNDARY "123456789000000000000987654321"
static const char* _STREAM_CONTENT_TYPE = "multipart/x-mixed-replace;boundary=" PART_BOUNDARY;
static const char* _STREAM_BOUNDARY = "\r\n--" PART_BOUNDARY "\r\n";
static const char* _STREAM_PART = "Content-Type: image/jpeg\r\nContent-Length: %u\r\n\r\n";

httpd_handle_t stream_httpd = NULL;
httpd_handle_t camera_httpd = NULL;
httpd_handle_t sensor_httpd = NULL;

int numero_port; // numéro du port du stream server

// ********************************************************
// stream_handler: routine qui gère le streaming vidéo

static esp_err_t stream_handler(httpd_req_t *req) {
  camera_fb_t * fb = NULL;
  esp_err_t res = ESP_OK;
  size_t _jpg_buf_len = 0;
  uint8_t * _jpg_buf = NULL;
  char * part_buf[64];

  static int64_t last_frame = 0;
  if (!last_frame) {
    last_frame = esp_timer_get_time();
  }

  res = httpd_resp_set_type(req, _STREAM_CONTENT_TYPE);
  if (res != ESP_OK) {
    return res;
  }
  httpd_resp_set_hdr(req, "Access-Control-Allow-Origin", "*");

  while (true) {
    fb = esp_camera_fb_get();
    if (!fb) {
      Serial.println("Echec de la capture de camera");
      res = ESP_FAIL;
    } else {
      if (fb->width > 400) {
        if (fb->format != PIXFORMAT_JPEG) {
          bool jpeg_converted = frame2jpg(fb, 80, &_jpg_buf, &_jpg_buf_len);
          esp_camera_fb_return(fb);
          fb = NULL;
          if (!jpeg_converted) {
            Serial.println("Echec de la compression JPEG");
            res = ESP_FAIL;
          }
        } else {
          _jpg_buf_len = fb->len;
          _jpg_buf = fb->buf;
        }
      }
    }
    if (res == ESP_OK) {
      size_t hlen = snprintf((char *)part_buf, 64, _STREAM_PART, _jpg_buf_len);
      res = httpd_resp_send_chunk(req, (const char *)part_buf, hlen);
    }
    if (res == ESP_OK) {
      res = httpd_resp_send_chunk(req, (const char *)_jpg_buf, _jpg_buf_len);
    }
    if (res == ESP_OK) {
      res = httpd_resp_send_chunk(req, _STREAM_BOUNDARY, strlen(_STREAM_BOUNDARY));
    }
    if (fb) {
      esp_camera_fb_return(fb);
      fb = NULL;
      _jpg_buf = NULL;
    } else if (_jpg_buf) {
      free(_jpg_buf);
      _jpg_buf = NULL;
    }
    if (res != ESP_OK) {
      break;
    }
  }
  last_frame = 0;
  return res;
}

// ********************************************************
// web_handler: construction de la page web

static esp_err_t web_handler(httpd_req_t *req) {
  httpd_resp_set_type(req, "text/html");
  httpd_resp_set_hdr(req, "Content-Encoding", "identity");
  sensor_t * s = esp_camera_sensor_get();

  char pageWeb[175] = "";
  strcat(pageWeb, "<!doctype html> <html> <head> <title id='title'>ESP32-CAM</title> </head> <body> <img id='stream' src='http://");
  // l'adresse du stream server (exemple: 192.168.0.145:81):
  char adresse[20] = "";
  sprintf (adresse, "%d.%d.%d.%d:%d", WiFi.localIP()[0], WiFi.localIP()[1], WiFi.localIP()[2], WiFi.localIP()[3],numero_port);
  strcat(pageWeb, adresse);
  strcat(pageWeb, "/stream'><p>toto</p> </body> </html>");
  int taillePage = strlen(pageWeb);

  return httpd_resp_send(req, (const char *)pageWeb, taillePage);
}

static esp_err_t sensor_handler(httpd_req_t *req) { 
  httpd_resp_set_type(req, "text/plain");
  //char response[30] = "";
  distanceSound();
  return httpd_resp_sendstr(req, (const char *)readSensor().c_str());
}
//httpd_resp_set_hdr(req, "Content-Encoding", "identity");

// ********************************************************
// startCameraServer: démarrage du web server et du stream server

void startCameraServer() {
  httpd_config_t config = HTTPD_DEFAULT_CONFIG();

  httpd_uri_t index_uri = {
    .uri       = "/",
    .method    = HTTP_GET,
    .handler   = web_handler,
    .user_ctx  = NULL
  };

  httpd_uri_t stream_uri = {
    .uri       = "/stream",
    .method    = HTTP_GET,
    .handler   = stream_handler,
    .user_ctx  = NULL
  };

  httpd_uri_t sensor_uri = {
    .uri       = "/sensor",
    .method    = HTTP_GET,
    .handler   = sensor_handler,
    .user_ctx  = NULL
  };
  
  Serial.printf("Demarrage du web server sur le port: '%d'\n", config.server_port);
  if (httpd_start(&camera_httpd, &config) == ESP_OK) {
    httpd_register_uri_handler(camera_httpd, &index_uri);
  }

  config.server_port += 1;
  config.ctrl_port += 1;
  Serial.printf("Demarrage du stream server sur le port: '%d'\n", config.server_port);
  if (httpd_start(&stream_httpd, &config) == ESP_OK) {
    httpd_register_uri_handler(stream_httpd, &stream_uri);
  }
  config.server_port += 1;
  config.ctrl_port += 1;
  Serial.printf("Demarrage du sensor server sur le port: '%d'\n", config.server_port);
  if (httpd_start(&sensor_httpd, &config) == ESP_OK) {
    httpd_register_uri_handler(sensor_httpd, &sensor_uri);
  }
  numero_port = config.server_port;
}

// ********************************************************
// initialisation de la caméra, connexion au réseau WiFi.

void setup() {
  Serial.begin(115200);
  Serial.println();
  Serial.println("====");
  
  pinMode(15,OUTPUT);

  // TO CLEAN
  tone(15, 2000, 1);
  noTone(15);
  tone(15, 2000, 1);
  noTone(15);

  // définition des broches pour le modèle AI Thinker - ESP32-CAM
  camera_config_t config;
  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer = LEDC_TIMER_0;
  config.pin_d0 = 5;
  config.pin_d1 = 18;
  config.pin_d2 = 19;
  config.pin_d3 = 21;
  config.pin_d4 = 36;
  config.pin_d5 = 39;
  config.pin_d6 = 34;
  config.pin_d7 = 35;
  config.pin_xclk = 0;
  config.pin_pclk = 22;
  config.pin_vsync = 25;
  config.pin_href = 23;
  config.pin_sscb_sda = 26;
  config.pin_sscb_scl = 27;
  config.pin_pwdn = 32;
  config.pin_reset = -1;
  
  config.xclk_freq_hz = 20000000;
  config.pixel_format = PIXFORMAT_JPEG;  //YUV422|GRAYSCALE|RGB565|JPEG
  config.frame_size = FRAMESIZE_VGA;  // QVGA|CIF|VGA|SVGA|XGA|SXGA|UXGA
  config.jpeg_quality = 47;//10;  // 0-63 ; plus bas = meilleure qualité
  config.fb_count = 2; // nombre de frame buffers

  // initialisation de la caméra
  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.printf("Echec de l'initialisation de la camera, erreur 0x%x", err);
    return;
  }

  sensor_t * s = esp_camera_sensor_get();

  Serial.println("");
  Serial.print("Connexion au reseau WiFi: ");
  Serial.println(ssid);
  delay(100);

  WiFi.softAP(ssid, password);//,1,0,2); // change to 1 device autorized
  IPAddress IP = WiFi.softAPIP();
  Serial.print("AP IP address: ");
  Serial.println(IP);

  /*server.on("/distance", HTTP_GET, [](AsyncWebServerRequest *request){
    request->send_P(200, "text/plain", readSensor().c_str());
    distanceSound();
  });
  server.begin();*/

  startCameraServer();

  Serial.print("Camera Ready! Use 'http://");
  Serial.print(WiFi.localIP());
  Serial.println("' to connect");

}

// ********************************************************
// Distance Sound
void distanceSound(){
  if ((proximityR == 0) || (proximityL == 0) || (proximityC  == 0)){
    buzz(15, 1550, 5);
    Serial.println("zone0");
  } else if ((inRange(0,proximityR,20)) | (inRange(0,proximityL,20)) | (inRange(0,proximityC,20))){
    Serial.println("zone1");
    buzz(15, 1550, 200);
  } else if ((inRange(20,proximityR,40)) | (inRange(20,proximityL,40)) | (inRange(20,proximityC,40))){
    buzz(15, 1550, 400);
    Serial.println("zone2");
  } else if ((inRange(40,proximityR,60)) | (inRange(40,proximityL,60)) | (inRange(40,proximityC,60))){
    buzz(15, 1550, 600);
    Serial.println("zone3");
  } else if ((inRange(60,proximityR,80)) | (inRange(60,proximityL,80)) | (inRange(60,proximityC,80))){
    buzz(15, 1550, 800);
    Serial.println("zone4");
  } else if ((inRange(80,proximityR,100)) | (inRange(80,proximityL,100)) | (inRange(80,proximityC,100))){
    buzz(15, 1550, 1000);
    Serial.println("zone5");
  }
}

// ********************************************************
// loop ne fait rien!
void loop() {
}

// TO CLEAN AFTER
static void buzz(int targetPin, long frequency, long length) {
  long delayValue = 1000000/frequency/2; // calculate the delay value between transitions
  // 1 second's worth of microseconds, divided by the frequency, then split in half since
  // there are two phases to each cycle
  long numCycles = frequency * length/ 1000; // calculate the number of cycles for proper timing
  // multiply frequency, which is really cycles per second, by the number of seconds to 
  // get the total number of cycles to produce
 for (long i=0; i < numCycles; i++){ // for the calculated length of time...
    digitalWrite(targetPin,HIGH); // write the buzzer pin high to push out the diaphram
    delayMicroseconds(delayValue); // wait for the calculated delay value
    digitalWrite(targetPin,LOW); // write the buzzer pin low to pull back the diaphram
    delayMicroseconds(delayValue); // wait again for the calculated delay value
  }
}

bool inRange(float minimum,float val, float maximum) {
  if ((minimum <= val) && (val <= maximum)){

    return true;
  } else {
    return false;
  }
}
