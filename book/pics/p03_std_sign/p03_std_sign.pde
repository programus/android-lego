float s = 60;
float len = 20 + 7;
float offset = (7 + 1) / 2.;

strokeWeight(s * .05);

size((int)(s * (offset * 2 + len)), (int)(s * (offset * 2 + len)));
background(#ffffff);

float[] corners = {
      -offset, -offset,
      len - offset, -offset,
      -offset, len - offset,
      len - offset, len - offset
};

translate(offset * s * 2, offset * s * 2);

ellipseMode(RADIUS);
noStroke();
for (int i = 0; i < corners.length; i += 2) {
  color c = #000000;
  for (float r : new float[]{3.5, 2.5, 1.5}) {
    fill(c);
    ellipse(corners[i] * s, corners[i + 1] * s, r * s, r * s);
    c = (~c) | 0xff000000;
  }
}

rectMode(CENTER);
for (int y = 0; y < 20; y++) {
  for (int x = 0; x < 20; x++) {
    fill(((x & 1) ^ (y & 1)) == 0 ? #404040 : #c0c0c0);
    rect(x * s, y * s, s, s);
  }
}

color c = #00e080;
stroke(c);
for (int i = 0; i < 20; i++) {
  line(0, i * s, 19 * s, i * s);
  line(i * s, 0, i * s, 19 * s);
}

line(-offset * 2 * s, 0, (19 + offset * 2) * s, 0);
line(0, -offset * 2 * s, 0, (19 + offset * 2) * s);
float dot = .1;
for (int i = (int)(-offset * 2); i < len + offset; i++) {
  line(-dot * s, i * s, dot * s, i * s);
  line(i * s, -dot * s, i * s, dot * s);
}

float b = .2;
for (int i = 0; i < corners.length; i += 2) {
  line((corners[i] - b) * s, corners[i + 1] * s, (corners[i] + b) * s, corners[i + 1] * s);
  line(corners[i] * s, (corners[i + 1] - b) * s, corners[i] * s, (corners[i + 1] + b) * s);
}

noStroke();
fill(c);
float w = 5;
float h = 1.5;
triangle(
  (19 + offset * 2) * s, 0, 
  (19 + offset * 2) * s - w * s / 10, -h * s / 10,
  (19 + offset * 2) * s - w * s / 10, h * s / 10
  );
triangle(
  0, (19 + offset * 2) * s, 
  -h * s / 10, (19 + offset * 2) * s - w * s / 10, 
  h * s / 10, (19 + offset * 2) * s - w * s / 10
  );

textSize(s * .6);
fill(#ff8000);
for (int i = (int)(-offset * 2) + 1; i < len + offset - 4; i++) {
  if (i != 0) {
    textAlign(RIGHT, CENTER);
    text(i, -(dot * 2) * s, i * s);
    textAlign(CENTER, BOTTOM);
    text(i, i * s, -(dot * 2) * s);
  }
}
textAlign(RIGHT, TOP);
text(0, -(dot * 2) * s, 0);

for (int i = 0; i < corners.length; i += 2) {
  textAlign(CENTER, BOTTOM);
  text(String.format("p[%d] (%.0f, %.0f)", i >> 1, corners[i], corners[i + 1]), corners[i] * s, (corners[i + 1] - .5) * s);
}

save("../p03-std-sign.png");
