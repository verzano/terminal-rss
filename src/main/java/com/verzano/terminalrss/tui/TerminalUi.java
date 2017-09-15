package com.verzano.terminalrss.tui;

import com.verzano.terminalrss.tui.ansi.Ansi;
import com.verzano.terminalrss.tui.container.floor.Floor;
import com.verzano.terminalrss.tui.container.floor.FloorOptions;
import com.verzano.terminalrss.tui.floater.Floater;
import com.verzano.terminalrss.tui.metrics.Point;
import com.verzano.terminalrss.tui.metrics.Size;
import com.verzano.terminalrss.tui.task.print.PrintTask;
import com.verzano.terminalrss.tui.widget.Widget;
import lombok.Getter;
import lombok.Setter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.verzano.terminalrss.tui.ansi.Ansi.*;
import static com.verzano.terminalrss.tui.constants.Key.ESCAPED_PREFIX;
import static com.verzano.terminalrss.tui.floater.Floater.NULL_FLOATER;
import static com.verzano.terminalrss.tui.metrics.Size.FILL_CONTAINER;
import static com.verzano.terminalrss.tui.widget.Widget.NULL_WIDGET;

// TODO use an executor to schedule events
// TODO add defaults for attributes as well as some css style waterfall thing for getting them maybe...
public class TerminalUi {

  private static final AtomicBoolean run = new AtomicBoolean(true);
  private static final BlockingDeque<PrintTask> printTaskQueue = new LinkedBlockingDeque<>();
  private static final Terminal terminal;
  private static Floor floor = new Floor();
  @Getter
  private static Floater floater = NULL_FLOATER;
  // TODO needs to be a stack of focus
  @Getter
  @Setter
  private static Widget focusedWidget = NULL_WIDGET;
  private static final Thread keyActionThread = new Thread(TerminalUi::keyActionLoop, "Key Action");
  @Getter
  private static Size size;
  private static final Thread printingThread = new Thread(TerminalUi::printingLoop, "Printing");
  private static final Thread resizingThread = new Thread(TerminalUi::resizingLoop, "Resizing");

  static {
    try {
      terminal = TerminalBuilder.terminal();
      terminal.enterRawMode();
      terminal.echo(false);

      size = new Size(terminal.getWidth(), terminal.getHeight());

      printingThread.start();
      keyActionThread.start();
      resizingThread.start();

      printTaskQueue.addFirst(() -> print(Ansi.HIDE_CURSOR));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private TerminalUi() {}

  // TODO only permit one at a time... or have a stack...
  public static void setFloater(Floater floater) {
    TerminalUi.floater = floater;
    TerminalUi.floater.setFocused();
  }

  public static void removeFloater() {
    TerminalUi.floater = NULL_FLOATER;
  }

  public static int getWidth() {
    return size.getWidth();
  }

  public static int getHeight() {
    return size.getHeight();
  }

  public static void setBaseWidget(Widget baseWidget) {
    floor.addWidget(baseWidget,
        new FloorOptions(new Size(FILL_CONTAINER, FILL_CONTAINER), new Point(1, 1)));
  }

  private static void printingLoop() {
    clear();

    while (run.get()) {
      try {
        printTaskQueue.take().print();
        terminal.writer().flush();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    clear();
  }

  private static void keyActionLoop() {
    try {
      while (run.get()) {
        // TODO this is kind of a lame way to do this
        int key = terminal.reader().read(100);
        switch (key) {
          case ESC:
            switch (terminal.reader().read()) {
              case '[':
                focusedWidget.fireKeyActions(ESCAPED_PREFIX + (char) terminal.reader().read());
                break;
            }
            break;
          case -2:
            break;
          default:
            focusedWidget.fireKeyActions((char) key + "");
            break;
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // TODO maybe have a like a little delay so that multiple resizes can be grouped together...
  private static void resizingLoop() {
    while (run.get()) {
      if (size.getWidth() != terminal.getWidth() || size.getHeight() != terminal.getHeight()) {
        size.setWidth(terminal.getWidth());
        size.setHeight(terminal.getHeight());
        resize();
        reprint();
      }
      try {
        Thread.sleep(200);
      } catch (InterruptedException ignored) { }
    }
  }

  public static void shutdown() {
    new Thread(() -> {
      printTaskQueue.addFirst(() -> print(SHOW_CURSOR));
      printTaskQueue.addFirst(() -> run.set(false));

      try {
        printingThread.join();
        keyActionThread.join();
        resizingThread.join();
      } catch (InterruptedException ignored) { }

      try {
        terminal.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }).start();
  }

  private static void clear() {
    String emptyLine = new String(new char[size.getWidth()]).replace("\0", " ");
    for (int row = 1; row <= size.getHeight(); row++) {
      move(1, row);
      terminal.writer().print(emptyLine);
    }
    move(1, 1);
    terminal.flush();
  }

  public static void resize() {
    if (Thread.currentThread() != printingThread) {
      printTaskQueue.addFirst(TerminalUi::resize);
    } else {
      floor.arrange();
      if (floater != NULL_FLOATER) {
        floater.arrange();
      }
    }
  }

  public static void reprint() {
    if (Thread.currentThread() != printingThread) {
      printTaskQueue.add(TerminalUi::reprint);
    } else {
      floor.print();
      if (floater != NULL_FLOATER) {
        floater.print();
      }
    }
  }

  public static void move(int x, int y) {
    if (Thread.currentThread() != printingThread) {
      printTaskQueue.add(() -> move(x, y));
    } else {
      terminal.writer().printf(SET_POSITION, y, x);
    }
  }

  public static void print(String s) {
    if (Thread.currentThread() != printingThread) {
      printTaskQueue.add(() -> print(s));
    } else {
      terminal.writer().print(s);
    }
  }

  public static void printn(String s, int n) {
    if (Thread.currentThread() != printingThread) {
      printTaskQueue.add(() -> printn(s, n));
    } else {
      for (int i = 0; i < n; i++) {
        terminal.writer().print(s);
      }
    }
  }

  public static void schedulePrintTask(PrintTask printTask) {
    printTaskQueue.add(printTask);
  }
}
