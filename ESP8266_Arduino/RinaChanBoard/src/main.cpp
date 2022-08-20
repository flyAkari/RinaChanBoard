#include <Arduino.h>
#include <LedControl_HW_SPI.h>
//#include <LedControl_SW_SPI.h>
#include <ESP8266WiFi.h>
#include <WiFiUdp.h>
#include <ESP8266HTTPClient.h>

const char *ssid = "XiaoMi_WiFi";         // WiFi Name
const char *password = "25721520abcabb"; // WiFi Password

unsigned int localPort = 8888;     // local port to listen on
const unsigned long HTTP_TIMEOUT = 5000;
WiFiClient client;
HTTPClient http;

// buffers for receiving and sending data
char packetBuffer[UDP_TX_PACKET_MAX_SIZE + 1]; // buffer to hold incoming packet,
char ReplyBuffer[] = "RinaboardIsOn";      // a string to send back

WiFiUDP Udp;

const int DIN_PIN = D7;
const int CS_PIN = D1;
const int CLK_PIN = D5;

char cur_ip[17];
//https://xantorohara.github.io/led-matrix-editor/#0018181818000000|0018181818040000|0000442810000000|0006182018060000|00183c2c18000000|003c020000000000|00007e0000000000|02057e0000000000|1000102044443800|1000101010100000|00183c0c00000000|0018181c10200000|00001e2018060000|10284482926c0000|3c66e78181e7663c|0010207e20100000|0008047e04080000|0044281028440000|103c503814781000|0038444444380000
const uint64_t EYES[] = {
    0x0,
    0x0018181818000000, //1     |
    0x0018181818040000, //2    `|
    0x0000442810000000, //3     ^
    0x0006182018060000, //4     >
    0x00183c2c18000000, //5     0
    0x003c020000000000, //6    `-
    0x00007e0000000000, //7     -
    0x02057e0000000000, //8    .-
    0x1000102044443800, //9     ?
    0x1000101010100000, //10    !
    0x00183c0c00000000, //11
    0x0018181c10200000, //12    イ
    0x00001e2018060000, //13    >
    0x10284482926c0000, //14    ?
    0x3c66e78181e7663c, //15    +
    0x0010207e20100000, //16    →
    0x0008047e04080000, //17    ←
    0x0044281028440000, //18    x
    0x103c503814781000, //19    $
    0x0038444444380000  //20    o
};

//https://xantorohara.github.io/led-matrix-editor/#000000e000000000|000000e010000000|000000e010080000|0000804020000000|0080402010f00000|00c0201010f00000|0000804020e00000|00f0101020c00000|0000f008f8000000|0000804040800000|8040202040800000|0080404040408000|0080404040404080|0000f010e0000000
const uint64_t MOUTHES[] = {
    0x0,
    0x000000e000000000, //1
    0x000000e010000000, //2
    0x000000e010080000, //3
    0x0000804020000000, //4
    0x0080402010f00000, //5
    0x00c0201010f00000, //6
    0x0000804020e00000, //7
    0x00f0101020c00000, //8
    0x0000f008f8000000, //9
    0x0000804040800000, //10
    0x8040202040800000, //11
    0x0080404040408000, //12
    0x0080404040404080, //13
    0x0000f010e0000000  //14
}; 

//https://xantorohara.github.io/led-matrix-editor/#0000001800000000|0000002850000000|0000002a54000000|0000000028000000
const uint64_t CHEEKS[] = {
    0x0,
    0x0000001800000000, //1
    0x0000002850000000, //2
    0x0000002a54000000, //3
    0x0000000028000000  //4
};

const uint64_t NUMBERS[] = {
    0x3c24242424243c00, // 0
    0x1010101010101000, // 1
    0x3c04043c20203c00, // 2
    0x3c20203c20203c00, // 3
    0x2020203c24242400, // 4
    0x3c20203c04043c00, // 5
    0x3c24243c04043c00, // 6
    0x2020202020203c00, // 7
    0x3c24243c24243c00, // 8
    0x3c20203c24243c00, // 9
    0x1818000000000000  //.
};

