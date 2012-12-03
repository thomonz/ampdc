/**
 * Ampd is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Ampd is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Ampd.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.danbrough.ampd;

import org.danbrough.ampd.covers.CoverService;
import org.danbrough.ampd.lyrics.LyricsService;

/**
 * 
 * @author dan
 * 
 */
public interface Ampd {
  enum MpdView {
    INFO, LIBRARY, CONTROLS, PLAYLIST, SERVERS;
  }

  enum LibraryMode {
    DIRECTORIES, ARTISTS, PLAYLISTS;
  }

  MpdService getMpdService();

  CoverService getCoverService();

  LyricsService getLyricsService();

  void openView(MpdView view);

}
