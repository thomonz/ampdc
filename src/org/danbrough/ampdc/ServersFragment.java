package org.danbrough.ampdc;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.danbrough.ampdc.mpd.MpdServer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

public class ServersFragment extends SherlockListFragment {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(ServersFragment.class.getSimpleName());
  View view;
  Ampdc ampd;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    log.error("onActivityCreated()");
    super.onActivityCreated(savedInstanceState);
    ampd = (Ampdc) getActivity();

    List<MpdServer> servers = ampd.getMpdService().getDatabase().getServers();
    Collections.sort(servers, new Comparator<MpdServer>() {

      @Override
      public int compare(MpdServer lhs, MpdServer rhs) {
        return rhs.getLastAccessed() - lhs.getLastAccessed();
      }
    });

    ArrayAdapter<MpdServer> adapter = new ArrayAdapter<MpdServer>(
        getActivity(), android.R.layout.simple_list_item_1, servers);
    setListAdapter(adapter);
  }

  @Override
  public void onResume() {
    log.info("onResume()");
    super.onResume();
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    MpdServer server = (MpdServer) getListAdapter().getItem(position);
    log.info("selected : {}", server);
    ampd.getMpdService().connectTo(server);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    log.debug("onCreateView() {}", this);
    view = inflater.inflate(R.layout.servers, container, false);
    return view;
  }

}