const int eye_num = sizeof(EYES) / sizeof(EYES[0]);
const int mouth_num = sizeof(MOUTHES) / sizeof(MOUTHES[0]);
const int cheek_num = sizeof(CHEEKS) / sizeof(CHEEKS[0]);

typedef struct FACE
{
    int eyeL;
    int eyeR;
    int mouth;
    int cheek;
} FACE;

FACE face[] = {
    {3, 3, 11, 2},
    {3, 3, 5, 2},
    {5, 5, 1, 0},
    {0, 0, 1, 0},
    {3, 3, 9, 2},
    {11, 11, 5, 0},
    {4, 4, 5, 4},
    {8, 9, 10, 0},
    {6, 7, 7, 0},
    {3, 3, 6, 3},
    {4, 4, 8, 2},
    {5, 5, 3, 2},
    {0, 0, 4, 1},
    {0, 0, 0, 0}};

const int FACE_LEN = sizeof(face) / sizeof(FACE);

const byte addr_tbl[16][24] = {
    {4, 4, 4, 4, 4, 4, 4, 4,/**/2, 2, 2, 2, 2, 2, 2, 2,/**/0, 0, 0, 0, 0, 0, 0, 0},
    {4, 4, 4, 4, 4, 4, 4, 4,/**/2, 2, 2, 2, 2, 2, 2, 2,/**/0, 0, 0, 0, 0, 0, 0, 0},
    {4, 4, 4, 4, 4, 4, 4, 4,/**/2, 2, 2, 2, 2, 2, 2, 2,/**/0, 0, 0, 0, 0, 0, 0, 0},
    {4, 4, 4, 4, 4, 4, 4, 4,/**/2, 2, 2, 2, 2, 2, 2, 2,/**/0, 0, 0, 0, 0, 0, 0, 0},
    {4, 4, 4, 4, 4, 4, 4, 4,/**/2, 2, 2, 2, 2, 2, 2, 2,/**/0, 0, 0, 0, 0, 0, 0, 0},
    {4, 4, 4, 4, 4, 4, 4, 4,/**/2, 2, 2, 2, 2, 2, 2, 2,/**/0, 0, 0, 0, 0, 0, 0, 0},
    {4, 4, 4, 4, 4, 4, 4, 4,/**/2, 2, 2, 2, 2, 2, 2, 2,/**/0, 0, 0, 0, 0, 0, 0, 0},
    {4, 4, 4, 4, 4, 4, 4, 4,/**/2, 2, 2, 2, 2, 2, 2, 2,/**/0, 0, 0, 0, 0, 0, 0, 0},
    ///////////////////////////////////////////////////////////////////////////////
    {5, 5, 5, 5, 5, 5, 5, 5,/**/3, 3, 3, 3, 3, 3, 3, 3,/**/1, 1, 1, 1, 1, 1, 1, 1},
    {5, 5, 5, 5, 5, 5, 5, 5,/**/3, 3, 3, 3, 3, 3, 3, 3,/**/1, 1, 1, 1, 1, 1, 1, 1},
    {5, 5, 5, 5, 5, 5, 5, 5,/**/3, 3, 3, 3, 3, 3, 3, 3,/**/1, 1, 1, 1, 1, 1, 1, 1},
    {5, 5, 5, 5, 5, 5, 5, 5,/**/3, 3, 3, 3, 3, 3, 3, 3,/**/1, 1, 1, 1, 1, 1, 1, 1},
    {5, 5, 5, 5, 5, 5, 5, 5,/**/3, 3, 3, 3, 3, 3, 3, 3,/**/1, 1, 1, 1, 1, 1, 1, 1},
    {5, 5, 5, 5, 5, 5, 5, 5,/**/3, 3, 3, 3, 3, 3, 3, 3,/**/1, 1, 1, 1, 1, 1, 1, 1},
    {5, 5, 5, 5, 5, 5, 5, 5,/**/3, 3, 3, 3, 3, 3, 3, 3,/**/1, 1, 1, 1, 1, 1, 1, 1},
    {5, 5, 5, 5, 5, 5, 5, 5,/**/3, 3, 3, 3, 3, 3, 3, 3,/**/1, 1, 1, 1, 1, 1, 1, 1}};

