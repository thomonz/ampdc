package org.danbrough.ampd.mpd;

import java.io.Serializable;
import java.util.HashMap;

public class AttributeBean implements Serializable {

  private static final long serialVersionUID = -7220123454082512776L;

  public interface AttributeChangeListener {
    void onAttributeChanged(Object source, String name, String value,
        String oldValue);
  }

  protected HashMap<String, String> attrs = new HashMap<String, String>();

  public AttributeBean() {
    super();
  }

  public final void setAttribute(String name, String value) {

    String oldValue = null;

    if (value == null) {
      oldValue = this.attrs.remove(name);
    } else {
      oldValue = this.attrs.put(name, value);
    }

    if ((value == null && oldValue != null)
        || (value != null && !value.equals(oldValue))) {
      onAttributeChanged(name, value, oldValue);
    }

  }

  protected void onAttributeChanged(String name, String value, String oldValue) {

  }

  public final String getAttribute(String name, String defaultValue) {
    String value = attrs.get(name);
    return value == null ? defaultValue : value;
  }

  public final int getIntegerAttribute(String name, int defaultValue) {
    String value = attrs.get(name);
    if (value == null)
      return defaultValue;
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {

    }
    return defaultValue;
  }

  public final float getFloatAttribute(String name, float defaultValue) {
    String value = attrs.get(name);
    if (value == null)
      return defaultValue;
    try {
      return Float.parseFloat(value);
    } catch (NumberFormatException e) {

    }
    return defaultValue;
  }

  public final double getDoubleAttribute(String name, double defaultValue) {
    String value = attrs.get(name);
    if (value == null)
      return defaultValue;
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException e) {

    }
    return defaultValue;
  }

  public final boolean getBooleanAttribute(String name, boolean defaultValue) {
    return Boolean.valueOf(attrs.get(name));
  }

  public void clearAttributes() {
    attrs.clear();
  }
}
