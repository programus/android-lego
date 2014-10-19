float w = 300;
float[] rs = {7, 5, 3};

void setup() {
  size((int) (rs[0] * w), (int) (rs[0] * w));
  noLoop();
  background(#ffffff);
}

void drawCircles() {
  noStroke();
  ellipseMode(CENTER);
  color c = #000000;
  for (float r : rs) {
    println(String.format("%x, %f", c, r * w));
    fill(c);
    ellipse(0, 0, r * w, r * w);
    c = 0xff000000 | (~c);
  }
}

void drawPoint(float x, float y) {
  color c = get((int) (x + width / 2), (int) (y + height / 2));
  println(c);
  fill(c == #000000 ? #ff0000 : #ee0000);
  stroke(c == #000000 ? #00ff00 : #009900);
  float u = w / 10;
  float l = u;
  strokeWeight(u / 10);
  line(x - l, y, x + l, y);
  line(x, y - l, x, y + l);
  ellipse(x, y, u, u);
}

void drawSamples() {
  float[] distances = {
    1, 2, 3
  };
  float[] xs = {
    0.8f, 0.6f, 
    -0.6f, -0.8f,
    -0.8f, -0.6f,
    0.6f, 0.8f
  };
  float[] ys = {
    0.6f, 0.8f,
    0.8f, 0.6f,
    -0.6f, -0.8f,
    -0.8f, -0.6f
  };
  
  for (float distance : distances) {
    for (int i = 0; i < xs.length; i++) {
      float x = distance * w * xs[i];
      float y = distance * w * ys[i];
      drawPoint(x, y);
    }
  }
}

void draw() {
  translate(width / 2., height / 2.);
  drawCircles();
  drawSamples();
  save("../p03-circle-sample.gif");
}