const byte row_tbl[16][24] = {
    {7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0},
    {7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0},
    {7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0},
    {7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0},
    {7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0},
    {7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0},
    {7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0},
    {7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0},
    ///////////////////////////////////////////////////////////////////////////////
    {7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0},
    {7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0},
    {7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0},
    {7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0},
    {7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0},
    {7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0},
    {7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0},
    {7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0,/**/7, 6, 5, 4, 3, 2, 1, 0}};

const byte col_tbl[16][24] = {
    {1, 1, 1, 1, 1, 1, 1, 1,/**/1, 1, 1, 1, 1, 1, 1, 1,/**/1, 1, 1, 1, 1, 1, 1, 1},
    {2, 2, 2, 2, 2, 2, 2, 2,/**/2, 2, 2, 2, 2, 2, 2, 2,/**/2, 2, 2, 2, 2, 2, 2, 2},
    {3, 3, 3, 3, 3, 3, 3, 3,/**/3, 3, 3, 3, 3, 3, 3, 3,/**/3, 3, 3, 3, 3, 3, 3, 3},
    {4, 4, 4, 4, 4, 4, 4, 4,/**/4, 4, 4, 4, 4, 4, 4, 4,/**/4, 4, 4, 4, 4, 4, 4, 4},
    {5, 5, 5, 5, 5, 5, 5, 5,/**/5, 5, 5, 5, 5, 5, 5, 5,/**/5, 5, 5, 5, 5, 5, 5, 5},
    {6, 6, 6, 6, 6, 6, 6, 6,/**/6, 6, 6, 6, 6, 6, 6, 6,/**/6, 6, 6, 6, 6, 6, 6, 6},
    {7, 7, 7, 7, 7, 7, 7, 7,/**/7, 7, 7, 7, 7, 7, 7, 7,/**/7, 7, 7, 7, 7, 7, 7, 7},
    {0, 0, 0, 0, 0, 0, 0, 0,/**/0, 0, 0, 0, 0, 0, 0, 0,/**/0, 0, 0, 0, 0, 0, 0, 0},
    ///////////////////////////////////////////////////////////////////////////////
    {1, 1, 1, 1, 1, 1, 1, 1,/**/1, 1, 1, 1, 1, 1, 1, 1,/**/1, 1, 1, 1, 1, 1, 1, 1},
    {2, 2, 2, 2, 2, 2, 2, 2,/**/2, 2, 2, 2, 2, 2, 2, 2,/**/2, 2, 2, 2, 2, 2, 2, 2},
    {3, 3, 3, 3, 3, 3, 3, 3,/**/3, 3, 3, 3, 3, 3, 3, 3,/**/3, 3, 3, 3, 3, 3, 3, 3},
    {4, 4, 4, 4, 4, 4, 4, 4,/**/4, 4, 4, 4, 4, 4, 4, 4,/**/4, 4, 4, 4, 4, 4, 4, 4},
    {5, 5, 5, 5, 5, 5, 5, 5,/**/5, 5, 5, 5, 5, 5, 5, 5,/**/5, 5, 5, 5, 5, 5, 5, 5},
    {6, 6, 6, 6, 6, 6, 6, 6,/**/6, 6, 6, 6, 6, 6, 6, 6,/**/6, 6, 6, 6, 6, 6, 6, 6},
    {7, 7, 7, 7, 7, 7, 7, 7,/**/7, 7, 7, 7, 7, 7, 7, 7,/**/7, 7, 7, 7, 7, 7, 7, 7},
    {0, 0, 0, 0, 0, 0, 0, 0,/**/0, 0, 0, 0, 0, 0, 0, 0,/**/0, 0, 0, 0, 0, 0, 0, 0}};

/* SWSPI */
// LedControl_SW_SPI led = LedControl_SW_SPI();
/* HWSPI */
LedControl_HW_SPI led = LedControl_HW_SPI();

byte vMemory[16][3];
void setMemory(int row, int col, boolean state)
{
    bitWrite(vMemory[row][col >> 3], col & 0x07, state);
}

