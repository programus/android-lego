static class Box {
  static int size;
  int x, y;
  Box(int x, int y) {
    this.x = x;
    this.y = y;
  }
}

static class Pic {
  static int blockSize = 30;
  static color borderColor = #ff0000;
  static color bgColor = #ffffff;
  static color[] blockColors = {#000000, #ffffff};
  static color lineColor = #808080;
  static color textColor = #ff8000;
  static color currColor = #ffe000;
  static color prevColor = #000eff;
  static color arrowColor = #3333ff;
  static float lineWeight = 1;
  static float currWeight = 2;
  static float prevWeight = 2;
  static int maxIndex = 5;
  static int arrowSize = 10;

  int[] blocks;
  int numStart;
  String name;
  
  int w;
  int h;
  int offsetX;
  int offsetY;
  PGraphics pg;
  
  Pic(String name, int numStart, int[] blocks) {
    this.name = name;
    this.numStart = numStart;
    this.blocks = blocks;
  }
}

void setupSize(Pic pic) {
  int w = 0;
  int h = 1;
  boolean needExx = false;
  boolean needExy = false;
  for (int i = 0; i < pic.blocks.length; i++) {
    int b = pic.blocks[i];
    w += b;
    if (b == 0 && i >= pic.numStart) {
      needExy = true;
      if (i == 0 || i == pic.blocks.length - 1) {
        needExx = true;
      }
    }
  }
  
  if (needExy) {
    h <<= 1;
  }
  
  if (needExx) {
    pic.offsetX = Pic.blockSize;
  }
  
  pic.w = w;
  pic.h = h;

  pic.pg = createGraphics(w * Pic.blockSize + pic.offsetX * 2 + 1, h * Pic.blockSize + (h != 1 ? Pic.arrowSize : 1));
}

void drawBlocks(Pic pic) {
  int colorIndex = 0;
  int x = 0;
  pic.pg.noStroke();
  for (int w : pic.blocks) {
    pic.pg.fill(Pic.blockColors[colorIndex++ & 1]);
    pic.pg.rect(Pic.blockSize * x + pic.offsetX, 0, Pic.blockSize * w, Pic.blockSize);
    x += w;
  }
}

void drawLines(Pic pic) {
  pic.pg.stroke(Pic.lineColor);
  pic.pg.strokeWeight(Pic.lineWeight);
  pic.pg.noFill();
  pic.pg.rect(pic.offsetX, 0, pic.w * Pic.blockSize, Pic.blockSize);
  for (int i = 1; i < pic.w; i++) {
    int x = i * Pic.blockSize + pic.offsetX;
    pic.pg.line(x, 0, x, Pic.blockSize);
  }
}

void drawBoxes(Pic pic) {
}

void drawText(Pic pic) {
  pic.pg.noStroke();
  pic.pg.textSize(Pic.blockSize * .8f);
  pic.pg.textAlign(CENTER, BOTTOM);
  int start = 0;
  for (int i = 0; i < pic.blocks.length; i++) {
    int w = pic.blocks[i];
    int n = i - pic.numStart;
    if (n >= Pic.maxIndex) {
      break;
    }
    if (i >= pic.numStart) {
      float x = (start + w / 2.f) * Pic.blockSize + pic.offsetX;
      float y = Pic.blockSize;
      if (w == 0) {
        pic.pg.fill(Pic.arrowColor);
        pic.pg.triangle(
          x, y, 
          x - Pic.arrowSize * .2f, y + Pic.arrowSize, 
          x + Pic.arrowSize * .2f, y + Pic.arrowSize);
        y = y * 2 + Pic.arrowSize * .7f;
      }
      pic.pg.fill(Pic.textColor);
      pic.pg.text(n, x, y);
    }
    start += w;
  }
}

void savePic(Pic pic) {
  if (pic.name != null) {
    pic.pg.save(pic.name);
  }
}

void drawPic(Pic pic) {
  background(bg);
  setupSize(pic);
  if (pic.pg != null) {
    pic.pg.beginDraw();
    pic.pg.background(Pic.bgColor);
    drawBlocks(pic);
    drawLines(pic);
    drawBoxes(pic);
    drawText(pic);
    pic.pg.endDraw();
    image(pic.pg, width >> 1, height >> 1);
  }
  savePic(pic);
}

int picIndex;
Pic[] pics = {
  new Pic("../p03-bwbwb.gif", 2, new int[]{0, 4, 4, 4, 12, 4, 4, 4}),
  new Pic("../p03-bwbwb-x.gif", 0, new int[]{0, 4, 4, 4, 12, 4, 4, 4}),
  new Pic("../p03-bwbwb-0.gif", 0, new int[]{4, 4, 12, 4, 4, 6}),
};
color bg;

void draw() {
  drawPic(pics[picIndex++ % pics.length]);
}

void setup() {
  size(displayWidth, displayHeight);
  noLoop();
  bg = get(0, 0);
  imageMode(CENTER);
}

void mouseClicked() {
  redraw();
}
