package com.sinergise.geopedia.pro.client.ui.table;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * This class has been copied from GWT Action Cell to include "title" attribute on button
 *
 * @param <C> the type that this Cell represents
 */
public class GPDActionCell<C> extends AbstractCell<C> {

  /**
   * The delegate that will handle events from the cell.
   *
   * @param <T> the type that this delegate acts on
   */
  public static interface Delegate<T> {
    /**
     * Perform the desired action on the given object.
     *
     * @param object the object to be acted upon
     */
    void execute(T object);
  }

  private final SafeHtml html;
  private final Delegate<C> delegate;

  /**
   * Construct a new {@link GPDActionCell}.
   *
   * @param message the message to display on the button
   * @param delegate the delegate that will handle events
   */
  public GPDActionCell(SafeHtml message,String title, Delegate<C> delegate) {
    super("click", "keydown");
    this.delegate = delegate;
    this.html = new SafeHtmlBuilder().appendHtmlConstant(
        "<button type=\"button\" tabindex=\"-1\" title=\""+title+"\">").append(message).appendHtmlConstant(
        "</button>").toSafeHtml();
  }

  /**
   * Construct a new {@link GPDActionCell} with a text String that does not contain
   * HTML markup.
   *
   * @param text the text to display on the button
   * @param delegate the delegate that will handle events
   */
  public GPDActionCell(String text, String title, Delegate<C> delegate) {
    this(SafeHtmlUtils.fromString(text), title, delegate);
  }

  @Override
  public void onBrowserEvent(Context context, Element parent, C value,
      NativeEvent event, ValueUpdater<C> valueUpdater) {
    super.onBrowserEvent(context, parent, value, event, valueUpdater);
    if ("click".equals(event.getType())) {
      EventTarget eventTarget = event.getEventTarget();
      if (!Element.is(eventTarget)) {
        return;
      }
      if (parent.getFirstChildElement().isOrHasChild(Element.as(eventTarget))) {
        onEnterKeyDown(context, parent, value, event, valueUpdater);
      }
    }
  }

  @Override
  public void render(Context context, C value, SafeHtmlBuilder sb) {
    sb.append(html);
  }

  @Override
  protected void onEnterKeyDown(Context context, Element parent, C value,
      NativeEvent event, ValueUpdater<C> valueUpdater) {
    delegate.execute(value);
  }
}
