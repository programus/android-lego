int blockSize = 50;
size(blockSize * 32 + 1, blockSize);
background(255);
fill(0);
stroke(0);
int[] offsets = {2, 4, 4};
int[] widths = {4, 12, 4};
int offset = 0;
for (int i = 0; i < offsets.length; i++) {
  offset += offsets[i];
  rect(blockSize * offset, 0, blockSize * widths[i], blockSize);
  offset += widths[i];
}
stroke(0x80);
noFill();
rect(0, 0, width - 1, height - 1);
for (int i = 1; i < 32; i++) {
  line(blockSize * i, 0, blockSize * i, height);
}
fill(255, 128, 0);
textSize(blockSize * .8f);
textAlign(CENTER, BOTTOM);
offset = 0;
for (int i = 0; i < offsets.length; i++) {
  offset += offsets[i];
  float x = offset + widths[i] / 2.f;
  text(i * 2, x * blockSize, blockSize);
  offset += widths[i];
  if (i + 1 < offsets.length) {
    x = offset + offsets[i + 1] / 2.f;
    text(i * 2 + 1, x * blockSize, height);
  }
}
save("../p03-bwbwb.gif");
