

// variable for storing the potentiometer value
int potValue = 0;

void setup() {
  Serial.begin(115200);
  delay(1000);
  analogReadResolution(10);
}

void loop() {
  // Reading potentiometer value
  potValue = analogRead(4);
  Serial.println(potValue);
  delay(100);
}
