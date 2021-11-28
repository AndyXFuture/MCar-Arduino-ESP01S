#include <ESP8266WiFi.h>
#include <WiFiUdp.h>
//#include "Ticker.h"

#define PWM_M1  0
#define PWM_M2  1
#define PWM_M3  2
#define PWM_M4  3

//Ticker ticker;
int AX,AY,BX,BY;
int M1_Speed,M2_Speed,M3_Speed,M4_Speed;
int local_max;

const char* ssid = "your ssid";
const char* password = "your password";

WiFiUDP Udp;
unsigned int localUdpPort = 9999;
char incomingPacket[255];

void flash(){
     M1_Speed=(BY-BX-AX+AY)/4;
     M2_Speed=(BY+BX+AX+AY)/4;
     M3_Speed=(BY-BX+AX+AY)/4;
     M4_Speed=(BY+BX-AX+AY)/4;


     local_max=abs(M1_Speed);
     local_max=max(abs(M2_Speed),local_max);
     local_max=max(abs(M3_Speed),local_max);
     local_max=max(abs(M4_Speed),local_max);
     if(local_max == 0){
        local_max = 16;
     }
     //映射
     M1_Speed=map(M1_Speed,-local_max,local_max,0,1023);
     M2_Speed=map(M2_Speed,-local_max,local_max,1023,0);
     M3_Speed=map(M3_Speed,-local_max,local_max,1023,0);
     M4_Speed=map(M4_Speed,-local_max,local_max,0,1023);

     analogWrite(PWM_M1,M1_Speed);
     analogWrite(PWM_M2,M2_Speed);
     analogWrite(PWM_M3,M3_Speed);
     analogWrite(PWM_M4,M4_Speed);
}


void setup()
{
  pinMode(PWM_M1, OUTPUT);
  pinMode(PWM_M2, OUTPUT);
  pinMode(PWM_M3, OUTPUT);
  pinMode(PWM_M4, OUTPUT);
  analogWrite(PWM_M1,512);
  analogWrite(PWM_M2,512);
  analogWrite(PWM_M3,512);
  analogWrite(PWM_M4,512);
  
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
  }

  Udp.begin(localUdpPort);
  
  //ticker.attach_ms(50,flash);
}
void loop()
{
  if (Udp.parsePacket())//解析包不为空
  {
    memset(incomingPacket, 0, sizeof(incomingPacket));
    Udp.read(incomingPacket,255);

    //解析
    int Data = atoi(incomingPacket);
    AX = Data/1000000-16;
    AY = (Data/10000)%100-16;
    BX = (Data/100)%100-16;
    BY = Data%100-16;

    flash();
  }
}
