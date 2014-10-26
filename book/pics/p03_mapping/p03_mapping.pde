int p = 2;

size(1024 * p, 600 * p);

stroke(#000000);
fill(#000000);
strokeWeight(3 * p);

background(#ffffff);

int start = 100 * p;

int u = 10 * p;

line(start, 200 * p, start + 30 * u, 200 * p);
line(start, 300 * p, start + 60 * u, 300 * p);

textSize(20 * p);
textAlign(RIGHT, CENTER);
text("A ", start, 200 * p);
text("C ", start, 300 * p);
textAlign(LEFT, CENTER);
text(" B", start + 30 * u, 200 * p);
text(" D", start + 60 * u, 300 * p);

stroke(#ee7900);
strokeWeight(1 * p);
float x = 23.64;

ellipseMode(RADIUS);
fill(#ee7900);
float r = 1.5;
ellipse(start + x * u, 200 * p, r * p, r * p);
ellipse(start + x * u * 2, 300 * p, r * p, r * p);

line(start + x * u, 200 * p, start + x * u * 2, 300 * p);
textAlign(CENTER, BOTTOM);
text(String.format("x = %.2fmm", x), start + x * u, 200 * p);
textAlign(CENTER, TOP);
text(String.format("2x = %.2fmm", x * 2), start + x * u * 2, 300 * p);

save("../p03-mapping.png");
