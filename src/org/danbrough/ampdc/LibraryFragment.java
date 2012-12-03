package org.danbrough.ampdc;

import java.util.HashMap;
import java.util.List;

import org.danbrough.ampdc.Ampdc.LibraryMode;
import org.danbrough.ampdc.mpd.MpdDirectory;
import org.danbrough.ampdc.mpd.MpdFile;
import org.danbrough.ampdc.mpd.MpdNode;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class LibraryFragment extends SherlockListFragment {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(LibraryFragment.class.getSimpleName());

  public static final int ADD_TO_PLAYLIST = 1;
  public static final int ADD_AND_PLAY = 2;
  public static final int REPLACE_IN_PLAYLIST = 3;
  public static final int REPLACE_AND_PLAY = 4;

  private static final String ATTR_LAST_SCROLL_POS = "lastScrollPos";
  private static final String ATTR_LAST_MODE = "lastMode";
  private static final String ATTR_LAST_PATH = "lastPath";

  Spinner spinnerMode;

  MpdService mpd;

  LibraryMode mode = LibraryMode.DIRECTORIES;
  ArrayAdapter<MpdNode> adapter;
  ImageButton btnUp;
  HashMap<String, Integer> scrollStateCache = new HashMap<String, Integer>();
  MpdDirectory currentDir;
  int playlistLoadSize = 100;
  int longClickPosition;
  String lastDirectory;
  String lastArtist;
  String lastAlbum;
  String lastPlaylist;
  int firstVisiblePosition;

  @Override
  public View onCreateView(final LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    log.debug("onCreateView() {}", this);

    Ampdc ampd = (Ampdc) getActivity();
    this.mpd = ampd.getMpdService();

    View view = inflater.inflate(R.layout.library, container, false);

    btnUp = (ImageButton) view.findViewById(R.id.btnUp);

    btnUp.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        showDirectory(new MpdDirectory(currentDir.getParentPath()));
      }
    });

    ListView listView = (ListView) view.findViewById(android.R.id.list);

    listView.setOnItemLongClickListener(new OnItemLongClickListener() {

      @Override
      public boolean onItemLongClick(AdapterView<?> parent, View view,
          int position, long id) {
        longClickPosition = position;
        return false;
      }

    });

    listView.setOnScrollListener(new OnScrollListener() {

      @Override
      public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == 0 && currentDir != null) {
          scrollStateCache.put(currentDir.getPath(),
              firstVisiblePosition = view.getFirstVisiblePosition());
        }
      }

      @Override
      public void onScroll(AbsListView view, int firstVisibleItem,
          int visibleItemCount, int totalItemCount) {
      }
    });
    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    registerForContextMenu(listView);

    adapter = new ArrayAdapter<MpdNode>(getActivity(), R.layout.library_item) {
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        // log.trace("getView() pos: " + position + " "
        // + getListView().getCheckedItemPosition());
        View view = convertView;
        if (view == null) {
          view = inflater.inflate(R.layout.library_item, parent, false);
        }

        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        ImageView icon = (ImageView) view.findViewById(android.R.id.icon);

        MpdNode node = getItem(position);

        text1.setText(node.getTitle());
        icon.setImageResource(node instanceof MpdFile ? R.drawable.audiofile
            : R.drawable.folder);

        return view;

      }
    };

    adapter.setNotifyOnChange(false);
    setListAdapter(adapter);

    spinnerMode = (Spinner) view.findViewById(R.id.spinnerLibraryMode);

    final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
        getActivity(), android.R.layout.simple_spinner_item);

    spinnerAdapter.add(getString(R.string.library_directories));
    spinnerAdapter.add(getString(R.string.library_artists));
    spinnerAdapter.add(getString(R.string.library_playlists));

    spinnerAdapter
        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    spinnerMode.setAdapter(spinnerAdapter);

    spinnerMode.setOnItemSelectedListener(new OnItemSelectedListener() {

      @Override
      public void onItemSelected(AdapterView<?> parent, View view,
          int position, long id) {

        if (position == LibraryMode.DIRECTORIES.ordinal()) {
          loadDirectories();
        } else if (position == LibraryMode.ARTISTS.ordinal()) {
          loadArtists();
        } else if (position == LibraryMode.PLAYLISTS.ordinal()) {
          loadPlaylists();
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    currentDir = new MpdDirectory("/");

    return view;

  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    log.debug("onListItemClick {}:{}", position, id);
    MpdNode node = adapter.getItem(position);
    if (node instanceof MpdFile) {
      log.warn("clicked on file: " + ((MpdFile) node).getTitle());
    } else {
      log.debug("clicked on {}", node);
    }
    if (node instanceof MpdDirectory) {
      showDirectory((MpdDirectory) node);
    }
  }

  private void loadDirectories() {
    log.info("loadDirectories()");
    mode = LibraryMode.DIRECTORIES;

    adapter.clear();
    getActivity().runOnUiThread(new Runnable() {

      @Override
      public void run() {
        showDirectory(currentDir);
      }
    });
  }

  protected void showDirectory(final MpdDirectory dir) {
    log.debug("showDirectory() {} ", dir);
    if (dir == null) {
      log.error("directory is null");
      return;
    }

    currentDir = dir;

    mpd.listDirectory(dir, new Callback<Void>() {

      @Override
      public void onError(Exception e) {
      }

      @Override
      public void callback(Void v) {

        getActivity().runOnUiThread(new Runnable() {

          @Override
          public void run() {
            String path = dir.getPath();
            btnUp.setVisibility(path.equals("/") ? View.GONE : View.VISIBLE);

            getActivity().setTitle(path.equals("/") ? "" : path);
            adapter.clear();

            for (MpdNode node : dir.getChildren()) {
              adapter.add(node);
            }

            Integer firstVisiblePosition = scrollStateCache.get(currentDir
                .getPath());
            final int selection = firstVisiblePosition != null ? firstVisiblePosition
                : 0;

            adapter.notifyDataSetChanged();
            getListView().postDelayed(new Runnable() {

              @Override
              public void run() {
                log.debug("setting selection to: " + selection + " for  path: "
                    + currentDir.getPath());
                getListView().setSelection(selection);
              }
            }, 10);

          }
        });
      }
    });

  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);

    menu.add(Menu.NONE, ADD_TO_PLAYLIST, 0, R.string.menuAddToPlaylist);
    menu.add(Menu.NONE, ADD_AND_PLAY, 1, R.string.menuAddToPlaylistAndPlay);
    menu.add(Menu.NONE, REPLACE_IN_PLAYLIST, 2, R.string.menuReplaceInPlaylist);
    menu.add(Menu.NONE, REPLACE_AND_PLAY, 3,
        R.string.menuReplaceInPlaylistAndPlay);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    log.trace("onContextItemSelected: {}", item);
    MpdNode node = adapter.getItem(longClickPosition);
    log.debug("selection: " + node.getPath());

    if (longClickPosition != -1) {
      longClickPosition = -1;
      switch (item.getItemId()) {
      case ADD_TO_PLAYLIST:
        mpd.addToPlaylist(node, false, false);
        break;
      case ADD_AND_PLAY:
        mpd.addToPlaylist(node, true, false);
        break;
      case REPLACE_IN_PLAYLIST:
        mpd.addToPlaylist(node, false, true);
        break;
      case REPLACE_AND_PLAY:
        mpd.addToPlaylist(node, true, true);
        break;
      }
    }

    return true;
  }

  protected void loadArtists() {
    log.info("loadArtists()");
    mode = LibraryMode.ARTISTS;

    currentDir = new MpdDirectory("/");
    mpd.listArtists(new Callback<List<String>>() {

      @Override
      public void onError(Exception e) {
      }

      @Override
      public void callback(final List<String> artists) {
        getListView().post(new Runnable() {

          @Override
          public void run() {
            adapter.clear();
            for (String artist : artists) {
              log.trace("artist: " + artist);
              MpdDirectory artistDir = new MpdDirectory(artist);
              adapter.add(artistDir);
            }
            adapter.notifyDataSetChanged();
          }
        });
      }
    });
  }

  protected void loadPlaylists() {
    log.info("loadPlaylists()");
    mode = LibraryMode.PLAYLISTS;
  }

  @Override
  public void onPause() {
    super.onPause();

    Editor editor = getActivity().getSharedPreferences(getClass().getName(),
        Context.MODE_PRIVATE).edit();

    editor.putString(ATTR_LAST_PATH, currentDir.getPath());
    editor.putString(ATTR_LAST_MODE, mode.name());
    editor.putString(ATTR_LAST_SCROLL_POS, firstVisiblePosition + "");

    editor.commit();
  }

  @Override
  public void onResume() {
    super.onResume();
    log.debug("onResume()");

    SharedPreferences prefs = getActivity().getSharedPreferences(
        getClass().getName(), Context.MODE_PRIVATE);

    currentDir = new MpdDirectory(prefs.getString(ATTR_LAST_PATH, "/"));

    firstVisiblePosition = Integer.parseInt(prefs.getString(
        ATTR_LAST_SCROLL_POS, "0"));
    log.debug("firstViisble pos: " + firstVisiblePosition);
    scrollStateCache.put(currentDir.getPath(), firstVisiblePosition);

    mode = LibraryMode.valueOf(prefs.getString(ATTR_LAST_MODE,
        LibraryMode.DIRECTORIES.name()));

    spinnerMode.setSelection(mode.ordinal());

  }
}
