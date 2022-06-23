#include <HCSR04.h>

HCSR04 hc(11, new int[3]{2,3,4},3); //initialisation class HCSR04 (trig pin , echo pin)
float tolerance = 10;


float zone5 = 5;// 1m m découpé en 5 zone 20cm par zone
float zone4 = 4;// 2m à 1m découpé en 5 zone
float zone3 = 3;// 1m à 40cm découpé en 5 zone
float zone2 = 2;// 40cm à 20cm découpé en 5 zone
float zone1 = 1;// 20cm à 0cm découpé en 5 zone

// D2 gauche = sensor 0
// D3 droite = sensor 1
// D4 centre = sensor 2

float proximityL = 0;
float proximityR = 0;
float proximityC = 0;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  pinMode(12,OUTPUT);
  pinMode(10, OUTPUT);
  pinMode(9, OUTPUT);

  tone(12, 2000, 1);
  noTone(12);
  tone(12, 2000, 1);
  noTone(12);

}

void loop() {
  // put your main code here, to run repeatedly:
  proximityL = hc.dist(0);
  delay(60);
  proximityR = hc.dist(1);
  delay(60);
  proximityC = hc.dist(2);
  //delay(1000);
  //digitalWrite(12,HIGH);
  //delay(500);
  //digitalWrite(12,LOW);

  //digitalWrite(12,HIGH);
  //delay(500);
  //digitalWrite(12,LOW);
  //delay(2000);
  //proximityR = 30;
  //proximityL = 100;
  //proximityC = 50;
  Serial.println(proximityR);
  Serial.println(proximityL);
  Serial.println(proximityC);

  if ((proximityR == 0) || (proximityL == 0) || (proximityC  == 0)){
    buzz(12, 1550, 5);
    Serial.println("zone0");
  } else if ((inRange(0,proximityR,20)) | (inRange(0,proximityL,20)) | (inRange(0,proximityC,20))){
    Serial.println("zone1");
    buzz(12, 1550, 200);
  } else if ((inRange(20,proximityR,40)) | (inRange(20,proximityL,40)) | (inRange(20,proximityC,40))){
    buzz(12, 1550, 400);
    Serial.println("zone2");
  } else if ((inRange(40,proximityR,60)) | (inRange(40,proximityL,60)) | (inRange(40,proximityC,60))){
    buzz(12, 1550, 600);
    Serial.println("zone3");
  } else if ((inRange(60,proximityR,80)) | (inRange(60,proximityL,80)) | (inRange(60,proximityC,80))){
    buzz(12, 1550, 800);
    Serial.println("zone4");
  } else if ((inRange(80,proximityR,100)) | (inRange(80,proximityL,100)) | (inRange(80,proximityC,100))){
    buzz(12, 1550, 1000);
    Serial.println("zone5");
  }
}

void beepZone5(char zone,char position
){

}

void beepZone4(){
  
}

void beepZone3(){
  
}

void beepZone2(){
  
}

void beepZone1(){
  
}

void buzz(int targetPin, long frequency, long length) {
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

bool inRange(float minimum,float val, float maximum)
{
  if ((minimum <= val) && (val <= maximum)){

    return true;
  } else {
    return false;
  }
}
