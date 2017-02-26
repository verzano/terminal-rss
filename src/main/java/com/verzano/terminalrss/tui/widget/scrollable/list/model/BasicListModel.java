package com.verzano.terminalrss.tui.widget.scrollable.list.model;

import com.verzano.terminalrss.tui.TUIStringable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class BasicListModel<T extends TUIStringable> implements ListModel<T> {
  private List<T> items;

  public BasicListModel(Collection<T> items) {
    setItems(items);
  }

  @Override
  public boolean addItem(T item) {
    return items.add(item);
  }

  @Override
  public boolean removeItem(T item) {
    return items.remove(item);
  }

  @Override
  public T getItemAt(int index) {
    return items.get(index);
  }

  @Override
  public int getItemIndex(T item) {
    return items.indexOf(item);
  }

  @Override
  public Collection<T> getItems() {
    return items;
  }

  @Override
  public void setItems(Collection<T> items) {
    this.items = new LinkedList<>(items);
  }

  @Override
  public int getItemCount() {
    return items.size();
  }
}