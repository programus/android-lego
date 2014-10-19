static class Pic {
  static int blockSize = 30 * 4;
  static color borderColor = #ff0000;
  static color bgColor = #ffffff;
  static color[] blockColors = {#000000, #ffffff};
  static color lineColor = #808080;
  static color textColor = #ff8000;
  static color currColor = #00bbf7;
  static color prevColor = #4ecf12;
  static color currFillColor = 0x8000bbf7;
  static color prevFillColor = 0x804ecf12;
  static color arrowColor = #3333ff;
  static float lineWeight = 1 * 4;
  static float currWeight = 2 * 4;
  static float prevWeight = 2 * 4;
  static int maxIndex = 5;
  static int arrowSize = 10 * 4;

  int[] blocks;
  int numStart;
  String name;
  int curr;
  boolean showState;
  
  int w;
  int h;
  int offsetX;
  int offsetY;
  PGraphics pg;
  
  Pic(String name, int numStart, int curr, int[] blocks, boolean showState) {
    this.name = name;
    this.numStart = numStart;
    this.curr = curr;
    this.blocks = blocks;
    this.showState = showState;
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
  
  if (pic.curr > 0 && pic.curr <= w && pic.showState) {
    pic.offsetY = pic.blockSize;
  }
  
  if (needExx) {
    pic.offsetX = Pic.blockSize;
  }
  
  pic.w = w;
  pic.h = h;

  pic.pg = createGraphics(w * Pic.blockSize + pic.offsetX * 2 + 1, h * Pic.blockSize + pic.offsetY + (needExy ? Pic.arrowSize : 1));
}

void drawBlocks(Pic pic) {
  int colorIndex = 0;
  int x = 0;
  pic.pg.noStroke();
  for (int w : pic.blocks) {
    pic.pg.fill(Pic.blockColors[colorIndex++ & 1]);
    pic.pg.rect(Pic.blockSize * x + pic.offsetX, pic.offsetY, Pic.blockSize * w, Pic.blockSize);
    x += w;
  }
}

void drawLines(Pic pic) {
  pic.pg.stroke(Pic.lineColor);
  pic.pg.strokeWeight(Pic.lineWeight);
  pic.pg.noFill();
  pic.pg.rect(pic.offsetX, pic.offsetY, pic.w * Pic.blockSize, Pic.blockSize);
  for (int i = 1; i < pic.w; i++) {
    int x = i * Pic.blockSize + pic.offsetX;
    pic.pg.line(x, pic.offsetY, x, pic.offsetY + Pic.blockSize);
  }
}

float drawArrow(Pic pic, float x, float y, color c, boolean revert) {
  pic.pg.noStroke();
  pic.pg.fill(c);
  pic.pg.triangle(
    x, y, 
    x - Pic.arrowSize * .2f, y + (revert ? -Pic.arrowSize : Pic.arrowSize), 
    x + Pic.arrowSize * .2f, y + (revert ? -Pic.arrowSize : Pic.arrowSize));
  return Pic.arrowSize * .7f;
}

void drawBox(Pic pic, int index, boolean isCurr) {
  if (index >= 0 && index < pic.w) {
    float weight = isCurr ? Pic.currWeight : Pic.prevWeight;
    float x = index * Pic.blockSize + weight / 2 + pic.offsetX;
    float y = weight / 2 + pic.offsetY;
    float u = Pic.blockSize - weight;
    pic.pg.fill(isCurr ? Pic.currFillColor : Pic.prevFillColor);
    pic.pg.stroke(isCurr ? Pic.currColor : Pic.prevColor);
    pic.pg.strokeWeight(weight);
    pic.pg.rect(x, y, u, u);
    if (isCurr) {
      pic.pg.line(x, y, x + u, y + u);
      pic.pg.line(x + u, y, x, y + u);
    } else if (pic.showState) {
      x += u / 2;
      y = pic.offsetY - 2 - drawArrow(pic, x, pic.offsetY, Pic.arrowColor, true);
      pic.pg.fill(Pic.textColor);
      int sum = 0;
      int part = 0;
      for (int i = 0; i < pic.blocks.length; i++) {
        sum += pic.blocks[i];
        if (index < sum) {
          part = i - pic.numStart;
          break;
        }
      }
      pic.pg.textSize(Pic.blockSize * .5f);
      String text = String.format("currentState = %d", part);
      float tw = pic.pg.textWidth(text);
      pic.pg.textAlign(CENTER, BOTTOM);
      if (x - tw / 2 < 0) {
        pic.pg.textAlign(LEFT, BOTTOM);
        x = 0;
      }
      pic.pg.text(String.format("currentState = %d", part), x, y);
    }
  }
}

void drawBoxes(Pic pic) {
  drawBox(pic, pic.curr - 1, false);
  drawBox(pic, pic.curr, true);
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
      float x = (start + w / 2.f) * Pic.blockSize;
      float y = Pic.blockSize;
      if (w == 0) {
        y = y * 2 + drawArrow(pic, x + pic.offsetX, y + pic.offsetY, Pic.arrowColor, false);
      }
      pic.pg.fill(Pic.textColor);
      pic.pg.text(n, x + pic.offsetX, y + pic.offsetY);
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
//  new Pic("../p03-bwbwb.gif", 2, -1, new int[]{0, 4, 4, 4, 12, 4, 4, 4}, false),
//  new Pic("../p03-bwbwb-x.gif", 0, 3, new int[]{0, 4, 4, 4, 12, 4, 4, 4}, true),
//  new Pic("../p03-bwbwb-0.gif", 0, 8, new int[]{4, 4, 12, 4, 4, 6}, false),
//  new Pic("../p03-bwbwb-47-1.gif", 0, 8, new int[]{5, 5, 5, 5, 5}, true),
//  new Pic("../p03-bwbwb-47-2.gif", 0, 15, new int[]{5, 5, 5, 5, 5}, true),
  new Pic("../p03-fpf-b-1.gif", 0, 6, new int[]{3, 3, 9, 3, 3, 2}, true),
  new Pic("../p03-fpf-b-3.gif", 0, 18, new int[]{3, 3, 9, 3, 3, 2}, true),
  new Pic("../p03-fpf-b-0.gif", 0, 2, new int[]{3, 3, 9, 3, 3, 2}, true),
  new Pic("../p03-fpf-b-2.gif", 0, 8, new int[]{3, 3, 9, 3, 3, 2}, true),
  new Pic("../p03-fpf-b-4.gif", 0, 20, new int[]{3, 3, 9, 3, 3, 2}, true),
  new Pic("../p03-fpf-w-1.gif", 0, 5, new int[]{3, 3, 9, 3, 3, 2}, true),
  new Pic("../p03-fpf-w-3.gif", 0, 16, new int[]{3, 3, 9, 3, 3, 2}, true),
  new Pic("../p03-fpf-w-0.gif", 0, 3, new int[]{3, 3, 9, 3, 3, 2}, true),
  new Pic("../p03-fpf-w-2.gif", 0, 15, new int[]{3, 3, 9, 3, 3, 2}, true),
  new Pic("../p03-fpf-w-4.gif", 0, 21, new int[]{3, 3, 9, 3, 3, 2}, true),
//  new Pic("../p03-shift-1.gif", 0, 14, new int[]{2, 2, 2, 2, 6, 2, 2, 2}, true),
//  new Pic("../p03-shift-2.gif", 2, 14, new int[]{2, 2, 2, 2, 6, 2, 2, 2}, true),
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