void setRinaBoard(int row, int col, boolean state)
{
    led.setLed(addr_tbl[row][col], row_tbl[row][col], col_tbl[row][col], state);
}


void showRefreashDirection()
{
    for (int i = 0; i < 6; i++)
    {
        for (int j = 0; j < 8; j++)
        {
            for (int k = 0; k < 8; k++)
            {
                led.setLed(i, j, k, 0);
            }
        }
    }
    for (int i = 0; i < 6; i++)
    {
        for (int j = 0; j < 8; j++)
        {
            for (int k = 0; k < 8; k++)
            {
                led.setLed(i, j, k, 1);
                delay(3);
            }
        }
    }
    delay(500);
    for (int i = 0; i < 6; i++)
    {
        for (int j = 0; j < 8; j++)
        {
            for (int k = 0; k < 8; k++)
            {
                led.setLed(i, j, k, 0);
            }
        }
    }
}

void setLeftEye(uint64_t image)
{
    for (int i = 0; i < 8; i++)
    {
        byte row = (image >> i * 8) & 0xFF;
        for (int j = 0; j < 8; j++)
        {
            setMemory(i, j + 2, bitRead(row, j));
        }
    }
}

void setRightEye(uint64_t image)
{
    for (int i = 0; i < 8; i++)
    {
        byte row = (image >> i * 8) & 0xFF;
        for (int j = 0; j < 8; j++)
        {
            setMemory(i, 21 - j, bitRead(row, j));
        }
    }
}

void setEyes(uint64_t image)
{
    for (int i = 0; i < 8; i++)
    {
        byte row = (image >> i * 8) & 0xFF;
        for (int j = 0; j < 8; j++)
        {
            setMemory(i, j + 2, bitRead(row, j));
            setMemory(i, 21 - j, bitRead(row, j));
        }
    }
}

void setCheeks(uint64_t image)
{
    for (int i = 3; i < 5; i++)
    {
        byte row = (image >> i * 8) & 0xFF;
        for (int j = 0; j < 8; j++)
        {
            setMemory(i + 5, j, bitRead(row, j));
            setMemory(i + 5, 23 - j, bitRead(row, j));
        }
    }
}

void setMouth(uint64_t image)
{
    for (int i = 0; i < 8; i++)
    {
        byte row = (image >> i * 8) & 0xFF;
        for (int j = 0; j < 8; j++)
        {
            setMemory(i + 8, j + 4, bitRead(row, j));
            setMemory(i + 8, 19 - j, bitRead(row, j));
        }
    }
}

void setFace(int eyeL, int eyeR, int mouth, int cheek)
{
    if (eyeL >= eye_num || eyeL >= eye_num || mouth >= mouth_num || cheek >= cheek_num)
    {
        Serial.println("array out of bounds");
        return;
    }
    setLeftEye(EYES[eyeL]);
    setRightEye(EYES[eyeR]);
    setMouth(MOUTHES[mouth]);
    setCheeks(CHEEKS[cheek]);
}

void displayMemory()
{
    for (int i = 0; i < 16; i++)
    {
        for (int j = 0; j < 3; j++)
        {
            for (int t = 0; t < 8; t++)
            {
                setRinaBoard(i, j * 8 + t, bitRead(vMemory[i][j], t));
            }
        }
    }
}

int cur_face[4];

/*put num in "num1,num2,num3,num4," into numArr[4]*/
void numStr_to_numArray(const char *numStr, int *numArr, int numArrLen)
{
    if (nullptr == numStr || nullptr == numArr)
    {
        Serial.println("nullptr");
        return;
    }
    boolean flag = false;
    for (int i = 0; i < 40; i++)
    {
        if ('\0' == numStr[i])
        {
            flag = true;
        }
    }
    if (false == flag)
    {
        Serial.println("numStr too long");
        return;
    }
    int i = 0;
    char temp[40];
    strcpy(temp, numStr);
    char *cp = temp;
    int face_part_index = 0;
    while (temp[i] != '\0')
    {
        if (',' == temp[i])
        {
            temp[i] = '\0';
            numArr[face_part_index] = atoi(cp);
            face_part_index++;
            if (face_part_index > (numArrLen - 1))
            {
                return;
            }
            cp = &temp[i + 1];
        }
        i++;
    }
}

