package org.danbrough.ampdc;

import org.danbrough.ampdc.mpd.MpdFile;
import org.danbrough.ampdc.mpd.MpdListenerAdapter;
import org.danbrough.ampdc.mpd.PlaylistItem;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class PlaylistFragment extends SherlockListFragment {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(PlaylistFragment.class.getSimpleName());

  private static final int MENU_CLEAR_PLAYLIST = 1;

  MpdService mpd;
  int selectedPosition = -1;
  int longClickPosition = -1;
  boolean selectedViaClick = false;

  MpdListenerAdapter mpdListener = new MpdListenerAdapter() {
    @Override
    public void onChangedSong() {
      PlaylistFragment.this.onChangedSong();
    }

    @Override
    public void onChangedPlaylist() {
      PlaylistFragment.this.onChangedPlaylist();
    }
  };

  ArrayAdapter<PlaylistItem> adapter;

  @Override
  public void onActivityCreated(final Bundle savedInstanceState) {
    log.error("onActivityCreated()");
    super.onActivityCreated(savedInstanceState);
    Ampdc ampd = (Ampdc) getActivity();
    this.mpd = ampd.getMpdService();
    setEmptyText(getString(R.string.empty_playlist));

    adapter = new ArrayAdapter<PlaylistItem>(getActivity(),
        R.layout.playlist_item) {
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        // log.trace("getView() pos: " + position + " "
        // + getListView().getCheckedItemPosition());
        View view = convertView;
        if (view == null) {
          LayoutInflater inflater = getLayoutInflater(savedInstanceState);
          view = inflater.inflate(R.layout.playlist_item, parent, false);
        }

        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        TextView text2 = (TextView) view.findViewById(android.R.id.text2);
        ImageView checkedIcon = (ImageView) view
            .findViewById(android.R.id.checkbox);

        PlaylistItem item = getItem(position);
        text1.setText(item.getTitle());
        text2.setText("from " + item.getAlbum() + " by " + item.getArtist());

        boolean checked = selectedPosition == position;

        if (checkedIcon != null) {
          checkedIcon.setVisibility(checked ? View.VISIBLE : View.GONE);
        }
        view.setBackgroundColor(checked ? getResources().getColor(
            R.color.holo_blue_dark) : Color.TRANSPARENT);

        return view;

      }
    };
    adapter.setNotifyOnChange(false);
    setListAdapter(adapter);

    getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

      @Override
      public boolean onItemLongClick(AdapterView<?> parent, View view,
          int position, long id) {
        longClickPosition = position;
        return false;
      }

    });

    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
      getListView().setSelector(R.drawable.blue);
    }

    getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    registerForContextMenu(getListView());

  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenuInfo menuInfo) {
    if (!mpd.getPlaylist().isEmpty()) {
      menu.add(Menu.NONE, MENU_CLEAR_PLAYLIST, Menu.NONE,
          R.string.menuClearPlaylist);
    }
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case MENU_CLEAR_PLAYLIST:
      mpd.clearPlaylist();
      break;
    }
    return true;
  }

  protected void onChangedPlaylist() {
    log.debug("onChangedPlaylist()");
    getActivity().runOnUiThread(new Runnable() {

      @Override
      public void run() {
        adapter.clear();
        for (PlaylistItem item : mpd.getPlaylist())
          adapter.add(item);
        adapter.notifyDataSetChanged();
      }
    });
  }

  protected void onChangedSong() {
    final MpdFile file = mpd.getCurrentSong();
    log.trace("onChangedSong() {}", file);
    if (file == null) {
      selectedPosition = -1;
      return;
    }

    selectedPosition = file.getPosition();

    getActivity().runOnUiThread(new Runnable() {

      @Override
      public void run() {
        getListView().setItemChecked(selectedPosition, true);
        if (!selectedViaClick) {
          setSelection(selectedPosition);
        } else {
          selectedViaClick = false;
        }
      }
    });
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    log.trace("onListItemClick() pos:{} id:{}", position, id);
    mpd.seekTo(position, 0);
    selectedPosition = position;
    selectedViaClick = true;
    getListView().setItemChecked(position, true);

  }

  @Override
  public void onResume() {
    super.onResume();

    onChangedPlaylist();
    onChangedSong();
    mpd.addMpdListener(mpdListener);

  }

  @Override
  public void onPause() {
    mpd.removeMpdListener(mpdListener);
    adapter.clear();
    adapter.notifyDataSetChanged();
    super.onPause();
  }
}
