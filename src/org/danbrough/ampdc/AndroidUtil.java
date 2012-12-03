package org.danbrough.ampdc;

import android.content.Context;
import android.content.res.Configuration;

public final class AndroidUtil {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(AndroidUtil.class.getSimpleName());

  public enum ScreenLayout {
    UNKNOWN, SMALL, NORMAL, LARGE, XLARGE;
  }

  private static AndroidUtil INSTANCE;
  private final Context context;
  private ScreenLayout screenLayout = null;

  public AndroidUtil(Context context) {
    this.context = context;
  }

  public ScreenLayout getScreenLayout() {
    if (screenLayout != null)
      return screenLayout;
    int i = context.getResources().getConfiguration().screenLayout;

    switch (i & Configuration.SCREENLAYOUT_SIZE_MASK) {
    case Configuration.SCREENLAYOUT_SIZE_SMALL:
      log.debug("SCREENLAYOUT_SIZE_SMALL");
      screenLayout = ScreenLayout.SMALL;
      break;
    case Configuration.SCREENLAYOUT_SIZE_NORMAL:
      log.debug("SCREENLAYOUT_SIZE_NORMAL");
      screenLayout = ScreenLayout.NORMAL;
      break;
    case Configuration.SCREENLAYOUT_SIZE_LARGE:
      log.debug("SCREENLAYOUT_SIZE_LARGE");
      screenLayout = ScreenLayout.LARGE;
      break;
    case Configuration.SCREENLAYOUT_SIZE_XLARGE:
      log.debug("SCREENLAYOUT_SIZE_XLARGE");
      screenLayout = ScreenLayout.XLARGE;
      break;
    default:
      log.warn("unknown screen layout: " + i);
      screenLayout = ScreenLayout.NORMAL;
      break;
    }
    return screenLayout;
  }

  public static AndroidUtil getInstance(Context context) {
    if (INSTANCE != null)
      return INSTANCE;
    INSTANCE = new AndroidUtil(context);
    return INSTANCE;
  }

}
