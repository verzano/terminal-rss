package com.verzano.terminalrss.ui.widget;

import com.verzano.terminalrss.content.ContentType;
import com.verzano.terminalrss.ui.TerminalUI;
import com.verzano.terminalrss.ui.floater.binary.BinaryChoiceFloater;
import com.verzano.terminalrss.ui.metrics.Size;
import com.verzano.terminalrss.ui.task.key.KeyTask;
import com.verzano.terminalrss.ui.widget.ansi.Attribute;
import com.verzano.terminalrss.ui.widget.container.shelf.Shelf;
import com.verzano.terminalrss.ui.widget.container.shelf.ShelfOptions;
import com.verzano.terminalrss.ui.widget.text.entry.RolodexWidget;
import com.verzano.terminalrss.ui.widget.text.entry.TextEntryWidget;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.verzano.terminalrss.ui.metrics.Size.FILL_NEEDED;
import static com.verzano.terminalrss.ui.widget.Widget.NULL_WIDGET;
import static com.verzano.terminalrss.ui.widget.constants.Key.TAB;
import static com.verzano.terminalrss.ui.widget.constants.Orientation.HORIZONTAL;

public class AddSourceFloater extends BinaryChoiceFloater {
  private TextEntryWidget uriTextEntry;
  private RolodexWidget<ContentType> contentTypeRolodex;
  private TextEntryWidget contentTagEntry;

  public AddSourceFloater(KeyTask addSourceAction, KeyTask cancelAction) {
    super(
        NULL_WIDGET,
        addSourceAction, "Add Source",
        cancelAction, "Cancel");
    Shelf displayWidget = new Shelf(HORIZONTAL, 1);

    uriTextEntry = new TextEntryWidget();
    uriTextEntry.addKeyAction(TAB, () -> {
      contentTypeRolodex.setFocused();
      TerminalUI.reprint();
    });

    List<ContentType> types = Arrays.stream(ContentType.values())
        .filter(ct -> ct != ContentType.NULL_TYPE)
        .collect(Collectors.toList());
    contentTypeRolodex = new RolodexWidget<>(types, 3, 3);
    contentTypeRolodex.addKeyAction(TAB, () -> {
      contentTagEntry.setFocused();
      TerminalUI.reprint();
    });

    contentTagEntry = new TextEntryWidget();
    contentTagEntry.addKeyAction(TAB, () -> {
      getPositiveButton().setFocused();
      TerminalUI.reprint();
    });

    displayWidget.addWidget(uriTextEntry, new ShelfOptions(new Size(30, FILL_NEEDED)));
    displayWidget.addWidget(contentTypeRolodex, new ShelfOptions(new Size(20, FILL_NEEDED)));
    displayWidget.addWidget(contentTagEntry, new ShelfOptions(new Size(20, FILL_NEEDED)));

    setDisplayWidget(displayWidget);

    getBaseWidget().setUnfocusedAttribute(Attribute.INVERSE_ON);
    getBaseWidget().setFocusedAttribute(Attribute.INVERSE_ON);
  }

  public String getUri() {
    return uriTextEntry.getText();
  }

  public ContentType getContentType() {
    return contentTypeRolodex.getSelectedItem();
  }

  public String getContentTag() {
    return contentTagEntry.getText();
  }

  public void clear() {
    uriTextEntry.setText("");
    contentTagEntry.setText("");
    contentTypeRolodex.setSelectedIndex(0);
  }
}