void printIpOnLeftEye()
{
    char *p = cur_ip;
    if (p == nullptr)
    {
        return;
    }
    for (int i = 0; p[i] != '\0'; i++)
    {
        if (i >= sizeof(cur_ip))
        {
            return;
        }
        if (p[i] == '.')
        {
            setLeftEye(NUMBERS[10]);
            setRightEye(EYES[0]);
            setMouth(MOUTHES[0]);
            setCheeks(CHEEKS[0]);
            displayMemory();
        }
        else if (p[i] >= '0' && p[i] <= '9')
        {
            setLeftEye(NUMBERS[p[i] - '0']);
            setRightEye(EYES[0]);
            setMouth(MOUTHES[0]);
            setCheeks(CHEEKS[0]);
            displayMemory();
        }
        delay(500);
    }
    setLeftEye(EYES[0]);
    setRightEye(EYES[0]);
    setMouth(MOUTHES[0]);
    setCheeks(CHEEKS[0]);
    displayMemory();
}

void setup()
{
    Serial.begin(9600);
    Serial.println("Start");
    // we have already set the number of devices when we created the LedControl
    /* SWSPI */
    // led.begin(DIN_PIN, CLK_PIN, CS_PIN, 6);
    /* HWSPI */
    led.begin(CS_PIN, 6);
    int devices = led.getDeviceCount();
    // we have to init all devices in a loop
    for (int address = 0; address < devices; address++)
    {
        /*The MAX72XX is in power-saving mode on startup*/
        led.shutdown(address, false);
        /* Set the brightness to a medium values */
        led.setIntensity(address, 15);
        /* and clear the display */
        led.clearDisplay(address);
    }
    showRefreashDirection();
    setFace(1,1,1,0);
    displayMemory();
    WiFi.mode(WIFI_STA);
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED)
    {
        delay(500);
        Serial.print(".");
    }
    Serial.println("");
    Serial.println("WiFi connected");
    Serial.println("IP address: ");
    Serial.println(WiFi.localIP());
    strcpy(cur_ip, WiFi.localIP().toString().c_str());
    // Serial.println("After Convert:");
    // Serial.println(cur_ip);
    //printIpOnLeftEye();
    Udp.begin(localPort);
    setFace(3,3,2,2);
    displayMemory();
}
// int i = 0;
void loop()
{
    int packetSize = Udp.parsePacket();
    if (packetSize)
    {
        Serial.printf("Received packet of size %d from %s:%d\n    (to %s:%d, free heap = %d B)\n",
                      packetSize,
                      Udp.remoteIP().toString().c_str(), Udp.remotePort(),
                      Udp.destinationIP().toString().c_str(), Udp.localPort(),
                      ESP.getFreeHeap());

        // read the packet into packetBufffer
        int n = Udp.read(packetBuffer, UDP_TX_PACKET_MAX_SIZE);
        packetBuffer[n] = '\0';
        // 222,222,222,222,

        Serial.print("Rina Contents:");
        Serial.println(packetBuffer);
        Serial.println("result:");
        if (0 == strcmp("RinaBoardUdpTest", packetBuffer))
        {
            Serial.println("RinaBoardGetTestMessage");
            Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
            Udp.write(ReplyBuffer);
            Udp.endPacket();  
        }
        else
        {
            numStr_to_numArray(packetBuffer, cur_face, 4);
            for (int i = 0; i < 4; i++)
            {
                Serial.println(cur_face[i]);
            }
            setFace(cur_face[0], cur_face[1], cur_face[2], cur_face[3]);
            displayMemory();
            // Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
            // Udp.write('a');
            // Udp.endPacket(); 
        }
    }
    // setFace(face[i].eyeL+1, face[i].eyeR+1, face[i].mouth+1, face[i].cheek+1);
    // displayMemory();
    // if (++i >= FACE_LEN)
    //     i = 0;
    // delay(1000);
}